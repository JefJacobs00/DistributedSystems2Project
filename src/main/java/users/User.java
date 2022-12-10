package users;

import Globals.*;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import interfaceRMI.ICentralHealthAuthority;
import interfaceRMI.IMatchingService;
import interfaceRMI.IMixingServer;
import interfaceRMI.IRegistar;
import io.jsondb.annotation.Document;
import io.jsondb.annotation.Id;
import io.jsondb.annotation.Secret;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.bouncycastle.util.encoders.Hex.toHexString;

@Document(collection = "users", schemaVersion= "1.0")
public class User {

    @Id
    private String phoneNumber;

    private String password;
    private SignedData[] tokens;

    private IRegistar registar;
    private IMixingServer mixingServer;
    private IMatchingService matchingService;
    private UserLog currentLog;
    private Map<LocalDate, List<UserLog>> logs;

    public User(String phoneNumber){
        this.phoneNumber = phoneNumber;
        this.start();
        this.logs = new HashMap<>();
    }

    public User(){}

    public synchronized void start(){
        try {
            Registry myRegistry = LocateRegistry.getRegistry("localhost", 1099);
            registar = (IRegistar) myRegistry.lookup("Registar");
            mixingServer = (IMixingServer) myRegistry.lookup("MixingServer");
            matchingService = (IMatchingService) myRegistry.lookup("MatchingServer");
            tokens = registar.enrollUser(this.phoneNumber);
            this.logs = new HashMap<>();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @JsonIgnore
    public QRValues readQRCodeFromFilePath(String filePath) throws IOException, NotFoundException {
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(
                new BufferedImageLuminanceSource(
                        ImageIO.read(new FileInputStream(filePath)))));
        Result qrCodeResult = new MultiFormatReader().decode(binaryBitmap);
        String[] result = qrCodeResult.getText().split(";");

        return new QRValues(Long.decode(result[0]),result[1], result[2]);
    }

    @JsonIgnore
    public String visitFacility(BufferedImage qrcode) throws NotFoundException, SignatureException, RemoteException, InvalidKeyException {
        QRValues qr = readQRImage(qrcode);
        return sendCapsule(createCapsule(qr));
    }

    @JsonIgnore
    public boolean checkInfected() throws RemoteException {
        List<String> tokens = getInfectedTokens();
        for (String token : tokens) {
            mixingServer.sendInformedToken(token);
        }

        return tokens.size() > 0;
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
        for (UserLog log: logs.get(day)) {
            if (log.cfHash.equals(tuple.getCateringFacilityHash()) && log.visitInterval.hasOverlap(tuple.getTimeInterval()))
                return log.userToken;
        }

        return null;
    }



    @JsonIgnore
    public void sendLogs(String chaIdentifier) throws RemoteException, NotBoundException {
        Registry myRegistry = LocateRegistry.getRegistry("localhost", 1099);
        ICentralHealthAuthority cha = (ICentralHealthAuthority) myRegistry.lookup(chaIdentifier);
        cha.receiveUserLogs(logs);
    }

    @JsonIgnore
    public void leaveFacility(){
        currentLog.endVisitInterval(LocalDateTime.now());
        //Add to logs
        List logsToday = logs.get(LocalDate.now());
        if(logsToday == null)
            logs.put(LocalDate.now(), new ArrayList<>());
        logs.get(LocalDate.now()).add(currentLog);
        currentLog = null;
    }

    @JsonIgnore

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

        this.currentLog = new UserLog(tokens[index].getSignature(),qr.getHash(),qr.getRandomNumber(),getCurrentTimeInterval());
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

    @JsonGetter
    public String getPhoneNumber() {
        return phoneNumber;
    }

    @JsonSetter
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @JsonGetter
    public String getPassword() {
        return password;
    }

    @JsonSetter
    public void setPassword(String password) {
        this.password = password;
    }
    public List<UserLog> getUserLogsList(){
        List<UserLog> userLogList = new ArrayList<>();
        for (List<UserLog> userLogsDay : logs.values()){
            userLogList.addAll(userLogsDay);
        }
        return userLogList;
    }

    public String getUserStatus() throws RemoteException {
        return this.checkInfected() ? "Infected" : "Healthy";
    }
}
