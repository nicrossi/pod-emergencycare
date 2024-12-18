package ar.edu.itba.pod.tpe1.client.service.strategy;

import org.apache.commons.lang3.Validate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class ServiceStrategyProvider {
    private final Map<ServiceType, ServiceStrategy> serviceStrategyMap;
    private final ServiceStrategyFactory serviceStrategyFactory;

    public ServiceStrategyProvider() {
        serviceStrategyFactory = new ServiceStrategyFactory();
        serviceStrategyMap = initServiceStrategyMap();
    }

    private Map<ServiceType, ServiceStrategy> initServiceStrategyMap() {
        Map<ServiceType, ServiceStrategy> serviceStrategyMap = new HashMap<>();
        serviceStrategyMap.put(ServiceType.HEALTH_CHECK, serviceStrategyFactory.create(ServiceType.HEALTH_CHECK, ServiceType.HEALTH_CHECK.getTarget()));
        serviceStrategyMap.put(ServiceType.ADMINISTRATION, serviceStrategyFactory.create(ServiceType.ADMINISTRATION, ServiceType.ADMINISTRATION.getTarget()));
        serviceStrategyMap.put(ServiceType.WAITING_ROOM, serviceStrategyFactory.create(ServiceType.WAITING_ROOM, ServiceType.WAITING_ROOM.getTarget()));
        serviceStrategyMap.put(ServiceType.EMERGENCY_CARE, serviceStrategyFactory.create(ServiceType.EMERGENCY_CARE, ServiceType.EMERGENCY_CARE.getTarget()));
        serviceStrategyMap.put(ServiceType.DOCTOR_PAGER, serviceStrategyFactory.create(ServiceType.DOCTOR_PAGER, ServiceType.DOCTOR_PAGER.getTarget()));
        serviceStrategyMap.put(ServiceType.QUERY, serviceStrategyFactory.create(ServiceType.QUERY, ServiceType.QUERY.getTarget()));
        return serviceStrategyMap;
    }

    public ServiceStrategy getServiceStrategy(ServiceType serviceType, String target) {
        ServiceStrategy st = Optional.ofNullable(serviceStrategyMap.get(serviceType))
                .orElseThrow(() -> new IllegalArgumentException("Service '" + serviceType + "' could not be loaded."));
        // if target is not default, we return a new instance of the service strategy
        return serviceType.getTarget().equals(target) ? st : serviceStrategyFactory.create(serviceType, target);
    }

    private static class ServiceStrategyFactory {
        private final Map<ServiceType, Function<String, ServiceStrategy>> strategyMap = new HashMap<>();

        public ServiceStrategyFactory() {
            register(ServiceType.HEALTH_CHECK, HealthCheckServiceStrategy::new);
            register(ServiceType.ADMINISTRATION, AdministrationStrategy::new);
            register(ServiceType.WAITING_ROOM, WaitingRoomStrategy::new);
            register(ServiceType.EMERGENCY_CARE, EmergencyCareStrategy::new);
            register(ServiceType.DOCTOR_PAGER, DoctorPagerStrategy::new);
            register(ServiceType.QUERY, QueryStrategy::new);
        }

        public void register(ServiceType serviceType, Function<String, ServiceStrategy> constructor) {
            strategyMap.put(serviceType, constructor);
        }

        public ServiceStrategy create(ServiceType serviceType, String target) {
            Function<String, ServiceStrategy> constructor = strategyMap.get(serviceType);
            Validate.notNull(constructor, "Service '" + serviceType + "' not supported.");

            return constructor.apply(target);
        }
    }
}
