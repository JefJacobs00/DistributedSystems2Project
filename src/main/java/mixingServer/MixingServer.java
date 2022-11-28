package mixingServer;

import interfaceRMI.IMixingServer;
import Globals.Capsule;

import java.rmi.RemoteException;

public class MixingServer implements IMixingServer {

    public MixingServer(){}

    @Override
    public String receiveCapsule(Capsule capsule) throws RemoteException {
        // Check validity (user token, day, spent)
        // Sign cfhash and send it back (4 last values are the code for the day)
        return null;
    }
}
