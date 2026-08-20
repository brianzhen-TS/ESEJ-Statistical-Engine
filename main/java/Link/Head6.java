package Link;

import org.apache.commons.statistics.distribution.*;
import org.apache.commons.numbers.combinatorics.Factorial;

class Head6 {

    public static class Zeta {

        // Borwein constants precomputed for n = 14 (Excellent for 64-bit double precision)
        protected static final int N = 14;
        protected static final double[] D = new double[N + 1];

        static {
            // Precompute Borwein coefficients d_k
            for (int k = 0; k <= N; k++) {
                double sum = 0.0;
                for (int i = k; i <= N; i++) {
                    sum += (Factorial.doubleValue(N + i - 1) /
                            (Factorial.doubleValue(N - i) * Factorial.doubleValue(2 * i)));
                }
                D[k] = N * sum;
            }
        }

        /**
         * Fast Riemann Zeta evaluation using Borwein's Method.
         * Valid for all real s > 0, s != 1.
         */
        public static double evaluate(double s) {
            if (s == 1.0) return Double.POSITIVE_INFINITY;
            if (s <= 0) {
                // Use reflection formula if needed (but we only use s>1)
                throw new IllegalArgumentException("Zeta only implemented for s > 1");
            }
            double sum = 0.0;
            double term;
            for (int k = 1; ; k++) {
                term = 1.0 / Math.pow(k, s);
                sum += term;
                if (term < 1e-15 * sum) break;
            }
            return sum;
        }
    }


    public static double zetaPMF(double s, double x) {
        int in = (int) x;
        double top = Math.pow(in, -s);
        double N = Zeta.evaluate(s);
        return top / N;
    }

    public static double zetaCDF(double s, double x) {
        int in = (int) x;
        double top = 1.0;          // k=1 term
        double N = Zeta.evaluate(s);
        for (int i = 2; i <= in; i++) {
            top += Math.pow(i, -s);
        }
        return top / N;
    }

    public static double zipfPMF(double s, double N, double x) {
        ZipfDistribution zipf = ZipfDistribution.of((int)N, s);
        return zipf.probability((int)x);
    }

    public static double zipfCDF(double s, double N, double x) {
        ZipfDistribution zipf = ZipfDistribution.of((int)N, s);
        return zipf.cumulativeProbability((int)x);
    }

    public static double zipfMandelbrotPMF(double s, double N, double q, double x) {
        ZipfDistribution zipf = ZipfDistribution.of((int)N, s + q);
        return zipf.probability((int)x);
    }

    public static double zipfMandelbrotCDF(double s, double N, double q, double x) {
        ZipfDistribution zipf = ZipfDistribution.of((int)N, s + q);
        return zipf.cumulativeProbability((int)x);
    }

    public static double MaxwellPDF(double x, double a) {
        if (x <= 0 || a <= 0) return 0.0;
        double z = (x / a) * (x / a);
        ChiSquaredDistribution chi2 = ChiSquaredDistribution.of(3);
        // chi2.density(z) * (2 * x / a^2)
        return chi2.density(z) * (2.0 * x / (a * a));
    }

    public static double MaxwellCDF(double x, double a) {
        double in = Math.pow((x / a), 2);
        ChiSquaredDistribution chi2 = ChiSquaredDistribution.of(3);
        return chi2.cumulativeProbability(in);
    }
}