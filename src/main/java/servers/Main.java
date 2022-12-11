package servers;

import io.jsondb.JsonDBTemplate;
import servers.InstanceRegistar;
import users.User;

import javax.swing.*;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Main {


    private void startServer() {

        String dbFilesLocation = "src/main/java/JsonDB/registar.json";
        String baseScanPackage = "servers";
        JsonDBTemplate jsonDBTemplate = new JsonDBTemplate(dbFilesLocation, baseScanPackage);

        if (!jsonDBTemplate.collectionExists(InstanceRegistar.class))
            jsonDBTemplate.createCollection(InstanceRegistar.class);
        if (!jsonDBTemplate.collectionExists(InstanceMatchingServer.class))
            jsonDBTemplate.createCollection(InstanceMatchingServer.class);


        try {
            Registry registry = LocateRegistry.createRegistry(1099);
            Registar registar = new Registar(0);
            registry.bind("Registar", registar);

            InstanceRegistar regInst = new InstanceRegistar(registar);

            MatchingServer matchingServer = new MatchingServer(0);

            InstanceMatchingServer matchingServerInst = new InstanceMatchingServer(matchingServer);


            jsonDBTemplate.upsert(regInst);
            jsonDBTemplate.upsert(matchingServerInst);

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
