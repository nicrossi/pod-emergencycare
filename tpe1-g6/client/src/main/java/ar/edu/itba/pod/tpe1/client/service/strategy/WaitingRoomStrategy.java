package ar.edu.itba.pod.tpe1.client.service.strategy;

import ar.edu.itba.pod.tpe1.waitingRoom.WaitingRoomServiceGrpc;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

public class WaitingRoomStrategy extends AbstractServiceStrategy {
    private static final Logger logger = LoggerFactory.getLogger(WaitingRoomStrategy.class);
    private final WaitingRoomServiceGrpc.WaitingRoomServiceStub stub;

    public WaitingRoomStrategy(String serviceAddress) {
        super(logger, ManagedChannelBuilder.forTarget(serviceAddress).usePlaintext().build());
        this.stub = WaitingRoomServiceGrpc.newStub(channel);
    }

    @Override
    protected Runnable getActionTask(String action, CountDownLatch latch) {
        // TODO: implement
        return null;
    }
}
