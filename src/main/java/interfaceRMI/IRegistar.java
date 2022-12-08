package interfaceRMI;

import Globals.SignedData;
import users.CateringFacility;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public interface IRegistar extends Remote {
    public String enrollCF(CateringFacility cateringFacility) throws RemoteException, NoSuchAlgorithmException;
    public SignedData[] enrollUser(String phoneNumber) throws Exception;

    public Boolean validateToken(SignedData token) throws RemoteException, InvalidKeyException, SignatureException;

    public List<String> getNymsForDay(LocalDate day) throws RemoteException;

}
