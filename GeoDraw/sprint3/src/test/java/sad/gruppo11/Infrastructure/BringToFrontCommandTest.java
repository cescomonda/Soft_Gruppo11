
package sad.gruppo11.Infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.LineSegment;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.ColorData;

import java.util.ArrayList;
import java.util.List;

class BringToFrontCommandTest {

    private Drawing mockDrawing;
    private Shape mockShape;
    private BringToFrontCommand command;
    private final int MOCK_SHAPE_INDEX = 1; // Assume shape is initially at index 1 in a list of e.g. 3 shapes

    @BeforeEach
    void setUp() {
        mockDrawing = mock(Drawing.class);
        mockShape = new LineSegment(new Point2D(0,0), new Point2D(1,1), ColorData.BLACK);
        command = new BringToFrontCommand(mockDrawing, mockShape);

        when(mockDrawing.getShapeIndex(mockShape)).thenReturn(MOCK_SHAPE_INDEX);
        // For undo logic: when removeShape is called, then addShapeAtIndex
        when(mockDrawing.removeShape(mockShape)).thenReturn(true); // Simulate successful removal
        // Simulate a list for getModifiableShapesList().size() check in undo
        List<Shape> shapeList = new ArrayList<>();
        shapeList.add(mock(Shape.class)); // Dummy shapes to affect size
        shapeList.add(mock(Shape.class));
        shapeList.add(mock(Shape.class));
        when(mockDrawing.getModifiableShapesList()).thenReturn(shapeList);
    }

    @Test
    void constructor_nullDrawing_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> new BringToFrontCommand(null, mockShape));
    }

    @Test
    void constructor_nullShape_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> new BringToFrontCommand(mockDrawing, null));
    }

    @Test
    void execute_shapeExists_shouldCallBringToFrontOnDrawing() {
        command.execute();

        verify(mockDrawing, times(1)).getShapeIndex(mockShape);
        verify(mockDrawing, times(1)).bringToFront(mockShape);
    }

    @Test
    void execute_shapeDoesNotExist_shouldNotCallBringToFront() {
        when(mockDrawing.getShapeIndex(mockShape)).thenReturn(-1); // Shape not found
        BringToFrontCommand cmdShapeNotFound = new BringToFrontCommand(mockDrawing, mockShape);
        
        cmdShapeNotFound.execute();
        
        verify(mockDrawing, times(1)).getShapeIndex(mockShape); // Index is fetched
        verify(mockDrawing, never()).bringToFront(mockShape);  // but bringToFront is not called
    }

    @Test
    void undo_shapeWasBroughtToFront_shouldRestoreOriginalIndex() {
        command.execute(); // originalIndex is stored
        clearInvocations(mockDrawing);

        command.undo();

        // Verify the sequence: remove, then addAtIndex
        InOrder inOrder = inOrder(mockDrawing);
        inOrder.verify(mockDrawing, times(1)).removeShape(mockShape);
        
        ArgumentCaptor<Shape> shapeCaptor = ArgumentCaptor.forClass(Shape.class);
        ArgumentCaptor<Integer> indexCaptor = ArgumentCaptor.forClass(Integer.class);
        inOrder.verify(mockDrawing, times(1)).addShapeAtIndex(shapeCaptor.capture(), indexCaptor.capture());
        
        assertSame(mockShape, shapeCaptor.getValue());
        assertEquals(MOCK_SHAPE_INDEX, indexCaptor.getValue());
    }
    
    @Test
    void undo_shapeWasNotOriginallyInDrawing_shouldDoNothingOrHandleGracefully() {
        when(mockDrawing.getShapeIndex(mockShape)).thenReturn(-1);
        BringToFrontCommand cmdShapeNotFound = new BringToFrontCommand(mockDrawing, mockShape);
        cmdShapeNotFound.execute(); // originalIndex will be -1
        clearInvocations(mockDrawing);

        cmdShapeNotFound.undo();
        // If originalIndex is -1, removeShape and addShapeAtIndex should not be called
        verify(mockDrawing, never()).removeShape(any(Shape.class));
        verify(mockDrawing, never()).addShapeAtIndex(any(Shape.class), anyInt());
    }
    
    @Test
    void undo_addShapeAtIndexOutOfBounds_shouldAddShapeToEndAsFallback() {
        command.execute(); // originalIndex is MOCK_SHAPE_INDEX (e.g., 1)
        clearInvocations(mockDrawing);

        // Simulate that the list size became smaller than originalIndex during other operations
        List<Shape> smallerList = new ArrayList<>(); // Empty list
        when(mockDrawing.getModifiableShapesList()).thenReturn(smallerList); // size 0
        // So, MOCK_SHAPE_INDEX (1) is now out of bounds for addAtIndex (0)

        command.undo();

        InOrder inOrder = inOrder(mockDrawing);
        inOrder.verify(mockDrawing).removeShape(mockShape); // Still removed
        inOrder.verify(mockDrawing).addShape(mockShape);    // Fallback to addShape
        verify(mockDrawing, never()).addShapeAtIndex(any(Shape.class), anyInt());
    }


    @Test
    void toString_shouldReturnMeaningfulString() {
        String str = command.toString();
        assertTrue(str.contains("BringToFrontCommand"));
        assertTrue(str.contains(mockShape.getId().toString()));
    }
}
            