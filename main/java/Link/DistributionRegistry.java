package Link;

import java.util.*;

public class DistributionRegistry {
    private static final Map<String, Distribution> dist = new HashMap<>();

    public static void register(Distribution d) {
        dist.put(d.getName().toLowerCase(), d);
    }

    public static Distribution get(String name) {
        return dist.get(name.toLowerCase());
    }

    public static Collection<Link.Distribution> getAll() { return dist.values(); }

    static {
        // ----- Family 1 / 2 / 7 / 9 (continuous) -----
        register(new NormalDist());
        register(new TDist());
        register(new FDist());
        register(new ChiSquaredDist());
        register(new LogNormalDist());
        register(new ExponentialDist());
        register(new FisherZDist());
        register(new ErlangDist());
        register(new GammaDist());
        register(new CauchyDist());
        register(new LaplaceDist());
        register(new ParetoDist());
        register(new LogisticDist());

        // ----- Family 3 (discrete) -----
        register(new BinomialDist());
        register(new PoissonDist());
        register(new BernoulliDist());
        register(new HypergeometricDist());
        register(new GeometricDist());

        // ----- Family 4 (discrete – hypergeometric‑like) -----
        register(new NegativeHypergeometricDist());
        register(new NegativeBinomialDist());
        register(new BetaBinomialDist());
        register(new NegativeBetaBinomialDist());
        register(new BetaGeometricDist());

        // ----- Family 5 (Beta and related) -----
        register(new BetaDist());
        register(new BetaPrimeDist());
        register(new GeneralizedBetaDist());
        register(new GeneralizedBetaPrimeDist());
        register(new BetaRatioDist());        // Beta ratio X/Y from Head5

        // ----- Family 6 (Zeta, Zipf, Maxwell) -----
        register(new ZetaDist());
        register(new ZipfDist());
        register(new ZipfMandelbrotDist());
        register(new MaxwellDist());

        // ----- Family 8 (Beta combinations) -----
        register(new BetaXPlusYDist());
        register(new BetaXTimesYDist());
        register(new BetaRatioXOverYDist());
        register(new BetaMirrorDist());
    }

    // -----------------------------------------------------------------
    // Distribution implementations that delegate to your existing Heads
    // -----------------------------------------------------------------

    // Normal (from Head2)
    private static class NormalDist implements Distribution {
        public String getName() { return "Normal"; }
        public List<Parameter> getParameters() {
            return Arrays.asList(
                    new Parameter("mean", Double.class, "Mean (μ)"),
                    new Parameter("sd", Double.class, "Standard deviation (σ)")
            );
        }
        public double pdf(double x, Map<String,Double> p) {
            return Head2.NormalPDF(x, p.get("mean"), p.get("sd"));
        }
        public double cdf(double x, Map<String,Double> p) {
            return Head2.NormalCDF(x, p.get("mean"), p.get("sd"));
        }
    }

    // T (from Head7)
    private static class TDist implements Distribution {
        public String getName() { return "T"; }
        public List<Parameter> getParameters() {
            return List.of(new Parameter("df", Double.class, "Degrees of freedom"));
        }
        public double pdf(double x, Map<String,Double> p) {
            return Head7.TPDF(x, p.get("df"));
        }
        public double cdf(double x, Map<String,Double> p) {
            return Head7.TCDF(x, p.get("df"));
        }
    }

    // F (from Head7)
    private static class FDist implements Distribution {
        public String getName() { return "F"; }
        public List<Parameter> getParameters() {
            return Arrays.asList(
                    new Parameter("df1", Double.class, "Numerator df"),
                    new Parameter("df2", Double.class, "Denominator df")
            );
        }
        public double pdf(double x, Map<String,Double> p) {
            return Head7.FPDF(x, p.get("df1").intValue(), p.get("df2").intValue());
        }
        public double cdf(double x, Map<String,Double> p) {
            return Head7.FCDF(x, p.get("df1").intValue(), p.get("df2").intValue());
        }
    }

    // Chi‑squared (from Head7)
    private static class ChiSquaredDist implements Distribution {
        public String getName() { return "Chi-squared"; }
        public List<Parameter> getParameters() {
            return List.of(new Parameter("df", Double.class, "Degrees of freedom"));
        }
        public double pdf(double x, Map<String,Double> p) {
            return Head7.chiSquarePDF(x, p.get("df").intValue());
        }
        public double cdf(double x, Map<String,Double> p) {
            return Head7.chiSquareCDF(x, p.get("df").intValue());
        }
    }

