package users;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Main {
    public static void main(String[] args){
        CateringFacility cf = new CateringFacility("id1", "cf","somewhere", "0495366639" ,"localhost" , 1399 );
        User user = new User("0495366639");
    }
}
