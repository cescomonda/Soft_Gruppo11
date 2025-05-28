
package sad.gruppo11.Model.geometry;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RectTest {

    @Test
    void constructorPointAndDimensions() {
        Point2D topLeft = new Point2D(10, 20);
        Rect r = new Rect(topLeft, 100, 50);
        assertEquals(10, r.getX());
        assertEquals(20, r.getY());
        assertEquals(100, r.getWidth());
        assertEquals(50, r.getHeight());
        assertEquals(topLeft, r.getTopLeft()); // Checks equality, not same instance
    }

    @Test
    void constructorCoordinatesAndDimensions() {
        Rect r = new Rect(10, 20, 100, 50);
        assertEquals(10, r.getX());
        assertEquals(20, r.getY());
        assertEquals(100, r.getWidth());
        assertEquals(50, r.getHeight());
    }
    
    @Test
    void constructorNegativeDimensionsClampedToZero() {
        Rect r = new Rect(10, 20, -5, -10);
        assertEquals(0, r.getWidth());
        assertEquals(0, r.getHeight());
    }

    @Test
    void copyConstructor() {
        Rect original = new Rect(10, 20, 100, 50);
        Rect copy = new Rect(original);
        assertEquals(original.getX(), copy.getX());
        assertEquals(original.getY(), copy.getY());
        assertEquals(original.getWidth(), copy.getWidth());
        assertEquals(original.getHeight(), copy.getHeight());
        assertNotSame(original.getTopLeft(), copy.getTopLeft()); // Ensure deep copy of Point2D
        assertNotSame(original, copy);
    }
    
    @Test
    void setters() {
        Rect r = new Rect(0,0,0,0);
        Point2D newTopLeft = new Point2D(5,10);
        r.setTopLeft(newTopLeft);
        assertEquals(newTopLeft, r.getTopLeft());
        
        r.setTopLeft(15,25);
        assertEquals(15, r.getX());
        assertEquals(25, r.getY());
        
        r.setWidth(200);
        assertEquals(200, r.getWidth());
        r.setHeight(300);
        assertEquals(300, r.getHeight());
        
        r.setWidth(-10); // Should clamp to 0
        assertEquals(0, r.getWidth());
        r.setHeight(-20); // Should clamp to 0
        assertEquals(0, r.getHeight());
    }

    @Test
    void derivedProperties() {
        Rect r = new Rect(10, 20, 100, 50);
        assertEquals(110, r.getRight()); // 10 + 100
        assertEquals(70, r.getBottom());  // 20 + 50
        assertEquals(new Point2D(110, 70), r.getBottomRight());
        assertEquals(new Point2D(60, 45), r.getCenter()); // 10 + 50, 20 + 25
    }

    @Test
    void contains() {
        Rect r = new Rect(10, 20, 100, 50);
        assertTrue(r.contains(new Point2D(10, 20)));    // Top-left corner
        assertTrue(r.contains(new Point2D(110, 70)));   // Bottom-right corner
        assertTrue(r.contains(new Point2D(50, 40)));    // Inside
        assertFalse(r.contains(new Point2D(5, 20)));    // Left, outside
        assertFalse(r.contains(new Point2D(10, 15)));   // Top, outside
        assertFalse(r.contains(new Point2D(115, 70)));  // Right, outside
        assertFalse(r.contains(new Point2D(110, 75)));  // Bottom, outside
    }

    @Test
    void translateXY() {
        Rect r = new Rect(10, 20, 100, 50);
        r.translate(5, -5);
        assertEquals(15, r.getX());
        assertEquals(15, r.getY());
        // Dimensions should remain unchanged
        assertEquals(100, r.getWidth());
        assertEquals(50, r.getHeight());
    }
    
    @Test
    void translateVector() {
        Rect r = new Rect(10, 20, 100, 50);
        r.translate(new Vector2D(5, -5));
        assertEquals(15, r.getX());
        assertEquals(15, r.getY());
        assertEquals(100, r.getWidth());
        assertEquals(50, r.getHeight());
    }

    @Test
    void translatedXY() {
        Rect r1 = new Rect(10, 20, 100, 50);
        Rect r2 = r1.translated(5, -5);
        // r1 should be unchanged
        assertEquals(10, r1.getX());
        assertEquals(20, r1.getY());
        // r2 should be translated
        assertEquals(15, r2.getX());
        assertEquals(15, r2.getY());
        assertEquals(100, r2.getWidth());
        assertEquals(50, r2.getHeight());
    }
    
    @Test
    void translatedVector() {
        Rect r1 = new Rect(10, 20, 100, 50);
        Rect r2 = r1.translated(new Vector2D(5, -5));
        assertEquals(10, r1.getX());
        assertEquals(20, r1.getY());
        assertEquals(15, r2.getX());
        assertEquals(15, r2.getY());
    }

    @Test
    void equalsAndHashCode() {
        Rect r1 = new Rect(10, 20, 100, 50);
        Rect r2 = new Rect(new Point2D(10, 20), 100, 50);
        Rect r3 = new Rect(0, 0, 100, 50);

        assertEquals(r1, r2);
        assertNotEquals(r1, r3);
        assertEquals(r1.hashCode(), r2.hashCode());
    }
    
    @Test
    void testToString() {
        Rect r = new Rect(1.0, 2.5, 10.0, 20.5);
        String str = r.toString();
        assertTrue(str.contains("topLeft=Point2D{x=1.0, y=2.5}"));
        assertTrue(str.contains("width=10.00")); // Format %.2f
        assertTrue(str.contains("height=20.50"));
    }
}
