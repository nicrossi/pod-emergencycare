package ar.edu.itba.pod.tpe1.server.servants;

import ar.edu.itba.pod.tpe1.emergencyCare.CareAllPatientsResponse;
import ar.edu.itba.pod.tpe1.emergencyCare.CarePatientRequest;
import ar.edu.itba.pod.tpe1.emergencyCare.CarePatientResponse;
import ar.edu.itba.pod.tpe1.emergencyCare.DischargePatientRequest;
import ar.edu.itba.pod.tpe1.emergencyCare.DischargePatientResponse;
import ar.edu.itba.pod.tpe1.emergencyCare.EmergencyCareServiceGrpc;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmergencyCareServant extends EmergencyCareServiceGrpc.EmergencyCareServiceImplBase {
    private static final Logger logger = LoggerFactory.getLogger(EmergencyCareServant.class);

    public void carePatient(CarePatientRequest request, StreamObserver<CarePatientResponse> responseObserver) {
        logger.info("Attending patient ...");
    }

    public void careAllPatients(Empty empty, StreamObserver<CareAllPatientsResponse> responseObserver) {
        logger.info("Attending all patients ...");
    }

    public void dischargePatient(DischargePatientRequest request, StreamObserver<DischargePatientResponse> responseObserver) {
        logger.info("Discharging patient ...");
    }
}
