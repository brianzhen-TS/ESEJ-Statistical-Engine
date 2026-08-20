package FXSystem.ControllersAndStructures.AbstractClassesAndInterfaces;

import FXSystem.ControllersAndStructures.AbstractClassesAndInterfaces.Properties.Computable;
import FXSystem.ControllersAndStructures.AbstractClassesAndInterfaces.Properties.Extendable;
import FXSystem.ControllersAndStructures.AbstractClassesAndInterfaces.Properties.Extractable;
import FXSystem.ControllersAndStructures.Controllers.Initialize.FXSinglePlotterController;
import eta.util.DistMap;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

/**
 * The main abstraction class of the FXSystem one-dimensional applications controllers ({@link FXSinglePlotterController},
 * {@link FXSystem.ControllersAndStructures.Controllers.FXCompareMultiController},
 * {@link FXSystem.ControllersAndStructures.Controllers.FXRegressionPlotterController}) and the standard abstraction class
 * of ESEJ FXSystem GUI. Though not all classes uses it ({@link FXSystem.Comparators.EtaFXDistributionComparatorTwo} didn't use
 * it due to its designing purpose), it makes building extensions from the class extends this abstract class much easier.
 * by just writing:
 * <pre>
 * {@code // Code example from FXSystem.SinglePlotAndInit.EtaFXPlotter.java
 * package FXSystem.SinglePlotAndInit;
 *
 * // imports...
 *
 * // Documentations...
 *
 * public class EtaFXPlotter extends Application {
 *     @Override
 *     public void start(Stage stage) throws Exception {
 *         FXSinglePlotterController controller = new FXSinglePlotterController();  // constructor of the extended class
 *         try {
 *             stage = controller.buildStage();
 *             stage.show();
 *         } catch (Exception | Error ex) {
 *             RuntimeAnomalyError error = new RuntimeAnomalyError(ex.getCause());
 *             controller.showError(error.getMessage());
 *         }
 *     }
 * }
 * }
 * </pre>
 * and enables faster development speed when constructing the controller components and better ease of debugging. This class also implements some interfaces(or
 * I call them "properties") which extends its functionalities and optimizing its design when major extensions are added
 * into this class, and it also extends {@link PlottersFXComponents} for implementing the components to this class. See
 * its documentation for more details.
 * @see PlottersFXComponents
 * @see FXSystem.ControllersAndStructures.Controllers.FXCompareMultiController
 * @see FXSinglePlotterController
 * @see FXSystem.ControllersAndStructures.Controllers.FXRegressionPlotterController
 * @since ESEJ 1.0
 * */
public abstract class PlottersFXMainController extends PlottersFXComponents implements Computable, Extendable, Extractable {
    /**
     * Builds the application with the following default settings. See the implemented class for their details.
     * @see FXSystem.ControllersAndStructures.Controllers.FXCompareMultiController
     */
    protected BorderPane createRoot() {
        return new BorderPane();
    }

    /**
     * Builds the application with/without the default settings depending on the user input.
     * @param SingleApplications Is it for a single-page applications or not.
     */
    protected BorderPane createRoot(boolean SingleApplications) {
        return new BorderPane();
    }

    protected abstract Image loadImage();
    protected Image setSplashScene() {
        return new Image("");
    }
    public Stage buildStage() {
        return new Stage();
    }

    @Override
    public void compute() {
        Computable.super.compute();
    }
    @Override
    public Map<String, Double> ConfigParams() {
        return Computable.super.ConfigParams();
    }
    @Override
    public void getInputs() {
        Computable.super.getInputs();
    }

    @Override
    protected String defaultValue(String name) {
        return name;
    }
    @Override
    protected abstract void showError(String message);

    @Override
    public Stage buildSinglePageStage() {
        return new Stage();
    }
    @Override
    public BorderPane SinglePageBorderPane() {
        return new BorderPane();
    }
    @Override
    public Map<String, Double> ConvertMap(DistMap map) {
        return new HashMap<>(map);
    }
    @Override
    public DistMap InsertMap(Map<String, Double> map) {
        return new DistMap(map);
    }
    @Override
    public void MapIO() {}
}
