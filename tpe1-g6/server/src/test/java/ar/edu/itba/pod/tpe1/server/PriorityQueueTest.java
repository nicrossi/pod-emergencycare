package ar.edu.itba.pod.tpe1.server;

import ar.edu.itba.pod.tpe1.server.model.ComparablePatient;
import ar.edu.itba.pod.tpe1.waitingRoom.PatientSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class PriorityQueueTest {

    @Test
    public void testPriorityBlockingQueue() throws InterruptedException {
        // System under test
        PriorityBlockingQueue<ComparablePatient> queue = new PriorityBlockingQueue<>();

        final int INSERTED_PATIENTS = 100;

        List<ComparablePatient> insertionOrder = Collections.synchronizedList(new ArrayList<>());
        Random random = new Random();
        ExecutorService executorService = Executors.newFixedThreadPool(20);

        for (int i = 1; i <= INSERTED_PATIENTS; i++) {
            final int id = i;
            executorService.submit(() -> {
                try {
                    int level = random.nextInt(5) + 1;
                    PatientSet patientSet = PatientSet.newBuilder()
                            .setPatientName("Patient-" + id)
                            .setLevel(level)
                            .build();
                    ComparablePatient patient = new ComparablePatient(patientSet);
                    queue.add(patient);
                    insertionOrder.add(patient);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        // Shutdown the executor service and wait for tasks to complete
        executorService.shutdown();
        if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
            System.err.println("Executor service did not terminate in the specified time.");
            executorService.shutdownNow();
        }
        TimeUnit.SECONDS.sleep(10);

        // Expect the queue to have the same size as the insertion order
        assertEquals(INSERTED_PATIENTS, insertionOrder.size());
        // Sort the insertion order to compare with the queue
        Collections.sort(insertionOrder);
        // First element should be the highest level
        assert queue.peek() != null;
        assertEquals(5, queue.peek().getLevel());

        for (ComparablePatient expected : insertionOrder) {
            // Traversing does not guarantee order, so we poll
            ComparablePatient current = queue.poll();
            assertNotNull(current);
            assertEquals(expected.getName(), current.getName());
            assertEquals(expected.getLevel(), current.getLevel());
            assertEquals(expected, current);
        }
    }
}