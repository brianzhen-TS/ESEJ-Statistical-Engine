package FXSystem.ControllersAndStructures.AbstractClassesAndInterfaces.SpecialAbstractClasses;

/**
 * DTO class for defining the size of the implemented {@link TypeI} stages.
 */
public class TypeIStageSize {
    private int height;
    private int width;

    /**
     * Set the size of the stage with the given values(height & Width).
     */
    public TypeIStageSize(int height, int width) {
        this.height = height;
        this.width = width;
    }

    /**
     * Set the size of the stage with the default values: <br>
     * - Height: 800, <br>
     * - Width: 1100.
     */
    public TypeIStageSize() {
        this.height = 800;
        this.width = 1100;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }
}
