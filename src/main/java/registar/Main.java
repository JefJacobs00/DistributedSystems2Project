package registar;

import mixingServer.MatchingServer;
import mixingServer.MixingServer;
import users.CentralHealthAuthority;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.SecureRandom;
import java.security.cert.CertificateFactorySpi;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

public class Main {

    private void startServer() {
        try {
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.bind("Registar", new Registar());
            registry.bind("MatchingServer", new MatchingServer());
//            registry.bind("MixingServer", new MixingServer());
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
