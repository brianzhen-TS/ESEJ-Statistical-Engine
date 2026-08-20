package Backends.TwoComparator;

import Backends.PowerAnalyser.FXPowerAnalyserServer;
import FXBackends.EtaConsoleFXServer;
import Link.ExceptionStat.SystemException.*;
import Link.ExceptionStat.SystemError.RuntimeAnomalyError;
import Link.NonCentralHead;
import eta.util.StreamX;
import smile.stat.hypothesis.*;
import smile.math.MathEx;

import Link.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.IntFunction;
import java.util.function.IntToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * GUI‑friendly statistical computation engine for the Eta system designed for comparing two different (or same) distributions.
 * <p>
 * Provides: <br>
 * - Convenience methods for eight distribution families (same as the general-use {@link EtaConsoleFXServer}). <br>
 * - Able users to compute power from various statistical tests. (Used in power analysers like {@link FXPowerAnalyserServer})
 * </p>
 * @Note This is the FX Server comparator(for two distributions) ver. of the console. Built using Apache Commons Maths & Statistics and Smile libraries.
 * @Since Eta11.5 Java edition (ESEJ 1.0)
 */
@SuppressWarnings("ALL")
public class EtaServerComparatorTwoFX {
    private static StreamX streamX = new StreamX();

    /**
     * Links the comparator backend to the core for computing the output in this specialized server.
     * @param distNames the names of the two distributions,   (Type: {@code List<String>})
     * @param DistType the type of the distributions chosen(PDF / CDF),
     * @param xValue the input value(s) (Single / Array),
     * @param params the parameter(s) required for the distribution 1 & 2,   (Type: {@code List<Map<String, Double>>})
     * @throws RuntimeAnomalyError when it detects something wrong was occurred, and then the throwable cause:
     * @throws Exception the cause(sources) of the error.
     * */
    public static ComparatorOutput compute(List<Link.Distribution> dist, String DistType, Object xValue, List<Map<String, Double>> params) throws Exception {
        try {
            if (dist.size() != 2 || params.size() != 2) {
                throw new InvalidInputException("Exactly two distributions and two parameter maps required.");
            }
            List<Map<String, Double>> sanitizedParams = new ArrayList<>();
            for (int i = 0; i < params.size(); i++) {
                Map<String, Double> original = params.get(i);
                Map<String, Double> sanitized = new HashMap<>(original);
                Distribution d = dist.get(i);
                String name = d.getName();

                // Non‑central distributions
                if (name.contains("NonCentral F") || name.contains("Noncentral F")) {
                    sanitized.put("df1", Math.max(sanitized.getOrDefault("df1", 1.0), 1e-6));
                    sanitized.put("df2", Math.max(sanitized.getOrDefault("df2", 1.0), 1e-6));
                    sanitized.put("lambda", Math.max(sanitized.getOrDefault("lambda", 0.0), 1e-6));
                } else if (name.contains("NonCentral T") || name.contains("Noncentral T")) {
                    sanitized.put("df", Math.max(sanitized.getOrDefault("df", 1.0), 1e-6));
                    sanitized.put("lambda", Math.max(sanitized.getOrDefault("lambda", 0.0), 1e-6));
                } else if (name.contains("NonCentral Chi") || name.contains("Noncentral Chi")) {
                    sanitized.put("df", Math.max(sanitized.getOrDefault("df", 1.0), 1e-6));
                    sanitized.put("lambda", Math.max(sanitized.getOrDefault("lambda", 0.0), 1e-6));
                }

                // Central distributions (just to be safe)
                if (name.equals("F") || name.equals("Chi-squared") || name.equals("T")) {
                    if (name.equals("F")) {
                        sanitized.put("df1", Math.max(sanitized.getOrDefault("df1", 1.0), 1e-6));
                        sanitized.put("df2", Math.max(sanitized.getOrDefault("df2", 1.0), 1e-6));
                    } else {
                        sanitized.put("df", Math.max(sanitized.getOrDefault("df", 1.0), 1e-6));
                    }
                }
                sanitizedParams.add(sanitized);
            }

            List<List<Double>> allResults = ComparatorEngine.compare(dist, DistType, xValue, sanitizedParams);
            return new CompOut(allResults.get(0), allResults.get(1));
        } catch (Exception e) {
            throw new RuntimeAnomalyError("Anomaly occurred. Please verify your inputs and re-run the system. To see why, please read the following: ", e);
        }
    }

    public static List<double[]> computeToList(List<Distribution> dist, String DistType, Object xValue, List<Map<String, Double>> params) throws Exception {
        try {
            if (dist.size() != 2 || params.size() != 2) {
                throw new IllegalArgumentException("Exactly two distributions and two parameter maps required.");
            }

            // Use the existing compute method to get the full result (List<List<Double>>)
            ComparatorOutput output = compute(dist, DistType, xValue, params);

            // Extract the two lists and convert to double[]
            List<Double> list1 = output.Dist1();
            List<Double> list2 = output.Dist2();

            return IntStream.range(list1.size(), list2.size())
                    .mapToObj(i -> new double[]{list1.get(i), list2.get(i)})
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeAnomalyError("Anomaly occurred in computeToList: ", e);
        }
    }

