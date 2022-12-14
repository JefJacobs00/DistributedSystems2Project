package users;

import Globals.SignedData;
import Globals.UserLog;
import interfaceRMI.ICentralHealthAuthority;
import interfaceRMI.IMatchingService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.bouncycastle.pqc.math.linearalgebra.ByteUtils.toHexString;

public class CentralHealthAuthority extends UnicastRemoteObject implements ICentralHealthAuthority {
    private Signature signature;
    private KeyPair keyPairSign;

    private List<UserLog> logs;

    private IMatchingService matchingServer;
    public CentralHealthAuthority() throws NoSuchAlgorithmException, RemoteException, NotBoundException {
        signature = Signature.getInstance("SHA256withRSA");
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairSign = keyPairGenerator.generateKeyPair();
        this.logs = new ArrayList<>();
        Registry myRegistry = LocateRegistry.getRegistry("localhost", 1099);
        matchingServer = (IMatchingService) myRegistry.lookup("MatchingServer");
    }

    public String start() throws RemoteException, AlreadyBoundException {
        Registry registry = LocateRegistry.getRegistry(1099);
        SecureRandom s = new SecureRandom();
        String name = ""+s.nextInt();
        name = name.substring(name.length() > 5 ?name.length() -5: name.length());
        registry.bind(name, this);

        return name;
    }

    private void sendUserLogsToMatchingServer(List<UserLog> logs) throws IOException, InvalidKeyException, SignatureException, NoSuchAlgorithmException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(logs);
        byte[] bytes = bos.toByteArray();

        signature.initSign(keyPairSign.getPrivate());
        signature.update(bytes);
        String signedLogs = toHexString(signature.sign());

        SignedData data = new SignedData(signedLogs, logs);

        matchingServer.receiveInfectedUserLogs(data);
    }

    public void sendLogs() throws IOException, SignatureException, NoSuchAlgorithmException, InvalidKeyException {
        if (logs.isEmpty())
            return;

        sendUserLogsToMatchingServer(logs);
        logs.clear();
    }

    public List<UserLog> getUserLogs(){
        return this.logs;
    }

    public List<UserLog> getUserLogsList(){
        return logs;
    }

    @Override
    public void receiveUserLogs(List<UserLog> logs) throws RemoteException {
        this.logs = logs;
    }
}
