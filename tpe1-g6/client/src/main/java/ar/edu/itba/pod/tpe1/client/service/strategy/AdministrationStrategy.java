package ar.edu.itba.pod.tpe1.client.service.strategy;

import ar.edu.itba.pod.tpe1.administration.AdministrationServiceGrpc;
import ar.edu.itba.pod.tpe1.administration.Doctor;
import ar.edu.itba.pod.tpe1.client.service.util.AdministrationClientUtil;
import com.google.protobuf.Int32Value;
import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class AdministrationStrategy implements ServiceStrategy {
    private static final Logger logger = LoggerFactory.getLogger(AdministrationStrategy.class);
    private final AdministrationServiceGrpc.AdministrationServiceStub stub;
    private final ManagedChannel channel;

    public AdministrationStrategy(String serviceAddress) {
        channel = ManagedChannelBuilder.forTarget(serviceAddress)
                .usePlaintext()
                .build();
        stub = AdministrationServiceGrpc.newStub(channel);
    }

    @Override
    public void execute(String action) {
        CountDownLatch latch = new CountDownLatch(1);

        Runnable actionTask = getActionTask(action, latch);
        if (Objects.isNull(actionTask)) {
            logger.error("Unknown action: {}", action);
            latch.countDown();
            return;
        }

        try {
            actionTask.run();
            latch.await(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error("Execution interrupted: {}", e.getMessage());
        } finally {
            channel.shutdown();
            // TODO maybe add some additional validation to check if the channel is terminated ?
        }
    }

    private Runnable getActionTask(String action, CountDownLatch latch) {
        StreamObserver<Int32Value> addRoomObserver = new StreamObserver<>() {
            @Override
            public void onNext(Int32Value roomNumber) {
                logger.info("Room #{} added successfully", roomNumber.getValue());
                // TODO: implement
            }

            @Override
            public void onError(Throwable t) {
                logger.error("Failed to add room: {}", t.getMessage(), t);
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        };
        StreamObserver<Doctor> addDoctorObserver = new StreamObserver<>() {
            @Override
            public void onNext(Doctor doctor) {
                logger.info("Doctor {} ({}) added successfully", doctor.getName(), doctor.getLevel());
                // TODO: implement
            }

            @Override
            public void onError(Throwable t) {
                logger.error("Failed to add doctor: {}", t.getMessage());
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        };
        StreamObserver<Doctor> setDoctorObserver = new StreamObserver<>() {
            @Override
            public void onNext(Doctor doctor) {
                logger.info("Doctor {} ({}) is {}", doctor.getName(), doctor.getLevel(), doctor.getAvailability());
                // TODO: implement
            }

            @Override
            public void onError(Throwable t) {
                logger.error("Failed to set doctor's availability: {}", t.getMessage());
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        };
        StreamObserver<Doctor> checkDoctorObserver = new StreamObserver<>() {
            @Override
            public void onNext(Doctor doctor) {
                logger.info("Doctor {} ({}) is {}", doctor.getName(), doctor.getLevel(), doctor.getAvailability());
                // TODO: implement
            }

            @Override
            public void onError(Throwable t) {
                logger.error("Failed to check doctor's availability: {}", t.getMessage());
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        };

        return switch (action) {
            case "addRoom" -> () -> stub.addRoom(Empty.getDefaultInstance(), addRoomObserver);
            case "addDoctor" -> () -> stub.addDoctor(AdministrationClientUtil.getAddDoctorRequest(), addDoctorObserver);
            case "setDoctor" -> () -> stub.setDoctor(AdministrationClientUtil.getSetDoctorRequest(), setDoctorObserver);
            case "checkDoctor" -> () -> stub.checkDoctor(AdministrationClientUtil.getCheckDoctorRequest(), checkDoctorObserver);
            default -> null;
        };
    }
}