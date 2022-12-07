package users;

import Globals.SignedData;
import Globals.UserLog;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.*;
import java.util.List;

import static org.bouncycastle.pqc.math.linearalgebra.ByteUtils.toHexString;

public class CentralHealthAuthority {
    private Signature signature;
    private KeyPair keyPairSign;

    public CentralHealthAuthority() throws NoSuchAlgorithmException {
        signature = Signature.getInstance("SHA256withRSA");
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairSign = keyPairGenerator.generateKeyPair();
    }

    public void sendUserLogs(List<UserLog> logs) throws IOException, InvalidKeyException, SignatureException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(logs);
        byte[] bytes = bos.toByteArray();

        signature.initSign(keyPairSign.getPrivate());
        signature.update(bytes);
        String signedLogs = toHexString(signature.sign());

        SignedData data = new SignedData(signedLogs, logs);

        //Send this to the matching service
    }
}
