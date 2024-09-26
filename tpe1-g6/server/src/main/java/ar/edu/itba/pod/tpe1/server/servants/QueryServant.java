package ar.edu.itba.pod.tpe1.server.servants;

import ar.edu.itba.pod.tpe1.emergencyCare.RoomStatus;
import ar.edu.itba.pod.tpe1.query.*;
import ar.edu.itba.pod.tpe1.server.repository.CareRespository;
import ar.edu.itba.pod.tpe1.server.repository.HistoryRepository;
import ar.edu.itba.pod.tpe1.server.repository.PatientsRepository;
import ar.edu.itba.pod.tpe1.server.repository.RoomsRepository;
import ar.edu.itba.pod.tpe1.waitingRoom.Patient;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EmptyStackException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;

import static ar.edu.itba.pod.tpe1.emergencyCare.EmergencyCareServiceModel.stringName;

public class QueryServant extends QueryServiceGrpc.QueryServiceImplBase {
    private static final Logger logger = LoggerFactory.getLogger(QueryServant.class);

    private final ReadWriteLock lock;

    private final HistoryRepository historyRepository;
    private final PatientsRepository patientsRepository;
    private final RoomsRepository roomsRepository;
    private final CareRespository careRespository;

    public QueryServant(HistoryRepository hR, PatientsRepository pR, RoomsRepository rR, CareRespository cR, ReadWriteLock lock) {
        patientsRepository = pR;
        historyRepository = hR;
        roomsRepository = rR;
        careRespository = cR;
        this.lock = lock;
    }

    @Override
    public void queryRooms(Empty request, StreamObserver<QueryRoomsResponse> responseObserver) {
        QueryRoomsResponse.Builder responseBuilder = QueryRoomsResponse.newBuilder();
        List<RoomStatus> rooms = roomsRepository.getRooms();

        List<QueryRoomInfo> listOfRooms = new ArrayList<>();

        for (int i = 0; i < rooms.size(); i++) {
            String status = rooms.get(i).getValueDescriptor().getOptions().getExtension(stringName);
            QueryRoomInfo.Builder roomInfoBuilder = QueryRoomInfo.newBuilder()
                    .setRoomId(i + 1)
                    .setStatus(status);

            if ("Occupied".equals(status)) {
                CaredInfo care = careRespository.getCare(i + 1);
                roomInfoBuilder.setDoctor(care.getDoctor()).setPatient(care.getPatient());
            }
            listOfRooms.add(roomInfoBuilder.build());
        }

        responseBuilder.addAllRoomsInfo(listOfRooms);

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void queryWaitingRoom(Empty request, StreamObserver<QueryWaitingRoomResponse> responseObserver) {
        List<Patient> sortedPatients;
        lock.readLock().lock();
        try {
            // Check if there are any patients waiting
            if (patientsRepository.getPatientsWaitingCount() == 0) {
                // No patients, so no file should be created, just return an empty response
                responseObserver.onCompleted();
                return;
            }

            // Get sorted patients
            sortedPatients = patientsRepository.getSortedPatients();
        } finally {
            lock.readLock().unlock();
        }

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
        List<CaredInfo> history;

        // Acquire a read lock before accessing the shared history repository
        lock.readLock().lock();
        try {
            // Check if we should filter by a specific room
            if (request.hasRoom()) {
                int roomNumber = request.getRoom();
                history = historyRepository.getHistory(roomNumber);
            } else {
                // No filter, return the entire care history
                history = historyRepository.getHistory();
            }
        } finally {
            lock.readLock().unlock();
        }

        // If the history is empty, no cares have been resolved
        if (history.isEmpty()) {
            // Log the issue and do not create a CSV file
            logger.warn("No emergency care has been solved yet. No file will be created.");
            responseObserver.onError(new EmptyStackException());  // Or StatusRuntimeException if preferred
            return;
        }

        // Build the response using the resolved care information
        QueryCaresResponse.Builder responseBuilder = QueryCaresResponse.newBuilder();
        for (CaredInfo care : history) {
            responseBuilder.addHistory(care);  // Add each care entry to the response
        }

        // Send the response back to the client
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

}
