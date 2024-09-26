package ar.edu.itba.pod.tpe1.server.servants;

import ar.edu.itba.pod.tpe1.administration.*;
import ar.edu.itba.pod.tpe1.server.repository.DoctorsRepository;
import ar.edu.itba.pod.tpe1.server.repository.RoomsRepository;
import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;

public class AdministrationServant extends AdministrationServiceGrpc.AdministrationServiceImplBase {
    private static final Logger logger = LoggerFactory.getLogger(AdministrationServant.class);

    private final ReadWriteLock lock;
    private final RoomsRepository roomsRepository;
    private final DoctorsRepository doctorsRepository;

    public AdministrationServant(DoctorsRepository dR, RoomsRepository rR, ReadWriteLock lock) {
        roomsRepository = rR;
        doctorsRepository = dR;
        this.lock = lock;
    }


    public void addRoom(Empty empty, StreamObserver<Int32Value> responseObserver) {
        logger.info("Adding room ...");
        int roomId;

        lock.writeLock().lock();
        try{
            roomId = roomsRepository.addRoom();
        }finally{
            lock.writeLock().unlock();
        }

        logger.info("Added room #{}", roomId);
        responseObserver.onNext(Int32Value.of(roomId));
        responseObserver.onCompleted();
    }

    public void addDoctor(AddDoctorRequest request, StreamObserver<Doctor> responseObserver) {
        logger.info("Adding doctor ...");
        Doctor reqDoctor = Doctor.newBuilder()
                .setName(request.getDoctorName())
                .setLevel(request.getLevel())
                .setAvailability(AvailabilityStatus.AVAILABILITY_STATUS_UNAVAILABLE)
                .build();
        logger.info("Added doctor {}", reqDoctor);

        Doctor response;

        lock.writeLock().lock();
        try{
            response = doctorsRepository.addDoctor(reqDoctor);
        }finally{
            lock.writeLock().unlock();
        }

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public void setDoctor(SetDoctorRequest request, StreamObserver<Doctor> responseObserver) {
        logger.info("Setting doctor ...");
        if(request.getAvailability() == AvailabilityStatus.AVAILABILITY_STATUS_ATTENDING) {
            throw new IllegalArgumentException("Only 'available' and 'unavailable' are allowed");
        }

        Optional<Doctor> prevDoctor;
        lock.readLock().lock();
        try{
            prevDoctor= doctorsRepository.getDoctor(request.getDoctorName());
        }finally{
            lock.readLock().unlock();
        }

        prevDoctor.ifPresentOrElse(doctor -> {
            Doctor nextDoctor = doctor.toBuilder().setAvailability(request.getAvailability()).build();
            if (doctor.getAvailability() == AvailabilityStatus.AVAILABILITY_STATUS_ATTENDING) {
                throw new IllegalArgumentException("Doctor is already attending");
            }

            Doctor response;
            lock.writeLock().lock();
            try{
                response = doctorsRepository.modifyDoctor(nextDoctor);
            }finally{
                lock.writeLock().unlock();
            }

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }, () -> {
            throw new IllegalArgumentException("Doctor not found");
        });
    }

    public void checkDoctor(StringValue request, StreamObserver<Doctor> responseObserver) {
        logger.info("Checking doctor ...");

        Optional<Doctor> reqDoctor;

        lock.readLock().lock();
        try{
            reqDoctor= doctorsRepository.getDoctor(request.getValue());
        }finally {
            lock.readLock().unlock();
        }

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
