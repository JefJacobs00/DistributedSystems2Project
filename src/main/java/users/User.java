package users;

import Globals.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import interfaceRMI.ICentralHealthAuthority;
import interfaceRMI.IMatchingService;
import interfaceRMI.IMixingServer;
import interfaceRMI.IRegistar;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class User {

    private String phoneNumber;
    private SignedData[] tokens;

    private IRegistar registar;
    private IMixingServer mixingServer;
    private IMatchingService matchingService;
    private UserLog currentLog;
    private List<UserLog> logs;

    private int tokenIndex;

    public User(String phoneNumber){
        this.phoneNumber = phoneNumber;
        tokenIndex = 0;
        this.start();
        this.logs = new ArrayList<>();

        try {
            this.logs = loadFromFile();
        }catch (Exception e){}

    }

    public User(){}

    public synchronized void start(){
        try {
            Registry myRegistry = LocateRegistry.getRegistry("localhost", 1099);
            registar = (IRegistar) myRegistry.lookup("Registar");
            mixingServer = (IMixingServer) myRegistry.lookup("MixingServer");
            matchingService = (IMatchingService) myRegistry.lookup("MatchingServer");
            tokens = registar.enrollUser(this.phoneNumber);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public QRValues readQRCodeFromFilePath(String filePath) throws IOException, NotFoundException {
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(
                new BufferedImageLuminanceSource(
                        ImageIO.read(new FileInputStream(filePath)))));
        Result qrCodeResult = new MultiFormatReader().decode(binaryBitmap);
        String[] result = qrCodeResult.getText().split(";");

        return new QRValues(Long.decode(result[0]),result[1], result[2]);
    }

    public String visitFacility(BufferedImage qrcode) throws NotFoundException, SignatureException, RemoteException, InvalidKeyException {
        QRValues qr = readQRImage(qrcode);
        return sendCapsule(createCapsule(qr));
    }

    public boolean checkInfected() throws RemoteException {
        List<String> tokens = getInfectedTokens();
        for (String token : tokens) {
            mixingServer.sendInformedToken(token);
        }

        return tokens.size() > 0;
    }

    private void saveToFile(){
        JsonArray logs = new JsonArray();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (UserLog userLog: this.logs) {
            JsonObject jsonLog = new JsonObject();
            jsonLog.addProperty("cfHash",userLog.cfHash);
            jsonLog.addProperty("userToken",userLog.userToken);
            jsonLog.addProperty("hashRandomNumber",userLog.hashRandomNumber);
            jsonLog.addProperty("start", dtf.format(userLog.getVisitInterval().getStart()));
            jsonLog.addProperty("end", dtf.format(userLog.getVisitInterval().getEnd()));

            logs.add(jsonLog);
        }

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("logs_"+phoneNumber+".json"));
            writer.write(logs.toString());
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private List<UserLog> loadFromFile() throws IOException {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        JsonParser parser = new JsonParser();
        JsonArray jsonArray = (JsonArray) parser.parse(new FileReader("logs_"+phoneNumber+".json"));

        List<UserLog> userLogs = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject object = (JsonObject) jsonArray.get(i);

            LocalDateTime start = LocalDateTime.parse(object.get("start").getAsString(), dtf);
            LocalDateTime end = LocalDateTime.parse(object.get("end").getAsString(), dtf);
            TimeInterval interval = new TimeInterval(start,end);

            UserLog log = new UserLog(object.get("userToken").toString(),object.get("cfHash").toString(),object.get("hashRandomNumber").getAsLong(), interval);
            userLogs.add(log);
        }

        return userLogs;
    }


    private List<String> getInfectedTokens() throws RemoteException {
        List<CriticalTuple> criticalFacilities = matchingService.getCriticalTuples();
        List<String> tokens = new ArrayList<>();
        for (CriticalTuple criticalTuple: criticalFacilities) {
            String token = getTokenFromLogs(criticalTuple);
            if(token != null){
                tokens.add(token);
            }
        }

        return tokens;
    }


    private String getTokenFromLogs(CriticalTuple tuple){
        LocalDate day = tuple.getTimeInterval().getStart().toLocalDate();
        for (UserLog log: logs) {
            if (log.cfHash.equals(tuple.getCateringFacilityHash()) && log.visitInterval.hasOverlap(tuple.getTimeInterval()))
                return log.userToken;
        }

        return null;
    }



    public void sendLogs(String chaIdentifier) throws RemoteException, NotBoundException {
        Registry myRegistry = LocateRegistry.getRegistry("localhost", 1099);
        ICentralHealthAuthority cha = (ICentralHealthAuthority) myRegistry.lookup(chaIdentifier);
        cha.receiveUserLogs(logs);
    }

    public void leaveFacility(){
        currentLog.endVisitInterval(LocalDateTime.now());
        //Add to logs
        logs.add(currentLog);
        currentLog = null;
        saveToFile();
    }


    public QRValues readQRImage(BufferedImage qrcode) throws NotFoundException {
        LuminanceSource source = new BufferedImageLuminanceSource(qrcode);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        Result qrCodeResult = new MultiFormatReader().decode(bitmap);
        String[] result = qrCodeResult.getText().split(";");

        return new QRValues(Long.decode(result[0]),result[1], result[2]);
    }


    private Capsule createCapsule(QRValues qr){
        LocalDateTime time = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).minusMinutes(LocalDateTime.now().getMinute()%30);
        int index = (time.getHour()*60 + time.getMinute())/30;
        index = tokenIndex;
        this.currentLog = new UserLog(tokens[index].getSignature(),qr.getHash(),qr.getRandomNumber(),getCurrentTimeInterval());
        tokenIndex++;
        return new Capsule(getCurrentTimeInterval(),tokens[index],qr.getHash());
    }


    private String sendCapsule(Capsule capsule) throws SignatureException, RemoteException, InvalidKeyException {
        return mixingServer.receiveCapsule(capsule);
    }


    private TimeInterval getCurrentTimeInterval(){
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusMinutes(24*60/tokens.length);

        return new TimeInterval(start, end);
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public List<UserLog> getLogs() {
        return logs;
    }
    public String getUserStatus() throws RemoteException {
        return this.checkInfected() ? "Infected" : "Healthy";
    }
}
