package ar.edu.itba.pod.tpe1.client.service.strategy;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ServiceStrategyProvider {
    private final Map<ServiceType, ServiceStrategy> serviceStrategyMap;
    private final HealthCheckServiceStrategy healthCheckServiceStrategy;

    public ServiceStrategyProvider() {
        healthCheckServiceStrategy = new HealthCheckServiceStrategy(ServiceType.HEALTH_CHECK.getTarget());
        serviceStrategyMap = initServiceStrategyMap();
    }

    private Map<ServiceType, ServiceStrategy> initServiceStrategyMap() {
        Map<ServiceType, ServiceStrategy> serviceStrategyMap = new HashMap<>();
        serviceStrategyMap.put(ServiceType.HEALTH_CHECK, healthCheckServiceStrategy);
        return serviceStrategyMap;
    }

    public ServiceStrategy getServiceStrategy(ServiceType serviceType, String target) {
        ServiceStrategy st = Optional.ofNullable(serviceStrategyMap.get(serviceType))
                .orElseThrow(() -> new IllegalArgumentException("Service '" + serviceType + "' could not be loaded."));
        // if target is not default, we return a new instance of the service strategy
        return ServiceType.HEALTH_CHECK.getTarget().equals(target) ? st : new HealthCheckServiceStrategy(target);
    }
}
