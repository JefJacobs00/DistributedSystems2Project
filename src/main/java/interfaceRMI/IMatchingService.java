package interfaceRMI;

import Globals.Capsule;
import Globals.CriticalTuples;
import Globals.SignedData;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public interface IMatchingService extends Remote {
    public void receiveInfectedUserLogs(SignedData signedData) throws RemoteException, NoSuchAlgorithmException;
    void receiveFlushedCapsules(List<Capsule> capsuleList) throws RemoteException;

    List<CriticalTuples> getCriticalTuples() throws RemoteException;
}
