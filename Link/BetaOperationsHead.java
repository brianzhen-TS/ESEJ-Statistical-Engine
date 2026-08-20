package Link;

import org.apache.commons.statistics.distribution.*;

class BetaOperationsHead {
    public static double BetaPDF(double x, double a, double b) {
        BetaDistribution beta = BetaDistribution.of(a, b);
        return beta.density(x);
    }

    public static double BetaCDF(double x, double a, double b) {
        BetaDistribution beta = BetaDistribution.of(a, b);
        return beta.cumulativeProbability(x);
    }

    public static double BetaXPlusYPDF(double x, double a1, double b1, double a2, double b2) {
        if (x <= 0 || x >= 2) return 0.0;
        if (a1 <= 0 || b1 <= 0 || a2 <= 0 || b2 <= 0) return 0.0;

        BetaDistribution beta1 = BetaDistribution.of(a1, b1);
        BetaDistribution beta2 = BetaDistribution.of(a2, b2);

        double lower = Math.max(0.0, x - 1.0);
        double upper = Math.min(1.0, x);
        if (lower >= upper) return 0.0;

        return Integral.adaptiveIntegral(lower, upper,t -> {
            double fX = beta1.density(t);
            double fY = beta2.density(x - t);
            return fX * fY;
        });
    }

    public static double BetaXPlusYCDF(double x, double a1, double b1, double a2, double b2) {
        if (x <= 0) return 0.0;
        if (x >= 2) return 1.0;
        return Integral.adaptiveIntegral(0, x, z -> BetaXPlusYPDF(z, a1, b1, a2, b2));
    }

    public static double BetaXTimesYPDF(double x, double a1, double b1, double a2, double b2) {
        if (x <= 0 || x >= 1) return 0.0;
        if (a1 <= 0 || b1 <= 0 || a2 <= 0 || b2 <= 0) return 0.0;
        if (a1 == 1.0 && b1 == 1.0 && a2 == 1.0 && b2 == 1.0) {
            return -Math.log(x);
        }

        BetaDistribution beta1 = BetaDistribution.of(a1, b1);
        BetaDistribution beta2 = BetaDistribution.of(a2, b2);

        return Integral.adaptiveIntegral(0, x, t -> {
            double fX = beta1.density(t);
            double zOverT = x / t;
            if (zOverT > 1.0) return 0.0;
            double fY = beta2.density(zOverT);
            return fX * fY / t;
        });
    }

    public static double BetaXTimesYCDF(double x, double a1, double b1, double a2, double b2) {
        if (x < 0) return 0; // Beta is defined for x >= 0
        return Integral.adaptiveIntegral(0, x, z -> BetaXTimesYPDF(z, a1, b1, a2, b2));
    }

    public static double BetaRatioXOverYPDF(double x, double a1, double b1, double a2, double b2) {
        if (x < 0) return 0.0;
        if (x >= 1) return 1.0;
        if (a1 == 1.0 && b1 == 1.0 && a2 == 1.0 && b2 == 1.0) {
            return x - x * Math.log(x);
        }
        return Integral.adaptiveIntegral(0, x, z -> BetaXTimesYPDF(z, a1, b1, a2, b2));
    }

    public static double BetaRatioXOverYCDF(double x, double a1, double b1, double a2, double b2) {
        if (x < 0) return 0;
        return Integral.adaptiveIntegral(0, x, z -> BetaRatioXOverYPDF(z, a1, b1, a2, b2));
    }

    /**
     * Computes the probability density function (PDF) of the mirror-imaged beta distribution with given parameters.
     * @param x the value at which to evaluate the PDF
     * @param a the first shape parameter (must be positive)
     * @param b the second shape parameter (must be positive)
     * @return the PDF of the beta distribution at x
     */
    public static double BetaMirrorPDF(double x, double a, double b) {
        return BetaPDF(1 - x, b, a);
    }

    /**
     * Computes the cumulative distribution function (CDF) of the mirror-imaged beta distribution with given parameters.
     * @param x the value at which to evaluate the CDF
     * @param a the first shape parameter (must be positive)
     * @param b the second shape parameter (must be positive)
     * @return the CDF of the beta distribution at x
     */
    public static double BetaMirrorCDF(double x, double a, double b) {
        return 1 - BetaCDF(1 - x, a, b);
    }
}
