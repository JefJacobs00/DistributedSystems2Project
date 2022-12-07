package registar;

import interfaceRMI.IRegistar;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.bouncycastle.util.encoders.Hex;
import users.CateringFacility;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.bouncycastle.util.encoders.Hex.decode;
import static org.bouncycastle.util.encoders.Hex.toHexString;


public class Registar extends UnicastRemoteObject implements IRegistar {
    // Mapping between user and issued tokens
    private HashMap<String, String[]> users;
    private HashMap<CateringFacility , SecretKey> secretKeys;
    private HashMap<String , String[]> facilitySynonyms;

    private KeyGenerator kg;
    private Signature signature;
    private KeyPair keyPairSign;

    private SecretKeySpec keyAES;

    private DateTimeFormatter dtf;

    private static final int DAILYTOKENCOUNT = 48;

    protected Registar() throws Exception {
        kg = KeyGenerator.getInstance("HmacSHA256");
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairSign = keyPairGenerator.generateKeyPair();
        signature = Signature.getInstance("SHA256withRSA");

        users = new HashMap<String, String[]>();
        secretKeys = new HashMap<>();
        dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    }


    @Override
    public String enrollCF(CateringFacility cf) throws NoSuchAlgorithmException {
        // Send a batch of day specific pseudonyms
        secretKeys.putIfAbsent(cf,kg.generateKey());
        return toHexString(getDerivedKey(cf));
    }

    public byte[] getDerivedKey(CateringFacility cf) throws NoSuchAlgorithmException {
        byte[] masterKey = secretKeys.get(cf).getEncoded();

        LocalDateTime localDate = LocalDate.now().atStartOfDay();
        byte[] date = dtf.format(localDate).getBytes(StandardCharsets.UTF_8);

        //KDF
        HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA256Digest());
        hkdf.init(new HKDFParameters(masterKey, cf.getBusinessId().getBytes(), date));
        byte[] key = new byte[2048];
        hkdf.generateBytes(key, 0, 2048 );

        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        sha.update(key);
        sha.update(cf.getAddress().getBytes());
        sha.update(date);

        return sha.digest();
    }
    

    @Override
    public String[] enrollUser(String phoneNumber) throws Exception{

        // Generate & return the tokens it can use.
        String[] tokens = new String[DAILYTOKENCOUNT];
        signature.initSign(keyPairSign.getPrivate());

        // Sign using current day
        LocalDate localDate = LocalDate.now();



        for(int i = 0; i < DAILYTOKENCOUNT; i++){
            LocalDateTime interval = localDate.atStartOfDay().plusMinutes(30*i);
            signature.update(phoneNumber.getBytes());
            signature.update(dtf.format(interval).getBytes(StandardCharsets.UTF_8));
            String token = toHexString(signature.sign());
            tokens[i] = token;
        }



        users.put(phoneNumber, tokens);
        return tokens;
    }

    @Override
    public Boolean validateToken(String token) throws RemoteException, InvalidKeyException, SignatureException {
        byte[] tokenBytes = decode(token);

        signature.initVerify(keyPairSign.getPublic());
        LocalDateTime interval = LocalDateTime.now();
        interval = interval.truncatedTo(ChronoUnit.MINUTES).minusMinutes(interval.getMinute()%30);
        System.out.println(dtf.format(interval));
        signature.update(dtf.format(interval).getBytes(StandardCharsets.UTF_8));

        return signature.verify(tokenBytes);
    }




}




    