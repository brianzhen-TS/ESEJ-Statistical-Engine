package FXSystem.SinglePlotAndInit;

import FXSystem.ControllersAndStructures.Controllers.Initialize.FXSinglePlotterController;
import Link.ExceptionStat.SystemError.RuntimeAnomalyError;

import FXSystem.DataAnalysers.EtaFXDataAnalyser;
import FXSystem.Comparators.EtaFXDistributionComparatorTwo;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Standalone JavaFX visualizer for the Eta system(former name of ESEJ engine). The main page of the FXSystem app of the ESEJ engine.
 * <p>
 * Provides: <br>
 * - A static {@code plot()} method to show a distribution chart from any context. <br>
 * - A full interactive UI to select distributions, set parameters, and plot. <br>
 * - Able to switch between comparator({@link EtaFXDistributionComparatorTwo}), contour plotter
 * ({@link EtaFXPlotterContour}), power analyzer({@link FXSystem.PowerAnalyser.AppOfPowerAnalyser}), data analyzer({@link EtaFXDataAnalyser})
 * , and Regression visualizer({@link FXSystem.Fitters.FXRegressionApp}). You can access
 * them via the control panel in the bottom-right corner.
 * </p>
 * This page also has its web-server variant, see {@code FXSystemWeb.App.FXComputeSingleSplashSceneWeb} for further information. (currently not included)
 * <h3>How to use it</h3>
 * <p>
 * 1. Start your application. (enter {@code mvn javafx:run} in the terminal) <br>
 * 2. After the splash scene was shown, you'll get transited into the main page. In the left panel of this page, select
 * the name of the distribution you want to plot from the combo box at the top. If you want to access noncentral distributions,
 * press "Switch to Non-Central Distributions" to access them.<br>
 * 3. Enter the parameter in the top of the scroll pane, and you can adjust the range of the plot, the points (which affects
 * the smoothness of the plot), and the type of the distribution(PDF / CDF). <br>
 * 4. Press "Plot" and if all goes well, it will open a new stage showing the chart with the data you've previously entered. If
 * you want to save the chart as .png file, press "Save as PNG" and then it will open the save dialog on your OS. This is
 * similar to "Export to Excel" button if you want to export the plotted data to an Excel file(.xlsx file). <br>
 * 5. Close the chart stage if you want to plot it again. <br>
 * 6. If you want to access more functionalities, click the buttons on the right-bottom grid to open them.
 * </p>
 * @Requirements Install Java SE 25 or later(though this GUI was written using Java SE 26)
 * @Note
 * <p>
 * <b>1.</b> This is the visualization ver. of the console, using JavaFX for coding this plotter and the rest of the system.
 * <b>This is a Maven project, so users must configure this build using Maven.</b>
 * </p>
 * <p>
 * <b>2.</b> If you want to embed it to your project which already has frameworks like spring boot, you may have to use maven
 * for running FXSystem. To do this, you have to run the following code on your terminal: <pre>{@code mvn clean install -DskipTests -e}</pre>
 * or if you want to include testing: <pre>{@code mvn clean install -e}</pre>
 * where -e acts similar to {@code exception.printStackTrace()}. (where exception has type class {@link java.lang.Exception};
 * though this is optional)
 * After that run: <pre>{@code mvn javafx:run}</pre> to run on your project. <br>
 * </p>
 * <b>Because of the complexities, I <span style="color: red"> don't </span> recommend users to add it to your maven
 * project which already has other frameworks(like spring boot that I've mentioned above)on it.</b> Instead, you could put it
 * on a separate project to avoid extra complexities and ruining your original projects. (like running it using Maven or Gradle
 * via configuring its Maven access)
 * @see FXSystem.Comparators.EtaFXDistributionComparatorTwo
 * @see FXSystem.SinglePlotAndInit.EtaFXPlotterContour
 * @see FXSystem.DataAnalysers.EtaFXDataAnalyser
 * @see FXSystem.PowerAnalyser.AppOfPowerAnalyser
 * @see FXSystem.Fitters.FXRegressionApp
 * @since Eta11.5 Java edition (ESEJ 1.0)
 */
public class EtaFXPlotter extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXSinglePlotterController controller = new FXSinglePlotterController();
        try {
            stage = controller.buildStage();
            stage.show();
        } catch (Exception | Error ex) {
            RuntimeAnomalyError error = new RuntimeAnomalyError(ex.getCause());
            controller.showError(error.getMessage());
            error.printStackTrace();
        }
    }
}