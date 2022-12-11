package mixingServer;

import Globals.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import interfaceRMI.IMatchingService;
import interfaceRMI.IRegistar;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.bouncycastle.pqc.math.linearalgebra.ByteUtils.toHexString;

public class MatchingServer extends UnicastRemoteObject implements IMatchingService {

    private IRegistar registar;

    private List<Capsule> capsules;

    private HashSet<String> uninformedTokens;

    private HashSet<String> informedTokens;
    private  List<CriticalTuple> criticalFacilities;
    public MatchingServer() throws RemoteException, NotBoundException {
        Registry myRegistry = LocateRegistry.getRegistry("localhost", 1099);
        registar = (IRegistar) myRegistry.lookup("Registar");
        capsules = new ArrayList<>();
        criticalFacilities = new ArrayList<>();
        uninformedTokens = new HashSet<>();
        informedTokens = new HashSet<>();

        try {
            capsules = loadCapsulesFromFile();
            criticalFacilities = loadCriticalFacilitiesFromFile();
            uninformedTokens = loadUninformedTokensFromFile();
            informedTokens = loadInformedTokensFromFile();
        }catch (Exception e){
        }
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
            informedTokens.add(log.userToken);
        }

        saveToFile();
    }

    public void sendUninformedTokensToRegistar(){
        // TODO send uninformed to registar
    }


    private void markTokensUninformed(String cateringFacilityHash, TimeInterval interval){
        for (Capsule capsule: capsules) {
            if (capsule.getCfHash().equals(cateringFacilityHash) && interval.hasOverlap(capsule.getInterval()) && !informedTokens.contains(capsule.getUserToken().getSignature())){
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

        saveToFile();
    }

    @Override
    public void receiveInformedToken(String token) throws RemoteException {
        uninformedTokens.remove(token);
        informedTokens.add(token);
        saveToFile();
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

    private void saveToFile(){
        JsonObject data = new JsonObject();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        JsonArray capsules = new JsonArray();
        for (Capsule capsule: this.capsules) {
            JsonObject jsonCapsule = new JsonObject();
            jsonCapsule.addProperty("cfHash",capsule.getCfHash());
            jsonCapsule.addProperty("userToken",capsule.getUserToken().getSignature());
            jsonCapsule.addProperty("hashRandomNumber",capsule.getUserToken().getData().toString());
            jsonCapsule.addProperty("start", dtf.format(capsule.getInterval().getStart()));
            jsonCapsule.addProperty("end", dtf.format(capsule.getInterval().getEnd()));

            capsules.add(jsonCapsule);
        }

        JsonArray uninformedTokens = new JsonArray();

        for (String token :this.uninformedTokens) {
            uninformedTokens.add(token);
        }

        JsonArray informedTokens = new JsonArray();

        for (String token :this.informedTokens) {
            informedTokens.add(token);
        }

        JsonArray criticalFacilitiesJson = new JsonArray();
        for (CriticalTuple criticalTuple: this.criticalFacilities) {
            JsonObject jsonTuple = new JsonObject();
            jsonTuple.addProperty("cfHash", criticalTuple.getCateringFacilityHash());
            jsonTuple.addProperty("start", dtf.format(criticalTuple.getTimeInterval().getStart()));
            jsonTuple.addProperty("end", dtf.format(criticalTuple.getTimeInterval().getEnd()));

            criticalFacilitiesJson.add(jsonTuple);
        }

        data.add("criticalFacilities",criticalFacilitiesJson);
        data.add("uninformedTokens", uninformedTokens);
        data.add("informedTokens", informedTokens);
        data.add("capsules", capsules);

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("matchingServer.json"));
            writer.write(data.toString());
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private HashSet<String> loadUninformedTokensFromFile() throws IOException {
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = (JsonObject) parser.parse(new FileReader("matchingServer.json"));

        JsonArray jsonArray = jsonObject.get("uninformedTokens").getAsJsonArray();

        HashSet<String> uninformedTokens = new HashSet<>();

        for (int i = 0; i < jsonArray.size(); i++) {
            JsonElement object = jsonArray.get(i);
            uninformedTokens.add(object.getAsString());
        }

        return uninformedTokens;
    }

    private HashSet<String> loadInformedTokensFromFile() throws IOException {
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = (JsonObject) parser.parse(new FileReader("matchingServer.json"));

        JsonArray jsonArray = jsonObject.get("informedTokens").getAsJsonArray();

        HashSet<String> informedTokens = new HashSet<>();

        for (int i = 0; i < jsonArray.size(); i++) {
            JsonElement object = jsonArray.get(i);
            informedTokens.add(object.getAsString());
        }

        return informedTokens;
    }

    private List<CriticalTuple> loadCriticalFacilitiesFromFile() throws IOException {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = (JsonObject) parser.parse(new FileReader("matchingServer.json"));

        JsonArray jsonFacilities = jsonObject.get("criticalFacilities").getAsJsonArray();

        List<CriticalTuple> criticalTuples = new ArrayList<>();

        for (int i = 0; i < jsonFacilities.size(); i++) {
            JsonObject object = (JsonObject) jsonFacilities.get(i);

            LocalDateTime start = LocalDateTime.parse(object.get("start").getAsString(), dtf);
            LocalDateTime end = LocalDateTime.parse(object.get("end").getAsString(), dtf);
            TimeInterval interval = new TimeInterval(start,end);

            CriticalTuple criticalTuple = new CriticalTuple(object.get("cfHash").getAsString(),interval);
            criticalTuples.add(criticalTuple);
        }

        return criticalTuples;
    }

    private List<Capsule> loadCapsulesFromFile() throws IOException {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = (JsonObject) parser.parse(new FileReader("matchingServer.json"));

        JsonArray jsonCapsules = jsonObject.get("capsules").getAsJsonArray();

        List<Capsule> capsules = new ArrayList<>();

        for (int i = 0; i < jsonCapsules.size(); i++) {
            JsonObject object = (JsonObject) jsonCapsules.get(i);

            LocalDateTime start = LocalDateTime.parse(object.get("start").getAsString(), dtf);
            LocalDateTime end = LocalDateTime.parse(object.get("end").getAsString(), dtf);
            TimeInterval interval = new TimeInterval(start,end);

            Capsule capsule = new Capsule(interval,new SignedData(object.get("userToken").getAsString(), object.get("hashRandomNumber").getAsLong()), object.get("cfHash").getAsString());
            capsules.add(capsule);
        }

        return capsules;
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
