package Link;

import java.util.List;
import java.util.Map;

public interface Distribution {
    String getName();
    List<Parameter> getParameters();
    double pdf(double x, Map<String, Double> params);
    double cdf(double x, Map<String, Double> params);
}
