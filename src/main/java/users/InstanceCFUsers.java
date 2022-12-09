package users;

import io.jsondb.annotation.Document;
import io.jsondb.annotation.Id;

import java.awt.image.BufferedImage;
@Document(collection = "instancesCFUsers", schemaVersion= "1.0")
public class InstanceCFUsers {
    @Id
    private String businessId;
    private String name;

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
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

    public void setQrCode(BufferedImage qrCode) {
        this.qrCode = qrCode;
    }

    private String address;
    private String phoneNumber;
    private BufferedImage qrCode;
}
