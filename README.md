(Preview of the README.md; Not the final version yet.)
# ESEJ engine (Eta Statistical Engine Java ver.)
ESEJ project, or I originally called it Eta system Java edition, was a statistical multipurpose toolkit for Java-native builds and projects.
It was designed to extend R language(not included in the current version) and other JVM statistical packages like Apache 
Commons Statistics, Smile, and JFreeChart data visualization package for building
GUIs (via JavaFX or Java Swing) and Web/Cloud applications (using Spring frameworks like Spring boot and Spring cloud; though
currently not included in alpha version) and direct usages, as well as providing options for either accessing R (multi-language; via Rserve) or using 
native built-in functions (develop your project entirely in Java).
## Included UIs
In the current version of the ESEJ engine:
* FXSystem - The main GUI of ESEJ engine enabling users to interact with the engine. Enable users to:
  * Plot univariate distributions(in the main page of the GUI) - it can plot various distributions and export the plot as .png file or extract
the data to Excel file;
  * Compare (Two/Multi; up to 10) multiple distributions to visualize the differences between each data given (assume that
they are distributed in a specific pattern); In the Two-comparator page, you can enable ECDF plots mode to determine goodness-of-fit
of a sample given a specified distribution. Similar to Main page, the Two-comparator page can also export the plot as .png file but cannot extract
the data.
  * Plot multivariate distributions(2D/3D) - enables users to plot multivariate distributions(only bivariate; current version
only allow users to plot bivariate Normal) to contour heat Canvas(2D) or in a meshView(3D). Only 2D page allow user to export
the canvas as .png file.
  * Data Analyzer / Power Analyzer / Regression visualizer - Enables users to perform data visualization, power analysis,
and regression visualization in the FXSystem. These pages are one of the advanced tool this application provides. All pages
(except power analysis page because it won't generate any charts) can export the generated chart as .png file.
* Note: In the current version of the application, you can't rename or restyle the generated charts. I'll add these
capabilities if you guys needed it.

If you want to know how to use them, see their documentations for details.

## Configuration using build tools
This project was built using Apache Maven for installing and managing required dependecies, and running FXSystem GUI. 
Unfortunately, I haven't packaging the FXSystem using Maven yet, so you cannot install it via Maven (though you can configure it as
a local module) or Gradle(Kotlin) yet. I'll 
add them later in beta version releasing.
<br>
But to assemble it using maven, here's how:
<p>
- First, install the .zip file from the releases and unzip it <br>
- Then, in your IDE (IntelliJ IDEA recommanded), add it to your project classpath, and in your project's root pom.xml, add
their dependecies. In the current settings, The required dependencies are: <br>
<b>
eta-link, <br>
eta-core, <br>
eta-gui-shared-backend (since the main page and the multi-comparatoe page uses it) <br>
eta-util-java, (the utillty class) <br>
eta-gui (for rest of them) <br>
fx-system (the application) <br>
</b>
- Set up your entry point. The entry point of the app was:

```java
import FXSystem.ControllersAndStructures.Controllers.Initialize.FXSinglePlotterController;
import javafx.application.Application;
import javafx.stage.Stage;

// Example
public class TestApp extends Application {
    @Override
    public void start(Stage stage) {
        FXSinglePlotterController controller = new FXSinglePlotterController();
        stage = controller.buildStage();
        stage.show();
    }
}
```

And remember to configure the main class as the entry point class name(in your project's root pom.xml). Then run <br>

```bash
mvn javafx:run
```

in your terminal or in IntelliJ IDEA, open "Maven" and then "Execute Maven Goal", then enter the above command.
Remember inside your project's root pom.xml, it has something like:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-maven-plugin</artifactId>
            <version>0.0.8</version>
            <configuration>
                <mainClass>Test.TestApp</mainClass>
            </configuration>
        </plugin>
    </plugins>
</build>
```

</p>
<b>Note:</b> it might contain other unavailable feature such as web backends. Do not use it until new package updates
(where this script will may be removed).

## Found bugs or want some new features?
You can freely convey it via create new issues at its GitHub repository. Since it was still in alpha, so maybe there's 
incompleteness / bugs that it may affect your experiences. If you did find some bugs or want to add something, feel free
to create your issues!
