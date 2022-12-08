package registrar;

import mixingServer.MixingServer;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.*;

public class Main {


    private void startServer() throws SQLException {
        try {
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.bind("Registar", new Registar());
            registry.bind("MixingServer", new MixingServer());
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("system is ready");
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/registrar","root","root");

        Statement statement = connection.createStatement();

        ResultSet resultSet = statement.executeQuery("select * from registrar");

        while(resultSet.next()){
            System.out.println(resultSet.getString(""));
        }
    }

    public static void main(String[] args) throws SQLException {
        Main main = new Main();
        main.startServer();
    }
}
