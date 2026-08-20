package FXSystem.ControllersAndStructures.Controllers;

import Backends.DataIO.JavaFXCharts;
import FXBackends.EtaServerComparatorMultiFX;
import FXSystem.Comparators.EtaFXDistributionComparatorMulti;
import FXSystem.ControllersAndStructures.AbstractClassesAndInterfaces.LayoutsExtra.InsertableGrid;
import FXSystem.ControllersAndStructures.AbstractClassesAndInterfaces.Properties.Insertable;
import FXSystem.ControllersAndStructures.AbstractClassesAndInterfaces.PlottersFXMainController;
import Link.ComparatorMultiOutput;
import Link.Distribution;
import Link.DistributionRegistry;
import Link.Parameter;
import eta.util.StreamX;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @Supports {@link EtaFXDistributionComparatorMulti}*/
public class FXCompareMultiController extends PlottersFXMainController {
    // UI components
    private VBox panelsContainer;
    public static Button addButton;
    public static Button removeButton;
    private RadioButton pdfRadio;
    private TextField xMinField, xMaxField, pointsField;
    private Button plotButton;
    private LineChart<Number, Number> chart;
    private final int MAX_PANELS = 10;
    private List<DistributionPanel> distPanels = new ArrayList<>();

    private StreamX streamX = new StreamX();

    public FXCompareMultiController() {}

    @Override
    protected BorderPane createRoot() {
        // --- Initialise buttons ---
        addButton = new Button("+ Add Distribution");
        removeButton = new Button("- Remove Last");
        addButton.setOnAction(e -> addPanel("Distribution " + (distPanels.size() + 1), "Normal"));
        removeButton.setOnAction(e -> removeLastPanel());
        updateButtons();

        // --- Create scrollable panels container ---
        panelsContainer = new VBox(10);
        panelsContainer.setPadding(new Insets(10));
        ScrollPane scroll = new ScrollPane(panelsContainer);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(200);
        scroll.setStyle("-fx-background-color: transparent; -fx-border-color: lightgray; -fx-border-width: 1;");

        // Add initial two panels
        addPanel("Distribution 1", "Normal");
        addPanel("Distribution 2", "T");

        // Button bar
        HBox buttonBar = new HBox(10, addButton, removeButton);
        buttonBar.setPadding(new Insets(5, 0, 0, 0));

        VBox topBox = new VBox(10, scroll, buttonBar);

        // --- Main layout ---
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        root.setTop(topBox);

        // Center: chart
        chart = createChart();
        root.setCenter(chart);

        // Bottom: controls
        root.setBottom(createControlPanel());

        return root;
    }

    private void addPanel(String label, String defaultDist) {
        if (distPanels.size() >= MAX_PANELS) {
            showError("Maximum " + MAX_PANELS + " distributions allowed.");
            return;
        }
        DistributionPanel panel = new DistributionPanel(label, defaultDist);
        distPanels.add(panel);
        panelsContainer.getChildren().add(panel);
        updateButtons();
    }

    private void removeLastPanel() {
        if (distPanels.size() <= 2) {
            showError("Minimum 2 distributions required.");
            return;
        }
        DistributionPanel last = distPanels.remove(distPanels.size() - 1);
        panelsContainer.getChildren().remove(last);
        updateButtons();
    }

    private void updateButtons() {
        if (addButton != null && removeButton != null) {
            removeButton.setDisable(distPanels.size() <= 2);
            addButton.setDisable(distPanels.size() >= MAX_PANELS);
        }
    }

    private LineChart<Number, Number> createChart() {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("x");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Density / Probability");
        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Distribution Comparison");
        lineChart.setCreateSymbols(false);
        lineChart.setAnimated(false);
        return lineChart;
    }

