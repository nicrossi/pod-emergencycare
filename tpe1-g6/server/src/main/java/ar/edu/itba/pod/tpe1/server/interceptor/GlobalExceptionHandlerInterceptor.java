package ar.edu.itba.pod.tpe1.server.interceptor;

import com.google.rpc.Code;
import io.grpc.*;
import io.grpc.protobuf.StatusProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


public class GlobalExceptionHandlerInterceptor implements ServerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandlerInterceptor.class);

    @Override
    public <T, R> ServerCall.Listener<T> interceptCall(
            ServerCall<T, R> serverCall, Metadata headers, ServerCallHandler<T, R> serverCallHandler) {
        ServerCall.Listener<T> delegate = serverCallHandler.startCall(serverCall, headers);
        return new ExceptionHandler<>(delegate, serverCall, headers);
    }

    private static class ExceptionHandler<T, R> extends ForwardingServerCallListener.SimpleForwardingServerCallListener<T> {

        private final ServerCall<T, R> delegate;
        private final Metadata headers;

        ExceptionHandler(ServerCall.Listener<T> listener, ServerCall<T, R> serverCall, Metadata headers) {
            super(listener);
            this.delegate = serverCall;
            this.headers = headers;
        }

        @Override
        public void onHalfClose() {
            try {
                super.onHalfClose();
            } catch (RuntimeException ex) {
                handleException(ex, delegate, headers);
            }
        }

        private final Map<Class<? extends Throwable>, Code> errorCodesByException = Map.of(
                IllegalArgumentException.class, Code.INVALID_ARGUMENT,
                IllegalStateException.class, Code.FAILED_PRECONDITION,
                RuntimeException.class, Code.INTERNAL,
                NullPointerException.class, Code.INTERNAL,
                IndexOutOfBoundsException.class, Code.OUT_OF_RANGE,
                UnsupportedOperationException.class, Code.UNIMPLEMENTED,
                Exception.class, Code.UNKNOWN,
                Throwable.class, Code.UNKNOWN
        );

        private void handleException(RuntimeException exception, ServerCall<T, R> serverCall, Metadata headers) {
            Throwable error = exception;
            logger.error("Exception occurred: {}", exception.getMessage(), exception); // Log the exception

            if (!errorCodesByException.containsKey(error.getClass())) {
                // Si la excepción vino "wrappeada" entonces necesitamos preguntar por la causa.
                error = error.getCause();
                if (error == null || !errorCodesByException.containsKey(error.getClass())) {
                    // Una excepción NO esperada.
                    serverCall.close(Status.UNKNOWN.withDescription(exception.getMessage()), headers);
                    return;
                }
            }
            // Una excepción esperada.
            Code code = errorCodesByException.getOrDefault(error.getClass(), Code.UNKNOWN);
            com.google.rpc.Status rpcStatus = com.google.rpc.Status.newBuilder()
                    .setCode(code.getNumber())
                    .setMessage(error.getMessage())
                    .build();
            StatusRuntimeException statusRuntimeException = StatusProto.toStatusRuntimeException(rpcStatus);
            Status newStatus = Status.fromThrowable(statusRuntimeException);
            serverCall.close(newStatus, headers);
        }
    }

}