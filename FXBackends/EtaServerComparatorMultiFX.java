package FXBackends;

import Link.*;
import Link.ExceptionStat.SystemException.*;
import Link.ExceptionStat.SystemError.RuntimeAnomalyError;
import Link.CompOut;
import Link.ComparatorMultiOutput;
import Link.Distribution;
import eta.util.StreamX;

import java.util.*;
import java.util.function.IntFunction;
import java.util.function.IntToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * GUI‑friendly statistical computation engine for the Eta system designed for comparing at most ten different (or same) distributions simultaneously.
 * <p>
 * Provides: <br>
 * - Convenience methods for eight distribution families (same as the general-use {@link EtaConsoleFXServer}). <br>
 * - Can compare up to ten distributions simultaneously(but recommend 6-8 for better ease of visualizing).
 * </p>
 * @Note This is the FX Server multi-comparator ver. of the console. Built using Apache Commons Maths & Statistics and Smile libraries.
 * @Since Eta11.5 Java edition (ESEJ 1.0)
 */
@SuppressWarnings("ALL")
public class EtaServerComparatorMultiFX {
    private static StreamX streamX = new StreamX();

    /**
     * Links the comparator backend to the core for computing the output in this specialized server.
     * @param distNames the names of the distributions,   (Type: {@code List<String>})
     * @param DistType the type of the distributions chosen(PDF / CDF),
     * @param xValue the input value(s) (Single / Array),
     * @param params the parameter(s) required for the distributions,   (Type: {@code List<Map<String, Double>>})
     * @throws RuntimeAnomalyError when it detects something wrong was occurred, and then the throwable cause:
     * @throws Exception the cause(sources) of the error.
     * */
    public static ComparatorMultiOutput compute(List<Distribution> dist, String DistType, Object xValue, List<Map<String, Double>> params) throws Exception{
        try {
            if (dist.isEmpty() || params.isEmpty()) {
                throw new ArrayIsEmptyException("Inputs cannot be empty.");
            }
            if (dist.size() != params.size()) {
                throw new InvalidInputException("Size mismatch between distribution names and parameter lists.");
            }
            if (dist.size() > 10) {
                throw new InputOutOfRangeException("Cannot process more than than ten distribution names simultaneously(reaches maximum).");
            }

            List<List<Double>> allResults = ComparatorEngine.compare(dist, DistType, xValue, params);
            List<List<Double>> firstTwo = allResults.stream().limit(2).collect(Collectors.toList());
            while (firstTwo.size() < 2) {
                firstTwo.add(Collections.emptyList());
            }
            CompOut compOut = new CompOut(firstTwo.get(0), firstTwo.get(1));

            List<List<Double>> remaining = allResults.stream().skip(2).collect(Collectors.toList());

            return new Link.CompMultiOut(compOut, remaining);
        } catch (Exception e) {
            throw new RuntimeAnomalyError("Anomaly occurred. Please verify your inputs and re-run the system. To see why, please read the following: ", e);
        }
    }

    /**
     * Computes PDF/CDF for multiple distributions, returning a primitive double array.
     *
     * @param distNames  list of distribution names (must match params size)
     * @param DistType   "PDF" or "CDF"
     * @param xValue     x‑values (will be sorted in‑place if sort is true)
     * @param params     parameter maps for each distribution
     * @param notSorted      if true, sort xValue in ascending order before computation
     * @return {@code double[distNames.size()][xValue.length]} – results for each distribution
     * @throws RuntimeAnomalyError when it detects something wrong was occurred, and then the throwable cause:
     * @throws Exception the cause(sources) of the error
     */
    public static double[][] computeFast(List<String> distNames, String DistType,
                                         double[] xValue, List<Map<String, Double>> params,
                                         boolean notSorted) throws Exception {
        try {
            if (distNames.isEmpty() || params.isEmpty()) {
                throw new ArrayIsEmptyException("Inputs cannot be empty.");
            }
            if (distNames.size() != params.size()) {
                throw new InvalidInputException("Size mismatch between distribution names and parameter lists.");
            }
            if (distNames.size() > 10) {
                throw new InputOutOfRangeException("Cannot process more than ten distribution names.");
            }
            if (xValue == null) {
                throw new IllegalArgumentException("xValue cannot be null.");
            }

            if (notSorted) {
                double[] sorted = xValue.clone();
                Arrays.sort(sorted);
                xValue = sorted;
            }

            double[] finalXValue = xValue;

            IntFunction<double[]> funcT = i -> {
                String name = distNames.get(i);
                Distribution dist = DistributionRegistry.get(name);

                if (dist == null) {
                    throw new RuntimeException(new NoSuchDistributionException(
                            "Distribution with name " + name + " not found."
                    ));
                }

                Map<String, Double> param = params.get(i);
                boolean isPdf = DistType.equalsIgnoreCase("PDF");

                IntStream stream = IntStream.range(0, finalXValue.length);

                IntToDoubleFunction func1 = j -> isPdf
                        ? dist.pdf(finalXValue[j], param)
                        : dist.cdf(finalXValue[j], param);

                return streamX.IntStreamDoubleStream(stream, func1).toArray();
            };

            IntStream stream = IntStream.range(0, distNames.size());
            return streamX.IntStreamToDoubleArray(stream, funcT);
        } catch (Exception e) {
            throw new RuntimeAnomalyError("Anomaly occurred: " + e.getMessage(), e);
        }
    }
}
