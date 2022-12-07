package users;

import com.google.zxing.NotFoundException;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.InvalidKeyException;
import java.security.SignatureException;

import static org.bouncycastle.util.encoders.Hex.toHexString;

public class Main {
    public static void main(String[] args){
        CateringFacility cf = new CateringFacility("id1", "cf","somewhere", "0495366639" ,"localhost" , 1399 );
        User user = new User("0495366618");
        try {
            String result = user.visitFacility(cf.getQrCode());
            System.out.println(result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
