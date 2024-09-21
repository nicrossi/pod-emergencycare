package ar.edu.itba.pod.tpe1.server;

import ar.edu.itba.pod.tpe1.server.repository.DoctorsRepository;
import ar.edu.itba.pod.tpe1.server.repository.HistoryRepository;
import ar.edu.itba.pod.tpe1.server.repository.PatientsRepository;
import ar.edu.itba.pod.tpe1.server.repository.RoomsRepository;
import ar.edu.itba.pod.tpe1.server.servants.AdministrationServant;
import ar.edu.itba.pod.tpe1.server.servants.DoctorPagerServant;
import ar.edu.itba.pod.tpe1.server.interceptor.GlobalExceptionHandlerInterceptor;
import ar.edu.itba.pod.tpe1.server.servants.HealthCheckServant;
import ar.edu.itba.pod.tpe1.server.servants.QueryServant;
import ar.edu.itba.pod.tpe1.server.servants.WaitingRoomServant;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    private static final DoctorsRepository docRepo = new DoctorsRepository();
    private static final PatientsRepository patRepo = new PatientsRepository();
    private static final RoomsRepository rooRepo = new RoomsRepository();
    private static final HistoryRepository hisRepo = new HistoryRepository();


    public static void main(String[] args) throws InterruptedException, IOException {
        logger.info(" Server Starting ...");

        int port = 50051;
        io.grpc.Server server = ServerBuilder.forPort(port)
                .addService(new HealthCheckServant())
                .addService(new AdministrationServant(docRepo, rooRepo))
                .addService(new WaitingRoomServant(patRepo))
                .addService(new DoctorPagerServant())
                .addService(new QueryServant(hisRepo))
                .intercept(new GlobalExceptionHandlerInterceptor())
                .build();
        server.start();
        logger.info("Server started, listening on " + port);
        server.awaitTermination();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down gRPC server since JVM is shutting down");
            server.shutdown();
            logger.info("Server shut down");
        }));
    }}
