package Backends.PowerAnalyser;

import Backends.TwoComparator.EtaServerComparatorTwoFX;
import Link.ComparatorPower;
import smile.math.MathEx;
import smile.stat.hypothesis.ChiSqTest;
import smile.stat.hypothesis.FTest;
import smile.stat.hypothesis.TTest;

import java.util.*;

public class FXPowerAnalyserServer {
    public record ParamDef(String name, String label, String defaultVal) {
    }

    public static List<ParamDef> getParameterDefinitions(String family, String sub) {
        List<ParamDef> list = new ArrayList<>();
        switch (family) {
            case "T-test" -> {
                // No extra params needed (sample data already in text area)
            }
            case "Chi-squared" -> {
                if ("Variance test".equals(sub)) {
                    list.add(new ParamDef("hypothesizedVariance", "Hypothesized variance", "1.0"));
                }
                // Goodness‑of‑fit and Contingency don't need extra numeric params
            }
            case "F-test" -> {
                if ("Variance test (two samples)".equals(sub)) {
                    // no extra
                } else if ("ANOVA (one‑way)".equals(sub)) {
                    // no extra
                }
            }
        }
        return list;
    }

    public static Object statisticsAndParams(String family, String sub, String input, boolean needsParams,
                                             Map<String, Double> extraParams) throws Exception {
        // Normalize family and sub strings (replace non‑breaking dashes)
        family = family.trim().replace("‑", "-").replace("–", "-");
        sub = sub.trim().replace("‑", "-").replace("–", "-");

        Object data = parseData(family, sub, input);

        if (!needsParams) {
            // Return critical value
            switch (family) {
                case "T-test" -> {
                    double[][] arr = (double[][]) data;
                    TTest tTest = TTest.test(arr[0], arr[1]);
                    return tTest.t();
                }
                case "Chi-squared" -> {
                    ChiSqTest test;
                    switch (sub) {
                        case "Variance test" -> {
                            double[] sample = (double[]) data;
                            double hypVar = extraParams.getOrDefault("hypothesizedVariance", 1.0);
                            double n = sample.length;
                            double sampleVar = MathEx.var(sample);
                            return (n - 1) * sampleVar / hypVar;  // chi‑squared statistic
                        }
                        case "Contingency table" -> {
                            int[][] table = (int[][]) data;
                            test = ChiSqTest.test(table);
                            return test.chisq();
                        }
                        case "Goodness-of-fit" -> {
                            Object[] obsExp = (Object[]) data;
                            int[] observed = (int[]) obsExp[0];
                            double[] expected = (double[]) obsExp[1];
                            // Normalize expected probabilities to sum to 1
                            double sum = 0.0;
                            for (double v : expected) sum += v;
                            if (Math.abs(sum - 1.0) > 1e-6) {
                                for (int i = 0; i < expected.length; i++) expected[i] /= sum;
                            }
                            test = ChiSqTest.test(observed, expected);
                            return test.chisq();
                        }
                        default -> throw new IllegalArgumentException("Unknown sub-test: " + sub);
                    }
                }
                case "F-test" -> {
                    FTest test;
                    switch (sub) {
                        case "Variance test (two samples)" -> {
                            double[][] arr = (double[][]) data;
                            test = FTest.test(arr[0], arr[1]);
                            return test.f();
                        }
                        case "ANOVA (one-way)" -> {
                            Object[] arr = (Object[]) data;
                            int[] groups = (int[]) arr[0];
                            double[] values = (double[]) arr[1];
                            test = FTest.test(groups, values);
                            return test.f();
                        }
                        default -> throw new IllegalArgumentException("Unknown sub-test: " + sub);
                    }
                }
                default -> throw new UnsupportedOperationException("Unsupported family: " + family);
            }
        } else {
            // Return parameters (df, lambda, etc.)
            switch (family) {
                case "T-test" -> {
                    double[][] arr = (double[][]) data;
                    TTest tTest = TTest.test(arr[0], arr[1]);
                    double std = Math.sqrt((MathEx.stdev(arr[0]) / arr[0].length) +
                            MathEx.stdev(arr[1]) / arr[1].length);
                    double lambda = ComparatorPower.power.NoncentralParam("T",
                            Map.of("stDev", std,
                                    "mean1", MathEx.mean(arr[0]),
                                    "mean2", MathEx.mean(arr[1]),
                                    "df", tTest.df()));
                    lambda = Math.abs(lambda);
                    double df = Math.abs(tTest.df());
                    return new Object[]{df, lambda};
                }
                case "Chi-squared" -> {
                    ChiSqTest test;
                    switch (sub) {
                        case "Variance test" -> {
                            double[] sample = (double[]) data;
                            double hypVar = extraParams.getOrDefault("hypothesizedVariance", 1.0);
                            double n = sample.length;
                            double sampleVar = MathEx.var(sample);
                            double chi2 = (n - 1) * sampleVar / hypVar;
                            double df = n - 1;
                            // For non‑central parameter, we need something – we'll use a placeholder
                            double lambda = 0.0; // or compute from effect size if available
                            // For simplicity, set lambda to 0 and handle later
                            return new Object[]{lambda, df};
                        }
                        case "Contingency table" -> {
                            int[][] table = (int[][]) data;
                            test = ChiSqTest.test(table);
                            double lambda = ComparatorPower.power.NoncentralParam(
                                    "Chi-squared", Map.of(
                                            "stDev", MathEx.stdev(table[0]), // placeholder
                                            "df", test.df()
                                    ));
                            lambda = Math.abs(lambda);
                            double df = Math.abs(test.df());
                            return new Object[]{lambda, df};
                        }
                        case "Goodness-of-fit" -> {
                            Object[] obsExp = (Object[]) data;
                            int[] observed = (int[]) obsExp[0];
                            double[] expected = (double[]) obsExp[1];
                            double sum = 0.0;
                            for (double v : expected) sum += v;
                            if (Math.abs(sum - 1.0) > 1e-6) {
                                for (int i = 0; i < expected.length; i++) expected[i] /= sum;
                            }
                            test = ChiSqTest.test(observed, expected);
                            double lambda = ComparatorPower.power.NoncentralParam(
                                    "Chi-squared", Map.of(
                                            "stDev", MathEx.stdev(observed),
                                            "df", test.df()
                                    ));
                            lambda = Math.abs(lambda);
                            double df = Math.abs(test.df());
                            return new Object[]{lambda, df};
                        }
                        default -> throw new IllegalArgumentException("Unknown sub-test: " + sub);
                    }
                }
                case "F-test" -> {
                    FTest test;
                    switch (sub) {
                        case "Variance test (two samples)" -> {
                            double[][] arr = (double[][]) data;
                            test = FTest.test(arr[0], arr[1]);
                            double size = test.df1() + test.df2() + 2;
                            double lambda = ComparatorPower.power.NoncentralParam("F", Map.of(
                                    "Size", size, "FStatistic", test.f()
                            ));
                            lambda = Math.abs(lambda);
                            double df1 = Math.abs(test.df1());
                            double df2 = Math.abs(test.df2());
                            return new Object[]{df1, df2, lambda};
                        }
                        case "ANOVA (one-way)" -> {
                            Object[] arr = (Object[]) data;
                            int[] groups = (int[]) arr[0];
                            double[] values = (double[]) arr[1];
                            test = FTest.test(groups, values);
                            double size = test.df1() + test.df2() + 1;
                            double lambda = ComparatorPower.power.NoncentralParam("F", Map.of(
                                    "Size", size, "FStatistic", test.f()
                            ));
                            lambda = Math.abs(lambda);
                            double df1 = Math.abs(test.df1());
                            double df2 = Math.abs(test.df2());
                            return new Object[]{df1, df2, lambda};
                        }
                        default -> throw new IllegalArgumentException("Unknown sub-test: " + sub);
                    }
                }
                default -> throw new UnsupportedOperationException("Unsupported family: " + family);
            }
        }
    }

