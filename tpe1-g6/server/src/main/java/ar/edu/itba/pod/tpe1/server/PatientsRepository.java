package ar.edu.itba.pod.tpe1.server;

import ar.edu.itba.pod.tpe1.server.model.ComparablePatient;
import ar.edu.itba.pod.tpe1.waitingRoom.*;
import org.apache.commons.lang3.Validate;

import java.util.Optional;
import java.util.concurrent.PriorityBlockingQueue;

public class PatientsRepository {
    private final Object lock = "lock";
    private final PriorityBlockingQueue<ComparablePatient> patients = new PriorityBlockingQueue<>();

    private final int MAX_LEVEL = 5;
    private final int MIN_LEVEL = 1;

    public Patient addPatient(Patient patient) {
        Validate.isTrue(patient.getLevel() >= MIN_LEVEL && patient.getLevel() <= MAX_LEVEL, "Patient level out of range");

        synchronized (lock) {
            ComparablePatient comparablePatient = new ComparablePatient(patient);
            if (patients.contains(comparablePatient)) {
                throw new IllegalArgumentException("Patient already exists");
            }

            patients.add(comparablePatient);
            return patient;
        }
    }

    public Patient updateLevel(Patient patient) {
        Validate.isTrue(patient.getLevel() >= MIN_LEVEL && patient.getLevel() <= MAX_LEVEL, "Patient level out of range");

        synchronized (lock) {
            ComparablePatient comparablePatient = new ComparablePatient(patient);
            if (!patients.contains(comparablePatient)) {
                throw new IllegalArgumentException("Patient does not exist");
            }

            // TODO: Handle error or maybe return optional
            if (patients.remove(comparablePatient)) {
                patients.add(comparablePatient);
            }
            return patient;
        }
    }

//    public Optional<Patient> getPatient(String patientName) {
//        synchronized (lock) {
//            return Optional.ofNullable(patients.get(patientName));
//        }
//    }
}