    // Log‑normal (from Head2)
    private static class LogNormalDist implements Distribution {
        public String getName() { return "Log-normal"; }
        public List<Parameter> getParameters() {
            return Arrays.asList(
                    new Parameter("meanlog", Double.class, "Mean on log scale"),
                    new Parameter("sdlog", Double.class, "SD on log scale")
            );
        }
        public double pdf(double x, Map<String,Double> p) {
            if (x == 0) return 0.0;
            return Head2.LogNormalPDF(x, p.get("meanlog"), p.get("sdlog"));
        }
        public double cdf(double x, Map<String,Double> p) {
            if (x == 0) return 0.0;
            return Head2.LogNormalCDF(x, p.get("meanlog"), p.get("sdlog"));
        }
    }

    // Exponential (from Head2)
    private static class ExponentialDist implements Distribution {
        public String getName() { return "Exponential"; }
        public List<Parameter> getParameters() {
            return List.of(new Parameter("rate", Double.class, "Rate (λ)"));
        }
        public double pdf(double x, Map<String,Double> p) {
            return Head2.ExponentialPDF(x, p.get("rate"));
        }
        public double cdf(double x, Map<String,Double> p) {
            return Head2.ExponentialCDF(x, p.get("rate"));
        }
    }

    // Fisher Z (from Head7)
    private static class FisherZDist implements Distribution {
        public String getName() { return "Fisher Z"; }
        public List<Parameter> getParameters() {
            return Arrays.asList(
                    new Parameter("df1", Double.class, "Degrees of freedom 1"),
                    new Parameter("df2", Double.class, "Degrees of freedom 2")
            );
        }
        public double pdf(double x, Map<String,Double> p) {
            return Head7.FisherZPDF(x, p.get("df1").intValue(), p.get("df2").intValue());
        }
        public double cdf(double x, Map<String,Double> p) {
            return Head7.FisherZCDF(x, p.get("df1").intValue(), p.get("df2").intValue());
        }
    }

    // Erlang (from Head7)
    private static class ErlangDist implements Distribution {
        public String getName() { return "Erlang"; }
        public List<Parameter> getParameters() {
            return Arrays.asList(
                    new Parameter("shape", Double.class, "Shape (k)"),
                    new Parameter("rate", Double.class, "Rate (λ)")
            );
        }
        public double pdf(double x, Map<String,Double> p) {
            return Head7.ErlangPDF(x, p.get("shape").intValue(), p.get("rate"));
        }
        public double cdf(double x, Map<String,Double> p) {
            return Head7.ErlangCDF(x, p.get("shape").intValue(), p.get("rate"));
        }
    }

    // ----- Discrete distributions (Family 3) -----

    private static class BinomialDist implements Distribution {
        public String getName() { return "Binomial"; }
        public List<Parameter> getParameters() {
            return Arrays.asList(
                    new Parameter("n", Double.class, "Number of trials"),
                    new Parameter("p", Double.class, "Success probability")
            );
        }
        public double pdf(double x, Map<String,Double> p) {
            return Head3.BinomialPDF((int)x, p.get("n").intValue(), p.get("p"));
        }
        public double cdf(double x, Map<String,Double> p) {
            return Head3.BinomialCDF((int)x, p.get("n").intValue(), p.get("p"));
        }
    }

    private static class PoissonDist implements Distribution {
        public String getName() { return "Poisson"; }
        public List<Parameter> getParameters() {
            return List.of(new Parameter("lambda", Double.class, "Rate (λ)"));
        }
        public double pdf(double x, Map<String,Double> p) {
            return Head3.PoissonPDF((int)x, p.get("lambda"));
        }
        public double cdf(double x, Map<String,Double> p) {
            return Head3.PoissonCDF(x, p.get("lambda"));
        }
    }

    private static class BernoulliDist implements Distribution {
        public String getName() { return "Bernoulli"; }
        public List<Parameter> getParameters() {
            return List.of(new Parameter("p", Double.class, "Success probability"));
        }
        public double pdf(double x, Map<String,Double> p) {
            return Head3.BernoulliPDF(p.get("p"), x);
        }
        public double cdf(double x, Map<String,Double> p) {
            return Head3.BernoulliCDF(p.get("p"), x);
        }
    }

