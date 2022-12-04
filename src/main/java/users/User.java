package users;

import Globals.Capsule;
import Globals.QRValues;
import Globals.TimeInterval;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import interfaceRMI.IMixingServer;
import interfaceRMI.IRegistar;
import mixingServer.MixingServer;

import javax.crypto.KeyAgreement;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static org.bouncycastle.util.encoders.Hex.toHexString;

public class User {
    private String phoneNumber;
    private String[] tokens;

    private IRegistar registar;
    private IMixingServer mixingServer;

    public User(String phoneNumber){
        this.phoneNumber = phoneNumber;
        this.start();
    }

    public synchronized void start(){
        try {
            Registry myRegistry = LocateRegistry.getRegistry("localhost", 1099);
            registar = (IRegistar) myRegistry.lookup("Registar");
            initSecureConnection();
            mixingServer = (IMixingServer) myRegistry.lookup("MixingServer");

            tokens = registar.entrolUser(this.phoneNumber);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public QRValues readQRCode(String filePath) throws IOException, NotFoundException {
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
    public void ExportLogs(String path){

    }

    private QRValues readQRImage(BufferedImage qrcode) throws NotFoundException {
        LuminanceSource source = new BufferedImageLuminanceSource(qrcode);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        Result qrCodeResult = new MultiFormatReader().decode(bitmap);
        String[] result = qrCodeResult.getText().split(";");

        return new QRValues(Long.decode(result[0]),result[1], result[2]);
    }

    private Capsule createCapsule(QRValues qr){
        LocalDateTime time = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).minusMinutes(LocalDateTime.now().getMinute()%30);
        int index = (time.getHour()*60 + time.getMinute())/30;

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

    private void initSecureConnection() throws Exception{
        KeyPair keyPairDH = createDHKey();
        byte[] keyA = registar.initSecureConnection(keyPairDH.getPublic().getEncoded());
        KeyAgreement keyAgreement = createKeyAgree(keyPairDH);
        byte[] sharedSecret = createSharedSecret(keyAgreement, keyA);

        System.out.println("Shared secret: " + toHexString(sharedSecret));
    }

    private KeyPair createDHKey() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DH");
        keyPairGenerator.initialize(2048);

        return keyPairGenerator.generateKeyPair();
    }

    private KeyAgreement createKeyAgree(KeyPair keyPair) throws Exception{
        KeyAgreement keyAgree = KeyAgreement.getInstance("DH");
        keyAgree.init(keyPair.getPrivate());

        return keyAgree;
    }

    private byte[] createSharedSecret(KeyAgreement keyAgreement, byte[] keyB) throws Exception{
        KeyFactory keyFactory = KeyFactory.getInstance("DH");

        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyB);

        PublicKey pubKeyB = keyFactory.generatePublic(x509KeySpec);
        keyAgreement.doPhase(pubKeyB, true);

        return keyAgreement.generateSecret();
    }


}
