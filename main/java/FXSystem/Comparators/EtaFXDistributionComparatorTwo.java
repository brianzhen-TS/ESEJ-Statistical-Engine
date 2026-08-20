package FXSystem.Comparators;

import FXBackends.EtaConsoleFXServer;
import FXSystem.ControllersAndStructures.AbstractClassesAndInterfaces.PlottersFXComponents;
import Link.*;
import Link.ExceptionStat.SystemError.RuntimeAnomalyError;
import Link.ExceptionStat.SystemException.*;
import Backends.DataIO.JavaFXCharts;

import Backends.TwoComparator.EtaServerComparatorTwoFX;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Standalone JavaFX visualizer for the Eta system designed for making comparisons of two distributions. Part of FXSystem.
 * <p>
 * Provides: <br>
 * - A static plot() method to show a distribution chart from any context.<br>
 * - A full interactive UI to select distributions, set parameters, and plot.<br>
 * - A comparator that allows users to compare two different distributions to determine their differences.<br>
 * - Able users to determine goodness-of-fit by entering empirical data and the system will output both empirical CDF(ECDF) and
 * theoretical (expected) distribution plots.
 * </p>
 * <h3>How to use it</h3>
 * <p>
 * <b>1.</b> After the page was opened (from {@link FXSystem.SinglePlotAndInit.EtaFXPlotter}), select the pair of distributions
 * you want to compare, and enter their parameters. This procedure is similar to {@link FXSystem.SinglePlotAndInit.EtaFXPlotter}
 * but you have to do it on two distributions. <br>
 * <b>Note:</b> If you want to enable ECDF fitting, please press "Switch to ECDF vs Theoretical CDF" to enable it. and note
 * that you have to enter empirical data on the second panel(adjacent to distribution 1 panel; comma separated) <br>
 * <b>2.</b> Unlike {@link FXSystem.SinglePlotAndInit.EtaFXPlotter}, you can select the distribution type in the range
 * configuration grid. After all was done, click "Compare" to show result. <br>
 * <b>3.</b> Click "Save Chart" if you want to save it as .png file.
 * </p>
 * @see FXSystem.SinglePlotAndInit.EtaFXPlotter
 * @Note This is the visual comparator ver. of the console, using JavaFX for coding this comparator. If you want to access
 * {@link EtaFXDistributionComparatorMulti}, click "Switch to Multi Comparator" button in the control grid to open it.
 * @Since Eta11.5 Java edition (ESEJ 1.0)
*/

@SuppressWarnings("ALL")
public class EtaFXDistributionComparatorTwo extends PlottersFXComponents {
    // UI components
    private ComboBox<String> distCombo1;
    private GridPane paramGrid1;
    private Map<String, TextField> paramFields1 = new HashMap<>();

    private ComboBox<String> distCombo2;
    private GridPane paramGrid2;
    private Map<String, TextField> paramFields2 = new HashMap<>();

    private TextArea dataInputArea;
    private TextArea powerDataInput;

    private boolean ecdfMode = false;
    private Button switchModeButton;
    private Button returnModeButton;

    private RadioButton pdfRadio, cdfRadio;
    private TextField xMinField, xMaxField, pointsField;
    private Button plotButton;
    private LineChart<Number, Number> chart;
    private VBox secondPanelContainer;

    private Stage MultiCompare;
    private Button switchToMulti;

    // Threading support
    private ProgressIndicator progressIndicator;
    private AtomicBoolean computationRunning = new AtomicBoolean(false);

    private static final ExecutorService EXECUTOR =
            Executors.newThreadPerTaskExecutor(
                    Thread.ofVirtual()
                            .name("MultiCompare-", 0)
                            .factory()
            );

    @Override
    public void start(Stage stage) throws FileNotFoundException {
        try {
            stage = buildStage();
            stage.show();
        } catch (Exception ex) {
            RuntimeAnomalyError error = new RuntimeAnomalyError(ex.getCause());
            error.printStackTrace();
        }
    }

