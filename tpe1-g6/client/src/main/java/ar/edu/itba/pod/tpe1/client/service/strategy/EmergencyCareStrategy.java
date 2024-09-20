package ar.edu.itba.pod.tpe1.client.service.strategy;

import ar.edu.itba.pod.tpe1.emergencyCare.EmergencyCareServiceGrpc;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

public class EmergencyCareStrategy extends AbstractServiceStrategy {
    private static final Logger logger = LoggerFactory.getLogger(EmergencyCareStrategy.class);
    private final EmergencyCareServiceGrpc.EmergencyCareServiceStub stub;

    public EmergencyCareStrategy(String serviceAddress) {
        super(logger, ManagedChannelBuilder.forTarget(serviceAddress).usePlaintext().build());
        this.stub = EmergencyCareServiceGrpc.newStub(channel);
    }

    @Override
    protected Runnable getActionTask(String action, CountDownLatch latch) {
        // TODO: implement
        return null;
    }
}
