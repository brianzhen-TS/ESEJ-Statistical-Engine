/**
 * The power analyzer of Eta FXSystem. It was designed for performing power analysis for various statistical tests like
 * <b><p>
 * - Student's T <br>
 * - Chi-squared(Goodness-of-fit / Homogeneity, Contingency tables, Variance) <br>
 * - F(Variance, One-Way-ANOVA)
 * </p></b>
 * and output the results in the box below with the following format:
 * <pre>{@code
 * // Chi-squared variance test with inputs 1,2.3,4.5,7.8,1.5 and hypothesized variance 1.0
 * === Power Analysis Result ===
 * Test: Chi-squared – Variance test
 *
 * Statistic: 31.148
 * Power:     0.9999986582614014
 * PDF (central):    1.3417385985067813E-6
 * PDF (non‑central):1.3417385985067813E-6
 * }</pre>
 */

package FXSystem.PowerAnalyser;