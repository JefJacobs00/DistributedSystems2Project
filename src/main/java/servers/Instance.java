package servers;

import Globals.Capsule;
import Globals.CriticalTuple;
import Globals.SignedData;
import interfaceRMI.IMatchingService;
import io.jsondb.annotation.Document;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Document(collection = "MatchingServer", schemaVersion = "1.0")
public class Instance extends UnicastRemoteObject implements IMatchingService {

    public String id;

    public Instance() throws Exception
    {}

    public Instance(String id) throws Exception{
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void receiveInfectedUserLogs(SignedData signedData) throws RemoteException, NoSuchAlgorithmException {

    }

    @Override
    public void receiveFlushedCapsules(List<Capsule> capsuleList) throws RemoteException {

    }

    @Override
    public void receiveInformedToken(String token) throws RemoteException {

    }

    @Override
    public List<CriticalTuple> getCriticalTuples() throws RemoteException {
        return null;
    }
}
