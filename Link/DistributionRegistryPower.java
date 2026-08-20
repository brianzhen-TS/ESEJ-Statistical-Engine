package Link;

import java.util.*;

@SuppressWarnings("ALL")
public class DistributionRegistryPower {
    private static final Map<String, Distribution> dists = new LinkedHashMap<>();

    public static void register(Distribution d) {
        dists.put(d.getName().toLowerCase(), d);
    }

    public static Distribution get(String name) {
        return dists.get(name.toLowerCase());
    }

    public static Collection<Distribution> getAll() { return dists.values(); }

    static {
        register(new NonCentralT());
        register(new NonCentralF());
        register(new NonCentralChiSquared());
    }

    static class NonCentralT implements Distribution {
        public String getName() { return "NonCentral T"; }
        public List<Parameter> getParameters() {
            return Arrays.asList(
                    new Parameter("df", Double.class, "Degrees of freedom"),
                    new Parameter("lambda", Double.class, "Noncentrality parameter")
            );
        }
        public double pdf(double x, Map<String, Double> p) {
            return NonCentralHead.NonCentT(x, p.get("df"), p.get("lambda"));
        }
        public double cdf(double x, Map<String, Double> p) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    static class NonCentralChiSquared implements Distribution {
        public String getName() { return "NonCentral Chi-squared"; }
        public List<Parameter> getParameters() {
            return Arrays.asList(
                    new Parameter("df", Double.class, "Degrees of freedom"),
                    new Parameter("lambda", Double.class, "Noncentrality parameter")
            );
        }
        public double pdf(double x, Map<String, Double> p) {
            return NonCentralHead.NonCentChiSquared(x, p.get("df"), p.get("lambda"));
        }
        public double cdf(double x, Map<String, Double> p) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    static class NonCentralF implements Distribution {
        public String getName() { return "NonCentral F"; }
        public List<Parameter> getParameters() {
            return Arrays.asList(
                    new Parameter("df1", Double.class, "Degrees of freedom 1"),
                    new Parameter("df2", Double.class, "Degrees of freedom 2"),
                    new Parameter("lambda", Double.class, "Noncentrality parameter")
            );
        }
        public double pdf(double x, Map<String, Double> p) {
            if (x == 0.0) x = 1e-20;
            return NonCentralHead.NonCentF(x, p.get("df1"), p.get("df2"), p.get("lambda"));
        }
        public double cdf(double x, Map<String, Double> p) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
