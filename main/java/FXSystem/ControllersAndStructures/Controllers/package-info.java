/**
 * The controller package of the FXSystem GUI. It was the backbone of all FXSystem pages and provides all characteristics
 * of the front-end such as the layouts and the functionalities of the front-end. This helps to reduce the code so that
 * only a few lines of code are required in the front-end:
 * <pre>
 * {@code
 * // Example: using a multi-comparator controller
 * public class AppOfESEJ extends Application {
 *     @Override
 *     public void start(Stage stage) {
 *         FXCompareMultiController controller = new FXCompareMultiController();
 *         stage = controller.createStage();
 *         stage.show();
 *     }
 *
 *     // optional
 *     public static void main(String[] args) {
 *         launch(args);
 *     }
 * }
 * }
 * </pre>
 * The controllers don't have any specific descriptions about the details, but I've marked their usages in the front-end,
 * so that users can know where it was used.
 */
package FXSystem.ControllersAndStructures.Controllers;