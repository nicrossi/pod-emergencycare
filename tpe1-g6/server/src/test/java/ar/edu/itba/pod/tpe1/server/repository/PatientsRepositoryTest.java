package ar.edu.itba.pod.tpe1.server.repository;

import ar.edu.itba.pod.tpe1.waitingRoom.Patient;
import ar.edu.itba.pod.tpe1.waitingRoom.PatientState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class PatientsRepositoryTest {
    private static final int MAX_SEVERITY_LEVEL = 5;

    // System under test
    private PatientsRepository repository;


    @BeforeEach
    public void setUp() {
        repository = new PatientsRepository();
    }

    @Test
    public void shouldThrowExceptionWhenAddingPatientWithInvalidLevel() {
        Patient invalidPatient = Patient.newBuilder()
                .setPatientName("JohnDoe")
                .setLevel(6).build();
        assertThrows(IllegalArgumentException.class, () -> {
            repository.addPatient(invalidPatient);
        });
    }

    @Test
    public void shouldAddPatient() {
        Patient patient = Patient.newBuilder()
                .setPatientName("JohnDoe")
                .setLevel(3).build();
        assertEquals(0, repository.getPatientsWaitingCount());
        repository.addPatient(patient);
        assertEquals(1, repository.getPatientsWaitingCount());
        assertEquals(patient, repository.getWaitingRoomNextPatient()
                .orElseThrow(() -> new IllegalStateException("Waiting room is empty")));
    }


    @Test
    public void testCheckPatient() {
        final String MOST_CRITICAL_PATIENT = "MostCriticalPatient";

        Patient patient1 = Patient.newBuilder()
                .setPatientName("Patient1")
                .setLevel(4).build();
        Patient mostCriticalPatient = Patient.newBuilder()
                .setPatientName(MOST_CRITICAL_PATIENT)
                .setLevel(MAX_SEVERITY_LEVEL).build();
        Patient patient3 = Patient.newBuilder()
                .setPatientName("Patient3")
                .setLevel(MAX_SEVERITY_LEVEL).build();
        Patient patient4 = Patient.newBuilder()
                .setPatientName("Patient4")
                .setLevel(2).build();
        repository.addPatient(patient1);
        repository.addPatient(mostCriticalPatient);
        repository.addPatient(patient3);
        repository.addPatient(patient4);

        PatientState state = repository.checkPatient(MOST_CRITICAL_PATIENT);

        assertNotNull(state);
        assertEquals(MOST_CRITICAL_PATIENT, state.getPatientName());
        assertEquals(MAX_SEVERITY_LEVEL, state.getLevel());
        assertEquals(0, state.getQueuePlace()); // Most critical patient is the first in the queue (index 0)
    }

    @Test
    public void udpateLevelPreservingFifoOrderingTest() {
        Patient patient1 = Patient.newBuilder()
                .setPatientName("Patient1")
                .setLevel(4).build();
        Patient patient2 = Patient.newBuilder()
                .setPatientName("Patient2")
                .setLevel(5).build();
        repository.addPatient(patient1);
        repository.addPatient(patient2);
        // Patient 2 is next in the queue
        assertEquals(patient2, repository.peekWaitingRoomNextPatient()
                .orElseThrow(() -> new IllegalStateException("Waiting room is empty")));
        // Update patient 2 level
        patient1 = patient1.toBuilder().setLevel(5).build();
        repository.updateLevel(patient1);
        // Patient 1 is now next in the queue, as it has the same level as patient 2 but was added first
        assertEquals(patient1, repository.peekWaitingRoomNextPatient()
                .orElseThrow(() -> new IllegalStateException("Waiting room is empty")));

    }

}
