package FXSystem.Comparators;

import FXSystem.ControllersAndStructures.Controllers.FXCompareMultiController;
import Link.*;
import Link.ExceptionStat.SystemError.RuntimeAnomalyError;
import javafx.application.Application;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.*;

/**
 * The multi-comparator page of the FXSystem GUI. It extends {@link EtaFXDistributionComparatorTwo} to support multiple
 * given distributions comparing, but without the ECDF fitting capabilities because it might make the result looks messy
 * and unrecognizable. This comparator can compare up to 10 distributions at a time. (exceeding that limit will throw
 * {@link RuntimeAnomalyError}). This comparator allow users to save the chart as .png file. but currently does not support
 * Excel currently(same as {@link EtaFXDistributionComparatorTwo}).
 * <h3>How to use it</h3>
 * <p>
 * <b>1.</b> After the page was opened (From {@link EtaFXDistributionComparatorTwo}; see its documentation for details),
 * select and add distributions you want to compare. You can adjust the counts via "+ Add Distribution" and "- Remove Last"
 * buttons (up to 10; as mensioned above), but note that you may have to scroll down to see them. Remember to update your
 * parameter settings before computing. <br>
 * <b>2.</b> Choose the distribution type and configure the input ranges. After that, click "Compare". <br>
 * <b>3.</b> Click "Save chart" in the control grid to export the chart as .png file.
 * </p>
 * @see EtaFXDistributionComparatorTwo
 * @since ESEJ 1.0
 */
@SuppressWarnings("ALL")
public class EtaFXDistributionComparatorMulti extends Application {

    @Override
    public void start(Stage stage) {
        FXCompareMultiController controller = new FXCompareMultiController();
        try {
            stage = controller.createStage();
            stage.show();
        } catch (Exception e) {
            RuntimeAnomalyError error = new RuntimeAnomalyError(e.getMessage(), e.getCause());
            controller.showError(e.getMessage());
            e.printStackTrace();
        }
    }
}