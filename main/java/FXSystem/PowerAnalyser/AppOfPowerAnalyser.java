package FXSystem.PowerAnalyser;

import FXSystem.Comparators.EtaFXDistributionComparatorTwo;
import FXSystem.ControllersAndStructures.Controllers.EtaFXPowerAnalyzerController;
import Link.ExceptionStat.SystemError.RuntimeAnomalyError;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
/**
 * Part of the FXSystem. It adds power analysis capabilities to the GUI, allowing users to analyze the power of
 * a specific hypothesis test and determine the statistical significance of a given null hypothesis. <br>
 * Tests include: <br>
 * - T test for two independent samples <br>
 * - Chi-squared tests(goodness-of-fit, contingency tables, single-sample variance test) <br>
 * - F tests(two-samples variance, one-way-ANOVA)
 * <h3>How to use it</h3>
 * <p>
 * <b>1.</b> After opening from {@link FXSystem.SinglePlotAndInit.EtaFXPlotter}, You'll see two combo boxes above, select
 * the test family and select the subtest you want to perform and enter you data in the "Data input" text area. <br>
 * <b>Note:</b> there's also "Additional parameters" section in below the data input area. If it pops something out, you have to
 * enter the required parameters. <br>
 * <b>2.</b> Click "Compute Power". After that the formatted results will display in the "Results" text area.
 * <b>Note:</b> this application assumes that the given alpha level is 0.05 (the default), so be careful while evaluating
 * the results.
 * </p>
 * @see FXSystem.ControllersAndStructures.Controllers.EtaFXPowerAnalyzerController
 * @see FXSystem.SinglePlotAndInit.EtaFXPlotter
 * @since ESEJ 1.0*/
public class AppOfPowerAnalyser extends Application {
    @Override
    public void start(Stage stage) {
        try {
            EtaFXPowerAnalyzerController powerAnalyzer = new EtaFXPowerAnalyzerController();

            Scene scene = new Scene(powerAnalyzer, 900, 700);
            scene.getStylesheets().add("FXSystem/Comparators/Comparator.css");

            stage.setTitle("Eta Power Analyser");
            stage.setScene(scene);

            Image icon = loadIcon();
            stage.getIcons().add(icon);

            stage.show();

            stage.setOnCloseRequest(_ -> EtaFXPowerAnalyzerController.shutdownExecutor());
        } catch (Exception e) {
            RuntimeAnomalyError error = new RuntimeAnomalyError(e);
            Alert alert = new Alert(Alert.AlertType.ERROR, error.getMessage(), ButtonType.OK);
            alert.showAndWait();
        }
    }

    private static Image loadIcon() {
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
