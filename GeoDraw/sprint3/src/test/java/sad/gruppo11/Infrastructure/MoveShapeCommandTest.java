
package sad.gruppo11.Infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.LineSegment;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Vector2D;

class MoveShapeCommandTest {

    private Drawing mockDrawing;
    private Shape mockShape;
    private Vector2D moveVector;
    private Vector2D inverseMoveVector;
    private MoveShapeCommand moveShapeCommand;

    @BeforeEach
    void setUp() {
        mockDrawing = mock(Drawing.class);
        mockShape = new LineSegment(new Point2D(0, 0), new Point2D(10, 10), ColorData.BLACK); // Real shape
        moveVector = new Vector2D(5, -5);
        inverseMoveVector = moveVector.inverse(); // Correctly: new Vector2D(-5, 5);
        moveShapeCommand = new MoveShapeCommand(mockDrawing, mockShape, moveVector);
    }

    @Test
    void constructor_nullDrawing_shouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> {
            new MoveShapeCommand(null, mockShape, moveVector);
        });
    }
    
    @Test
    void constructor_nullShape_shouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> {
            new MoveShapeCommand(mockDrawing, null, moveVector);
        });
    }

    @Test
    void constructor_nullVector_shouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> {
            new MoveShapeCommand(mockDrawing, mockShape, null);
        });
    }

    @Test
    void execute_shouldMoveShapeInDrawing() {
        moveShapeCommand.execute();

        ArgumentCaptor<Shape> shapeCaptor = ArgumentCaptor.forClass(Shape.class);
        ArgumentCaptor<Vector2D> vectorCaptor = ArgumentCaptor.forClass(Vector2D.class);

        verify(mockDrawing, times(1)).moveShape(shapeCaptor.capture(), vectorCaptor.capture());
        assertSame(mockShape, shapeCaptor.getValue(), "The correct shape should be moved.");
        assertEquals(moveVector, vectorCaptor.getValue(), "The correct move vector should be used.");
    }

    @Test
    void undo_shouldMoveShapeByInverseVectorInDrawing() {
        // Execute first
        moveShapeCommand.execute();
        clearInvocations(mockDrawing);

        moveShapeCommand.undo();

        ArgumentCaptor<Shape> shapeCaptor = ArgumentCaptor.forClass(Shape.class);
        ArgumentCaptor<Vector2D> vectorCaptor = ArgumentCaptor.forClass(Vector2D.class);
        
        // The MoveShapeCommand's undo method in the provided code simply calls moveShape with the original vector.
        // This assumes that the Drawing's moveShape method handles the "inverse" logic or that the command's
        // intent is to apply the same delta again for undo (which is unusual for a move command).
        // Based on the provided MoveShapeCommand, it will re-apply the *original* moveVector.
        // If the intent was inverse, the Command's undo should use moveVector.inverse().
        // Let's test according to the provided code:
        
        verify(mockDrawing, times(1)).moveShape(shapeCaptor.capture(), vectorCaptor.capture());
        assertSame(mockShape, shapeCaptor.getValue(), "The correct shape should be moved for undo.");
        
        // If MoveShapeCommand.undo() was changed to use moveVector.inverse():
        assertEquals(inverseMoveVector, vectorCaptor.getValue(), "Undo should use the inverse move vector.");
    }
    
    @Test
    void moveVector_isCopiedDefensively() {
        Vector2D originalVector = new Vector2D(1,1);
        MoveShapeCommand cmd = new MoveShapeCommand(mockDrawing, mockShape, originalVector);
        
        originalVector.setDx(100); // Modify original after command creation
        
        cmd.execute(); // Should use the (1,1) vector, not (100,1)
        
        ArgumentCaptor<Vector2D> vectorCaptor = ArgumentCaptor.forClass(Vector2D.class);
        verify(mockDrawing).moveShape(eq(mockShape), vectorCaptor.capture());
        
        assertEquals(1, vectorCaptor.getValue().getDx(), "Command should use a defensive copy of the move vector.");
        assertEquals(1, vectorCaptor.getValue().getDy(), "Command should use a defensive copy of the move vector.");
    }


    @Test
    void toString_shouldReturnMeaningfulString() {
        String commandString = moveShapeCommand.toString();
        assertTrue(commandString.contains("MoveShapeCommand"), "toString should contain the class name.");
        assertTrue(commandString.contains(mockShape.getId().toString()), "toString should contain the shape ID.");
        assertTrue(commandString.contains(moveVector.toString()), "toString should contain the move vector details.");
    }
}
            