
package sad.gruppo11.Infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.GroupShape;
import sad.gruppo11.Model.LineSegment;
import sad.gruppo11.Model.RectangleShape;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Rect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class UngroupShapeCommandTest {

    private Drawing mockDrawing;
    private GroupShape mockGroupShape;
    private Shape mockChild1;
    private Shape mockChild2;
    private List<Shape> children;
    private UngroupShapeCommand command;
    private final int MOCK_GROUP_INDEX = 0;

    @BeforeEach
    void setUp() {
        mockDrawing = mock(Drawing.class);
        mockChild1 = new LineSegment(new Point2D(0,0), new Point2D(1,1), ColorData.RED);
        mockChild2 = new RectangleShape(new Rect(2,2,3,3), ColorData.BLUE, ColorData.GREEN);
        children = new ArrayList<>(Arrays.asList(mockChild1, mockChild2));
        
        // Use spy for GroupShape to control getChildren() and for verification
        mockGroupShape = spy(new GroupShape(children));
        // when(mockGroupShape.getChildren()).thenReturn(children); // Already handled by spy constructor

        command = new UngroupShapeCommand(mockDrawing, mockGroupShape);

        // Stubbing for execute
        when(mockDrawing.getShapeIndex(mockGroupShape)).thenReturn(MOCK_GROUP_INDEX);
        when(mockDrawing.removeShape(mockGroupShape)).thenReturn(true); // Simulate successful removal
        // Stubbing for undo
        when(mockDrawing.getModifiableShapesList()).thenReturn(new ArrayList<>());
    }

    @Test
    void constructor_nullDrawing_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> new UngroupShapeCommand(null, mockGroupShape));
    }

    @Test
    void constructor_nullGroupShape_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> new UngroupShapeCommand(mockDrawing, null));
    }

    @Test
    void execute_shouldRemoveGroupAndAddChildrenToDrawing() {
        command.execute();

        InOrder inOrder = inOrder(mockDrawing, mockGroupShape);
        // Verify group's children are fetched
        inOrder.verify(mockGroupShape).getChildren();
        // Verify group's index is fetched
        inOrder.verify(mockDrawing).getShapeIndex(mockGroupShape);
        // Verify group is removed
        inOrder.verify(mockDrawing).removeShape(mockGroupShape);
        // Verify children are added
        inOrder.verify(mockDrawing).addShape(mockChild1);
        inOrder.verify(mockDrawing).addShape(mockChild2);
    }
    
    @Test
    void execute_groupNotInDrawing_shouldNotAddChildren() {
        when(mockDrawing.removeShape(mockGroupShape)).thenReturn(false); // Simulate group not found / not removed

        command.execute();

        verify(mockDrawing, times(1)).removeShape(mockGroupShape);
        verify(mockDrawing, never()).addShape(mockChild1);
        verify(mockDrawing, never()).addShape(mockChild2);
    }

    @Test
    void undo_shouldRemoveChildrenAndAddGroupBack() {
        command.execute(); // originalChildren and originalGroupIndex are stored
        clearInvocations(mockDrawing);

        command.undo();

        InOrder inOrder = inOrder(mockDrawing);
        // Verify children are removed first
        inOrder.verify(mockDrawing).removeShape(mockChild1);
        inOrder.verify(mockDrawing).removeShape(mockChild2);
        // Verify group is added back at original index
        ArgumentCaptor<Shape> shapeCaptor = ArgumentCaptor.forClass(Shape.class);
        ArgumentCaptor<Integer> indexCaptor = ArgumentCaptor.forClass(Integer.class);
        inOrder.verify(mockDrawing).addShapeAtIndex(shapeCaptor.capture(), indexCaptor.capture());
        
        assertSame(mockGroupShape, shapeCaptor.getValue());
        assertEquals(MOCK_GROUP_INDEX, indexCaptor.getValue());
    }
    
    @Test
    void undo_withoutExecute_shouldHandleGracefully() {
        // If execute not called, originalChildren is null.
        command.undo();
        verify(mockDrawing, never()).removeShape(any(Shape.class)); // Children not removed
        verify(mockDrawing, never()).addShapeAtIndex(any(Shape.class), anyInt()); // Group not added back
        verify(mockDrawing, never()).addShape(any(Shape.class));
    }


    @Test
    void toString_shouldReturnMeaningfulString() {
        String str = command.toString();
        assertTrue(str.contains("UngroupShapeCommand"));
        assertTrue(str.contains(mockGroupShape.getId().toString()));
    }
}
            