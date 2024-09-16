package ar.edu.itba.pod.tpe1.server.servants;

import ar.edu.itba.pod.tpe1.healthCheck.HealthCheckServiceGrpc;
import ar.edu.itba.pod.tpe1.healthCheck.HealthCheckResponse;
import com.google.protobuf.StringValue;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;

public class HealthCheckServant extends HealthCheckServiceGrpc.HealthCheckServiceImplBase {
    public void healthCheck(Empty request, StreamObserver<HealthCheckResponse> responseObserver) {
        HealthCheckResponse response = HealthCheckResponse.newBuilder().setStatus(StringValue.of("OK")).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
