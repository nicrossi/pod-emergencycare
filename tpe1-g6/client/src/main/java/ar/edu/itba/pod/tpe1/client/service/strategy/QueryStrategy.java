package ar.edu.itba.pod.tpe1.client.service.strategy;

import ar.edu.itba.pod.tpe1.client.service.util.QueryClientUtil;
import ar.edu.itba.pod.tpe1.query.*;
import com.google.protobuf.Empty;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.CountDownLatch;

public class QueryStrategy extends AbstractServiceStrategy {
    private static final Logger logger = LoggerFactory.getLogger(QueryStrategy.class);
    private final QueryServiceGrpc.QueryServiceStub stub;

    public QueryStrategy(String serviceAddress) {
        super(logger, ManagedChannelBuilder.forTarget(serviceAddress).usePlaintext().build());
        this.stub = QueryServiceGrpc.newStub(channel);
    }

    @Override
    protected Runnable getActionTask(String action, CountDownLatch latch) {
        String outPath = Validate.notBlank(System.getProperty("outPath"));
        //TODO: write to a file (do not append, overwrite if it exists)

        StreamObserver<QueryRoomsResponse> queryRoomsObserver = new StreamObserver<>() {
            @Override
            public void onNext(QueryRoomsResponse value) {
                logger.info("Received query rooms response: {}", value);
                String buffer = QueryClientUtil.convertToCSV(
                        QueryClientUtil.QueryRoomsHeaders,
                        value.getRoomsInfoList(),
                        QueryClientUtil::mapQueryRoomInfo
                );
                //TODO: actually make a file...
                logger.info(buffer);
            }
            @Override
            public void onError(Throwable t) {
                logger.error("No rooms have been created yet: {}", t.getMessage(), t);
                latch.countDown();
            }
            @Override
            public void onCompleted() {
                latch.countDown();
            }
        };

        StreamObserver<QueryWaitingRoomResponse> queryWaitingRoomObserver = new StreamObserver<>() {

            @Override
            public void onNext(QueryWaitingRoomResponse value) {
                logger.info("Received query waiting room response: {}", value);
                String buffer = QueryClientUtil.convertToCSV(
                        QueryClientUtil.QueryWaitingRoomHeaders,
                        value.getPatientsInfoList(),
                        QueryClientUtil::mapQueryWaitingRoomInfo
                );
                //TODO: actually make a file...
                logger.info(buffer);
            }

            @Override
            public void onError(Throwable t) {
                logger.error("No patients in the waiting room: {}", t.getMessage(), t);
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        };

        StreamObserver<QueryCaresResponse> queryCaresObserver = new StreamObserver<>() {
            @Override
            public void onNext(QueryCaresResponse value) {
                logger.info("Received query resolved cares response: {}", value);
                String buffer = QueryClientUtil.convertToCSV(
                        QueryClientUtil.QueryCaresHeaders,
                        value.getHistoryList(),
                        QueryClientUtil::mapCaredInfo
                );
                //TODO: actually make a file...
                logger.info(buffer);
            }

            @Override
            public void onError(Throwable t) {
                logger.error("No cares have been resolved yet: {}", t.getMessage(), t);
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        };

        return switch(action) {
            case "queryRooms" -> () -> stub.queryRooms(Empty.getDefaultInstance(), queryRoomsObserver);
            case "queryWaitingRoom" -> () -> stub.queryWaitingRoom(Empty.getDefaultInstance(), queryWaitingRoomObserver);
            case "queryCares" -> () -> stub.queryCares(QueryClientUtil.getQueryRequest(), queryCaresObserver);
            default -> null;
        };
    }

}