    private VBox createControlPanel() {
        pdfRadio = new RadioButton("PDF");
        RadioButton cdfRadio = new RadioButton("CDF");
        ToggleGroup group = new ToggleGroup();
        pdfRadio.setToggleGroup(group);
        cdfRadio.setToggleGroup(group);
        pdfRadio.setSelected(true);

        xMinField = new TextField("-3");
        xMaxField = new TextField("3");
        pointsField = new TextField("300");

        plotButton = new Button("Compare");
        Button saveButton = new Button("Save chart");
        saveButton.setOnAction(_ -> {
            try {
                JavaFXCharts.ImageBuilder(chart, "MultiComparison_of_" + distPanels.size() + "_distributions.png");
            } catch (Exception e) {
                showError(e.getMessage());
            }
        });
        plotButton.setOnAction(e -> compute());

        GridPane grid = InsertableGrid.InsertViaRows(
                new Region[]{new Label("Function:"), pdfRadio, cdfRadio},
                new Region[]{new Label("x min:"), xMinField},
                new Region[]{new Label("x max:"), xMaxField},
                new Region[]{new Label("Points:"), pointsField},
                new Region[]{plotButton},
                new Region[]{saveButton}
        );
        grid.setHgap(15);
        grid.setVgap(8);
        grid.setPadding(new Insets(10, 0, 10, 0));

        VBox vbox = new VBox(grid);
        vbox.setPadding(new Insets(10, 0, 0, 0));
        return vbox;
    }

