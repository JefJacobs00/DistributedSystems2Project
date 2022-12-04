package users;

import Globals.QRValues;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import interfaceRMI.IRegistar;
import net.glxn.qrgen.javase.QRCode;
import org.bouncycastle.util.Longs;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Map;

import static org.bouncycastle.pqc.math.linearalgebra.ByteUtils.toHexString;

public class CateringFacility implements java.io.Serializable {
    private String buisnessId;
    private String name;
    private String address;
    private String phoneNumber;

    private String[] dayTokens;

    private BufferedImage qrCode;

    private final String hostName;
    private final int port;

    private IRegistar registar;

    public CateringFacility(String buisnessId, String name, String address, String phoneNumber, String hostName, int port) {
        this.buisnessId = buisnessId;
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.hostName = hostName;
        this.port = port;

        start();
    }

    public void start(){
        try {
            Registry myRegistry = LocateRegistry.getRegistry(hostName, 1099);
            registar = (IRegistar) myRegistry.lookup("Registar");
            String token = registar.enrolCF(this);
            qrCode = createQrInformation(token);
            ImageIO.write(qrCode, "jpg", new File("image.jpg"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private BufferedImage createQrInformation(String nym) throws NoSuchAlgorithmException, IOException {
        SecureRandom s = new SecureRandom();
        long r = s.nextLong();
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(r);
        byte[] bytes = buffer.array();
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        sha.update(bytes);
        sha.update(nym.getBytes());
        byte[] hash = sha.digest();
        QRValues qr = new QRValues(r, buisnessId, toHexString(hash));
        return qr.convertToImage();
    }

    public String getBuisnessId() {
        return buisnessId;
    }

    public void setBuisnessId(String buisnessId) {
        this.buisnessId = buisnessId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public BufferedImage getQrCode() {
        return qrCode;
    }
}
