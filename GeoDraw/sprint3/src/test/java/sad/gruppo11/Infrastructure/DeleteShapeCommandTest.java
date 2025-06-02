
package sad.gruppo11.Infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.LineSegment;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.ColorData;

import java.util.ArrayList;

class DeleteShapeCommandTest {

    private Drawing mockDrawing;
    private Shape mockShape;
    private DeleteShapeCommand deleteShapeCommand;
    private final int MOCK_SHAPE_INDEX = 3;

    @BeforeEach
    void setUp() {
        mockDrawing = mock(Drawing.class);
        mockShape = new LineSegment(new Point2D(0, 0), new Point2D(10, 10), ColorData.BLACK); // Use a real shape
        deleteShapeCommand = new DeleteShapeCommand(mockDrawing, mockShape);

        // Stubbing getShapeIndex for execute
        when(mockDrawing.getShapeIndex(mockShape)).thenReturn(MOCK_SHAPE_INDEX);
        // Stubbing getModifiableShapesList for undo logic if addShapeAtIndex needs it
        when(mockDrawing.getModifiableShapesList()).thenReturn(new ArrayList<>());
    }

    @Test
    void constructor_nullDrawing_shouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> {
            new DeleteShapeCommand(null, mockShape);
        });
    }

    @Test
    void constructor_nullShape_shouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> {
            new DeleteShapeCommand(mockDrawing, null);
        });
    }

    @Test
    void execute_shouldRemoveShapeFromDrawingAndStoreIndex() {
        deleteShapeCommand.execute();

        verify(mockDrawing, times(1)).getShapeIndex(mockShape); // Verify index was fetched
        ArgumentCaptor<Shape> shapeCaptor = ArgumentCaptor.forClass(Shape.class);
        verify(mockDrawing, times(1)).removeShape(shapeCaptor.capture());
        assertSame(mockShape, shapeCaptor.getValue(), "The correct shape should be removed.");
    }
    
    @Test
    void undo_originalIndexOutOfBounds_shouldAddShapeToEnd() {
        // Simulate execute
        deleteShapeCommand.execute();
        clearInvocations(mockDrawing);

        // Simulate scenario where originalIndex is now out of bounds (e.g., list smaller)
        // To do this robustly, we need to control what getModifiableShapesList().size() returns
        // For this specific test, we can assume the original index was, say, 5, and list is now size 2.
        // The DeleteShapeCommand stores the index internally. We can't easily change it after execute.
        // So, we'll rely on the command's internal logic that if originalIndex is out of bounds for addShapeAtIndex,
        // it falls back to addShape().

        // We need to mock getModifiableShapesList().size() to be less than MOCK_SHAPE_INDEX for the fallback.
        ArrayList<Shape> smallerList = new ArrayList<>();
        when(mockDrawing.getModifiableShapesList()).thenReturn(smallerList); // List of size 0

        deleteShapeCommand.undo();

        // Verify addShape is called (fallback) instead of addShapeAtIndex if index is bad
        verify(mockDrawing, times(1)).addShape(mockShape);
        verify(mockDrawing, never()).addShapeAtIndex(any(Shape.class), anyInt());
    }
    
    @Test
    void undo_originalIndexWasNegative_shouldAddShapeToEnd() {
        // Simulate a scenario where getShapeIndex returned -1 (shape not found during execute, though unlikely for delete)
        when(mockDrawing.getShapeIndex(mockShape)).thenReturn(-1); // Shape not found
        deleteShapeCommand.execute(); // originalIndex will be -1
        clearInvocations(mockDrawing);

        deleteShapeCommand.undo();
        
        verify(mockDrawing, times(1)).addShape(mockShape); // Fallback to addShape
        verify(mockDrawing, never()).addShapeAtIndex(any(Shape.class), anyInt());
    }


    @Test
    void toString_shouldReturnMeaningfulString() {
        String commandString = deleteShapeCommand.toString();
        assertTrue(commandString.contains("DeleteShapeCommand"), "toString should contain the class name.");
        assertTrue(commandString.contains(mockShape.getId().toString()), "toString should contain the shape ID.");
    }
}
            