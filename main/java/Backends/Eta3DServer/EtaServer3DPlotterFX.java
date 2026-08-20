package Backends.Eta3DServer;

import Link.*;
import Link.ExceptionStat.SystemException.*;
import Link.Parameter;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.CullFace;

import Link.MultivariateDistribution;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;

public class EtaServer3DPlotterFX {

    public static double[][] computeGrid(String distName,
                                         Map<String, Double> params,
                                         double xMin, double xMax,
                                         double yMin, double yMax,
                                         int nx, int ny) throws Exception {
        if (nx < 2 || ny < 2) throw new InvalidInputException("Grid size must be at least 2x2.");
        if (xMin >= xMax || yMin >= yMax) throw new IllegalArgumentException("Invalid axis ranges");

        MultivariateDistribution dist = MultivariateDistributionRegistry.get(distName);
        if (dist == null) {
            throw new NoSuchDistributionException("Link.Distribution not found: " + distName);
        }

        for (Parameter p : dist.getParameters()) {
            if (!params.containsKey(p.name())) {
                throw new IllegalArgumentException("Missing parameter " + p.name());
            }
        }

        double[][] grid = new double[nx][ny];
        double xStep = (xMax - xMin) / (nx - 1);
        double yStep = (yMax - yMin) / (ny - 1);
        IntStream.range(0, nx).parallel().forEach(i -> {
            double x = xMin + i * xStep;
            for (int j = 0; j < ny; j++) {
                double y = yMin + j * yStep;   // FIXED
                grid[i][j] = dist.density(new double[]{x, y}, params);
            }
        });
        return grid;
    }

    public static TriangleMesh buildMesh(double[][] grid,
                                         double xMin, double xMax,
                                         double yMin, double yMax,
                                         double heightScale) {
        int nx = grid.length;
        int ny = grid[0].length;

        float[] points = new float[nx * ny * 3];
        float[] texCoords = new float[nx * ny * 2];
        int[] faces = new int[(nx - 1) * (ny - 1) * 12];

        int pIdx = 0, tIdx = 0, fIdx = 0;

        // Build vertices: (x, density, y) – density as height (y-axis)
        for (int i = 0; i < nx; i++) {
            double x = xMin + (xMax - xMin) * i / (nx - 1);
            for (int j = 0; j < ny; j++) {
                double y = yMin + (yMax - yMin) * j / (ny - 1);
                double z = grid[i][j] * heightScale; // density value
                points[pIdx++] = (float) x;
                points[pIdx++] = (float) z; // height (density)
                points[pIdx++] = (float) y;
                texCoords[tIdx++] = (float) i / (nx - 1);
                texCoords[tIdx++] = (float) j / (ny - 1);
            }
        }

        // Build faces (two triangles per grid cell)
        for (int i = 0; i < nx - 1; i++) {
            for (int j = 0; j < ny - 1; j++) {
                int topLeft = i * ny + j;
                int topRight = (i + 1) * ny + j;   // FIXED
                int bottomLeft = i * ny + (j + 1);
                int bottomRight = (i + 1) * ny + (j + 1);

                // Triangle 1: topLeft, bottomLeft, topRight
                faces[fIdx++] = topLeft;
                faces[fIdx++] = topLeft;
                faces[fIdx++] = bottomLeft;
                faces[fIdx++] = bottomLeft;
                faces[fIdx++] = topRight;
                faces[fIdx++] = topRight;

                // Triangle 2: topRight, bottomLeft, bottomRight
                faces[fIdx++] = topRight;
                faces[fIdx++] = topRight;
                faces[fIdx++] = bottomLeft;
                faces[fIdx++] = bottomLeft;
                faces[fIdx++] = bottomRight;
                faces[fIdx++] = bottomRight;
            }
        }

        if (fIdx != faces.length) {
            throw new RuntimeException("Face index mismatch: wrote " + fIdx + " but array length is " + faces.length);
        }

        TriangleMesh mesh = new TriangleMesh();
        mesh.getPoints().addAll(points);
        mesh.getTexCoords().addAll(texCoords);
        mesh.getFaces().addAll(faces);
        return mesh;
    }

