package Link;

import org.apache.commons.statistics.distribution.*;

class Head9 {
    public static double gammaPDF(double x, double a, double b) {
        if (x == 0.0) x = 1e-20;
        GammaDistribution gamma = GammaDistribution.of(a, b);
        return gamma.density(x);
    }

    public static double gammaCDF(double x, double a, double b) {
        if (x == 0.0) x = 1e-20;
        GammaDistribution gamma = GammaDistribution.of(a, b);
        return gamma.cumulativeProbability(x);
    }

    public static double cauchyPDF(double x, double mu, double gamma) {
        CauchyDistribution cauchy = CauchyDistribution.of(mu, gamma);
        return cauchy.density(x);
    }

    public static double cauchyCDF(double x, double mu, double gamma) {
        CauchyDistribution cauchy = CauchyDistribution.of(mu, gamma);
        return cauchy.cumulativeProbability(x);
    }

    public static double LaplacePDF(double x, double mu, double gamma) {
        LaplaceDistribution laplace = LaplaceDistribution.of(mu, gamma);
        return laplace.density(x);
    }

    public static double LaplaceCDF(double x, double mu, double gamma) {
        LaplaceDistribution laplace = LaplaceDistribution.of(mu, gamma);
        return laplace.cumulativeProbability(x);
    }

    public static double ParetoPDF(double x, double scale, double shape) {
        if (x == 0.0) x = 1e-20;
        ParetoDistribution pareto = ParetoDistribution.of(scale, shape);
        return pareto.density(x);
    }

    public static double ParetoCDF(double x, double scale, double shape) {
        if (x == 0.0) x = 1e-20;
        ParetoDistribution pareto = ParetoDistribution.of(scale, shape);
        return pareto.cumulativeProbability(x);
    }

    public static double LogisticPDF(double x, double scale, double shape) {
        if (x == 0.0) x = 1e-20;
        LogisticDistribution logistic = LogisticDistribution.of(scale, shape);
        return logistic.density(x);
    }

    public static double LogisticCDF(double x, double scale, double shape) {
        if (x == 0.0) x = 1e-20;
        LogisticDistribution logistic = LogisticDistribution.of(scale, shape);
        return logistic.cumulativeProbability(x);
    }
}
