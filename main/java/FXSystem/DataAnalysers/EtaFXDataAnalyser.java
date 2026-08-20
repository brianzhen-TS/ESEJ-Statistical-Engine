package FXSystem.DataAnalysers;

import Backends.DataIO.JavaFXCharts;
import Link.ExceptionStat.SystemException.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.chart.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.ArrayList;

/**
 * Part of the FXSystem with Data visualization capabilities. The application allows users to generate the following types
 * of plots: <br>
 * - Pie chart <br>
 * - Line chart <br>
 * - Bar chart <br>
 * - Area chart <br>
 * - Stacked bar chart <br>
 * - Scatter chart <br>
 * The chart are constructed using JavaFX built-in methods.
 * <h3>How to use it</h3>
 * <p>
 * 1. After the page was opened (from {@link FXSystem.SinglePlotAndInit.EtaFXPlotter}), you can see the chart type combo box. Click
 * the combo box and select the type of the chart you want to generate. <br>
 * 2. Enter the data. Different types of charts receives different types of data input formats(e.g., label,values as for
 * pie chart, and others are labels,labels). You can enter as many groups of data as you want, but remember separate them
 * by a new row (press enter). <br>
 * 3. Click "Generate chart" to generate the chosen chart with the data given in the above, and if all goes well, the chart
 * stage will appear. You can save it as .png file by pressing "Save chart" button.
 * </p>
 * @see FXSystem.SinglePlotAndInit.EtaFXPlotter
 * @since ESEJ 1.0
 */
@SuppressWarnings("ALL")
public class EtaFXDataAnalyser extends Application {
    private static Image loadIcon() {
        // Try to load from classpath
        InputStream is = EtaFXDataAnalyser.class.getResourceAsStream("/FXSystem/SinglePlotAndInit/eta-icon.jpeg");
        if (is != null) {
            return new Image(is);
        }
        // Fallback: try file system
        try {
            return new Image(new FileInputStream("FXSystem/SinglePlotAndInit/eta-icon.jpeg"));
        } catch (FileNotFoundException e) {
            // Return null or a default icon; don't crash the app
            System.err.println("Icon not found: " + e.getMessage());
            return null;
        }
    }

    enum ChartType {
        PIECHART,
        LINECHART,
        BARCHART,
        AREACHART,
        STACKEDBARCHART,
        SCATTERCHART,
    }

    private static ComboBox<ChartType> chartTypeCombo;
    private static TextArea dataInputArea;
    private static Button plotButton;
    private static Label statusLabel;

    private static void generateChart() {
        String input = dataInputArea.getText();
        if (input == null || input.isBlank()) {
            showError("Please enter data.");
            return;
        }

        ChartType selectedChartType = chartTypeCombo.getValue();
        if (selectedChartType == null) {
            showError("Please select a chart type.");
            return;
        }

        try {
            Chart chart = createChart(selectedChartType, input);
            showChartInNewStage(chart, selectedChartType);
            statusLabel.setText("Chart generated successfully.");
        } catch (Exception e) {
            showError("Error generating chart: " + e.getMessage());
            statusLabel.setText("Error");
        }
    }

