package users;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import interfaceRMI.IRegistar;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class User {
    private String phoneNumber;
    private String[] tokens;

    private IRegistar registar;

    public User(String phoneNumber){
        this.phoneNumber = phoneNumber;
        this.start();
    }

    public synchronized void start(){
        try {
            Registry myRegistry = LocateRegistry.getRegistry("localhost", 1099);
            registar = (IRegistar) myRegistry.lookup("Registar");

            String[] a = registar.entrolUser(this.phoneNumber);
            System.out.println(a[0]);
            System.out.println(readQRCode(new File("image.jpg").getPath()));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String readQRCode(String filePath) throws FileNotFoundException, IOException, NotFoundException {
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(
                new BufferedImageLuminanceSource(
                        ImageIO.read(new FileInputStream(filePath)))));
        Result qrCodeResult = new MultiFormatReader().decode(binaryBitmap);
        return qrCodeResult.getText();
    }
}
