package ar.edu.itba.pod.tpe1.server.servants;

import ar.edu.itba.pod.tpe1.query.*;
import ar.edu.itba.pod.tpe1.server.repository.HistoryRepository;
import ar.edu.itba.pod.tpe1.server.repository.PatientsRepository;
import ar.edu.itba.pod.tpe1.waitingRoom.Patient;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class QueryServant extends QueryServiceGrpc.QueryServiceImplBase {
    private static final Logger logger = LoggerFactory.getLogger(QueryServant.class);

    private final HistoryRepository historyRepository;
    private final PatientsRepository patientsRepository;

    public QueryServant(HistoryRepository hR, PatientsRepository pR) {
        patientsRepository = pR;
        historyRepository = hR;
    }

    @Override
    public void queryRooms(Empty request, StreamObserver<QueryRoomsResponse> responseObserver) {
        //TODO: implement
        //Room,Status,Patient,Doctor
    }

    @Override
    public void queryWaitingRoom(Empty request, StreamObserver<QueryWaitingRoomResponse> responseObserver) {
        // Check if there are any patients waiting
        if (patientsRepository.getPatientsWaitingCount() == 0) {
            // No patients, so no file should be created, just return an empty response
            responseObserver.onCompleted();
            return;
        }

        // Get sorted patients
        List<Patient> sortedPatients = patientsRepository.getSortedPatients();

        // Build the response
        QueryWaitingRoomResponse.Builder responseBuilder = QueryWaitingRoomResponse.newBuilder();
        for (Patient patient : sortedPatients) {
            responseBuilder.addPatientsInfo(patient);
        }

        // Send the response back to the client
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void queryCares(QueryRequest request, StreamObserver<QueryCaresResponse> responseObserver) {
        List<CaredInfo> history = request.hasRoom() ?
                historyRepository.getHistory(request.getRoom())
                : historyRepository.getHistory();

        QueryCaresResponse response = QueryCaresResponse.newBuilder()
                .addAllHistory(history)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
