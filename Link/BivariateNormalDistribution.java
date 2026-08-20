package Link;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

class BivariateNormalDistribution implements MultivariateDistribution {
    @Override
    public String getName() {
        return "Bivariate Normal";
    }

    @Override
    public List<Parameter> getParameters() {
        return Arrays.asList(
                new Parameter("meanX", Double.class, "Mean of X"),
                new Parameter("meanY", Double.class, "Mean of Y"),
                new Parameter("varX", Double.class, "Variance of X"),
                new Parameter("varY", Double.class, "Variance of Y"),
                new Parameter("cov", Double.class, "Covariance")
        );
    }

    @Override
    public double pdf(double x, Map<String, Double> params) {
        // Not used for multivariate; we use density(double[])
        throw new UnsupportedOperationException("Use density(double[]) for multivariate");
    }

    @Override
    public double cdf(double x, Map<String, Double> params) {
        throw new UnsupportedOperationException("CDF not implemented for multivariate");
    }

    @Override
    public double density(double[] x, Map<String, Double> params) {
        try {
            double muX = params.get("meanX");
            double muY = params.get("meanY");
            double varX = params.get("varX");
            double varY = params.get("varY");
            double cov = params.get("cov");
            double det = varX * varY - cov * cov;  // for a 2x2 matrix
            if (det <= 0) return 0.0; // invalid covariance matrix
            double dx = x[0] - muX;
            double dy = x[1] - muY;
            double exponent = -0.5 * ((varY * dx * dx - 2 * cov * dx * dy + varX * dy * dy) / det);
            return Math.exp(exponent) / (2 * Math.PI * Math.sqrt(det));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}