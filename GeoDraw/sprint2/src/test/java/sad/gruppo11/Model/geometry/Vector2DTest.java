
package sad.gruppo11.Model.geometry;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class Vector2DTest {

    @Test
    void constructorAndGetters() {
        Vector2D v = new Vector2D(3.0, -4.0);
        assertEquals(3.0, v.getDx());
        assertEquals(-4.0, v.getDy());
    }

    @Test
    void copyConstructor() {
        Vector2D original = new Vector2D(1.0, 2.0);
        Vector2D copy = new Vector2D(original);
        assertEquals(original.getDx(), copy.getDx());
        assertEquals(original.getDy(), copy.getDy());
        assertNotSame(original, copy);
    }
    
    @Test
    void setters() {
        Vector2D v = new Vector2D(0,0);
        v.setDx(1.2);
        v.setDy(3.4);
        assertEquals(1.2, v.getDx());
        assertEquals(3.4, v.getDy());
    }

    @Test
    void length() {
        Vector2D v = new Vector2D(3.0, 4.0);
        assertEquals(5.0, v.length(), 0.001);
        Vector2D zero = new Vector2D(0,0);
        assertEquals(0.0, zero.length(), 0.001);
    }

    @Test
    void normalize() {
        Vector2D v = new Vector2D(3.0, 4.0);
        v.normalize();
        assertEquals(0.6, v.getDx(), 0.001); // 3/5
        assertEquals(0.8, v.getDy(), 0.001); // 4/5
        assertEquals(1.0, v.length(), 0.001);

        Vector2D zero = new Vector2D(0,0);
        zero.normalize(); // Should not throw error, remains (0,0)
        assertEquals(0.0, zero.getDx());
        assertEquals(0.0, zero.getDy());
    }
    
    @Test
    void normalized() {
        Vector2D v = new Vector2D(3.0, 4.0);
        Vector2D vn = v.normalized();
        assertEquals(0.6, vn.getDx(), 0.001);
        assertEquals(0.8, vn.getDy(), 0.001);
        assertEquals(1.0, vn.length(), 0.001);
        // Original vector should be unchanged
        assertEquals(3.0, v.getDx()); 
        assertEquals(4.0, v.getDy());


        Vector2D zero = new Vector2D(0,0);
        Vector2D zeroN = zero.normalized();
        assertEquals(0.0, zeroN.getDx());
        assertEquals(0.0, zeroN.getDy());
    }

    @Test
    void inverse() {
        Vector2D v = new Vector2D(2.5, -1.5);
        Vector2D inv = v.inverse();
        assertEquals(-2.5, inv.getDx());
        assertEquals(1.5, inv.getDy());
    }

    @Test
    void equalsAndHashCode() {
        Vector2D v1 = new Vector2D(1.0, 2.0);
        Vector2D v2 = new Vector2D(1.0, 2.0);
        Vector2D v3 = new Vector2D(3.0, 4.0);

        assertEquals(v1, v2);
        assertNotEquals(v1, v3);
        assertEquals(v1.hashCode(), v2.hashCode());
    }

    @Test
    void testToString() {
        Vector2D v = new Vector2D(1.2, 3.4);
        assertTrue(v.toString().contains("dx=1.2"));
        assertTrue(v.toString().contains("dy=3.4"));
    }
}
