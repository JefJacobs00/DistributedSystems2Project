package interfaceRMI;

import Globals.SignedData;

import java.rmi.RemoteException;

public interface IMatchingService {
    public void receiveSignedUserLogs(SignedData signedData) throws RemoteException;
}
