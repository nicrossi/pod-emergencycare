package ar.edu.itba.pod.tpe1.client.service.strategy;

import ar.edu.itba.pod.tpe1.client.service.util.WaitingRoomClientUtil;
import ar.edu.itba.pod.tpe1.waitingRoom.Patient;
import ar.edu.itba.pod.tpe1.waitingRoom.PatientState;
import ar.edu.itba.pod.tpe1.waitingRoom.WaitingRoomServiceGrpc;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

public class WaitingRoomStrategy extends AbstractServiceStrategy {
    private static final Logger logger = LoggerFactory.getLogger(WaitingRoomStrategy.class);
    private final WaitingRoomServiceGrpc.WaitingRoomServiceStub stub;

    public WaitingRoomStrategy(String serviceAddress) {
        super(logger, ManagedChannelBuilder.forTarget(serviceAddress).usePlaintext().build());
        this.stub = WaitingRoomServiceGrpc.newStub(channel);
    }

    @Override
    protected Runnable getActionTask(String action, CountDownLatch latch) {
        StreamObserver<Patient> addPatientObserver = new StreamObserver<>() {
            @Override
            public void onNext(Patient patient) {
                logger.info("Patient {} ({}) is in the waiting room", patient.getPatientName(), patient.getLevel());
            }

            @Override
            public void onError(Throwable throwable) {
                logger.error("Failed to add patient: {}", throwable.getMessage());
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        };
        StreamObserver<PatientState> checkPatientObserver = new StreamObserver<>() {
            @Override
            public void onNext(PatientState ps) {
                logger.info("Patient {} ({}) is in the waiting room with {} patients ahead",
                        ps.getPatientName(), ps.getLevel(), ps.getQueuePlace());
            }

            @Override
            public void onError(Throwable throwable) {
                logger.error("Failed to check patient: {}", throwable.getMessage());
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        };

        return switch (action) {
            case "addPatient" -> () -> stub.addPatient(WaitingRoomClientUtil.getAddPatientRequest(), addPatientObserver);
            case "updateLevel" -> () -> stub.updateLevel(WaitingRoomClientUtil.getUpdateLevelRequest(), addPatientObserver);
            case "checkPatient" -> () -> stub.checkPatient(WaitingRoomClientUtil.getCheckPatientRequest(), checkPatientObserver);
            default -> null;
        };
    }
}
