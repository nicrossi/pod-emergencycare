package ar.edu.itba.pod.tpe1.client.service.strategy;

import ar.edu.itba.pod.tpe1.client.service.util.QueryClientUtil;
import ar.edu.itba.pod.tpe1.query.*;
import com.google.protobuf.Empty;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
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

                // Check if there are rooms in the response
                if (value.getRoomsInfoList().isEmpty()) {
                    logger.warn("No rooms have been created yet. No file will be created.");
                    return;
                }

                // Convert the response to CSV format
                String buffer = QueryClientUtil.convertToCSV(
                        QueryClientUtil.QueryRoomsHeaders,    // CSV headers for rooms
                        value.getRoomsInfoList(),             // List of room information
                        QueryClientUtil::mapQueryRoomInfo     // Mapping function to format room info for CSV
                );

                // Write the CSV data to the file
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(outPath))) {
                    writer.write(buffer);  // Write CSV content to the specified file
                    logger.info("CSV file successfully written to {}", outPath);
                } catch (IOException e) {
                    logger.error("Error writing CSV file: {}", e.getMessage(), e);
                }
            }

            @Override
            public void onError(Throwable t) {
                logger.error("Failed to query rooms: {}", t.getMessage(), t);
                latch.countDown();  // Countdown the latch to indicate the task is complete, even in error
            }

            @Override
            public void onCompleted() {
                latch.countDown();  // Countdown the latch when the operation is completed
            }
        };


        StreamObserver<QueryWaitingRoomResponse> queryWaitingRoomObserver = new StreamObserver<>() {

            @Override
            public void onNext(QueryWaitingRoomResponse value) {
                logger.info("Received query waiting room response: {}", value);

                // Check if there are patients
                if (value.getPatientsInfoList().isEmpty()) {
                    logger.warn("No patients in the waiting room. No file will be created.");
                    return;
                }

                // Convert the response to CSV format
                String buffer = QueryClientUtil.convertToCSV(
                        QueryClientUtil.QueryWaitingRoomHeaders,
                        value.getPatientsInfoList(),
                        QueryClientUtil::mapQueryWaitingRoomInfo
                );

                // Write the CSV data to the file
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(outPath))) {
                    writer.write(buffer);
                    logger.info("CSV file successfully written to {}", outPath);
                } catch (IOException e) {
                    logger.error("Error writing CSV file: {}", e.getMessage(), e);
                }
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