    @Override
    public void compute() {
        try {
            String type = pdfRadio.isSelected() ? "PDF" : "CDF";
            double min, max;
            int points;
            try {
                min = Double.parseDouble(xMinField.getText());
                max = Double.parseDouble(xMaxField.getText());
                points = Integer.parseInt(pointsField.getText());
                if (points < 10) points = 10;
                if (points > 5000) points = 5000;
            } catch (NumberFormatException e) {
                showError("Invalid x‑range or points.");
                return;
            }
            if (min >= max) {
                showError("xMin must be less than xMax.");
                return;
            }

            double[] xValues = new double[points];
            double step = (max - min) / (points - 1);
            for (int i = 0; i < points; i++) {
                xValues[i] = min + i * step;
            }

            List<String> distNames = new ArrayList<>();
            List<Map<String, Double>> paramsList = new ArrayList<>();
            for (DistributionPanel panel : distPanels) {
                distNames.add(panel.getDistributionName());
                paramsList.add(panel.getParameterMap());
            }

            List<Distribution> dist = new ArrayList<>();
            for (int i = 0; i < distNames.size(); i++){
                dist.add(DistributionRegistry.get(distNames.get(i)));
            }

            // Ordinary input size (size < 1000)
            ComparatorMultiOutput output = EtaServerComparatorMultiFX.compute(
                    dist, type, xValues, paramsList
            );

            // Large inputs (size > 1000)
            double[][] fastX = EtaServerComparatorMultiFX.computeFast(distNames, type, xValues, paramsList, false);


            // Flatten results
            List<List<Double>> allResults = new ArrayList<>();
            allResults.add(output.Out1And2().Dist1());
            allResults.add(output.Out1And2().Dist2());
            if (output.Out3And10() != null) {
                allResults.addAll(output.Out3And10());
            }
            double[][] result = streamX.ListToStreamDoubleArray(allResults).toArray(double[][]::new);

            chart.getData().clear();
            if (xValues.length < 1000) {
                for (int i = 0; i < allResults.size(); i++) {
                    XYChart.Series<Number, Number> series = new XYChart.Series<>();
                    series.setName(distNames.get(i) + " " + type);
                    for (int j = 0; j < xValues.length; j++) {
                        series.getData().add(new XYChart.Data<>(xValues[j], result[i][j]));
                    }
                    chart.getData().add(series);
                }
            } else {
                for (int i = 0; i < fastX.length; i++) {
                    XYChart.Series<Number, Number> series = new XYChart.Series<>();
                    series.setName(distNames.get(i) + " " + type);
                    for (int j = 0; j < xValues.length; j++) {
                        double[] resultFast = fastX[i];
                        series.getData().add(new XYChart.Data<>(xValues[j], resultFast[j]));
                    }
                }
            }
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Stage createStage() {
        Stage stage = new Stage();
        // --- Scene ---
        BorderPane root = createRoot();
        Scene scene = new Scene(root, 1300, 800);
        scene.getStylesheets().add("FXSystem/Comparators/Comparator.css");
        stage.setScene(scene);
        stage.setTitle("Multi-Distribution Comparator");
        Image icon = loadImage();
        if (icon != null) stage.getIcons().add(icon);
        return stage;
    }

    @Override
    protected Image loadImage() {
        // 1. Try from classpath (inside JAR)
        InputStream is = EtaFXDistributionComparatorMulti.class.getResourceAsStream("/FXSystem/Comparators/eta-icon.jpeg");
        if (is != null) {
            return new Image(is);
        }
        // 2. Try from file system (development environment)
        try {
            return new Image(new FileInputStream("FXSystem/Comparators/eta-icon.jpeg"));
        } catch (FileNotFoundException e) {
            System.err.println("Icon not found: " + e.getMessage());
            // 3. Fallback: return null (or a default placeholder)
            return null;
        }
    }

    @Override
    public void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
            alert.showAndWait();
        });
    }

    // ----- Inner DistributionPanel -----
    private static class DistributionPanel extends VBox implements Insertable {
        private final ComboBox<String> distCombo;
        private final GridPane paramGrid;
        private final Map<String, TextField> paramFields = new HashMap<>();

        private DistributionPanel(String labelText, String defaultDist) {
            super(5);
            setPadding(new Insets(10));
            setStyle("-fx-border-color: #33BBAD; -fx-border-radius: 5;");

            Label title = new Label(labelText);
            title.getStyleClass().add("section-title");
            distCombo = new ComboBox<>();
            for (Distribution d : DistributionRegistry.getAll()) {
                distCombo.getItems().add(d.getName());
            }
            distCombo.setValue(defaultDist);
            distCombo.getStylesheets().add("FXSystem/Comparators/Comparator.css");
            distCombo.setOnAction(e -> rebuildFields());

            paramGrid = new GridPane();
            paramGrid.setHgap(10);
            paramGrid.setVgap(5);
            paramGrid.setPadding(new Insets(5, 0, 0, 0));
            ScrollPane scroll = new ScrollPane(paramGrid);
            scroll.setFitToWidth(true);
            scroll.setPrefHeight(200);

            getChildren().addAll(title, distCombo, scroll);
            rebuildFields();
        }

        @Override
        public void rebuildFields() {
            paramGrid.getChildren().clear();
            paramFields.clear();
            Distribution dist = DistributionRegistry.get(distCombo.getValue());
            if (dist == null) return;

            int row = 0;
            for (Parameter p : dist.getParameters()) {
                Label label = new Label(p.description() + ":");
                TextField field = new TextField(defaultValue(p.name()));
                paramFields.put(p.name(), field);
                paramGrid.addRow(row, label);
                paramGrid.addRow(row, field);
                row++;
            }
        }

        @Override
        public String defaultValue(String name) {
            return switch (name) {
                case "mean", "meanLog" -> "0";
                case "n" -> "10";
                case "p" -> "0.5";
                case "alpha", "beta", "a" -> "2";
                default -> "1";
            };
        }

        public String getDistributionName() {
            return distCombo.getValue();
        }

        @Override
        public Map<String, Double> getParameterMap() {
            Map<String, Double> params = new HashMap<>();
            for (Map.Entry<String, TextField> entry : paramFields.entrySet()) {
                try {
                    params.put(entry.getKey(), Double.parseDouble(entry.getValue().getText()));
                } catch (NumberFormatException e) {
                    params.put(entry.getKey(), 0.0);
                }
            }
            return params;
        }
    }
}
