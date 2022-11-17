package registar;

import interfaceRMI.IRegistar;
import users.CateringFacility;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.*;
import java.security.spec.AlgorithmParameterSpec;
import java.util.*;

import static java.awt.SystemColor.info;


public class Registar extends UnicastRemoteObject implements IRegistar {
    // Mapping between user and issued tokens
    private HashMap<String, Set<String> > users;
    private HashMap<CateringFacility , SecretKey> secretKeys;
    private HashMap<String , String[]> facilitySynonyms;

    private KeyGenerator kg;
    private Signature signature;
    private KeyPair keyPair;

    private static final int DAILYTOKENCOUNT = 48;

    protected Registar() throws RemoteException, NoSuchAlgorithmException {
        kg = KeyGenerator.getInstance("HmacSHA256");
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DSA");
        keyPair = keyPairGenerator.generateKeyPair();
        signature = Signature.getInstance("SHA256WithDSA");

        users = new HashMap<String, Set<String>>();
        secretKeys = new HashMap<>();

    }

    @Override
    public String[] EnrolCF(CateringFacility cf) {
        // Send a batch of day specific pseudonyms

        SecretKey s = kg.generateKey();
        secretKeys.put(cf, s);

        // KDF secret key

        // create batch of nym

        return null;
    }

    @Override
    public String[] EntrolUser(String phoneNumber) throws RemoteException, InvalidKeyException, SignatureException {
        users.put(phoneNumber, new HashSet<String>());

        // Generate & return the tokens it can use.
        String[] tokens = new String[DAILYTOKENCOUNT];
        SecureRandom s = new SecureRandom();
        signature.initSign(keyPair.getPrivate(), s);

        // Sign using current day
        signature.update("17/11/2022".getBytes(StandardCharsets.UTF_8));

        for(int i = 0; i < DAILYTOKENCOUNT; i++){
            String token = Base64.getEncoder().encodeToString(signature.sign());
            tokens[i] = token;
            users.get(phoneNumber).add(token);
        }

        return tokens;
    }

}