    public static Map<String, Double> paramMap(String family, Object params) {
        Map<String, Double> map = new HashMap<>();
        Object[] obj = (Object[]) params;
        switch (family) {
            case "T-test" -> {
                map.put("df", (double) obj[0]);
                map.put("lambda", (double) obj[1]);
            }
            case "Chi-squared" -> {
                map.put("lambda", (double) obj[0]);
                map.put("df", (double) obj[1]);
            }
            case "F-test" -> {
                map.put("df1", (double) obj[0]);
                map.put("df2", (double) obj[1]);
                map.put("lambda", (double) obj[2]);
            }
        }
        return map;
    }

    // -------------------- Data Parsing --------------------
    public static Object parseData(String family, String sub, String input) {
        // Split lines
        String[] lines = input.split("\\n");
        List<String> nonEmptyLines = new ArrayList<>();
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) nonEmptyLines.add(trimmed);
        }

        switch (family) {
            case "T-test" -> {
                String[] rawLines = input.split("\\n");
                List<Double> sample1 = new ArrayList<>();
                List<Double> sample2 = new ArrayList<>();
                boolean second = false;
                for (String line : rawLines) {
                    if (line.trim().isEmpty()) {
                        second = true;
                        continue;
                    }
                    List<Double> values = parseNumbers(line);
                    if (second) sample2.addAll(values);
                    else sample1.addAll(values);
                }
                if (sample1.isEmpty() || sample2.isEmpty()) {
                    throw new IllegalArgumentException("Both samples must contain data.");
                }
                return new double[][]{
                        sample1.stream().mapToDouble(Double::doubleValue).toArray(),
                        sample2.stream().mapToDouble(Double::doubleValue).toArray()
                };
            }
            case "Chi-squared" -> {
                if ("Goodness‑of‑fit".equals(sub)) {
                    if (nonEmptyLines.size() < 2) {
                        throw new IllegalArgumentException("Need observed and expected lines.");
                    }
                    int[] observed = parseIntegerArray(nonEmptyLines.get(0));
                    double[] expected = parseDoubleArray(nonEmptyLines.get(1));
                    return new Object[]{observed, expected};
                } else if ("Contingency table".equals(sub)) {
                    // Each line is a row, numbers separated by commas
                    int[][] table = new int[nonEmptyLines.size()][];
                    for (int i = 0; i < nonEmptyLines.size(); i++) {
                        table[i] = parseIntegerArray(nonEmptyLines.get(i));
                    }
                    return table;
                } else if ("Variance test".equals(sub)) {
                    // Single sample
                    return parseDoubleArray(nonEmptyLines.getFirst());
                }
            }
            case "F-test" -> {
                if ("Variance test (two samples)".equals(sub)) {
                    // Two samples like T-test
                    String[] rawLines = input.split("\\n");
                    List<Double> sample1 = new ArrayList<>();
                    List<Double> sample2 = new ArrayList<>();
                    boolean second = false;
                    for (String line : rawLines) {
                        if (line.trim().isEmpty()) {
                            second = true;
                            continue;
                        }
                        List<Double> values = parseNumbers(line);
                        if (second) sample2.addAll(values);
                        else sample1.addAll(values);
                    }
                    if (sample1.isEmpty() || sample2.isEmpty()) {
                        throw new IllegalArgumentException("Both samples must contain data.");
                    }
                    return new double[][]{
                            sample1.stream().mapToDouble(Double::doubleValue).toArray(),
                            sample2.stream().mapToDouble(Double::doubleValue).toArray()
                    };
                } else if ("ANOVA (one‑way)".equals(sub)) {
                    // Format: first line = group labels (optional), subsequent lines: value, group
                    // For simplicity, we expect two lines: values and groups (same length)
                    if (nonEmptyLines.size() < 2) {
                        throw new IllegalArgumentException("Need values and groups.");
                    }
                    double[] values = parseDoubleArray(nonEmptyLines.get(0));
                    int[] groups = parseIntegerArray(nonEmptyLines.get(1));
                    if (values.length != groups.length) {
                        throw new IllegalArgumentException("Values and groups must have same length.");
                    }
                    return new Object[]{values, groups};
                }
            }
        }
        throw new UnsupportedOperationException("Unsupported test type.");
    }

    // Helper: parse comma/space separated numbers into List<Double>
    private static List<Double> parseNumbers(String line) {
        List<Double> list = new ArrayList<>();
        for (String part : line.split("[,\\s]+")) {
            part = part.trim();
            if (!part.isEmpty()) {
                try {
                    list.add(Double.parseDouble(part));
                } catch (NumberFormatException ignored) {}
            }
        }
        return list;
    }

    private static double[] parseDoubleArray(String line) {
        return parseNumbers(line).stream().mapToDouble(Double::doubleValue).toArray();
    }

    private static int[] parseIntegerArray(String line) {
        return parseNumbers(line).stream().mapToInt(Double::intValue).toArray();
    }

    // -------------------- Power Computation --------------------
    public static double[] computePower(String family, String sub, Object parsed, Map<String, Double> extraParams) throws Exception {
        switch (family) {
            case "T-test" -> {
                double[][] data = (double[][]) parsed;
                return EtaServerComparatorTwoFX.powerAnalysis.T(data);
            }
            case "Chi-squared" -> {
                if ("Goodness‑of‑fit".equals(sub)) {
                    Object[] obsExp = (Object[]) parsed;
                    int[] observed = (int[]) obsExp[0];
                    double[] expected = (double[]) obsExp[1];
                    return EtaServerComparatorTwoFX.powerAnalysis.ChiSquared.GoodnessOfFit(observed, expected);
                } else if ("Contingency table".equals(sub)) {
                    int[][] table = (int[][]) parsed;
                    return EtaServerComparatorTwoFX.powerAnalysis.ChiSquared.ContingencyTable(table);
                } else if ("Variance test".equals(sub)) {
                    double[] sample = (double[]) parsed;
                    double hypVar = extraParams.getOrDefault("hypothesizedVariance", 1.0);
                    return EtaServerComparatorTwoFX.powerAnalysis.ChiSquared.VarianceTest(sample, hypVar);
                }
            }
            case "F-test" -> {
                if ("Variance test (two samples)".equals(sub)) {
                    double[][] samples = (double[][]) parsed;
                    return EtaServerComparatorTwoFX.powerAnalysis.F.VarianceTest(samples);
                } else if ("ANOVA (one‑way)".equals(sub)) {
                    Object[] valGroups = (Object[]) parsed;
                    int[] groups = (int[]) valGroups[1];
                    long distinctGroups = Arrays.stream(groups).distinct().count();
                    FTest test = FTest.test(groups, (double[]) valGroups[0]);
                    if (test.df1() < 1 || test.df2() < 1) {
                        throw new IllegalArgumentException(
                                "Insufficient data for ANOVA: df1=" + test.df1() + ", df2=" + test.df2() +
                                        ". You need at least two groups with at least two observations total (e.g., two groups, two observations each)."
                        );
                    }
                    if (distinctGroups < 2) {
                        throw new IllegalArgumentException(
                                "ANOVA requires at least two distinct groups. Found " + distinctGroups + " group(s)."
                        );
                    }
                    double[] values = (double[]) valGroups[0];
                    return EtaServerComparatorTwoFX.powerAnalysis.F.ANOVA(values, groups);
                }
            }
        }
        throw new UnsupportedOperationException("Computation not implemented.");
    }

    // -------------------- Result Formatting --------------------
    public static String formatResult(String family, String sub, double[] result) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Power Analysis Result ===\n");
        sb.append("Test: ").append(family).append(" – ").append(sub).append("\n\n");

        if (result.length >= 2) {
            sb.append("Statistic: ").append(result[0]).append("\n");
            sb.append("Power:     ").append(result[1]).append("\n");
        }
        if (result.length >= 4) {
            sb.append("PDF (central):    ").append(result[2]).append("\n");
            sb.append("PDF (non‑central):").append(result[3]).append("\n");
        }
        // Additional info can be added
        return sb.toString();
    }
}
