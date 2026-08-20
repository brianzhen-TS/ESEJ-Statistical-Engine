package FXSystem.ControllersAndStructures.Controllers.Initialize;

import Backends.DataIO.JavaFXCharts;
import Backends.DataIO.JavaFXToFilesIO;
import FXBackends.EtaConsoleFXServer;
import FXSystem.Comparators.EtaFXDistributionComparatorTwo;
import FXSystem.ControllersAndStructures.AbstractClassesAndInterfaces.LayoutsExtra.InsertableGrid;
import FXSystem.ControllersAndStructures.AbstractClassesAndInterfaces.PlottersFXMainController;
import FXSystem.DataAnalysers.EtaFXDataAnalyser;
import FXSystem.Fitters.FXRegressionApp;
import FXSystem.PowerAnalyser.AppOfPowerAnalyser;
import FXSystem.SinglePlotAndInit.EtaFXPlotter;
import FXSystem.SinglePlotAndInit.EtaFXPlotterContour;
import Link.*;
import Link.ExceptionStat.SystemError.RuntimeAnomalyError;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/** @Supports {@link EtaFXPlotter}*/
public class FXSinglePlotterController extends PlottersFXMainController {

    // UI components for the interactive window
    private ComboBox<String> distCombo;
    private VBox paramBox;
    private final Map<String, TextField> paramFields = new HashMap<>();
    private RadioButton pdfRadio, cdfRadio;
    private TextField xMinField, xMaxField, pointsField;

    private Button switchToPowerButton;
    private Button backToOrdinaryButton;
    private static boolean isOrdinary = true;
    private static Distribution distValue;

    // External files (pages)
    private Stage comparatorStage;
    private Stage ContourStage;
    private Stage analyserStage;
    private Stage PowerAnalyserStage;
    private Stage RegressionStage;

    private static LineChart<Number, Number> chart;
    private double[] lastXArray;
    private double[] lastYArray;

    private static final ExecutorService EXECUTOR =
            Executors.newThreadPerTaskExecutor(
                    Thread.ofVirtual()
                            .name("SinglePlot-", 0)
                            .factory()
            );

    public FXSinglePlotterController() {}

