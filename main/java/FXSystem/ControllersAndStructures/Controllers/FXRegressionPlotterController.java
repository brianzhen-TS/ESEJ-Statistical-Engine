package FXSystem.ControllersAndStructures.Controllers;

import FXSystem.ControllersAndStructures.AbstractClassesAndInterfaces.PlottersFXMainController;
import FXSystem.ControllersAndStructures.AbstractClassesAndInterfaces.SpecialAbstractClasses.TypeI;
import FXSystem.ControllersAndStructures.AbstractClassesAndInterfaces.SpecialAbstractClasses.TypeIStageSize;
import FXSystem.SinglePlotAndInit.EtaFXPlotter;
import JFreeChartFXSystemInit.Generator;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.fx.ChartViewer;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** @Supports {@link FXSystem.Fitters.FXRegressionApp}*/
public class FXRegressionPlotterController extends PlottersFXMainController {

    private static Button generateBtn;
    private static Button saveBtn;
    private static TextArea xInputArea;
    private static TextArea yInputArea;
    private ChartViewer chartViewer;
    private JFreeChart currentChart;

    private boolean debugMode = false;

    private TypeIStageSize sizeConfig = new TypeIStageSize();

    private static final ExecutorService EXECUTOR =
            Executors.newThreadPerTaskExecutor(
                    Thread.ofVirtual()
                            .name("MultiCompare-", 0)
                            .factory()
            );

    @Override
    public void getInputs() {
        // parse data from text areas
        String xText = xInputArea.getText();
        String yText = yInputArea.getText();
        if (xText.trim().isEmpty() || yText.trim().isEmpty()) {
            throw new IllegalArgumentException("Please enter both X and Y values.");
        }

        List<Double> xVals = parseDoubles(xText);
        List<Double> yVals = parseDoubles(yText);
        if (xVals.size() != yVals.size()) {
            throw new IllegalArgumentException("X and Y must have the same number of values.");
        }
        // store as primitive arrays for computation
        xValues = xVals.stream().mapToDouble(Double::doubleValue).toArray();
        yValues = yVals.stream().mapToDouble(Double::doubleValue).toArray();
    }

    private double[] xValues;
    private double[] yValues;

    private List<Double> parseDoubles(String text) {
        return Arrays.stream(text.split("[,\\s]+"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Double::parseDouble)
                .toList();
    }

    @Override
    public void compute() {
        if (debugMode) IO.println("Compute method enabled.");

        if (xValues == null || yValues == null || xValues.length == 0) {
            showError("No data to plot. Please enter values and click Generate.");
            return;
        }

        Task<JFreeChart> task = new Task<>() {
            @Override
            protected JFreeChart call() {
                return Generator.createRegressionChart(xValues, yValues, "Regression plots");
            }
        };

        task.setOnSucceeded(_ -> {
            currentChart = task.getValue();
            if (chartViewer != null) {
                chartViewer.setChart(currentChart);
                if (debugMode) IO.println("Chart update successful.");
            } else {
                showError("Chart viewer not initialised.");
            }
        });

        task.setOnFailed(_ -> {
            Throwable ex = task.getException();
            showError("Error occurred: " + (ex != null ? ex.getMessage() : "unknown"));
            ex.printStackTrace();
        });

        EXECUTOR.submit(task);
    }

    @Override
    protected BorderPane createRoot() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        root.getStylesheets().add(getClass().getResource("/FXSystem/Fitters/Regression.css").toExternalForm());

        LayoutComponents components = new LayoutComponents();
        components.ConfigResults();
        if (debugMode) IO.println("chartViewer = " + chartViewer);
        chartViewer = components.getViewer();
        root.setCenter(chartViewer);
        root.setLeft(components.ConfigVBox());
        root.setTop(components.TopPanel());

        // Wire event handlers
        generateBtn.setOnAction(_ -> {
            try {
                getInputs();
                compute();
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        saveBtn.setOnAction(_ -> {
            if (currentChart == null) {
                showError("No chart to save. Generate a plot first.");
                return;
            }
            FileChooser chooser = new FileChooser();
            chooser.setInitialFileName("regression_plot.png");
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png")
            );
            File file = chooser.showSaveDialog(chartViewer.getScene().getWindow());
            if (file != null) {
                try {
                    org.jfree.chart.ChartUtils.saveChartAsPNG(file, currentChart, 800, 600);
                } catch (IOException ex) {
                    showError("Failed to save chart: " + ex.getMessage());
                }
            }
        });

        return root;
    }

    @Override
    public Stage buildStage() {
        Stage stage = new Stage();
        stage.setScene(new Scene(createRoot(), sizeConfig.getWidth(), sizeConfig.getHeight()));
        stage.setTitle("Eta Regression Visualizer");
        stage.getIcons().add(loadImage());
        return stage;
    }

    @Override
    protected Image loadImage() {
        // Try to load from classpath
        InputStream is = EtaFXPlotter.class.getResourceAsStream("/FXSystem/Fitters/eta-icon.jpeg");
        if (is != null) {
            return new Image(is);
        }
        // Fallback: try file system
        try {
            return new Image(new FileInputStream("FXSystem/Fitters/eta-icon.jpeg"));
        } catch (FileNotFoundException e) {
            // Return null or a default icon; don't crash the app
            System.err.println("Icon not found: " + e.getMessage());
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

    // ----- Inner Layout Class -----
    class LayoutComponents extends TypeI {
        private ChartViewer viewer;

        @Override
        protected VBox BoxOne() {
            Label label = new Label("X-coordinate input");
            xInputArea = new TextArea();
            xInputArea.setPrefHeight(200);
            xInputArea.setPrefWidth(200);
            xInputArea.setPromptText("e.g.: 1.4, 7.2, 2.4, 5.8, 1.9, 7.9");
            return new VBox(10, label, xInputArea);
        }

        @Override
        protected VBox BoxTwo() {
            Label label = new Label("Observed Y values");
            yInputArea = new TextArea();
            yInputArea.setPrefHeight(200);
            yInputArea.setPrefWidth(200);
            yInputArea.setPromptText("e.g.: 1.9, 9.2, 2.3, 5.2, 1.5, 7.1");
            return new VBox(10, label, yInputArea);
        }

        @Override
        protected VBox ConfigVBox() {
            VBox box = new VBox(BoxOne(), BoxTwo());
            box.setSpacing(15);
            return box;
        }

        @Override
        protected HBox TopPanel() {
            generateBtn = new Button("Generate Plot");
            saveBtn = new Button("Save Chart");

            return new HBox(15, new Label("ESEJ Regression Visualizer"), generateBtn, saveBtn);
        }

        @Override
        protected Pane ConfigResults() {
            XYSeriesCollection emptyData = new XYSeriesCollection();
            JFreeChart dummyChart = ChartFactory.createScatterPlot(
                    "Linear Regression", "X", "Y", emptyData,
                    PlotOrientation.VERTICAL, true, true, false
            );
            viewer = new ChartViewer(dummyChart);
            viewer.setPrefSize(TopPanel().getPrefHeight() - sizeConfig.getHeight(), ConfigVBox().getPrefHeight() - sizeConfig.getWidth());
            return new StackPane(viewer);
        }

        public ChartViewer getViewer() {
            return viewer;
        }
    }
}