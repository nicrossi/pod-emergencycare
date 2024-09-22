package ar.edu.itba.pod.tpe1.server.model;

import ar.edu.itba.pod.tpe1.waitingRoom.Patient;
import lombok.Getter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.concurrent.atomic.AtomicLong;

@Getter
public class ComparablePatient implements Comparable<ComparablePatient> {
    private static final AtomicLong seq = new AtomicLong(0);
    private final long seqNum;
    private Patient patient;

    public ComparablePatient(Patient patient) {
        this.patient = patient;
        this.seqNum = seq.getAndIncrement();
    }

    public String getName() {
        return patient.getPatientName();
    }

    public int getLevel() {
        return patient.getLevel();
    }

    public void setLevel(int level) {
        this.patient = patient.toBuilder().setLevel(level).build();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ComparablePatient that)) {
            return false;
        }

        Patient thisPatient = getPatient();
        Patient thatPatient = that.getPatient();
        return new EqualsBuilder()
                .append(thisPatient.getPatientName(), thatPatient.getPatientName())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getSeqNum())
                .append(getPatient()).toHashCode();
    }
}
