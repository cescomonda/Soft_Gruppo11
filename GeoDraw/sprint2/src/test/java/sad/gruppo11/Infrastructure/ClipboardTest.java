
package sad.gruppo11.Infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.RectangleShape;
import sad.gruppo11.Model.geometry.Rect;
import sad.gruppo11.Model.geometry.ColorData;

import static org.junit.jupiter.api.Assertions.*;

public class ClipboardTest {
    private Clipboard clipboard;
    private Shape testShape;

    @BeforeEach
    void setUp() {
        clipboard = Clipboard.getInstance();
        clipboard.clear(); // Ensure clean state for each test
        testShape = new RectangleShape(new Rect(0,0,10,10), ColorData.BLACK, ColorData.TRANSPARENT);
    }

    @Test
    void getInstanceReturnsSameInstance() {
        Clipboard c1 = Clipboard.getInstance();
        Clipboard c2 = Clipboard.getInstance();
        assertSame(c1, c2);
    }

    @Test
    void initiallyEmpty() {
        assertTrue(clipboard.isEmpty());
        assertNull(clipboard.get());
    }

    @Test
    void setAndGetShape() {
        clipboard.set(testShape);
        assertFalse(clipboard.isEmpty());
        
        Shape retrievedShape = clipboard.get();
        assertNotNull(retrievedShape);
        assertNotSame(testShape, retrievedShape, "Get should return a clone");
        assertEquals(testShape.getId(), retrievedShape.getId(), "Clone from set() should have same ID as original");
        
        Shape retrievedShape2 = clipboard.get();
        assertNotSame(retrievedShape, retrievedShape2, "Multiple gets should return different clones");
        assertEquals(retrievedShape.getId(), retrievedShape2.getId());
    }
    
    @Test
    void setShapeIsCloned() {
        // Modify original shape after setting it to clipboard
        // The clipboard content should remain unchanged.
        Rect initialBounds = testShape.getBounds();
        clipboard.set(testShape);
        
        // Modify the original testShape
        ((RectangleShape)testShape).resize(new Rect(100,100,1,1));
        
        Shape fromClipboard = clipboard.get();
        assertEquals(initialBounds, fromClipboard.getBounds(), "Clipboard content should be a clone of the shape at the moment of set()");
    }


    @Test
    void clearClipboard() {
        clipboard.set(testShape);
        assertFalse(clipboard.isEmpty());
        clipboard.clear();
        assertTrue(clipboard.isEmpty());
        assertNull(clipboard.get());
    }
    
    @Test
    void setNullShapeThrowsException() {
        assertThrows(NullPointerException.class, () -> clipboard.set(null));
    }
}
