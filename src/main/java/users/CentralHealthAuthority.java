package users;

import Globals.SignedData;
import Globals.UserLog;
import interfaceRMI.ICentralHealthAuthority;
import interfaceRMI.IMatchingService;
import interfaceRMI.IRegistar;
import mixingServer.MatchingServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.bouncycastle.pqc.math.linearalgebra.ByteUtils.toHexString;

public class CentralHealthAuthority implements ICentralHealthAuthority {
    private Signature signature;
    private KeyPair keyPairSign;

    private IMatchingService matchingServer;
    public CentralHealthAuthority(String hostnameMatching, int portMatching ) throws NoSuchAlgorithmException, RemoteException, NotBoundException {
        signature = Signature.getInstance("SHA256withRSA");
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairSign = keyPairGenerator.generateKeyPair();

        Registry myRegistry = LocateRegistry.getRegistry("localhost", 1099);
        matchingServer = (IMatchingService) myRegistry.lookup("MatchingServer");
    }

    public void sendUserLogsToMatchingServer(List<UserLog> logs) throws IOException, InvalidKeyException, SignatureException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(logs);
        byte[] bytes = bos.toByteArray();

        signature.initSign(keyPairSign.getPrivate());
        signature.update(bytes);
        String signedLogs = toHexString(signature.sign());

        SignedData data = new SignedData(signedLogs, logs);

        matchingServer.receiveSignedUserLogs(data);
    }

    @Override
    public void receiveUserLogs(Map<LocalDate, List<UserLog>> logs) throws RemoteException {

    }
}
