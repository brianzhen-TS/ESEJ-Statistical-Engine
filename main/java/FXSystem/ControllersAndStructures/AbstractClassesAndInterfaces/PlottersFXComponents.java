package FXSystem.ControllersAndStructures.AbstractClassesAndInterfaces;

import javafx.application.Application;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * The abstract class of the following pages components: <br>
 * - {@link FXSystem.ControllersAndStructures.Controllers.FX3DPlotterController} <br>
 * - {@link FXSystem.Comparators.EtaFXDistributionComparatorTwo} <br>
 * - Extended by {@link PlottersFXMainController} <br>
 * Which provides more control on their structure and makes them easier to maintain. This class contains basic components
 * for the application's structure, and it was one of the main components of {@link PlottersFXMainController}.
 * @see FXSystem.SinglePlotAndInit.EtaFXPlotter
 * @see FXSystem.SinglePlotAndInit.EtaFXPlotter3D
 * @see FXSystem.Comparators.EtaFXDistributionComparatorTwo
 * @see PlottersFXMainController
 * @since ESEJ 1.0
 */
public abstract class PlottersFXComponents extends Application {
    // for all files in FXSystem
    public void start(Stage primaryStage) throws Exception {}
    protected abstract String defaultValue(String name);
    protected abstract void showError(String message);

    // For EtaFXPlotter.java
    protected BorderPane createMainUI() {
        return new BorderPane();
    }
    protected void updateParameterFields() {}
}
