package ar.edu.itba.pod.tpe1.client.service.strategy;

import ar.edu.itba.pod.tpe1.healthCheck.HealthCheckResponse;
import ar.edu.itba.pod.tpe1.healthCheck.HealthCheckServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class HealthCheckServiceStrategy implements ServiceStrategy {
    private static final Logger logger = LoggerFactory.getLogger(HealthCheckServiceStrategy.class);
    private final HealthCheckServiceGrpc.HealthCheckServiceStub stub;
    private final ManagedChannel channel;

    public HealthCheckServiceStrategy(String serviceAddress) {
        channel = ManagedChannelBuilder.forTarget(serviceAddress)
                .usePlaintext()
                .build();
        stub = HealthCheckServiceGrpc.newStub(channel);
    }

    @Override
    public void execute(String action) {
        CountDownLatch latch = new CountDownLatch(1);

        stub.healthCheck(com.google.protobuf.Empty.getDefaultInstance(), new StreamObserver<>() {
            @Override
            public void onNext(HealthCheckResponse response) {
                logger.info("HealthCheck, status: {}", response.getStatus().getValue());
            }

            @Override
            public void onError(Throwable t) {
                logger.error("HealthCheck failed: {}", t.getMessage());
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        });

        try {
            latch.await(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error("HealthCheck interrupted: {}", e.getMessage());
        } finally {
            channel.shutdown();
            // TODO maybe add some additional validation to check if the channel is terminated ?
        }
    }
}
