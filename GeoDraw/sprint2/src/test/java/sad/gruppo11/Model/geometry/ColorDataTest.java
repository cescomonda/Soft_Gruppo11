
package sad.gruppo11.Model.geometry;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ColorDataTest {

    @Test
    void constructorAndGettersValidValues() {
        ColorData color = new ColorData(100, 150, 200, 0.5);
        assertEquals(100, color.getR());
        assertEquals(150, color.getG());
        assertEquals(200, color.getB());
        assertEquals(0.5, color.getA(), 0.001);
    }

    @Test
    void constructorClampsValues() {
        ColorData color = new ColorData(-10, 300, 256, -0.5);
        assertEquals(0, color.getR());    // Clamped from -10
        assertEquals(255, color.getG());  // Clamped from 300
        assertEquals(255, color.getB());  // Clamped from 256
        assertEquals(0.0, color.getA(), 0.001); // Clamped from -0.5

        ColorData color2 = new ColorData(0, 0, 0, 1.5);
        assertEquals(1.0, color2.getA(), 0.001); // Clamped from 1.5
    }

    @Test
    void copyConstructor() {
        ColorData original = new ColorData(10, 20, 30, 0.8);
        ColorData copy = new ColorData(original);
        assertEquals(original.getR(), copy.getR());
        assertEquals(original.getG(), copy.getG());
        assertEquals(original.getB(), copy.getB());
        assertEquals(original.getA(), copy.getA(), 0.001);
        assertNotSame(original, copy);
    }
    
    @Test
    void staticColorConstants() {
        assertEquals(new ColorData(0,0,0,1.0), ColorData.BLACK);
        assertEquals(new ColorData(255,255,255,1.0), ColorData.WHITE);
        assertEquals(new ColorData(255,0,0,1.0), ColorData.RED);
        assertEquals(new ColorData(0,255,0,1.0), ColorData.GREEN);
        assertEquals(new ColorData(0,0,255,1.0), ColorData.BLUE);
        assertEquals(new ColorData(255,255,0,1.0), ColorData.YELLOW);
        assertEquals(new ColorData(0,0,0,0.0), ColorData.TRANSPARENT);
    }

    @Test
    void equalsAndHashCode() {
        ColorData c1 = new ColorData(50, 100, 150, 0.7);
        ColorData c2 = new ColorData(50, 100, 150, 0.7);
        ColorData c3 = new ColorData(50, 100, 150, 0.8);
        ColorData c4 = new ColorData(0, 100, 150, 0.7);

        assertEquals(c1, c2);
        assertNotEquals(c1, c3); // Different alpha
        assertNotEquals(c1, c4); // Different red
        assertEquals(c1.hashCode(), c2.hashCode());
    }
    
    @Test
    void testToString() {
        ColorData c = new ColorData(10, 20, 30, 0.75);
        String str = c.toString();
        assertTrue(str.contains("r=10"));
        assertTrue(str.contains("g=20"));
        assertTrue(str.contains("b=30"));
        assertTrue(str.contains("a=0.75")); // Format %.2f
    }
}
