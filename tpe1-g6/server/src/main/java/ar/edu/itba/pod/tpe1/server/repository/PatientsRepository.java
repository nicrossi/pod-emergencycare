package ar.edu.itba.pod.tpe1.server.repository;

import ar.edu.itba.pod.tpe1.server.model.ComparablePatient;
import ar.edu.itba.pod.tpe1.waitingRoom.*;
import org.apache.commons.lang3.Validate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

public class PatientsRepository {
    private final Object lock = "lock";
    private final Map<String, Patient> patients = new ConcurrentHashMap<>();
    private final PriorityBlockingQueue<ComparablePatient> waitingRoom = new PriorityBlockingQueue<>();

    private final int MAX_LEVEL = 5;
    private final int MIN_LEVEL = 1;

    public Patient addPatient(Patient patient) {
        Validate.isTrue(patient.getLevel() >= MIN_LEVEL && patient.getLevel() <= MAX_LEVEL, "Patient level out of range");

        synchronized (lock) {
            Validate.isTrue(!patients.containsKey(patient.getPatientName()), "Patient already exist");

            patients.put(patient.getPatientName(), patient);
            waitingRoom.add(new ComparablePatient(patient));

            return patient;
        }
    }

    public Patient updateLevel(Patient patient) {
        Validate.isTrue(patient.getLevel() >= MIN_LEVEL && patient.getLevel() <= MAX_LEVEL, "Patient level out of range");

        synchronized (lock) {
            Validate.isTrue(patients.containsKey(patient.getPatientName()), "Patient does not exist");

            ComparablePatient comparablePatient = new ComparablePatient(patient);
            if (waitingRoom.remove(comparablePatient)) {
                waitingRoom.add(comparablePatient);
            }

            return patient;
        }
    }

    public Patient getPatient(String patientName) {
        Validate.notBlank(patientName, "Patient name cannot be blank");
        synchronized (lock) {
            Patient patient = patients.get(patientName);
            if (patient == null) {
                throw new IllegalArgumentException("Patient does not exist");
            }

            return patient;
        }
    }

    public PatientState checkPatient(String patientName) {
        Validate.notBlank(patientName, "Patient name cannot be blank");

        synchronized (lock) {
            Validate.isTrue(patients.containsKey(patientName), "Patient does not exist");

            List<ComparablePatient> patientList = new ArrayList<>(waitingRoom);
            Collections.sort(patientList);
            int index = patientList.indexOf(new ComparablePatient(patients.get(patientName)));
            return PatientState.newBuilder()
                    .setPatientName(patientName)
                    .setLevel(patients.get(patientName).getLevel())
                    .setQueuePlace(index)
                    .build();
        }
    }
}

