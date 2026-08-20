package Backends.DataIO;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.image.WritableImage;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.imageio.ImageIO;
import javax.swing.text.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JavaFXToFilesIO {
    public static class Excel {
        public Excel() {}

        /**
         * Export a single series (x, y) to an Excel file.
         * @param xValues   double array of x coordinates
         * @param yValues   double array of y coordinates (same length)
         * @param seriesName column header for y
         * @param filePath  output path (must end with .xlsx)
         * @param sheetName name of the sheet
         */
        public void exportXY(double[] xValues, double[] yValues, String seriesName,
                             String filePath, String sheetName) throws IOException {
            if (xValues.length != yValues.length) {
                throw new IllegalArgumentException("x and y arrays must have same length");
            }
            // Build data map (LinkedHashMap preserves order)
            Map<String, List<Double>> data = new LinkedHashMap<>();
            List<Double> xList = new ArrayList<>(xValues.length);
            List<Double> yList = new ArrayList<>(yValues.length);
            for (int i = 0; i < xValues.length; i++) {
                xList.add(xValues[i]);
                yList.add(yValues[i]);
            }
            data.put("X", xList);
            data.put(seriesName, yList);
            exportData(data, filePath, sheetName);
        }

        /**
         * Export multiple series (x, y1, y2, ...) to Excel.
         * @param xValues     double array of x
         * @param yValuesList list of double arrays (each representing a series)
         * @param seriesNames list of column headers for each y series
         * @param filePath    output path
         * @param sheetName   sheet name
         */
        public void exportXYMultiple(double[] xValues, List<double[]> yValuesList,
                                     List<String> seriesNames, String filePath, String sheetName) throws IOException {
            if (yValuesList.size() != seriesNames.size()) {
                throw new IllegalArgumentException("Number of y series must match number of series names");
            }
            Map<String, List<Double>> data = new LinkedHashMap<>();
            // Add X
            List<Double> xList = new ArrayList<>(xValues.length);
            for (double v : xValues) xList.add(v);
            data.put("X", xList);
            // Add each y series
            for (int i = 0; i < yValuesList.size(); i++) {
                double[] y = yValuesList.get(i);
                if (y.length != xValues.length) {
                    throw new IllegalArgumentException("All y arrays must have same length as x");
                }
                List<Double> yList = new ArrayList<>(y.length);
                for (double v : y) yList.add(v);
                data.put(seriesNames.get(i), yList);
            }
            exportData(data, filePath, sheetName);
        }

        // Internal: write map to Excel
        private void exportData(Map<String, List<Double>> data, String filePath, String sheetName) throws IOException {
            // Debug: print first few values
            String[] headers = data.keySet().toArray(new String[0]);
            int rowCount = data.get(headers[0]).size();
            boolean debug = false;
            if (debug) {
                System.out.println("Exporting to Excel: " + filePath);
                System.out.println("Headers: " + String.join(", ", headers));
                System.out.println("Row count: " + rowCount);
                for (int i = 0; i < rowCount; i++) {
                    IO.println(String.format("X val %d: %f; Y val %d: %f", i, data.get(headers[0]).get(i), i, data.get(headers[1]).get(i)));
                }
            }

            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet(sheetName);

                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < headers.length; i++) {
                    headerRow.createCell(i).setCellValue(headers[i]);
                }

                for (int i = 0; i < rowCount; i++) {
                    Row row = sheet.createRow(i + 1);
                    for (int j = 0; j < headers.length; j++) {
                        List<Double> col = data.get(headers[j]);
                        if (col != null && i < col.size()) {
                            row.createCell(j).setCellValue(col.get(i));
                        }
                    }
                }

                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                    workbook.write(fileOut);
                }
            }
        }
    }

    public static class PDF {
        public PDF() {}

        public void exportChartToPDF(Node node, String title, String filePath) throws IOException {
            WritableImage image = node.snapshot(null, null);
            BufferedImage bI = SwingFXUtils.fromFXImage(image, null);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(bI, "png", out);
            byte[] imageBytes = out.toByteArray();

            PdfWriter writer = new PdfWriter(new FileOutputStream(filePath));
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            document.add(new Paragraph(title).setFontSize(20));

            Image pdfImage = new Image(ImageDataFactory.create(imageBytes));
            float pageWidth = pdfDoc.getDefaultPageSize().getWidth() - 72;
            float pageHeight = pdfDoc.getDefaultPageSize().getHeight() - 72;
            pdfImage.scaleToFit(pageWidth, pageHeight);
            document.add(pdfImage);

            document.close();
        }

        public void exportDataTableToPDF(Map<String, List<Double>> data, String title, String filePath) throws IOException {
            PdfWriter writer = new PdfWriter(new FileOutputStream(filePath));
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, PageSize.A4.rotate());

            document.add(new Paragraph(title).setFontSize(20));

            int colCount = data.size();
            if (colCount == 0) {
                document.add(new Paragraph("No data"));
                document.close();
                return;
            }

            Table table = new Table(colCount);
            table.setWidth(UnitValue.createPercentValue(100));

            String[] headers = data.keySet().toArray(new String[0]);
            for (String h : headers) {
                table.addCell(h);
            }

            int rowCount = data.get(headers[0]).size();
            for (int i = 0; i < rowCount; i++) {
                for (String h : headers) {
                    List<Double> col = data.get(h);
                    Double value = (i < col.size()) ? col.get(i) : null;
                    table.addCell(value != null ? value.toString() : "");
                }
            }

            document.add(table);
            document.close();
        }

        public void exportChartAndData(Node chartNode, Map<String, List<Double>> data,
                                       String title, String filePath) throws IOException {
            PdfWriter writer = new PdfWriter(new FileOutputStream(filePath));
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, PageSize.A4);

            document.add(new Paragraph(title).setFontSize(18));

            WritableImage snapshot = chartNode.snapshot(null, null);
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(snapshot, null);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);
            Image pdfImage = new Image(com.itextpdf.io.image.ImageDataFactory.create(baos.toByteArray()));
            pdfImage.scaleToFit(500, 300);
            document.add(pdfImage);

            document.add(new Paragraph("Data Table").setFontSize(14));
            Table table = new Table(data.size());
            String[] headers = data.keySet().toArray(new String[0]);
            for (String h : headers) table.addCell(h);
            int rowCount = data.get(headers[0]).size();
            for (int i = 0; i < rowCount; i++) {
                for (String h : headers) {
                    table.addCell(data.get(h).get(i).toString());
                }
            }
            document.add(table);

            document.close();
        }
    }
}
