package registar;

import interfaceRMI.IRegistar;
import users.CateringFacility;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.HashMap;
import java.util.Objects;

import static java.awt.SystemColor.info;


public class Registar extends UnicastRemoteObject implements IRegistar {
    private HashMap<String, String[] > users;
    private HashMap<CateringFacility, SecretKey> secretKeys;
    private HashMap<CateringFacility, String[]> facilitySynonyms;

    private KeyGenerator kg;

    private static final int DAILYTOKENCOUNT = 48;

    protected Registar() throws RemoteException, NoSuchAlgorithmException {
        kg = KeyGenerator.getInstance("HmacSHA256");
        users = new HashMap<String, String[]>();

    }

    @Override
    public String[] EnrolCF(CateringFacility cf) {
        // Send a batch of day specific pseudonyms

        SecretKey s = kg.generateKey();
        secretKeys.put(cf, s);

        // KDF secret key
        String[] nyms = new String[14];
        for (int i =0;i < nyms.length; i++){
            nyms[i] = "" + Objects.hash(s, cf.getAddress(),i);
        }


        return nyms;
    }

    @Override
    public String[] EntrolUser(String phoneNumber) throws RemoteException {
        users.put(phoneNumber, new String[DAILYTOKENCOUNT]);

        // Generate & return the tokens it can use.
        return null;
    }

}