    public static double[][] computeFast(List<String> distNames, String DistType, double[] xValue, List<Map<String, Double>> params, boolean notSorted) throws Exception {
        try {
            if (distNames.size() != 2 || params.size() != 2) {
                throw new InvalidInputException("Exactly two distributions and two parameter maps required.");
            }

            if (notSorted) {
                double[] sorted = xValue.clone();
                Arrays.sort(sorted);
                xValue = sorted;
            }

            double[] finalXValue = xValue;

            IntFunction<double[]> func = i -> {
                String name = distNames.get(i);
                Distribution dist = DistributionRegistry.get(name);

                if (dist == null) {
                    throw new RuntimeException(new NoSuchDistributionException(
                            "Distribution with name " + distNames.get(i) + " not found."
                    ));
                }

                Map<String, Double> param = params.get(i);
                boolean isPdf = DistType.equalsIgnoreCase("PDF");

                IntStream stream = IntStream.range(0, finalXValue.length);

                IntToDoubleFunction funcToDouble = j -> isPdf
                        ? dist.pdf(finalXValue[j], param)
                        : dist.cdf(finalXValue[j], param);

                return streamX.IntStreamDoubleStream(stream, funcToDouble).toArray();
            };

            IntStream stream = IntStream.range(0, distNames.size());
            return streamX.IntStreamToDoubleArray(stream, func);
        } catch (Exception e) {
            throw new RuntimeAnomalyError("Anomaly occurred. Please verify your inputs and re-run the system. To see why, please read the following: ", e);
        }
    }

    /**
     * Provides power analysis capabilities for the Eta system. Using Smile library.
     * <p>
     * <b>Provides</b>: <br>
     * - T test power (only for two independent samples) <br>
     * - Chi-squared (Goodness-of-fit, Contingency table analysis, Variance test) <br>
     * - F (Variance test, ANOVA)
     * </p>
     */
    public static class powerAnalysis {

        /**
         * Power analysis for a two‑sample T‑test (unequal variances not supported).
         * @param data double[2][n] where data[0] = sample1, data[1] = sample2
         * @return {@code double[]}
         * <p> where <br>
         * {@code double[0] = t}  -the statistic, <br>
         * {@code double[1] = power}  -the power of the test, <br>
         * {@code double[2] = p1}  -the density value of the T distribution given t; the statistic, <br>
         * {@code double[3] = p2}  -the density value of the NonCentral T distribution given t.
         * </p>
         */
        public static double[] T(double[][] data) throws Exception {
            // Compute sample statistics
            double[] sample1 = data[0];
            double[] sample2 = data[1];
            int n1 = sample1.length, n2 = sample2.length;
            double mean1 = MathEx.mean(sample1);
            double mean2 = MathEx.mean(sample2);
            double var1 = MathEx.var(sample1);
            double var2 = MathEx.var(sample2);
            double pooledVar = ((n1 - 1) * var1 + (n2 - 1) * var2) / (n1 + n2 - 2);
            double pooledStd = Math.sqrt(pooledVar * (1.0 / n1 + 1.0 / n2));

            // Smile T‑test
            TTest test = TTest.test(sample1, sample2);
            double t = test.t();
            double df = test.df();
            if (df < 0) df = 0;

            // Non‑centrality parameter for T distribution
            Map<String, Double> param = new HashMap<>();
            param.put("stDev", pooledStd);
            param.put("mean1", mean1);
            param.put("mean2", mean2);
            param.put("df", df);
            double ncp = Link.ComparatorPower.power.NoncentralParam("T", param);
            if (ncp < 0) ncp = 1e-6;

            // Power = 1 - CDF(non‑central T) at the critical value (two‑sided)
            double power = ComparatorPower.power.Value(t, "T", Map.of("df", df, "lambda", ncp));

            // PDF values (for diagnostic)
            Distribution tDist = DistributionRegistry.get("T");
            double p1 = tDist.pdf(t, Map.of("df", df));
            double p2 = NonCentralHead.NonCentT(t, df, ncp);

            return new double[]{t, power, p1, p2};
        }

