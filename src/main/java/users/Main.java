package users;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Main {
    public static void main(String[] args){
        User user = new User("0495366639");
        CateringFacility cf = new CateringFacility("id", "cf","somewhere", "0495366639" ,"localhost" , 1399 );
    }
}
