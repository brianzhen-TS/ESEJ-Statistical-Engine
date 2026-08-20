package FXSystem.SinglePlotAndInit;

// EtaFXMultivariatePlotter.java
import Backends.DataIO.JavaFXCharts;
import FXSystem.ControllersAndStructures.AbstractClassesAndInterfaces.LayoutsExtra.InsertableGrid;
import FXSystem.ControllersAndStructures.AbstractClassesAndInterfaces.SpecialAbstractClasses.TypeI;
import FXSystem.ControllersAndStructures.AbstractClassesAndInterfaces.SpecialAbstractClasses.TypeIStageSize;
import FXSystem.ControllersAndStructures.Controllers.FXContourController;
import Link.*;

import Link.ExceptionStat.SystemError.RuntimeAnomalyError;
import Link.MultivariateDistribution;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

import static FXSystem.ControllersAndStructures.Controllers.FXContourController.loadIcon;

/**
 * Standalone JavaFX visualizer for the Eta system designed for plotting multivariate distributions to a 2D contour. Part of the FXSystem.
 * <p>
 * Provides: <br>
 * - A static plot() method to show a distribution chart from any context.<br>
 * - A full interactive UI to select distributions, set parameters, and plot.<br>
 * - Able to generate contour plots and save chart(as .png file) of multivariate distributions.
 * </p>
 * <h3>How to use it</h3>
 * <b>Note:</b> This is also serves as the linking page between the main page ({@link EtaFXPlotter}) and the 3D plotter page
 * ({@link EtaFXPlotter3D}). So this instructions is split into two parts: Plotting and Navigating / Exporting.
 * <h4>Plotting</h4>
 * <p>
 * <b>1.</b> Click the "Distribution" combo box to select distribution you want to plot (currently supports Bivariate Normal)
 * <br>
 * <b>2.</b> In the "Parameter" section, enter the required parameters. Then in the "Chart configurations" section,
 * configure the input ranges and you can click "Show grid" to overlay grids onto your chart. <br>
 * <b>3.</b> Click "Generate Contour Plot" to generate the contour plot.
 * </p>
 * <h4>Navigating / Exporting</h4>
 * <b>For navigating:</b> Click "To 3D Plotter" to open {@link EtaFXPlotter3D}. <br>
 * <b>For Exporting:</b> Click "Save chart" to save the generated chsart as .png file.
 * @Note This is the visual contour ver. of the console, using JavaFX for coding this plotter. <br>
 * Does support 3D plot generations via the 3D plotter({@link EtaFXPlotter3D}), users can open it via "To 3D plotter" button.
 * @see EtaFXPlotter
 * @see EtaFXPlotter3D
 * @Since Eta11.5 Java edition (ESEJ 1.0)
 */
@SuppressWarnings("ALL")
public class EtaFXPlotterContour extends TypeI {

    // UI components
    private ComboBox<String> distCombo;
    private GridPane paramGrid;
    private Map<String, TextField> paramFields = new HashMap<>();

    private TextField xMinField, xMaxField, yMinField, yMaxField;
    private CheckBox showGridCheck;
    private Button plotButton;
    private Button To3DPlotterButton;
    private Button saveButton;
    private Canvas canvas;
    private Label statusLabel;

    private Button showPlotUsingSmile;
    private Stage ThreeDimensionalPlotterStage;

    private int CANVAS_SIZE;
    private int MARGIN;

    private FXContourController controller = new FXContourController();
    private TypeIStageSize sizeConfig = new TypeIStageSize();

    @Override
    public void start(Stage stage) {
        try {
            BorderPane root = new BorderPane();
            root.setPadding(new Insets(10));

            // ----- Top: distribution selection -----
            root.setTop(TopPanel());

            // ----- Center: canvas -----
            root.setCenter(ConfigResults());

            // ----- Bottom: range inputs and status -----
            root.setLeft(ConfigVBox());

            // ----- Scene -----
            Scene scene = new Scene(root, sizeConfig.getWidth(), sizeConfig.getHeight());
            String css = getClass().getResource("/FXSystem/SinglePlotAndInit/Multivariate.css").toExternalForm();
            scene.getStylesheets().add(css);
            stage.setScene(scene);
            stage.setTitle("Eta Contour Plotter");
            Image icon = loadIcon();
            if (icon != null) stage.getIcons().add(icon);
            stage.show();

            // Initialize parameter fields for the first distribution
            if (!distCombo.getItems().isEmpty()) {
                controller.rebuildParameterFields(distCombo.getValue());
            }
        } catch (Exception e) {
            RuntimeAnomalyError error = new RuntimeAnomalyError(e);
            controller.showError(error.getMessage());
        }
    }

    @Override
    protected VBox BoxOne() {
        controller.setParamGrid(new GridPane());
        paramGrid = controller.getParamGrid();
        paramGrid.setHgap(10);
        paramGrid.setVgap(5);
        paramGrid.setPadding(new Insets(10));
        paramGrid.setStyle("-fx-border-color: lightgray; -fx-border-width: 1;");

        ScrollPane paramScroll = new ScrollPane(paramGrid);
        paramScroll.setFitToWidth(true);
        paramScroll.setPrefWidth(300);

        VBox leftPanel = new VBox(5, new Label("Parameters"), paramScroll);
        leftPanel.setSpacing(10);
        return leftPanel;
    }

