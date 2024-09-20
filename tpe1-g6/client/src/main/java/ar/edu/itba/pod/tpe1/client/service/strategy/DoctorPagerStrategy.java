package ar.edu.itba.pod.tpe1.client.service.strategy;

import ar.edu.itba.pod.tpe1.doctorPager.DoctorPagerServiceGrpc;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

public class DoctorPagerStrategy extends AbstractServiceStrategy {
    private static final Logger logger = LoggerFactory.getLogger(DoctorPagerStrategy.class);
    private final DoctorPagerServiceGrpc.DoctorPagerServiceStub stub;

    public DoctorPagerStrategy(String serviceAddress) {
        super(logger, ManagedChannelBuilder.forTarget(serviceAddress).usePlaintext().build());
        this.stub = DoctorPagerServiceGrpc.newStub(channel);
    }

    @Override
    protected Runnable getActionTask(String action, CountDownLatch latch) {
        // TODO: implement
        return null;
    }
}