    @Override
    protected Image loadImage() {
        // Try to load from classpath
        InputStream is = EtaFXPlotter.class.getResourceAsStream("/FXSystem/SinglePlotAndInit/eta-icon.jpeg");
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

    @Override
    public BorderPane SinglePageBorderPane() {
        return createRoot(true);
    }

    @Override
    protected BorderPane createMainUI() {
        return createRoot(false);
    }

    private void plot(String distName, Map<String, Double> params,
                     String type, double xMin, double xMax, int points) {
        if (!Platform.isFxApplicationThread()) {
            final CountDownLatch latch = new CountDownLatch(1);
            Platform.startup(latch::countDown);
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("JavaFX startup interrupted", e);
            }
        }
        Platform.runLater(() -> {
            try {
                createAndShowPlot(distName, params, type, xMin, xMax, points);
            } catch (Exception e) {
                throw new RuntimeException(e.getCause());
            }
        });
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

    private void plotFromUI() {
        String distName = distCombo.getValue();
        if (distName == null) return;
        distCombo.getStyleClass().add("dist-combo");

        Map<String, Double> params = new HashMap<>();
        for (Map.Entry<String, TextField> e : paramFields.entrySet()) {
            try {
                params.put(e.getKey(), Double.parseDouble(e.getValue().getText()));
            } catch (NumberFormatException ex) {
                showError("Invalid number for " + e.getKey());
                return;
            }
        }

        String type = pdfRadio.isSelected() ? "PDF" : "CDF";
        double xMin, xMax;
        int points;
        try {
            xMin = Double.parseDouble(xMinField.getText());
            xMax = Double.parseDouble(xMaxField.getText());
            points = Integer.parseInt(pointsField.getText());
            if (points < 10) points = 10;
            if (points > 2000) points = 2000;
        } catch (NumberFormatException ex) {
            showError("Invalid x‑range or points");
            return;
        }
        if (xMin >= xMax) {
            showError("xMin must be less than xMax");
            return;
        }

        plot(distName, params, type, xMin, xMax, points);
    }

    private void createAndShowPlot(String distName, Map<String, Double> params,
                                   String type, double xMin, double xMax, int points) {
        Stage stage = new Stage();

        // 1. Check distribution
        Distribution dist = isOrdinary ? DistributionRegistry.get(distName) : DistributionRegistryPower.get(distName);
        if (dist == null) {
            showError("Unknown distribution: " + distName);
            return;
        }

        String missingParams = validateParameters(dist, params);
        if (missingParams != null) {
            showError("Missing required parameter(s): " + missingParams);
            return;
        }

        // 2. Generate x‑values
        double[] xArray = new double[points];
        double step = (xMax - xMin) / (points - 1);
        for (int i = 0; i < points; i++) {
            xArray[i] = xMin + i * step;
        }

        // 3. Create the chart (axes only, no series yet)
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("x");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(type + " value");

        chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(type + " of " + distName + " distribution");
        chart.setCreateSymbols(false);
        chart.getStylesheets().add("/FXSystem/SinglePlotAndInit/Plotter.css");

        // 4. Create background task to compute y‑values
        Task<List<Double>> task = new Task<>() {
            @Override
            protected List<Double> call() throws Exception {
                PlotterOutput output = EtaConsoleFXServer.compute(dist, type, xArray, params, false);
                return output.Dist();
            }
        };

        // 5. On success: store data, populate chart, and show stage
        task.setOnSucceeded(_ -> {
            List<Double> yList = task.getValue();

            // Store data
            lastXArray = xArray;
            lastYArray = yList.stream().mapToDouble(Double::doubleValue).toArray();

            // Build series from stored data
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(type);
            for (int i = 0; i < lastXArray.length; i++) {
                series.getData().add(new XYChart.Data<>(lastXArray[i], lastYArray[i]));
            }
            chart.getData().add(series);

            // Now show the stage
            stage.show();
        });

        task.setOnFailed(_ -> {
            Throwable ex = task.getException();
            showError("Computation failed: " + (ex != null ? ex.getMessage() : "unknown error"));
        });

        // 6. Buttons
        Button savePNG = new Button("Save as PNG");
        savePNG.setOnAction(_ -> {
            try {
                JavaFXCharts.ImageBuilder(chart, type + "_plot_of_" + distName + ".png");
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        var excelExport = createExcelExport(distName, type, stage);

        // 7. Layout
        BorderPane root = new BorderPane();
        root.setCenter(chart);

        HBox bottom = new HBox(10, savePNG, excelExport);
        bottom.getStyleClass().add("hbox");
        bottom.getStylesheets().add("/FXSystem/SinglePlotAndInit/Plotter.css");
        bottom.setPadding(new Insets(10));
        root.setBottom(bottom);

        // 8. Scene and stage
        stage.setTitle("Eta Plot: " + distName);
        Image icon = loadImage();
        if (icon != null) stage.getIcons().add(icon);
        stage.setScene(new Scene(root, 800, 600));

        // 9. Start the background task
        EXECUTOR.submit(task);
    }

    private @NonNull Button createExcelExport(String distName, String type, Stage stage) {
        Button excelExport = new Button("Export to Excel");
        excelExport.setOnAction(_ -> {
            try {
                if (lastXArray == null || lastYArray == null) {
                    showError("No data to export. Please compute a plot first.");
                    return;
                }
                FileChooser fileChooser = new FileChooser();
                fileChooser.setInitialFileName("data.xlsx");
                fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("Excel files (*.xlsx)", "*.xlsx")
                );
                File file = fileChooser.showSaveDialog(stage);
                if (file != null) {
                    String path = file.getAbsolutePath();
                    if (!path.toLowerCase().endsWith(".xlsx")) {
                        file = new File(path + ".xlsx");
                    }
                    JavaFXToFilesIO.Excel excel = new JavaFXToFilesIO.Excel();
                    excel.exportXY(
                            lastXArray,
                            lastYArray,
                            type,
                            file.getAbsolutePath(),
                            String.format("Data of %s of %s distribution", type, distName)
                    );
                }
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });
        return excelExport;
    }

    private static String validateParameters(Distribution dist, Map<String, Double> params) {
        StringBuilder missing = new StringBuilder();
        for (Parameter p : dist.getParameters()) {
            if (!params.containsKey(p.name())) {
                if (!missing.isEmpty()) missing.append(", ");
                missing.append(p.name());
            }
        }
        return missing.isEmpty() ? null : missing.toString();
    }

    @Override
    protected BorderPane createRoot(boolean SingleApplications) {
        try {
            BorderPane root = new BorderPane();
            root.setPadding(new Insets(10));

            // Top: distribution selection
            distCombo = new ComboBox<>();
            distCombo.setValue("Exponential");
            distCombo.getStyleClass().add("dist-combo");
            // Single event handler – checks mode and calls appropriate update
            distCombo.setOnAction(_ -> {
                if (isOrdinary) {
                    updateParameterFields();
                } else {
                    updatePowerParams();
                }
            });

            // Populate combo with ordinary distributions
            updateCombo(); // now populates based on isOrdinary

            HBox topBar = new HBox(10, new Label("Distribution:"), distCombo);
            topBar.setPadding(new Insets(0, 0, 10, 0));

            // Left: dynamic parameter panel (unchanged)
            paramBox = new VBox(5);
            paramBox.setPadding(new Insets(10));
            ScrollPane paramScroll = new ScrollPane(paramBox);
            paramScroll.setFitToWidth(true);
            paramScroll.setPrefWidth(280);

            // Center: placeholder
            String text = "Select distribution and parameters, then click 'Plot'.";
            if (!SingleApplications) {
                text = """ 
                    Select distribution and parameters, then click 'Plot'.
                    Click 'Open comparator', 'Open contour plotter' to open comparator or contour plotter,
                    or click 'Open data analyser' to open data analyser.
                    """;
            }
            Label placeholder = new Label(text);
            root.setCenter(placeholder);

            var openComparator = createOpenComparator();

            var openContourPlotter = createOpenContourPlotter();

            var openPowerAnalyser = createOpenPowerAnalyser();

            var openDataAnalyser = createOpenDataAnalyser();

            var openRegressionVisualizer = createOpenRegressionVisualizer();

            // Buttons navigating user to other pages
            GridPane selector = InsertableGrid.InsertViaRows(
                    new Region[]{openDataAnalyser, new Label("To open data analyser")},  // 0
                    new Region[]{openContourPlotter, new Label("To open contour plotter")},  // 1
                    new Region[]{openComparator, new Label("To open comparator")},  // 2
                    new Region[]{openPowerAnalyser, new Label("To open power analyser")},  // 3
                    new Region[]{openRegressionVisualizer, new Label("To open regression visualizer")}  // 4
            );
            selector.setVgap(10);
            selector.setHgap(5);

            Label OpenDescript = new Label("Click the buttons below to access more features:");

            VBox controlToOtherPages = new VBox(OpenDescript, selector);
            controlToOtherPages.setSpacing(10);
            controlToOtherPages.setPadding(new Insets(10));

            // Bottom: plot controls
            pdfRadio = new RadioButton("PDF");
            cdfRadio = new RadioButton("CDF");
            ToggleGroup typeGroup = new ToggleGroup();
            pdfRadio.setToggleGroup(typeGroup);
            cdfRadio.setToggleGroup(typeGroup);
            pdfRadio.setSelected(true);

            xMinField = new TextField("0");
            xMaxField = new TextField("3");
            pointsField = new TextField("300");

            Button plotBtn = new Button("Plot");
            plotBtn.setOnAction(_ -> plotFromUI());

            GridPane controls = InsertableGrid.InsertViaRows(
                    new Region[]{new Label("Type:"), pdfRadio, cdfRadio},
                    new Region[]{new Label("x min:"), xMinField},
                    new Region[]{new Label("x max:"), xMaxField},
                    new Region[]{new Label("Points:"), pointsField},
                    new Region[]{plotBtn}
            );
            controls.setHgap(10);
            controls.setVgap(5);

            // Mode switching buttons
            switchToPowerButton = new Button("Switch to Non‑Central Distributions");
            backToOrdinaryButton = new Button("Back to Ordinary Distributions");
            backToOrdinaryButton.setDisable(true);

            switchToPowerButton.setOnAction(_ -> {
                isOrdinary = false;
                cdfRadio.setVisible(false);
                updateCombo();                // refresh combo list
                distCombo.setValue(DistributionRegistryPower.getAll().iterator().next().getName());
                updatePowerParams();          // update parameter fields
                switchToPowerButton.setDisable(true);
                backToOrdinaryButton.setDisable(false);
                if (chart != null) chart.getData().clear();      // clear chart if any
            });

            backToOrdinaryButton.setOnAction(_ -> {
                isOrdinary = true;
                cdfRadio.setVisible(true);
                updateCombo();                // refresh combo list
                distCombo.setValue(DistributionRegistry.getAll().iterator().next().getName());
                updateParameterFields();      // update parameter fields
                backToOrdinaryButton.setDisable(true);
                switchToPowerButton.setDisable(false);
                if (chart != null) chart.getData().clear();
            });

            VBox modeButtons = new VBox(5, switchToPowerButton, backToOrdinaryButton);

            if (SingleApplications) {
                controlToOtherPages.setVisible(false);
            }

            VBox controlsWithMode = new VBox(10, controls, modeButtons);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            // selector is the VBox with navigation buttons (comparator, contour, analyzer)
            HBox bottomControl = new HBox(15, controlsWithMode, spacer, controlToOtherPages);

            VBox bottomBar = new VBox(5, bottomControl);
            bottomBar.setPadding(new Insets(10, 0, 0, 0));

            root.setBottom(bottomBar);
            root.setTop(topBar);
            root.setLeft(paramScroll);

            return root;
        } catch (Exception ex) {
            throw new RuntimeAnomalyError("Anomaly occurred: ", ex.getCause());
        }
    }

    private @NonNull Button createOpenRegressionVisualizer() {
        Button openRegressionVisualizer = new Button("Open regression visualizer");
        openRegressionVisualizer.setOnAction(_ -> {
            if (RegressionStage != null && RegressionStage.isShowing()) {
                RegressionStage.toFront();
                RegressionStage.requestFocus();
                return;
            }

            RegressionStage = new Stage();
            FXRegressionApp compApp = new FXRegressionApp();
            try {
                compApp.start(RegressionStage);
                RegressionStage.setOnHidden(_ -> RegressionStage = null);
            } catch (Exception ex) {
                RegressionStage = null;
                showError("Could not open regression: " + ex.getMessage());
            }
        });
        return openRegressionVisualizer;
    }

    private @NonNull Button createOpenDataAnalyser() {
        Button openDataAnalyser = new Button("Open data analyser");
        openDataAnalyser.setOnAction(_ -> {
            if (analyserStage != null && analyserStage.isShowing()) {
                analyserStage.toFront();
                analyserStage.requestFocus();
                return;
            }

            analyserStage = new Stage();
            EtaFXDataAnalyser compApp = new EtaFXDataAnalyser();
            try {
                compApp.start(analyserStage);
                analyserStage.setOnHidden(_ -> analyserStage = null);
            } catch (Exception ex) {
                analyserStage = null;
                showError("Could not open analyser: " + ex.getMessage());
            }
        });
        return openDataAnalyser;
    }

    private @NonNull Button createOpenContourPlotter() {
        Button openContourPlotter = new Button("Open contour plotter(EtaFXPlotterContour)");
        openContourPlotter.setOnAction(_ -> {
            if (ContourStage != null && ContourStage.isShowing()) {
                ContourStage.toFront();
                ContourStage.requestFocus();
                return;
            }

            ContourStage = new Stage();
            EtaFXPlotterContour compApp = new EtaFXPlotterContour();
            try {
                compApp.start(ContourStage);
                ContourStage.setOnHidden(_ -> ContourStage = null);
            } catch (Exception ex) {
                ContourStage = null;
                showError("Could not open plotter: " + ex.getMessage());
            }
        });
        return openContourPlotter;
    }

    private @NonNull Button createOpenComparator() {
        Button openComparator = new Button("Open comparator");
        openComparator.setOnAction(_ -> {
            if (comparatorStage != null && comparatorStage.isShowing()) {
                comparatorStage.toFront();
                comparatorStage.requestFocus();
                return;
            }
            comparatorStage = new Stage();
            EtaFXDistributionComparatorTwo compApp = new EtaFXDistributionComparatorTwo();
            try {
                compApp.start(comparatorStage);
                comparatorStage.setOnHidden(_ -> comparatorStage = null);
            } catch (Exception ex) {
                comparatorStage = null;
                showError("Could not open comparator: " + ex.getMessage());
            }
        });
        return openComparator;
    }

    private @NonNull Button createOpenPowerAnalyser() {
        Button openPowerAnalyser = new Button("Open power analyser");
        openPowerAnalyser.setDisable(true);
        openPowerAnalyser.setOnAction(_ -> {
            if (PowerAnalyserStage != null && PowerAnalyserStage.isShowing()) {
                PowerAnalyserStage.toFront();
                PowerAnalyserStage.requestFocus();
                return;
            }

            PowerAnalyserStage = new Stage();
            AppOfPowerAnalyser compApp = new AppOfPowerAnalyser();
            try {
                compApp.start(PowerAnalyserStage);
                PowerAnalyserStage.setOnHidden(_ -> PowerAnalyserStage = null);
            } catch (Exception ex) {
                PowerAnalyserStage = null;
                showError("Could not open analyser: " + ex.getMessage());
            }
        });
        return openPowerAnalyser;
    }

    private void updateCombo() {
        distCombo.getItems().clear();
        if (isOrdinary) {
            for (Distribution d : DistributionRegistry.getAll()) {
                distCombo.getItems().add(d.getName());
            }
        } else {
            for (Distribution d : DistributionRegistryPower.getAll().stream().filter(dist -> dist.getName().equals(
                    "NonCentral T"
            )).toList()) {
                distCombo.getItems().add(d.getName());
            }
        }
        if (!distCombo.getItems().isEmpty()) {
            distCombo.setValue(distCombo.getItems().getFirst());
        }
    }

    @Override
    protected void updateParameterFields() {
        paramFields.clear();
        paramBox.getChildren().clear();
        String selected = distCombo.getValue();
        if (selected == null) return;
        distValue = DistributionRegistry.get(selected);
        if (distValue == null) return;

        for (Parameter p : distValue.getParameters()) {
            Label label = new Label(p.description() + ":");
            TextField field = new TextField(defaultValue(p.name()));
            paramFields.put(p.name(), field);
            HBox row = new HBox(5, label, field);
            row.setPadding(new Insets(2, 0, 2, 0));
            paramBox.getChildren().add(row);
        }
    }

    private void updatePowerParams() {
        paramFields.clear();
        paramBox.getChildren().clear();
        String selected = distCombo.getValue();
        if (selected == null) return;
        distValue = DistributionRegistryPower.get(selected);
        if (distValue == null) return;

        for (Parameter p : distValue.getParameters()) {
            Label label = new Label(p.description() + ":");
            TextField field = new TextField(defaultValue(p.name()));
            paramFields.put(p.name(), field);
            HBox row = new HBox(5, label, field);
            row.setPadding(new Insets(2, 0, 2, 0));
            paramBox.getChildren().add(row);
        }
    }

    private Stage constructStage(boolean singleApplications) {
        // 1. Create splash stage
        Stage splashStage = new Stage();
        splashStage.initStyle(StageStyle.UNDECORATED);
        splashStage.setAlwaysOnTop(true);
        // 2. Build splash content
        VBox splashRoot = new VBox();
        splashRoot.setPrefSize(300, 400);
        splashRoot.setAlignment(Pos.CENTER);
        splashRoot.setBackground(new Background(new BackgroundFill(
                Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY
        )));

        InputStream splashStream = getClass().getResourceAsStream("/FXSystem/SinglePlotAndInit/eta-load-screen.jpeg");
        Image splashImage = (splashStream != null) ? new Image(splashStream) : null;

        if (splashImage != null) {
            ImageView imageView = new ImageView(splashImage);
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(600);
            imageView.setFitHeight(400);
            splashRoot.getChildren().add(imageView);
        } else {
            Label fallback = new Label("Loading Eta...");
            fallback.setStyle("-fx-text-fill: #A9B7C6; -fx-font-size: 24; -fx-font-weight: bold;");
            splashRoot.getChildren().add(fallback);
        }

        Scene splashScene = new Scene(splashRoot, 300, 400);
        splashStage.setScene(splashScene);

        // 3. Add icon
        Image icon = loadImage();
        if (icon != null) splashStage.getIcons().add(icon);

        // 4. Center and show splash
        splashStage.centerOnScreen();
        splashStage.show();

        // 5. Load main UI in background
        Thread.ofVirtual().name("InitWorker").start(() -> {
            try {
                Thread.sleep(3000);

                // Build main UI
                BorderPane mainRoot = singleApplications
                        ? SinglePageBorderPane()
                        : createMainUI();

                int h = singleApplications ? 600 : 700;
                int w = singleApplications ? 825 : 1100;
                Scene mainScene = new Scene(mainRoot, w, h);
                URL cssUrl = getClass().getResource("/FXSystem/SinglePlotAndInit/Plotter.css");
                if (cssUrl != null) {
                    mainScene.getStylesheets().add(cssUrl.toExternalForm());
                }

                Platform.runLater(this::updateParameterFields);

                // 6. On JavaFX thread: close splash, show main stage
                Platform.runLater(() -> {
                    // Create main stage
                    Stage mainStage = new Stage();
                    mainStage.setTitle(singleApplications
                            ? "Eta Statistical Visualizer Phi mod"
                            : "Eta Statistical Visualizer");
                    if (icon != null) mainStage.getIcons().add(icon);
                    mainStage.setScene(mainScene);
                    mainStage.centerOnScreen();
                    mainStage.setOnCloseRequest(_ -> {
                        Platform.exit();
                        System.exit(0);
                    });
                    // Close splash and open the main app
                    splashStage.close();
                    mainStage.show();
                });

            } catch (InterruptedException e) {
                System.err.println("Splash loading interrupted.");
                Platform.runLater(() -> {
                    splashStage.close();
                    showError("Loading failed: " + e.getMessage());
                });
            }
        });

        return splashStage;
    }

    @Override
    public Stage buildSinglePageStage() {
        return constructStage(true);
    }

    @Override
    public Stage buildStage() {
        return constructStage(false);
    }

    @Override
    public void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.showAndWait();
    }
}
