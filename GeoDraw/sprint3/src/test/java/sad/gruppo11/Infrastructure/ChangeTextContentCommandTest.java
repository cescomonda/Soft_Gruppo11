
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

class ChangeTextContentCommandTest {

    private Drawing mockDrawing; // Drawing might not be directly used by this command's logic but passed to super
    private TextShape mockTextShape;
    private String oldContent;
    private String newContent;
    private ChangeTextContentCommand command;

    @BeforeEach
    void setUp() {
        mockDrawing = mock(Drawing.class);
        oldContent = "Old Text";
        newContent = "New Text";

        // Use a spy for TextShape to verify setText is called and to stub getText
        mockTextShape = spy(new TextShape(oldContent, new Point2D(0,0), 12, "Arial", ColorData.BLACK));
        // when(mockTextShape.getText()).thenReturn(oldContent); // Already set by constructor for spy

        command = new ChangeTextContentCommand(mockDrawing, mockTextShape, newContent);
    }

    @Test
    void constructor_nullDrawing_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> new ChangeTextContentCommand(null, mockTextShape, newContent));
    }

    @Test
    void constructor_nullShape_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> new ChangeTextContentCommand(mockDrawing, null, newContent));
    }

    @Test
    void constructor_nullNewContent_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> new ChangeTextContentCommand(mockDrawing, mockTextShape, null));
    }

    @Test
    void constructor_nonTextShape_shouldThrowIllegalArgumentException() {
        Shape notATextShape = mock(LineSegment.class);
        assertThrows(IllegalArgumentException.class, () -> {
            new ChangeTextContentCommand(mockDrawing, notATextShape, newContent);
        });
    }

    @Test
    void execute_shouldSetNewTextContentAndStoreOldContent() {
        command.execute();

        // Verify that setText was called on the TextShape with the new content
        verify(mockTextShape, times(1)).setText(newContent);
        
        // Old content storage is internal, tested via undo
    }
    
    @Test
    void execute_calledMultipleTimes_oldContentStoredOnlyOnce() {
        command.execute(); // Stores "Old Text" as oldContent
        
        // Simulate text content changed elsewhere
        String intermediateContent = "Intermediate Text";
        when(mockTextShape.getText()).thenReturn(intermediateContent);
        
        command.execute(); // oldContent in command should still be "Old Text"
        
        verify(mockTextShape, times(2)).setText(newContent); // setText(newContent) called twice
        
        // Undo should revert to "Old Text"
        clearInvocations(mockTextShape);
        command.undo();
        verify(mockTextShape).setText(oldContent);
    }

    @Test
    void undo_shouldSetOldTextContent() {
        command.execute(); // oldContent is stored
        clearInvocations(mockTextShape);

        command.undo();

        // Verify that setText was called on the TextShape with the old content
        verify(mockTextShape, times(1)).setText(oldContent);
    }
    
    @Test
    void undo_withoutExecute_oldContentIsNull_shouldSetNullOrEmpty() {
        // If execute() isn't called, oldContent in command is null.
        // TextShape.setText allows null (becomes empty string).
        ChangeTextContentCommand cmd = new ChangeTextContentCommand(mockDrawing, mockTextShape, "any");
        // cmd.oldContent is null here
        
        cmd.undo();
        verify(mockTextShape).setText(null); // Command will pass null to TextShape.setText
    }


    @Test
    void toString_shouldReturnMeaningfulString() {
        String str = command.toString();
        assertTrue(str.contains("ChangeTextContentCommand"));
        assertTrue(str.contains(mockTextShape.getId().toString()));
        assertTrue(str.contains("newContent='" + newContent + "'"));
    }
}
            