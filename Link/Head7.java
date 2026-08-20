package Link;

import org.apache.commons.numbers.gamma.Beta;
import org.apache.commons.statistics.distribution.*;

class Head7 {
    public static double TPDF(double x, double df) {
        TDistribution t = TDistribution.of(df);
        return t.density(x);
    }

    public static double TCDF(double x, double df) {
        TDistribution t = TDistribution.of(df);
        return t.cumulativeProbability(x);
    }

    public static double FPDF(double x, int df1, int df2) {
        if (x == 0.0) x = 0.00001;
        if (df1 <= 0) df1 = 1;
        if (df2 <= 0) df2 = 1;
        FDistribution f = FDistribution.of(df1, df2);
        return f.density(x);
    }

    public static double FCDF(double x, int df1, int df2) {
        if (x == 0.0) x = 0.00001;
        FDistribution f = FDistribution.of(df1, df2);
        return f.cumulativeProbability(x);
    }

    public static double chiSquarePDF(double x, int df) {
        if (x == 0.0) x = 0.00001;
        ChiSquaredDistribution chi2 = ChiSquaredDistribution.of(df);
        return chi2.density(x);
    }

    public static double chiSquareCDF(double x, int df) {
        if (x == 0.0) x = 0.00001;
        ChiSquaredDistribution chi2 = (ChiSquaredDistribution.of(df));
        return chi2.cumulativeProbability(x);
    }

    public static double FisherZPDF(double x, int df1, int df2) {
        if (x == 0.0) x = 0.00001;
        if (df1 <= 0 || df2 <= 0) {
            throw new IllegalArgumentException("df1 and df2 must be positive.");
        }
        double a = df1 / 2.0;
        double b = df2 / 2.0;
        double beta = Beta.value(a, b);          // Beta function from Apache Commons

        // Correct constant: 2 * df1^(df1/2) * df2^(df2/2) / B(df1/2, df2/2)
        double constant = 2.0 * Math.pow(df1, a) * Math.pow(df2, b) / beta;

        // pdf = constant * exp(df1 * x) / (df2 + df1 * exp(2x))^((df1+df2)/2)
        double exp2x = Math.exp(2.0 * x);
        double numerator = Math.exp(df1 * x);
        double denominator = Math.pow(df2 + df1 * exp2x, (df1 + df2) / 2.0);
        return constant * numerator / denominator;
    }

    public static double FisherZCDF(double x, int df1, int df2) {
        if (x == 0.0) x = 0.00001;
        // P(Z <= z) = P(X <= exp(2z))  where X ~ F(df1, df2)
        double fValue = Math.exp(2.0 * x);
        return FCDF(fValue, df1, df2);   // FCDF uses Smile's FDistribution
    }

    public static double ErlangPDF(double x, int a, double b) {
        if (x == 0.0) x = 0.00001;
        GammaDistribution gamma = GammaDistribution.of(a, 1 / b);
        return gamma.density(x);
    }

    public static double ErlangCDF(double x, int a, double b) {
        if (x == 0.0) x = 0.00001;
        GammaDistribution gamma = GammaDistribution.of(a, 1 / b);
        return gamma.cumulativeProbability(x);
    }

    // terminal for debugging
    static void main() {
        double x = 5;
        int df1 = 6;
        int df2 = 3;
        double p = FCDF(x, df1, df2);
        IO.println(String.format("Probability = %.10f%n" , p));
    }
}
