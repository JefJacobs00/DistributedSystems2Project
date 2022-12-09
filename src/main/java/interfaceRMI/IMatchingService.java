package interfaceRMI;

import Globals.Capsule;
import Globals.CriticalTuple;
import Globals.SignedData;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public interface IMatchingService extends Remote {
    public void receiveInfectedUserLogs(SignedData signedData) throws RemoteException, NoSuchAlgorithmException;
    void receiveFlushedCapsules(List<Capsule> capsuleList) throws RemoteException;
    void receiveInformedToken(String token) throws RemoteException;

    List<CriticalTuple> getCriticalTuples() throws RemoteException;
}
