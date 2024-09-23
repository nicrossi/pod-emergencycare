package ar.edu.itba.pod.tpe1.server.repository;

import ar.edu.itba.pod.tpe1.server.model.ComparablePatient;
import ar.edu.itba.pod.tpe1.waitingRoom.*;
import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

public class PatientsRepository {
    private final Object lock = "lock";
    private final Map<String, ComparablePatient> patients = new ConcurrentHashMap<>();
    private final PriorityBlockingQueue<ComparablePatient> waitingRoom = new PriorityBlockingQueue<>();

    private final int MAX_LEVEL = 5;
    private final int MIN_LEVEL = 1;

    public Patient addPatient(Patient patient) {
        Validate.isTrue(patient.getLevel() >= MIN_LEVEL && patient.getLevel() <= MAX_LEVEL, "Patient level out of range");

        synchronized (lock) {
            Validate.isTrue(!patients.containsKey(patient.getPatientName()), "Patient already exist");

            ComparablePatient cp = new ComparablePatient(patient);
            patients.put(patient.getPatientName(), cp);
            waitingRoom.add(cp);

            return patient;
        }
    }

    public Patient updateLevel(Patient patient) {
        Validate.isTrue(patient.getLevel() >= MIN_LEVEL && patient.getLevel() <= MAX_LEVEL, "Patient level out of range");

        synchronized (lock) {
            Validate.isTrue(patients.containsKey(patient.getPatientName()), "Patient does not exist");

            ComparablePatient cp = patients.get(patient.getPatientName());
            if (waitingRoom.remove(cp)) {
                cp.setLevel(patient.getLevel());
                waitingRoom.add(cp);
            }

            return patient;
        }
    }

    public PatientState checkPatient(String patientName) {
        Validate.notBlank(patientName, "Patient name cannot be blank");

        synchronized (lock) {
            Validate.isTrue(patients.containsKey(patientName), "Patient does not exist");

            ComparablePatient patient = patients.get(patientName);
            List<ComparablePatient> patientList = new ArrayList<>(waitingRoom);
            Collections.sort(patientList);
            int index = patientList.indexOf(patient);
            return PatientState.newBuilder()
                    .setPatientName(patientName)
                    .setLevel(patient.getLevel())
                    .setQueuePlace(index)
                    .build();
        }
    }

    public int getPatientsWaitingCount() {
        return waitingRoom.size();
    }

    public Optional<Patient> getWaitingRoomNextPatient() {
        synchronized (lock) {
            ComparablePatient patient = waitingRoom.poll();
            if (patient == null) {
                return Optional.empty();
            }

            return Optional.ofNullable(patients.remove(patient.getName()))
                           .map(ComparablePatient::getPatient);
        }
    }

    public Optional<Patient> peekWaitingRoomNextPatient() {
        return Optional.ofNullable(waitingRoom.peek())
                       .map(ComparablePatient::getPatient);
    }
}
