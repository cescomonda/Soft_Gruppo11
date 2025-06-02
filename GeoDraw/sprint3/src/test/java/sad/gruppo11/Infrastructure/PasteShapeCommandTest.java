
package sad.gruppo11.Infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.LineSegment; // Concrete shape
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Vector2D;

import java.util.UUID;

class PasteShapeCommandTest {

    private Drawing mockDrawing;
    private Clipboard mockClipboard;
    private Shape shapeFromClipboard;
    private Shape clonedShapeForPasting; // The one with new ID
    private Vector2D pasteOffset;
    private PasteShapeCommand command;
    private UUID originalShapeId;
    private UUID pastedShapeId;


    @BeforeEach
    void setUp() {
        mockDrawing = mock(Drawing.class);
        mockClipboard = mock(Clipboard.class);
        pasteOffset = new Vector2D(10, 10);

        originalShapeId = UUID.randomUUID();
        pastedShapeId = UUID.randomUUID(); // Different ID for the pasted instance

        // Shape that would be "on the clipboard" (already a clone of an original)
        shapeFromClipboard = mock(Shape.class); // Using a mock for flexibility
        when(shapeFromClipboard.getId()).thenReturn(originalShapeId);

        // The shape that results from shapeFromClipboard.cloneWithNewId()
        clonedShapeForPasting = mock(Shape.class);
        when(clonedShapeForPasting.getId()).thenReturn(pastedShapeId);

        // Stubbing the clipboard's get method
        when(mockClipboard.get()).thenReturn(shapeFromClipboard);
        // Stubbing the cloneWithNewId method
        when(shapeFromClipboard.cloneWithNewId()).thenReturn(clonedShapeForPasting);
        
        command = new PasteShapeCommand(mockDrawing, mockClipboard, pasteOffset);
    }

    @Test
    void constructor_nullDrawing_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> new PasteShapeCommand(null, mockClipboard, pasteOffset));
    }

    @Test
    void constructor_nullClipboard_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> new PasteShapeCommand(mockDrawing, null, pasteOffset));
    }
    
    @Test
    void constructor_nullOffset_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> new PasteShapeCommand(mockDrawing, mockClipboard, null));
    }

    @Test
    void execute_clipboardNotEmpty_shouldCloneMoveAndAddShapeToDrawing() {
        command.execute();

        // Verify clipboard.get() was called
        verify(mockClipboard, times(1)).get();
        // Verify cloneWithNewId() was called on the shape from clipboard
        verify(shapeFromClipboard, times(1)).cloneWithNewId();
        // Verify the new clone was moved
        verify(clonedShapeForPasting, times(1)).move(pasteOffset);
        // Verify the new clone was added to drawing
        ArgumentCaptor<Shape> shapeCaptor = ArgumentCaptor.forClass(Shape.class);
        verify(mockDrawing, times(1)).addShape(shapeCaptor.capture());
        
        Shape addedShape = shapeCaptor.getValue();
        assertSame(clonedShapeForPasting, addedShape, "The cloned and moved shape should be added.");
        assertEquals(pastedShapeId, addedShape.getId(), "Added shape should have the new ID.");
    }

    @Test
    void execute_clipboardEmpty_shouldDoNothingToDrawing() {
        when(mockClipboard.get()).thenReturn(null); // Simulate empty clipboard
        PasteShapeCommand cmdWithEmptyClipboard = new PasteShapeCommand(mockDrawing, mockClipboard, pasteOffset);

        cmdWithEmptyClipboard.execute();

        verify(mockClipboard, times(1)).get();
        verify(mockDrawing, never()).addShape(any(Shape.class));
        verify(shapeFromClipboard, never()).cloneWithNewId(); // Original mock shapeFromClipboard shouldn't be touched
        verify(clonedShapeForPasting, never()).move(any(Vector2D.class));
    }

    @Test
    void undo_shapeWasPasted_shouldRemovePastedShapeFromDrawing() {
        command.execute(); // pastedShapeInstance is set
        clearInvocations(mockDrawing, mockClipboard, shapeFromClipboard, clonedShapeForPasting);

        command.undo();

        ArgumentCaptor<Shape> shapeCaptor = ArgumentCaptor.forClass(Shape.class);
        verify(mockDrawing, times(1)).removeShape(shapeCaptor.capture());
        Shape removedShape = shapeCaptor.getValue();
        assertSame(clonedShapeForPasting, removedShape, "The previously pasted shape instance should be removed.");
        assertEquals(pastedShapeId, removedShape.getId());
    }

    @Test
    void undo_clipboardWasEmptyOnExecute_shouldDoNothing() {
        when(mockClipboard.get()).thenReturn(null);
        PasteShapeCommand cmdWithEmptyClipboard = new PasteShapeCommand(mockDrawing, mockClipboard, pasteOffset);
        cmdWithEmptyClipboard.execute(); // pastedShapeInstance will be null
        clearInvocations(mockDrawing);

        cmdWithEmptyClipboard.undo();
        verify(mockDrawing, never()).removeShape(any(Shape.class));
    }
    
    @Test
    void pasteOffset_isCopiedDefensively() {
        Vector2D originalOffset = new Vector2D(1,1);
        PasteShapeCommand cmd = new PasteShapeCommand(mockDrawing, mockClipboard, originalOffset);
        
        originalOffset.setDx(100); // Modify original offset
        
        cmd.execute(); // Should use offset (1,1)
        
        verify(clonedShapeForPasting).move(argThat(v -> v.getDx() == 1.0 && v.getDy() == 1.0));
    }


    @Test
    void toString_shouldReturnMeaningfulString() {
        command.execute(); // To set pastedShapeInstance
        String str = command.toString();
        assertTrue(str.contains("PasteShapeCommand"));
        assertTrue(str.contains("pastedShapeId=" + pastedShapeId));
        assertTrue(str.contains("offset=" + pasteOffset.toString()));
    }

    @Test
    void toString_clipboardEmptyOnExecute_shouldIndicateNoPaste() {
        when(mockClipboard.get()).thenReturn(null);
        PasteShapeCommand cmdWithEmptyClipboard = new PasteShapeCommand(mockDrawing, mockClipboard, pasteOffset);
        cmdWithEmptyClipboard.execute();
        String str = cmdWithEmptyClipboard.toString();
        assertTrue(str.contains("PasteShapeCommand"));
        assertTrue(str.contains("pastedShapeId=none_pasted"));
    }
}
            