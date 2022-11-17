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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    private DateTimeFormatter dtf;

    private static final int DAILYTOKENCOUNT = 48;

    protected Registar() throws RemoteException, NoSuchAlgorithmException {
        kg = KeyGenerator.getInstance("HmacSHA256");
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DSA");
        keyPair = keyPairGenerator.generateKeyPair();
        signature = Signature.getInstance("SHA256WithDSA");

        users = new HashMap<String, Set<String>>();
        secretKeys = new HashMap<>();
        dtf = DateTimeFormatter.ofPattern("dd/MM/uuuu");

    }

    @Override
    public String[] enrolCF(CateringFacility cf) {
        // Send a batch of day specific pseudonyms

        SecretKey s = kg.generateKey();
        secretKeys.put(cf, s);

        // KDF secret key

        // create batch of nym

        return null;
    }

    @Override
    public String[] entrolUser(String phoneNumber) throws RemoteException, InvalidKeyException, SignatureException {
        users.put(phoneNumber, new HashSet<String>());

        // Generate & return the tokens it can use.
        String[] tokens = new String[DAILYTOKENCOUNT];
        SecureRandom s = new SecureRandom();
        signature.initSign(keyPair.getPrivate(), s);

        // Sign using current day
        LocalDate localDate = LocalDate.now();
        signature.update(dtf.format(localDate).getBytes(StandardCharsets.UTF_8));

        for(int i = 0; i < DAILYTOKENCOUNT; i++){
            String token = Base64.getEncoder().encodeToString(signature.sign());
            tokens[i] = token;
            users.get(phoneNumber).add(token);
        }

        return tokens;
    }

    @Override
    public PublicKey getPublicKey() throws RemoteException {
        return keyPair.getPublic();
    }


}
