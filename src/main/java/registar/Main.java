package registar;

import mixingServer.MatchingServerGUI;
import mixingServer.MixingServerGUI;

import javax.swing.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Main {


    private void startServer() {
        try {
            Registry registry = LocateRegistry.createRegistry(1099);
            JFrame registarFrame = new RegistrarGUI("Registar");
            registarFrame.setVisible(true);
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
