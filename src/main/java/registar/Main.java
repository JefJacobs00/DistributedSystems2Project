package registar;

import io.jsondb.JsonDBTemplate;
import mixingServer.MatchingServer;
import mixingServer.MatchingServerGUI;
import mixingServer.MixingServer;
import mixingServer.MixingServerGUI;
import users.CentralHealthAuthority;

import javax.swing.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Main {


    private void startServer() {

        String dbFilesLocation = "src/main/java/JsonDB/registar.json";
        String baseScanPackage = "registar";
        JsonDBTemplate jsonDBTemplate = new JsonDBTemplate(dbFilesLocation, baseScanPackage);
        jsonDBTemplate.createCollection(InstanceRegistar.class);
        InstanceRegistar instanceRegistar = new InstanceRegistar();

        instanceRegistar.setId("1");
        instanceRegistar.setDtf(null);
        instanceRegistar.setUsers(null);
        instanceRegistar.setSecretKeys(null);
        instanceRegistar.setFacilitySynonyms(null);
        jsonDBTemplate.insert(instanceRegistar);



        try {
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.bind("Registar", new Registar());
            JFrame matchingServerFrame = new MatchingServerGUI("Matching Server");
            matchingServerFrame.setVisible(true);
            JFrame mixingServerFrame = new MixingServerGUI("Mixing Server");
            mixingServerFrame.setVisible(true);
//            registry.bind("MatchingServer", new MatchingServer());
//            registry.bind("MixingServer", new MixingServer());
//            CentralHealthAuthority cha = new CentralHealthAuthority("localhost", 1099);
//            CentralHealthAuthority cha = new CentralHealthAuthority();
//            System.out.println(cha.start());
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("system is ready");
    }

    public static void main(String[] args){
        Main main = new Main();
        main.startServer();
    }
}
