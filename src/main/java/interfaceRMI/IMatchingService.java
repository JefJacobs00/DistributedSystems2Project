package interfaceRMI;

import Globals.SignedData;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;

public interface IMatchingService extends Remote {
    public void receiveSignedUserLogs(SignedData signedData) throws RemoteException, NoSuchAlgorithmException;
}