    /**
     * Computes face normals for a TriangleMesh and adds them to the mesh.
     */
    private static void computeNormals(TriangleMesh mesh) {
        float[] points = new float[mesh.getPoints().size()];
        mesh.getPoints().toArray(points);
        int[] faces = new int[mesh.getFaces().size()];
        mesh.getFaces().toArray(faces);

        float[] normals = new float[faces.length / 2 * 3];
        int nIdx = 0;
        for (int i = 0; i < faces.length; i += 6) {
            int p1 = faces[i] * 3;
            int p2 = faces[i + 2] * 3;
            int p3 = faces[i + 4] * 3;
            float x1 = points[p1], y1 = points[p1 + 1], z1 = points[p1 + 2];
            float x2 = points[p2], y2 = points[p2 + 1], z2 = points[p2 + 2];
            float x3 = points[p3], y3 = points[p3 + 1], z3 = points[p3 + 2];
            float ux = x2 - x1, uy = y2 - y1, uz = z2 - z1;
            float vx = x3 - x1, vy = y3 - y1, vz = z3 - z1;
            float nx = uy * vz - uz * vy;
            float ny = uz * vx - ux * vz;
            float nz = ux * vy - uy * vx;
            float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
            if (len > 0) { nx /= len; ny /= len; nz /= len; }
            for (int j = 0; j < 3; j++) {
                normals[nIdx++] = nx;
                normals[nIdx++] = ny;
                normals[nIdx++] = nz;
            }
        }
        mesh.getNormals().addAll(normals);
    }

    /**
     * Creates a color texture image from the grid.
     */
    private static Image createColorTexture(double[][] grid, double min, double max, Function<Double, Color> colorMap) {
        int nx = grid.length;
        int ny = grid[0].length;
        WritableImage image = new WritableImage(nx, ny);
        PixelWriter writer = image.getPixelWriter();
        double range = max - min;
        for (int i = 0; i < nx; i++) {
            for (int j = 0; j < ny; j++) {
                double d = grid[i][j];
                double intensity = (d - min) / range;
                Color color = colorMap.apply(intensity);
                writer.setColor(i, j, color);
            }
        }
        return image;
    }

    /**
     * Full 3D surface creator – returns a ready‑to‑use MeshView.
     * @param distName      distribution name
     * @param params        parameter map
     * @param xMin Min range of x
     * @param xMax Max range of x
     * @param yMin Min range of y
     * @param yMax  Max range of y
     * @param nx   number of points in x direction
     * @param ny   number of points in y direction
     * @param heightScale   scaling factor for the vertical axis
     * @param colorMap      function mapping intensity [0..1] to Color
     * @return a MeshView with normals and a diffuse color map applied
     * @throws Exception if grid computation fails
     */
    public static MeshView createSurface(String distName,
                                         Map<String, Double> params,
                                         double xMin, double xMax,
                                         double yMin, double yMax,
                                         int nx, int ny,
                                         double heightScale,
                                         Function<Double, Color> colorMap) throws Exception {
        // 1. Compute grid
        double[][] grid = computeGrid(distName, params, xMin, xMax, yMin, yMax, nx, ny);

        // 2. Find min/max density
        double minDensity = Double.POSITIVE_INFINITY;
        double maxDensity = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < nx; i++) {
            for (int j = 0; j < ny; j++) {
                double v = grid[i][j];
                if (v < minDensity) minDensity = v;
                if (v > maxDensity) maxDensity = v;
            }
        }

        double range = maxDensity - minDensity;
        // Auto-scale if heightScale <= 0
        if (heightScale <= 0) {
            heightScale = 5.0 / range;   // aim for height ~5 units
            if (heightScale > 200) heightScale = 200; // clamp
        }

        if (!Double.isFinite(maxDensity) || !Double.isFinite(minDensity) || maxDensity == minDensity) {
            throw new ArithmeticException("Invalid density range (all zero or constant).");
        }

        // 3. Build mesh
        TriangleMesh mesh = buildMesh(grid, xMin, xMax, yMin, yMax, heightScale);
        computeNormals(mesh);

        // 4. Create texture
        Image texture = createColorTexture(grid, minDensity, maxDensity, colorMap);

        // 5. Build material and MeshView
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseMap(texture);
        material.setDiffuseColor(Color.WHITE); // fallback

        MeshView meshView = new MeshView(mesh);
        meshView.setMaterial(material);
        meshView.setCullFace(CullFace.NONE);

        return meshView;
    }
}