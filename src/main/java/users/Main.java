package users;

import io.jsondb.JsonDBTemplate;

import java.awt.image.BufferedImage;

import static org.bouncycastle.util.encoders.Hex.toHexString;

public class Main {
    public static void main(String[] args){
        String dbFilesLocation = "src/main/java/JsonDB/users.json";
        String baseScanPackage = "users";
        JsonDBTemplate jsonDBTemplate = new JsonDBTemplate(dbFilesLocation, baseScanPackage);

        if (!jsonDBTemplate.collectionExists(InstanceUser.class))
            jsonDBTemplate.createCollection(InstanceUser.class);
        if (!jsonDBTemplate.collectionExists(CateringFacility.class))
            jsonDBTemplate.createCollection(CateringFacility.class);

        CateringFacility cf = new CateringFacility("id1", "cf","somewhere", "0495366639" ,"localhost" , 1099 );
        BufferedImage qr = cf.requestQrCode();
//        User user = new User("0495366618");


//        jsonDBTemplate.upsert(cf);
//        jsonDBTemplate.upsert(user);
//        try {
//            String result = user.visitFacility(qr);
//            System.out.println(result);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
    }
}
