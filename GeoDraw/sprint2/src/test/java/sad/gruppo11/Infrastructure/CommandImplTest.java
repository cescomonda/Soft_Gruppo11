
package sad.gruppo11.Infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.TextShape;
import sad.gruppo11.Model.RectangleShape;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Rect;
import sad.gruppo11.Model.geometry.Vector2D;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CommandImplTest {

    @Mock private Drawing mockDrawing;
    @Mock private Shape mockShape;
    @Mock private TextShape mockTextShape; // Specific mock for text commands
    @Mock private Clipboard mockClipboard;

    private Shape realShape; // For commands that need to modify state

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        realShape = new RectangleShape(new Rect(10, 10, 20, 20), ColorData.BLACK, ColorData.TRANSPARENT);
        when(mockShape.getId()).thenReturn(UUID.randomUUID()); // Give mockShape an ID for toString
        when(mockTextShape.getId()).thenReturn(UUID.randomUUID());
    }

    @Test
    void addShapeCommand() {
        AddShapeCommand cmd = new AddShapeCommand(mockDrawing, mockShape);
        cmd.execute();
        verify(mockDrawing).addShape(mockShape);
        cmd.undo();
        verify(mockDrawing).removeShape(mockShape);
        assertNotNull(cmd.toString());
    }

    @Test
    void deleteShapeCommand() {
        when(mockDrawing.getShapeIndex(mockShape)).thenReturn(0); // Assume shape exists for removal
        DeleteShapeCommand cmd = new DeleteShapeCommand(mockDrawing, mockShape);
        cmd.execute();
        verify(mockDrawing).removeShape(mockShape);
        cmd.undo();
        // Check if it's added back at the original index or end
        verify(mockDrawing).addShapeAtIndex(mockShape, 0); 
        assertNotNull(cmd.toString());
    }
    
    @Test
    void deleteShapeCommandUndoWithDifferentListSize() {
        when(mockDrawing.getShapeIndex(realShape)).thenReturn(5); // Original index
        when(mockDrawing.getModifiableShapesList()).thenReturn(new ArrayList<>()); // Simulate list became smaller
        
        DeleteShapeCommand cmd = new DeleteShapeCommand(mockDrawing, realShape);
        cmd.execute(); // originalIndex is set to 5
        
        // During undo, if originalIndex (5) > current list size (e.g. 0), it should add to end
        cmd.undo();
        verify(mockDrawing).addShape(realShape); // Verifies fallback to add at end
    }


    @Test
    void moveShapeCommand() {
        Vector2D moveVec = new Vector2D(5, 5);
        MoveShapeCommand cmd = new MoveShapeCommand(mockShape, moveVec);
        cmd.execute();
        verify(mockShape).move(moveVec);
        cmd.undo();
        verify(mockShape).move(moveVec.inverse());
        assertNotNull(cmd.toString());
    }

    @Test
    void resizeShapeCommand() {
        Rect oldBounds = new Rect(0,0,10,10);
        Rect newBounds = new Rect(5,5,20,20);
        when(mockShape.getBounds()).thenReturn(oldBounds); // For capturing oldBounds

        ResizeShapeCommand cmd = new ResizeShapeCommand(mockShape, newBounds);
        cmd.execute();
        verify(mockShape).resize(newBounds);
        cmd.undo();
        verify(mockShape).resize(oldBounds);
        assertNotNull(cmd.toString());
    }

    @Test
    void changeStrokeColorCommand() {
        ColorData oldColor = ColorData.RED;
        ColorData newColor = ColorData.BLUE;
        when(mockShape.getStrokeColor()).thenReturn(oldColor);

        ChangeStrokeColorCommand cmd = new ChangeStrokeColorCommand(mockShape, newColor);
        cmd.execute();
        verify(mockShape).setStrokeColor(newColor);
        cmd.undo();
        verify(mockShape).setStrokeColor(oldColor);
        assertNotNull(cmd.toString());
    }

    @Test
    void changeFillColorCommand() {
        ColorData oldColor = ColorData.GREEN;
        ColorData newColor = ColorData.YELLOW;
        when(mockShape.getFillColor()).thenReturn(oldColor);

        ChangeFillColorCommand cmd = new ChangeFillColorCommand(mockShape, newColor);
        cmd.execute();
        verify(mockShape).setFillColor(newColor);
        cmd.undo();
        verify(mockShape).setFillColor(oldColor);
        assertNotNull(cmd.toString());
    }
    
    @Test
    void changeFillColorCommandWithNullInitialFill() {
        ColorData newColor = ColorData.YELLOW;
        when(mockShape.getFillColor()).thenReturn(null); // Model might return null

        ChangeFillColorCommand cmd = new ChangeFillColorCommand(mockShape, newColor);
        cmd.execute();
        verify(mockShape).setFillColor(newColor);
        cmd.undo();
        verify(mockShape).setFillColor(ColorData.TRANSPARENT); // Should revert to TRANSPARENT if old was null
    }


    @Test
    void cutShapeCommand() {
        when(mockDrawing.getShapeIndex(mockShape)).thenReturn(0);
        CutShapeCommand cmd = new CutShapeCommand(mockShape, mockDrawing, mockClipboard);
        cmd.execute();
        verify(mockClipboard).set(mockShape);
        verify(mockDrawing).removeShape(mockShape);
        cmd.undo();
        verify(mockDrawing).addShapeAtIndex(mockShape, 0);
        assertNotNull(cmd.toString());
    }

    @Test
    void copyShapeCommand() {
        CopyShapeCommand cmd = new CopyShapeCommand(mockShape, mockClipboard);
        cmd.execute();
        verify(mockClipboard).set(mockShape);
        cmd.undo(); // No-op for undo
        verifyNoMoreInteractions(mockClipboard); // Ensure set is not called again on undo
        assertNotNull(cmd.toString());
    }

    @Test
    void pasteShapeCommand() {
        Shape shapeToPaste = new RectangleShape(new Rect(0,0,5,5), ColorData.BLACK, ColorData.TRANSPARENT);
        Vector2D offset = new Vector2D(1,1);
        when(mockClipboard.get()).thenReturn(shapeToPaste); // Clipboard returns a clone
        
        // Need a real drawing to test addShape and a real shape for move
        Drawing realDrawing = new Drawing();
        PasteShapeCommand cmd = new PasteShapeCommand(realDrawing, mockClipboard, offset);
        cmd.execute();
        
        assertEquals(1, realDrawing.getShapesInZOrder().size());
        Shape pasted = realDrawing.getShapesInZOrder().get(0);
        assertEquals(1, pasted.getBounds().getX()); // Check if offset was applied
        assertEquals(1, pasted.getBounds().getY());

        cmd.undo();
        assertTrue(realDrawing.getShapesInZOrder().isEmpty());
        assertNotNull(cmd.toString());
    }
    
    @Test
    void pasteShapeCommandEmptyClipboard() {
        when(mockClipboard.get()).thenReturn(null);
        Vector2D offset = new Vector2D(1,1);
        PasteShapeCommand cmd = new PasteShapeCommand(mockDrawing, mockClipboard, offset);
        cmd.execute();
        verify(mockDrawing, never()).addShape(any(Shape.class)); // No shape added
        cmd.undo(); // Should not throw error
        assertNotNull(cmd.toString());
    }


    @Test
    void bringToFrontCommand() {
        when(mockDrawing.getShapeIndex(mockShape)).thenReturn(0);
        BringToFrontCommand cmd = new BringToFrontCommand(mockDrawing, mockShape);
        cmd.execute();
        verify(mockDrawing).bringToFront(mockShape);
        cmd.undo();
        verify(mockDrawing, times(1)).removeShape(mockShape); // Called during undo logic
        verify(mockDrawing).addShapeAtIndex(mockShape, 0);
        assertNotNull(cmd.toString());
    }

    @Test
    void sendToBackCommand() {
        when(mockDrawing.getShapeIndex(mockShape)).thenReturn(1);
        SendToBackCommand cmd = new SendToBackCommand(mockDrawing, mockShape);
        cmd.execute();
        verify(mockDrawing).sendToBack(mockShape);
        cmd.undo();
        verify(mockDrawing, times(1)).removeShape(mockShape);
        verify(mockDrawing).addShapeAtIndex(mockShape,1);
        assertNotNull(cmd.toString());
    }

    @Test
    void rotateShapeCommand() {
        double oldAngle = 30.0;
        double newAngle = 90.0;
        when(mockShape.getRotation()).thenReturn(oldAngle);

        RotateShapeCommand cmd = new RotateShapeCommand(mockShape, newAngle);
        cmd.execute();
        verify(mockShape).setRotation(newAngle);
        cmd.undo();
        verify(mockShape).setRotation(oldAngle);
        assertNotNull(cmd.toString());
    }

    @Test
    void changeTextContentCommand() {
        String oldText = "Hello";
        String newText = "World";
        when(mockTextShape.getText()).thenReturn(oldText);
        
        ChangeTextContentCommand cmd = new ChangeTextContentCommand(mockTextShape, newText);
        cmd.execute();
        verify(mockTextShape).setText(newText);
        cmd.undo();
        verify(mockTextShape).setText(oldText);
        assertNotNull(cmd.toString());
    }
    
    @Test
    void changeTextContentCommandRequiresTextShape() {
         assertThrows(IllegalArgumentException.class, () -> new ChangeTextContentCommand(mockShape, "text"));
    }

    @Test
    void changeTextSizeCommand() {
        double oldSize = 12.0;
        double newSize = 24.0;
        when(mockTextShape.getFontSize()).thenReturn(oldSize);

        ChangeTextSizeCommand cmd = new ChangeTextSizeCommand(mockTextShape, newSize);
        cmd.execute();
        verify(mockTextShape).setFontSize(newSize);
        cmd.undo();
        verify(mockTextShape).setFontSize(oldSize);
        assertNotNull(cmd.toString());
    }

    @Test
    void changeTextSizeCommandRequiresTextShape() {
         assertThrows(IllegalArgumentException.class, () -> new ChangeTextSizeCommand(mockShape, 12.0));
    }
    
    @Test
    void changeTextSizeCommandRequiresPositiveSize() {
        assertThrows(IllegalArgumentException.class, () -> new ChangeTextSizeCommand(mockTextShape, 0));
        assertThrows(IllegalArgumentException.class, () -> new ChangeTextSizeCommand(mockTextShape, -5));
    }
    
    // Abstract command constructor tests
    @Test
    void abstractDrawingCommandConstructorNullCheck() {
        assertThrows(NullPointerException.class, () -> new AbstractDrawingCommand(null) {
            @Override public void execute() {}
            @Override public void undo() {}
        });
    }

    @Test
    void abstractShapeCommandConstructorNullCheck() {
        assertThrows(NullPointerException.class, () -> new AbstractShapeCommand(null) {
            @Override public void execute() {}
            @Override public void undo() {}
        });
    }
}
