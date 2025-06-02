
package sad.gruppo11.Infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.LineSegment; // Using a concrete shape for simplicity
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.ColorData;

class AddShapeCommandTest {

    private Drawing mockDrawing;
    private Shape mockShape;
    private AddShapeCommand addShapeCommand;

    @BeforeEach
    void setUp() {
        mockDrawing = mock(Drawing.class);
        // Use a real, simple shape instance for the command
        mockShape = new LineSegment(new Point2D(0, 0), new Point2D(10, 10), ColorData.BLACK);
        addShapeCommand = new AddShapeCommand(mockDrawing, mockShape);
    }

    @Test
    void constructor_nullDrawing_shouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> {
            new AddShapeCommand(null, mockShape);
        });
    }

    @Test
    void constructor_nullShape_shouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> {
            new AddShapeCommand(mockDrawing, null);
        });
    }

    @Test
    void execute_shouldAddShapeToDrawing() {
        // Simulate that the shape is not initially in the drawing
        when(mockDrawing.getShapeIndex(mockShape)).thenReturn(-1);

        addShapeCommand.execute();

        // Verify that addShape was called on the drawing with the correct shape
        ArgumentCaptor<Shape> shapeArgumentCaptor = ArgumentCaptor.forClass(Shape.class);
        verify(mockDrawing, times(1)).addShape(shapeArgumentCaptor.capture());
        assertSame(mockShape, shapeArgumentCaptor.getValue(), "The correct shape should be added to the drawing.");
    }
    
    @Test
    void execute_shapeAlreadyPresent_shouldRemoveAndReAddShape() {
        // Simulate that the shape is already in the drawing (e.g., due to redo logic)
        when(mockDrawing.getShapeIndex(mockShape)).thenReturn(0); // Shape is present

        addShapeCommand.execute();

        // Verify removeShape is called, then addShape
        verify(mockDrawing, times(1)).removeShape(mockShape);
        verify(mockDrawing, times(1)).addShape(mockShape);
        
        // Ensure addShape is called after removeShape in this scenario
        org.mockito.InOrder inOrder = inOrder(mockDrawing);
        inOrder.verify(mockDrawing).removeShape(mockShape);
        inOrder.verify(mockDrawing).addShape(mockShape);
    }


    @Test
    void undo_shouldRemoveShapeFromDrawing() {
        // Execute first to simulate the state before undo
        when(mockDrawing.getShapeIndex(mockShape)).thenReturn(-1);
        addShapeCommand.execute(); 
        // Clear invocations from execute() to only verify undo()
        clearInvocations(mockDrawing); 

        addShapeCommand.undo();

        // Verify that removeShape was called on the drawing with the correct shape
        ArgumentCaptor<Shape> shapeArgumentCaptor = ArgumentCaptor.forClass(Shape.class);
        verify(mockDrawing, times(1)).removeShape(shapeArgumentCaptor.capture());
        assertSame(mockShape, shapeArgumentCaptor.getValue(), "The correct shape should be removed from the drawing on undo.");
    }

    @Test
    void toString_shouldReturnMeaningfulString() {
        String commandString = addShapeCommand.toString();
        assertTrue(commandString.contains("AddShapeCommand"), "toString should contain the class name.");
        assertTrue(commandString.contains(mockShape.getId().toString()), "toString should contain the shape ID.");
    }
}
            