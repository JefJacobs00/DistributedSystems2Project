package mixingServer;

import interfaceRMI.IMixingServer;
import Globals.Capsule;

import java.rmi.RemoteException;

public class MixingServer implements IMixingServer {

    @Override
    public String receiveCapsule(Capsule capsule) throws RemoteException {
        return null;
    }
}
