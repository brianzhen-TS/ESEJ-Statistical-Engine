package FXSystem.Fitters;

import FXSystem.ControllersAndStructures.Controllers.FXRegressionPlotterController;
import Link.ExceptionStat.SystemError.RuntimeAnomalyError;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * A page of FXSystem GUI which provides regression visualizing into the application. It can perform ordinary least square
 * for a given x-coordinate and y-coordinate input, and it able users to generate a chart and save it as .png file.
 * This page has the similar layouts compared to
 * {@link FXSystem.SinglePlotAndInit.EtaFXPlotterContour} because they have used the same abstract class for configuring
 * their layouts, which simplifies the design.
 * <h3>How to use it</h3>
 * <p>
 * <b>1.</b> After opened from {@link FXSystem.SinglePlotAndInit.EtaFXPlotter}, You'll see two textAreas on the left,
 * the chart at the center, and the control panel on the top. In the input section(the textAreas), enter your data plot
 * xy-coordinates. <br>
 * <b>2.</b> Press "Generate Plot" to generate the plot with the given data. If the plotting was success, it will generate
 * a regression line with the plot given. After that, you can press "Save Chart" to export it as .png file.
 * </p>
 * @see FXSystem.SinglePlotAndInit.EtaFXPlotter
 * @see FXSystem.SinglePlotAndInit.EtaFXPlotterContour
 * @since ESEJ 1.0*/
public class FXRegressionApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        FXRegressionPlotterController controller = new FXRegressionPlotterController();
        try {
            primaryStage = controller.buildStage();
            primaryStage.show();
        } catch (Exception e) {
            RuntimeAnomalyError error = new RuntimeAnomalyError(e);
            controller.showError(error.getMessage());
        }
    }
}
