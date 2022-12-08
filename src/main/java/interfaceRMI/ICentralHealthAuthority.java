package interfaceRMI;

import Globals.UserLog;

import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ICentralHealthAuthority {
    public void receiveUserLogs(Map<LocalDate, List<UserLog>> logs) throws RemoteException;
}
