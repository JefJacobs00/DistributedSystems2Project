package Globals;

public class Capsule implements java.io.Serializable {

    private TimeInterval interval;
    private SignedData userToken;
    private String cfHash;

    public Capsule(TimeInterval interval, SignedData userToken, String cfHash) {
        this.interval = interval;
        this.userToken = userToken;
        this.cfHash = cfHash;
    }

    public TimeInterval getInterval() {
        return interval;
    }

    public void setInterval(TimeInterval interval) {
        this.interval = interval;
    }

    public SignedData getUserToken() {
        return userToken;
    }

    public void setUserToken(SignedData userToken) {
        this.userToken = userToken;
    }

    public String getCfHash() {
        return cfHash;
    }

    public String getCfHashShortString(){
        if(cfHash.length() > 20){
            return cfHash.substring(0, 20) + "...";
        } else {
            return cfHash;
        }
    }

    public void setCfHash(String cfHash) {
        this.cfHash = cfHash;
    }

    @Override
    public String toString() {
        return "Capsule: " +
                "interval:" + interval +
                ", User Token:" + userToken + ", \n" +
                "CF Hash:" + cfHash;
    }
}
