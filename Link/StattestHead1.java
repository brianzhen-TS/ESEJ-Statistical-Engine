package Link;

import org.apache.commons.numbers.gamma.Erf;
import org.apache.commons.statistics.distribution.*;

import java.lang.IO;

public class StattestHead1 {
    public static double NormalCDF(double x, double mu, double s) {
        return (1 + Erf.value((x - mu) / s)) / 2;
    }

    public static double gammaCDF(double x, double a, double b) {
        GammaDistribution gamma = GammaDistribution.of(a, b);
        return gamma.cumulativeProbability(x);
    }

    public static double chiSquaredCDF(double x, int df) {
        if (!(df >= 0)) throw new IllegalArgumentException("df cannot be lesser than 0.");
        if (!(x > 0)) return 0;
        return gammaCDF(x, (double) df / 2, (double) 1 / 2); // gamma(x ; df / 2, 1 / 2) = chi_2(x; df)
    }

    public static double FCDF(double x, int df1, int df2) {
        FDistribution f = FDistribution.of(df1, df2);
        return f.cumulativeProbability(x);
    }

    public static double TCDF(double x, int df) {
        TDistribution t = TDistribution.of(df);
        return t.cumulativeProbability(x);
    }

    // terminal for debugging
    void main() {
        double x = 1;
        double m = -3;
        double s = 1;
        double p = NormalCDF(x, m, s);
        IO.println("Probability = " + p);
    }
}