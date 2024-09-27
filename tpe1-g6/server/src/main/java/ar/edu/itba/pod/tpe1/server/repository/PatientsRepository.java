package ar.edu.itba.pod.tpe1.server.repository;

import ar.edu.itba.pod.tpe1.server.model.ComparablePatient;
import ar.edu.itba.pod.tpe1.waitingRoom.*;
import org.apache.commons.lang3.Validate;

import java.util.*;
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
            Validate.isTrue(!patients.containsKey(patient.getPatientName()), "Patient already exists");

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

    public Iterator<Patient> waitingRoomIterator() {
        synchronized (lock) {
            List<ComparablePatient> snapshot = new ArrayList<>(waitingRoom);
            Collections.sort(snapshot);
            return new WaitingRoomIterator(snapshot);
        }
    }

    private class WaitingRoomIterator implements Iterator<Patient> {
        private final List<ComparablePatient> snapshot;
        private int currentIndex = 0;
        private ComparablePatient lastReturned = null;

        public WaitingRoomIterator(List<ComparablePatient> snapshot) {
            this.snapshot = snapshot;
        }

        @Override
        public boolean hasNext() {
            return currentIndex < snapshot.size();
        }

        @Override
        public Patient next() {
            lastReturned = snapshot.get(currentIndex++);
            return lastReturned.getPatient();
        }

        @Override
        public void remove() {
            if (lastReturned == null) {
                throw new IllegalStateException("next() has not been called or remove() already called after the last call to next()");
            }
            synchronized (lock) {
                waitingRoom.remove(lastReturned);
                patients.remove(lastReturned.getName());
            }
            lastReturned = null;
        }
    }

    public List<Patient> getSortedPatients() {
        // Sort patients by emergency level (assuming Patient has a getLevel() method)
        return collectPatients(waitingRoomIterator());
    }

    private List<Patient> collectPatients(Iterator<Patient> iterator) {
        List<Patient> patients = new ArrayList<>();
        while (iterator.hasNext()) {
            patients.add(iterator.next());
        }
        return patients;
    }
}

