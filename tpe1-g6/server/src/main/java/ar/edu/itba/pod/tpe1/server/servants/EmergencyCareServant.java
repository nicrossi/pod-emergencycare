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

import java.util.Iterator;
import java.util.Optional;

public class EmergencyCareServant extends EmergencyCareServiceGrpc.EmergencyCareServiceImplBase {
    private static final Logger logger = LoggerFactory.getLogger(EmergencyCareServant.class);

    private final PatientsRepository patientsRepository;
    private final RoomsRepository roomsRepository;
    private final DoctorsRepository doctorsRepository;
    private final HistoryRepository historyRepository;
    private final CareRespository careRepository;


    public EmergencyCareServant(PatientsRepository pR, DoctorsRepository dR, RoomsRepository rR, HistoryRepository hR, CareRespository cR) {
        patientsRepository = pR;
        doctorsRepository = dR;
        roomsRepository = rR;
        historyRepository = hR;
        careRepository = cR;
    }


    public void carePatient(CarePatientRequest request, StreamObserver<CarePatientResponse> responseObserver) {
        logger.info("Attending patient ...");
        // TODO: we need to lock all repos until we either start care or find it isn't possible
        int roomId = request.getRoom();
        RoomStatus currentRoomStatus = validateAndGetRoomAvailability(responseObserver, roomId);
        CaredInfo caredInfo = null;
        CarePatientResponse.Builder careResp = CarePatientResponse.newBuilder().setRoom(roomId);

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
                } else {
                    careResp.setStatus(currentRoomStatus);
                }

                break; // exit the iteration
            }
        }
        if (caredInfo == null) {
            logger.info("No patient could be attended");
            logger.info("Room #{} remains {}", roomId, currentRoomStatus);
            careResp.setStatus(currentRoomStatus);
        }
        responseObserver.onNext(careResp.build());
        responseObserver.onCompleted();
    }

    public void careAllPatients(Empty empty, StreamObserver<CareAllPatientsResponse> responseObserver) {
        logger.info("Attending all patients ...");
    }

    public void dischargePatient(DischargePatientRequest request, StreamObserver<DischargePatientResponse> responseObserver) {
        logger.info("Discharging patient ...");
        //remember to add to historyRepository! it's supposed to store completed cares
        CaredInfo caredInfo = careRepository.endCare(request.getRoom(), request.getPatientName(), request.getDoctorName());
        historyRepository.addHistory(caredInfo);
        dischargeRoomAndDoctor(responseObserver, request.getRoom(), caredInfo.getDoctor());

        //TODO: there has to be a better way to do this...
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
            RoomStatus currentRoomStatus = roomsRepository.getRoomStatus(roomId);
            if (currentRoomStatus != RoomStatus.ROOM_STATUS_FREE) {
                StatusRuntimeException error = Status.FAILED_PRECONDITION
                        .withDescription("Room #" + roomId + " remains occupied")
                        .asRuntimeException();
                responseObserver.onError(error);
            }
            return currentRoomStatus;
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
