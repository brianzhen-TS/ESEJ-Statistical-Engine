package FXSystem.ControllersAndStructures.AbstractClassesAndInterfaces.LayoutsExtra;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

public class InsertableGrid extends GridPane {
    public static GridPane InsertViaRows(Region[]... rows) {
        GridPane gridPane = new GridPane();
        for (int r = 0; r < rows.length; r++) {
            for (int c = 0; c < rows[r].length; c++) {
                gridPane.add(rows[r][c], c, r);
            }
        }
        return gridPane;
    }

    public static GridPane InsertViaColumns(Region[]... columns) {
        GridPane gridPane = new GridPane();
        for (int c = 0; c < columns.length; c++) {
            for (int r = 0; r < columns[c].length; r++) {
                gridPane.add(columns[c][r], c, r);
            }
        }
        return gridPane;
    }
}
