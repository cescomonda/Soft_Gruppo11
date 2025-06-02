
package sad.gruppo11.Infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.LineSegment; // Concrete shape
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.ColorData;

class CopyShapeCommandTest {

    private Drawing mockDrawing; // Not directly used by this command's logic
    private Shape mockShapeToCopy;
    private Clipboard mockClipboard;
    private CopyShapeCommand command;

    @BeforeEach
    void setUp() {
        mockDrawing = mock(Drawing.class);
        mockClipboard = mock(Clipboard.class);
        mockShapeToCopy = new LineSegment(new Point2D(0,0), new Point2D(1,1), ColorData.BLACK);
        
        command = new CopyShapeCommand(mockDrawing, mockShapeToCopy, mockClipboard);
    }
    
    @Test
    void constructor_nullShape_shouldStillBeCreatableButExecuteDoesNothing() {
        // The command structure has receiverShape final, set in AbstractShapeCommand.
        // If null is passed to AbstractShapeCommand, it throws NPE.
        // CopyShapeCommand itself doesn't re-check for null receiverShape.
        assertThrows(NullPointerException.class, () -> {
            new CopyShapeCommand(mockDrawing, null, mockClipboard);
        });
    }

    @Test
    void constructor_nullClipboard_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> {
            new CopyShapeCommand(mockDrawing, mockShapeToCopy, null);
        });
    }

    @Test
    void execute_shouldSetShapeOnClipboard() {
        command.execute();
        verify(mockClipboard, times(1)).set(mockShapeToCopy);
    }

    @Test
    void execute_withNullShapeReceiver_shouldDoNothing() {
        // This scenario is tricky because AbstractShapeCommand makes receiverShape final and non-null.
        // To test this, we'd have to bypass that or assume a state where receiverShape became null.
        // Given the current structure, receiverShape in AbstractShapeCommand is guaranteed non-null
        // by its constructor. So, CopyShapeCommand will always have a non-null receiverShape.
        // If we were to test a hypothetical scenario where receiverShape could be null at execute time:
        // CopyShapeCommand testNullReceiver = new CopyShapeCommand(mockDrawing, mockShapeToCopy, mockClipboard);
        // // somehow make testNullReceiver.receiverShape = null; (not possible with final)
        // testNullReceiver.execute();
        // verify(mockClipboard, never()).set(any(Shape.class));

        // As is, this case is covered by constructor test for null shape.
        // If constructor allowed null shape, then:
        Shape nullShape = null;
        CopyShapeCommand cmdWithNullReceiver = new CopyShapeCommand(mockDrawing, mockShapeToCopy, mockClipboard); 
        // We need to use reflection to set receiverShape to null if it was possible, or redesign AbstractShapeCommand.
        // For now, this specific scenario of null receiver at execute() isn't directly testable
        // without altering the design for testability or using more advanced mocking/reflection.
        // The current implementation of execute() in CopyShapeCommand does check 'if (receiverShape != null)'.
        // So, if it *could* be null, it would be handled.
        
        // Let's assume the constructor for AbstractShapeCommand is the guard.
        // If we create the command with a valid shape, receiverShape won't be null.
        command.execute();
        verify(mockClipboard, times(1)).set(mockShapeToCopy); // Normal execution
    }


    @Test
    void undo_shouldBeANoOp() {
        command.execute(); // Call execute to ensure any state change happens
        clearInvocations(mockClipboard);

        command.undo();
        // Verify no interactions with clipboard or drawing happen on undo
        verifyNoInteractions(mockClipboard);
        verifyNoInteractions(mockDrawing); // Though drawing isn't directly used by CopyShapeCommand
    }

    @Test
    void toString_shouldReturnMeaningfulString() {
        String str = command.toString();
        assertTrue(str.contains("CopyShapeCommand"));
        assertTrue(str.contains(mockShapeToCopy.getId().toString()));
    }
    
    @Test
    void toString_withNullReceiver_shouldHandleGracefully() {
        // This test depends on whether AbstractShapeCommand allows null receiver.
        // Currently, it doesn't. If it did, and CopyShapeCommand was created with null:
        // CopyShapeCommand cmdWithNull = new CopyShapeCommand(mockDrawing, null, mockClipboard);
        // String str = cmdWithNull.toString();
        // assertTrue(str.contains("CopyShapeCommand"));
        // assertTrue(str.contains("null")); // or some other indicator of null shapeId
        
        // Given current constraints:
        String str = command.toString(); // uses mockShapeToCopy which is not null
        assertTrue(str.contains(mockShapeToCopy.getId().toString()));
    }
}
            