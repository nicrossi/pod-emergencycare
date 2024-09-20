package ar.edu.itba.pod.tpe1.client.service.strategy;

import ar.edu.itba.pod.tpe1.query.QueryServiceGrpc;
import io.grpc.ManagedChannelBuilder;
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
        // TODO: implement
        return null;
    }

}
