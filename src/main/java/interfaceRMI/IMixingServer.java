package interfaceRMI;

import Globals.Capsule;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.SignatureException;

public interface IMixingServer extends Remote {
    public String receiveCapsule(Capsule capsule) throws RemoteException, SignatureException, InvalidKeyException;
}
