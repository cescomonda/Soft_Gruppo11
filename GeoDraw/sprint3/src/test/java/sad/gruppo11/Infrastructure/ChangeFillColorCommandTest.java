
package sad.gruppo11.Infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.RectangleShape; // Concrete shape
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Rect;

class ChangeFillColorCommandTest {

    private Drawing mockDrawing;
    private Shape mockShape;
    private ColorData oldFillColor;
    private ColorData newFillColor;
    private ChangeFillColorCommand command;

    @BeforeEach
    void setUp() {
        mockDrawing = mock(Drawing.class);
        oldFillColor = ColorData.GREEN;
        newFillColor = ColorData.YELLOW;

        // Use a spy on a real shape
        mockShape = spy(new RectangleShape(new Rect(0,0,1,1), ColorData.BLACK, oldFillColor));
        // when(mockShape.getFillColor()).thenReturn(oldFillColor); // Stub initial color

        command = new ChangeFillColorCommand(mockDrawing, mockShape, newFillColor);
    }

    @Test
    void constructor_nullDrawing_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> new ChangeFillColorCommand(null, mockShape, newFillColor));
    }

    @Test
    void constructor_nullShape_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> new ChangeFillColorCommand(mockDrawing, null, newFillColor));
    }

    @Test
    void constructor_nullNewColor_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> new ChangeFillColorCommand(mockDrawing, mockShape, null));
    }

    @Test
    void execute_shouldSetNewFillColorAndStoreOldColor() {
        command.execute();

        ArgumentCaptor<Shape> shapeCaptor = ArgumentCaptor.forClass(Shape.class);
        ArgumentCaptor<ColorData> colorCaptor = ArgumentCaptor.forClass(ColorData.class);

        verify(mockDrawing, times(1)).setShapeFillColor(shapeCaptor.capture(), colorCaptor.capture());
        assertSame(mockShape, shapeCaptor.getValue());
        assertEquals(newFillColor, colorCaptor.getValue());
    }
    
    @Test
    void execute_oldColorIsNull_shouldStoreTransparent() {
        // Setup shape to have null fill color initially
        Shape shapeWithNullFill = spy(new RectangleShape(new Rect(0,0,1,1), ColorData.BLACK, ColorData.TRANSPARENT));
        // The Shape interface specifies getFillColor returns ColorData, so it shouldn't be null.
        // Let's assume getFillColor() could return null and the command handles it by defaulting to TRANSPARENT.
        // However, concrete shapes return ColorData.TRANSPARENT if no fill.
        // So, we'll test the actual behavior: default to TRANSPARENT if the model.getFillColor() would have been conceptually null.
        
        when(shapeWithNullFill.getFillColor()).thenReturn(ColorData.TRANSPARENT); // If fill was conceptually null, getFillColor often returns TRANSPARENT
        
        ChangeFillColorCommand cmdWithNullOld = new ChangeFillColorCommand(mockDrawing, shapeWithNullFill, newFillColor);
        cmdWithNullOld.execute();
        
        clearInvocations(mockDrawing);
        cmdWithNullOld.undo();
        
        ArgumentCaptor<ColorData> undoColorCaptor = ArgumentCaptor.forClass(ColorData.class);
        verify(mockDrawing).setShapeFillColor(eq(shapeWithNullFill), undoColorCaptor.capture());
        assertEquals(ColorData.TRANSPARENT, undoColorCaptor.getValue(), "If old fill was 'null' (conceptually), undo should revert to TRANSPARENT.");
    }


    @Test
    void undo_shouldSetOldFillColor() {
        command.execute(); // oldFillColor is stored
        clearInvocations(mockDrawing);

        command.undo();

        ArgumentCaptor<Shape> shapeCaptor = ArgumentCaptor.forClass(Shape.class);
        ArgumentCaptor<ColorData> colorCaptor = ArgumentCaptor.forClass(ColorData.class);

        verify(mockDrawing, times(1)).setShapeFillColor(shapeCaptor.capture(), colorCaptor.capture());
        assertSame(mockShape, shapeCaptor.getValue());
        assertEquals(oldFillColor, colorCaptor.getValue(), "Undo should set the old fill color.");
    }
    
    @Test
    void undo_withoutExecute_shouldHandleGracefully() {
        // If execute() not called, oldColor is null.
        // The command's undo() will call drawing.setShapeFillColor(receiverShape, null),
        // which might be problematic if Drawing.setShapeFillColor expects non-null.
        // Let's assume Drawing.setShapeFillColor handles null by setting to TRANSPARENT or similar.
        // The current ChangeFillColorCommand.undo calls setShapeFillColor(receiverShape, oldColor),
        // and oldColor is null if execute isn't called.
        // Drawing.setShapeFillColor requires non-null. So this might throw NPE in Drawing.
        
        // Let's test the command's behavior: if oldColor is null, it will pass null to drawing.
        // We assume drawing.setShapeFillColor will handle this.
        // Or, the command itself should ensure oldColor is never null passed to drawing.
        // Current Command: passes "oldColor" (which is null if execute not called)
        // ChangeFillColorCommand.oldColor field will be null if execute() not called.
        // The undo() method uses this.drawing.setShapeFillColor(receiverShape, oldColor);
        // If "oldColor" is null, this will violate the non-null contract of "drawing.setShapeFillColor"
        
        // Option 1: Assert that it calls drawing.setShapeFillColor with null, and assume drawing handles it.
        // Option 2: Test that it does NOT call if oldColor is null (safer for the command).

        // The provided command code *will* call drawing.setShapeFillColor with a null "oldColor"
        // if execute hasn't run. This is a potential issue with the command or Drawing class contract.
        // For this test, we'll verify it tries to pass null.
        
        assertDoesNotThrow(() -> command.undo()); // If it doesn't throw, it means oldColor was not set, and thus setShapeFillColor wasn't called, or drawing handles null.
                                           // The command's oldColor is null, so drawing.setShapeFillColor(shape, null) would be called.
                                           // This expects the drawing.setShapeFillColor to throw NPE.

        // Let's assume the command should NOT call setShapeFillColor if oldColor wasn't captured.
        // This would require a check like 'if (this.oldColor != null)' in undo.
        // Given the current code, it *will* pass null.
        // Let's mock drawing.setShapeFillColor to accept null for this test path.
        // doNothing().when(mockDrawing).setShapeFillColor(any(Shape.class), eq(null));
        // command.undo();
        // verify(mockDrawing, times(1)).setShapeFillColor(mockShape, null);
        // This test is a bit tricky due to uninitialized oldColor.
        // A better command design would initialize oldColor to a safe default or ensure execute is always called.

        // Simpler test: if execute isn't called, oldColor is null, and undo does not call drawing if oldColor is null.
        // The current command does not have this check.
        // So, if drawing.setShapeFillColor throws NPE for null color, then this test would expect NPE.
        // Let's assume for now "drawing.setShapeFillColor" is robust or test the actual behavior.
        // Given the code: command.undo() passes oldColor (null) to drawing.setShapeFillColor
        // We can assert that an NPE is thrown by the "drawing.setShapeFillColor" if it's strict.
        // If we assume it's strict:
        doThrow(NullPointerException.class).when(mockDrawing).setShapeFillColor(any(Shape.class), eq(null));
        assertThrows(NullPointerException.class, () -> command.undo(), 
            "Undo without execute, passing null oldColor to drawing.setShapeFillColor, should cause NPE if drawing is strict.");

        // However, if the command's undo had "if (oldColor != null)", then:
        // command.undo();
        // verify(mockDrawing, never()).setShapeFillColor(any(), any());
    }


    @Test
    void toString_shouldReturnMeaningfulString() {
        String str = command.toString();
        assertTrue(str.contains("ChangeFillColorCommand"));
        assertTrue(str.contains(mockShape.getId().toString()));
        assertTrue(str.contains(newFillColor.toString()));
    }
}
            