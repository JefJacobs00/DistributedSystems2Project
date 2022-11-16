package interfaceRMI;

import users.CateringFacility;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRegistar extends Remote {
    public String[] EnrolCF(CateringFacility cf) throws  RemoteException;
    public String[] EntrolUser(String phoneNumber) throws RemoteException;
}
