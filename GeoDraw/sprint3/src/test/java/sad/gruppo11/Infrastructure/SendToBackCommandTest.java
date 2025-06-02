
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

class SendToBackCommandTest {

    private Drawing mockDrawing;
    private Shape mockShape;
    private SendToBackCommand command;
    private final int MOCK_SHAPE_INDEX = 1;

    @BeforeEach
    void setUp() {
        mockDrawing = mock(Drawing.class);
        mockShape = new LineSegment(new Point2D(0,0), new Point2D(1,1), ColorData.BLACK);
        command = new SendToBackCommand(mockDrawing, mockShape);

        when(mockDrawing.getShapeIndex(mockShape)).thenReturn(MOCK_SHAPE_INDEX);
        when(mockDrawing.removeShape(mockShape)).thenReturn(true);
        List<Shape> shapeList = new ArrayList<>();
        shapeList.add(mock(Shape.class));
        shapeList.add(mock(Shape.class));
        when(mockDrawing.getModifiableShapesList()).thenReturn(shapeList);
    }

    @Test
    void constructor_nullDrawing_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> new SendToBackCommand(null, mockShape));
    }

    @Test
    void constructor_nullShape_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> new SendToBackCommand(mockDrawing, null));
    }

    @Test
    void execute_shapeExists_shouldCallSendToBackOnDrawing() {
        command.execute();

        verify(mockDrawing, times(1)).getShapeIndex(mockShape);
        verify(mockDrawing, times(1)).sendToBack(mockShape);
    }
    
    @Test
    void execute_shapeDoesNotExist_shouldNotCallSendToBack() {
        when(mockDrawing.getShapeIndex(mockShape)).thenReturn(-1); // Shape not found
        SendToBackCommand cmdShapeNotFound = new SendToBackCommand(mockDrawing, mockShape);
        
        cmdShapeNotFound.execute();
        
        verify(mockDrawing, times(1)).getShapeIndex(mockShape);
        verify(mockDrawing, never()).sendToBack(mockShape);
    }

    @Test
    void undo_shapeWasSentToBack_shouldRestoreOriginalIndex() {
        command.execute(); // originalIndex is stored
        clearInvocations(mockDrawing);

        command.undo();

        InOrder inOrder = inOrder(mockDrawing);
        inOrder.verify(mockDrawing, times(1)).removeShape(mockShape);
        
        ArgumentCaptor<Shape> shapeCaptor = ArgumentCaptor.forClass(Shape.class);
        ArgumentCaptor<Integer> indexCaptor = ArgumentCaptor.forClass(Integer.class);
        inOrder.verify(mockDrawing, times(1)).addShapeAtIndex(shapeCaptor.capture(), indexCaptor.capture());
        
        assertSame(mockShape, shapeCaptor.getValue());
        assertEquals(MOCK_SHAPE_INDEX, indexCaptor.getValue());
    }

    @Test
    void toString_shouldReturnMeaningfulString() {
        String str = command.toString();
        assertTrue(str.contains("SendToBackCommand"));
        assertTrue(str.contains(mockShape.getId().toString()));
    }
}
            