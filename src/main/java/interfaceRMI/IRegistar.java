package interfaceRMI;

import users.CateringFacility;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

public interface IRegistar extends Remote {
    public String[] EnrolCF(CateringFacility cateringFacility) throws  RemoteException;
    public String[] EntrolUser(String phoneNumber) throws RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException;
}
