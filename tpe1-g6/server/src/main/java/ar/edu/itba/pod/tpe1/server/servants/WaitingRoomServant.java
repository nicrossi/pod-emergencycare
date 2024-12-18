package ar.edu.itba.pod.tpe1.server.servants;

import ar.edu.itba.pod.tpe1.server.repository.PatientsRepository;
import ar.edu.itba.pod.tpe1.waitingRoom.Patient;
import ar.edu.itba.pod.tpe1.waitingRoom.PatientCheck;
import ar.edu.itba.pod.tpe1.waitingRoom.PatientState;
import ar.edu.itba.pod.tpe1.waitingRoom.WaitingRoomServiceGrpc;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.ReadWriteLock;

public class WaitingRoomServant extends WaitingRoomServiceGrpc.WaitingRoomServiceImplBase {
    private static final Logger logger = LoggerFactory.getLogger(WaitingRoomServant.class);
    private final PatientsRepository patientsRepository;

    private final ReadWriteLock lock;

    public WaitingRoomServant(PatientsRepository patientsRepository, ReadWriteLock lock) {
        this.patientsRepository = patientsRepository;
        this.lock = lock;
    }

    public void addPatient(Patient request, StreamObserver<Patient> responseObserver) {
        logger.info("Adding patient ...");
        Patient response;
        lock.writeLock().lock();
        try {
            response = patientsRepository.addPatient(request);
        } finally {
            lock.writeLock().unlock();
        }

        logger.info("Patient {} ({}) is in the waiting room", response.getPatientName(), response.getLevel());
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public void updateLevel(Patient request, StreamObserver<Patient> responseObserver) {
        logger.info("Updating patient level ...");
        Patient response;
        lock.writeLock().lock();
        try {
            response = patientsRepository.updateLevel(request);
        } finally {
            lock.writeLock().unlock();
        }

        logger.info("Patient {} ({}) is in the waiting room", response.getPatientName(), response.getLevel());
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public void checkPatient(PatientCheck request, StreamObserver<PatientState> responseObserver) {
        logger.info("Checking patient ...");
        PatientState response;
        lock.readLock().lock();
        try {
            response = patientsRepository.checkPatient(request.getPatientName());
        } finally {
            lock.readLock().unlock();
        }

        logger.info("Patient {} ({}) is in the waiting room with {} patients ahead",
                response.getPatientName(), response.getLevel(), response.getQueuePlace());
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
