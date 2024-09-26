package ar.edu.itba.pod.tpe1.client.service.strategy;

import ar.edu.itba.pod.tpe1.client.service.util.EmergencyCareUtil;
import ar.edu.itba.pod.tpe1.emergencyCare.CareAllPatientsResponse;
import ar.edu.itba.pod.tpe1.emergencyCare.CarePatientResponse;
import ar.edu.itba.pod.tpe1.emergencyCare.DischargePatientResponse;
import ar.edu.itba.pod.tpe1.emergencyCare.EmergencyCareServiceGrpc;
import com.google.protobuf.Empty;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

import static ar.edu.itba.pod.tpe1.emergencyCare.EmergencyCareServiceModel.stringName;

public class EmergencyCareStrategy extends AbstractServiceStrategy {
    private static final Logger logger = LoggerFactory.getLogger(EmergencyCareStrategy.class);
    private final EmergencyCareServiceGrpc.EmergencyCareServiceStub stub;

    public EmergencyCareStrategy(String serviceAddress) {
        super(logger, ManagedChannelBuilder.forTarget(serviceAddress).usePlaintext().build());
        this.stub = EmergencyCareServiceGrpc.newStub(channel);
    }

    @Override
    protected Runnable getActionTask(String action, CountDownLatch latch) {
        StreamObserver<CarePatientResponse> carePatientObserver = new StreamObserver<>() {
            @Override
            public void onNext(CarePatientResponse value) {
                handleCarePatientResponse(value);
            }

            @Override
            public void onError(Throwable t) {
                handleError(t);
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
                logger.info("Patient {} ({}) has been discharged from Doctor {} ({}) ard the Room #{} is now Free",
                        value.getPatientName(),
                        value.getPatientLevel(),
                        value.getDoctorName(),
                        value.getDoctorLevel(),
                        value.getRoom()
                );
            }

            @Override
            public void onError(Throwable t) {
                handleError(t);
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        };
        StreamObserver<CareAllPatientsResponse> careAllPatientsObserver = new StreamObserver<>() {
            @Override
            public void onNext(CareAllPatientsResponse value) {
                value.getStatesList().forEach(EmergencyCareStrategy::handleCarePatientResponse);
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

        return switch (action) {
            case "carePatient" -> () -> stub.carePatient(EmergencyCareUtil.getCarePatientRequest(), carePatientObserver);
            case "dischargePatient" -> () -> stub.dischargePatient(EmergencyCareUtil.getDischargePatientRequest(), dischargePatientObserver);
            case "careAllPatients" -> () -> stub.careAllPatients(Empty.newBuilder().build(), careAllPatientsObserver);
            default -> null;
        };
    }

    private static void handleCarePatientResponse(CarePatientResponse response) {
        if (response.hasStatus()) {
            String statusName = response.getStatus().getValueDescriptor().getOptions().getExtension(stringName);
            logger.info("Room #{} remains {}", response.getRoom(), statusName);
        } else {
            logger.info("Patient {} ({}) and Doctor {} ({}) are now in Room #{}",
                    response.getEffect().getPatientName(),
                    response.getEffect().getPatientLevel(),
                    response.getEffect().getDoctorName(),
                    response.getEffect().getDoctorLevel(),
                    response.getRoom());
        }
    }
}
