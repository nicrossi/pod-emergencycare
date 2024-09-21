package ar.edu.itba.pod.tpe1.server;

import ar.edu.itba.pod.tpe1.administration.Doctor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DoctorsRepository {
    private final Object lock = "lock";

    private final Map<String, Doctor> doctors = new HashMap<>();

    private final int MAX_LEVEL = 5;
    private final int MIN_LEVEL = 1;

    public Doctor addDoctor(Doctor doctor) {
        if(doctor.getLevel() > MAX_LEVEL || doctor.getLevel() < MIN_LEVEL) {
            throw new IllegalArgumentException("Doctor level out of range");
        }

        synchronized (lock) {
            if(doctors.containsKey(doctor.getName())) {
                throw new IllegalArgumentException("Doctor already exists");
            }
            doctors.put(doctor.getName(), doctor);
            return doctor;
        }
    }

    public Optional<Doctor> getDoctor(String doctorName) {
        synchronized (lock) {
            return  Optional.ofNullable(doctors.get(doctorName));
        }
    }
}
