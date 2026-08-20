package Link;

import org.apache.commons.statistics.distribution.*;

class Head3 {
    public static double BinomialPDF(int x, int n, double p) {
        BinomialDistribution binomial = BinomialDistribution.of(n, p);
        return binomial.probability(x);
    }

    public static double BinomialCDF(int x, int n, double p) {
        BinomialDistribution binomial = BinomialDistribution.of(n, p);
        return binomial.cumulativeProbability(x);
    }

    public static double PoissonPDF(int x, double rate) {
        PoissonDistribution poisson = PoissonDistribution.of(rate);
        return poisson.probability(x);
    }

    public static double PoissonCDF(double x, double rate) {
        PoissonDistribution poisson = PoissonDistribution.of(rate);
        return poisson.cumulativeProbability((int)x);
    }

    public static double BernoulliPDF(double p, double n) {
        if (p < 0 || p > 1) throw new IllegalArgumentException("p must be in [0, 1]");
        double q = 1 - p;

        double prop;
        if (n < 0 || n > 1) throw new IllegalArgumentException("n must be in [0, 1]");
        if (n == 0) prop = q;
        else prop = p;
        return prop;
    }

    public static double BernoulliCDF(double p, double n) {
        if (p < 0 || p > 1) throw new IllegalArgumentException("p must be in [0, 1]");
        double q = 1 - p;

        double prop;
        if (n < 0) {
            prop = 0;
        } else if (n < 1) {
            prop = q;
        } else {
            prop = 1;
        }
        return prop;
    }

    public static double HypergeometricPDF(double x, double n, double K, double N) {
        HypergeometricDistribution hyper = HypergeometricDistribution.of((int)N, (int)K, (int)n);
        return hyper.probability((int)x);
    }

    public static double HypergeometricCDF(double x, double n, double K, double N) {
        HypergeometricDistribution hyper = HypergeometricDistribution.of((int)N, (int)K, (int)n);
        return hyper.cumulativeProbability((int)x);
    }

    public static double GeometricPDF(int x, double p) {
        GeometricDistribution geometric = GeometricDistribution.of(p);
        return geometric.probability(x);
    }

    public static double GeometricCDF(int x, double p) {
        GeometricDistribution geometric = GeometricDistribution.of(p);
        return geometric.cumulativeProbability(x);
    }

    // terminal for debugging
    static void main() {
        double p = 0.33;
        double prop = BernoulliPDF(p, 1);
        System.out.printf("Probability = %.9f%n", prop);
    }
}