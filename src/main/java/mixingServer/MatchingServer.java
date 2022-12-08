package mixingServer;

import Globals.SignedData;
import Globals.TimeInterval;
import Globals.UserLog;
import interfaceRMI.IMatchingService;
import interfaceRMI.IRegistar;
import users.User;

import java.nio.ByteBuffer;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.*;

import static org.bouncycastle.pqc.math.linearalgebra.ByteUtils.toHexString;

public class MatchingServer extends UnicastRemoteObject implements IMatchingService {

    private IRegistar registar;
    Map<String, List<TimeInterval>> criticalFacilities;
    public MatchingServer() throws RemoteException, NotBoundException {
        Registry myRegistry = LocateRegistry.getRegistry("localhost", 1099);
        registar = (IRegistar) myRegistry.lookup("Registar");
    }

    private List<String> getNymsForDay(LocalDate date) throws RemoteException {
        return registar.getNymsForDay(date);
    }

    @Override
    public void receiveSignedUserLogs(SignedData signedData) throws RemoteException, NoSuchAlgorithmException {
        if (!(signedData.getData() instanceof Collection<?>)){
            throw new IllegalArgumentException("Received data is of invalid type");
        }
        Map<LocalDate, List<String>> nyms = new HashMap<>();
        List<UserLog> logs = (List<UserLog>) signedData.getData();

        for (UserLog log: logs) {
            LocalDate day = log.getVisitInterval().getStart().toLocalDate();
            if (!nyms.containsKey(day)){
                List<String> nmysOfDay = getNymsForDay(day);
                nyms.put(day, nmysOfDay);
            }

            validateLog(log, nyms.get(day));

            appendCriticalPlaces(log);
        }
    }

    private void appendCriticalPlaces(UserLog log){
        if (!criticalFacilities.containsKey(log.cfHash)){
            criticalFacilities.put(log.cfHash, new ArrayList<>());
        }

        criticalFacilities.get(log.cfHash).add(log.visitInterval);
    }

    private void validateLog(UserLog log, List<String> nyms) throws NoSuchAlgorithmException {
        if(!isValidLog(log, nyms))
            throw new IllegalArgumentException("User has invalid log: ("+"the user visited catering facility "+ log.cfHash +
                    " at "+ log.visitInterval.getStart().toLocalDate().toString() +
                    " no such catering facility was found for that day.");
    }

    private boolean isValidLog(UserLog log, List<String> nyms) throws NoSuchAlgorithmException {
        byte[] randomNumber = longToByte(log.hashRandomNumber);
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        for (String nym : nyms) {
            sha.update(randomNumber);
            sha.update(nym.getBytes());
            String hash = toHexString(sha.digest());

            if (log.cfHash.contains(hash))
                return true;
        }

        return false;
    }

    private byte[] longToByte(long number){
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(number);

        return buffer.array();
    }
}
