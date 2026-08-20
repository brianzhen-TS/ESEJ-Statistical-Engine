package eta.util;

import Link.Distribution;
import Link.DistributionRegistry;
import Link.Parameter;

import java.util.*;

/**
 * A utility map for distribution parameters with additional metadata methods.
 * This class is thread-safe for read-only operations; writes are not synchronized.
 */
public class DistMap implements Map<String, Double> {

    private final Map<String, Double> delegate = new HashMap<>();

    // ----- Constructors -----
    public DistMap() {}

    public DistMap(Map<String, Double> initial) {
        delegate.putAll(initial);
    }

    // ----- Map interface delegation -----
    @Override
    public int size() { return delegate.size(); }
    @Override
    public boolean isEmpty() { return delegate.isEmpty(); }
    @Override
    public boolean containsKey(Object key) { return delegate.containsKey(key); }
    @Override
    public boolean containsValue(Object value) { return delegate.containsValue(value); }
    @Override
    public Double get(Object key) { return delegate.get(key); }
    @Override
    public Double put(String key, Double value) { return delegate.put(key, value); }
    @Override
    public Double remove(Object key) { return delegate.remove(key); }
    @Override
    public void putAll(Map<? extends String, ? extends Double> m) { delegate.putAll(m); }
    @Override
    public void clear() { delegate.clear(); }
    @Override
    public Set<String> keySet() { return delegate.keySet(); }
    @Override
    public Collection<Double> values() { return delegate.values(); }
    @Override
    public Set<Entry<String, Double>> entrySet() { return delegate.entrySet(); }

    // ----- Convenience methods -----
    public boolean containsDistribution(String name) {
        return DistributionRegistry.get(name) != null;
    }

    public List<String> getAllDistributions() {
        return DistributionRegistry.getAll().stream().map(Distribution::getName).toList();
    }

    public List<String> getParameterDescriptions(String distName) {
        Distribution dist = DistributionRegistry.get(distName);
        if (dist == null) {
            throw new IllegalArgumentException("Unknown distribution: " + distName);
        }
        List<String> descs = new ArrayList<>();
        for (Parameter p : dist.getParameters()) {
            descs.add(p.description());
        }
        return descs;
    }

    public List<List<String>> getParameterDescriptions(List<String> distNames) {
        List<List<String>> result = new ArrayList<>();
        for (String name : distNames) {
            result.add(getParameterDescriptions(name));
        }
        return result;
    }

    public boolean containsParameter(String distName, String expectedDesc) {
        return getParameterDescriptions(distName).contains(expectedDesc);
    }

    public boolean containsParameterAny(List<String> distNames, String expectedDesc) {
        for (String name : distNames) {
            if (containsParameter(name, expectedDesc)) return true;
        }
        return false;
    }

    public String[] getParameterDescriptions(String distName, int count) {
        List<String> all = getParameterDescriptions(distName);
        if (count > all.size()) {
            throw new ArrayIndexOutOfBoundsException(
                    "Requested " + count + " but only " + all.size() + " available"
            );
        }
        return all.subList(0, count).toArray(String[]::new);
    }

    public List<String[]> getParameterDescriptions(List<String> distNames, int count) {
        List<String[]> result = new ArrayList<>();
        for (String name : distNames) {
            result.add(getParameterDescriptions(name, count));
        }
        return result;
    }

    // ----- Factory methods (like Map.of) -----
    public static DistMap of() {
        return new DistMap();
    }

    public static DistMap of(String k1, Double v1) {
        DistMap map = new DistMap();
        map.put(k1, v1);
        return map;
    }

    public static DistMap of(String k1, Double v1, String k2, Double v2) {
        DistMap map = new DistMap();
        map.put(k1, v1);
        map.put(k2, v2);
        return map;
    }

    // ----- Builder -----
    public static class Builder {
        private final DistMap map = new DistMap();
        public Builder put(String key, Double value) {
            map.put(key, value);
            return this;
        }
        public DistMap build() {
            return map;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}