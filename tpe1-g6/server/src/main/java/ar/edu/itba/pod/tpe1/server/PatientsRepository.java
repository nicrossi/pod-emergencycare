package ar.edu.itba.pod.tpe1.server;

import ar.edu.itba.pod.tpe1.waitingRoom.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PatientsRepository {
    private final Object lock = "lock";

    private final Map<String, Patient> patients = new HashMap<>();

    private final int MAX_LEVEL = 5;
    private final int MIN_LEVEL = 1;

    public Patient addPatient(Patient patient) {
        if (patient.getLevel() > MAX_LEVEL || patient.getLevel() < MIN_LEVEL) {
            throw new IllegalArgumentException("Patient level out of range");
        }

        synchronized (lock) {
            if (patients.containsKey(patient.getPatientName())) {
                throw new IllegalArgumentException("Patient already exists");
            }

            patients.put(patient.getPatientName(), patient);
            return patient;
        }
    }

    public Optional<Patient> getPatient(String patientName) {
        synchronized (lock) {
            return Optional.ofNullable(patients.get(patientName));
        }
    }
}