        public static class ChiSquared {
            private static Distribution dist = DistributionRegistry.get("Chi-squared");
            private static double chi_2;
            private static double df;
            private static Map<String, Double> param = new ConcurrentHashMap<>();
            private static double NonCentral;
            private static double power, p1, p2;
            public static double[] GoodnessOfFit(int[] observed, double[] expected) throws Exception {
                for (double p : expected) {
                    if (p < 0 || p > 1) {
                        throw new InvalidInputException("expected probability must be between 0 and 1.");
                    }
                };
                ChiSqTest test = ChiSqTest.test(observed, expected);
                chi_2 = test.chisq();
                df = test.df();
                param.put("df", df);
                NonCentral = ComparatorPower.power.NoncentralParam("Chi-squared", param);
                Map<String, Double> list = new ConcurrentHashMap<>();
                list.put("df", df);
                list.put("lambda", NonCentral);
                power = ComparatorPower.power.Value(chi_2, "Chi-squared", list);
                p1 = dist.pdf(chi_2, param);
                p2 = NonCentralHead.NonCentChiSquared(chi_2, param.get("df"), NonCentral);
                return new double[]{chi_2, power, p1, p2};
            }
            public static double[] ContingencyTable(int[][] table) throws Exception {
                ChiSqTest test = ChiSqTest.test(table);
                chi_2 = test.chisq();
                NonCentral = ComparatorPower.power.NoncentralParam("Chi-squared", Map.of("df", test.df()));
                Map<String, Double> list = new ConcurrentHashMap<>();
                list.put("df", test.df());
                list.put("lambda", NonCentral);
                power = ComparatorPower.power.Value(chi_2, "Chi-squared", list);
                p1 = dist.pdf(chi_2, Map.of("df", test.df()));
                p2 = NonCentralHead.NonCentChiSquared(chi_2, test.df(), NonCentral);
                return new double[]{chi_2, power, p1, p2};
            }
            public static double[] VarianceTest(double[] sample, double hypothesizedVariance) throws Exception {
                double Var = MathEx.var(sample);
                double df = sample.length - 1;
                chi_2 = df * Var / hypothesizedVariance;
                NonCentral = ComparatorPower.power.NoncentralParam("Chi-squared", Map.of("df", df));
                Map<String, Double> list = new ConcurrentHashMap<>();
                list.put("df", df);
                list.put("lambda", NonCentral);
                power = ComparatorPower.power.Value(chi_2, "Chi-squared", list);
                p1 = dist.pdf(chi_2, Map.of("df", df));
                p2 = NonCentralHead.NonCentChiSquared(chi_2, df, NonCentral);
                return new double[]{chi_2, power, p1, p2};
            }
        }

        // F‑test power analysis
        public static class F {
            private static double F;
            private static double NonCentral;
            private static double power, p1, p2;
            private static Distribution dist = DistributionRegistry.get("F");
            public static double[] VarianceTest(double[][] samples) throws Exception {
                FTest test = FTest.test(samples[0], samples[1]);
                F = test.f();
                Map<String, Double> param = new ConcurrentHashMap<>();
                param.put("df1", (double) test.df1());
                param.put("df2", (double) test.df2());

                Map<String, Double> listParam = new ConcurrentHashMap<>();
                listParam.put("Size", (double)(test.df1() + test.df2() + 2));
                listParam.put("FStatistic", F);

                NonCentral = ComparatorPower.power.NoncentralParam("F", listParam);
                Map<String, Double> powerMap = new HashMap<>();
                powerMap.put("df1", (double) test.df1());
                powerMap.put("df2", (double) test.df2());
                powerMap.put("lambda", NonCentral);
                power = ComparatorPower.power.Value(F, "F", powerMap);
                p1 = dist.pdf(F, param);
                p2 = NonCentralHead.NonCentF(F, powerMap.get("df1"), powerMap.get("df2"), powerMap.get("lambda"));
                return new double[]{F, power, p1, p2};
            }
            public static double[] ANOVA(double[] values, int[] groups) throws Exception {
                FTest test = FTest.test(groups, values);
                F = test.f();

                Map<String, Double> param = new ConcurrentHashMap<>();
                param.put("FStatistic", F);
                param.put("Size", (double)(test.df1() + test.df2()));
                param.put("df1", (double) test.df1());
                param.put("df2", (double) test.df2());
                NonCentral = ComparatorPower.power.NoncentralParam("F", param);
                param.put("lambda", NonCentral);
                Map<String, Double> list = new ConcurrentHashMap<>(param);
                power = ComparatorPower.power.Value(F, "F", list);
                p1 = dist.pdf(NonCentral, param);
                p2 = NonCentralHead.NonCentF(F, list.get("df1"), list.get("df2"), list.get("lambda"));
                return new double[]{F, power, p1, p2};
            }
        }

        /**
         * The enum for all the test the backend supports. Can be used as the description of your GUI features(like buttons).
         */
        public class TestTypeEnum {
            /**
             * The enum of Chi-squared test power computations. It includes:
             * <p>
             * - Goodness-of-fit: can determine how fit does the model given samples. <br>
             * - Homogeneity: can determine the homogeneity(how correlated) of the two sample groups. <br>
             * - Contingency-table: can determine the relations of each element in a contingency table. <br>
             * - Variance: can determine the difference between sample variance and the group variance.
             * </p>*/
            public enum ChiSquared {
                GoodnessOfFit("Goodness-of-fit"),
                Homogeneity("Homogeneity"),
                ContingencyTable("Contingency-table"),
                Variance("Variance");

                private String s;

                ChiSquared(String s) {
                    this.s = s;
                }

                public String toString() {
                    return this.s;
                }
            }

            /**
             * The enum of F-test power computations. It includes:
             * <p>
             * - Variance: can determine the difference between the group variance given two groups. <br>
             * - ANOVA: ANOVA tests. Used to analyze the mean parameters of three or more groups.
             * </p>
             * */
            public enum F {
                Variance("Variance"),
                ANOVA("ANOVA");

                private String s;

                F(String s) {
                    this.s = s;
                }

                public String toString() {
                    return this.s;
                }
            }
        }
    }
}
