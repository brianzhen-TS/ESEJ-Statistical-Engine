package Link;

import java.util.List;
/**
 * The output format of the FX Multi-comparator Server.
 * <p>
 * Unlike the Two-comparator({@code EtaServerComparatorTwo}), it splits the first and second(primary; {@code Out1And2}) and the rest
 * of the inputs({@code Out3And10}; the limit where the server still supports) apart, letting the first two distributions be processed in two-comparator while the rest of them
 * be processed in the Multi-comparator.
 * </p>
 */
public interface ComparatorMultiOutput {
    ComparatorOutput Out1And2();
    List<List<Double>> Out3And10();
}
