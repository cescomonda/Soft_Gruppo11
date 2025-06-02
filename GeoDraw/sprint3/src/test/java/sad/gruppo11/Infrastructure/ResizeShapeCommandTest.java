
package sad.gruppo11.Infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.RectangleShape; // Using a concrete shape
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Rect;

class ResizeShapeCommandTest {

    private Drawing mockDrawing;
    private Shape mockShape;
    private Rect oldBounds;
    private Rect newBounds;
    private ResizeShapeCommand resizeShapeCommand;

    @BeforeEach
    void setUp() {
        mockDrawing = mock(Drawing.class);
        
        oldBounds = new Rect(10, 10, 50, 50);
        // Create a real shape and stub its getBounds()
        mockShape = spy(new RectangleShape(oldBounds, ColorData.BLACK, ColorData.TRANSPARENT));
        // No need to explicitly stub getBounds() if we pass the shape to the command,
        // as the command calls getBounds() internally on the first execute.

        newBounds = new Rect(20, 20, 100, 100);
        resizeShapeCommand = new ResizeShapeCommand(mockDrawing, mockShape, newBounds);
    }

    @Test
    void constructor_nullDrawing_shouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> {
            new ResizeShapeCommand(null, mockShape, newBounds);
        });
    }

    @Test
    void constructor_nullShape_shouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> {
            new ResizeShapeCommand(mockDrawing, null, newBounds);
        });
    }

    @Test
    void constructor_nullNewBounds_shouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> {
            new ResizeShapeCommand(mockDrawing, mockShape, null);
        });
    }

    @Test
    void execute_shouldResizeShapeInDrawingAndStoreOldBounds() {
        // The command internally calls mockShape.getBounds() to store oldBounds on first execute.
        // Since mockShape is a spy on a real RectangleShape initialized with oldBounds, this will work.

        resizeShapeCommand.execute();

        ArgumentCaptor<Shape> shapeCaptor = ArgumentCaptor.forClass(Shape.class);
        ArgumentCaptor<Rect> rectCaptor = ArgumentCaptor.forClass(Rect.class);

        verify(mockDrawing, times(1)).resizeShape(shapeCaptor.capture(), rectCaptor.capture());
        assertSame(mockShape, shapeCaptor.getValue(), "The correct shape should be resized.");
        assertEquals(newBounds, rectCaptor.getValue(), "The shape should be resized to newBounds.");
        
        // To verify oldBounds was stored, we can't directly access it from the command instance
        // (it's private). We test its effect in the undo() method.
    }

    @Test
    void execute_calledMultipleTimes_oldBoundsStoredOnlyOnce() {
        resizeShapeCommand.execute(); // First execute stores oldBounds
        clearInvocations(mockDrawing, mockShape); // Clear spy interactions for mockShape

        // Change the "current" bounds of the shape in the drawing (conceptually)
        // For the test, we can directly modify the spy if needed, or ensure the mockDrawing.resizeShape
        // actually changes the bounds that getBounds() would return.
        // However, the command's logic is that oldBounds is set only if null.
        Rect intermediateBounds = new Rect(0,0,10,10);
        when(mockShape.getBounds()).thenReturn(intermediateBounds); // Simulate shape bounds changed elsewhere

        resizeShapeCommand.execute(); // Second execute should still use original oldBounds for undo

        verify(mockDrawing, times(1)).resizeShape(mockShape, newBounds); // Resized to newBounds again

        // Now, undo should revert to the *original* oldBounds, not intermediateBounds
        clearInvocations(mockDrawing);
        resizeShapeCommand.undo();
        
        ArgumentCaptor<Rect> undoRectCaptor = ArgumentCaptor.forClass(Rect.class);
        verify(mockDrawing, times(1)).resizeShape(eq(mockShape), undoRectCaptor.capture());
        assertEquals(oldBounds, undoRectCaptor.getValue(), "Undo should revert to the initially stored oldBounds.");
    }


    @Test
    void undo_shouldResizeShapeToOldBoundsInDrawing() {
        // Execute first to store oldBounds
        resizeShapeCommand.execute();
        clearInvocations(mockDrawing);

        resizeShapeCommand.undo();

        ArgumentCaptor<Shape> shapeCaptor = ArgumentCaptor.forClass(Shape.class);
        ArgumentCaptor<Rect> rectCaptor = ArgumentCaptor.forClass(Rect.class);

        verify(mockDrawing, times(1)).resizeShape(shapeCaptor.capture(), rectCaptor.capture());
        assertSame(mockShape, shapeCaptor.getValue(), "The correct shape should be involved in undo.");
        assertEquals(oldBounds, rectCaptor.getValue(), "Undo should resize the shape back to oldBounds.");
    }
    
    @Test
    void undo_withoutExecute_shouldDoNothingOrHandleGracefully() {
        // Current implementation of undo relies on oldBounds being set by execute.
        // If execute() was not called, oldBounds is null, and undo() does nothing.
        assertDoesNotThrow(() -> {
            resizeShapeCommand.undo();
        });
        verify(mockDrawing, never()).resizeShape(any(Shape.class), any(Rect.class));
    }


    @Test
    void newBounds_isCopiedDefensively() {
        Rect originalNewBounds = new Rect(1,1,1,1);
        ResizeShapeCommand cmd = new ResizeShapeCommand(mockDrawing, mockShape, originalNewBounds);
        
        originalNewBounds.setWidth(100); // Modify original after command creation
        
        cmd.execute(); 
        
        ArgumentCaptor<Rect> rectCaptor = ArgumentCaptor.forClass(Rect.class);
        verify(mockDrawing).resizeShape(eq(mockShape), rectCaptor.capture());
        
        assertEquals(1, rectCaptor.getValue().getWidth(), "Command should use a defensive copy of newBounds.");
    }

    @Test
    void toString_shouldReturnMeaningfulString() {
        String commandString = resizeShapeCommand.toString();
        assertTrue(commandString.contains("ResizeShapeCommand"), "toString should contain the class name.");
        assertTrue(commandString.contains(mockShape.getId().toString()), "toString should contain the shape ID.");
        assertTrue(commandString.contains(newBounds.toString()), "toString should contain the new bounds details.");
    }
}
            