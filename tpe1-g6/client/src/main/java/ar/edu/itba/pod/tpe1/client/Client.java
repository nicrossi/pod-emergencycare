package ar.edu.itba.pod.tpe1.client;

import ar.edu.itba.pod.tpe1.client.service.strategy.ServiceStrategyProvider;
import ar.edu.itba.pod.tpe1.client.service.strategy.ServiceStrategy;
import ar.edu.itba.pod.tpe1.client.service.strategy.ServiceType;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Client {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    public static void main(String[] args) throws InterruptedException {
        logger.info("tpe1-g6 Client Starting ...");
        final String serverAddress = Validate.notBlank(System.getProperty("serverAddress"), "'serverAddress' can't be empty");
        final String action = Validate.notBlank(System.getProperty("action"), "'action' can't be empty");
        final String service = Validate.notBlank(System.getProperty("service"), "'service' can't be empty");

        ServiceStrategyProvider serviceStrategyProvider = new ServiceStrategyProvider();

        try {
            ServiceStrategy serviceStrategy = serviceStrategyProvider.getServiceStrategy(ServiceType.selectService(service), serverAddress);
            serviceStrategy.execute(action);
        } catch (Exception e){
            logger.error("Error executing service: {}", e.getMessage(), e);
        }
    }
}
