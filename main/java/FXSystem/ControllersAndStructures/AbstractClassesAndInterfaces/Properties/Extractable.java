package FXSystem.ControllersAndStructures.AbstractClassesAndInterfaces.Properties;

import eta.util.DistMap;

import java.util.HashMap;
import java.util.Map;

public interface Extractable {
    DistMap InsertMap(Map<String, Double> map);
    void MapIO();
    Map<String, Double> ConvertMap(DistMap map);
}
