package Link;

import java.util.Map;

public class ComparatorPower {
    private static double CohenD(double m1, double m2, double s) {
        if (s <= 0) throw new ArithmeticException("s must be greater than zero");
        return (m1 - m2) / s;
    }

    private static double fSquared(double Total, double group, double statistic) {
        return statistic * ((group - 1) / (Total - group));
    }

    public static class power {
        public static double NoncentralParam(String distName, Map<String, Double> map) throws Exception{
            // T
            Double s = map.get("stDev");
            Double m1 = map.get("mean1");
            Double m2 = map.get("mean2");

            // T and Chi-squared
            Double df = map.get("df");

            // default values (T & Chi-squared)
            if (s == null || m1 == null || m2 == null || df == null) {
                s = 1e-100;
                m1 = 0.0;
                m2 = 0.0;
                df = 0.0;
            }

            // F
            Double Size = map.get("Size");
            Double FStatistic = map.get("FStatistic");

            // default values (F)
            if (Size == null) Size = 0.0;
            if (FStatistic == null) FStatistic = 1e-100;

            return switch (distName) {
                case "Chi-squared" -> (Math.pow(s, 2) - 2 * m1) / 2;
                case "T" -> Math.sqrt(df + 1) * CohenD(m1, m2, s);
                case "F" -> df * fSquared(Size, Size - 1, FStatistic);
                default -> throw new NoSuchMethodException("Distribution not found.");
            };
        }

        public static double Value(double statistics, String distName, Map<String, Double> list) throws Exception {
            return switch (distName) {
                case "Chi-squared" -> 1 - NonCentralHead.NonCentChiSquared(statistics, list.get("df"), list.get("lambda"));
                case "T" -> 1 - NonCentralHead.NonCentT(statistics, list.get("df"), list.get("lambda"));
                case "F" -> 1 - NonCentralHead.NonCentF(statistics, list.get("df1"), list.get("df2"), list.get("lambda"));
                default -> throw new NoSuchMethodException("Distribution not found.");
            };
        }
    }
}
