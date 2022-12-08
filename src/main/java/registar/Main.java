package registar;

import mixingServer.MatchingServer;
import mixingServer.MixingServer;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.cert.CertificateFactorySpi;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

public class Main {


    private void startServer() {
        try {
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.bind("Registar", new Registar());
            registry.bind("MixingServer", new MixingServer());
            registry.bind("MatchingServer", new MatchingServer());
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
