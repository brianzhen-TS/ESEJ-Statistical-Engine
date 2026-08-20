package Link;

import java.util.List;

/**
 * The record method for the Multi-comparator server({@code EtaServerComparatorMultiFX}).
 */
public record CompMultiOut(ComparatorOutput Out1And2, List<List<Double>> Out3And10) implements ComparatorMultiOutput {
    @Override
    public ComparatorOutput Out1And2() { return Out1And2; }
    @Override
    public List<List<Double>> Out3And10() { return Out3And10; }
}
