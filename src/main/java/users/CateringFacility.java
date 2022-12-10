package users;

import Globals.QRValues;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import interfaceRMI.IRegistar;
import io.jsondb.annotation.Document;
import io.jsondb.annotation.Id;

import javax.imageio.ImageIO;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Objects;

import static org.bouncycastle.pqc.math.linearalgebra.ByteUtils.toHexString;

@Document(collection = "CateringFacilities", schemaVersion= "1.0")
public class CateringFacility implements java.io.Serializable {
    @Id
    private String businessId;
    private String name;
    private String address;
    private String phoneNumber;
    private BufferedImage qrCode;

    private String hostName;
    private int port;

    private IRegistar registar;

    public CateringFacility(String businessId, String name, String address, String phoneNumber, String hostName, int port) {
        this.businessId = businessId;
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.hostName = hostName;
        this.port = port;
    }

    public CateringFacility(){}

    @JsonIgnore
    public BufferedImage requestQrCode(){
        try {
            Registry myRegistry = LocateRegistry.getRegistry(this.hostName, this.port);
            registar = (IRegistar) myRegistry.lookup("Registar");
            String token = registar.enrollCF(this);
            return createQrInformation(token);
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
        QRValues qr = new QRValues(r, businessId, toHexString(hash));
        return qr.convertToImage();
    }

    @JsonGetter
    public String getBusinessId() {
        return businessId;
    }

    @JsonSetter
    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    @JsonGetter
    public String getName() {
        return name;
    }

    @JsonSetter
    public void setName(String name) {
        this.name = name;
    }

    @JsonGetter
    public String getAddress() {
        return address;
    }

    @JsonSetter
    public void setAddress(String address) {
        this.address = address;
    }

    @JsonGetter
    public String getPhoneNumber() {
        return phoneNumber;
    }

    @JsonSetter
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @JsonIgnore
    public BufferedImage getQrCode() {
        return qrCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CateringFacility that = (CateringFacility) o;
        return Objects.equals(businessId, that.businessId) && Objects.equals(name, that.name) && Objects.equals(address, that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(businessId, name, address);
    }
}