    private static class HypergeometricDist implements Distribution {
        public String getName() { return "Hypergeometric"; }
        public List<Parameter> getParameters() {
            return Arrays.asList(
                    new Parameter("N", Double.class, "Population size"),
                    new Parameter("K", Double.class, "Successes in population"),
                    new Parameter("n", Double.class, "Number of draws")
            );
        }
        public double pdf(double x, Map<String,Double> p) {
            return Head3.HypergeometricPDF(x, p.get("n"), p.get("K"), p.get("N"));
        }
        public double cdf(double x, Map<String,Double> p) {
            return Head3.HypergeometricCDF(x, p.get("n"), p.get("K"), p.get("N"));
        }
    }

    private static class GeometricDist implements Distribution {
        public String getName() { return "Geometric"; }
        public List<Parameter> getParameters() {
            return List.of(new Parameter("p", Double.class, "Success probability"));
        }
        public double pdf(double x, Map<String,Double> p) {
            return Head3.GeometricPDF((int)x - 1, p.get("p"));
        }
        public double cdf(double x, Map<String,Double> p) {
            return Head3.GeometricCDF((int)x - 1, p.get("p"));
        }
    }

    // ----- Family 4 distributions (wrapping Head4) -----

    private static class NegativeHypergeometricDist implements Distribution {
        public String getName() { return "Negative Hypergeometric"; }
        public List<Parameter> getParameters() {
            return Arrays.asList(
                    new Parameter("N", Double.class, "Population size"),
                    new Parameter("K", Double.class, "Successes in population"),
                    new Parameter("r", Double.class, "Failures to stop")
            );
        }
        public double pdf(double x, Map<String,Double> p) {
            return Head4.NHGDPMF(x, p.get("r"), p.get("N"), p.get("K"));
        }
        public double cdf(double x, Map<String,Double> p) {
            return Head4.NHGDCDF(x, p.get("r"), p.get("N"), p.get("K"));
        }
    }

    private static class NegativeBinomialDist implements Distribution {
        public String getName() { return "Negative Binomial"; }
        public List<Parameter> getParameters() {
            return Arrays.asList(
                    new Parameter("r", Double.class, "Number of failures until stop"),
                    new Parameter("p", Double.class, "Success probability")
            );
        }
        public double pdf(double x, Map<String,Double> p) {
            return Head4.NBPMF(x, p.get("r"), p.get("p"));
        }
        public double cdf(double x, Map<String,Double> p) {
            return Head4.NBCDF(x, p.get("r"), p.get("p"));
        }
    }

    private static class BetaBinomialDist implements Distribution {
        public String getName() { return "Beta Binomial"; }
        public List<Parameter> getParameters() {
            return Arrays.asList(
                    new Parameter("n", Double.class, "Number of trials"),
                    new Parameter("alpha", Double.class, "Alpha shape"),
                    new Parameter("beta", Double.class, "Beta shape")
            );
        }
        public double pdf(double x, Map<String,Double> p) {
            return Head4.BetaBinomialPMF(x, p.get("n"), p.get("alpha"), p.get("beta"));
        }
        public double cdf(double x, Map<String,Double> p) {
            return Head4.BetaBinomialCDF(x, p.get("n"), p.get("alpha"), p.get("beta"));
        }
    }

    private static class NegativeBetaBinomialDist implements Distribution {
        public String getName() { return "Negative Beta Binomial"; }
        public List<Parameter> getParameters() {
            return Arrays.asList(
                    new Parameter("r", Double.class, "Number of failures"),
                    new Parameter("alpha", Double.class, "Alpha shape"),
                    new Parameter("beta", Double.class, "Beta shape")
            );
        }
        public double pdf(double x, Map<String,Double> p) {
            return Head4.NegativeBetaBinomialPMF(x, p.get("r"), p.get("alpha"), p.get("beta"));
        }
        public double cdf(double x, Map<String,Double> p) {
            return Head4.NegativeBetaBinomialCDF(x, p.get("r"), p.get("alpha"), p.get("beta"));
        }
    }

    private static class BetaGeometricDist implements Distribution {
        public String getName() { return "Beta Geometric"; }
        public List<Parameter> getParameters() {
            return Arrays.asList(
                    new Parameter("alpha", Double.class, "Alpha shape"),
                    new Parameter("beta", Double.class, "Beta shape")
            );
        }
        public double pdf(double x, Map<String,Double> p) {
            return Head4.BetaGeometricPMF(x, p.get("alpha"), p.get("beta"));
        }
        public double cdf(double x, Map<String,Double> p) {
            return Head4.BetaGeometricCDF(x, p.get("alpha"), p.get("beta"));
        }
    }

