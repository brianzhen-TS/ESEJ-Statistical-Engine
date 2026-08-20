package FXSystem.ControllersAndStructures.AbstractClassesAndInterfaces.Properties;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface Computable {
    default void compute() {}
    default Map<String, Double> ConfigParams() {
        return new ConcurrentHashMap<>();
    }
    default void getInputs() {}
}
