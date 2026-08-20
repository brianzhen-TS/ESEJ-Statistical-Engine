package Link;

import Link.ExceptionStat.SystemException.*;

import java.util.*;

/**
 * Unified engine for comparing multiple distributions.
 */
public final class ComparatorEngine {

    private ComparatorEngine() {}

    /**
     * Computes PDF/CDF for a list of distributions over the same x‑values.
     * @param dist  list of distribution names (must not be empty)
     * @param distType   "PDF" or "CDF"
     * @param xValue     single Double or double[] (auto‑detected)
     * @param paramList  list of parameter maps, one per distribution
     * @return list of result lists (each inner list corresponds to one distribution)
     * @throws Exception if any error occurs
     */
    public static List<List<Double>> compare(
            List<Distribution> dist,
            String distType,
            Object xValue,
            List<Map<String, Double>> paramList
    ) throws Exception {
        // --- Validation ---
        if (dist == null || paramList == null) {
            throw new NullPointerException("Inputs must not be null.");
        }
        if (dist.isEmpty() || paramList.isEmpty()) {
            throw new ArrayIsEmptyException("Link.Distribution names and parameter lists must not be empty.");
        }
        if (dist.size() != paramList.size()) {
            throw new InvalidInputException("Number of distribution names and parameter maps must match.");
        }
        if (dist.size() > 10) {
            throw new InputOutOfRangeException("Maximum 10 distributions allowed.");
        }

        // Convert x‑value to double[]
        double[] xValues = ArrayUtils.toDoubleArray(xValue);

        // Prepare results
        List<List<Double>> results = new ArrayList<>(dist.size());

        // Compute for each distribution
        for (int i = 0; i < dist.size(); i++) {
            Map<String, Double> params = paramList.get(i);

            // Compute values for all x
            List<Double> series = new ArrayList<>(xValues.length);
            for (double x : xValues) {
                double value = distType.equalsIgnoreCase("PDF")
                        ? dist.get(i).pdf(x, params)
                        : dist.get(i).cdf(x, params);
                series.add(value);
            }
            results.add(series);
        }

        return results;
    }
}
