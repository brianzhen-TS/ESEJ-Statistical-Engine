package Link;

class Integral {
    private static final double DEFAULT_EPS = 1e-12;
    private static final int MAX_DEPTH = 20;

    // --------------------------------------------------------------------
    // Gauss-Legendre quadrature (16-point) – accurate and fast
    // Nodes and weights precomputed for interval [-1,1], mapped to [a,b]
    // --------------------------------------------------------------------
    private static final double[] GL16_X = {
            -0.9894009349916499, -0.9445750230732326, -0.8656312023878318, -0.7554044083550030,
            -0.6178762444026438, -0.4580167776572274, -0.2816035507792589, -0.0950125098376374,
            0.0950125098376374,  0.2816035507792589,  0.4580167776572274,  0.6178762444026438,
            0.7554044083550030,  0.8656312023878318,  0.9445750230732326,  0.9894009349916499
    };

    private static final double[] GL16_W = {
            0.0271524594117541,  0.0622535239386479,  0.0951585116824928,  0.1246289712555339,
            0.1495959888165767,  0.1691565193950025,  0.1826034150449236,  0.1894506104550685,
            0.1894506104550685,  0.1826034150449236,  0.1691565193950025,  0.1495959888165767,
            0.1246289712555339,  0.0951585116824928,  0.0622535239386479,  0.0271524594117541
    };

    // This method sometimes could give us a really unstable results(you can open the plotter to see how)
    @Deprecated
    public static double gaussLegendreIntegral(double a, double b, java.util.function.DoubleUnaryOperator f) {
        double m = (b - a) / 2.0;
        double c = (b + a) / 2.0;
        double sum = 0.0;
        for (int i = 0; i < GL16_X.length; i++) {
            double x = c + m * GL16_X[i];
            sum += GL16_W[i] * f.applyAsDouble(x);
        }
        return m * sum;
    }

    public static double adaptiveIntegral(double a, double b, java.util.function.DoubleUnaryOperator f) {
        return AdaptiveIntegrator.adaptiveIntegrate(f, a, b, DEFAULT_EPS, MAX_DEPTH);
    }
}
