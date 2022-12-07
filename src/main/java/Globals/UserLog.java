package Globals;

public class UserLog implements java.io.Serializable {
    public String userToken;
    public String cfHash;
    public long hashRandomNumber;
    public TimeInterval visitInterval;

    public UserLog(String userToken, String cfHash, long hashRandomNumber, TimeInterval visitInterval) {
        this.userToken = userToken;
        this.cfHash = cfHash;
        this.hashRandomNumber = hashRandomNumber;
        this.visitInterval = visitInterval;
    }

    public String getUserToken() {
        return userToken;
    }

    public String getCfHash() {
        return cfHash;
    }

    public long getHashRandomNumber() {
        return hashRandomNumber;
    }

    public TimeInterval getVisitInterval() {
        return visitInterval;
    }
}
