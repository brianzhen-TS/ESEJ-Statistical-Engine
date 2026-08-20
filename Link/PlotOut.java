package Link;

import java.util.List;

public record PlotOut(List<Double> dist) implements Link.PlotterOutput {
    @Override
    public List<Double> Dist() {
        return dist;
    }
}
