package ar.edu.itba.pod.tpe1.server.model;

import ar.edu.itba.pod.tpe1.waitingRoom.PatientSet;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import java.util.concurrent.atomic.AtomicLong;

@Getter
@EqualsAndHashCode
public class ComparablePatient implements Comparable<ComparablePatient> {
    private static final AtomicLong seq = new AtomicLong(0);
    private final long seqNum;
    private final PatientSet patient;

    public ComparablePatient(PatientSet patient) {
        this.patient = patient;
        this.seqNum = seq.getAndIncrement();
    }

    public String getName() {
        return patient.getPatientName();
    }

    public int getLevel() {
        return patient.getLevel();
    }

    @Override
    public int compareTo(ComparablePatient o) {
        // applies first-in-first-out tie-breaking to comparable elements
        int res = Integer.compare(o.getPatient().getLevel(), this.patient.getLevel());
        if (res == 0 && o != this) {
            res = (seqNum < o.seqNum ? -1 : 1);
        }
        return res;
    }

    @Override
    public String toString() {
        return "ComparablePatient { " +
                patient +
                " (" + patient.getLevel() + ") }";
    }
}
