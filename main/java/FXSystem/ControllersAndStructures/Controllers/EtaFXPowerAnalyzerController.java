package FXSystem.ControllersAndStructures.Controllers;

import Backends.PowerAnalyser.FXPowerAnalyserServer;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/** @Supports {@link FXSystem.PowerAnalyser.AppOfPowerAnalyser}*/
public class EtaFXPowerAnalyzerController extends BorderPane {

    // -------------------- UI Components --------------------
    private final ComboBox<String> testFamilyCombo = new ComboBox<>();
    private final ComboBox<String> subTestCombo = new ComboBox<>();
    private final TextArea dataInputArea = new TextArea();
    private final TextArea resultArea = new TextArea();
    private final Button computeButton = new Button("Compute Power");
    private final Label statusLabel = new Label("Ready");

    // Parameter fields (dynamically generated)
    private final GridPane paramGrid = new GridPane();
    private final Map<String, TextField> paramFields = new HashMap<>();

    // -------------------- Threading --------------------
    private static final ExecutorService EXECUTOR =
            Executors.newFixedThreadPool(
                    2,
                    new PowerAnalyzerThreadFactory("PowerAnalyzer")
            );

    private static class PowerAnalyzerThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        PowerAnalyzerThreadFactory(String namePrefix) {
            this.namePrefix = namePrefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, namePrefix + "-" + threadNumber.getAndIncrement());
            t.setDaemon(true);
            t.setPriority(Thread.NORM_PRIORITY - 1);
            t.setUncaughtExceptionHandler((thread, ex) ->
                    System.err.println("Uncaught in " + thread.getName() + ": " + ex)
            );
            return t;
        }
    }

    // -------------------- Constructor --------------------
    public EtaFXPowerAnalyzerController() {
        buildUI();
        setupListeners();
        // Default selection
        testFamilyCombo.getSelectionModel().selectFirst();
        updateSubTests();
    }

    // -------------------- UI Construction --------------------
    private void buildUI() {
        // ----- Top: test family and sub‑test selection -----
        testFamilyCombo.getItems().addAll("T-test", "Chi-squared", "F-test");
        testFamilyCombo.setValue("T-test");
        testFamilyCombo.getStyleClass().add("test-combo");

        subTestCombo.setPrefWidth(180);
        subTestCombo.getStyleClass().add("sub-combo");

        HBox topBox = new HBox(10,
                new Label("Test family:"), testFamilyCombo,
                new Label("Sub‑test:"), subTestCombo
        );
        topBox.setPadding(new Insets(10));
        topBox.setStyle("-fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");

        // ----- Center: data input and parameters -----
        dataInputArea.setPromptText("""
                Enter data according to the test type.
                Examples:
                T‑test:   sample1 values (comma/space separated)
                          (blank line)
                          sample2 values
                Chi‑squared Goodness‑of‑fit (same as F-tests for variances):
                          observed counts (comma separated)
                          expected probabilities (comma separated)
                For ANOVA (one‑way):
                - First line: observed values (comma separated)
                - Second line: group labels (integers, comma separated)
                - Example:
                  5.2, 6.1, 7.3, 5.8
                  1, 1, 2, 2
                """);
        dataInputArea.setPrefHeight(200);
        dataInputArea.setWrapText(true);

        // Parameter grid (dynamically updated)
        paramGrid.setHgap(10);
        paramGrid.setVgap(5);
        paramGrid.setPadding(new Insets(5));
        ScrollPane paramScroll = new ScrollPane(paramGrid);
        paramScroll.setFitToWidth(true);
        paramScroll.setPrefHeight(100);

        VBox centerBox = new VBox(10,
                new Label("Data input:"), dataInputArea,
                new Label("Additional parameters:"), paramScroll
        );
        centerBox.setPadding(new Insets(10));

        // ----- Bottom: compute button, progress, status, results -----
        resultArea.setEditable(false);
        resultArea.setPrefHeight(150);
        resultArea.setWrapText(true);


        HBox controlBox = new HBox(10,
                computeButton,
                statusLabel
        );
        controlBox.setPadding(new Insets(5));
        controlBox.setStyle("-fx-alignment: center-left;");

        VBox bottomBox = new VBox(5,
                controlBox,
                new Label("Results:"),
                resultArea
        );
        bottomBox.setPadding(new Insets(10));

        // Assemble
        this.setTop(topBox);
        this.setCenter(centerBox);
        this.setBottom(bottomBox);

        // Initial parameter fields for default test
        updateParameterFields();
    }

    // -------------------- Listeners --------------------
    private void setupListeners() {
        testFamilyCombo.setOnAction(e -> {
            updateSubTests();
            updateParameterFields();
        });
        subTestCombo.setOnAction(e -> updateParameterFields());

        computeButton.setOnAction(e -> startComputation());
    }

    // -------------------- Dynamic UI Updates --------------------
    private void updateSubTests() {
        subTestCombo.getItems().clear();
        String family = testFamilyCombo.getValue();
        if (family == null) return;

        switch (family) {
            case "T-test" -> subTestCombo.getItems().add("Two‑sample (independent)");
            case "Chi-squared" -> {
                subTestCombo.getItems().addAll(
                        "Goodness‑of‑fit",
                        "Contingency table",
                        "Variance test"
                );
            }
            case "F-test" -> {
                subTestCombo.getItems().addAll(
                        "Variance test (two samples)",
                        "ANOVA (one‑way)"
                );
            }
        }
        subTestCombo.getSelectionModel().selectFirst();
    }

    private void updateParameterFields() {
        paramGrid.getChildren().clear();
        paramFields.clear();

        String family = testFamilyCombo.getValue();
        String sub = subTestCombo.getValue();
        if (family == null || sub == null) return;

        // Define parameter labels and default values per test type
        List<FXPowerAnalyserServer.ParamDef> params = getParameterDefinitions(family, sub);
        int row = 0;
        for (FXPowerAnalyserServer.ParamDef p : params) {
            Label label = new Label(p.label() + ":");
            TextField field = new TextField(p.defaultVal());
            field.setPrefWidth(100);
            paramGrid.add(label, 0, row);
            paramGrid.add(field, 1, row);
            paramFields.put(p.name(), field);
            row++;
        }
    }

    private List<FXPowerAnalyserServer.ParamDef> getParameterDefinitions(String family, String sub) {
        return FXPowerAnalyserServer.getParameterDefinitions(family, sub);
    }

    // -------------------- Computation --------------------
    private void startComputation() {
        // Validate input
        String dataText = dataInputArea.getText();
        if (dataText.trim().isEmpty()) {
            showError("Please enter data.");
            return;
        }

        computeButton.setDisable(false);
        statusLabel.setText("Computing...");
        resultArea.clear();

        // Capture parameters
        String family = testFamilyCombo.getValue();
        String sub = subTestCombo.getValue();
        Map<String, Double> extraParams = new HashMap<>();
        for (Map.Entry<String, TextField> entry : paramFields.entrySet()) {
            try {
                double val = Double.parseDouble(entry.getValue().getText());
                extraParams.put(entry.getKey(), val);
            } catch (NumberFormatException e) {
                showError("Invalid number in parameter: " + entry.getKey());
                statusLabel.setText("Ready");
                return;
            }
        }

        // Create background task
        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                updateMessage("Parsing data...");
                // Parse data according to test type
                Object parsed = parseData(family, sub, dataText);

                updateMessage("Computing power...");
                // Compute power
                double[] result = computePower(family, sub, parsed, extraParams);

                // Format result
                return formatResult(family, sub, result);
            }
        };

        task.setOnSucceeded(e -> {
            resultArea.setText(task.getValue());
            statusLabel.setText("Done");
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            showError("Computation failed: " + ex.getMessage());
            ex.printStackTrace();
            statusLabel.setText("Error");
            computeButton.setDisable(false);
        });

        // Submit to our executor
        EXECUTOR.submit(task);
    }

    // -------------------- Data Parsing --------------------
    private Object parseData(String family, String sub, String input) {
        return FXPowerAnalyserServer.parseData(family, sub, input);
    }

    // -------------------- Power Computation --------------------
    private double[] computePower(String family, String sub, Object parsed, Map<String, Double> extraParams) throws Exception {
        return FXPowerAnalyserServer.computePower(family, sub, parsed, extraParams);
    }

    // -------------------- Result Formatting --------------------
    private String formatResult(String family, String sub, double[] result) {
        return FXPowerAnalyserServer.formatResult(family, sub, result);
    }

    // -------------------- Error Handling --------------------
    private void showError(String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
            alert.showAndWait();
        });
    }

    // -------------------- Shutdown hook (optional) --------------------
    public static void shutdownExecutor() {
        EXECUTOR.shutdownNow();
    }
}