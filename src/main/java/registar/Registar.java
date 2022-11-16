package registar;

import interfaceRMI.IRegistar;
import users.CateringFacility;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;


public class Registar extends UnicastRemoteObject implements IRegistar {
    HashMap<String, String[] > users;
    private static final int DAILYTOKENCOUNT = 48;

    protected Registar() throws RemoteException {
        users = new HashMap<String, String[]>();
    }

    @Override
    public String[] EnrolCF(CateringFacility cf) {
        // Send a batch of day specific pseudonyms
        return new String[0];
    }

    @Override
    public String[] EntrolUser(String phoneNumber) throws RemoteException {
        users.put(phoneNumber, new String[DAILYTOKENCOUNT]);

        // Generate & return the tokens it can use.
        return null;
    }

}
