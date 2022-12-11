package servers;

import Globals.SignedData;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import interfaceRMI.IRegistar;
import io.jsondb.annotation.Document;
import io.jsondb.annotation.Id;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;
import users.CateringFacility;

import javax.crypto.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.bouncycastle.util.encoders.Hex.decode;
import static org.bouncycastle.util.encoders.Hex.toHexString;



public class Registar extends UnicastRemoteObject implements IRegistar {

    private int id;

    private HashMap<String, SignedData[]> users;
    private HashMap<CateringFacility , SecretKey> secretKeys;

    private HashMap<LocalDate , List<String>> facilitySynonyms;

    private KeyGenerator kg;

    private Signature signature;

    private KeyPair keyPairSign;

    private DateTimeFormatter dtf;

    private static final int DAILYTOKENCOUNT = 48;

    public Registar(int id) throws Exception {
        this.id = id;
        kg = KeyGenerator.getInstance("HmacSHA256");
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairSign = keyPairGenerator.generateKeyPair();
        signature = Signature.getInstance("SHA256withRSA");

        users = new HashMap<>();
        secretKeys = new HashMap<>();
        dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        this.facilitySynonyms = new HashMap<>();
    }

    public Registar(InstanceRegistar instanceRegistar) throws Exception{
        kg = KeyGenerator.getInstance("HmacSHA256");
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairSign = keyPairGenerator.generateKeyPair();
        signature = Signature.getInstance("SHA256withRSA");

        this.id = instanceRegistar.getId();
        this.users = instanceRegistar.getUsers();
        this.secretKeys = new HashMap<>();
        this.dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        this.facilitySynonyms = (HashMap<LocalDate, List<String>>) getFacilitySynonymsLocalDate(instanceRegistar.getFacilitySynonyms());
    }

    public Registar() throws Exception
    {
        kg = KeyGenerator.getInstance("HmacSHA256");
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairSign = keyPairGenerator.generateKeyPair();
        signature = Signature.getInstance("SHA256withRSA");

        users = new HashMap<>();
        secretKeys = new HashMap<>();
        dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        this.facilitySynonyms = new HashMap<>();
    }


    @Override
    @JsonIgnore
    public String enrollCF(CateringFacility cf) throws NoSuchAlgorithmException {
        // Send a batch of day specific pseudonyms
        secretKeys.putIfAbsent(cf,kg.generateKey());
        byte[] derivedKey = getDerivedKey(cf);
        String nym = getFacilityNym(derivedKey, cf, LocalDate.now());

        if(!this.facilitySynonyms.containsKey(LocalDate.now())){
            this.facilitySynonyms.put(LocalDate.now(), new ArrayList<String>());
        }

        List<String> nyms = this.facilitySynonyms.get(LocalDate.now());
        nyms.add(nym);

        return nym;
    }

    @JsonIgnore
    public byte[] getDerivedKey(CateringFacility cf) throws NoSuchAlgorithmException {
        byte[] masterKey = secretKeys.get(cf).getEncoded();

        LocalDateTime localDate = LocalDate.now().atStartOfDay();
        byte[] date = dtf.format(localDate).getBytes(StandardCharsets.UTF_8);

        //KDF
        HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA256Digest());
        hkdf.init(new HKDFParameters(masterKey, cf.getBusinessId().getBytes(), date));
        byte[] key = new byte[2048];
        hkdf.generateBytes(key, 0, 2048 );

        return key;
    }

    @JsonIgnore
    private String getFacilityNym(byte[] derivedKey, CateringFacility cf, LocalDate day) throws NoSuchAlgorithmException {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        sha.update(derivedKey);
        sha.update(cf.getAddress().getBytes());
        sha.update(day.toString().getBytes());

        return toHexString(sha.digest());
    }
    

    @Override
    @JsonIgnore
    public SignedData[] enrollUser(String phoneNumber) throws Exception{

        // Generate & return the tokens it can use.
        SignedData[] tokens = new SignedData[DAILYTOKENCOUNT];
        signature.initSign(keyPairSign.getPrivate());
        SecureRandom s = new SecureRandom();

        // Sign using current day
        LocalDate localDate = LocalDate.now();

        for(int i = 0; i < DAILYTOKENCOUNT; i++){
            long r = s.nextLong();
            signature.update(longToBytes(r));
            LocalDateTime interval = localDate.atStartOfDay();
            signature.update(dtf.format(interval).getBytes(StandardCharsets.UTF_8));
            String token = toHexString(signature.sign());
            tokens[i] = new SignedData(token, r);
        }



        users.put(phoneNumber, tokens);
        return tokens;
    }

    @JsonIgnore
    private byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }

    @Override
    @JsonIgnore
    public Boolean validateToken(SignedData token) throws RemoteException, InvalidKeyException, SignatureException {
        byte[] tokenBytes = decode(token.getSignature());

        signature.initVerify(keyPairSign.getPublic());
        signature.update(longToBytes((long) token.getData()));
        LocalDate localDate = LocalDate.now();
        LocalDateTime interval = localDate.atStartOfDay();
        signature.update(dtf.format(interval).getBytes(StandardCharsets.UTF_8));

        return signature.verify(tokenBytes);
    }

    @Override
    @JsonIgnore
    public List<String> getNymsForDay(LocalDate day) throws RemoteException {

        if(!this.facilitySynonyms.containsKey(day)){
            throw new IllegalArgumentException("No nyms for day: "+ day.toString());
        }

        return this.facilitySynonyms.get(day);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @JsonIgnore
    public List<CateringFacility> getCateringFacilities() {
        return new ArrayList<>(secretKeys.keySet());
    }

    public HashMap<LocalDate, List<String>> getFacilitySynonyms() {
        return facilitySynonyms;
    }

    public HashMap<LocalDate, List<String>> getFacilitySynonymsLocalDate(HashMap<String, List<String>> facilitySynonymsStrings) {
        HashMap<LocalDate, List<String>> facilitySynonyms = new HashMap<>();
        for(Map.Entry<String, List<String>> entry : facilitySynonymsStrings.entrySet()) {
            facilitySynonyms.put(LocalDate.parse(entry.getKey(), dtf), entry.getValue());
        }
        return facilitySynonyms;
    }


    public List<Map.Entry<LocalDate, String>> getFacilitySynonymPairs() {
        List<Map.Entry<LocalDate, String>> facilitySynonymPairs = new ArrayList<>();
        for (Map.Entry<LocalDate, List<String>> entry : this.facilitySynonyms.entrySet()) {
            for(int i = 0; i < entry.getValue().size(); i++){
                Map.Entry<LocalDate, String> mapEntry = new AbstractMap.SimpleEntry<LocalDate, String>(entry.getKey(), entry.getValue().get(i));
                facilitySynonymPairs.add(mapEntry);
            }
        }
        return facilitySynonymPairs;
    }

    public SignedData[] getAssignedTokensPerUser(String phoneNumber) {
        return users.get(phoneNumber);
    }

    public List<String> getUserPhoneNumbers(){
        return new ArrayList<>(users.keySet());
    }


    public HashMap<String, SignedData[]> getUsers() {
        return users;
    }

    public HashMap<CateringFacility, SecretKey> getSecretKeys() {
        return secretKeys;
    }

    public KeyGenerator getKg() {
        return kg;
    }

    public Signature getSignature() {
        return signature;
    }

    public KeyPair getKeyPairSign() {
        return keyPairSign;
    }

    public DateTimeFormatter getDtf() {
        return dtf;
    }
}




    