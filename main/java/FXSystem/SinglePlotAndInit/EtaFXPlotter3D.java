package FXSystem.SinglePlotAndInit;

import FXSystem.ControllersAndStructures.Controllers.FX3DPlotterController;
import Link.*;
import Link.ExceptionStat.SystemError.RuntimeAnomalyError;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Objects;

/**
 * The 3D plotter variant of {@link EtaFXPlotterContour} with 3D visualization capability and able to generate interactive
 * charts. Part of the FXSystem.<br>
 * Provides: <br>
 * - A full interactive UI to select distributions, set parameters, and plot.<br>
 * - Able to generate contour plots and save chart(as .png file) of multivariate distributions.
 * <h3>How to use it</h3>
 * <b>1.</b> After opened from {@link EtaFXPlotterContour}, enter the required parameters for the chosen distribution from
 * the combo box. This procedure is the same as {@link EtaFXPlotterContour}, so see its documentation for details. <br>
 * <b>2.</b> Configure the range of inputs (<b>min to max</b>) and how smooth your output was (<b>x points</b> and <b>y points</b>).
 * <br>
 * <b>3.</b> Click "Generate 3D Surface" to generate the result.
 * @see EtaFXPlotter
 * @see EtaFXPlotterContour
 * @since ESEJ 1.0*/
@SuppressWarnings("ALL")
public class EtaFXPlotter3D extends Application {

    @Override
    public void start(Stage stage) {
        FX3DPlotterController controller = new FX3DPlotterController();
        try {
            BorderPane root = controller.root();
            // ----- Scene -----
            Scene scene = new Scene(root, 1200, 800);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/FXSystem/SinglePlotAndInit/Plotter.css")).toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Eta 3D Surface Plotter");
            Image icon = loadIcon();
            stage.getIcons().add(icon);
            stage.show();
        } catch (Exception e) {
            RuntimeAnomalyError error = new RuntimeAnomalyError(e);
            controller.showError(error.getMessage());
        }
    }

    private static Image loadIcon() {
        InputStream is = EtaFXPlotter.class.getResourceAsStream("/FXSystem/SinglePlotAndInit/eta-icon.jpeg");
        if (is == null) is = EtaFXPlotter.class.getResourceAsStream("/WhatsApp%20Image%202026-06-10%20at%2015.27.48.jpeg");
        if (is == null) {
            try {
                return new Image(new FileInputStream("FXSystem/SinglePlotAndInit/eta-icon.jpeg"));
            } catch (Exception e) {
                try {
                    return new Image(new FileInputStream("WhatsApp Image 2026-06-10 at 15.27.48.jpeg"));
                } catch (Exception ex) {
                    IO.println("Warning: Could not load icon image. Using default.");
                    return null;
                }
            }
        }
        return new Image(is);
    }
}