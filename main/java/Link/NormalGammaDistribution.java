package Link;

import org.apache.commons.numbers.gamma.Gamma;

import java.util.List;
import java.util.Map;

public class NormalGammaDistribution implements MultivariateDistribution {
    @Override
    public double density(double[] x, Map<String, Double> params) {
        try {
            double mu = params.get("mu");
            double lambda = params.get("lambda");
            double alpha = params.get("alpha");
            double beta = params.get("beta");

            // Normal
            double dx = x[0] - mu;
            double y = x[1];
            double Exp = Math.exp(-(lambda * y * Math.pow(dx, 2)) / 2);
            double NNormal =  1 / Math.sqrt(lambda / (2 * Math.PI));
            double Nor = Exp / NNormal;

            // Gamma
            double terms = Math.pow(y, alpha - 1) * Math.exp(-beta * y);
            double NGamma = Gamma.value(alpha) / Math.pow(beta, alpha);
            double Gamma = terms / NGamma;

            return Nor * Gamma;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String getName() {
        return "Normal-Gamma";
    }

    @Override
    public List<Parameter> getParameters() {
        return List.of(
                new Parameter("mu", Double.class, "mean"),
                new Parameter("lambda", Double.class, "scale"),
                new Parameter("alpha", Double.class, "alpha shape"),
                new Parameter("beta", Double.class, "beta shape")
        );
    }

    @Override
    public double pdf(double x, Map<String, Double> params) {
        throw new UnsupportedOperationException("Use density(double[]) for multivariate");
    }

    @Override
    public double cdf(double x, Map<String, Double> params) {
        throw new UnsupportedOperationException("CDF not implemented for multivariate");
    }
}