    private static PieChart createPieChart(String input) {
        PieChart chart = new PieChart();
        chart.setTitle("Output: Pie chart");
        String[] lines = input.split("\\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split(",");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Each line must be 'label,value' for pie chart.");
            }
            String label = parts[0].trim();
            double value;
            try {
                value = Double.parseDouble(parts[1].trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid number: " + parts[1]);
            }
            chart.getData().add(new PieChart.Data(label, value));
        }
        return chart;
    }

    private static LineChart<Number, Number> createLineChart(String input) {
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Output: Line chart");

        String[] lines = input.split("\\n");
        List<List<Double>> allSeries = new ArrayList<>();
        int maxLength = 0;

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split(",");
            List<Double> values = new ArrayList<>();
            for (String part : parts) {
                part = part.trim();
                if (part.isEmpty()) continue;
                try {
                    values.add(Double.parseDouble(part));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid number: " + part);
                }
            }
            if (values.isEmpty()) continue;
            allSeries.add(values);
            maxLength = Math.max(maxLength, values.size());
        }

        if (allSeries.isEmpty()) {
            throw new IllegalArgumentException("No data entered.");
        }

        // Pad all series to the same length (fill with 0)
        for (List<Double> series : allSeries) {
            while (series.size() < maxLength) {
                series.add(0.0);
            }
        }

        // Create one series per line
        for (int i = 0; i < allSeries.size(); i++) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName("Data" + (i + 1));
            List<Double> values = allSeries.get(i);
            for (int j = 0; j < values.size(); j++) {
                series.getData().add(new XYChart.Data<>(j, values.get(j)));
            }
            chart.getData().add(series);
        }

        return chart;
    }

    private static BarChart<String, Number> createBarChart(String input) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Output: Bar chart");

        String[] lines = input.split("\\n");
        List<List<Double>> allSeries = new ArrayList<>();
        int maxLength = 0;

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split(",");
            List<Double> values = new ArrayList<>();
            for (String part : parts) {
                part = part.trim();
                if (part.isEmpty()) continue;
                try {
                    values.add(Double.parseDouble(part));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid number: " + part);
                }
            }
            if (values.isEmpty()) continue;
            allSeries.add(values);
            maxLength = Math.max(maxLength, values.size());
        }

        if (allSeries.isEmpty()) {
            throw new IllegalArgumentException("No data entered.");
        }

        for (List<Double> series : allSeries) {
            while (series.size() < maxLength) {
                series.add(0.0);
            }
        }

        for (int i = 0; i < allSeries.size(); i++) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Data" + (i + 1));
            List<Double> values = allSeries.get(i);
            for (int j = 0; j < values.size(); j++) {
                series.getData().add(new XYChart.Data<>("Cat" + (j + 1), values.get(j)));
            }
            chart.getData().add(series);
        }

        return chart;
    }

    private static AreaChart<Number, Number> createAreaChart(String input) {
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        AreaChart<Number, Number> chart = new AreaChart<>(xAxis, yAxis);
        chart.setTitle("Output: Area Chart");

        String[] lines = input.split("\\n");
        List<List<Double>> allSeries = new ArrayList<>();
        int maxLength = 0;

        // Parse each line into a list of numbers
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split(",");
            List<Double> values = new ArrayList<>();
            for (String part : parts) {
                part = part.trim();
                if (part.isEmpty()) continue;
                try {
                    values.add(Double.parseDouble(part));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid number: " + part);
                }
            }
            if (values.isEmpty()) continue;
            allSeries.add(values);
            if (values.size() > maxLength) maxLength = values.size();
        }

        if (allSeries.isEmpty()) {
            throw new IllegalArgumentException("No data entered.");
        }

        // Pad all series to the same length (fill with 0)
        for (List<Double> series : allSeries) {
            while (series.size() < maxLength) {
                series.add(0.0);
            }
        }

        // Create one series per line
        for (int i = 0; i < allSeries.size(); i++) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName("Data" + (i + 1));
            List<Double> values = allSeries.get(i);
            for (int j = 0; j < values.size(); j++) {
                series.getData().add(new XYChart.Data<>(j, values.get(j)));
            }
            chart.getData().add(series);
        }

        return chart;
    }

    private static StackedBarChart<String, Number> createStackedBarChart(String input) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        StackedBarChart<String, Number> chart = new StackedBarChart<>(xAxis, yAxis);
        chart.setTitle("Output: Stacked Bar Chart");


        String[] lines = input.split("\\n");
        List<List<Double>> allValues = new ArrayList<>();
        int maxCategories = 0;

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split(",");
            List<Double> values = new ArrayList<>();
            for (String part : parts) {
                part = part.trim();
                if (part.isEmpty()) continue;
                try {
                    values.add(Double.parseDouble(part));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid number: " + part);
                }
            }
            allValues.add(values);
            if (values.size() > maxCategories) maxCategories = values.size();
        }

        if (allValues.isEmpty()) {
            throw new ArrayIsEmptyException("No data entered.");
        }

        for (List<Double> series : allValues) {
            while (series.size() < maxCategories) {
                series.add(0.0);
            }
        }

        for (int i = 0; i < allValues.size(); i++) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Data" + (i + 1));
            List<Double> values = allValues.get(i);
            for (int j = 0; j < values.size(); j++) {
                String category = "Cat" + (j + 1);
                series.getData().add(new XYChart.Data<>(category, values.get(j)));
            }
            chart.getData().add(series);
        }

        return chart;
    }

    private static ScatterChart<Number, Number> createScatteredChart(String input) {
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        ScatterChart<Number, Number> chart = new ScatterChart<>(xAxis, yAxis);
        chart.setTitle("Output: Scattered chart");

        String[] lines = input.split("\\n");
        List<List<Double>> allValues = new ArrayList<>();
        int maxLength = 0;

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split(",");
            List<Double> values = new ArrayList<>();
            for (String part : parts) {
                if (part.isEmpty()) continue;
                try {
                    values.add(Double.parseDouble(part));
                } catch (NumberFormatException e) {
                    throw new InvalidInputException("Invalid number: " + part);
                }
            }
            if (values.isEmpty()) continue;
            allValues.add(values);
            if (values.size() > maxLength) maxLength = values.size();
        }

        if (allValues.isEmpty()) {
            throw new ArrayIsEmptyException("No data entered.");
        }

        for (List<Double> values : allValues) {
            while (values.size() < maxLength) {
                values.add(0.0);
            }
        }

        for (int i = 0; i < allValues.size(); i++) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName("Data" + (i + 1));
            List<Double> values = allValues.get(i);
            for (int j = 0; j < values.size(); j++) {
                series.getData().add(new XYChart.Data<>(j, values.get(j)));
            }
            chart.getData().add(series);
        }

        return chart;
    }

    private static void showChartInNewStage(Chart chart, ChartType type) {
        Stage stage = new Stage();
        stage.setTitle("Chart: " + type);
        Button saveButton = new Button("Save chart");
        saveButton.getStylesheets().add("FXSystem/DataAnalysers/DataAnalyser.css");
        saveButton.setOnAction(e -> {
            try {
                JavaFXCharts.ImageBuilder(chart, "plot_of_" + type.name() + ".png");
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });
        BorderPane root = new BorderPane();
        root.setCenter(chart);
        root.setBottom(saveButton);
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        Image icon = loadIcon();
        if (icon != null) stage.getIcons().add(icon);
        stage.show();
    }

    public static Chart createChart(ChartType chartType, String input) {
        return switch (chartType) {
            case PIECHART -> createPieChart(input);
            case LINECHART -> createLineChart(input);
            case BARCHART -> createBarChart(input);
            case AREACHART -> createAreaChart(input);
            case STACKEDBARCHART -> createStackedBarChart(input);
            case SCATTERCHART -> createScatteredChart(input);
        };
    }

    private static void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.showAndWait();
    }

    // Acts as the support of the DataAnalysers branch of the Phi mod
    public static BorderPane root() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        ChartType[] types = ChartType.values();

        // Top config.
        chartTypeCombo = new ComboBox<>();
        chartTypeCombo.getStyleClass().add("chart-type-combo");
        chartTypeCombo.getItems().addAll(ChartType.values());
        chartTypeCombo.setValue(ChartType.BARCHART);
        HBox topBar = new HBox(10, new Label("Chart type: "), chartTypeCombo);
        topBar.setPadding(new Insets(10));

        // Center config.
        Label dataLabel = new Label("Data: ");
        dataInputArea = new TextArea();
        dataInputArea.setPrefHeight(200);
        dataInputArea.setPromptText("""
        Enter data:
            For line/bar/area/stackedbar/scatter charts: each line = one series, values comma‑separated.
            Example (two series, three points each):
            10, 20, 30
            5, 15, 25
            For pie chart: each line as 'label,value'
            Example:
            Apples, 30
            Oranges, 45
            """);
        VBox centerBox = new VBox(5, dataLabel, dataInputArea);
        centerBox.setPadding(new Insets(10, 0, 0, 0));

        // Bottom config.
        plotButton = new Button("Generate chart");
        plotButton.setOnAction(e -> generateChart());

        statusLabel = new Label("Ready");
        HBox bottomBar = new HBox(10, plotButton, statusLabel);
        bottomBar.setPadding(new Insets(10, 0, 0, 0));

        root.setTop(topBar);
        root.setCenter(centerBox);
        root.setBottom(bottomBar);
        return root;
    }

    @Override
    public void start(Stage stage) {
        BorderPane root = root();

        Scene scene = new Scene(root, 700, 500);
        String css = Objects.requireNonNull(getClass().getResource("/FXSystem/DataAnalysers/DataAnalyser.css")).toExternalForm();
        scene.getStylesheets().add(css);
        stage.setScene(scene);
        stage.setTitle("Eta Data Analyser");
        Image icon = loadIcon();
        if (icon != null) stage.getIcons().add(icon);
        stage.show();
    }
}
