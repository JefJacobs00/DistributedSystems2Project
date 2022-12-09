package mixingServer;

import interfaceRMI.IMatchingService;
import interfaceRMI.IMixingServer;
import Globals.Capsule;
import interfaceRMI.IRegistar;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.bouncycastle.pqc.math.linearalgebra.ByteUtils.toHexString;

public class MixingServer extends UnicastRemoteObject implements IMixingServer {
    private IRegistar registar;
    private IMatchingService matchingService;
    private ArrayList<String> spentTokens;

    private List<Capsule> receivedCapsules;

    private KeyPair keyPair;
    private Signature signature;

    public MixingServer() throws RemoteException, NotBoundException, NoSuchAlgorithmException, InvalidKeyException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPair = keyPairGenerator.generateKeyPair();
        signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(keyPair.getPrivate());
        Registry myRegistry = LocateRegistry.getRegistry("localhost", 1099);
        registar = (IRegistar) myRegistry.lookup("Registar");
        matchingService = (IMatchingService) myRegistry.lookup("MatchingServer");
        spentTokens = new ArrayList<>();
        this.receivedCapsules = new ArrayList<>();
    }

    public List<Capsule> getReceivedCapsules() {
        return receivedCapsules;
    }

    @Override
    public String receiveCapsule(Capsule capsule) throws RemoteException, SignatureException, InvalidKeyException {
        capsule.getInterval().setStart(LocalDateTime.now());
        receivedCapsules.add(capsule);
        boolean isTokenValid = registar.validateToken(capsule.getUserToken());
        boolean isSpend = spentTokens.contains(capsule.getUserToken().getSignature());
        if(isTokenValid && !isSpend){
            spentTokens.add(capsule.getUserToken().getSignature());
            signature.update(capsule.getCfHash().getBytes());
            return toHexString(signature.sign());
        }
        return "Invalid token";
    }

    @Override
    public void sendInformedToken(String token) throws RemoteException {
        matchingService.receiveInformedToken(token);
    }

    public void flushCapsules() throws RemoteException {
        matchingService.receiveFlushedCapsules(receivedCapsules);
        receivedCapsules = new ArrayList<>();
        spentTokens = new ArrayList<>();
    }
}
