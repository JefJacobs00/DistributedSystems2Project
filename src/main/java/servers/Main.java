package servers;

import io.jsondb.JsonDBTemplate;
import users.User;

import javax.swing.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Main {


    private void startServer() {

        String dbFilesLocation = "src/main/java/JsonDB/Registrar.json";
        String baseScanPackage = "Registrar";
        JsonDBTemplate jsonDBTemplate = new JsonDBTemplate(dbFilesLocation, baseScanPackage);

        if (!jsonDBTemplate.collectionExists(Registar.class))
            jsonDBTemplate.createCollection(Registar.class);
        if(!jsonDBTemplate.collectionExists(MatchingServer.class))
            jsonDBTemplate.createCollection(MatchingServer.class);





        try {
            Registry registry = LocateRegistry.createRegistry(1099);
            Registar registar = new Registar(0);
            registry.bind("Registar", registar);

            jsonDBTemplate.upsert(registar);
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
