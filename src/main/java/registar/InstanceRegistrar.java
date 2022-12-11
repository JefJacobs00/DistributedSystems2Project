package registar;

import Globals.SignedData;
import io.jsondb.annotation.Document;
import io.jsondb.annotation.Id;
import users.CateringFacility;

import javax.crypto.SecretKey;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

/**
 * A test Pojo representing a imaginary class Instance.
 * @version 1.0 28-Sep-2016
 */
@Document(collection = "instancesRegistrar", schemaVersion= "1.0")
public class InstanceRegistrar {
    @Id
    private String id;
    private HashMap<String, SignedData[]> users;
    private HashMap<CateringFacility, SecretKey> secretKeys;
    private HashMap<LocalDate , List<String>> facilitySynonyms;
    private DateTimeFormatter dtf;



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public HashMap<String, SignedData[]> getUsers() {
        return users;
    }


    public void setUsers(HashMap<String, SignedData[]> users) {
        this.users = users;
    }

    public HashMap<CateringFacility, SecretKey> getSecretKeys() {
        return secretKeys;
    }

    public void setSecretKeys(HashMap<CateringFacility, SecretKey> secretKeys) {
        this.secretKeys = secretKeys;
    }

    public HashMap<LocalDate, List<String>> getFacilitySynonyms() {
        return facilitySynonyms;
    }

    public void setFacilitySynonyms(HashMap<LocalDate, List<String>> facilitySynonyms) {
        this.facilitySynonyms = facilitySynonyms;
    }

    public DateTimeFormatter getDtf() {
        return dtf;
    }

    public void setDtf(DateTimeFormatter dtf) {
        this.dtf = dtf;
    }

}
