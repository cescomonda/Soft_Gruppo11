
package sad.gruppo11.Infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.LineSegment; // Concrete shape
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.ColorData;

import java.util.ArrayList;

class CutShapeCommandTest {

    private Drawing mockDrawing;
    private Shape mockShapeToCut;
    private Clipboard mockClipboard;
    private CutShapeCommand command;
    private final int MOCK_SHAPE_INDEX = 2;

    @BeforeEach
    void setUp() {
        mockDrawing = mock(Drawing.class);
        mockClipboard = mock(Clipboard.class);
        mockShapeToCut = new LineSegment(new Point2D(5,5), new Point2D(15,15), ColorData.GREEN);
        
        command = new CutShapeCommand(mockDrawing, mockShapeToCut, mockClipboard);

        // Stubbing for execute
        when(mockDrawing.getShapeIndex(mockShapeToCut)).thenReturn(MOCK_SHAPE_INDEX);
        // Stubbing for undo
        when(mockDrawing.getModifiableShapesList()).thenReturn(new ArrayList<>()); // For addShapeAtIndex or addShape
    }

    @Test
    void constructor_nullDrawing_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> new CutShapeCommand(null, mockShapeToCut, mockClipboard));
    }

    @Test
    void constructor_nullShape_shouldThrowException() {
        // AbstractShapeCommand handles this
        assertThrows(NullPointerException.class, () -> new CutShapeCommand(mockDrawing, null, mockClipboard));
    }

    @Test
    void constructor_nullClipboard_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> new CutShapeCommand(mockDrawing, mockShapeToCut, null));
    }

    @Test
    void execute_shouldSetShapeOnClipboardAndRemoveFromDrawing() {
        command.execute();

        InOrder inOrder = inOrder(mockClipboard, mockDrawing);

        // Verify shape is set on clipboard first
        inOrder.verify(mockClipboard, times(1)).set(mockShapeToCut);
        // Verify getShapeIndex is called
        inOrder.verify(mockDrawing, times(1)).getShapeIndex(mockShapeToCut);
        // Verify shape is removed from drawing
        ArgumentCaptor<Shape> shapeCaptor = ArgumentCaptor.forClass(Shape.class);
        inOrder.verify(mockDrawing, times(1)).removeShape(shapeCaptor.capture());
        assertSame(mockShapeToCut, shapeCaptor.getValue());
    }
    
    @Test
    void undo_originalIndexOutOfBounds_shouldAddShapeToEnd() {
        command.execute();
        clearInvocations(mockDrawing);
        
        ArrayList<Shape> smallerList = new ArrayList<>();
        when(mockDrawing.getModifiableShapesList()).thenReturn(smallerList); // List of size 0

        command.undo();
        
        verify(mockDrawing, times(1)).addShape(mockShapeToCut);
        verify(mockDrawing, never()).addShapeAtIndex(any(Shape.class), anyInt());
    }
    
    @Test
    void undo_originalIndexWasNegative_shouldAddShapeToEnd() {
        when(mockDrawing.getShapeIndex(mockShapeToCut)).thenReturn(-1); // Shape not found
        command.execute(); // originalIndex will be -1
        clearInvocations(mockDrawing);

        command.undo();
        
        verify(mockDrawing, times(1)).addShape(mockShapeToCut);
        verify(mockDrawing, never()).addShapeAtIndex(any(Shape.class), anyInt());
    }


    @Test
    void toString_shouldReturnMeaningfulString() {
        String str = command.toString();
        assertTrue(str.contains("CutShapeCommand"));
        assertTrue(str.contains(mockShapeToCut.getId().toString()));
    }
}
            