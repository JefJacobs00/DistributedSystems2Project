package users;

import Globals.*;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import interfaceRMI.IMixingServer;
import interfaceRMI.IRegistar;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
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

public class User {
    private String phoneNumber;
    private SignedData[] tokens;

    private IRegistar registar;
    private IMixingServer mixingServer;

    private UserLog currentLog;

    private Map<LocalDate, List<UserLog>> logs;

    public User(String phoneNumber){
        this.phoneNumber = phoneNumber;
        this.start();
        this.logs = new HashMap<>();
    }

    public synchronized void start(){
        try {
            Registry myRegistry = LocateRegistry.getRegistry("localhost", 1099);
            registar = (IRegistar) myRegistry.lookup("Registar");
            mixingServer = (IMixingServer) myRegistry.lookup("MixingServer");
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



    //TODO export the logs of the user to a desired file
    public void exportLogs(String path){

    }

    public void leaveFacility(){
        currentLog.endVisitInterval(LocalDateTime.now());
        //Add to logs
        List logsToday = logs.get(LocalDate.now());
        if(logsToday == null)
            logs.put(LocalDate.now(), new ArrayList<>());
        logs.get(LocalDate.now()).add(currentLog);
        currentLog = null;
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

}
