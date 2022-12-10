package interfaceRMI;

import Globals.UserLog;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ICentralHealthAuthority extends Remote {
    public void receiveUserLogs(Map<LocalDate, List<UserLog>> logs) throws RemoteException;
}
