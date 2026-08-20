package EtaCore.Interfaces;

import Link.ComparatorOutput;

import java.util.List;
import java.util.Map;

public interface WebServerServiceCompute {
    List<List<Double>> computeMulti(List<String> distNames,
                                    String type,
                                    Object xVal,
                                    List<Map<String, Double>> params) throws Exception;
    List<List<Double>> computeTwo(String dist1, String dist2, String type,
                                  Object xVal, List<Map<String, Double>> params) throws Exception;
    Object computeSingle(String distName, String type, Object xVal, Map<String, Double> params) throws Exception ;
    ComparatorOutput compareWithECDF(String distName, double[] data, double[] xVal, Map<String, Double> params) throws Exception;
}
