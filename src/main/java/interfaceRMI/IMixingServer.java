package interfaceRMI;

import users.Capsule;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IMixingServer extends Remote {
    public String receiveCapsule(Capsule capsule) throws RemoteException;
}