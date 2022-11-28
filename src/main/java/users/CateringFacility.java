package users;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import interfaceRMI.IRegistar;
import net.glxn.qrgen.javase.QRCode;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;

public class CateringFacility implements java.io.Serializable {
    private String buisnessId;
    private String name;
    private String address;
    private String phoneNumber;

    private String[] dayTokens;

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
            String[] test = registar.enrolCF(this);
            //information into json object --> to string to qr code
            BufferedImage image = generateQrCode("test"+buisnessId+"code");
            ImageIO.write(image, "jpg", new File("image.jpg"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private BufferedImage generateQrCode(String information) throws  IOException {
        ByteArrayOutputStream stream = QRCode.from(information).withSize(250, 250).stream();
        ByteArrayInputStream bis = new ByteArrayInputStream(stream.toByteArray());
        return ImageIO.read(bis);
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
}
