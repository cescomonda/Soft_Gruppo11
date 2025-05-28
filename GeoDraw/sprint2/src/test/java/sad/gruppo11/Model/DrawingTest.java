
package sad.gruppo11.Model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Rect;
import sad.gruppo11.View.Observer;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class DrawingTest {
    private Drawing drawing;
    private Shape shape1, shape2, shape3;
    private Observer mockObserver;

    @BeforeEach
    void setUp() {
        drawing = new Drawing();
        shape1 = new RectangleShape(new Rect(0,0,10,10), ColorData.BLACK, ColorData.TRANSPARENT);
        shape2 = new EllipseShape(new Rect(10,10,5,5), ColorData.RED, ColorData.WHITE);
        shape3 = new LineSegment(new Point2D(0,0), new Point2D(1,1), ColorData.BLUE);
        mockObserver = Mockito.mock(Observer.class);
        drawing.attach(mockObserver);
    }

    @Test
    void addShape() {
        drawing.addShape(shape1);
        assertEquals(1, drawing.getShapesInZOrder().size());
        assertTrue(drawing.getShapesInZOrder().contains(shape1));
        verify(mockObserver).update(eq(drawing), any(Drawing.DrawingChangeEvent.class));
    }

    @Test
    void addShapeAtIndex() {
        drawing.addShape(shape1);
        drawing.addShape(shape3); // [shape1, shape3]
        drawing.addShapeAtIndex(shape2, 1); // [shape1, shape2, shape3]
        
        List<Shape> shapes = drawing.getShapesInZOrder();
        assertEquals(3, shapes.size());
        assertEquals(shape1, shapes.get(0));
        assertEquals(shape2, shapes.get(1));
        assertEquals(shape3, shapes.get(2));
        verify(mockObserver, times(3)).update(eq(drawing), any(Drawing.DrawingChangeEvent.class));
    }
    
    @Test
    void addShapeAtIndexOutOfBounds() {
        assertThrows(IndexOutOfBoundsException.class, () -> drawing.addShapeAtIndex(shape1, 1));
        drawing.addShape(shape1); // Now size is 1
        assertThrows(IndexOutOfBoundsException.class, () -> drawing.addShapeAtIndex(shape2, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> drawing.addShapeAtIndex(shape2, 3)); // size is 1, valid indices 0, 1
    }


    @Test
    void removeShape() {
        drawing.addShape(shape1);
        drawing.addShape(shape2);
        assertTrue(drawing.removeShape(shape1));
        assertEquals(1, drawing.getShapesInZOrder().size());
        assertFalse(drawing.getShapesInZOrder().contains(shape1));
        assertTrue(drawing.getShapesInZOrder().contains(shape2));
        verify(mockObserver, times(3)).update(eq(drawing), any(Drawing.DrawingChangeEvent.class)); // 2 add, 1 remove
        
        assertFalse(drawing.removeShape(shape1)); // Already removed
        verify(mockObserver, times(3)).update(eq(drawing), any(Drawing.DrawingChangeEvent.class)); // No new notification
    }
    
    @Test
    void removeShapeById() {
        drawing.addShape(shape1);
        drawing.addShape(shape2);
        UUID idToRemove = shape1.getId();
        
        Shape removed = drawing.removeShapeById(idToRemove);
        assertEquals(shape1, removed);
        assertEquals(1, drawing.getShapesInZOrder().size());
        assertFalse(drawing.getShapesInZOrder().contains(shape1));
        
        Shape notRemoved = drawing.removeShapeById(UUID.randomUUID()); // Non-existent ID
        assertNull(notRemoved);
        assertEquals(1, drawing.getShapesInZOrder().size());
        verify(mockObserver, times(3)).update(eq(drawing), any(Drawing.DrawingChangeEvent.class));
    }

    @Test
    void clear() {
        drawing.addShape(shape1);
        drawing.addShape(shape2);
        drawing.clear();
        assertTrue(drawing.getShapesInZOrder().isEmpty());
        verify(mockObserver, times(3)).update(eq(drawing), any(Drawing.DrawingChangeEvent.class)); // 2 add, 1 clear
        
        drawing.clear(); // Clear empty drawing
        verify(mockObserver, times(3)).update(eq(drawing), any(Drawing.DrawingChangeEvent.class)); // No new notification
    }

    @Test
    void getShapesInZOrderUnmodifiable() {
        drawing.addShape(shape1);
        List<Shape> shapes = drawing.getShapesInZOrder();
        assertThrows(UnsupportedOperationException.class, () -> shapes.add(shape2));
    }
    
    @Test
    void getModifiableShapesList() {
        drawing.addShape(shape1);
        List<Shape> shapes = drawing.getModifiableShapesList();
        shapes.add(shape2); // This should modify the internal list
        assertEquals(2, drawing.getShapesInZOrder().size());
        // Note: Modifying directly bypasses notifications. This method is likely for commands.
    }

    @Test
    void findShapeById() {
        drawing.addShape(shape1);
        assertEquals(shape1, drawing.findShapeById(shape1.getId()));
        assertNull(drawing.findShapeById(UUID.randomUUID()));
    }

    @Test
    void getShapeIndex() {
        drawing.addShape(shape1);
        drawing.addShape(shape2);
        assertEquals(0, drawing.getShapeIndex(shape1));
        assertEquals(1, drawing.getShapeIndex(shape2));
        assertEquals(-1, drawing.getShapeIndex(shape3));
    }

    @Test
    void bringToFront() {
        drawing.addShape(shape1); // [s1]
        drawing.addShape(shape2); // [s1, s2]
        drawing.addShape(shape3); // [s1, s2, s3]
        
        drawing.bringToFront(shape1); // [s2, s3, s1]
        List<Shape> shapes = drawing.getShapesInZOrder();
        assertEquals(shape1, shapes.get(2));
        assertEquals(shape2, shapes.get(0));
        assertEquals(shape3, shapes.get(1));
        verify(mockObserver, times(4)).update(eq(drawing), any(Drawing.DrawingChangeEvent.class));
    }

    @Test
    void sendToBack() {
        drawing.addShape(shape1); // [s1]
        drawing.addShape(shape2); // [s1, s2]
        drawing.addShape(shape3); // [s1, s2, s3]

        drawing.sendToBack(shape3); // [s3, s1, s2]
        List<Shape> shapes = drawing.getShapesInZOrder();
        assertEquals(shape3, shapes.get(0));
        assertEquals(shape1, shapes.get(1));
        assertEquals(shape2, shapes.get(2));
        verify(mockObserver, times(4)).update(eq(drawing), any(Drawing.DrawingChangeEvent.class));
    }

    @Test
    void observerAttachDetach() {
        drawing.addShape(shape1); // Notifies mockObserver (attached in setUp)
        verify(mockObserver, times(1)).update(eq(drawing), any(Drawing.DrawingChangeEvent.class));

        drawing.detach(mockObserver);
        drawing.addShape(shape2); // Should not notify mockObserver
        verify(mockObserver, times(1)).update(eq(drawing), any(Drawing.DrawingChangeEvent.class)); // Still 1 call
    }
    
    @Test
    void toStringTest() {
        String str = drawing.toString();
        assertTrue(str.contains("shapesCount=0"));
        assertTrue(str.contains("observersCount=1"));
        drawing.addShape(shape1);
        str = drawing.toString();
        assertTrue(str.contains("shapesCount=1"));
    }
}
