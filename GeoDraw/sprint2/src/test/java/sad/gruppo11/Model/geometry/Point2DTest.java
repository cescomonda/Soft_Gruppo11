
package sad.gruppo11.Model.geometry;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class Point2DTest {

    @Test
    void constructorAndGetters() {
        Point2D p = new Point2D(10.5, 20.3);
        assertEquals(10.5, p.getX(), 0.001);
        assertEquals(20.3, p.getY(), 0.001);
    }

    @Test
    void copyConstructor() {
        Point2D original = new Point2D(1.0, 2.0);
        Point2D copy = new Point2D(original);
        assertEquals(original.getX(), copy.getX());
        assertEquals(original.getY(), copy.getY());
        assertNotSame(original, copy); // Ensure it's a deep copy for coordinates, though Point2D is immutable here
    }

    @Test
    void setters() {
        Point2D p = new Point2D(0, 0);
        p.setX(5.0);
        p.setY(7.0);
        assertEquals(5.0, p.getX());
        assertEquals(7.0, p.getY());
    }

    @Test
    void distance() {
        Point2D p1 = new Point2D(1, 1);
        Point2D p2 = new Point2D(4, 5); // dx=3, dy=4, distance=5
        assertEquals(5.0, p1.distance(p2), 0.001);
        assertEquals(0.0, p1.distance(p1), 0.001);
    }

    @Test
    void translateXY() {
        Point2D p = new Point2D(1, 1);
        p.translate(2, 3);
        assertEquals(3.0, p.getX());
        assertEquals(4.0, p.getY());
    }

    @Test
    void translateVector() {
        Point2D p = new Point2D(1, 1);
        Vector2D v = new Vector2D(-1, 2);
        p.translate(v);
        assertEquals(0.0, p.getX());
        assertEquals(3.0, p.getY());
    }

    @Test
    void equalsAndHashCode() {
        Point2D p1 = new Point2D(1.2, 3.4);
        Point2D p2 = new Point2D(1.2, 3.4);
        Point2D p3 = new Point2D(5.6, 7.8);
        Point2D p4 = new Point2D(1.2, 7.8);

        assertEquals(p1, p2);
        assertNotEquals(p1, p3);
        assertNotEquals(p1, p4);
        assertNotEquals(p1, null);
        assertNotEquals(p1, new Object());

        assertEquals(p1.hashCode(), p2.hashCode());
        assertNotEquals(p1.hashCode(), p3.hashCode()); // Probable, not guaranteed by contract but typical
    }

    @Test
    void testToString() {
        Point2D p = new Point2D(1.0, 2.5);
        String str = p.toString();
        assertTrue(str.contains("x=1.0"));
        assertTrue(str.contains("y=2.5"));
    }
}
