package Link;

import Link.ExceptionStat.SystemException.InputOutOfRangeException;
import org.apache.commons.statistics.distribution.ChiSquaredDistribution;
import org.apache.commons.statistics.distribution.FDistribution;
import org.apache.commons.statistics.distribution.PoissonDistribution;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class NonCentralHead {
    private static final int COUNT = Runtime.getRuntime().availableProcessors();
    private static final double EPSILON = 1e-15;
    private static final double TINY = 1e-30;

    // Fully unpinned Virtual Thread Executor layout
    private static final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    // --- High-Precision Self-Contained Math Primitives ---

    private static double logGamma(double x) {
        if (x <= 0) return Double.NaN;
        double[] coef = {
                76.18009172947146,  -86.50532032941677,
                24.01409824083091,  -1.231739572450155,
                0.1208650973866179e-2, -0.5395239384953e-5
        };
        double y = x;
        double tmp = x + 5.5;
        tmp -= (x + 0.5) * Math.log(tmp);
        double ser = 1.000000000190015;
        for (int j = 0; j <= 5; j++) {
            y++;
            ser += coef[j] / y;
        }
        return -tmp + Math.log(2.5066282746310005 * ser / x);
    }

    private static double logBeta(double a, double b) {
        return logGamma(a) + logGamma(b) - logGamma(a + b);
    }

    private static double baseRegularizedGammaP(double a, double x) {
        if (x <= 0.0 || a <= 0.0) return 0.0;
        if (x < a + 1.0) {
            double sum = 1.0 / a;
            double term = 1.0 / a;
            for (int n = 1; n < 2000; n++) {
                term *= x / (a + n);
                sum += term;
                if (term < sum * EPSILON) break;
            }
            return sum * Math.exp(a * Math.log(x) - x - logGamma(a));
        } else {
            double f;
            double C, D;
            double b1 = x + 1.0 - a;
            D = b1; if (Math.abs(D) < TINY) D = TINY;
            C = b1; if (Math.abs(C) < TINY) C = TINY;
            D = 1.0 / D;
            f = C * D;
            for (int i = 2; i < 2000; i++) {
                double ai = (i - 1) * (a - (i - 1));
                double bi = x + 2.0 * i - 1.0 - a;
                D = bi + ai * D; if (Math.abs(D) < TINY) D = TINY;
                C = bi + ai / C; if (Math.abs(C) < TINY) C = TINY;
                D = 1.0 / D;
                double delta = C * D;
                f *= delta;
                if (Math.abs(delta - 1.0) < EPSILON) break;
            }
            return 1.0 - f * Math.exp(a * Math.log(x) - x - logGamma(a));
        }
    }

    private static double baseRegularizedBeta(double a, double b, double x) {
        if (x <= 0.0) return 0.0;
        if (x >= 1.0) return 1.0;
        if (x > (a + 1.0) / (a + b + 2.0)) {
            return 1.0 - baseRegularizedBeta(b, a, 1.0 - x);
        }
        double front = Math.exp(a * Math.log(x) + b * Math.log(1.0 - x) - Math.log(a) - logBeta(a, b));
        double f = 1.0;
        double C = f, D = 0.0;
        for (int n = 1; n < 2000; n++) {
            double an = (n % 2 != 0) ?
                    - (a + (double) (n - 1) / 2) * (a + b + (double) (n - 1) / 2) * x / ((a + (n - 1)) * (a + n)) :
                    ((double) n / 2) * (b - (double) n / 2) * x / ((a + n - 1) * (a + n));
            D = 1.0 + an * D; if (Math.abs(D) < TINY) D = TINY;
            C = 1.0 + an / C; if (Math.abs(C) < TINY) C = TINY;
            D = 1.0 / D;
            double delta = C * D;
            f *= delta;
            if (Math.abs(delta - 1.0) < EPSILON) break;
        }
        return front * f;
    }

    private static double stNormalCDF(double z) {
        if (z == 0.0) return 0.5;
        double val = 0.5 * baseRegularizedGammaP(0.5, z * z / 2.0);
        return z > 0.0 ? 0.5 + val : 0.5 - val;
    }

    // --- Core High-Precision Recurrence Implementations ---
    public static double NonCentChiSquared(double x, double df, double lambda) {
        if (x <= 0.0) return 0.0;
        if (df < 0 || lambda < 0) {
            throw new IllegalArgumentException("df and lambda must be non-negative.");
        }

        PoissonDistribution pDist = PoissonDistribution.of(lambda / 2);

        int totalTerms = 6000;
        int chunkSize = (int) Math.ceil((double) totalTerms / COUNT);

        List<Callable<Double>> tasks = new ArrayList<>();

        for (int i = 0; i < COUNT; i++) {
            final int start = i * chunkSize;
            final int end = Math.min(totalTerms, start + chunkSize);

            // Skip empty chunks if COUNT is larger than total terms
            if (start >= totalTerms) break;

            tasks.add(() -> {
                double subSum = 0.0;
                for (int j = start; j < end; j++) {
                    ChiSquaredDistribution chiDist = ChiSquaredDistribution.of(df + 2 * j);
                    double chi = chiDist.density(x);
                    subSum += pDist.probability(j) * chi;
                }
                return subSum;
            });
        }

        try {
            // Invoke all threads simultaneously and collect their individual Futures
            List<Future<Double>> futures = executor.invokeAll(tasks);

            double totalSum = 0.0;
            for (Future<Double> future : futures) {
                totalSum += future.get(); // Correctly aggregate every single batch calculation
            }

            return totalSum;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread integration execution was interrupted", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Error computing noncentral distribution subinterval", e.getCause());
        }
    }

    public static double NonCentF(double x, double df1, double df2, double lambda) {
        if (x <= 0.0) return 0.0;
        if (df1 < 0 || df2 < 0 || lambda < 0) throw new InputOutOfRangeException("Negative inputs invalid.");

        PoissonDistribution PDist = PoissonDistribution.of(lambda / 2);

        int totalTerms = 6000;
        int chunkSize = (int) Math.ceil((double) totalTerms / COUNT);

        List<Callable<Double>> tasks = new ArrayList<>();

        for(int i = 0; i < COUNT; i++) {
            final int start = i * chunkSize;
            final int end = Math.min(totalTerms, start + chunkSize);

            if (start >= totalTerms) break;

            tasks.add(() -> {
                double subSum = 0.0;
                for (int j = start; j < end; j++) {
                    FDistribution fDist = FDistribution.of((df1 / 2) + j, df2 / 2);
                    double f = fDist.density(x);
                    subSum += PDist.probability(j) * f;
                }
                return subSum;
            });
        }

        try {
            List<Future<Double>> res = executor.invokeAll(tasks);
            double totalSum = 0.0;
            for (Future<Double> result : res) {
                totalSum += result.get();
            }
            return totalSum;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread integration execution was interrupted", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Error computing noncentral distribution subinterval", e.getCause());
        }
    }

    /**
     * Noncentral t CDF evaluated via Simpson integration over completely unpinned virtual tasks.
     */
    public static double NonCentT(double t, double f, double delta) {
        double sMax = 15.0 + Math.sqrt(f);
        int gridPoints = 6000;
        double h = sMax / gridPoints;

        double logConstant = Math.log(2.0) + (f / 2.0) * Math.log(0.5) - logGamma(f / 2.0);

        // Partition integration intervals cleanly across Virtual Threads via Callable batch slots
        int chunks = COUNT;
        int chunkSize = gridPoints / chunks;
        List<Callable<Double>> tasks = new ArrayList<>();

        for (int c = 0; c < chunks; c++) {
            final int start = c * chunkSize;
            final int end = (c == chunks - 1) ? gridPoints : (start + chunkSize);

            tasks.add(() -> {
                double subSum = 0.0;
                for (int i = start; i < end; i++) {
                    double s = i * h;
                    if (s <= 0.0) continue;

                    double logDensity = logConstant + (f - 1.0) * Math.log(s) - 0.5 * s * s;
                    double density = Math.exp(logDensity);
                    if (density < TINY) continue;

                    double normalZ = (t * s) / Math.sqrt(f) - delta;
                    double integrand = density * stNormalCDF(normalZ);

                    // Map Simpson multipliers natively
                    if (i == 0) subSum += integrand;
                    else if (i % 2 != 0) subSum += 4.0 * integrand;
                    else subSum += 2.0 * integrand;
                }
                return subSum;
            });
        }

        double integrationSum = 0.0;
        try {
            List<Future<Double>> results = executor.invokeAll(tasks);
            for (Future<Double> res : results) {
                integrationSum += res.get();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return (h / 3.0) * integrationSum;
    }
}