    private Stage buildStage() throws ImageNotFoundException {
        Stage stage = new Stage();
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Top: distribution selectors
        HBox topPanel = createDistributionSelectors();
        root.setTop(topPanel);

        // Center: chart
        BorderPane panel = new BorderPane();
        chart = createChart();
        Button saveButton = new Button("Save Chart");
        saveButton.getStylesheets().add("FXSystem/Comparators/Comparator.css");
        saveButton.setOnAction(e -> {
            try {
                JavaFXCharts.ImageBuilder(chart, "Comparison_of_" + distCombo1.getValue() + "_and_" + distCombo2.getValue() + ".png");
            } catch (Exception ex) {
                RuntimeAnomalyError error = new RuntimeAnomalyError(ex.getCause());
                showError(error.getMessage());
            }
        });
        panel.setCenter(chart);
        panel.setBottom(saveButton);

        root.setCenter(panel);

        // Bottom: controls
        VBox bottomPanel = createControlPanel();
        root.setBottom(bottomPanel);

        Scene scene = new Scene(root, 1300, 800);
        scene.getStylesheets().add("FXSystem/Comparators/Comparator.css");
        stage.setScene(scene);
        stage.setTitle("Eta Distribution Comparator");
        Image icon = loadIcon();
        if (icon != null) stage.getIcons().add(icon);

        // Initialize parameter fields for default distribution
        rebuildParameterFields(distCombo1.getValue(), paramGrid1, paramFields1);

        updateSecondPanel();
        return stage;
    }

    public HBox createDistributionSelectors() {
        // Panel 1 (always visible)
        VBox panel1 = new VBox(5);
        panel1.setPadding(new Insets(10));
        panel1.setStyle("-fx-border-color: #33BBAD; -fx-border-radius: 5;");
        Label title1 = new Label("Distribution 1");
        title1.getStyleClass().add("section-title");
        distCombo1 = new ComboBox<>();
        for (Distribution d : DistributionRegistry.getAll()) {
            distCombo1.getItems().add(d.getName());
        }
        distCombo1.setValue("Normal");
        distCombo1.setOnAction(e -> rebuildParameterFields(distCombo1.getValue(), paramGrid1, paramFields1));
        distCombo1.getStyleClass().add("dist-combo1");

        paramGrid1 = new GridPane();
        paramGrid1.setHgap(10);
        paramGrid1.setVgap(5);
        paramGrid1.setPadding(new Insets(5, 0, 0, 0));
        ScrollPane scroll1 = new ScrollPane(paramGrid1);
        scroll1.setFitToWidth(true);
        scroll1.setPrefHeight(100);

        panel1.getChildren().addAll(title1, distCombo1, scroll1);

        // Panel 2 (dynamic container)
        secondPanelContainer = new VBox(5);
        secondPanelContainer.setPadding(new Insets(10));
        secondPanelContainer.setStyle("-fx-border-color: #aa33bb; -fx-border-radius: 5;");

        // We'll fill it in updateSecondPanel()
        HBox hbox = new HBox(20, panel1, secondPanelContainer);
        hbox.setPadding(new Insets(0, 0, 10, 0));
        return hbox;
    }

