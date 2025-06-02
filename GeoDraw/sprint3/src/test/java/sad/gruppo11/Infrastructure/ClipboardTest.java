
package sad.gruppo11.Infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.LineSegment; // Concrete shape for testing
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.ColorData;

import java.util.UUID;

class ClipboardTest {

    private Clipboard clipboard;
    private Shape mockShape;
    private Shape clonedMockShape;

    @BeforeEach
    void setUp() {
        // Reset singleton instance for a clean test environment if possible,
        // or ensure tests account for shared state. For Clipboard, getting a new instance
        // is fine as its state is simple and reset by clear() or set().
        // We get the singleton instance.
        clipboard = Clipboard.getInstance();
        clipboard.clear(); // Ensure clipboard is empty before each test

        // Create a real, simple shape to be cloned, or use a mock that correctly handles clone()
        // Using a real LineSegment instance for simplicity in cloning
        mockShape = new LineSegment(new Point2D(0,0), new Point2D(10,10), ColorData.BLACK);

        // Shape.clone() is expected to return a new instance with the same ID for clipboard operations
        // For the purpose of this test, we'll simulate this behavior for the mock.
        // If mockShape were a mock, it would be:
        // clonedMockShape = mock(Shape.class);
        // when(clonedMockShape.getId()).thenReturn(mockShape.getId()); // Same ID
        // when(mockShape.clone()).thenReturn(clonedMockShape);

        // Since mockShape is a real LineSegment, its clone() method will work as expected.
        clonedMockShape = mockShape.clone(); // This will be the object actually stored
    }

    @Test
    void getInstance_shouldReturnSameInstance() {
        Clipboard instance1 = Clipboard.getInstance();
        Clipboard instance2 = Clipboard.getInstance();
        assertSame(instance1, instance2, "getInstance should return the singleton instance.");
    }

    @Test
    void initialState_shouldBeEmpty() {
        assertTrue(clipboard.isEmpty(), "Clipboard should be empty initially.");
        assertNull(clipboard.get(), "get() on an empty clipboard should return null.");
    }

    @Test
    void set_shouldStoreACloneOfShape() {
        clipboard.set(mockShape);
        assertFalse(clipboard.isEmpty(), "Clipboard should not be empty after set.");

        Shape contentInClipboard = clipboard.get(); // This get() also returns a clone
        assertNotNull(contentInClipboard, "Content from clipboard should not be null.");
        
        // Verify it's a clone, not the original instance
        assertNotSame(mockShape, contentInClipboard, "Clipboard should store a clone, not the original shape.");
        
        // Verify the clone has the same essential properties (e.g., ID)
        assertEquals(mockShape.getId(), contentInClipboard.getId(), "Cloned shape in clipboard should have the same ID as the original.");
    }
    
    @Test
    void set_nullShape_shouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> {
            clipboard.set(null);
        }, "Setting null to clipboard should throw NullPointerException.");
    }

    @Test
    void get_shouldReturnACloneOfStoredShape() {
        clipboard.set(mockShape); // Stores a clone of mockShape

        Shape firstGet = clipboard.get();
        Shape secondGet = clipboard.get();

        assertNotNull(firstGet, "First get should not be null.");
        assertNotNull(secondGet, "Second get should not be null.");

        // Both gets should be clones of the *stored clone* (which is a clone of mockShape)
        // So, firstGet and secondGet should be different instances from each other,
        // and also different from the originally set mockShape.
        assertNotSame(mockShape, firstGet, "First get should be a clone.");
        assertNotSame(mockShape, secondGet, "Second get should be a clone.");
        assertNotSame(firstGet, secondGet, "Successive gets should return different clones.");

        assertEquals(mockShape.getId(), firstGet.getId(), "ID of first get should match original.");
        assertEquals(mockShape.getId(), secondGet.getId(), "ID of second get should match original.");
    }

    @Test
    void isEmpty_shouldReflectState() {
        assertTrue(clipboard.isEmpty(), "Clipboard should be empty initially.");
        clipboard.set(mockShape);
        assertFalse(clipboard.isEmpty(), "Clipboard should not be empty after set.");
        clipboard.clear();
        assertTrue(clipboard.isEmpty(), "Clipboard should be empty after clear.");
    }

    @Test
    void clear_shouldEmptyClipboard() {
        clipboard.set(mockShape);
        assertFalse(clipboard.isEmpty(), "Clipboard should not be empty before clear.");
        clipboard.clear();
        assertTrue(clipboard.isEmpty(), "Clipboard should be empty after clear.");
        assertNull(clipboard.get(), "get() after clear should return null.");
    }

    @Test
    void set_multipleTimes_shouldOverwrite() {
        Shape shape1 = new LineSegment(new Point2D(0,0), new Point2D(1,1), ColorData.RED);
        Shape shape2 = new LineSegment(new Point2D(10,10), new Point2D(20,20), ColorData.BLUE);
        UUID shape1Id = shape1.getId();
        UUID shape2Id = shape2.getId();

        clipboard.set(shape1);
        Shape content1 = clipboard.get();
        assertNotNull(content1);
        assertEquals(shape1Id, content1.getId(), "Clipboard should contain shape1.");

        clipboard.set(shape2);
        Shape content2 = clipboard.get();
        assertNotNull(content2);
        assertEquals(shape2Id, content2.getId(), "Clipboard should now contain shape2 (overwritten).");
        assertNotEquals(shape1Id, content2.getId(), "Shape1 ID should no longer be in clipboard's current content ID.");
    }
}
            