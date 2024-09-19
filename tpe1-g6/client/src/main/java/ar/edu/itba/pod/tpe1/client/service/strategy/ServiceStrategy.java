package ar.edu.itba.pod.tpe1.client.service.strategy;

@FunctionalInterface
public interface ServiceStrategy {
    void execute(String action);
}
