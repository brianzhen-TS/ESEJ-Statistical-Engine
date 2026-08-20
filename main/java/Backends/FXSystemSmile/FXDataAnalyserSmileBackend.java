package Backends.FXSystemSmile;

import smile.plot.swing.*;

import java.awt.image.BufferedImage;

/**
 * Backend for generating Smile-based plots (heatmaps, contour+heatmap)
 * with proper coordinate ranges.
 */
@SuppressWarnings("ALL")
public class FXDataAnalyserSmileBackend {

    /**
     * Generates a heatmap figure with proper axis scaling.
     *
     * @param grid      density matrix (rows = y, cols = x)
     * @param xMin      minimum x value
     * @param xMax      maximum x value
     * @param yMin      minimum y value
     * @param yMax      maximum y value
     * @param title     plot title
     * @param xLabel    label for x-axis
     * @param yLabel    label for y-axis
     * @return Smile Figure object
     */
    public static Figure generateHeatmap(double[][] grid,
                                         double xMin, double xMax, double yMin, double yMax,
                                         String title, String xLabel, String yLabel) {
        int rows = grid.length;
        int cols = (rows > 0) ? grid[0].length : 0;

        // Create coordinate arrays
        double[] xCoords = new double[cols];
        double[] yCoords = new double[rows];
        for (int i = 0; i < cols; i++) {
            xCoords[i] = xMin + (xMax - xMin) * i / (cols - 1);
        }
        for (int i = 0; i < rows; i++) {
            yCoords[i] = yMin + (yMax - yMin) * i / (rows - 1);
        }

        Heatmap heatmap = Heatmap.of(xCoords, yCoords, grid, 64);
        Figure fig = heatmap.figure();
        fig.setTitle(title);
        fig.setAxisLabels(xLabel, yLabel);
        return fig;
    }

    /**
     * Generates a contour overlay on top of the heatmap.
     */
    public static Figure generateContourHeatmap(double[][] grid,
                                                double xMin, double xMax, double yMin, double yMax,
                                                String title, String xLabel, String yLabel) {
        Figure fig = generateHeatmap(grid, xMin, xMax, yMin, yMax, title, xLabel, yLabel);

        int rows = grid.length;
        int cols = (rows > 0) ? grid[0].length : 0;

        double[] xCoords = new double[cols];
        double[] yCoords = new double[rows];
        for (int i = 0; i < cols; i++) {
            xCoords[i] = xMin + (xMax - xMin) * i / (cols - 1);
        }
        for (int i = 0; i < rows; i++) {
            yCoords[i] = yMin + (yMax - yMin) * i / (rows - 1);
        }

        Contour contour = Contour.of(xCoords, yCoords, grid);

        fig.add(contour);
        return fig;
    }

    /**
     * Renders a Smile plot to a BufferedImage with given dimensions.
     *
     * @param chartType   HEATMAP or CONTOUR_HEATMAP
     * @param grid        density matrix
     * @param xMin, xMax  x‑axis range
     * @param yMin, yMax  y‑axis range
     * @param width       image width in pixels
     * @param height      image height in pixels
     * @param title       plot title
     * @param xLabel      x‑axis label
     * @param yLabel      y‑axis label
     * @return BufferedImage
     */
    public static BufferedImage generateImage(ChartType chartType,
                                              double[][] grid,
                                              double xMin, double xMax,
                                              double yMin, double yMax,
                                              int width, int height,
                                              String title, String xLabel, String yLabel) throws Exception {
        Figure fig = switch (chartType) {
            case HEATMAP -> generateHeatmap(grid, xMin, xMax, yMin, yMax, title, xLabel, yLabel);
            case CONTOUR_HEATMAP -> generateContourHeatmap(grid, xMin, xMax, yMin, yMax, title, xLabel, yLabel);
        };
        return fig.toBufferedImage(width, height);
    }

    public enum ChartType {
        HEATMAP,
        CONTOUR_HEATMAP
    }
}