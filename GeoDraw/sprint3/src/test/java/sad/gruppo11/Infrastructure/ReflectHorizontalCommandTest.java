
package sad.gruppo11.Infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.Shape;

class ReflectHorizontalCommandTest {

    private Drawing mockDrawing;
    private Shape mockShape;
    private ReflectHorizontalCommand command;

    @BeforeEach
    void setUp() {
        mockDrawing = mock(Drawing.class);
        mockShape = mock(Shape.class); // Use a pure mock for Shape
        command = new ReflectHorizontalCommand(mockDrawing, mockShape);
    }
    
    @Test
    void constructor_nullDrawing_shouldThrowException() {
        // The AbstractShapeCommand takes drawing, but ReflectHorizontalCommand currently doesn't check it.
        // AbstractShapeCommand checks receiver (shape) but not drawing.
        // Let's assume constructor should be robust.
        // The provided ReflectHorizontalCommand *does* take drawing and pass it to super.
        // And AbstractShapeCommand constructor does *not* check drawing for null.
        // So, this test might pass if drawing is null.
        // For robustness, AbstractShapeCommand should probably check drawing.
        // Test as is:
        assertThrows(NullPointerException.class, () -> new ReflectHorizontalCommand(null, mockShape));
        
        // If AbstractShapeCommand was stricter for drawing:
        // assertThrows(NullPointerException.class, () -> new ReflectHorizontalCommand(null, mockShape));
    }

    @Test
    void constructor_nullShape_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> new ReflectHorizontalCommand(mockDrawing, null));
    }

    @Test
    void execute_shouldCallReflectHorizontalOnDrawingWithShape() {
        command.execute();
        // The command calls drawing.reflectShapeHorizontal(receiverShape)
        verify(mockDrawing, times(1)).reflectShapeHorizontal(mockShape);
    }

    @Test
    void undo_shouldCallReflectHorizontalOnDrawingWithShapeAgain() {
        command.execute(); // To simulate a state to undo from
        clearInvocations(mockDrawing);

        command.undo();
        // Since reflection is its own inverse for the shape's method
        verify(mockDrawing, times(1)).reflectShapeHorizontal(mockShape);
    }

}
            