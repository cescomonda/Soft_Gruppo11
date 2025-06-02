
package sad.gruppo11.Persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sad.gruppo11.Controller.ReusableShapeLibrary;
import sad.gruppo11.Model.Drawing;

import java.io.IOException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PersistenceControllerTest {

    private IDrawingSerializer mockDrawingSerializer;
    private IReusableShapeLibrarySerializer mockLibrarySerializer;
    private PersistenceController controller;
    private Drawing dummyDrawing;
    private ReusableShapeLibrary dummyLibrary;
    private String validPath = "test.ser";
    private String validLibPath = "test.geolib";


    @BeforeEach
    void setUp() {
        mockDrawingSerializer = Mockito.mock(IDrawingSerializer.class);
        mockLibrarySerializer = Mockito.mock(IReusableShapeLibrarySerializer.class);
        controller = new PersistenceController(mockDrawingSerializer, mockLibrarySerializer);
        dummyDrawing = new Drawing();
        dummyLibrary = new ReusableShapeLibrary();
    }

    @Test
    void constructorShouldThrowForNullSerializers() {
        assertThatNullPointerException().isThrownBy(() -> new PersistenceController(null, mockLibrarySerializer))
            .withMessageContaining("DrawingSerializer cannot be null");
        assertThatNullPointerException().isThrownBy(() -> new PersistenceController(mockDrawingSerializer, null))
            .withMessageContaining("ReusableShapeLibrarySerializer cannot be null");
    }
    
    @Test
    void constructorWithOnlyDrawingSerializerShouldUseDefaultLibrarySerializer() {
        // This test relies on the warning print, which is not ideal.
        // Better to verify interaction if possible, or just ensure no NPE.
        assertThatCode(() -> new PersistenceController(mockDrawingSerializer))
            .doesNotThrowAnyException();
    }

    // --- Drawing Persistence Tests ---
    @Test
    void saveDrawingShouldCallSerializer() throws IOException {
        controller.saveDrawing(dummyDrawing, validPath);
        verify(mockDrawingSerializer).save(dummyDrawing, validPath);
    }

    @Test
    void saveDrawingShouldThrowForNullDrawing() {
        assertThatNullPointerException().isThrownBy(() -> controller.saveDrawing(null, validPath))
            .withMessageContaining("Drawing to save cannot be null");
    }

    @Test
    void saveDrawingShouldThrowForNullPath() {
        assertThatNullPointerException().isThrownBy(() -> controller.saveDrawing(dummyDrawing, null))
            .withMessageContaining("File path cannot be null");
    }
    
    @Test
    void saveDrawingShouldThrowForEmptyPath() {
        assertThatIllegalArgumentException().isThrownBy(() -> controller.saveDrawing(dummyDrawing, ""))
            .withMessageContaining("File path cannot be empty");
    }

    @Test
    void loadDrawingShouldCallSerializerAndReturnDrawing() throws IOException, ClassNotFoundException {
        when(mockDrawingSerializer.load(validPath)).thenReturn(dummyDrawing);
        Drawing loaded = controller.loadDrawing(validPath);
        assertThat(loaded).isSameAs(dummyDrawing);
        verify(mockDrawingSerializer).load(validPath);
    }

    @Test
    void loadDrawingShouldThrowForNullPath() {
        assertThatNullPointerException().isThrownBy(() -> controller.loadDrawing(null))
            .withMessageContaining("File path cannot be null");
    }
    
    @Test
    void loadDrawingShouldThrowForEmptyPath() {
        assertThatIllegalArgumentException().isThrownBy(() -> controller.loadDrawing(""))
            .withMessageContaining("File path cannot be empty");
    }

    // --- Reusable Shape Library Persistence Tests ---
    @Test
    void exportReusableLibraryShouldCallSerializer() throws IOException {
        controller.exportReusableLibrary(dummyLibrary, validLibPath);
        verify(mockLibrarySerializer).save(dummyLibrary, validLibPath);
    }

    @Test
    void exportReusableLibraryShouldThrowForNullLibrary() {
        assertThatNullPointerException().isThrownBy(() -> controller.exportReusableLibrary(null, validLibPath))
            .withMessageContaining("Reusable library to export cannot be null");
    }

    @Test
    void exportReusableLibraryShouldThrowForNullPath() {
        assertThatNullPointerException().isThrownBy(() -> controller.exportReusableLibrary(dummyLibrary, null))
            .withMessageContaining("File path for exporting library cannot be null");
    }
    
    @Test
    void exportReusableLibraryShouldThrowForEmptyPath() {
        assertThatIllegalArgumentException().isThrownBy(() -> controller.exportReusableLibrary(dummyLibrary, ""))
            .withMessageContaining("File path for exporting library cannot be empty");
    }

    @Test
    void importReusableLibraryShouldCallSerializerAndReturnLibrary() throws IOException, ClassNotFoundException {
        when(mockLibrarySerializer.load(validLibPath)).thenReturn(dummyLibrary);
        ReusableShapeLibrary loaded = controller.importReusableLibrary(validLibPath);
        assertThat(loaded).isSameAs(dummyLibrary);
        verify(mockLibrarySerializer).load(validLibPath);
    }

    @Test
    void importReusableLibraryShouldThrowForNullPath() {
        assertThatNullPointerException().isThrownBy(() -> controller.importReusableLibrary(null))
            .withMessageContaining("File path for importing library cannot be null");
    }
    
    @Test
    void importReusableLibraryShouldThrowForEmptyPath() {
        assertThatIllegalArgumentException().isThrownBy(() -> controller.importReusableLibrary(""))
            .withMessageContaining("File path for importing library cannot be empty");
    }
}
