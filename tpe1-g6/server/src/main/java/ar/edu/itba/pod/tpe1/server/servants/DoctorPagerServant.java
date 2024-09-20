package ar.edu.itba.pod.tpe1.server.servants;

import ar.edu.itba.pod.tpe1.doctorPager.DoctorPagerRequest;
import ar.edu.itba.pod.tpe1.doctorPager.DoctorPagerResponse;
import ar.edu.itba.pod.tpe1.doctorPager.DoctorPagerServiceGrpc;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DoctorPagerServant extends DoctorPagerServiceGrpc.DoctorPagerServiceImplBase {
    private static final Logger logger = LoggerFactory.getLogger(DoctorPagerServant.class);

    public void register(DoctorPagerRequest request, StreamObserver<DoctorPagerResponse> responseObserver) {
        logger.info("register doctor ...");
    }

    public void unregister(DoctorPagerRequest request, StreamObserver<DoctorPagerRequest> responseObserver) {
        logger.info("unregister doctor ...");
    }
}
