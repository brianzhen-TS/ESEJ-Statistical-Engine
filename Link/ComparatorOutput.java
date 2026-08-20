package Link;

import java.util.List;

/**
 * The output format of the FX Two-comparator server.
 */
@SuppressWarnings("ALL")
public interface ComparatorOutput {
    List<Double> Dist1();
    List<Double> Dist2();
}
