// EtaCore/MultivariateDistributionRegistry.java
package Link;

import java.util.*;

public class MultivariateDistributionRegistry {
    private static final Map<String, MultivariateDistribution> registry = new LinkedHashMap<>();

    public static void register(MultivariateDistribution dist) {
        registry.put(dist.getName().toLowerCase(), dist);
    }

    public static MultivariateDistribution get(String name) {
        return registry.get(name.toLowerCase());
    }

    public static Collection<MultivariateDistribution> getAll() {
        return registry.values();
    }

    static {
        register(new BivariateNormalDistribution());
    }

}