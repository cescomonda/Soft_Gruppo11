
package sad.gruppo11;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.control.LabeledMatchers;
import org.testfx.matcher.control.TextInputControlMatchers;
import sad.gruppo11.Controller.GeoEngine;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.TextShape;
import sad.gruppo11.Model.geometry.Point2D;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.util.WaitForAsyncUtils.waitFor;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;


@ExtendWith(ApplicationExtension.class)
public class MainAppTestFX {

    private MainApp mainAppInstance;
    private Stage primaryStage;
    private GeoEngine geoEngine; // To inspect model state
    private Path tempSaveFile;


    @Start
    private void start(Stage stage) throws IOException {
        // This method is called by TestFX on the JavaFX Application Thread.
        // We need to get an instance of MainApp to inspect its state or GeoEngine.
        // One way is to load FXML and set the controller manually if MainApp is simple.
        // Or, if MainApp's start method is self-contained:
        
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("mockup.fxml"));
        // The controller is set in MainApp's start method or via FXML if fx:controller is used.
        // For this setup, we assume MainApp's own start method will initialize everything.
        
        // Create a new MainApp instance for each test
        mainAppInstance = new MainApp();
        mainAppInstance.start(stage); // This will load FXML and set up GeoEngine etc.
        
        this.primaryStage = stage;
        // Retrieve GeoEngine from MainApp instance (requires a getter in MainApp or package-private access for testing)
        // For now, we'll try to infer state from UI where possible, or add a getter to MainApp if essential.
        // Let's assume for now we can get it for test assertions (modify MainApp if needed)
        // Field geoEngine in MainApp is private. Add getter for test purposes or use reflection.
        // For simplicity, I'll assume a getter:
        // this.geoEngine = mainAppInstance.getGeoEngine(); 
        // If no getter, many tests will be harder for model verification.
        // Let's try to get it by looking up a node and getting scene, then user data or similar hack,
        // or simply by interacting and observing UI changes.

