package Link;

import org.apache.commons.numbers.combinatorics.*;
import org.apache.commons.numbers.gamma.*;
import org.apache.commons.statistics.distribution.PascalDistribution;

class Head4 {
    public static double NHGDPMF(double x, double r, double N, double K) {
        int in = (int) x;
        int GroupN = (int) N;
        int success = (int) K;
        int rate = (int) r;
        double a = BinomialCoefficient.value(in + rate - 1, in);
        double b = BinomialCoefficient.value(GroupN - rate - in, success - in);
        double c = BinomialCoefficient.value(GroupN, success);
        return a * b / c;
    }

    public static double NHGDCDF(double x, double r, double N, double K) {
        if (x < 0) return 0.0;

        // Initial PMF at k = 0 using LogFactorials
        // PMF(0) = [comb(M, r) * comb(N-M, 0)] / comb(N, r) -> simplifiable
        double logP0 = logBinomial((int)K, (int)r) + logBinomial((int)N - (int)K, 0) - logBinomial((int)N, (int)r);
        double pmf = Math.exp(logP0);
        double cdf = pmf;

        // Iterative stepping: PMF(k) -> PMF(k+1)
        for (int i = 0; i < x; i++) {
            // Multiplier derived from simplifying the ratio of consecutive hypergeometric terms
            double numerator = (r + i) * (N - K - i);
            double denominator = (i + 1) * (N - r - i);
            if (denominator <= 0) break;

            pmf *= numerator / denominator;
            cdf += pmf;
        }
        return Math.min(cdf, 1.0);
    }

    // Helper method for stable binomial coefficient calculations
    private static double logBinomial(int n, int k) {
        if (k < 0 || k > n) return Double.NEGATIVE_INFINITY;
        return LogGamma.value(n + 1)
                - LogGamma.value(k + 1)
                - LogGamma.value(n - k + 1);
    }
    
    public static double NBPMF(double x, double r, double p) {
        PascalDistribution pascal = PascalDistribution.of((int)r, p);
        return pascal.probability((int)x);
    }

    public static double NBCDF(double x, double r, double p) {
        PascalDistribution pascal = PascalDistribution.of((int)r, p);
        return pascal.cumulativeProbability((int)x);
    }
    
    public static double BetaBinomialPMF(double x, double n, double a, double b) {
        int in = (int) x;
        int sampleN = (int) n;
        double a1 = BinomialCoefficient.value(sampleN, in);
        double a2 = Beta.value(a, b);
        double a3 = Beta.value(in + a, sampleN - in + b);
        return a1 * a3 / a2;
    }

    public static double BetaBinomialCDF(double x, double n, double alpha, double beta) {
        if (x < 0) return 0.0;
        if (x >= n) return 1.0;

        // Calculate initial PMF at x = 0 using LogGamma for numerical stability
        double logP0 = LogGamma.value(n + 1)
                - LogGamma.value(1)
                - LogGamma.value(n + 1)
                + LogGamma.value(alpha + beta)
                - LogGamma.value(alpha)
                - LogGamma.value(beta)
                + LogGamma.value(beta + n)
                - LogGamma.value(alpha + beta + n);

        double pmf = Math.exp(logP0);
        double cdf = pmf;

        // Iterative stepping: PMF(k) -> PMF(k+1)
        for (int i = 0; i < x; i++) {
            pmf *= (double)(n - i) * (alpha + i) / ((i + 1) * (beta + n - 1 - i));
            cdf += pmf;
        }
        return Math.min(cdf, 1.0);
    }
    
    public static double NegativeBetaBinomialPMF(double x, double r, double a, double b) {
        int in = (int) x;
        int rate = (int) r;
        double a1 = BinomialCoefficient.value(in + rate - 1, in);
        double a2 = Beta.value(a, b);
        double a3 = Beta.value(rate + a, in + b);
        return a1 * a3 / a2;
    }

    public static double NegativeBetaBinomialCDF(double x, double r, double alpha, double beta) {
        if (x < 0) return 0.0;
        double logP0 = LogGamma.value(r + alpha)
                - LogGamma.value(r)
                - LogGamma.value(alpha)
                + LogGamma.value(alpha + beta)
                - LogGamma.value(alpha + beta + r);
        double pmf = Math.exp(logP0);
        double cdf = pmf;

        // Iterative stepping: PMF(k) -> PMF(k+1)
        for (int i = 0; i < x; i++) {
            pmf *= (double)(r + i) * (beta + i) / ((i + 1) * (alpha + beta + r + i));
            cdf += pmf;
        }
        return Math.min(cdf, 1.0);
    }

    public static double BetaGeometricPMF(double x, double a, double b) {
        return NegativeBetaBinomialPMF(x, 1, a, b);
    }

    public static double BetaGeometricCDF(double x, double a, double b) {
        return NegativeBetaBinomialCDF(x, 1, a, b);
    }
    
    // terminal for debugging
    static void main() {
        double x = 1.1;
        double n = 3;
        double a = 1.1;
        double b = 2;
        double p = BetaBinomialPMF(x, n, a, b);
        System.out.printf("Probability = %.10f%n", p);
    }
}