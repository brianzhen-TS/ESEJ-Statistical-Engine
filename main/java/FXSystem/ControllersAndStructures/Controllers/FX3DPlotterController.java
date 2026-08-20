package FXSystem.ControllersAndStructures.Controllers;

import Backends.Eta3DServer.EtaServer3DPlotterFX;
import FXSystem.ControllersAndStructures.AbstractClassesAndInterfaces.PlottersFXComponents;
import Link.MultivariateDistribution;
import Link.MultivariateDistributionRegistry;
import Link.Parameter;
import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

import java.util.HashMap;
import java.util.Map;

/** @Supports {@link FXSystem.SinglePlotAndInit.EtaFXPlotter3D}*/
public class FX3DPlotterController extends PlottersFXComponents {
    // UI components
    private ComboBox<String> distCombo;
    private GridPane paramGrid;
    private Map<String, TextField> paramFields = new HashMap<>();

    private TextField xMinField, xMaxField, yMinField, yMaxField;
    private TextField xPointsField, yPointsField;
    private CheckBox wireframeCheck;
    private Button plotButton, resetViewButton;
    private Label statusLabel;

    private SubScene subScene;
    private Group root3D;
    private MeshView meshView;
    private PerspectiveCamera camera;

    // Mouse rotation state
    private double mousePosX, mousePosY;
    private Rotate xRotate;
    private Rotate yRotate;

    private final int DEFAULT_GRID = 150;

    // methods
    public BorderPane root() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // ----- Top: distribution selection -----
        distCombo = new ComboBox<>();
        for (MultivariateDistribution d : MultivariateDistributionRegistry.getAll()) {
            distCombo.getItems().add(d.getName());
        }
        if (!distCombo.getItems().isEmpty()) {
            distCombo.setValue(distCombo.getItems().getFirst());
        }
        distCombo.setOnAction(_ -> updateParameterFields(distCombo.getValue()));
        distCombo.getStyleClass().add("dist-combo");

        plotButton = new Button("Generate 3D Surface");
        plotButton.setStyle("-fx-background-color: #33BBAD; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 4 8;");
        plotButton.setOnAction(_ -> generate3DPlot());

        HBox topBar = new HBox(20, new Label("Distribution:"), distCombo, plotButton);
        topBar.setPadding(new Insets(0, 0, 10, 0));
        root.setTop(topBar);

        // ----- Left: parameter panel -----
        paramGrid = new GridPane();
        paramGrid.setHgap(10);
        paramGrid.setVgap(5);
        paramGrid.setPadding(new Insets(10));
        paramGrid.setStyle("-fx-border-color: lightgray; -fx-border-width: 1;");
        ScrollPane paramScroll = new ScrollPane(paramGrid);
        paramScroll.setFitToWidth(true);
        paramScroll.setPrefWidth(300);

        // ----- Center: 3D scene -----
        root3D = new Group();
        meshView = new MeshView();

        xRotate = new Rotate(0, Rotate.X_AXIS);
        yRotate = new Rotate(0, Rotate.Y_AXIS);

        root3D.getChildren().add(meshView);
        root3D.getTransforms().addAll(xRotate, yRotate);

        AmbientLight ambientLight = new AmbientLight(Color.WHITE);
        root3D.getChildren().add(ambientLight);

        PointLight pointLight = new PointLight(Color.WHITE);
        pointLight.setTranslateX(100);
        pointLight.setTranslateY(-100);
        pointLight.setTranslateZ(200);
        root3D.getChildren().add(pointLight);

        subScene = new SubScene(root3D, 800, 600, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.LIGHTGRAY);

        camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(2000);
        camera.getTransforms().addAll(
                new Translate(0, 0, -500),
                new Rotate(20, Rotate.X_AXIS),
                new Rotate(-30, Rotate.Y_AXIS)
        );
        subScene.setCamera(camera);

        subScene.setOnMousePressed(event -> {
            mousePosX = event.getSceneX();
            mousePosY = event.getSceneY();
        });

        subScene.setOnMouseDragged(event -> {
            double deltaX = event.getSceneX() - mousePosX;
            double deltaY = event.getSceneY() - mousePosY;
            mousePosX = event.getSceneX();
            mousePosY = event.getSceneY();
            xRotate.setAngle(xRotate.getAngle() - deltaY);
            yRotate.setAngle(yRotate.getAngle() - deltaX);
        });

        subScene.setOnScroll(event -> {
            double zoom = 1.0 + (event.getDeltaY() > 0 ? 0.05 : -0.05);
            camera.translateZProperty().set(camera.getTranslateZ() + (camera.getTranslateZ() * zoom));
        });

        StackPane centerPane = new StackPane(subScene);
        centerPane.setStyle("-fx-border-color: white; -fx-border-width: 1;");
        root.setCenter(centerPane);

        GridPane grid1 = new GridPane();
        GridPane grid2 = new GridPane();

