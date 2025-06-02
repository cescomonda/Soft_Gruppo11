
package sad.gruppo11.Infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.TextShape;
import sad.gruppo11.Model.LineSegment; // For testing wrong shape type
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.ColorData;

class ChangeTextSizeCommandTest {

    private Drawing mockDrawing; // Not directly used by this command's logic but passed to super
    private TextShape mockTextShape;
    private double oldSize;
    private double newSize;
    private ChangeTextSizeCommand command;

    @BeforeEach
    void setUp() {
        mockDrawing = mock(Drawing.class);
        oldSize = 12.0;
        newSize = 24.0;

        // Use a spy for TextShape
        mockTextShape = spy(new TextShape("Test", new Point2D(0,0), oldSize, "Arial", ColorData.BLACK));
        // when(mockTextShape.getFontSize()).thenReturn(oldSize); // Already set by constructor for spy

        command = new ChangeTextSizeCommand(mockDrawing, mockTextShape, newSize);
    }

    @Test
    void constructor_nullDrawing_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> new ChangeTextSizeCommand(null, mockTextShape, newSize));
    }

    @Test
    void constructor_nullShape_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> new ChangeTextSizeCommand(mockDrawing, null, newSize));
    }

    @Test
    void constructor_nonPositiveNewSize_shouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new ChangeTextSizeCommand(mockDrawing, mockTextShape, 0.0));
        assertThrows(IllegalArgumentException.class, () -> new ChangeTextSizeCommand(mockDrawing, mockTextShape, -1.0));
    }

    @Test
    void constructor_nonTextShape_shouldThrowIllegalArgumentException() {
        Shape notATextShape = mock(LineSegment.class); // Any non-TextShape
        assertThrows(IllegalArgumentException.class, () -> {
            new ChangeTextSizeCommand(mockDrawing, notATextShape, newSize);
        });
    }

    @Test
    void execute_shouldSetNewFontSizeAndStoreOldSize() {
        command.execute();

        // Verify that setFontSize was called on the TextShape with the new size
        verify(mockTextShape, times(1)).setFontSize(newSize);
        
        // Old size storage is internal, tested via undo
    }
    
    @Test
    void execute_calledMultipleTimes_oldSizeStoredOnlyOnce() {
        command.execute(); // Stores 12.0 as oldSize
        
        // Simulate font size changed elsewhere
        double intermediateSize = 18.0;
        when(mockTextShape.getFontSize()).thenReturn(intermediateSize);
        
        command.execute(); // oldSize in command should still be 12.0
        
        verify(mockTextShape, times(2)).setFontSize(newSize); // setFontSize(newSize) called twice
        
        // Undo should revert to 12.0
        clearInvocations(mockTextShape);
        command.undo();
        verify(mockTextShape).setFontSize(oldSize);
    }

    @Test
    void undo_shouldSetOldFontSize() {
        command.execute(); // oldSize is stored
        clearInvocations(mockTextShape);

        command.undo();

        // Verify that setFontSize was called on the TextShape with the old size
        verify(mockTextShape, times(1)).setFontSize(oldSize);
    }
    
    @Test
    void undo_withoutExecute_oldSizeIsZero_shouldSetZero() {
        // If execute() isn't called, oldSize in command is 0.0 (default for double).
        // TextShape.setFontSize throws IAE for non-positive size.
        ChangeTextSizeCommand cmd = new ChangeTextSizeCommand(mockDrawing, mockTextShape, 10.0);
        // cmd.oldSize is 0.0 here

        // TextShape's setFontSize will throw an IllegalArgumentException if 0.0 is passed
        doThrow(IllegalArgumentException.class).when(mockTextShape).setFontSize(0.0);
        
        assertThrows(IllegalArgumentException.class, () -> cmd.undo(),
            "Undo without execute, passing 0.0 oldSize to TextShape.setFontSize, should cause IAE.");
    }

    @Test
    void toString_shouldReturnMeaningfulString() {
        String str = command.toString();
        assertTrue(str.contains("ChangeTextSizeCommand"));
        assertTrue(str.contains(mockTextShape.getId().toString()));
        assertTrue(str.contains("newSize=" + newSize));
    }
}
            