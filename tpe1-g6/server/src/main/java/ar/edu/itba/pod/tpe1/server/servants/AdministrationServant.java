package ar.edu.itba.pod.tpe1.server.servants;

import ar.edu.itba.pod.tpe1.administration.*;
import ar.edu.itba.pod.tpe1.server.repository.DoctorsRepository;
import ar.edu.itba.pod.tpe1.server.repository.RoomsRepository;
import com.google.protobuf.Any;
import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;
import com.google.protobuf.Empty;
import com.google.rpc.Code;
import com.google.rpc.ErrorInfo;
import com.google.rpc.StatusProto;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class AdministrationServant extends AdministrationServiceGrpc.AdministrationServiceImplBase {
    private static final Logger logger = LoggerFactory.getLogger(AdministrationServant.class);

    private final RoomsRepository roomsRepository;
    private final DoctorsRepository doctorsRepository;

    public AdministrationServant(DoctorsRepository dR, RoomsRepository rR) {
        roomsRepository = rR;
        doctorsRepository = dR;
    }


    public void addRoom(Empty empty, StreamObserver<Int32Value> responseObserver) {
        logger.info("Adding room ...");
        int roomId = roomsRepository.addRoom();
        logger.info("Added room #{}", roomId);
        responseObserver.onNext(Int32Value.of(roomId));
        responseObserver.onCompleted();
    }

    public void addDoctor(AddDoctorRequest request, StreamObserver<Doctor> responseObserver) {
        logger.info("Adding doctor ...");
        Doctor reqDoctor = Doctor.newBuilder()
                .setName(request.getDoctorName())
                .setLevel(request.getLevel())
                .build();
        logger.info("Added doctor {}", reqDoctor);
        Doctor response = doctorsRepository.addDoctor(reqDoctor);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public void setDoctor(SetDoctorRequest request, StreamObserver<Doctor> responseObserver) {
        logger.info("Setting doctor ...");
        Optional<Doctor> prevDoctor = doctorsRepository.getDoctor(request.getDoctorName());

        prevDoctor.ifPresentOrElse(doctor -> {
            Doctor nextDoctor = doctor.toBuilder().setAvailability(request.getAvailability()).build();
            if (doctor.getAvailability() == AvailabilityStatus.AVAILABILITY_STATUS_ATTENDING) {
                throw new IllegalArgumentException("Doctor is already attending");
            }
            Doctor response = doctorsRepository.modifyDoctor(nextDoctor);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }, () -> {
            throw new IllegalArgumentException("Doctor not found");
        });
    }

    public void checkDoctor(StringValue request, StreamObserver<Doctor> responseObserver) {
        logger.info("Checking doctor ...");
        Optional<Doctor> reqDoctor = doctorsRepository.getDoctor(request.getValue());

        reqDoctor.ifPresentOrElse(
                doctor -> {
                    responseObserver.onNext(doctor);
                    responseObserver.onCompleted();
                },
                () -> {
                    throw new IllegalArgumentException("Doctor not found");
                });
    }
}
