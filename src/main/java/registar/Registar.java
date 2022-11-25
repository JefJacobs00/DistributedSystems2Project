package registar;

import interfaceRMI.IRegistar;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;
import users.CateringFacility;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.*;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;
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
    public String[] enrolCF(CateringFacility cf)  {
        // Send a batch of day specific pseudonyms

        SecretKey s = kg.generateKey();
        secretKeys.put(cf, s);
        return null;
    }

    public byte[] getPseudonyms(CateringFacility cf) throws NoSuchAlgorithmException {
        byte[] masterKey = secretKeys.get(cf).getEncoded();

        LocalDate localDate = LocalDate.now();
        byte[] date = dtf.format(localDate).getBytes(StandardCharsets.UTF_8);

        //KDF
        HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA256Digest());
        hkdf.init(new HKDFParameters(masterKey, cf.getBuisnessId().getBytes(), date));
        byte[] key = new byte[2048];
        hkdf.generateBytes(key, 0, 2048 );

        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        sha.update(key);
        sha.update(cf.getAddress().getBytes());
        sha.update(date);

        return sha.digest();
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
    