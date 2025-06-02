
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
import sad.gruppo11.Model.geometry.Rect;
import sad.gruppo11.Model.geometry.ColorData;

class RotateShapeCommandTest {

    private Drawing mockDrawing;
    private Shape mockShape;
    private double oldAngle;
    private double targetAngle;
    private RotateShapeCommand command;

    @BeforeEach
    void setUp() {
        mockDrawing = mock(Drawing.class);
        oldAngle = 30.0;
        targetAngle = 90.0;

        // Use a spy on a real shape
        mockShape = spy(new RectangleShape(new Rect(0,0,1,1), ColorData.BLACK, ColorData.TRANSPARENT));
        when(mockShape.getRotation()).thenReturn(oldAngle); // Stub initial rotation

        command = new RotateShapeCommand(mockDrawing, mockShape, targetAngle);
    }

    @Test
    void constructor_nullDrawing_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> new RotateShapeCommand(null, mockShape, targetAngle));
    }

    @Test
    void constructor_nullShape_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> new RotateShapeCommand(mockDrawing, null, targetAngle));
    }

    @Test
    void execute_shouldSetNewRotationAndStoreOldRotation() {
        command.execute();

        ArgumentCaptor<Shape> shapeCaptor = ArgumentCaptor.forClass(Shape.class);
        ArgumentCaptor<Double> angleCaptor = ArgumentCaptor.forClass(Double.class);

        verify(mockDrawing, times(1)).setShapeRotation(shapeCaptor.capture(), angleCaptor.capture());
        assertSame(mockShape, shapeCaptor.getValue());
        assertEquals(targetAngle, angleCaptor.getValue(), 0.001);

        // Old angle storage is internal, tested via undo
    }

    @Test
    void undo_shouldSetOldRotation() {
        command.execute(); // oldAngle is stored
        clearInvocations(mockDrawing);

        command.undo();

        ArgumentCaptor<Shape> shapeCaptor = ArgumentCaptor.forClass(Shape.class);
        ArgumentCaptor<Double> angleCaptor = ArgumentCaptor.forClass(Double.class);

        verify(mockDrawing, times(1)).setShapeRotation(shapeCaptor.capture(), angleCaptor.capture());
        assertSame(mockShape, shapeCaptor.getValue());
        assertEquals(oldAngle, angleCaptor.getValue(), 0.001, "Undo should set the old rotation angle.");
    }
    
    @Test
    void undo_withoutExecute_shouldHandleGracefully() {
        // If execute() not called, oldAngle is not initialized (remains 0.0 by default for double).
        // The command's undo() will then call drawing.setShapeRotation with this uninitialized oldAngle.
        // This is acceptable if 0.0 is a valid default.
        // The current RotateShapeCommand initializes oldAngle in execute(). If not called, it's 0.0.
        // So, undo() would try to set rotation to 0.0.

        // Capture the oldAngle that would be used by undo without execute
        // It will be the default double value 0.0 because it's not set in execute()
        double angleBeforeExecute = mockShape.getRotation(); // This is 30.0 from spy setup
        
        command.undo(); // oldAngle inside command is 0.0

        ArgumentCaptor<Double> angleCaptor = ArgumentCaptor.forClass(Double.class);
        verify(mockDrawing, times(1)).setShapeRotation(eq(mockShape), angleCaptor.capture());
        assertEquals(0.0, angleCaptor.getValue(), 0.001, 
            "Undo without execute should attempt to set rotation to the uninitialized oldAngle (0.0).");
        
        // Ensure shape's actual rotation isn't changed from its initial if drawing.setShapeRotation doesn't run
        // This depends on whether drawing.setShapeRotation would be called with 0.0
        // The verify above confirms it *is* called.
    }

    @Test
    void toString_shouldReturnMeaningfulString() {
        String str = command.toString();
        assertTrue(str.contains("RotateShapeCommand"));
        assertTrue(str.contains(mockShape.getId().toString()));
        assertTrue(str.contains(String.valueOf(targetAngle)));
    }
}
            