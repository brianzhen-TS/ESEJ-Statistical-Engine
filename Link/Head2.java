package Link;

import org.apache.commons.statistics.distribution.*;
import org.apache.commons.numbers.gamma.Erf;

class Head2 {
    // Normal distribution
    public static double NormalPDF(double x, double mu, double s) {
        double exp = Math.exp(-Math.pow((x - mu) / s, 2));
        double N = Math.sqrt(2 * Math.PI) * s;
        return exp / N;
    }

    public static double NormalCDF(double x, double mu, double s) {
        return (1 + Erf.value((x - mu) / s)) / 2;
    }

    public static double LogNormalPDF(double x, double mu, double s) {
        LogNormalDistribution logN = LogNormalDistribution.of(mu, s);
        return logN.density(x);
    }

    public static double LogNormalCDF(double x, double mu, double s) {
        LogNormalDistribution logN = LogNormalDistribution.of(mu, s);
        return logN.cumulativeProbability(x);
    }

    public static double ExponentialPDF(double x, double rate) {
        ExponentialDistribution exponential = ExponentialDistribution.of(rate);
        return exponential.density(x);
    }

    public static double ExponentialCDF(double x, double rate) {
        ExponentialDistribution exponential = ExponentialDistribution.of(rate);
        return exponential.cumulativeProbability(x);
    }
}