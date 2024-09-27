package ar.edu.itba.pod.tpe1.server.repository;

import ar.edu.itba.pod.tpe1.administration.Doctor;
import ar.edu.itba.pod.tpe1.query.CaredInfo;
import ar.edu.itba.pod.tpe1.waitingRoom.Patient;
import com.sun.jdi.request.DuplicateRequestException;

import java.util.ArrayList;
import java.util.List;

public class CareRespository {
    private final Object lock = "lock";
    private final List<CaredInfo> currentlyCared = new ArrayList<>();

    public CaredInfo startCare(int roomId, Patient patient, Doctor doctor) {
        synchronized (lock) {
            CaredInfo newCare = CaredInfo.newBuilder()
                    .setRoomId(roomId)
                    .setDoctor(doctor)
                    .setPatient(patient).build();

            //check we aren't duplicating cares!
            currentlyCared.stream()
                    .filter(c ->
                            c.equals(newCare)
                    ).findFirst().ifPresent(c -> {
                        throw new DuplicateRequestException("This care already exists");
                    });

            currentlyCared.add(newCare);
            return newCare;
        }
    }

    public CaredInfo getCare(int roomId) {
        synchronized (lock) {
            CaredInfo care = currentlyCared.stream()
                    .filter(caredInfo -> caredInfo.getRoomId() == roomId)
                    .findFirst().orElse(null);
            if (care == null) {
                throw new IllegalArgumentException("Care not found in room #{}");
            }

            return care;
        }
    }

    public CaredInfo endCare(int roomId, String patientName, String doctorName) {
        synchronized (lock) {
            CaredInfo care = currentlyCared.stream()
                    .filter(c ->
                            c.getRoomId() == roomId &&
                                    c.getDoctor().getName().equals(doctorName) &&
                                    c.getPatient().getPatientName().equals(patientName)
                    ).findFirst().orElse(null);
            if (care == null) {
                throw new IllegalArgumentException("No care registered for patient %s by doctor %s in room #%s".formatted(patientName, doctorName, roomId));
            }
            currentlyCared.remove(care);

            return care;
        }
    }
}
