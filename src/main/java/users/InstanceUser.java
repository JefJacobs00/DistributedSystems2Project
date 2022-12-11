package users;

import Globals.UserLog;
import io.jsondb.annotation.Document;
import io.jsondb.annotation.Id;
import io.jsondb.annotation.Secret;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "InstanceUsers", schemaVersion= "1.0")
public class InstanceUser {

    @Id
    private String phoneNumber;

    @Secret
    private String password;

    private List<UserLog> logs;

    public InstanceUser() {
        this.logs = new ArrayList<>();
    }

    public InstanceUser(String phoneNumber, String password){
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.logs = new ArrayList<>();
    }

    public InstanceUser(User user){
        this.phoneNumber = user.getPhoneNumber();
        this.password = user.getPassword();
        this.logs = user.getLogs();
    }

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

    public List<UserLog> getLogs() {
        return logs;
    }

    public void setLogs(List<UserLog> logs) {
        this.logs = logs;
    }
}
