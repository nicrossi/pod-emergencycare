package ar.edu.itba.pod.tpe1.server;

import ar.edu.itba.pod.tpe1.server.repository.*;
import ar.edu.itba.pod.tpe1.server.servants.*;
import ar.edu.itba.pod.tpe1.server.interceptor.GlobalExceptionHandlerInterceptor;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.commons.lang3.Validate;

public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    private static final DoctorsRepository docRepo = new DoctorsRepository();
    private static final PatientsRepository patRepo = new PatientsRepository();
    private static final RoomsRepository rooRepo = new RoomsRepository();
    private static final HistoryRepository hisRepo = new HistoryRepository();
    private static final CareRepository carRepo = new CareRepository();

    private static final ReadWriteLock lock = new ReentrantReadWriteLock();

    private static final DoctorPagerServant doctorPagerServant = new DoctorPagerServant(docRepo);

    public static void main(String[] args) throws InterruptedException, IOException {
        final String portStr = Validate.notBlank(System.getProperty("port"), "server port is required");
        final int port = Integer.parseInt(portStr);

        io.grpc.Server server = ServerBuilder.forPort(port)
                .addService(new HealthCheckServant())
                .addService(new AdministrationServant(docRepo, rooRepo, doctorPagerServant, lock))
                .addService(new WaitingRoomServant(patRepo, lock))
                .addService(new EmergencyCareServant(patRepo, docRepo, rooRepo, hisRepo, carRepo, doctorPagerServant, lock))
                .addService(doctorPagerServant)
                .addService(new QueryServant(hisRepo, patRepo, rooRepo, carRepo, lock))
                .intercept(new GlobalExceptionHandlerInterceptor())
                .build();
        server.start();
        logger.info("Server started, listening on {}", port);
        server.awaitTermination();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down gRPC server since JVM is shutting down");
            server.shutdown();
            logger.info("Server shut down");
        }));
    }
}
