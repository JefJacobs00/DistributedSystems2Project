package servers;

import Globals.Capsule;
import Globals.CriticalTuple;
import interfaceRMI.IRegistar;
import io.jsondb.annotation.Document;
import io.jsondb.annotation.Id;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "InstanceMatchingServer", schemaVersion= "1.0")
public class InstanceMatchingServer {
    @Id
    private int id;
    private List<Capsule> capsules;

    private List<String> uninformedTokens;
    private  List<CriticalTuple> criticalFacilities;

    public InstanceMatchingServer() {
        capsules = new ArrayList<>();
        criticalFacilities = new ArrayList<>();
        uninformedTokens = new ArrayList<>();
    }

    public InstanceMatchingServer(int id) {
        this.id = id;
        capsules = new ArrayList<>();
        criticalFacilities = new ArrayList<>();
        uninformedTokens = new ArrayList<>();
    }

    public InstanceMatchingServer(MatchingServer matchingServer) throws RemoteException {
        this.id = matchingServer.getId();
        this.capsules = matchingServer.getCapsules();
        this.criticalFacilities = matchingServer.getCriticalTuples();
        this.uninformedTokens = matchingServer.getUninformedTokens();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Capsule> getCapsules() {
        return capsules;
    }

    public void setCapsules(List<Capsule> capsules) {
        this.capsules = capsules;
    }

    public List<String> getUninformedTokens() {
        return uninformedTokens;
    }

    public void setUninformedTokens(List<String> uninformedTokens) {
        this.uninformedTokens = uninformedTokens;
    }

    public List<CriticalTuple> getCriticalFacilities() {
        return criticalFacilities;
    }

    public void setCriticalFacilities(List<CriticalTuple> criticalFacilities) {
        this.criticalFacilities = criticalFacilities;
    }
}
