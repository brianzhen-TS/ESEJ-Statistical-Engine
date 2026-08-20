package Backends.Eta2DServer;

import Link.*;

import Link.MultivariateDistribution;

import Link.Parameter;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.Map;
import java.util.stream.IntStream;

/**
 * Backend for 2D contour plots.
 *
 * <p>Usage:
 * <pre>{@code
 * Canvas canvas = new Canvas(800, 600);
 * double[][] grid = EtaServer2DPlotterFX.computeGrid("Bivariate Normal", params, -3, 3, -3, 3, 300, 300);
 * EtaServer2DPlotterFX.renderContour(canvas, grid, -3, 3, -3, 3, "Density", "x", "y", true, 60);
 * }</pre>
 */
@SuppressWarnings("ALL")
public class EtaServer2DPlotterFX {
    /**
     * Computes a 2D density grid.
     *
     * @param distName distribution name
     * @param params   parameter map
     * @param xMin Min range of x
     * @param xMax Max range of x
     * @param yMin Min range of y
     * @param yMax  Max range of y
     * @param nx   number of points in x direction
     * @param ny   number of points in y direction
     * @return {@code double[nx][ny]} density values
     * @throws Exception if distribution not found, parameters missing, or computation fails
     */
    public static double[][] computeGrid(String distName,
                                         Map<String, Double> params,
                                         double xMin, double xMax,
                                         double yMin, double yMax,
                                         int nx, int ny) throws Exception {

        if (nx < 2 || ny < 2) throw new IllegalArgumentException("Grid size must be at least 2x2");
        if (xMin >= xMax || yMin >= yMax) throw new IllegalArgumentException("Invalid axis ranges");

        MultivariateDistribution dist = MultivariateDistributionRegistry.get(distName);
        if (dist == null) {
            throw new IllegalArgumentException("Distribution not found: " + distName);
        }

        // Validate required parameters
        for (Parameter p : dist.getParameters()) {
            if (!params.containsKey(p.name())) {
                throw new IllegalArgumentException("Missing required parameter: " + p.name());
            }
        }

        double[][] grid = new double[nx][ny];
        double xStep = (xMax - xMin) / (nx - 1);
        double yStep = (yMax - yMin) / (ny - 1);
        int threshold = 100;

        if (nx * ny > threshold) {
            IntStream.range(0, nx).parallel().forEach(val -> {
                double x = xMin + val * xStep;
                for (int j = 0; j < ny; j++) {
                    double y = yMin + j * yStep;
                    grid[val][j] = dist.density(new double[]{x, y}, params);
                }
            });
        } else {
            for (int i = 0; i < nx; i++) {
                double x = xMin + i * xStep;
                for (int j = 0; j < ny; j++) {
                    double y = yMin + j * yStep;
                    grid[i][j] = dist.density(new double[]{x, y}, params);
                }
            }
        }
        return grid;
    }

