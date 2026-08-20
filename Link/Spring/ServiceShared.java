package Link.Spring;

import Link.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class ServiceShared {
    public List<List<Double>> computeMulti(
            List<String> distNames,
            String type,
            Object xVal,
            List<Map<String, Double>> params
    ) throws Exception {
        List<Distribution> distributionList = new ArrayList<>();
        for (String distName : distNames) {
            if (distName == null) throw new NullPointerException("Distribution can't be null");
            distributionList.add(DistributionRegistry.get(distName));
        }
        return ComparatorEngine.compare(distributionList, type, xVal, params);
    }

    public List<List<Double>> computeTwo(String dist1, String dist2, String type,
                                         Object xVal, List<Map<String, Double>> params) throws Exception {
        Distribution distribution1 = DistributionRegistry.get(dist1);
        Distribution distribution2 = DistributionRegistry.get(dist2);
        List<Distribution> distributionList = List.of(distribution1, distribution2);
        return ComparatorEngine.compare(distributionList, type, xVal, params);
    }

    public Object computeSingle(String distName, String type, Object xVal, Map<String, Double> params) throws Exception {
        Distribution dist = DistributionRegistry.get(distName);
        if (dist == null) throw new IllegalArgumentException("Unknown: " + distName);

        // If xVal is a single Double
        if (xVal instanceof Double) {
            double x = (Double) xVal;
            return type.equalsIgnoreCase("PDF") ? dist.pdf(x, params) : dist.cdf(x, params);
        }

        // If xVal is a double[] (primitive array)
        if (xVal instanceof double[] xs) {
            List<Double> results = new ArrayList<>(xs.length);
            for (double x : xs) {
                results.add(type.equalsIgnoreCase("PDF") ? dist.pdf(x, params) : dist.cdf(x, params));
            }
            return results;
        }

        // If xVal is a List<Double> (from JSON)
        if (xVal instanceof List<?> list) {
            List<Double> results = new ArrayList<>(list.size());
            for (Object o : list) {
                if (!(o instanceof Number)) {
                    throw new IllegalArgumentException("List must contain numbers");
                }
                double x = ((Number) o).doubleValue();
                results.add(type.equalsIgnoreCase("PDF") ? dist.pdf(x, params) : dist.cdf(x, params));
            }
            return results;
        }

        throw new IllegalArgumentException("Unsupported x value type: " + xVal.getClass());
    }

    public ComparatorOutput compareWithECDF(String distName, Object dataObj, Object xValObj, Map<String, Double> params) throws Exception {
        double[] data = (double[]) dataObj;
        double[] xVal = (double[]) xValObj;
        Distribution dist = DistributionRegistry.get(distName);
        List<Double> ECDFPlots = new ArrayList<>(data.length);
        int count = 0;
        for (double x : data) {
            while (count < data.length && data[count] <= x) count++;
            ECDFPlots.add((double) count / data.length);
        }

        List<Double> theoreticalCDF = Arrays.stream(xVal)
                .map(val -> dist.cdf(val, params))
                .boxed()
                .toList();

        return new CompOut(theoreticalCDF, ECDFPlots);
    }
}
