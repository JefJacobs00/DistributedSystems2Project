package Globals;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeInterval implements java.io.Serializable {
    LocalDateTime start;
    LocalDateTime end;

    public TimeInterval(LocalDateTime start, LocalDateTime end) {
        this.start = start;
        this.end = end;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    public boolean hasOverlap(TimeInterval t2){
        return !t2.getEnd().isBefore(this.getStart()) && !t2.getStart().isAfter(this.getEnd()); // overlap
    }

    @Override
    public String toString() {
        DateTimeFormatter customFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");;
        return start.format(customFormat) + "->" + end.format(customFormat);
    }
}
