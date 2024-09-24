package ar.edu.itba.pod.tpe1.server.servants;

import ar.edu.itba.pod.tpe1.query.*;
import ar.edu.itba.pod.tpe1.server.repository.HistoryRepository;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class QueryServant extends QueryServiceGrpc.QueryServiceImplBase {
    private static final Logger logger = LoggerFactory.getLogger(QueryServant.class);

    private final HistoryRepository historyRepository;

    public QueryServant(HistoryRepository hR) {
        historyRepository = hR;
    }


    @Override
    public void queryRooms(Empty request, StreamObserver<QueryRoomsResponse> responseObserver) {
        //TODO: implement
    }

    @Override
    public void queryWaitingRoom(Empty request, StreamObserver<QueryWaitingRoomResponse> responseObserver) {
        //TODO: implement
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
