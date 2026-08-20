package Link;

import java.util.List;

/**
 * The record method for the Two-Comparator server({@code EtaServerComparatorTwo}).
 */
public record CompOut(List<Double> Dist1, List<Double> Dist2) implements ComparatorOutput {
    @Override
    public List<Double> Dist1() {
        return Dist1;
    }
    @Override
    public List<Double> Dist2() {
        return Dist2;
    }
}
