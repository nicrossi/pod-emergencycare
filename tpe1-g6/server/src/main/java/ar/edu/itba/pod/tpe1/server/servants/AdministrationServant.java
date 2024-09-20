package ar.edu.itba.pod.tpe1.server.servants;

import ar.edu.itba.pod.tpe1.administration.AdministrationServiceGrpc;
import ar.edu.itba.pod.tpe1.administration.AddDoctorRequest;
import ar.edu.itba.pod.tpe1.administration.Doctor;
import ar.edu.itba.pod.tpe1.administration.SetDoctorRequest;
import ar.edu.itba.pod.tpe1.server.DoctorsRepository;
import ar.edu.itba.pod.tpe1.server.PatientsRepository;
import ar.edu.itba.pod.tpe1.server.RoomsRepository;
import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        logger.info("Added room #" + roomId);
        responseObserver.onNext(Int32Value.of(roomId));
        responseObserver.onCompleted();
    }

    public void addDoctor(AddDoctorRequest request, StreamObserver<Doctor> responseObserver) {
        logger.info("Adding doctor ...");
        Doctor reqDoctor = Doctor.newBuilder()
                .setName(request.getDoctorName())
                .setLevel(request.getLevel())
                .build();
        logger.info("Added doctor " + reqDoctor.toString());
        Doctor response = doctorsRepository.addDoctor(reqDoctor);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public void setDoctor(SetDoctorRequest request, StreamObserver<Doctor> responseObserver) {
        logger.info("Setting doctor ...");
    }

    public void checkDoctor(StringValue request, StreamObserver<Doctor> responseObserver) {
        logger.info("Checking doctor ...");
    }
}
