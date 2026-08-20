package Link;

import org.apache.commons.numbers.gamma.Beta;

class Head5 {

    // ------ Beta Prime (also called Beta distribution of the second kind) ------
    public static double BetaPrimePDF(double x, double a, double b) {
        if (x < 0) return 0.0;
        double N = Beta.value(a, b);
        return Math.pow(x, a - 1) * Math.pow(1 + x, -(a + b)) / N;
    }

    public static double BetaPrimeCDF(double x, double a, double b) {
        if (x < 0) return 0.0;
        // Use numerical integration (or could use incomplete beta if available)
        return Integral.adaptiveIntegral(0, x, z -> BetaPrimePDF(z, a, b));
    }

    // ------ Generalized Beta of the first kind ------
    public static double GeneralizedBetaPDF(double x, double a, double b, double p, double q) {
        if (x < 0 || x > 1) return 0.0;
        double N = Beta.value(a, b);
        return Math.pow(x, a * p - 1) * Math.pow(1 - x, b * q - 1) / N;
    }

    public static double GeneralizedBetaCDF(double x, double a, double b, double p, double q) {
        if (x < 0) return 0.0;
        if (x >= 1) return 1.0;
        return Integral.adaptiveIntegral(0, x, z -> GeneralizedBetaPDF(z, a, b, p, q));
    }

    // ------ Generalized Beta of the second kind (Generalized Beta prime) ------
    public static double GeneralizedBetaPrimePDF(double x, double a, double b, double p, double q) {
        if (x < 0) return 0.0;
        double N = Beta.value(a, b);
        return Math.pow(x, a * p - 1) * Math.pow(1 + x, -(a * p + b * q)) / N;
    }

    public static double GeneralizedBetaPrimeCDF(double x, double a, double b, double p, double q) {
        if (x < 0) return 0.0;
        return Integral.adaptiveIntegral(0, x, z -> GeneralizedBetaPrimePDF(z, a, b, p, q));
    }
    
    // terminal for debugging
    static void main() {
        double x = 0.3;
        double a = 2.1;
        double b = 2.6;
        double prop = BetaPrimeCDF(x, a, b);
        System.out.println("Probability = " + prop);
    }
}