    private void updateSecondPanel() {
        secondPanelContainer.getChildren().clear();

        if (ecdfMode) {
            // ECDF mode: show data input
            Label title = new Label("Empirical Data");
            title.getStyleClass().add("section-title");
            dataInputArea = new TextArea();
            dataInputArea.setPrefHeight(150);
            dataInputArea.setPromptText("""
                    Enter data values, one per line or comma-separated.
                    Example:
                    1.2, 2.3, 3.1
                    4.5
                    6.7
                    """);
            secondPanelContainer.getChildren().addAll(title, dataInputArea);
        } else {
            // Ordinary mode: show distribution 2 selector and parameters
            Label title = new Label("Distribution 2");
            title.getStyleClass().add("section-title");
            distCombo2 = new ComboBox<>();
            for (Distribution d : DistributionRegistry.getAll()) {
                distCombo2.getItems().add(d.getName());
            }
            distCombo2.setValue("T");
            distCombo2.setOnAction(e -> rebuildParameterFields(distCombo2.getValue(), paramGrid2, paramFields2));
            distCombo2.getStyleClass().add("dist-combo2");

            paramGrid2 = new GridPane();
            paramGrid2.setHgap(10);
            paramGrid2.setVgap(5);
            paramGrid2.setPadding(new Insets(5, 0, 0, 0));
            ScrollPane scroll2 = new ScrollPane(paramGrid2);
            scroll2.setFitToWidth(true);
            scroll2.setPrefHeight(100);

            secondPanelContainer.getChildren().addAll(title, distCombo2, scroll2);

            // Initialize parameter fields for distCombo2
            if (distCombo2.getValue() != null) {
                rebuildParameterFields(distCombo2.getValue(), paramGrid2, paramFields2);
            }
        }
    }