    // ----- Family 5 (Beta and related, from Head5 and EtaCore.Heads.BetaOperationsHead) -----

    private static class BetaDist implements Distribution {
        public String getName() { return "Beta"; }
        public List<Parameter> getParameters() {
            return Arrays.asList(
                    new Parameter("alpha", Double.class, "Alpha shape"),
                    new Parameter("beta", Double.class, "Beta shape")
            );
        }
        public double pdf(double x, Map<String,Double> p) {
            return BetaOperationsHead.BetaPDF(x, p.get("alpha"), p.get("beta"));
        }
        public double cdf(double x, Map<String,Double> p) {
            return BetaOperationsHead.BetaCDF(x, p.get("alpha"), p.get("beta"));
        }
    }

    private static class BetaPrimeDist implements Distribution {
        public String getName() { return "Beta prime"; }
        public List<Parameter> getParameters() {
            return Arrays.asList(
                    new Parameter("alpha", Double.class, "Alpha shape"),
                    new Parameter("beta", Double.class, "Beta shape")
            );
        }
        public double pdf(double x, Map<String,Double> p) {
            return Head5.BetaPrimePDF(x, p.get("alpha"), p.get("beta"));
        }
        public double cdf(double x, Map<String,Double> p) {
            return Head5.BetaPrimeCDF(x, p.get("alpha"), p.get("beta"));
        }
    }

    private static class GeneralizedBetaDist implements Distribution {
        public String getName() { return "Generalized Beta"; }
        public List<Parameter> getParameters() {
            return Arrays.asList(
                    new Parameter("a", Double.class, "a"),
                    new Parameter("b", Double.class, "b"),
                    new Parameter("p", Double.class, "Shape p"),
                    new Parameter("q", Double.class, "Shape q")
            );
        }
        public double pdf(double x, Map<String,Double> p) {
            return Head5.GeneralizedBetaPDF(x, p.get("a"), p.get("b"), p.get("p"), p.get("q"));
        }
        public double cdf(double x, Map<String,Double> p) {
            return Head5.GeneralizedBetaCDF(x, p.get("a"), p.get("b"), p.get("p"), p.get("q"));
        }
    }

    private static class GeneralizedBetaPrimeDist implements Distribution {
        public String getName() { return "Generalized Beta prime"; }
        public List<Parameter> getParameters() {
            return Arrays.asList(
                    new Parameter("a", Double.class, "a"),
                    new Parameter("b", Double.class, "b"),
                    new Parameter("p", Double.class, "Shape p"),
                    new Parameter("q", Double.class, "Shape q")
            );
        }
        public double pdf(double x, Map<String,Double> p) {
            return Head5.GeneralizedBetaPrimePDF(x, p.get("a"), p.get("b"), p.get("p"), p.get("q"));
        }
        public double cdf(double x, Map<String,Double> p) {
            return Head5.GeneralizedBetaPrimeCDF(x, p.get("a"), p.get("b"), p.get("p"), p.get("q"));
        }
    }

    // Beta ratio X/Y from Head5 (not to be confused with Family 8 ratio)
    private static class BetaRatioDist implements Distribution {
        public String getName() { return "Beta ratio X/Y"; }
        public List<Parameter> getParameters() {
            return Arrays.asList(
                    new Parameter("alpha1", Double.class, "Alpha for X"),
                    new Parameter("beta1", Double.class, "Beta for X"),
                    new Parameter("alpha2", Double.class, "Alpha for Y"),
                    new Parameter("beta2", Double.class, "Beta for Y")
            );
        }
        public double pdf(double x, Map<String,Double> p) {
            return BetaOperationsHead.BetaRatioXOverYPDF(x, p.get("alpha1"), p.get("beta1"), p.get("alpha2"), p.get("beta2"));
        }
        public double cdf(double x, Map<String,Double> p) {
            return BetaOperationsHead.BetaRatioXOverYCDF(x, p.get("alpha1"), p.get("beta1"), p.get("alpha2"), p.get("beta2"));
        }
    }

    // ----- Family 6 (Zeta, Zipf, Maxwell) -----

