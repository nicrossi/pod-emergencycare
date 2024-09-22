package ar.edu.itba.pod.tpe1.server.servants;

import ar.edu.itba.pod.tpe1.administration.Doctor;
import ar.edu.itba.pod.tpe1.emergencyCare.*;
import ar.edu.itba.pod.tpe1.query.CaredInfo;
import ar.edu.itba.pod.tpe1.server.repository.*;
import ar.edu.itba.pod.tpe1.waitingRoom.Patient;
import ar.edu.itba.pod.tpe1.waitingRoom.PatientState;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        //TODO: remove this hardcoded mess
        Patient patient = patientsRepository.getPatient("Foo");
        Doctor doctor = doctorsRepository.getDoctor("John").get();
        int roomId = roomsRepository.getRoomStatus(request.getRoom()) == RoomStatus.ROOM_STATUS_FREE ?
                request.getRoom() : 1;
        roomsRepository.setRoomStatus(roomId, RoomStatus.ROOM_STATUS_OCCUPIED);

        //TODO: we need to lock all repos until we either start care or find it isn't possible
        CaredInfo caredInfo = careRepository.startCare(roomId, patient, doctor);
        CarePatientInfo effect = CarePatientInfo.newBuilder()
                .setDoctorName(doctor.getName())
                .setDoctorLevel(doctor.getLevel())
                .setPatientName(patient.getPatientName())
                .setPatientLevel(patient.getLevel())
                .build();

        CarePatientResponse.Builder careResp = CarePatientResponse.newBuilder()
                .setRoom(roomId);

        // if care couldn't be started, only set status
        if (caredInfo != null) {
            careResp.setEffect(effect);
        }else {
            careResp.setStatus(roomsRepository.getRoomStatus(roomId));
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
        roomsRepository.setRoomStatus(request.getRoom(), RoomStatus.ROOM_STATUS_FREE);

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
}
