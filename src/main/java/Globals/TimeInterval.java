package Globals;

import java.util.Calendar;
import java.util.Date;

public class TimeInterval implements java.io.Serializable {
    Calendar start;
    Calendar end;

    public TimeInterval(Calendar start, Calendar end) {
        this.start = start;
        this.end = end;
    }

    public Calendar getStart() {
        return start;
    }

    public void setStart(Calendar start) {
        this.start = start;
    }

    public Calendar getEnd() {
        return end;
    }

    public void setEnd(Calendar end) {
        this.end = end;
    }
}
