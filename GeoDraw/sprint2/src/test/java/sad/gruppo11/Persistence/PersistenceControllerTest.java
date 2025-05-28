
package sad.gruppo11.Persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sad.gruppo11.Model.Drawing;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;


public class PersistenceControllerTest {
    @Mock private IDrawingSerializer mockSerializer;
    @Mock private Drawing mockDrawing;
    private PersistenceController persistenceController;
    private final String testPath = "test.ser";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        persistenceController = new PersistenceController(mockSerializer);
    }

    @Test
    void saveDrawingDelegatesToSerializer() throws Exception {
        persistenceController.saveDrawing(mockDrawing, testPath);
        verify(mockSerializer).save(mockDrawing, testPath);
    }

    @Test
    void loadDrawingDelegatesToSerializer() throws Exception {
        when(mockSerializer.load(testPath)).thenReturn(mockDrawing);
        Drawing loaded = persistenceController.loadDrawing(testPath);
        verify(mockSerializer).load(testPath);
        assertSame(mockDrawing, loaded);
    }

    @Test
    void saveDrawingNullDrawingThrowsException() {
        assertThrows(NullPointerException.class, () -> persistenceController.saveDrawing(null, testPath));
    }

    @Test
    void saveDrawingNullPathThrowsException() {
        assertThrows(NullPointerException.class, () -> persistenceController.saveDrawing(mockDrawing, null));
    }
    
    @Test
    void saveDrawingEmptyPathThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> persistenceController.saveDrawing(mockDrawing, ""));
    }

    @Test
    void loadDrawingNullPathThrowsException() {
        assertThrows(NullPointerException.class, () -> persistenceController.loadDrawing(null));
    }
    
    @Test
    void loadDrawingEmptyPathThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> persistenceController.loadDrawing(""));
    }
    
    @Test
    void constructorNullSerializerThrowsException() {
        assertThrows(NullPointerException.class, () -> new PersistenceController(null));
    }
}
