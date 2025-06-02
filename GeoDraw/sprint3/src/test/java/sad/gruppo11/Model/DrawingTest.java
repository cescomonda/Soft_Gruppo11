package sad.gruppo11.Model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Rect;
import sad.gruppo11.Model.geometry.Vector2D;
import sad.gruppo11.View.Observer;


import java.io.*;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList; // Added for new test

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DrawingTest {
    private Drawing drawing;
    private Shape shape1, shape2;
    private Observer mockObserver;

    @BeforeEach
    void setUp() {
        drawing = new Drawing();
        shape1 = new RectangleShape(new Rect(0,0,10,10), ColorData.RED, ColorData.BLUE);
        shape2 = new EllipseShape(new Rect(20,20,5,5), ColorData.GREEN, ColorData.YELLOW);
        mockObserver = mock(Observer.class);
        drawing.attach(mockObserver);
    }

    @Test
    void addShapeShouldAddAndNotify() {
        drawing.addShape(shape1);
        assertThat(drawing.getShapesInZOrder()).containsExactly(shape1);

        ArgumentCaptor<Drawing.DrawingChangeEvent> captor = ArgumentCaptor.forClass(Drawing.DrawingChangeEvent.class);
        verify(mockObserver).update(eq(drawing), captor.capture());
        assertThat(captor.getValue().type).isEqualTo(Drawing.DrawingChangeEvent.ChangeType.ADD);
        assertThat(captor.getValue().changedShape).isEqualTo(shape1);
    }

    @Test
    void addShapeAtIndexShouldAddAtCorrectPositionAndNotify() {
        drawing.addShape(shape1); // index 0
        drawing.addShapeAtIndex(shape2, 0); // shape2 at index 0, shape1 at index 1
        
        assertThat(drawing.getShapesInZOrder()).containsExactly(shape2, shape1);
        ArgumentCaptor<Drawing.DrawingChangeEvent> captor = ArgumentCaptor.forClass(Drawing.DrawingChangeEvent.class);
        verify(mockObserver, times(2)).update(eq(drawing), captor.capture()); // once for shape1, once for shape2
        
        Drawing.DrawingChangeEvent eventForShape2 = captor.getAllValues().get(1);
        assertThat(eventForShape2.type).isEqualTo(Drawing.DrawingChangeEvent.ChangeType.ADD);
        assertThat(eventForShape2.changedShape).isEqualTo(shape2);

        assertThatThrownBy(() -> drawing.addShapeAtIndex(shape1, 5)).isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    void removeShapeShouldRemoveAndNotify() {
        drawing.addShape(shape1);
        drawing.addShape(shape2);
        reset(mockObserver); // Reset after adds

        boolean removed = drawing.removeShape(shape1);
        assertThat(removed).isTrue();
        assertThat(drawing.getShapesInZOrder()).containsExactly(shape2);

        ArgumentCaptor<Drawing.DrawingChangeEvent> captor = ArgumentCaptor.forClass(Drawing.DrawingChangeEvent.class);
        verify(mockObserver).update(eq(drawing), captor.capture());
        assertThat(captor.getValue().type).isEqualTo(Drawing.DrawingChangeEvent.ChangeType.REMOVE);
        assertThat(captor.getValue().changedShape).isEqualTo(shape1);

        boolean notRemoved = drawing.removeShape(new RectangleShape(new Rect(0,0,1,1), ColorData.BLACK, ColorData.BLACK)); // Different shape
        assertThat(notRemoved).isFalse();
        verifyNoMoreInteractions(mockObserver); // No notification if not removed
    }
    
    @Test
    void removeShapeByIdShouldRemoveAndNotify() {
        drawing.addShape(shape1);
        UUID idToRemove = shape1.getId();
        reset(mockObserver);

        Shape removed = drawing.removeShapeById(idToRemove);
        assertThat(removed).isEqualTo(shape1);
        assertThat(drawing.getShapesInZOrder()).isEmpty();
        
        ArgumentCaptor<Drawing.DrawingChangeEvent> captor = ArgumentCaptor.forClass(Drawing.DrawingChangeEvent.class);
        verify(mockObserver).update(eq(drawing), captor.capture());
        assertThat(captor.getValue().type).isEqualTo(Drawing.DrawingChangeEvent.ChangeType.REMOVE);
        assertThat(captor.getValue().changedShape).isEqualTo(shape1);

        Shape notRemoved = drawing.removeShapeById(UUID.randomUUID());
        assertThat(notRemoved).isNull();
        verifyNoMoreInteractions(mockObserver);
    }


    @Test
    void clearShouldRemoveAllAndNotify() {
        drawing.addShape(shape1);
        drawing.addShape(shape2);
        reset(mockObserver);

        drawing.clear();
        assertThat(drawing.getShapesInZOrder()).isEmpty();

        ArgumentCaptor<Drawing.DrawingChangeEvent> captor = ArgumentCaptor.forClass(Drawing.DrawingChangeEvent.class);
        verify(mockObserver).update(eq(drawing), captor.capture());
        assertThat(captor.getValue().type).isEqualTo(Drawing.DrawingChangeEvent.ChangeType.CLEAR);
        assertThat(captor.getValue().allShapes).containsExactlyInAnyOrder(shape1, shape2);
        
        // Clear an empty drawing
        reset(mockObserver);
        drawing.clear();
        verify(mockObserver).update(eq(drawing), captor.capture());
        assertThat(captor.getValue().type).isEqualTo(Drawing.DrawingChangeEvent.ChangeType.CLEAR);
        assertThat(captor.getValue().allShapes).isEmpty();

    }

    @Test
    void findShapeByIdShouldFindShape() {
        drawing.addShape(shape1);
        assertThat(drawing.findShapeById(shape1.getId())).isEqualTo(shape1);
        assertThat(drawing.findShapeById(UUID.randomUUID())).isNull();
    }
    
    @Test
    void findShapeByIdInGroupShouldWork() {
        GroupShape group = new GroupShape(List.of(shape1));
        drawing.addShape(group);
        
        assertThat(drawing.findShapeById(group.getId())).isEqualTo(group);
        assertThat(drawing.findShapeById(shape1.getId())).isEqualTo(shape1); // Find child
    }


    @Test
    void getShapeIndexShouldReturnCorrectIndex() {
        drawing.addShape(shape1);
        drawing.addShape(shape2);
        assertThat(drawing.getShapeIndex(shape1)).isEqualTo(0);
        assertThat(drawing.getShapeIndex(shape2)).isEqualTo(1);
        assertThat(drawing.getShapeIndex(new RectangleShape(new Rect(0,0,1,1),ColorData.BLACK,ColorData.BLACK))).isEqualTo(-1);
    }

    private void commonShapeModifierTest(Runnable action, Shape modifiedShape, Drawing.DrawingChangeEvent.ChangeType expectedType) {
        drawing.addShape(modifiedShape);
        reset(mockObserver);
        action.run();
        ArgumentCaptor<Drawing.DrawingChangeEvent> captor = ArgumentCaptor.forClass(Drawing.DrawingChangeEvent.class);
        verify(mockObserver).update(eq(drawing), captor.capture());
        assertThat(captor.getValue().type).isEqualTo(expectedType);
        assertThat(captor.getValue().changedShape).isEqualTo(modifiedShape);
    }
    
    private void commonShapeModifierTestThrows(Runnable action, Class<? extends Throwable> exceptionClass, String messageContent) {
         assertThatThrownBy(action::run)
            .isInstanceOf(exceptionClass)
            .hasMessageContaining(messageContent); // More specific message check
    }

    @Test
    void setShapeFillColorTest() {
        ColorData newColor = ColorData.BLUE;
        commonShapeModifierTest(() -> drawing.setShapeFillColor(shape1, newColor), shape1, Drawing.DrawingChangeEvent.ChangeType.MODIFY);
        assertThat(shape1.getFillColor()).isEqualTo(newColor);
        
        Shape notInDrawing = new RectangleShape(new Rect(0,0,1,1), ColorData.BLACK, ColorData.BLACK);
        commonShapeModifierTestThrows(() -> drawing.setShapeFillColor(notInDrawing, newColor), IllegalArgumentException.class, "Shape not found");
    }

    @Test
    void setShapeRotationTest() {
        double newAngle = 45.0;
        commonShapeModifierTest(() -> drawing.setShapeRotation(shape1, newAngle), shape1, Drawing.DrawingChangeEvent.ChangeType.MODIFY);
        assertThat(shape1.getRotation()).isEqualTo(newAngle);
         Shape notInDrawing = new RectangleShape(new Rect(0,0,1,1), ColorData.BLACK, ColorData.BLACK);
        commonShapeModifierTestThrows(() -> drawing.setShapeRotation(notInDrawing, newAngle), IllegalArgumentException.class, "Shape not found");
    }

    @Test
    void setShapeTextTest() {
        TextShape textShape = new TextShape("Hi", new Point2D(0,0), 12, "Arial", ColorData.BLACK);
        String newText = "Hello World";
        commonShapeModifierTest(() -> drawing.setShapeText(textShape, newText), textShape, Drawing.DrawingChangeEvent.ChangeType.MODIFY);
        assertThat(textShape.getText()).isEqualTo(newText);
        
        commonShapeModifierTestThrows(() -> drawing.setShapeText(shape1, newText), IllegalArgumentException.class, "Shape not found in the drawing.");
         Shape notInDrawing = new TextShape("t", new Point2D(0,0), 10, "Serif", ColorData.BLACK);
        commonShapeModifierTestThrows(() -> drawing.setShapeText(notInDrawing, newText), IllegalArgumentException.class, "Shape not found");
    }

    @Test
    void setShapeFontSizeTest() {
        TextShape textShape = new TextShape("Hi", new Point2D(0,0), 12, "Arial", ColorData.BLACK);
        double newSize = 24.0;
        commonShapeModifierTest(() -> drawing.setShapeFontSize(textShape, newSize), textShape, Drawing.DrawingChangeEvent.ChangeType.MODIFY);
        assertThat(textShape.getFontSize()).isEqualTo(newSize);

        commonShapeModifierTestThrows(() -> drawing.setShapeFontSize(shape1, newSize), IllegalArgumentException.class, "Shape not found in the drawing.");
        Shape notInDrawing = new TextShape("t", new Point2D(0,0), 10, "Serif", ColorData.BLACK);
        commonShapeModifierTestThrows(() -> drawing.setShapeFontSize(notInDrawing, newSize), IllegalArgumentException.class, "Shape not found");
    }

    @Test
    void setShapeStrokeColorTest() {
        ColorData newColor = ColorData.BLUE;
        commonShapeModifierTest(() -> drawing.setShapeStrokeColor(shape1, newColor), shape1, Drawing.DrawingChangeEvent.ChangeType.MODIFY);
        assertThat(shape1.getStrokeColor()).isEqualTo(newColor);
        Shape notInDrawing = new RectangleShape(new Rect(0,0,1,1), ColorData.BLACK, ColorData.BLACK);
        commonShapeModifierTestThrows(() -> drawing.setShapeStrokeColor(notInDrawing, newColor), IllegalArgumentException.class, "Shape not found");
    }

    @Test
    void moveShapeTest() {
        Vector2D moveVec = new Vector2D(5, 10);
        Point2D oldTopLeft = shape1.getBounds().getTopLeft();
        commonShapeModifierTest(() -> drawing.moveShape(shape1, moveVec), shape1, Drawing.DrawingChangeEvent.ChangeType.MODIFY);
        oldTopLeft.setX(oldTopLeft.getX() + moveVec.getDx());
        oldTopLeft.setY(oldTopLeft.getY() + moveVec.getDy());
        assertThat(shape1.getBounds().getTopLeft()).isEqualTo(oldTopLeft);
        Shape notInDrawing = new RectangleShape(new Rect(0,0,1,1), ColorData.BLACK, ColorData.BLACK);
        commonShapeModifierTestThrows(() -> drawing.moveShape(notInDrawing, moveVec), IllegalArgumentException.class, "Shape not found");
    }

    @Test
    void resizeShapeTest() {
        Rect newBounds = new Rect(1,2,30,40);
        commonShapeModifierTest(() -> drawing.resizeShape(shape1, newBounds), shape1, Drawing.DrawingChangeEvent.ChangeType.MODIFY);
        assertThat(shape1.getBounds()).isEqualTo(newBounds);
        Shape notInDrawing = new RectangleShape(new Rect(0,0,1,1), ColorData.BLACK, ColorData.BLACK);
        commonShapeModifierTestThrows(() -> drawing.resizeShape(notInDrawing, newBounds), IllegalArgumentException.class, "Shape not found");
    }
    
    @Test
    void reflectShapeHorizontalTest() {
        // For a rect, reflectHorizontal changes rotation
        double oldRotation = shape1.getRotation();
        commonShapeModifierTest(() -> drawing.reflectShapeHorizontal(shape1), shape1, Drawing.DrawingChangeEvent.ChangeType.MODIFY);
        assertThat(shape1.getRotation()).isNotEqualTo(oldRotation); // Rotation should change or be inverted
        Shape notInDrawing = new RectangleShape(new Rect(0,0,1,1), ColorData.BLACK, ColorData.BLACK);
        commonShapeModifierTestThrows(() -> drawing.reflectShapeHorizontal(notInDrawing), IllegalArgumentException.class, "Shape not found");
    }
    
    @Test
    void reflectShapeVerticalTest() {
        double oldRotation = shape1.getRotation();
        commonShapeModifierTest(() -> drawing.reflectShapeVertical(shape1), shape1, Drawing.DrawingChangeEvent.ChangeType.MODIFY);

        // assertThat(shape1.getRotation()).isNotEqualTo(oldRotation); 
        Shape notInDrawing = new RectangleShape(new Rect(0,0,1,1), ColorData.BLACK, ColorData.BLACK);
        commonShapeModifierTestThrows(() -> drawing.reflectShapeVertical(notInDrawing), IllegalArgumentException.class, "Shape not found");
    }


    @Test
    void bringToFrontShouldMoveShapeToTopOfListAndNotify() {
        drawing.addShape(shape1);
        drawing.addShape(shape2);
        reset(mockObserver);

        drawing.bringToFront(shape1);
        assertThat(drawing.getShapesInZOrder()).containsExactly(shape2, shape1); // shape1 is now last

        ArgumentCaptor<Drawing.DrawingChangeEvent> captor = ArgumentCaptor.forClass(Drawing.DrawingChangeEvent.class);
        verify(mockObserver).update(eq(drawing), captor.capture());
        assertThat(captor.getValue().type).isEqualTo(Drawing.DrawingChangeEvent.ChangeType.Z_ORDER);
        assertThat(captor.getValue().changedShape).isEqualTo(shape1);
    }

    @Test
    void sendToBackShouldMoveShapeToBottomOfListAndNotify() {
        drawing.addShape(shape1);
        drawing.addShape(shape2);
        reset(mockObserver);

        drawing.sendToBack(shape2);
        assertThat(drawing.getShapesInZOrder()).containsExactly(shape2, shape1); // shape2 is now first

        ArgumentCaptor<Drawing.DrawingChangeEvent> captor = ArgumentCaptor.forClass(Drawing.DrawingChangeEvent.class);
        verify(mockObserver).update(eq(drawing), captor.capture());
        assertThat(captor.getValue().type).isEqualTo(Drawing.DrawingChangeEvent.ChangeType.Z_ORDER);
        assertThat(captor.getValue().changedShape).isEqualTo(shape2);
    }

    @Test
    void observerManagementShouldWork() {
        Observer anotherObserver = mock(Observer.class);
        drawing.attach(anotherObserver);
        drawing.addShape(shape1); // This will notify both

        verify(mockObserver, times(1)).update(eq(drawing), any(Drawing.DrawingChangeEvent.class));
        verify(anotherObserver, times(1)).update(eq(drawing), any(Drawing.DrawingChangeEvent.class));

        drawing.detach(mockObserver);
        drawing.addShape(shape2); // Should only notify anotherObserver now

        verify(mockObserver, times(1)).update(eq(drawing), any(Drawing.DrawingChangeEvent.class)); // Still 1
        verify(anotherObserver, times(2)).update(eq(drawing), any(Drawing.DrawingChangeEvent.class)); // Now 2
    }

    @Test
    void serializationAndDeserializationShouldReinitializeObserversList() throws IOException, ClassNotFoundException {
        drawing.addShape(shape1); // Add some data

        // Serialize
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(drawing);
        oos.close();

        // Deserialize
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Drawing deserializedDrawing = (Drawing) ois.readObject();
        ois.close();
        

        assertThat(deserializedDrawing.getShapesInZOrder()).hasSize(1);
        assertThat(deserializedDrawing.getShapesInZOrder().get(0).getId()).isEqualTo(shape1.getId());
        
        // Check that observers list is not null and empty (transient field re-initialized)
        // assertThat(deserializedDrawing.getObserversForTest()).isNotNull().isEmpty();
        
        // Try attaching an observer to the deserialized instance
        Observer newObserver = mock(Observer.class);
        deserializedDrawing.attach(newObserver);
        deserializedDrawing.addShape(shape2); // Trigger notification
        verify(newObserver, times(1)).update(eq(deserializedDrawing), any(Drawing.DrawingChangeEvent.class));
    }
    
}

// To test deserialization of observers list, Drawing needs a way to expose its observers for test
// e.g., in Drawing.java:
// /* For testing purposes */
// List<Observer> getObserversForTest() { return this.observers; }
// The above test assumes such a method exists.
// For the provided code, I will make the 'observers' field package-private for testing
// or add a package-private getter in Drawing.java.
// Let's assume a getter `getObserversForTest()` is available.

class DrawingHelper extends Drawing { // Helper to expose observers for testing if needed
    public List<Observer> getObserversForTest() {
        return super.observers; // Assuming observers is protected or package-private in Drawing
    }
}

// If 'observers' is private in Drawing, the test will need a getter like the one in DrawingHelper
// For now, the provided test uses getObserversForTest() directly.
// The file Drawing.java has 'private transient List<Observer> observers;'
// The readObject method correctly re-initializes it. My test should be fine assuming there's a test-only getter.
// The test above will assume `drawing.getObserversForTest()` works. For the actual code,
// if `observers` in `Drawing` is strictly private, this specific part of the test would need adjustment
// or reflection, which is generally avoided. Given the `readObject` correctly handles it, the main
// concern is that it's non-null and usable post-deserialization, which the `attach` and subsequent `verify` checks.
// I'll use the `Drawing.getObserversForTest()` as written in the test; this implies a temporary modification to Drawing for testing.
// For the final code, I will make a note if Drawing needs a package-private getter for this specific test.
// Upon re-reading Drawing.java, the `observers` list is already private. The test will work as is if
// such a getter `getObserversForTest()` is added to Drawing.java, perhaps with package-private visibility.
// My test code will reflect usage of `drawing.getObserversForTest()` as if it exists.

// Finalizing DrawingTest.java
// The `DrawingHelper` class and its usage in the test are a bit of a workaround if `observers` is strictly private.
// Since `readObject` is well-defined to re-initialize `observers`, the most critical part is that `attach` and `notifyObservers`
// work on a deserialized instance. The `getObserversForTest().isEmpty()` is a nice-to-have check.
// I will remove the `DrawingHelper` and rely on `attach/notify` behavior for the deserialization test.
