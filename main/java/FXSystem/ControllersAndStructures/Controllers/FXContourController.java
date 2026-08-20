package FXSystem.ControllersAndStructures.Controllers;

import Backends.Eta2DServer.EtaServer2DPlotterFX;
import Backends.FXSystemSmile.FXDataAnalyserSmileBackend;
import FXSystem.Comparators.EtaFXDistributionComparatorTwo;
import FXSystem.ControllersAndStructures.AbstractClassesAndInterfaces.PlottersFXComponents;
import Link.MultivariateDistribution;
import Link.MultivariateDistributionRegistry;
import Link.Parameter;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/** @Supports {@link FXSystem.SinglePlotAndInit.EtaFXPlotterContour}*/
public class FXContourController extends PlottersFXComponents {
    private ComboBox<String> distCombo;
    private static GridPane paramGrid;
    private static Map<String, TextField> paramFields = new HashMap<>();

    private TextField xMinField, xMaxField, yMinField, yMaxField;
    private CheckBox showGridCheck;
    private Button plotButton;
    private Button saveButton;
    private Canvas canvas;
    private Label statusLabel;

    private int CanvaSize;
    private int Margin;

    public void setXMinField(TextField xMinField) {
        this.xMinField = xMinField;
    }
    public void setXMaxField(TextField xMaxField) {
        this.xMaxField = xMaxField;
    }
    public void setYMinField(TextField yMinField) {
        this.yMinField = yMinField;
    }
    public void setYMaxField(TextField yMaxField) {
        this.yMaxField = yMaxField;
    }
    public void setDistCombo(ComboBox<String> distCombo) {
        this.distCombo = distCombo;
    }
    public void setParamGrid(GridPane paramGrid) {
        this.paramGrid = paramGrid;
    }

