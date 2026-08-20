package Link;

import java.util.Map;

public interface MultivariateDistribution extends Distribution {
    /**
     * Computes the density at a point x (an array of coordinates).
     * @param x the coordinate vector
     * @param params parameter map
     * @return the density value
     */
    double density(double[] x, Map<String, Double> params);
}
