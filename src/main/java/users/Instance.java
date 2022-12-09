package users;

import Globals.UserLog;
import io.jsondb.annotation.Document;
import io.jsondb.annotation.Id;
import io.jsondb.annotation.Secret;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * A test Pojo representing a imaginary class Instance.
 * @version 1.0 28-Sep-2016
 */
@Document(collection = "instances", schemaVersion= "1.0")
public class Instance {
    //This field will be used as a primary key, every POJO should have one
    @Id
    private String id;
    private String phoneNumber;
    private String password;
    private Map<LocalDate, List<UserLog>> logs;

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Map<LocalDate, List<UserLog>> getLogs() {
        return logs;
    }

    public void setLogs(Map<LocalDate, List<UserLog>> logs) {
        this.logs = logs;
    }
}
