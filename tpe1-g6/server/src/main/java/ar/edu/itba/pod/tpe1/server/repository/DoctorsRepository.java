package ar.edu.itba.pod.tpe1.server.repository;

import ar.edu.itba.pod.tpe1.administration.AvailabilityStatus;
import ar.edu.itba.pod.tpe1.administration.Doctor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

public class DoctorsRepository {
    private final Object lock = "lock";

    private final SortedMap<String, Doctor> doctors = new TreeMap<>();
    private final SortedMap<Integer, Map<String, Doctor>> doctorMap = new TreeMap<>();

    private final int MAX_LEVEL = 5;

    public Doctor addDoctor(Doctor doctor) {
        int MIN_LEVEL = 1;
        if (doctor.getLevel() > MAX_LEVEL || doctor.getLevel() < MIN_LEVEL) {
            throw new IllegalArgumentException("Doctor level out of range");
        }

        synchronized (lock) {
            if (doctors.containsKey(doctor.getName())) {
                throw new IllegalArgumentException("Doctor already exists");
            }
            doctors.put(doctor.getName(), doctor);
            addDoctorToMap(doctor);
            return doctor;
        }
    }


    public Doctor modifyDoctor(Doctor modifiedDoctor) {
        synchronized (lock) {
            String name = modifiedDoctor.getName();
            Doctor prevDoctor = doctors.get(name);

            if (prevDoctor == null) {
                throw new IllegalArgumentException("Doctor %s not found".formatted(name));
            }

            doctors.put(modifiedDoctor.getName(), modifiedDoctor);
            updateDoctorInMap(modifiedDoctor);

            return modifiedDoctor;
        }
    }

    public Doctor setDoctorAvailabilityStatus(Doctor doctor, AvailabilityStatus status) {
        return modifyDoctor(doctor.toBuilder().setAvailability(status).build());
    }

    public Optional<Doctor> getDoctor(String doctorName) {
        synchronized (lock) {
            return Optional.ofNullable(doctors.get(doctorName));
        }
    }

    public Optional<Doctor> findNextAvailableDoctorClosestFit(int level) {
        synchronized (lock) {
            for (int i = level; i <= MAX_LEVEL; i++) {
                Map<String, Doctor> levelMap = doctorMap.get(i);
                if (levelMap != null && !levelMap.isEmpty()) {
                    for (Doctor doctor : levelMap.values()) {
                        if (doctor.getAvailability() == AvailabilityStatus.AVAILABILITY_STATUS_AVAILABLE) {
                            return Optional.of(doctor);
                        }
                    }
                }
            }
            return Optional.empty();
        }
    }

    private void addDoctorToMap(Doctor doctor) {
        int level = doctor.getLevel();
        Map<String, Doctor> levelMap = doctorMap.computeIfAbsent(level, k -> new HashMap<>());
        levelMap.put(doctor.getName(), doctor);
    }

    private void updateDoctorInMap(Doctor doctor) {
        int level = doctor.getLevel();
        Map<String, Doctor> levelMap = doctorMap.get(level);
        levelMap.put(doctor.getName(), doctor);
    }
}