        // A common way to get controller if set by FXMLLoader:
        // Parent root = loader.load();
        // mainAppInstance = loader.getController();
        // stage.setScene(new Scene(root));
        // stage.show();
        // this.geoEngine = mainAppInstance.getGeoEngine(); // Assuming MainApp has this getter.
        // For now, many tests will rely on UI inspection.
    }
    
    private <T> T getFromMainApp(Callable<T> callable) throws Exception {
        AtomicReference<T> result = new AtomicReference<>();
        AtomicReference<Throwable> exception = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                // Access MainApp's GeoEngine instance directly.
                // This requires MainApp to expose its GeoEngine instance, e.g., via a static field for testing,
                // or making the test class a friend, or a getter.
                // For now, let's assume a static helper for test purposes.
                // If MainApp has private GeoEngine geoEngine;
                // In MainApp: public GeoEngine getGeoEngineForTest() { return geoEngine; }
                // This test structure is a bit complex with how MainApp is started.
                // The @Start method creates a *new* MainApp.
                // The 'mainAppInstance' field here is the one created by TestFX.
                if (mainAppInstance != null && mainAppInstance.getGeoEngine() != null) { // Use public getter
                     result.set(callable.call());
                } else {
                    throw new IllegalStateException("MainApp or GeoEngine not initialized for test inspection.");
                }
            } catch (Exception e) {
                exception.set(e);
            }
        });
        waitForFxEvents();
        if (exception.get() != null) {
            throw new Exception("Error getting value from MainApp", exception.get());
        }
        return result.get();
    }


    @BeforeEach
    public void beforeEachTest(FxRobot robot) throws IOException {
        // Reset state before each test if needed, e.g., clear drawing
        // robot.clickOn("#newButton"); // If a "New" button exists
        // For now, each test starts with a fresh MainApp instance.
        if (geoEngine == null) { // Try to get geoEngine from the running MainApp
            try {
                geoEngine = getFromMainApp(() -> mainAppInstance.getGeoEngine());
            } catch (Exception e) {
                System.err.println("Could not get GeoEngine instance: " + e.getMessage());
                // geoEngine will remain null, tests might fail or adapt.
            }
        }
        
        // Ensure select tool is active initially for predictability
        robot.clickOn("#selectToolButton");
        waitForFxEvents();

        tempSaveFile = Files.createTempFile("testDrawingGeoDraw", ".ser");
    }

    @AfterEach
    public void afterEachTest(FxRobot robot) throws IOException {
        // Close any dialogs
        robot.lookup(".dialog-pane .button").queryAllAs(Button.class).forEach(button -> {
            if (button.getText().equalsIgnoreCase("cancel") || 
                button.getText().equalsIgnoreCase("ok") ||
                button.getText().equalsIgnoreCase("no") ) { // For confirm dialogs
                Platform.runLater(button::fire);
                waitForFxEvents();
            }
        });
        Files.deleteIfExists(tempSaveFile);
    }
    
    private Canvas getCanvas(FxRobot robot) {
        return robot.lookup("#Canvas").queryAs(Canvas.class);
    }

    @Test
    void testToolSelection(FxRobot robot) {
        robot.clickOn("#lineToolButton");
        verifyThat("#lineToolButton", (Button b) -> b.getStyle().contains("-fx-border-color"));
        verifyThat("#selectToolButton", (Button b) -> !b.getStyle().contains("-fx-border-color") || !b.getStyle().contains("#707070"));


        robot.clickOn("#rectangleToolButton");
        verifyThat("#rectangleToolButton", (Button b) -> b.getStyle().contains("-fx-border-color"));
        
        robot.clickOn("#ellipseToolButton");
        verifyThat("#ellipseToolButton", (Button b) -> b.getStyle().contains("-fx-border-color"));
        
        robot.clickOn("#textToolButton");
        verifyThat("#textToolButton", (Button b) -> b.getStyle().contains("-fx-border-color"));

        robot.clickOn("#selectToolButton"); // Back to select
        verifyThat("#selectToolButton", (Button b) -> b.getStyle().contains("-fx-border-color"));
    }

    @Test
    void testDrawRectangleAndSelect(FxRobot robot) {
        robot.clickOn("#rectangleToolButton");
        Canvas canvas = getCanvas(robot);
        
        // Draw a rectangle
        robot.moveTo(canvas, new javafx.geometry.Point2D(100,100)).press(MouseButton.PRIMARY);
        robot.moveTo(canvas, new javafx.geometry.Point2D(200,150)).release(MouseButton.PRIMARY);
        waitForFxEvents();

        // Verify a shape is in the model (requires GeoEngine access)
        assertEventuallyTrue(() -> getFromMainApp(() -> !geoEngine.getDrawing().getShapesInZOrder().isEmpty()));
        Shape drawnShape =getValue(() -> getFromMainApp(() -> geoEngine.getDrawing().getShapesInZOrder().get(0)));
        assertTrue(drawnShape instanceof sad.gruppo11.Model.RectangleShape);

        // Select the shape
        robot.clickOn("#selectToolButton");
        robot.moveTo(canvas, new javafx.geometry.Point2D(150,125)).clickOn(MouseButton.PRIMARY); // Click center of drawn rect
        waitForFxEvents();
        
        assertEventuallyTrue(() -> getFromMainApp(() -> geoEngine.getSelectedShape() != null));
        verifyThat("#shapeNamePropertyField", TextInputControlMatchers.hasText("RectangleShape"));
        // verifyThat("#shapeXPositionField", TextInputControlMatchers.hasText("100")); // Approx
        // verifyThat("#shapeYPositionField", TextInputControlMatchers.hasText("100"));
        verifyThat("#shapeStretchXField", TextInputControlMatchers.hasText("20.0")); // Width
        verifyThat("#shapeStretchYField", TextInputControlMatchers.hasText("10.0"));  // Height
    }
    
    @Test
    void testDrawLineAndChangeColor(FxRobot robot) {
        robot.clickOn("#lineToolButton");
        Canvas canvas = getCanvas(robot);
        robot.moveTo(canvas, new javafx.geometry.Point2D(50,50)).press(MouseButton.PRIMARY);
        robot.moveTo(canvas, new javafx.geometry.Point2D(150,50)).release(MouseButton.PRIMARY);
        waitForFxEvents();

        assertEventuallyTrue(() -> getFromMainApp(() -> !geoEngine.getDrawing().getShapesInZOrder().isEmpty()));
        Shape line =getValue(() -> getFromMainApp(() -> geoEngine.getDrawing().getShapesInZOrder().get(0)));
        assertTrue(line instanceof sad.gruppo11.Model.LineSegment);
        
        // Select it
        robot.clickOn("#selectToolButton");
        robot.moveTo(canvas, new javafx.geometry.Point2D(100,50)).clickOn(MouseButton.PRIMARY);
        waitForFxEvents();
        assertEventuallyTrue(() -> getFromMainApp(() -> geoEngine.getSelectedShape() != null));

        // Change stroke color
        Platform.runLater(() -> geoEngine.getSelectedShape().setStrokeColor(sad.gruppo11.Model.geometry.ColorData.RED) );
        waitForFxEvents();
        
        assertEventuallyTrue(() -> getFromMainApp(() -> {
            sad.gruppo11.Model.geometry.ColorData modelColor = geoEngine.getSelectedShape().getStrokeColor();
            return modelColor.getR() == 255 && modelColor.getG() == 0 && modelColor.getB() == 0;
        }));
    }

    @Test
    void testCreateTextAndModify(FxRobot robot) {
        robot.clickOn("#textToolButton");
        Canvas canvas = getCanvas(robot);
        robot.moveTo(canvas, new javafx.geometry.Point2D(200,200)).clickOn(MouseButton.PRIMARY);
        waitForFxEvents();

        // Handle TextInputDialog
        robot.lookup(".dialog-pane").match( (Node node) -> node instanceof DialogPane ).queryAs(DialogPane.class);
        robot.write("Hello TestFX");
        robot.clickOn(LabeledMatchers.hasText("OK"));
        waitForFxEvents();

        assertEventuallyTrue(() -> getFromMainApp(() -> !geoEngine.getDrawing().getShapesInZOrder().isEmpty()));
        Shape textShape =getValue(() -> getFromMainApp(() -> geoEngine.getDrawing().getShapesInZOrder().get(0)));
        assertTrue(textShape instanceof TextShape);
        assertEquals("Hello TestFX", ((TextShape)textShape).getText());

        // Select it (it should be auto-selected by GeoEngine after creation, or re-select)
        robot.clickOn("#selectToolButton");
        // Need to click on where the text actually is. Assuming (200,200) is within its bounds.
        robot.moveTo(canvas, new javafx.geometry.Point2D(210,210)).clickOn(MouseButton.PRIMARY); // Re-click to ensure selection
        waitForFxEvents();
        
        assertEventuallyTrue(() -> getFromMainApp(() -> geoEngine.getSelectedShape() == textShape));
        verifyThat("#textContentField", TextInputControlMatchers.hasText("Hello TestFX"));

        // Modify text content
        robot.clickOn("#textContentField").eraseText(12).write("New Content");
        robot.press(KeyCode.ENTER).release(KeyCode.ENTER); // Or click away to lose focus
        waitForFxEvents();
        
        assertEventuallyEquals("New Content", () -> getFromMainApp(() -> ((TextShape)geoEngine.getSelectedShape()).getText()));

        // Modify font size
        robot.clickOn("#textFontSizeField").eraseText(4).write("20");
        robot.press(KeyCode.ENTER).release(KeyCode.ENTER);
        waitForFxEvents();

        assertEventuallyEquals(20.0, () -> getFromMainApp(() -> ((TextShape)geoEngine.getSelectedShape()).getFontSize()));
    }
    
    @Test
    void testUndoRedo(FxRobot robot) {
        robot.clickOn("#rectangleToolButton");
        Canvas canvas = getCanvas(robot);
        robot.moveTo(canvas, new javafx.geometry.Point2D(10,10)).press(MouseButton.PRIMARY);
        robot.moveTo(canvas, new javafx.geometry.Point2D(50,50)).release(MouseButton.PRIMARY);
        waitForFxEvents();
        
        assertEventuallyTrue(() -> getFromMainApp(() -> geoEngine.getDrawing().getShapesInZOrder().size() == 1));
        verifyThat("#undoButton", node -> !node.isDisable()); // Undo button enabled implicitly by framework

        robot.clickOn("#undoButton");
        waitForFxEvents();
        assertEventuallyTrue(() -> getFromMainApp(() -> geoEngine.getDrawing().getShapesInZOrder().isEmpty()));
        verifyThat("#redoButton", node -> !node.isDisable()); // Redo button enabled

        robot.clickOn("#redoButton");
        waitForFxEvents();
        assertEventuallyTrue(() -> getFromMainApp(() -> geoEngine.getDrawing().getShapesInZOrder().size() == 1));
    }

    @Test
    void testCopyCutPaste(FxRobot robot) {
        // Draw a shape
        robot.clickOn("#ellipseToolButton");
        Canvas canvas = getCanvas(robot);
        robot.moveTo(canvas, new javafx.geometry.Point2D(30,30)).press(MouseButton.PRIMARY);
        robot.moveTo(canvas, new javafx.geometry.Point2D(80,80)).release(MouseButton.PRIMARY);
        waitForFxEvents(); // Shape 1
        assertEventuallyTrue(() -> getFromMainApp(() -> geoEngine.getDrawing().getShapesInZOrder().size() == 1));

        // Select it
        robot.clickOn("#selectToolButton");
        robot.moveTo(canvas, new javafx.geometry.Point2D(55,55)).clickOn(MouseButton.PRIMARY); // Click center
        waitForFxEvents();
        assertEventuallyTrue(() -> getFromMainApp(() -> geoEngine.getSelectedShape() != null));

        // Copy
        robot.clickOn("#copyButton");
        waitForFxEvents();
        verifyThat("#pasteButton", node -> !node.isDisable()); // Paste enabled

        // Paste
        robot.clickOn("#pasteButton");
        waitForFxEvents(); // Shape 2 (pasted copy)
        assertEventuallyTrue(() -> getFromMainApp(() -> geoEngine.getDrawing().getShapesInZOrder().size() == 2));
        List<Shape> shapes =getValue(() -> getFromMainApp(() -> geoEngine.getDrawing().getShapesInZOrder()));
        assertNotEquals(shapes.get(0).getId(), shapes.get(1).getId());
        // Verify pasted shape is offset (default paste offset is 10,10)
        Point2D originalCenter = shapes.get(0).getBounds().getCenter();
        Point2D pastedCenter = shapes.get(1).getBounds().getCenter();
        assertEquals(originalCenter.getX() + 10, pastedCenter.getX(), 1.0);
        assertEquals(originalCenter.getY() + 10, pastedCenter.getY(), 1.0);


        // Select the first shape again
        robot.moveTo(canvas, new javafx.geometry.Point2D(55,55)).clickOn(MouseButton.PRIMARY);
        waitForFxEvents();
        assertEventuallyTrue(() -> getFromMainApp(() -> geoEngine.getSelectedShape() == shapes.get(0)));

        // Cut
        robot.clickOn("#cutButton");
        waitForFxEvents(); // Shape 1 removed, clipboard has it
        assertEventuallyTrue(() -> getFromMainApp(() -> geoEngine.getDrawing().getShapesInZOrder().size() == 1));
        assertEventuallyTrue(() -> getFromMainApp(() -> geoEngine.getDrawing().getShapesInZOrder().get(0) == shapes.get(1))); // Only pasted shape remains

        // Paste again (should paste the one just cut)
        robot.clickOn("#pasteButton");
        waitForFxEvents(); // Shape 3 (pasted from cut)
        assertEventuallyTrue(() -> getFromMainApp(() -> geoEngine.getDrawing().getShapesInZOrder().size() == 2));
    }
    
    // Helper for tests that need to check a condition that becomes true asynchronously
    private void assertEventuallyTrue(Callable<Boolean> condition) {
        try {
            waitFor(5, TimeUnit.SECONDS, () -> { // Wait up to 5 seconds
                try {
                    return condition.call();
                } catch (Exception e) {
                    return false;
                }
            });
            assertTrue(condition.call());
        } catch (Exception e) {
            fail("Condition did not become true: " + e.getMessage());
        }
    }

    private <T> void assertEventuallyEquals(T expected, Callable<T> actualCallable) {
        try {
            waitFor(5, TimeUnit.SECONDS, () -> {
               try {
                   return expected.equals(actualCallable.call());
               } catch (Exception e) {
                   return false;
               }
           });
            assertEquals(expected, actualCallable.call());
        } catch (Exception e) {
            fail("Actual value did not eventually equal expected. Last value: " +getValue(actualCallable) + ". Expected: " + expected);
        }
    }
    
    // Helper to get value, handling potential exceptions from callable
    private <T> T getValue(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Note: Save/Load tests with FileChooser are complex in TestFX.
    // They would typically involve mocking the FileChooser behavior or using a robot for native dialogs,
    // which is beyond the scope of a simple automated test generation.
    // A simplified save/load test might programmatically set the path if MainApp allows it.
    // For this example, I'm omitting direct save/load tests due to FileChooser complexity.
}