    public LineChart<Number, Number> createChart() {
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
        cdfRadio = new RadioButton("CDF");
        ToggleGroup typeGroup = new ToggleGroup();
        pdfRadio.setToggleGroup(typeGroup);
        cdfRadio.setToggleGroup(typeGroup);
        pdfRadio.setSelected(true);   // default choice

        xMinField = new TextField("-3");
        xMaxField = new TextField("3");
        pointsField = new TextField("300");

        progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(30, 30);
        progressIndicator.setVisible(false);

        plotButton = new Button("Compare");
        plotButton.setOnAction(e -> {
            try {
                compareDistributions();
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        // Mode switching buttons
        switchModeButton = new Button("Switch to ECDF vs Theoretical CDF");
        switchModeButton.setOnAction(e -> {
            ecdfMode = true;
            updateSecondPanel();
            pdfRadio.setVisible(false);
            cdfRadio.setVisible(false);
            switchModeButton.setDisable(true);
            returnModeButton.setDisable(false);
            // Clear chart
            chart.getData().clear();
        });

        returnModeButton = new Button("Return to Ordinary Mode");
        returnModeButton.setDisable(true);
        returnModeButton.setOnAction(e -> {
            ecdfMode = false;
            updateSecondPanel();
            pdfRadio.setVisible(true);
            cdfRadio.setVisible(true);
            returnModeButton.setDisable(true);
            switchModeButton.setDisable(false);
            chart.getData().clear();
        });

        switchToMulti = new Button("Switch to Multi Comparator");
        switchToMulti.setOnAction(e -> {
            if (MultiCompare != null && MultiCompare.isShowing()) {
                // Stage exists and is visible – bring it to front
                MultiCompare.toFront();
                MultiCompare.requestFocus();
                return;
            }

            // Create a new stage
            MultiCompare = new Stage();
            EtaFXDistributionComparatorMulti compApp = new EtaFXDistributionComparatorMulti();
            try {
                compApp.start(MultiCompare);
                MultiCompare.setOnHidden(event -> MultiCompare = null);
            } catch (Exception ex) {
                MultiCompare = null;
                Alert alert = new Alert(Alert.AlertType.ERROR, "Could not open comparator: " + ex.getMessage());
                alert.showAndWait();
            }
        });

        GridPane controlsGrid = new GridPane();
        controlsGrid.setHgap(15);
        controlsGrid.setVgap(8);
        controlsGrid.setPadding(new Insets(10, 0, 10, 0));
        controlsGrid.addRow(0, new Label("Function:"), pdfRadio, cdfRadio);
        controlsGrid.addRow(1, new Label("x min:"), xMinField);
        controlsGrid.addRow(2, new Label("x max:"), xMaxField);
        controlsGrid.addRow(3, new Label("Points:"), pointsField);
        controlsGrid.add(plotButton, 1, 5);


        HBox modeButtons = new HBox(10, plotButton, switchModeButton);
        HBox Multi = new HBox(10, switchToMulti, returnModeButton);
        VBox controlMode = new VBox(10, modeButtons, Multi);
        VBox vbox = new VBox(10, controlsGrid, controlMode);
        vbox.setPadding(new Insets(10, 0, 0, 0));
        return vbox;
    }

    private void rebuildParameterFields(String distName, GridPane grid, Map<String, TextField> fieldMap) {
        grid.getChildren().clear();
        fieldMap.clear();
        Distribution dist = DistributionRegistry.get(distName);
        if (dist == null) return;

        int row = 0;
        for (Parameter p : dist.getParameters()) {
            Label label = new Label(p.description() + ":");
            TextField field = new TextField(defaultValue(p.name()));
            fieldMap.put(p.name(), field);
            grid.add(label, 0, row);
            grid.add(field, 1, row);
            row++;
        }
    }

    @Override
    protected String defaultValue(String name) {
        return switch (name) {
            case "mean", "meanLog" -> "0";
            case "n" -> "10";
            case "p" -> "0.5";
            case "alpha", "beta", "a" -> "2";
            default -> "1";
        };
    }

    private void compareDistributions() {
        // Prevent multiple concurrent computations
        if (computationRunning.getAndSet(true)) {
            return;
        }

        // Disable button and show progress
        plotButton.setDisable(true);
        progressIndicator.setVisible(true);

        // Capture the current state for the task
        String distName1 = distCombo1.getValue();
        if (distName1 == null) {
            showError("Please select distribution 1.");
            computationRunning.set(false);
            plotButton.setDisable(false);
            progressIndicator.setVisible(false);
            return;
        }

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
            computationRunning.set(false);
            plotButton.setDisable(false);
            progressIndicator.setVisible(false);
            return;
        }
        if (min >= max) {
            showError("xMin must be less than xMax.");
            computationRunning.set(false);
            plotButton.setDisable(false);
            progressIndicator.setVisible(false);
            return;
        }

        // Generate x‑values (sorted)
        double[] xValues = new double[points];
        double step = (max - min) / (points - 1);
        for (int i = 0; i < points; i++) {
            xValues[i] = min + i * step;
        }

        // Prepare data for the task
        boolean ordinaryMode = !ecdfMode;
        boolean ecdf = ecdfMode;

        // Gather parameters for distribution 1 (and 2 if ordinary)
        Map<String, Double> params1 = convertParamMap(paramFields1);
        Map<String, Double> params2 = ordinaryMode ? convertParamMap(paramFields2) : null;
        String distName2 = ordinaryMode ? distCombo2.getValue() : null;

        // For ECDF, we need the data
        String dataInput = ecdf ? dataInputArea.getText() : null;

        // --- Task for heavy computation ---
        Task<ComparatorOutput> task = new Task<>() {
            @Override
            protected ComparatorOutput call() throws Exception {
                if (ordinaryMode) {
                    List<Map<String, Double>> paramsDouble = new ArrayList<>();
                    paramsDouble.add(params1);
                    paramsDouble.add(params2);
                    return EtaServerComparatorTwoFX.compute(
                            Arrays.asList(DistributionRegistry.get(distName1), DistributionRegistry.get(distName2)), type, xValues, paramsDouble
                    );
                } else if (ecdf) {
                    // ECDF mode
                    double[] data = parseData(dataInput);
                    if (data.length == 0) {
                        throw new IllegalArgumentException("No valid data entered.");
                    }
                    return EtaConsoleFXServer.ComparateWithECDF(
                            distName1, data, xValues, params1, false
                    );
                } else {
                    throw new IllegalStateException("Unknown mode.");
                }
            }
        };

        task.setOnSucceeded(e -> {
            try {
                ComparatorOutput output = task.getValue();
                // Clear chart
                chart.getData().clear();

                if (ordinaryMode) {
                    // Two series
                    List<Double> result1 = output.Dist1();
                    List<Double> result2 = output.Dist2();

                    XYChart.Series<Number, Number> series1 = new XYChart.Series<>();
                    series1.setName(distName1 + " " + type);
                    for (int i = 0; i < xValues.length; i++) {
                        series1.getData().add(new XYChart.Data<>(xValues[i], result1.get(i)));
                    }
                    XYChart.Series<Number, Number> series2 = new XYChart.Series<>();
                    series2.setName(distName2 + " " + type);
                    for (int i = 0; i < xValues.length; i++) {
                        series2.getData().add(new XYChart.Data<>(xValues[i], result2.get(i)));
                    }
                    chart.getData().addAll(series1, series2);
                } else if (ecdf) {
                    // ECDF: output.Dist1() = ECDF, Dist2() = theoretical
                    List<Double> ecdfValues = output.Dist1();
                    List<Double> theoretical = output.Dist2();

                    XYChart.Series<Number, Number> seriesECDF = new XYChart.Series<>();
                    seriesECDF.setName("ECDF");
                    for (int i = 0; i < xValues.length; i++) {
                        seriesECDF.getData().add(new XYChart.Data<>(xValues[i], ecdfValues.get(i)));
                    }
                    XYChart.Series<Number, Number> seriesTheoretical = new XYChart.Series<>();
                    seriesTheoretical.setName(distName1 + " CDF");
                    for (int i = 0; i < xValues.length; i++) {
                        seriesTheoretical.getData().add(new XYChart.Data<>(xValues[i], theoretical.get(i)));
                    }
                    chart.getData().addAll(seriesECDF, seriesTheoretical);
                }
            } catch (Exception ex) {
                showError("Error updating chart: " + ex.getMessage());
                ex.printStackTrace();
            } finally {
                // Re‑enable UI
                plotButton.setDisable(false);
                progressIndicator.setVisible(false);
                computationRunning.set(false);
            }
        });

        task.setOnFailed(e -> {
            showError("Computation error: " + task.getException().getMessage());
            task.getException().printStackTrace();
            plotButton.setDisable(false);
            progressIndicator.setVisible(false);
            computationRunning.set(false);
        });

        EXECUTOR.submit(task);
    }

    // ---------- Helper methods ----------
    private double[] parseData(String input) {
        List<Double> values = new ArrayList<>();
        for (String line : input.split("\\n")) {
            line = line.trim();
            if (line.isEmpty()) continue;
            for (String part : line.split(",")) {
                part = part.trim();
                if (part.isEmpty()) continue;
                try {
                    values.add(Double.parseDouble(part));
                } catch (NumberFormatException ignored) {}
            }
        }
        return values.stream().mapToDouble(Double::doubleValue).toArray();
    }

    private Map<String, Double> convertParamMap(Map<String, TextField> fieldMap) {
        Map<String, Double> params = new HashMap<>();
        for (Map.Entry<String, TextField> entry : fieldMap.entrySet()) {
            try {
                double value = Double.parseDouble(entry.getValue().getText());
                params.put(entry.getKey(), value);
            } catch (NumberFormatException e) {
                params.put(entry.getKey(), 0.0);
            }
        }
        return params;
    }

    @Override
    protected void showError(String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
            alert.showAndWait();
        });
    }

    private void showResult(String title, String content) {
        Platform.runLater(() -> {
            Stage resultStage = new Stage();
            resultStage.setTitle(title);
            TextArea textArea = new TextArea(content);
            textArea.setEditable(false);
            textArea.setPrefSize(400, 300);
            Scene scene = new Scene(new StackPane(textArea), 400, 300);
            resultStage.setScene(scene);
            resultStage.show();
        });
    }

    private static Image loadIcon() throws ImageNotFoundException {
        // Try to load from classpath
        InputStream is = EtaFXDistributionComparatorTwo.class.getResourceAsStream("/FXSystem/Comparators/eta-icon.jpeg");
        if (is != null) {
            return new Image(is);
        }
        // Fallback: try file system
        try {
            return new Image(new FileInputStream("FXSystem/Comparators/eta-icon.jpeg"));
        } catch (FileNotFoundException e) {
            // Return null or a default icon; don't crash the app
            System.err.println("Icon not found: " + e.getMessage());
            return null;
        }
    }
}