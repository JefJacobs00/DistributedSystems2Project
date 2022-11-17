package interfaceRMI;

import users.CateringFacility;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;

public interface IRegistar extends Remote {
    public String[] enrolCF(CateringFacility cateringFacility) throws  RemoteException;
    public String[] entrolUser(String phoneNumber) throws RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException;
    public PublicKey getPublicKey() throws RemoteException;
}