    @Override
    protected VBox BoxTwo() {
        GridPane controls = new GridPane();
        controls.setPadding(new Insets(10, 0, 0, 0));

        controller.setXMinField(new TextField("-18"));
        controller.setXMaxField(new TextField("18"));
        controller.setYMinField(new TextField("-18"));
        controller.setYMaxField(new TextField("18"));

        controller.setShowGridCheck(new CheckBox("Show Grid"));

        xMinField = controller.getXMinField();
        xMaxField = controller.getXMaxField();
        yMinField = controller.getYMinField();
        yMaxField = controller.getYMaxField();
        showGridCheck = controller.getShowGridCheck();

        controls = InsertableGrid.InsertViaColumns(
                new Region[]{new Label("x min:"), new Label("y min:"), new Label("x max:"), new Label("y max:")},
                new Region[]{xMinField, yMinField, xMaxField, yMaxField},
                new Region[]{showGridCheck}
        );
        controls.setHgap(10);
        controls.setVgap(5);

        controller.setStatusLabel(new Label("Ready"));
        statusLabel = controller.getStatusLabel();
        HBox statusBar = new HBox(10, statusLabel);
        statusBar.setPadding(new Insets(5, 0, 0, 0));

        VBox ControlVBox = new VBox(new Label("Chart configurations"), controls, statusBar);
        ControlVBox.setSpacing(10);

        return ControlVBox;
    }

    @Override
    protected VBox ConfigVBox() {
        VBox leftPanel = new VBox(BoxOne(), BoxTwo());
        leftPanel.setSpacing(10);
        return leftPanel;
    }

    @Override
    protected HBox TopPanel() {
        controller.setDistCombo(new ComboBox<>());
        distCombo = controller.getDistCombo();
        distCombo.getStyleClass().add("dist-combo");
        for (MultivariateDistribution d : MultivariateDistributionRegistry.getAll()) {
            distCombo.getItems().add(d.getName());
        }
        if (!distCombo.getItems().isEmpty()) {
            distCombo.setValue(distCombo.getItems().get(0));
        }
        distCombo.setOnAction(e -> controller.rebuildParameterFields(distCombo.getValue()));

        controller.setPlotButton(new Button("Generate Contour Plot"));
        plotButton = controller.getPlotButton();
        plotButton.setStyle("-fx-background-color: #33BBAD; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 4 8;");
        plotButton.setOnAction(e -> {
            try {
                controller.generateContourPlot();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        showPlotUsingSmile = new Button("Show Plot(Smile builder)");
        showPlotUsingSmile.setVisible(false);
        showPlotUsingSmile.setStyle("-fx-background-color: #33BBAD; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 4 8;");
        showPlotUsingSmile.setOnAction(e -> {
            try {
                controller.showSmilePlot();
            } catch (Exception ex) {
                controller.showError("Smile plot error: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        To3DPlotterButton = new Button("To 3D Plotter");
        To3DPlotterButton.setStyle("-fx-background-color: #33BBAD; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 4 8;");
        To3DPlotterButton.setOnAction(e -> {
            if (ThreeDimensionalPlotterStage != null && ThreeDimensionalPlotterStage.isShowing()) {
                ThreeDimensionalPlotterStage.toFront();
                ThreeDimensionalPlotterStage.requestFocus();
                return;
            }
            ThreeDimensionalPlotterStage = new Stage();
            EtaFXPlotter3D Plotter3D = new EtaFXPlotter3D();
            try {
                Plotter3D.start(ThreeDimensionalPlotterStage);
                ThreeDimensionalPlotterStage.setOnHidden(event -> ThreeDimensionalPlotterStage = null);
            } catch (Exception ex) {
                ThreeDimensionalPlotterStage = null;
                Alert alert = new Alert(Alert.AlertType.ERROR, "Could not open 3D plotter: " + ex.getMessage());
                alert.showAndWait();
            }
        });

        controller.setSaveButton(new Button("Save chart"));
        saveButton = controller.getSaveButton();
        saveButton.setOnAction(e -> {
            try {
                JavaFXCharts.ImageBuilder(canvas, "2D_chart_of_" + distCombo.getValue());
            } catch (Exception ex) {
                controller.showError("Could not create chart: " + ex.getMessage());
            }
        });

        HBox select = new HBox(10, plotButton, To3DPlotterButton, saveButton, showPlotUsingSmile);
        select.setPadding(new Insets(0, 0, 10, 0));

        HBox topBar = new HBox(20, new Label("Distribution:"), distCombo, select);
        topBar.setPadding(new Insets(0, 0, 10, 0));

        return topBar;
    }

    @Override
    protected StackPane ConfigResults() {
        controller.setCanvaSize(600);
        controller.setMargin(60);

        CANVAS_SIZE = controller.getCanvaSize();
        MARGIN = controller.getMargin();

        controller.setCanvas(new Canvas(CANVAS_SIZE + 2 * MARGIN, CANVAS_SIZE + 2 * MARGIN + 40));
        canvas = controller.getCanvas();
        StackPane canvasPane = new StackPane(canvas);
        canvasPane.setStyle("-fx-border-color: white; -fx-border-width: 1;");
        return canvasPane;
    }
}