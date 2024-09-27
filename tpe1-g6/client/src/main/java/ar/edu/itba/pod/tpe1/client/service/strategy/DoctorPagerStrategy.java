package ar.edu.itba.pod.tpe1.client.service.strategy;

import ar.edu.itba.pod.tpe1.client.service.util.DoctorPagerClientUtil;
import ar.edu.itba.pod.tpe1.doctorPager.DoctorPagerResponse;
import ar.edu.itba.pod.tpe1.doctorPager.DoctorPagerServiceGrpc;
import ar.edu.itba.pod.tpe1.doctorPager.DoctorPagerUnregisterResponse;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.concurrent.CountDownLatch;

public class DoctorPagerStrategy extends AbstractServiceStrategy {
    private static final Logger logger = LoggerFactory.getLogger(DoctorPagerStrategy.class);
    private final DoctorPagerServiceGrpc.DoctorPagerServiceBlockingStub stub;

    public DoctorPagerStrategy(String serviceAddress) {
        super(logger, ManagedChannelBuilder.forTarget(serviceAddress).usePlaintext().build());
        this.stub = DoctorPagerServiceGrpc.newBlockingStub(channel);
    }

    @Override
    protected Runnable getActionTask(String action, CountDownLatch latch) {
        return switch (action) {
            case "register" -> () -> {
                try {
                    final Iterator<DoctorPagerResponse> responseIterator = stub.register(DoctorPagerClientUtil.getDoctorPagerRequest());
                    while (responseIterator.hasNext()) {
                        DoctorPagerResponse response = responseIterator.next();
                        logger.info(response.getMessage());
                    }
                } catch (Exception se) {
                    handleError(se);
                }
            };
            case "unregister" -> () -> {
                try {
                    final DoctorPagerUnregisterResponse response = stub.unregister(DoctorPagerClientUtil.getDoctorPagerRequest());
                    logger.info("Doctor {} ({}) unregistered successfully for pager", response.getDoctorName(), response.getDoctorLevel());
                } catch (StatusRuntimeException se) {
                    handleError(se);
                }
            };
            default -> null;
        };
    }
}
