package Globals;

import java.time.LocalDateTime;

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

    public String getUserTokenShortString() {
        if(userToken.length() > 20){
            return userToken.substring(0, 20) + "...";
        } else {
            return userToken;
        }
    }

    public String getCfHash() {
        return cfHash;
    }

    public String getCfHashShortString() {
        if(cfHash.length() > 20){
            return cfHash.substring(0, 20) + "...";
        } else {
            return cfHash;
        }
    }

    public long getHashRandomNumber() {
        return hashRandomNumber;
    }

    public TimeInterval getVisitInterval() {
        return visitInterval;
    }

    public void endVisitInterval(LocalDateTime end){
        visitInterval.setEnd(end);
    }
}
