package mixingServer;

import interfaceRMI.IMixingServer;
import Globals.Capsule;
import interfaceRMI.IRegistar;

import javax.rmi.ssl.SslRMIClientSocketFactory;
import java.io.InvalidObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.*;
import java.util.ArrayList;

import static org.bouncycastle.pqc.math.linearalgebra.ByteUtils.toHexString;

public class MixingServer extends UnicastRemoteObject implements IMixingServer {
    private IRegistar registar;
    private ArrayList<String> spentTokens;

    private KeyPair keyPair;
    private Signature signature;

    public MixingServer() throws RemoteException, NotBoundException, NoSuchAlgorithmException, InvalidKeyException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPair = keyPairGenerator.generateKeyPair();
        signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(keyPair.getPrivate());


        Registry myRegistry = LocateRegistry.getRegistry("localhost", 1099);
        registar = (IRegistar) myRegistry.lookup("Registar");
        spentTokens = new ArrayList<>();
    }

    @Override
    public String receiveCapsule(Capsule capsule) throws RemoteException, SignatureException, InvalidKeyException {
        // Check validity (user token, day, spent)
        boolean isTokenValid = registar.validateToken(capsule.getUserToken());
        boolean isSpend = spentTokens.contains(capsule.getUserToken().getSignature());
        if(isTokenValid && !isSpend){
            spentTokens.add(capsule.getUserToken().getSignature());
            signature.update(capsule.getCfHash().getBytes());
            return toHexString(signature.sign());
        }

        return "Invalid token";
    }
}
