package FXSystem.ControllersAndStructures.AbstractClassesAndInterfaces.Properties;

import java.util.Map;

public interface Insertable {
    void rebuildFields();
    String defaultValue(String name);
    Map<String, Double> getParameterMap();
}
