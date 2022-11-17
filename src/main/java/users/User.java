package users;

import interfaceRMI.IRegistar;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class User {
    private String phoneNumber;
    private String[] tokens;

    private IRegistar registar;

    public User(String phoneNumber){
        this.phoneNumber = phoneNumber;
        this.start();
    }

    public synchronized void start(){
        try {
            Registry myRegistry = LocateRegistry.getRegistry("localhost", 1099);
            registar = (IRegistar) myRegistry.lookup("Registar");

            String[] a = registar.entrolUser(this.phoneNumber);
            System.out.println(a[0]);


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
