package registar;

import io.jsondb.JsonDBTemplate;
import mixingServer.MatchingServer;
import mixingServer.MixingServer;
import users.CentralHealthAuthority;

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
            registry.bind("MixingServer", new MixingServer());
            registry.bind("MatchingServer", new MatchingServer());
            CentralHealthAuthority cha = new CentralHealthAuthority("localhost", 1099);
            System.out.println(cha.start());
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
