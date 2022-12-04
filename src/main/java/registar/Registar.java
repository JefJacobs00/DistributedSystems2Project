package registar;

import interfaceRMI.IRegistar;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;
import users.CateringFacility;

import javax.crypto.*;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Array;
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
    public byte[] initSecureConnection(byte[] pubKey) throws RemoteException{
        try {
            System.out.println("Creating secure connection");
            KeyPair keyPairDH = createDHKey();
            KeyAgreement agreement = createKeyAgree(keyPairDH);
            byte[] sharedSecret = createSharedSecret(agreement, pubKey);
            System.out.println(toHexString(sharedSecret));


            return keyPairDH.getPublic().getEncoded();
        }catch (Exception e){
            throw new RemoteException("Something went wrong");
        }
    }


    private KeyPair createDHKey() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DH");
        keyPairGenerator.initialize(2048);

        return keyPairGenerator.generateKeyPair();
    }

    private KeyAgreement createKeyAgree(KeyPair keyPair) throws Exception{
        KeyAgreement keyAgree = KeyAgreement.getInstance("DH");
        keyAgree.init(keyPair.getPrivate());

        return keyAgree;
    }

    private byte[] createSharedSecret(KeyAgreement keyAgreement, byte[] keyB) throws Exception{
        KeyFactory keyFactory = KeyFactory.getInstance("DH");

        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyB);

        PublicKey pubKeyB = keyFactory.generatePublic(x509KeySpec);
        keyAgreement.doPhase(pubKeyB, true);

        return keyAgreement.generateSecret();
    }

    private void test() throws Exception {
        /*
         * Alice creates her own DH key pair with 2048-bit key size
         */
        System.out.println("ALICE: Generate DH keypair ...");
        KeyPairGenerator aliceKpairGen = KeyPairGenerator.getInstance("DH");
        aliceKpairGen.initialize(2048);
        KeyPair aliceKpair = aliceKpairGen.generateKeyPair();

        // Alice creates and initializes her DH KeyAgreement object
        System.out.println("ALICE: Initialization ...");
        KeyAgreement aliceKeyAgree = KeyAgreement.getInstance("DH");
        aliceKeyAgree.init(aliceKpair.getPrivate());

        // Alice encodes her public key, and sends it over to Bob.
        byte[] alicePubKeyEnc = aliceKpair.getPublic().getEncoded();

        /*
         * Let's turn over to Bob. Bob has received Alice's public key
         * in encoded format.
         * He instantiates a DH public key from the encoded key material.
         */
        KeyFactory bobKeyFac = KeyFactory.getInstance("DH");
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(alicePubKeyEnc);

        PublicKey alicePubKey = bobKeyFac.generatePublic(x509KeySpec);

        /*
         * Bob gets the DH parameters associated with Alice's public key.
         * He must use the same parameters when he generates his own key
         * pair.
         */
        DHParameterSpec dhParamFromAlicePubKey = ((DHPublicKey)alicePubKey).getParams();

        // Bob creates his own DH key pair
        System.out.println("BOB: Generate DH keypair ...");
        KeyPairGenerator bobKpairGen = KeyPairGenerator.getInstance("DH");
        bobKpairGen.initialize(dhParamFromAlicePubKey);
        KeyPair bobKpair = bobKpairGen.generateKeyPair();

        // Bob creates and initializes his DH KeyAgreement object
        System.out.println("BOB: Initialization ...");
        KeyAgreement bobKeyAgree = KeyAgreement.getInstance("DH");
        bobKeyAgree.init(bobKpair.getPrivate());

        // Bob encodes his public key, and sends it over to Alice.
        byte[] bobPubKeyEnc = bobKpair.getPublic().getEncoded();

        /*
         * Alice uses Bob's public key for the first (and only) phase
         * of her version of the DH
         * protocol.
         * Before she can do so, she has to instantiate a DH public key
         * from Bob's encoded key material.
         */
        KeyFactory aliceKeyFac = KeyFactory.getInstance("DH");
        x509KeySpec = new X509EncodedKeySpec(bobPubKeyEnc);
        PublicKey bobPubKey = aliceKeyFac.generatePublic(x509KeySpec);
        System.out.println("ALICE: Execute PHASE1 ...");
        aliceKeyAgree.doPhase(bobPubKey, true);

        /*
         * Bob uses Alice's public key for the first (and only) phase
         * of his version of the DH
         * protocol.
         */
        System.out.println("BOB: Execute PHASE1 ...");
        bobKeyAgree.doPhase(alicePubKey, true);

        /*
         * At this stage, both Alice and Bob have completed the DH key
         * agreement protocol.
         * Both generate the (same) shared secret.
         */
        byte[] aliceSharedSecret = aliceKeyAgree.generateSecret();
        int aliceLen = aliceSharedSecret.length;
        byte[] bobSharedSecret = new byte[aliceLen];
        int bobLen;
        bobLen = bobKeyAgree.generateSecret(bobSharedSecret, 0);
        System.out.println("Alice secret: " +
                toHexString(aliceSharedSecret));
        System.out.println("Bob secret: " +
                toHexString(bobSharedSecret));
        if (!java.util.Arrays.equals(aliceSharedSecret, bobSharedSecret))
            throw new Exception("Shared secrets differ");
        System.out.println("Shared secrets are the same");

        System.out.println("Use shared secret as SecretKey object ...");
        SecretKeySpec bobAesKey = new SecretKeySpec(bobSharedSecret, 0, 32, "AES");
        SecretKeySpec aliceAesKey = new SecretKeySpec(aliceSharedSecret, 0, 32, "AES");

        /*
         * Bob encrypts, using AES in CBC mode
         */
        Cipher bobCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        bobCipher.init(Cipher.ENCRYPT_MODE, bobAesKey);
        byte[] cleartext = "This is just an example".getBytes();
        byte[] ciphertext = bobCipher.doFinal(cleartext);

        // Retrieve the parameter that was used, and transfer it to Alice in
        // encoded format
        byte[] encodedParams = bobCipher.getParameters().getEncoded();

        /*
         * Alice decrypts, using AES in CBC mode
         */

        // Instantiate AlgorithmParameters object from parameter encoding
        // obtained from Bob
        AlgorithmParameters aesParams = AlgorithmParameters.getInstance("AES");
        aesParams.init(encodedParams);
        Cipher aliceCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        aliceCipher.init(Cipher.DECRYPT_MODE, aliceAesKey, aesParams);
        byte[] recovered = aliceCipher.doFinal(ciphertext);
        if (!java.util.Arrays.equals(cleartext, recovered))
            throw new Exception("AES in CBC mode recovered text is " +
                    "different from cleartext");
        System.out.println("AES in CBC mode recovered text is same as cleartext");
    }


    @Override
    public String enrolCF(CateringFacility cf) throws NoSuchAlgorithmException {
        // Send a batch of day specific pseudonyms

        SecretKey s = kg.generateKey();
        secretKeys.put(cf, s);
        return toHexString(getPseudonym(cf));
    }

    public byte[] getPseudonym(CateringFacility cf) throws NoSuchAlgorithmException {
        byte[] masterKey = secretKeys.get(cf).getEncoded();

        LocalDateTime localDate = LocalDate.now().atStartOfDay();
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
        // Generate & return the tokens it can use.
        String[] tokens = new String[DAILYTOKENCOUNT];
        signature.initSign(keyPairSign.getPrivate());

        // Sign using current day
        LocalDate localDate = LocalDate.now();



        for(int i = 0; i < DAILYTOKENCOUNT; i++){
            LocalDateTime interval = localDate.atStartOfDay().plusMinutes(30*i);
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




    