package ar.edu.itba.pod.tpe1.client.service.strategy;

import ar.edu.itba.pod.tpe1.client.service.util.EmergencyCareUtil;
import ar.edu.itba.pod.tpe1.emergencyCare.CarePatientResponse;
import ar.edu.itba.pod.tpe1.emergencyCare.DischargePatientRequest;
import ar.edu.itba.pod.tpe1.emergencyCare.DischargePatientResponse;
import ar.edu.itba.pod.tpe1.emergencyCare.EmergencyCareServiceGrpc;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

public class EmergencyCareStrategy extends AbstractServiceStrategy {
    private static final Logger logger = LoggerFactory.getLogger(EmergencyCareStrategy.class);
    private final EmergencyCareServiceGrpc.EmergencyCareServiceStub stub;

    public EmergencyCareStrategy(String serviceAddress) {
        super(logger, ManagedChannelBuilder.forTarget(serviceAddress).usePlaintext().build());
        this.stub = EmergencyCareServiceGrpc.newStub(channel);
    }

    @Override
    protected Runnable getActionTask(String action, CountDownLatch latch) {
        // TODO: implement
        StreamObserver<CarePatientResponse> carePatientObserver = new StreamObserver<>() {
            @Override
            public void onNext(CarePatientResponse value) {
                logger.info("Patient {} ({}) and Doctor {} ({}) are now in Room #{}",
                        value.getEffect().getPatientName(),
                        value.getEffect().getPatientLevel(),
                        value.getEffect().getDoctorName(),
                        value.getEffect().getDoctorLevel(),
                        value.getRoom());
            }

            @Override
            public void onError(Throwable t) {
                logger.error("An error occurred while processing a request: {}", t.getMessage(), t);
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        };

        StreamObserver<DischargePatientResponse> dischargePatientObserver = new StreamObserver<>() {
            @Override
            public void onNext(DischargePatientResponse value) {
                logger.info("Patient {} ({}) and Doctor {} ({}) are now in Room # {}: {}",
                        value.getPatientName(),
                        value.getPatientLevel(),
                        value.getDoctorName(),
                        value.getDoctorLevel(),
                        value.getRoom(),
                        value.toString()
                );
            }

            @Override
            public void onError(Throwable t) {
                logger.error("An error occurred while processing a request: {}", t.getMessage(), t);
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        };

        //TODO: implement the rest
        return switch (action) {
            case "carePatient" -> () -> stub.carePatient(EmergencyCareUtil.getCarePatientRequest(), carePatientObserver);
            case "dischargePatient" -> () -> stub.dischargePatient(EmergencyCareUtil.getDischargePatientRequest(), dischargePatientObserver);
            default -> null;
        };
    }
}
