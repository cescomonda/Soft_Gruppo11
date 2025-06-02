
package sad.gruppo11.Infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.Shape;

class ReflectVerticalCommandTest {

    private Drawing mockDrawing;
    private Shape mockShape;
    private ReflectVerticalCommand command;

    @BeforeEach
    void setUp() {
        mockDrawing = mock(Drawing.class);
        mockShape = mock(Shape.class); // Use a pure mock for Shape
        command = new ReflectVerticalCommand(mockDrawing, mockShape);
    }

    @Test
    void constructor_nullDrawing_shouldBeAllowedByCurrentAbstractShapeCommand() {
        assertThrows(NullPointerException.class, () -> new ReflectVerticalCommand(null, mockShape));
    }

    @Test
    void constructor_nullShape_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> new ReflectVerticalCommand(mockDrawing, null));
    }

    @Test
    void execute_shouldCallReflectVerticalOnDrawingWithShape() {
        command.execute();
        verify(mockDrawing, times(1)).reflectShapeVertical(mockShape);
    }

    @Test
    void undo_shouldCallReflectVerticalOnDrawingWithShapeAgain() {
        command.execute();
        clearInvocations(mockDrawing);

        command.undo();
        verify(mockDrawing, times(1)).reflectShapeVertical(mockShape);
    }

}
            