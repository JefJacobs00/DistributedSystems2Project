package mixingServer;

import Globals.SignedData;
import interfaceRMI.IMatchingService;
import interfaceRMI.IMixingServer;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class MatchingServer extends UnicastRemoteObject implements IMatchingService {

    public MatchingServer() throws RemoteException {
    }

    @Override
    public void receiveSignedUserLogs(SignedData signedData) throws RemoteException {

    }
}
