package ar.edu.itba.pod.tpe1.client.service.strategy;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum ServiceType {
    HEALTH_CHECK("HealthCheck", "localhost:50051"),
    ADMINISTRATION("AdministrationClient", "localhost:50051"),
    WAITING_ROOM("WaitingRoomClient", "localhost:50051"),
    EMERGENCY_CARE("EmergencyCareClient", "localhost:50051"),
    DOCTOR_PAGER("DoctorPagerClient", "localhost:50051"),
    QUERY("QueryClient", "localhost:50051");

    private final String name;
    private final String target;

    public static ServiceType selectService(String serviceType) throws IllegalArgumentException {
        Preconditions.checkArgument(StringUtils.isNotBlank(serviceType), "serviceType can't be blank");
        return Arrays.stream(values())
                .filter(value -> value.name.equalsIgnoreCase(serviceType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Service '" + serviceType + "' not supported."));
    }

    @Override
    public String toString() {
        return name;
    }
}