    // JavaFX layouts
    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
    }
    public void setSaveButton(Button saveButton) {
        this.saveButton = saveButton;
    }
    public void setStatusLabel(Label statusLabel) {
        this.statusLabel = statusLabel;
    }
    public void setShowGridCheck(CheckBox showGridCheck) {
        this.showGridCheck = showGridCheck;
    }
    public void setPlotButton(Button plotButton) {
        this.plotButton = plotButton;
    }

    // Canva settings
    public void setCanvaSize(int size) {
        this.CanvaSize = size;
    }
    public void setMargin(int margin) {
        Margin = margin;
    }

    // Class constructor
    public FXContourController() {}

    // getters
    public GridPane getParamGrid() {
        return paramGrid;
    }

    public ComboBox<String> getDistCombo() {
        return distCombo;
    }

    public TextField getXMinField() {
        return xMinField;
    }
    public TextField getXMaxField() {
        return xMaxField;
    }
    public TextField getYMinField() {
        return yMinField;
    }
    public TextField getYMaxField() {
        return yMaxField;
    }

    public CheckBox getShowGridCheck() {
        return showGridCheck;
    }
    public Button getPlotButton() {
        return plotButton;
    }
    public Button getSaveButton() {
        return saveButton;
    }
    public Label getStatusLabel() {
        return statusLabel;
    }
    public Canvas getCanvas() {
        return canvas;
    }

    public int getCanvaSize() {
        return CanvaSize;
    }
    public int getMargin() {
        return Margin;
    }

    // ----- Dynamic parameter field builder -----
    public void rebuildParameterFields(String distName) {
        paramGrid.getChildren().clear();
        paramFields.clear();

        MultivariateDistribution dist = MultivariateDistributionRegistry.get(distName);
        if (dist == null) return;

        int row = 0;
        for (Parameter p : dist.getParameters()) {
            Label label = new Label(p.description() + ":");
            TextField field = new TextField(defaultValue(p.name()));
            paramFields.put(p.name(), field);
            paramGrid.add(label, 0, row);
            paramGrid.add(field, 1, row);
            row++;
        }
    }

    @Override
    protected String defaultValue(String name) {
        return switch (name) {
            case "meanX", "meanY", "cov" -> "0";
            default -> "1";
        };
    }

    // ----- Gather parameters from UI -----
    private Map<String, Double> gatherParameters() {
        Map<String, Double> params = new HashMap<>();
        for (Map.Entry<String, TextField> e : paramFields.entrySet()) {
            try {
                params.put(e.getKey(), Double.parseDouble(e.getValue().getText()));
            } catch (NumberFormatException ex) {
                showError("Invalid number for " + e.getKey());
                return null;
            }
        }
        return params;
    }

    // ----- Generate contour plot -----
    public void generateContourPlot() {
        String distName = distCombo.getValue();
        if (distName == null) return;

        Map<String, Double> params = gatherParameters();
        if (params == null) return;

        // Read ranges
        double xMin, xMax, yMin, yMax;
        try {
            xMin = Double.parseDouble(xMinField.getText().trim());
            xMax = Double.parseDouble(xMaxField.getText().trim());
            yMin = Double.parseDouble(yMinField.getText().trim());
            yMax = Double.parseDouble(yMaxField.getText().trim());
        } catch (NumberFormatException e) {
            xMin = -18; xMax = 18; yMin = -18; yMax = 18;
            setRangeFields(xMin, xMax, yMin, yMax);
            statusLabel.setText("Range reset to default (invalid input)");
        }

        if (xMin >= xMax || yMin >= yMax) {
            xMin = -18; xMax = 18; yMin = -18; yMax = 18;
            if (distName.equalsIgnoreCase("Normal-Gamma")) {
                xMin = 0.1;
                yMin = 0.1;
            }
            setRangeFields(xMin, xMax, yMin, yMax);
            statusLabel.setText("Range reset to default (invalid range)");
        }

        // Compute grid
        double[][] grid;
        try {
            grid = EtaServer2DPlotterFX.computeGrid(distName, params, xMin, xMax, yMin, yMax, CanvaSize, CanvaSize);
        } catch (Exception e) {
            statusLabel.setText("Computation error: " + e.getMessage());
            showError(e.getMessage());
            return;
        }

        // Render
        try {
            EtaServer2DPlotterFX.renderContour(
                    canvas,
                    grid,
                    xMin, xMax,
                    yMin, yMax,
                    distName + " Density Contour",
                    "X", "Y",
                    EtaServer2DPlotterFX.DEFAULT_COLOR_MAP,
                    showGridCheck.isSelected(),
                    Margin
            );
            statusLabel.setText("Contour Plotter ready");
        } catch (Exception e) {
            statusLabel.setText("Rendering error: " + e.getMessage());
            showError(e.getMessage());
            e.printStackTrace();
        }
    }

    public void showSmilePlot() throws Exception {
        String distName = distCombo.getValue();
        if (distName == null) return;

        Map<String, Double> params = gatherParameters();
        if (params == null) return;

        double xMin, xMax, yMin, yMax;
        try {
            xMin = Double.parseDouble(xMinField.getText().trim());
            xMax = Double.parseDouble(xMaxField.getText().trim());
            yMin = Double.parseDouble(yMinField.getText().trim());
            yMax = Double.parseDouble(yMaxField.getText().trim());
        } catch (NumberFormatException ex) {
            showError("Invalid range values. Using defaults.");
            xMin = -18; xMax = 18; yMin = -18; yMax = 18;
            setRangeFields(xMin, xMax, yMin, yMax);
        }

        if (xMin >= xMax || yMin >= yMax) {
            showError("Invalid range: xMin >= xMax or yMin >= yMax");
            return;
        }

        // Compute grid (reuse the existing computeGrid)
        double[][] grid = EtaServer2DPlotterFX.computeGrid(
                distName, params, xMin, xMax, yMin, yMax, CanvaSize, CanvaSize
        );

        // Generate Smile plot as BufferedImage
        int imgWidth = 700;
        int imgHeight = 700;
        BufferedImage bi = FXDataAnalyserSmileBackend.generateImage(
                FXDataAnalyserSmileBackend.ChartType.CONTOUR_HEATMAP,
                grid,
                xMin, xMax, yMin, yMax,
                imgWidth, imgHeight,
                distName + " Density (Smile)",
                "X", "Y"
        );

        // Convert to JavaFX Image and show in a new Stage
        Image fxImage = SwingFXUtils.toFXImage(bi, null);
        ImageView imageView = new ImageView(fxImage);
        imageView.setPreserveRatio(true);

        Stage plotStage = new Stage();
        plotStage.setTitle("Smile Plot – " + distName);
        plotStage.getIcons().add(loadIcon());
        Scene scene = new Scene(new StackPane(imageView), imgWidth + 20, imgHeight + 20);
        plotStage.setScene(scene);
        plotStage.show();
    }

    private void setRangeFields(double xMin, double xMax, double yMin, double yMax) {
        xMinField.setText(String.valueOf(xMin));
        xMaxField.setText(String.valueOf(xMax));
        yMinField.setText(String.valueOf(yMin));
        yMaxField.setText(String.valueOf(yMax));
    }

    @Override
    public void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.showAndWait();
    }

    public static Image loadIcon() {
        // Try to load from classpath
        InputStream is = EtaFXDistributionComparatorTwo.class.getResourceAsStream("/FXSystem/SinglePlotAndInit/eta-icon.jpeg");
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
}
