package mixingServer;

import Globals.*;
import interfaceRMI.IMatchingService;
import interfaceRMI.IRegistar;

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

    private List<Capsule> capsules;

    private HashSet<String> uninformedTokens;
    private  List<CriticalTuple> criticalFacilities;
    public MatchingServer() throws RemoteException, NotBoundException {
        Registry myRegistry = LocateRegistry.getRegistry("localhost", 1099);
        registar = (IRegistar) myRegistry.lookup("Registar");
        capsules = new ArrayList<>();
        criticalFacilities = new ArrayList<>();
        uninformedTokens = new HashSet<>();
    }

    private List<String> getNymsForDay(LocalDate date) throws RemoteException {
        return registar.getNymsForDay(date);
    }

    @Override
    public void receiveInfectedUserLogs(SignedData signedData) throws RemoteException, NoSuchAlgorithmException {
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
            appendCriticalTuples(log);

            // mark tokens that where at the facility at the critical time
            markTokensUninformed(log.cfHash, log.visitInterval);
            uninformedTokens.remove(log.userToken);
        }
    }

    public void sendUninformedTokensToRegistar(){
        // TODO send uninformed to registar
    }


    private void markTokensUninformed(String cateringFacilityHash, TimeInterval interval){
        for (Capsule capsule: capsules) {
            if (capsule.getCfHash().equals(cateringFacilityHash) && interval.hasOverlap(capsule.getInterval())){
                uninformedTokens.add(capsule.getUserToken().getSignature());
            }
        }
    }

    @Override
    public void receiveFlushedCapsules(List<Capsule> capsuleList) throws RemoteException {
        capsules.addAll(capsuleList);


        for (CriticalTuple criticalTuple: criticalFacilities) {
            markTokensUninformed(criticalTuple.getCateringFacilityHash(), criticalTuple.getTimeInterval());
        }

    }

    @Override
    public void receiveInformedToken(String token) throws RemoteException {
        uninformedTokens.remove(token);
    }

    @Override
    public List<CriticalTuple> getCriticalTuples() throws RemoteException {
        return criticalFacilities;
    }

    private void appendCriticalTuples(UserLog log){
        criticalFacilities.add(new CriticalTuple(log.cfHash, log.visitInterval));
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


    public List<Capsule> getCapsules() {
        return capsules;
    }

    public HashSet<String> getUninformedTokens() {
        return uninformedTokens;
    }
}
