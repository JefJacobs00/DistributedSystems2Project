package Globals;

public class CriticalTuple {
    private String cateringFacilityHash;
    private TimeInterval timeInterval;

    public CriticalTuple(String cateringFacilityHash, TimeInterval timeInterval) {
        this.cateringFacilityHash = cateringFacilityHash;
        this.timeInterval = timeInterval;
    }

    public String getCateringFacilityHash() {
        return cateringFacilityHash;
    }

    public String getCateringFacilityHashShortString() {
        if(cateringFacilityHash.length() > 20){
            return cateringFacilityHash.substring(0, 20) + "...";
        } else {
            return cateringFacilityHash;
        }
    }

    public void setCateringFacilityHash(String cateringFacilityHash) {
        this.cateringFacilityHash = cateringFacilityHash;
    }

    public TimeInterval getTimeInterval() {
        return timeInterval;
    }

    public void setTimeInterval(TimeInterval timeInterval) {
        this.timeInterval = timeInterval;
    }
}
