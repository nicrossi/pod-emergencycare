package ar.edu.itba.pod.tpe1.server.repository;

import ar.edu.itba.pod.tpe1.administration.AvailabilityStatus;
import ar.edu.itba.pod.tpe1.administration.Doctor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class DoctorsRepositoryTest {
    // System under test
    private DoctorsRepository repository;

    @BeforeEach
    public void setUp() {
        repository = new DoctorsRepository();
    }

    @Test
    public void shouldThrowExceptionWhenAddingDoctorWithLevelOutOfRange() {
        Doctor invalidDoctor = Doctor.newBuilder()
                .setName("JohnDoe")
                .setLevel(6).build();
        assertThrows(IllegalArgumentException.class, () -> {
            repository.addDoctor(invalidDoctor);
        });
    }

    @Test
    public void shouldThrowExceptionWhenDoctorAlreadyExists() {
        Doctor doctor = Doctor.newBuilder()
                .setName("JohnDoe")
                .setLevel(3).build();
        repository.addDoctor(doctor);
        assertThrows(IllegalArgumentException.class, () -> {
            repository.addDoctor(doctor);
        });
    }

    @Test
    public void shouldThrowExceptionWhenDoctorNotFound() {
        Doctor doctor = Doctor.newBuilder()
                .setName("JohnDoe")
                .setLevel(3).build();
        assertThrows(IllegalArgumentException.class, () -> {
            repository.modifyDoctor(doctor);
        });
    }

    @Test
    public void shouldGetNextAvailableDoctorClosestFit() {
        Doctor doctor1 = Doctor.newBuilder()
                .setName("Doctor1").setLevel(1)
                .setAvailability(AvailabilityStatus.AVAILABILITY_STATUS_AVAILABLE)
                .build();
        Doctor doctor2 = Doctor.newBuilder()
                .setName("Doctor2").setLevel(2)
                .setAvailability(AvailabilityStatus.AVAILABILITY_STATUS_UNAVAILABLE)
                .build();
        Doctor doctor3 = Doctor.newBuilder()
                .setName("Doctor3").setLevel(2)
                .setAvailability(AvailabilityStatus.AVAILABILITY_STATUS_ATTENDING)
                .build();
        Doctor doctor4 = Doctor.newBuilder()
                .setName("Doctor4").setLevel(3)
                .setAvailability(AvailabilityStatus.AVAILABILITY_STATUS_AVAILABLE)
                .build();
        Doctor doctor5 = Doctor.newBuilder()
                .setName("Doctor5").setLevel(5)
                .setAvailability(AvailabilityStatus.AVAILABILITY_STATUS_UNAVAILABLE)
                .build();
        repository.addDoctor(doctor1);
        repository.addDoctor(doctor2);
        repository.addDoctor(doctor3);
        repository.addDoctor(doctor4);
        repository.addDoctor(doctor5);
        assertEquals(doctor4, repository.findNextAvailableDoctorClosestFit(2).get());
    }

    @Test
    public void shouldReturnEmptyIfAvailableDoctorHasLowerLevel() {
        Doctor doctor1 = Doctor.newBuilder()
                .setName("Doctor1").setLevel(1)
                .setAvailability(AvailabilityStatus.AVAILABILITY_STATUS_AVAILABLE)
                .build();
        Doctor doctor2 = Doctor.newBuilder()
                .setName("Doctor2").setLevel(2)
                .setAvailability(AvailabilityStatus.AVAILABILITY_STATUS_UNAVAILABLE)
                .build();
        Doctor doctor3 = Doctor.newBuilder()
                .setName("Doctor3").setLevel(3)
                .setAvailability(AvailabilityStatus.AVAILABILITY_STATUS_ATTENDING)
                .build();
        repository.addDoctor(doctor1);
        repository.addDoctor(doctor2);
        repository.addDoctor(doctor3);
        assertFalse(repository.findNextAvailableDoctorClosestFit(2).isPresent());
    }

    @Test
    public void shouldModifyDoctor() {
        Doctor doctor = Doctor.newBuilder()
                .setName("JohnDoe")
                .setAvailability(AvailabilityStatus.AVAILABILITY_STATUS_AVAILABLE)
                .setLevel(3)
                .build();
        repository.addDoctor(doctor);
        Doctor modifiedDoctor = doctor.toBuilder()
                .setAvailability(AvailabilityStatus.AVAILABILITY_STATUS_ATTENDING)
                .build();
        repository.modifyDoctor(modifiedDoctor);
        assertEquals(modifiedDoctor, repository.getDoctor("JohnDoe").get());
    }
}