    private static class ZetaDist implements Distribution {
        public String getName() { return "Zeta"; }
        public List<Parameter> getParameters() {
            return List.of(new Parameter("s", Double.class, "Exponent s"));
        }
        public double pdf(double x, Map<String,Double> p) {
            return Head6.zetaPMF(p.get("s"), x);
        }
        public double cdf(double x, Map<String,Double> p) {
            return Head6.zetaCDF(p.get("s"), x);
        }
    }

    private static class ZipfDist implements Distribution {
        public String getName() { return "Zipf"; }
        public List<Parameter> getParameters() {
            return Arrays.asList(
                    new Parameter("s", Double.class, "Exponent s"),
                    new Parameter("n", Double.class, "Number of elements")
            );
        }
        public double pdf(double x, Map<String,Double> p) {
            return Head6.zipfPMF(p.get("s"), p.get("n"), x);
        }
        public double cdf(double x, Map<String,Double> p) {
            return Head6.zipfCDF(p.get("s"), p.get("n"), x);
        }
    }

    private static class ZipfMandelbrotDist implements Distribution {
        public String getName() { return "Zipf-Mandelbrot"; }
        public List<Parameter> getParameters() {
            return Arrays.asList(
                    new Parameter("s", Double.class, "Exponent s"),
                    new Parameter("n", Double.class, "Number of elements"),
                    new Parameter("q", Double.class, "Shift q")
            );
        }
        public double pdf(double x, Map<String,Double> p) {
            return Head6.zipfMandelbrotPMF(p.get("s"), p.get("n"), p.get("q"), x);
        }
        public double cdf(double x, Map<String,Double> p) {
            return Head6.zipfMandelbrotCDF(p.get("s"), p.get("n"), p.get("q"), x);
        }
    }

    private static class MaxwellDist implements Distribution {
        public String getName() { return "Maxwell"; }
        public List<Parameter> getParameters() {
            return List.of(new Parameter("a", Double.class, "Scale a"));
        }
        public double pdf(double x, Map<String,Double> p) {
            return Head6.MaxwellPDF(x, p.get("a"));
        }
        public double cdf(double x, Map<String,Double> p) {
            return Head6.MaxwellCDF(x, p.get("a"));
        }
    }

    // ----- Family 8 (Beta combinations, from EtaCore.Heads.BetaOperationsHead) -----

    private static class BetaXPlusYDist implements Distribution {
        public String getName() { return "Beta X+Y"; }
        public List<Parameter> getParameters() {
            return Arrays.asList(
                    new Parameter("alpha1", Double.class, "Alpha for X"),
                    new Parameter("beta1", Double.class, "Beta for X"),
                    new Parameter("alpha2", Double.class, "Alpha for Y"),
                    new Parameter("beta2", Double.class, "Beta for Y")
            );
        }
        public double pdf(double x, Map<String,Double> p) {
            return BetaOperationsHead.BetaXPlusYPDF(x, p.get("alpha1"), p.get("beta1"), p.get("alpha2"), p.get("beta2"));
        }
        public double cdf(double x, Map<String,Double> p) {
            return BetaOperationsHead.BetaXPlusYCDF(x, p.get("alpha1"), p.get("beta1"), p.get("alpha2"), p.get("beta2"));
        }
    }

    private static class BetaXTimesYDist implements Distribution {
        public String getName() { return "Beta X*Y"; }
        public List<Parameter> getParameters() {
            return Arrays.asList(
                    new Parameter("alpha1", Double.class, "Alpha for X"),
                    new Parameter("beta1", Double.class, "Beta for X"),
                    new Parameter("alpha2", Double.class, "Alpha for Y"),
                    new Parameter("beta2", Double.class, "Beta for Y")
            );
        }
        public double pdf(double x, Map<String,Double> p) {
            return BetaOperationsHead.BetaXTimesYPDF(x, p.get("alpha1"), p.get("beta1"), p.get("alpha2"), p.get("beta2"));
        }
        public double cdf(double x, Map<String,Double> p) {
            return BetaOperationsHead.BetaXTimesYCDF(x, p.get("alpha1"), p.get("beta1"), p.get("alpha2"), p.get("beta2"));
        }
    }

