package users;

import io.jsondb.JsonDBTemplate;

import java.awt.image.BufferedImage;

import static org.bouncycastle.util.encoders.Hex.toHexString;

public class Main {
    public static void main(String[] args){
        String dbFilesLocation = "src/main/java/JsonDB/users.json";
        String baseScanPackage = "users";
        JsonDBTemplate jsonDBTemplate = new JsonDBTemplate(dbFilesLocation, baseScanPackage);

        jsonDBTemplate.createCollection(InstanceUsers.class);


        InstanceUsers instanceUsers = new InstanceUsers();
        instanceUsers.setId("1");
        instanceUsers.setPhoneNumber("0123123123");
        instanceUsers.setPassword("b87eb02f5dd7e5232d7b0fc30a5015e4");
        jsonDBTemplate.insert(instanceUsers);

        instanceUsers.setId("2");
        instanceUsers.setPhoneNumber("045566677");
        instanceUsers.setPassword("SDGeb02f5dd7sdsdfsd2424");
        jsonDBTemplate.insert(instanceUsers);

        String dbFilesLocationCF = "src/main/java/JsonDB/CFusers.json";
        String baseScanPackageCF = "users";
        JsonDBTemplate jsonDBTemplateCF = new JsonDBTemplate(dbFilesLocationCF, baseScanPackageCF);
        //jsonDBTemplateCF.createCollection(InstanceCFUsers.class);

        InstanceCFUsers instanceCFUsers = new InstanceCFUsers();
        instanceCFUsers.setName("1");

        jsonDBTemplateCF.insert(instanceCFUsers);




        CateringFacility cf = new CateringFacility("id1", "cf","somewhere", "0495366639" ,"localhost" , 1099 );
        BufferedImage qr = cf.requestQrCode();
        User user = new User("0495366618");
        try {
            String result = user.visitFacility(qr);
            System.out.println(result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
