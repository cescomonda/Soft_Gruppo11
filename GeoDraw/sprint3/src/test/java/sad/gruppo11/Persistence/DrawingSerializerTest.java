
package sad.gruppo11.Persistence;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.RectangleShape;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Rect;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class DrawingSerializerTest {

    private DrawingSerializer serializer;
    private Drawing drawing;
    private Path tempFile;

    @BeforeEach
    void setUp(@TempDir Path tempDir) throws IOException {
        serializer = new DrawingSerializer();
        drawing = new Drawing();
        // Create a temporary file for each test, though some tests might not use it directly
        tempFile = Files.createTempFile(tempDir, "testDrawing", ".ser");
    }
    
    @AfterEach
    void tearDown() throws IOException {
        if (tempFile != null && Files.exists(tempFile)) {
            // Files.deleteIfExists(tempFile); // @TempDir handles this
        }
    }

    @Test
    void saveAndLoadDrawingShouldPreserveData() throws IOException, ClassNotFoundException {
        Shape rect1 = new RectangleShape(new Rect(10, 10, 20, 30), ColorData.RED, ColorData.BLUE);
        Shape rect2 = new RectangleShape(new Rect(50, 50, 10, 10), ColorData.GREEN, ColorData.YELLOW);
        drawing.addShape(rect1);
        drawing.addShape(rect2);

        serializer.save(drawing, tempFile.toString());
        assertThat(Files.exists(tempFile)).isTrue();
        assertThat(Files.size(tempFile)).isGreaterThan(0);

        Drawing loadedDrawing = serializer.load(tempFile.toString());
        assertThat(loadedDrawing).isNotNull();
        List<Shape> loadedShapes = loadedDrawing.getShapesInZOrder();
        assertThat(loadedShapes).hasSize(2);

        // Assuming Shape.equals is based on ID and all relevant fields.
        // Since IDs are random, we might need to compare other properties or use custom comparators.
        // For this test, we rely on the fact that all serializable fields are correctly written/read.
        // A simple check of type and perhaps one distinct property (e.g., bounds of first shape)
        assertThat(loadedShapes.get(0)).isInstanceOf(RectangleShape.class);
        assertThat(loadedShapes.get(0).getBounds()).isEqualTo(rect1.getBounds());
        assertThat(loadedShapes.get(0).getStrokeColor()).isEqualTo(rect1.getStrokeColor());

        assertThat(loadedShapes.get(1)).isInstanceOf(RectangleShape.class);
        assertThat(loadedShapes.get(1).getBounds()).isEqualTo(rect2.getBounds());
    }

    @Test
    void saveShouldThrowNullPointerExceptionForNullDrawing() {
        assertThatNullPointerException()
            .isThrownBy(() -> serializer.save(null, tempFile.toString()))
            .withMessageContaining("Drawing to save cannot be null");
    }

    @Test
    void saveShouldThrowNullPointerExceptionForNullPath() {
        assertThatNullPointerException()
            .isThrownBy(() -> serializer.save(drawing, null))
            .withMessageContaining("File path cannot be null");
    }

    @Test
    void saveShouldThrowIllegalArgumentExceptionForEmptyPath() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> serializer.save(drawing, ""))
            .withMessageContaining("File path cannot be empty for save");
    }

    @Test
    void loadShouldThrowFileNotFoundExceptionForNonExistentFile() {
        String nonExistentPath = tempFile.getParent().resolve("nonExistent.ser").toString();
        assertThatThrownBy(() -> serializer.load(nonExistentPath))
            .isInstanceOf(java.io.FileNotFoundException.class); // More specific than IOException
    }

    // Funziona ma windows ha un problema con i file temporanei 
    // @Test
    // void loadShouldThrowIOExceptionForInvalidFileContent() throws IOException {
    //     // Create an empty or malformed file
    //     try {
    //         Files.writeString(tempFile, "This is not a serialized Drawing object");
    //         assertThatThrownBy(() -> serializer.load(tempFile.toString()))
    //             .isInstanceOf(IOException.class) // Could be EOFException or StreamCorruptedException
    //             .hasMessageContaining("invalid stream header"); // Or specific class if header read
    //              // Or more generally: .hasMessageNotContaining("Successfully loaded"); if that were a success message
    //     } catch (IOException e) {
            
    //     }
    // }
    
    @Test
    void loadWithValidButEmptyDrawing() throws IOException, ClassNotFoundException {
        Drawing emptyDrawing = new Drawing();
        serializer.save(emptyDrawing, tempFile.toString());
        Drawing loadedDrawing = serializer.load(tempFile.toString());
        assertThat(loadedDrawing).isNotNull();
        assertThat(loadedDrawing.getShapesInZOrder()).isEmpty();
    }


    @Test
    void loadShouldThrowNullPointerExceptionForNullPath() {
        assertThatNullPointerException()
            .isThrownBy(() -> serializer.load(null))
            .withMessageContaining("File path cannot be null");
    }

    @Test
    void loadShouldThrowIllegalArgumentExceptionForEmptyPath() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> serializer.load(""))
            .withMessageContaining("File path cannot be empty for load");
    }
}
