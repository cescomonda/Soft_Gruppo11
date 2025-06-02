
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

class GroupShapesCommandTest {

    private Drawing mockDrawing;
    private Shape mockShape1;
    private Shape mockShape2;
    private List<Shape> shapesToGroup;
    private GroupShapesCommand command;

    @BeforeEach
    void setUp() {
        mockDrawing = mock(Drawing.class);
        mockShape1 = new LineSegment(new Point2D(0,0), new Point2D(1,1), ColorData.RED);
        mockShape2 = new RectangleShape(new Rect(2,2,3,3), ColorData.BLUE, ColorData.GREEN);
        shapesToGroup = new ArrayList<>(Arrays.asList(mockShape1, mockShape2));

        // Stubbing getShapeIndex for the sorting and removal logic in execute
        when(mockDrawing.getShapeIndex(mockShape1)).thenReturn(0); // Arbitrary indices
        when(mockDrawing.getShapeIndex(mockShape2)).thenReturn(1);
        
        command = new GroupShapesCommand(mockDrawing, shapesToGroup);
    }

    @Test
    void constructor_nullDrawing_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> new GroupShapesCommand(null, shapesToGroup));
    }

    @Test
    void constructor_nullShapesList_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> new GroupShapesCommand(mockDrawing, null));
    }

    @Test
    void constructor_lessThanTwoShapes_shouldThrowIllegalArgumentException() {
        List<Shape> singleShapeList = Arrays.asList(mockShape1);
        assertThrows(IllegalArgumentException.class, () -> new GroupShapesCommand(mockDrawing, singleShapeList));
        
        List<Shape> emptyList = new ArrayList<>();
        assertThrows(IllegalArgumentException.class, () -> new GroupShapesCommand(mockDrawing, emptyList));
    }

    @Test
    void execute_shouldRemoveIndividualShapesAndAddGroupShape() {
        command.execute();

        InOrder inOrder = inOrder(mockDrawing);
        // Order of removal depends on initial indices; mockShape2 (index 1) then mockShape1 (index 0)
        inOrder.verify(mockDrawing).removeShape(mockShape2);
        inOrder.verify(mockDrawing).removeShape(mockShape1);

        ArgumentCaptor<Shape> groupCaptor = ArgumentCaptor.forClass(Shape.class);
        inOrder.verify(mockDrawing).addShape(groupCaptor.capture());

        assertTrue(groupCaptor.getValue() instanceof GroupShape, "A GroupShape should be added.");
        GroupShape addedGroup = (GroupShape) groupCaptor.getValue();
        assertEquals(2, addedGroup.getChildren().size(), "Group should contain the original shapes.");
        assertTrue(addedGroup.getChildren().contains(mockShape1));
        assertTrue(addedGroup.getChildren().contains(mockShape2));
    }
    
    @Test
    void execute_shapeNotInDrawing_shouldBeSkipped() {
        when(mockDrawing.getShapeIndex(mockShape2)).thenReturn(-1); // mockShape2 is not in drawing
        
        command.execute(); // Should only process mockShape1 effectively, but group needs >= 2
                           // The current command structure would try to form a group even if some are skipped.
                           // The check for shapesToGroup.size() < 2 is in constructor.
                           // If one shape is skipped, group will be made with 1 shape, then GroupShape constructor might fail.
                           // Or GroupShape accepts 1 shape, which might be ok.
                           // Let's assume GroupShape constructor needs >= 1 for this test.
                           // The command itself does not re-check size after filtering.

        verify(mockDrawing, times(1)).removeShape(mockShape1);
        verify(mockDrawing, never()).removeShape(mockShape2); // Not removed as not found by index

        ArgumentCaptor<Shape> groupCaptor = ArgumentCaptor.forClass(Shape.class);
        verify(mockDrawing).addShape(groupCaptor.capture());
        GroupShape addedGroup = (GroupShape) groupCaptor.getValue();
        
        // Based on current command logic, it will create a GroupShape with whatever was successfully "removed" (conceptually)
        // And shapesToGroup passed to GroupShape constructor includes all original shapes.
        // This is a bit complex. The GroupShape constructor in the provided code takes List<Shape> directly.
        assertEquals(2, addedGroup.getChildren().size(), "GroupShape still constructed with original list");
    }


    @Test
    void undo_shouldRemoveGroupAndAddIndividualShapes() {
        command.execute();
        Shape createdGroup = ((GroupShapesCommand)command).getCreatedGroup(); // Access for verification
        clearInvocations(mockDrawing);

        command.undo();

        verify(mockDrawing, times(1)).removeShape(createdGroup);
        // Verify shapes are added back (order might not be original index without more complex stubbing)
        verify(mockDrawing, times(1)).addShape(mockShape1);
        verify(mockDrawing, times(1)).addShape(mockShape2);
    }
    
    @Test
    void undo_withoutExecute_shouldDoNothingOrHandleGracefully() {
        // If execute not called, createdGroup is null.
        command.undo();
        verify(mockDrawing, never()).removeShape(any(GroupShape.class));
        verify(mockDrawing, never()).addShape(any(Shape.class)); // Individual shapes not added back
    }

    @Test
    void toString_shouldReturnMeaningfulString() {
        String str = command.toString();
        assertTrue(str.contains("GroupShapesCommand"));
        assertTrue(str.contains("groupedShapeCount=2"));
        
        command.execute(); // To have createdGroup ID
        str = command.toString();
        assertTrue(str.contains("groupId=" + ((GroupShapesCommand)command).getCreatedGroup().getId().toString()));
    }
}
            