    private static class BetaRatioXOverYDist implements Distribution {
        public String getName() { return "Beta ratio X/Y"; }
        public List<Parameter> getParameters() {
            return Arrays.asList(
                    new Parameter("alpha1", Double.class, "Alpha for X"),
                    new Parameter("beta1", Double.class, "Beta for X"),
                    new Parameter("alpha2", Double.class, "Alpha for Y"),
                    new Parameter("beta2", Double.class, "Beta for Y")
            );
        }
        public double pdf(double x, Map<String,Double> p) {
            return BetaOperationsHead.BetaRatioXOverYPDF(x, p.get("alpha1"), p.get("beta1"), p.get("alpha2"), p.get("beta2"));
        }
        public double cdf(double x, Map<String,Double> p) {
            return BetaOperationsHead.BetaRatioXOverYCDF(x, p.get("alpha1"), p.get("beta1"), p.get("alpha2"), p.get("beta2"));
        }
    }

    private static class BetaMirrorDist implements Distribution {
        public String getName() { return "Beta mirror"; }
        public List<Parameter> getParameters() {
            return Arrays.asList(
                    new Parameter("alpha", Double.class, "Alpha shape (swapped)"),
                    new Parameter("beta", Double.class, "Beta shape (swapped)")
            );
        }
        public double pdf(double x, Map<String,Double> p) {
            return BetaOperationsHead.BetaMirrorPDF(x, p.get("alpha"), p.get("beta"));
        }
        public double cdf(double x, Map<String,Double> p) {
            return BetaOperationsHead.BetaMirrorCDF(x, p.get("alpha"), p.get("beta"));
        }
    }

    private static class GammaDist implements Distribution {
        public String getName() { return "Gamma"; }
        public List<Parameter> getParameters() {
            return Arrays.asList(
                    new Parameter("alpha", Double.class, "Alpha shape"),
                    new Parameter("beta", Double.class, "Rate")
            );
        }
        public double pdf(double x, Map<String,Double> p) {
            return Head9.gammaPDF(x, p.get("alpha"), p.get("beta"));
        }
        public double cdf(double x, Map<String,Double> p) {
            return Head9.gammaCDF(x, p.get("alpha"), p.get("beta"));
        }
    }

    private static class CauchyDist implements Distribution {
        public String getName() { return "Cauchy"; }
        public List<Parameter> getParameters() {
            return Arrays.asList(
                    new Parameter("mu", Double.class, "Location parameter"),
                    new Parameter("gamma", Double.class, "Scale parameter")
            );
        }
        public double pdf(double x, Map<String,Double> p) {
            return Head9.cauchyPDF(x, p.get("mu"), p.get("gamma"));
        }
        public double cdf(double x, Map<String,Double> p) {
            return Head9.cauchyCDF(x, p.get("mu"), p.get("gamma"));
        }
    }

    private static class LaplaceDist implements Distribution {
        // The Fourier transform of the Cauchy distribution
        public String getName() { return "Laplace"; }
        public List<Parameter> getParameters() {
            return Arrays.asList(
                    new Parameter("mu", Double.class, "Location parameter"),
                    new Parameter("gamma", Double.class, "Scale parameter")
            );
        }
        public double pdf(double x, Map<String,Double> p) {
            return Head9.LaplacePDF(x, p.get("mu"), p.get("gamma"));
        }
        public double cdf(double x, Map<String,Double> p) {
            return Head9.LaplaceCDF(x, p.get("mu"), p.get("gamma"));
        }
    }

    private static class ParetoDist implements Distribution {
        public String getName() { return "Pareto"; }
        public List<Parameter> getParameters() {
            return Arrays.asList(
                    new Parameter("Scale", Double.class, "Scale parameter"),
                    new Parameter("Shape", Double.class, "Shape parameter")
            );
        }
        public double pdf(double x, Map<String,Double> p) {
            return Head9.ParetoPDF(x, p.get("Scale"), p.get("Shape"));
        }
        public double cdf(double x, Map<String,Double> p) {
            return Head9.ParetoCDF(x, p.get("Scale"), p.get("Shape"));
        }
    }

    private static class LogisticDist implements Distribution {
        public String getName() { return "Logistic"; }
        public List<Parameter> getParameters() {
            return Arrays.asList(
                    new Parameter("mu", Double.class, "Mean"),
                    new Parameter("sigma", Double.class, "Scale parameter")
            );
        }
        public double pdf(double x, Map<String,Double> p) {
            return Head9.LogisticPDF(x, p.get("mu"), p.get("sigma"));
        }
        public double cdf(double x, Map<String,Double> p) {
            return Head9.LogisticCDF(x, p.get("mu"), p.get("sigma"));
        }
    }
}