    /**
     * Renders a contour plot on a Canvas.
     *
     * @param canvas  the canvas to draw on
     * @param grid    2D density grid (nx x ny)
     * @param title   plot title
     * @param xMin Min range of x
     * @param xMax Max range of x
     * @param yMin Min range of y
     * @param yMax  Max range of y
     * @param xLabel  x-axis label
     * @param yLabel  y-axis label
     * @param colorMap a function mapping intensity [0..1] to Color
     * @param showGrid whether to draw grid lines
     * @param margin  margin around the plot (for labels and legend)
     */
    public static void renderContour(Canvas canvas,
                                     double[][] grid,
                                     double xMin, double xMax,
                                     double yMin, double yMax,
                                     String title,
                                     String xLabel, String yLabel,
                                     ColorMap colorMap,
                                     boolean showGrid,
                                     int margin) {

        int nx = grid.length;
        int ny = grid[0].length;

        // Compute min/max density
        double min = IntStream.range(0, nx).parallel()
                .mapToDouble(i -> {
                    double localMin = Double.POSITIVE_INFINITY;
                    for (int j = 0; j < ny; j++) {
                        if (grid[i][j] < localMin) localMin = grid[i][j];
                    }
                    return localMin;
                }).min().orElse(Double.POSITIVE_INFINITY);

        double max = IntStream.range(0, nx).parallel()
                .mapToDouble(i -> {
                    double localMax = Double.NEGATIVE_INFINITY;
                    for (int j = 0; j < ny; j++) {
                        if (grid[i][j] > localMax) localMax = grid[i][j];
                    }
                    return localMax;
                }).max().orElse(Double.POSITIVE_INFINITY);

        if (!Double.isFinite(max) || !Double.isFinite(min) || max == min) {
            throw new ArithmeticException("Density values are all equal or not finite.");
        }
        double range = max - min;

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        int plotWidth = nx;
        int plotHeight = ny;
        int plotX = margin;
        int plotY = margin + 30; // space for title

        // Title
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Times New Roman", 18));
        gc.fillText(title, canvas.getWidth() / 2 - 100, margin + 10);

        int[] pixelBuffer = new int[nx * ny];
        IntStream.range(0, nx).parallel().forEach(val -> {
            for (int j = 0; j < ny; j++) {
                double d = grid[j][val];
                double intensity = (d - min) / range;
                Color color = colorMap.apply(intensity);

                int a = 255;
                int r = (int) (color.getRed() * a);
                int g = (int) (color.getGreen() * a);
                int b = (int) (color.getBlue() * a);
                int argb = (a << 24) | (r << 16) | (g << 8) | b;

                pixelBuffer[val * nx + j] = argb;
            }
        });

        // Draw heatmap
        PixelWriter pixelWriter = gc.getPixelWriter();
        pixelWriter.setPixels(plotX, plotY, nx, ny, PixelFormat.getIntArgbInstance()
        ,pixelBuffer, 0, nx);

        // Grid lines
        if (showGrid) {
            gc.setStroke(Color.LIGHTGRAY);
            gc.setLineWidth(0.5);
            int nTicks = 5;
            for (int i = 0; i <= nTicks; i++) {
                double x = xMin + (xMax - xMin) * i / nTicks;
                int xPos = plotX + (int) ((x - xMin) / (xMax - xMin) * plotWidth);
                gc.strokeLine(xPos, plotY, xPos, plotY + plotHeight);
                double y = yMin + (yMax - yMin) * i / nTicks;
                int yPos = plotY + (int) ((y - yMin) / (yMax - yMin) * plotHeight);
                gc.strokeLine(plotX, yPos, plotX + plotWidth, yPos);
            }
        }

        // Axes
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeLine(plotX, plotY + plotHeight, plotX + plotWidth, plotY + plotHeight);
        gc.strokeLine(plotX, plotY, plotX, plotY + plotHeight);

        // Axis labels
        gc.setFont(Font.font("Times New Roman", 14));
        gc.setFill(Color.BLACK);
        gc.fillText(xLabel, plotX + plotWidth / 2 - 10, plotY + plotHeight + 30);
        gc.save();
        gc.translate(plotX - 30, plotY + plotHeight / 2);
        gc.rotate(-90);
        gc.fillText(yLabel, -10, 0);
        gc.restore();

        // Tick marks
        gc.setFont(Font.font("Times New Roman", 10));
        int nTicks = 5;
        for (int i = 0; i <= nTicks; i++) {
            double x = xMin + (xMax - xMin) * i / nTicks;
            int xPos = plotX + (int) ((x - xMin) / (xMax - xMin) * plotWidth);
            gc.fillText(String.format("%.2f", x), xPos - 15, plotY + plotHeight + 20);
            double y = yMin + (yMax - yMin) * i / nTicks;
            int yPos = plotY + (int) ((y - yMin) / (yMax - yMin) * plotHeight);
            gc.fillText(String.format("%.2f", y), plotX - 35, yPos + 5);
        }

        // Legend
        int legendX = plotX + plotWidth + 30;
        int legendY = plotY;
        int legendHeight = plotHeight;
        int legendWidth = 30;

        for (int py = 0; py < legendHeight; py++) {
            double intensity = 1.0 - (double) py / legendHeight;
            Color color = colorMap.apply(intensity);
            gc.setFill(color);
            gc.fillRect(legendX, legendY + py, legendWidth, 1);
        }

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeRect(legendX, legendY, legendWidth, legendHeight);

        gc.setFont(Font.font("Times New Roman", 10));
        gc.setFill(Color.BLACK);
        gc.fillText(String.format("%.3f", max), legendX, legendY - 5);
        gc.fillText(String.format("%.3f", min), legendX, legendY + legendHeight + 15);
        gc.fillText("Density", legendX - 10, legendY + legendHeight + 30);
    }

    /**
     * Functional interface for color mapping.
     */
    @FunctionalInterface
    public interface ColorMap {
        Color apply(double intensity); // intensity in [0,1]
    }

    /**
     * Default color map: blue (0) → white (0.5) → red (1).
     */
    public static final ColorMap DEFAULT_COLOR_MAP = intensity -> {
        double r, g, b;
        if (intensity < 0.5) {
            double t = intensity / 0.5;
            r = t;
            g = t;
            b = 1.0;
        } else {
            double t = (intensity - 0.5) / 0.5;
            r = 1.0;
            g = 1.0 - t;
            b = 1.0 - t;
        }
        return new Color(r, g, b, 1.0);
    };
}