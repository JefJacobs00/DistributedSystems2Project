package Globals;

public class CriticalTuples {
    private String cateringFacilityHash;
    private TimeInterval timeInterval;

    public CriticalTuples(String cateringFacilityHash, TimeInterval timeInterval) {
        this.cateringFacilityHash = cateringFacilityHash;
        this.timeInterval = timeInterval;
    }

    public String getCateringFacilityHash() {
        return cateringFacilityHash;
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
