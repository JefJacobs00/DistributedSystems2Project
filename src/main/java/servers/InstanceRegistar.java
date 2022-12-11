package servers;

import Globals.SignedData;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.jsondb.annotation.Document;
import io.jsondb.annotation.Id;
import servers.Registar;
import users.CateringFacility;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Document(collection = "InstanceRegistar", schemaVersion= "1.0")
public class InstanceRegistar {

    public InstanceRegistar()
    {
        this.users = new HashMap<>();
        this.cateringFacilities = new ArrayList<>();
        this.facilitySynonyms = new HashMap<>();
    }

    public InstanceRegistar(int id) {
        this.id = id;
        this.users = new HashMap<>();
        this.cateringFacilities = new ArrayList<>();
        this.facilitySynonyms = new HashMap<>();
    }

    public InstanceRegistar(Registar registar){
        this.id = registar.getId();
        this.users = registar.getUsers();
        this.cateringFacilities = new ArrayList<>(registar.getCateringFacilities());
        this.facilitySynonyms = getFacilitySynonymsStrings(registar.getFacilitySynonyms());
    }

    @Id
    private int id;

    private HashMap<String, SignedData[]> users;
    private List<CateringFacility> cateringFacilities;
    private HashMap<String , List<String>> facilitySynonyms;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public HashMap<String, SignedData[]> getUsers() {
        return users;
    }


    public void setUsers(HashMap<String, SignedData[]> users) {
        this.users = users;
    }

    public List<CateringFacility> getCateringFacilities() {
        return cateringFacilities;
    }

    public void setCateringFacilities(List<CateringFacility> cateringFacilities) {
        this.cateringFacilities = cateringFacilities;
    }

    public HashMap<String, List<String>> getFacilitySynonyms() {
        return facilitySynonyms;
    }

    public void setFacilitySynonyms(HashMap<String, List<String>> facilitySynonyms) {
        this.facilitySynonyms = facilitySynonyms;
    }

    @JsonIgnore
    public HashMap<String, List<String>> getFacilitySynonymsStrings(HashMap<LocalDate, List<String>> facilitySynonymsLocalDate) {
        HashMap<String, List<String>> facilitySynonyms = new HashMap<>();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for(Map.Entry<LocalDate, List<String>> entry : facilitySynonymsLocalDate.entrySet()) {
            facilitySynonyms.put(entry.getKey().format(dtf), entry.getValue());
        }
        return facilitySynonyms;
    }
}
