package ar.edu.itba.pod.tpe1.server.servants;

import ar.edu.itba.pod.tpe1.administration.AvailabilityStatus;
import ar.edu.itba.pod.tpe1.administration.Doctor;
import ar.edu.itba.pod.tpe1.emergencyCare.*;
import ar.edu.itba.pod.tpe1.query.CaredInfo;
import ar.edu.itba.pod.tpe1.server.repository.*;
import ar.edu.itba.pod.tpe1.waitingRoom.Patient;
import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;

import static ar.edu.itba.pod.tpe1.emergencyCare.EmergencyCareServiceModel.stringName;

public class EmergencyCareServant extends EmergencyCareServiceGrpc.EmergencyCareServiceImplBase {
    private static final Logger logger = LoggerFactory.getLogger(EmergencyCareServant.class);

    private final PatientsRepository patientsRepository;
    private final RoomsRepository roomsRepository;
    private final DoctorsRepository doctorsRepository;
    private final HistoryRepository historyRepository;
    private final CareRespository careRepository;
    private final DoctorPagerServant doctorPagerServant;

    private final ReadWriteLock lock;

    public EmergencyCareServant(PatientsRepository pR, DoctorsRepository dR, RoomsRepository rR, HistoryRepository hR, CareRespository cR,
                                DoctorPagerServant pager, ReadWriteLock lock) {
        patientsRepository = pR;
        doctorsRepository = dR;
        roomsRepository = rR;
        historyRepository = hR;
        careRepository = cR;
        doctorPagerServant = pager;
        this.lock = lock;
    }


    public void carePatient(CarePatientRequest request, StreamObserver<CarePatientResponse> responseObserver) {
        logger.info("Attending patient ...");

        lock.writeLock().lock();
        try {
            int roomId = request.getRoom();
            CaredInfo caredInfo = null;
            CarePatientResponse.Builder careResp = CarePatientResponse.newBuilder().setRoom(roomId);
            RoomStatus currentRoomStatus = validateAndGetRoomAvailability(responseObserver, roomId);

            if (currentRoomStatus == RoomStatus.ROOM_STATUS_FREE) {
                Iterator<Patient>  iter = patientsRepository.waitingRoomIterator();
                while(iter.hasNext()) {
                    Patient patient = iter.next();
                    Optional<Doctor> optionalDoctor = doctorsRepository.findNextAvailableDoctorClosestFit(patient.getLevel());
                    // if doctor si not found, then check another patient
                    if (optionalDoctor.isPresent()) {
                        Doctor doctor = doctorsRepository.setDoctorAvailabilityStatus(optionalDoctor.get(), AvailabilityStatus.AVAILABILITY_STATUS_ATTENDING);
                        iter.remove();
                        caredInfo = careRepository.startCare(roomId, patient, doctor);
                        CarePatientInfo effect = CarePatientInfo.newBuilder()
                                .setDoctorName(doctor.getName())
                                .setDoctorLevel(doctor.getLevel())
                                .setPatientName(patient.getPatientName())
                                .setPatientLevel(patient.getLevel())
                                .build();

                        // if care couldn't be started, only set status
                        if (caredInfo != null) {
                            roomsRepository.setRoomStatus(roomId, RoomStatus.ROOM_STATUS_OCCUPIED);
                            careResp.setEffect(effect);
                            // notify doctor
                            String messageEvent = "Patient %s (%s) and Doctor %s (%s) are now in Room #%d"
                                    .formatted(patient.getPatientName(), patient.getLevel(), doctor.getName(), doctor.getLevel(), roomId);
                            doctorPagerServant.notifyDoctor(doctor.getName(), messageEvent);
                        } else {
                            careResp.setStatus(currentRoomStatus);
                        }

                        break; // exit the iteration
                    }
                }
            }

            if (caredInfo == null) {
                logger.info("No patient could be attended");
                String statusName = currentRoomStatus.getValueDescriptor().getOptions().getExtension(stringName);
                logger.info("Room #{} remains {}", roomId, statusName);
                careResp.setStatus(currentRoomStatus);
            }
            responseObserver.onNext(careResp.build());
            responseObserver.onCompleted();

        } finally {
            lock.writeLock().unlock();
        }

    }

