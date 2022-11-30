package Globals;

import net.glxn.qrgen.javase.QRCode;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class QRValues {
    private long randomNumber;
    private String facilityIdentifier;
    private String hash;

    public QRValues(long randomNumber, String facilityIdentifier, String hash) {
        this.randomNumber = randomNumber;
        this.facilityIdentifier = facilityIdentifier;
        this.hash = hash;
    }

    public BufferedImage convertToImage() throws IOException {
        return generateQrCode(randomNumber+";"+facilityIdentifier+";"+hash);
    }

    private BufferedImage generateQrCode(String information) throws IOException {
        ByteArrayOutputStream stream = QRCode.from(information).withSize(250, 250).stream();
        ByteArrayInputStream bis = new ByteArrayInputStream(stream.toByteArray());
        return ImageIO.read(bis);
    }

    public long getRandomNumber() {
        return randomNumber;
    }

    public void setRandomNumber(long randomNumber) {
        this.randomNumber = randomNumber;
    }

    public String getFacilityIdentifier() {
        return facilityIdentifier;
    }

    public void setFacilityIdentifier(String facilityIdentifier) {
        this.facilityIdentifier = facilityIdentifier;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
