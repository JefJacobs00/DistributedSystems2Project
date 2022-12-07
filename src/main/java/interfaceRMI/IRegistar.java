package interfaceRMI;

import Globals.SignedData;
import users.CateringFacility;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

public interface IRegistar extends Remote {
    public String enrollCF(CateringFacility cateringFacility) throws RemoteException, NoSuchAlgorithmException;
    public SignedData[] enrollUser(String phoneNumber) throws Exception;

    public Boolean validateToken(SignedData token) throws RemoteException, InvalidKeyException, SignatureException;

}
