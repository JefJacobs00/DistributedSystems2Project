package interfaceRMI;

import Globals.Capsule;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IMixingServer extends Remote {
    public String receiveCapsule(Capsule capsule) throws RemoteException;
}