    public void careAllPatients(Empty empty, StreamObserver<CareAllPatientsResponse> responseObserver) {
        logger.info("Attending all patients ...");
        List<CarePatientResponse> carePatientResponses = new ArrayList<>();
        CareAllPatientsResponse.Builder response = CareAllPatientsResponse.newBuilder();

        lock.writeLock().lock();
        try {
            List<RoomStatus> rooms = roomsRepository.getRooms();
            for (int i = 0; i < rooms.size(); i++) {
                int roomId = i + 1;
                CarePatientRequest request = CarePatientRequest.newBuilder()
                        .setRoom(roomId)
                        .build();
                carePatient(request, new StreamObserver<>() {
                    @Override
                    public void onNext(CarePatientResponse value) {
                        carePatientResponses.add(value);
                    }

                    @Override
                    public void onError(Throwable t) {
                        if (t instanceof StatusRuntimeException statusEx) {
                            logger.error(statusEx.getStatus().getDescription());
                        } else {
                            logger.error("An unexpected error occurred: {}", t.getMessage(), t);
                        }
                    }

                    @Override
                    public void onCompleted() {}
                });
            }

            response.addAllStates(carePatientResponses);
            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error while caring for patients: {}", e.getMessage(), e);
            responseObserver.onError(Status.INTERNAL.withDescription("Error while caring for patients").asRuntimeException());
        }finally{
            lock.writeLock().unlock();
        }
    }

    public void dischargePatient(DischargePatientRequest request, StreamObserver<DischargePatientResponse> responseObserver) {
        logger.info("Discharging patient ...");
        CaredInfo caredInfo;
        lock.writeLock().lock();
        try{
            caredInfo = careRepository.endCare(request.getRoom(), request.getPatientName(), request.getDoctorName());
            historyRepository.addHistory(caredInfo);
            dischargeRoomAndDoctor(responseObserver, request.getRoom(), caredInfo.getDoctor());
            // notify doctor
            String messageEvent = "Patient %s (%s) has been discharged from Doctor %s (%s) and the Room #%d is now Free"
                    .formatted(caredInfo.getPatient().getPatientName(), caredInfo.getPatient().getLevel(),
                               caredInfo.getDoctor().getName(), caredInfo.getDoctor().getLevel(), caredInfo.getRoomId());
            doctorPagerServant.notifyDoctor(caredInfo.getDoctor().getName(), messageEvent);
        } finally {
            lock.writeLock().unlock();
        }

        DischargePatientResponse response = DischargePatientResponse.newBuilder()
                .setDoctorLevel(caredInfo.getDoctor().getLevel())
                .setDoctorName(caredInfo.getDoctor().getName())
                .setPatientLevel(caredInfo.getPatient().getLevel())
                .setPatientName(caredInfo.getPatient().getPatientName())
                .setRoom(caredInfo.getRoomId())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private RoomStatus validateAndGetRoomAvailability(StreamObserver<CarePatientResponse> responseObserver, int roomId) {
        try {
            return roomsRepository.getRoomStatus(roomId);
        } catch (Exception e) {
            logger.error("Couldn't start patient caring: {}", e.getMessage(), e);
            StatusRuntimeException sre = Status.FAILED_PRECONDITION
                    .withDescription(e.getMessage())
                    .asRuntimeException();
            responseObserver.onError(sre);
            throw e;
        }
    }

    private void dischargeRoomAndDoctor(StreamObserver<DischargePatientResponse> responseObserver, int roomId, Doctor doctor) {
        RoomStatus prevStatus = null;
        try {
            prevStatus = roomsRepository.getRoomStatus(roomId);
            roomsRepository.setRoomStatus(roomId, RoomStatus.ROOM_STATUS_FREE);
            doctorsRepository.setDoctorAvailabilityStatus(doctor, AvailabilityStatus.AVAILABILITY_STATUS_AVAILABLE);
        } catch (Exception e) {
            logger.error("Couldn't discharge patient: {}", e.getMessage(), e);
            if (prevStatus != null) {
                roomsRepository.setRoomStatus(roomId, prevStatus);
            }
            StatusRuntimeException sre = Status.FAILED_PRECONDITION
                    .withDescription(e.getMessage())
                    .asRuntimeException();
            responseObserver.onError(sre);
            throw e;
        }
    }
}
