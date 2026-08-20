package Link;
import java.util.function.DoubleUnaryOperator;

class AdaptiveIntegrator {
    // Precomputed 15-point Gauss-Kronrod nodes and weights
    private static final double[] GK15_X = {
            -0.9914553711208132, -0.9491079123427585, -0.8648644233597691,
            -0.7415311855993945, -0.5860872354676911, -0.4058451513773972,
            -0.2077849550078985,  0.0,
            0.2077849550078985,  0.4058451513773972,  0.5860872354676911,
            0.7415311855993945,  0.8648644233597691,  0.9491079123427585,
            0.9914553711208132
    };
    private static final double[] GK15_W = {
            0.02293532201052922, 0.06309209262997856, 0.1047900103222502,
            0.1406532597155259,  0.1690047266392679,  0.1903505780647854,
            0.2044329400752989,  0.2094821410847278,  0.2044329400752989,
            0.1903505780647854,  0.1690047266392679,  0.1406532597155259,
            0.1047900103222502,  0.06309209262997856, 0.02293532201052922
    };
    private static final double[] GK7_W = { // Gauss-Legendre 7-point (embedded)
            0.1294849661688697, 0.2797053914892767, 0.3818300505051189,
            0.4179591836734694, 0.3818300505051189, 0.2797053914892767,
            0.1294849661688697
    };
    private static final double[] GK7_X = {
            -0.9491079123427585, -0.7415311855993945, -0.4058451513773972,
            0.0,
            0.4058451513773972,  0.7415311855993945,  0.9491079123427585
    };

    public static double adaptiveIntegrate(DoubleUnaryOperator f, double a, double b,
                                           double eps, int maxDepth) {
        return adaptiveSubdivide(f, a, b, eps, maxDepth, 0);
    }

    private static double adaptiveSubdivide(DoubleUnaryOperator f, double a, double b,
                                            double eps, int maxDepth, int depth) {
        double m = (a + b) / 2.0;
        double integral = gaussKronrod(f, a, b);
        double integralLeft = gaussKronrod(f, a, m);
        double integralRight = gaussKronrod(f, m, b);

        // Error estimate from Gauss-Kronrod (difference between 15- and 7-point)
        double err = Math.abs(integral - gauss7(f, a, b));

        if (depth >= maxDepth || err < eps * (1 + Math.abs(integral))) {
            return integral;
        }
        return adaptiveSubdivide(f, a, m, eps/2, maxDepth, depth+1)
                + adaptiveSubdivide(f, m, b, eps/2, maxDepth, depth+1);
    }

    private static double gaussKronrod(DoubleUnaryOperator f, double a, double b) {
        double m = (b - a) / 2.0, c = (b + a) / 2.0;
        double sum = 0.0;
        for (int i = 0; i < GK15_X.length; i++) {
            sum += GK15_W[i] * f.applyAsDouble(c + m * GK15_X[i]);
        }
        return m * sum;
    }

    private static double gauss7(DoubleUnaryOperator f, double a, double b) {
        double m = (b - a) / 2.0, c = (b + a) / 2.0;
        double sum = 0.0;
        for (int i = 0; i < GK7_X.length; i++) {
            sum += GK7_W[i] * f.applyAsDouble(c + m * GK7_X[i]);
        }
        return m * sum;
    }
}
