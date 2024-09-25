package ar.edu.itba.pod.tpe1.client.service.strategy;

import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public abstract class AbstractServiceStrategy implements ServiceStrategy {
    protected final Logger logger;
    protected final ManagedChannel channel;

    protected AbstractServiceStrategy(Logger logger, ManagedChannel channel) {
        this.logger = logger;
        this.channel = channel;
    }

    @Override
    public void execute(String action) {
        CountDownLatch latch = new CountDownLatch(1);

        Runnable actionTask = getActionTask(action, latch);
        if (Objects.isNull(actionTask)) {
            logger.error("Unknown action: {}", action);
            latch.countDown();
            return;
        }

        try {
            actionTask.run();
            latch.await(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error("Execution interrupted: {}", e.getMessage());
        } finally {
            channel.shutdown();
            // TODO maybe add some additional validation to check if the channel is terminated ?
        }
    }

    protected abstract Runnable getActionTask(String action, CountDownLatch latch);

    protected void handleError(Throwable t) {
        if (t instanceof StatusRuntimeException statusEx) {
            logger.error(statusEx.getStatus().getDescription());
        } else {
            logger.error("An unexpected error occurred: {}", t.getMessage(), t);
        }
    }
}