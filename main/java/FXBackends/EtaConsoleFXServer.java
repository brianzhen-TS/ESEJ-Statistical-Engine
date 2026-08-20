package FXBackends;// FXBackends.EtaConsoleFXServer.java – GUI‑friendly statistical backend

import java.lang.IO;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.IntStream;

import Link.*;
import Link.ExceptionStat.SystemException.*;
import Link.ExceptionStat.SystemError.RuntimeAnomalyError;
import eta.util.StreamX;

/**
 * GUI‑friendly statistical computation engine for the Eta system.
 * <p>
 * Provides: <br>
 * - Convenience methods for eight distribution families. <br>
 * - Unlike the Web version of the server({@code EtaConsoleWebServer}), it directly links to the EtaCore for better ease of calculations
 * and enable users to customize the GUI outputs. <br>
 * - Enables calculations of estimated CDFs(ECDF) to determine goodness-of-fit.
 * </p>
 * <b>Usage example:</b>
 * <pre>{@code import EtaSystem.FXBackends.EtaConsoleFXServer.compute;
 * import EtaSystem.EtaCore.*;
 * import EtaSystem.EtaCore.ExceptionStat.*;
 * import EtaSystem.EtaCore.Multivariate.*;   // Optional; Depends on your needs
 * // ...your JavaFX GUIs
 * PlotterOutput probability = EtaConsoleFXServer.compute(Distname, type, xValue, map, false);   // false if not sorted
 * // Then add it to the place you want it to be distplayed, like:
 * series.getData().add(new XYChart.Data<>(x, probability))  // x: input, y(= probability; Optional; Remember converting it to double before computation): output
 * }</pre>
 * <b>Where</b>
 * <p>
 * {@code DistName} is the name of the distribution you want to compute, <br>
 * {@code type} is the type of the distribution(PDF / CDF), and<br>
 * {@code param} is the parameters required for the computation.
 * </p>
 * @Note This is the FX Server general-use ver. of the console. Built using Apache Commons Maths & Statistics libraries.
 * @Since Eta11.5 Java edition (ESEJ 1.0)
 */
@SuppressWarnings("ALL")
public class EtaConsoleFXServer {
    private static StreamX streamX = new StreamX();

    /**
     * Computes PDF/CDF using a pre‑retrieved Link.Distribution object.
     * @param dist       the distribution object (already retrieved)
     * @param type       "PDF" or "CDF"
     * @param xValue     single Double or double[]
     * @param param      parameter map
     * @param notSorted  if true, sort xValue
     * @throws RuntimeAnomalyError if the system catches anomaly occurance(s), where the cause is represented by an {@code Exception}
     * @throws Exception the cause(s).
     */
    public static PlotterOutput compute(Distribution dist, String type, Object xValue, Map<String, Double> param, boolean notSorted) throws Exception {
        try {
            ConcurrentMap<String, Double> params = new ConcurrentHashMap<>(param);

            double[] array = toDoubleArray(xValue);
            if (notSorted) Arrays.sort(array);

            Function<Double, Double> map = val -> type.equalsIgnoreCase("PDF") ? dist.pdf(val, params) : dist.cdf(val, params);
            List<Double> values = streamX.ArrayToStreamDouble(array, map).toList();
            return new PlotOut(values);
        } catch (Exception e) {
            throw new RuntimeAnomalyError("Anomaly occurred. Please verify your inputs and re-run the system. To see why, please read the following: ", e);
        }
    }

    // fast mode
    public static double[] computePrimitive(Distribution dist, String type, Object xValue, Map<String, Double> param, boolean sort) throws Exception {
        ConcurrentMap<String, Double> params = new ConcurrentHashMap<>(param);
        double[] array = toDoubleArray(xValue);
        if (sort) Arrays.sort(array);

        double[] results = new double[array.length];
        int threshold = 10000;

        if (array.length > threshold) {
            // use threads for fast computing large arrays
            IntStream.range(0, array.length).forEach(val -> {results[val]
            = type.equalsIgnoreCase("PDF")
                    ? dist.pdf(val, params) : dist.cdf(val, params);});
        } else {
            for (int i = 0; i < array.length; i++) {
                results[i] = type.equalsIgnoreCase("PDF") ? dist.pdf(array[i], params) : dist.cdf(array[i], params);
            }
        }
        return results;
    }

    public static ComparatorOutput ComparateWithECDF(String TheroDistName, Object data, Object xValues, Map<String, Double> params, boolean notSorted) throws Exception {
        try {
            ConcurrentMap paramIn = new ConcurrentHashMap<>(params);
            Distribution dist = DistributionRegistry.get(TheroDistName);

            double[] arr = toDoubleArray(xValues);

            double[] dataSorted = toDoubleArray(data);
            if (notSorted) Arrays.sort(dataSorted);
            double[] xSorted = arr.clone();

            List<Double> empCDF = new ArrayList<>(arr.length);
            int count = 0;
            for (double x : arr) {
                while (count < dataSorted.length && dataSorted[count] <= x) count++;
                empCDF.add((double) count / dataSorted.length);
            }
            Function<Double, Double> func = val -> dist.cdf(val, paramIn);
            List<Double> theroCDF = streamX.ArrayToStreamDouble(arr, func).toList();
            return new CompOut(empCDF, theroCDF);
        } catch (Exception e) {
            throw new RuntimeAnomalyError("Anomaly occurred. Please verify your inputs and re-run the system. To see why, please read the following: ", e);
        }
    }

    /**
     * Convert Object to double[] for compute() method.
     * @param xValue the input(s) (Array / Single; Double)
     */
    private static double[] toDoubleArray(Object xValue) {
        if (xValue instanceof Double) return new double[]{(Double) xValue};
        if (xValue instanceof double[]) return (double[]) xValue;
        throw new InvalidInputException("xValue must be Double or double[]");
    }

    /**
     * The testing method for the server, showing what your front-end should receive from the back-end. If it has no anomalies, it should give:
     * <pre>
     * {@code // Distribution chosen: F
     * PlotOut[dist=[0.18763827704821573, 0.08494663130441583, 0.019232444852391457]]}
     * </pre>
     * Instead of something like this:
     * <pre>
     * {@code // Anomaly occurance(s)
     * Exception in thread "main" EtaCore.ExceptionStat.SystemError.RuntimeAnomalyError: Anomaly occurred. Please verify your inputs and re-run the system. To see why, please read the following:
     * 	at FXBackends.EtaConsoleFXServer.compute(FXBackends.EtaConsoleFXServer.java:61)
     * 	at FXBackends.EtaConsoleFXServer.main(FXBackends.EtaConsoleFXServer.java:101)
     * Caused by: EtaCore.ExceptionStat.SystemException.InvalidInputException: xValue must be Double or double[]
     * 	at FXBackends.EtaConsoleFXServer.toDoubleArray(FXBackends.EtaConsoleFXServer.java:72)
     * 	at FXBackends.EtaConsoleFXServer.compute(FXBackends.EtaConsoleFXServer.java:45)
     * 	... 1 more}
     * 	</pre>
     * 	Which is caused because the system received inputs with type {@code Double[]} instead of {@code double[]}(in this example).*/
    // Testing
    public static void main() throws Exception {
        String type = "PDF";
        double[] arr = {1.1, 6.7, 2.2};
        Map<String, Double> param = new HashMap<>();
        param.put("df1", 3.0);
        param.put("df2", 1.0);
        PlotterOutput output = compute(DistributionRegistry.get("F"), type, arr, param, true);
        IO.println(output);
    }
}