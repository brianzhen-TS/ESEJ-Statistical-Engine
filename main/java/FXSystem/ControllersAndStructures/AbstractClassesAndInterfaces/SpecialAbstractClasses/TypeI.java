package FXSystem.ControllersAndStructures.AbstractClassesAndInterfaces.SpecialAbstractClasses;

import javafx.application.Application;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * The first abstract class of this package. It was based on the {@link FXSystem.ControllersAndStructures.Controllers.FXContourController}
 * class layouts that contains: <br>
 * - A <b>VBox</b>(in {@code ConfigVBox()} method) contains another two VBoxes: {@code BoxOne()}(Top) and {@code BoxTwo()}
 * (Center) (placed in the left), <br>
 * - A <b>HBox</b>(in {@code TopPanel()} method) that sit at the top of the scene, (placed on the top) <br>
 * - A <b>Pane</b>(in {@code ConfigResults()} method) that outputs the graphical result of a computation. (placed in the center) <br>
 * Note that it only applies to configure a BorderPane root layout.
 */
public abstract class TypeI extends Application {
    @Override
    public void start(Stage primaryStage) {}

    protected abstract VBox BoxOne();
    protected abstract VBox BoxTwo();
    protected abstract VBox ConfigVBox();

    protected abstract HBox TopPanel();

    protected abstract Pane ConfigResults();
}
