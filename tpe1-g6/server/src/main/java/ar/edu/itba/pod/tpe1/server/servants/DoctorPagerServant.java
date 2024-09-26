package ar.edu.itba.pod.tpe1.server.servants;

import ar.edu.itba.pod.tpe1.administration.Doctor;
import ar.edu.itba.pod.tpe1.doctorPager.DoctorPagerRequest;
import ar.edu.itba.pod.tpe1.doctorPager.DoctorPagerResponse;
import ar.edu.itba.pod.tpe1.doctorPager.DoctorPagerServiceGrpc;
import ar.edu.itba.pod.tpe1.server.repository.DoctorsRepository;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DoctorPagerServant extends DoctorPagerServiceGrpc.DoctorPagerServiceImplBase {
    private static final Logger logger = LoggerFactory.getLogger(DoctorPagerServant.class);

    private final ConcurrentMap<String, StreamObserver<DoctorPagerResponse>> doctorObservers = new ConcurrentHashMap<>();
    private final DoctorsRepository doctorsRepository;

    public DoctorPagerServant(DoctorsRepository doctorsRepository) {
        this.doctorsRepository = doctorsRepository;
    }

    public void register(final DoctorPagerRequest request, final StreamObserver<DoctorPagerResponse> responseObserver) {
        String doctorName = request.getDoctorName();
        Doctor doctor = doctorsRepository.getDoctor(doctorName).orElseThrow(() -> {
            RuntimeException error = new IllegalArgumentException("Doctor not found");
            responseObserver.onError(error);
            return error;
        });
        logger.info("Registering doctor: {} for live notifications", doctorName);
        if (!doctorObservers.containsKey(doctorName)) {
            doctorObservers.put(doctorName, responseObserver);
            String message = "Doctor %s (%s) registered successfully for pager".formatted(doctorName, doctor.getLevel());
            logger.info(message);
            responseObserver.onNext(DoctorPagerResponse.newBuilder()
                    .setMessage(message)
                    .build());
        } else {
            String message = "Doctor %s (%s) already registered for pager".formatted(doctorName, doctor.getLevel());
            logger.error(message);
            StatusRuntimeException sre = Status.FAILED_PRECONDITION
                    .withDescription(message)
                    .asRuntimeException();
            responseObserver.onError(sre);
            throw sre;
        }
    }

    public void unregister(final DoctorPagerRequest request, final StreamObserver<DoctorPagerRequest> responseObserver) {
        logger.info("unregister doctor ...");
    }

    public void notifyDoctor(String doctorName, String eventMessage) {
        StreamObserver<DoctorPagerResponse> observer = doctorObservers.get(doctorName);
        if (observer != null) {
            observer.onNext(DoctorPagerResponse.newBuilder()
                    .setMessage(eventMessage)
                    .build());
        }
    }
}
