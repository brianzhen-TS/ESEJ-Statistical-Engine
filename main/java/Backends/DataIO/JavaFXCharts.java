package Backends.DataIO;

import Link.ExceptionStat.SystemError.RuntimeAnomalyError;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.Chart;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.embed.swing.SwingFXUtils;
import org.jfree.chart.JFreeChart;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@SuppressWarnings("ALL")
public class JavaFXCharts {
    public static void ImageBuilder(Chart chart, String name) {
        Stage stage = new Stage();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName(name);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png"));
        IO.println("Image I/O: builder: ");
        IO.println("Saving file, go to the save dialog to continue.\n");
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try {
                WritableImage image = chart.snapshot(null, null);
                BufferedImage bI = SwingFXUtils.fromFXImage(image, null);
                ImageIO.write(bI, "png", file);
                IO.println(String.format("""
                        Chart Image saved to: %s
                        Thank you for using the Image writer
                        """, file.getAbsolutePath()));
            } catch (IOException e) {
                throw new RuntimeAnomalyError("Anomaly occurred when generating the image: " + e.getMessage(), e.getCause());
            }
        }
        IO.println("Operation complete.");
    }

    public static void ImageBuilder(JFreeChart chart, String name) {
        Stage stage = new Stage();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName(name);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png"));
        IO.println("Image I/O: builder: ");
        IO.println("Saving file, go to the save dialog to continue.\n");
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try {
                BufferedImage bI = chart.createBufferedImage(600, 600);
                ImageIO.write(bI, "png", file);
                IO.println(String.format("""
                        Chart Image saved to: %s
                        Thank you for using the Image writer
                        """, file.getAbsolutePath()));
            } catch (IOException e) {
                throw new RuntimeAnomalyError("Anomaly occurred when generating the image: " + e.getMessage(), e.getCause());
            }
        }
        IO.println("Operation complete.");
    }

    public static void ImageBuilder(Canvas canvas, String name) {
        Stage stage = new Stage();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName(name);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png"));
        IO.println("Image I/O: builder: ");
        IO.println("Saving canva file, go to the save dialog to continue.\n");
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try {
                WritableImage image = canvas.snapshot(null, null);
                BufferedImage bI = SwingFXUtils.fromFXImage(image, null);
                ImageIO.write(bI, "png", file);
                IO.println(String.format("""
                        Canva Image saved to: %s
                        Thank you for using the Image writer
                """, file.getAbsolutePath()));
            } catch (IOException e) {
                throw new RuntimeAnomalyError("Anomaly occurred when generating the canva image: " + e.getMessage(), e.getCause());
            }
        }
        IO.println("Operation complete.");
    }
}