        xMinField = new TextField("-3");
        xMaxField = new TextField("3");
        yMinField = new TextField("-3");
        yMaxField = new TextField("3");
        xPointsField = new TextField(String.valueOf(DEFAULT_GRID));
        yPointsField = new TextField(String.valueOf(DEFAULT_GRID));
        wireframeCheck = new CheckBox("Wireframe");
        resetViewButton = new Button("Reset View");
        resetViewButton.setOnAction(_ -> resetCamera());
        statusLabel = new Label("Ready");

        // column 0
        grid1.add(new Label("x range:"), 0, 0);
        grid1.add(new Label("y range:"), 0, 1);
        grid2.add(new Label("x points:"), 0, 0);
        grid2.add(new Label("y points:"), 0, 1);

        // column 1
        grid1.add(xMaxField, 1, 0);
        grid1.add(yMaxField, 1, 1);
        grid2.add(xPointsField, 1, 0);
        grid2.add(yPointsField, 1, 1);

        // column 2
        grid1.add(new Label("to"), 2, 0);  // x
        grid1.add(new Label("to"), 2, 1);  // y

        // column 3
        grid1.add(xMinField, 3, 0);
        grid1.add(yMinField, 3, 1);

        grid1.setVgap(10);
        grid1.setHgap(10);

        grid2.setVgap(10);
        grid2.setHgap(10);

        HBox extras = new HBox(10);
        extras.getChildren().addAll(wireframeCheck, resetViewButton);

        VBox controls = new VBox(10);
        controls.setPadding(new Insets(10, 0, 0, 0));
        controls.getChildren().addAll(statusLabel, grid1, grid2, extras);

        VBox leftPanel = new VBox(5, new Label("Parameters"), paramScroll, controls);
        root.setLeft(leftPanel);

        // Initialize parameters – now using `this`, not a new controller
        if (!distCombo.getItems().isEmpty()) {
            updateParameterFields(distCombo.getValue());
        }

        return root;
    }

    public void resetCamera() {
        camera.setTranslateZ(-50);
        camera.setTranslateY(20);
        camera.setTranslateX(0);
        xRotate.setAngle(25);
        yRotate.setAngle(-35);
        root3D.getTransforms().clear();
        root3D.getTransforms().addAll(xRotate, yRotate);
        if (meshView.getMesh() != null) {
            root3D.getChildren().clear();
            root3D.getChildren().add(meshView);
        }
    }

    public void updateParameterFields(String distName) {
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

    public void generate3DPlot() {
        String distName = distCombo.getValue();
        if (distName == null) return;

        Map<String, Double> params = gatherParameters();
        if (params == null) return;

        double xMin, xMax, yMin, yMax;
        int nx, ny;
        try {
            xMin = Double.parseDouble(xMinField.getText().trim());
            xMax = Double.parseDouble(xMaxField.getText().trim());
            yMin = Double.parseDouble(yMinField.getText().trim());
            yMax = Double.parseDouble(yMaxField.getText().trim());
            nx = Integer.parseInt(xPointsField.getText().trim());
            ny = Integer.parseInt(yPointsField.getText().trim());
            if (nx < 10) nx = 10;
            if (ny < 10) ny = 10;
            if (nx > 400) nx = 400;
            if (ny > 400) ny = 400;
        } catch (NumberFormatException e) {
            showError("Invalid range or points.");
            return;
        }
        if (xMin >= xMax || yMin >= yMax) {
            showError("Invalid axis ranges.");
            return;
        }

        // Validate covariance for Bivariate Normal
        if ("Bivariate Normal".equals(distName)) {
            double varX = params.getOrDefault("varX", 1.0);
            double varY = params.getOrDefault("varY", 1.0);
            double cov = params.getOrDefault("cov", 0.0);
            double det = varX * varY - cov * cov;
            if (det <= 0) {
                statusLabel.setText("Invalid covariance matrix: det = " + det);
                showError("Covariance matrix not positive definite.");
                return;
            }
        }

        try {
            // Use the backend to create the mesh view
            MeshView meshView = EtaServer3DPlotterFX.createSurface(
                    distName, params,
                    xMin, xMax, yMin, yMax,
                    nx, ny,
                    0.0,
                    this::colorMap   // pass the front‑end's color mapping function
            );

            // Clear and add to scene
            root3D.getChildren().clear();
            root3D.getChildren().add(new AmbientLight(Color.WHITE));
            root3D.getChildren().add(meshView);

            // Set camera and status
            camera.getTransforms().clear();
            camera.setTranslateZ(-10);
            camera.setTranslateX(0);
            camera.setTranslateY(0);
            xRotate.setAngle(25);
            yRotate.setAngle(-35);

            statusLabel.setText("3D plot generated: " + distName);
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
            showError(e.getMessage());
            e.printStackTrace();
        }
    }

    private Color colorMap(double intensity) {
        if (intensity < 0) intensity = 0;
        if (intensity > 1) intensity = 1;
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
    }

    @Override
    public void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.showAndWait();
    }
}
