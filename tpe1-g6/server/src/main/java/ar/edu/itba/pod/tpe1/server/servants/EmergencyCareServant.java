package ar.edu.itba.pod.tpe1.server.servants;

import ar.edu.itba.pod.tpe1.emergencyCare.CareAllPatientsResponse;
import ar.edu.itba.pod.tpe1.emergencyCare.CarePatientRequest;
import ar.edu.itba.pod.tpe1.emergencyCare.CarePatientResponse;
import ar.edu.itba.pod.tpe1.emergencyCare.DischargePatientRequest;
import ar.edu.itba.pod.tpe1.emergencyCare.DischargePatientResponse;
import ar.edu.itba.pod.tpe1.emergencyCare.EmergencyCareServiceGrpc;
import ar.edu.itba.pod.tpe1.server.repository.DoctorsRepository;
import ar.edu.itba.pod.tpe1.server.repository.HistoryRepository;
import ar.edu.itba.pod.tpe1.server.repository.PatientsRepository;
import ar.edu.itba.pod.tpe1.server.repository.RoomsRepository;
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


    public EmergencyCareServant(PatientsRepository pR, DoctorsRepository dR, RoomsRepository rR, HistoryRepository hR) {
        patientsRepository = pR;
        doctorsRepository = dR;
        roomsRepository = rR;
        historyRepository = hR;
    }


    public void carePatient(CarePatientRequest request, StreamObserver<CarePatientResponse> responseObserver) {
        logger.info("Attending patient ...");
    }

    public void careAllPatients(Empty empty, StreamObserver<CareAllPatientsResponse> responseObserver) {
        logger.info("Attending all patients ...");
    }

    public void dischargePatient(DischargePatientRequest request, StreamObserver<DischargePatientResponse> responseObserver) {
        logger.info("Discharging patient ...");
        //TODO: implement
        //remember to add to historyRepository! it's supposed to store completed cares
    }
}
