
package sad.gruppo11.Persistence;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.RectangleShape;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Rect;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class DrawingSerializerTest {
    private DrawingSerializer serializer;
    private Drawing drawing;
    private File tempFile;

    @BeforeEach
    void setUp() throws IOException {
        serializer = new DrawingSerializer();
        drawing = new Drawing();
        drawing.addShape(new RectangleShape(new Rect(0,0,10,10), ColorData.BLACK, ColorData.TRANSPARENT));
        tempFile = File.createTempFile("test_drawing", ".ser");
    }

    @AfterEach
    void tearDown() {
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
    }

    @Test
    void saveAndLoadDrawing() throws IOException, ClassNotFoundException {
        serializer.save(drawing, tempFile.getAbsolutePath());
        assertTrue(tempFile.exists() && tempFile.length() > 0);

        Drawing loadedDrawing = serializer.load(tempFile.getAbsolutePath());
        assertNotNull(loadedDrawing);
        assertEquals(drawing.getShapesInZOrder().size(), loadedDrawing.getShapesInZOrder().size());
        
        // Simple check, assumes Shape equals is well-defined or compares relevant parts
        // For this test, we'll just check one shape's properties if IDs are different after load due to new UUID generation for deserialized objects
        // However, UUIDs should be preserved by default Java serialization.
        assertEquals(drawing.getShapesInZOrder().get(0).getId(), loadedDrawing.getShapesInZOrder().get(0).getId());
        assertEquals(drawing.getShapesInZOrder().get(0).getBounds(), loadedDrawing.getShapesInZOrder().get(0).getBounds());
    }

    @Test
    void saveNullDrawingThrowsException() {
        assertThrows(NullPointerException.class, () -> serializer.save(null, tempFile.getAbsolutePath()));
    }

    @Test
    void saveToNullPathThrowsException() {
        assertThrows(NullPointerException.class, () -> serializer.save(drawing, null));
    }
    
    @Test
    void saveToEmptyPathThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> serializer.save(drawing, ""));
    }

    @Test
    void loadFromNullPathThrowsException() {
        assertThrows(NullPointerException.class, () -> serializer.load(null));
    }
    
    @Test
    void loadFromEmptyPathThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> serializer.load(""));
    }

    @Test
    void loadNonExistentFileThrowsIOException() {
        File nonExistent = new File("non_existent_drawing.ser");
        assertThrows(IOException.class, () -> serializer.load(nonExistent.getAbsolutePath()));
    }

    @Test
    void loadInvalidFileContentThrowsIOException() throws IOException {
        // Create a file with non-Drawing content
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(new java.io.FileOutputStream(tempFile))) {
            oos.writeObject("This is not a Drawing object");
        }
        assertThrows(IOException.class, () -> serializer.load(tempFile.getAbsolutePath()));
    }
}
