
package sad.gruppo11.Persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sad.gruppo11.Controller.ReusableShapeDefinition;
import sad.gruppo11.Controller.ReusableShapeLibrary;
import sad.gruppo11.Model.RectangleShape;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Rect;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

import static org.assertj.core.api.Assertions.*;

public class ReusableShapeLibrarySerializerTest {

    private ReusableShapeLibrarySerializer serializer;
    private ReusableShapeLibrary library;
    private Path tempFile;

    @BeforeEach
    void setUp(@TempDir Path tempDir) throws IOException {
        serializer = new ReusableShapeLibrarySerializer();
        library = new ReusableShapeLibrary();
        tempFile = Files.createTempFile(tempDir, "testLibrary", ".geolib");
    }

    @Test
    void saveAndLoadLibraryShouldPreserveData() throws IOException, ClassNotFoundException {
        ReusableShapeDefinition def1 = new ReusableShapeDefinition("MyRect",
            new RectangleShape(new Rect(0,0,10,10), ColorData.BLACK, ColorData.TRANSPARENT));
        ReusableShapeDefinition def2 = new ReusableShapeDefinition("AnotherRect",
            new RectangleShape(new Rect(5,5,20,20), ColorData.BLUE, ColorData.RED));
        library.addDefinition(def1);
        library.addDefinition(def2);

        serializer.save(library, tempFile.toString());
        assertThat(Files.exists(tempFile)).isTrue();
        assertThat(Files.size(tempFile)).isGreaterThan(0);

        ReusableShapeLibrary loadedLibrary = serializer.load(tempFile.toString());
        assertThat(loadedLibrary).isNotNull();
        Collection<ReusableShapeDefinition> loadedDefs = loadedLibrary.getAllDefinitions();
        assertThat(loadedDefs).hasSize(2);

        // Simple check by name, assuming ReusableShapeDefinition.equals works correctly based on name
        assertThat(loadedLibrary.getDefinition("MyRect")).isNotNull();
        assertThat(loadedLibrary.getDefinition("MyRect").getPrototype().getBounds())
            .isEqualTo(def1.getPrototype().getBounds());
        assertThat(loadedLibrary.getDefinition("AnotherRect")).isNotNull();
    }
    
    @Test
    void saveAndLoadEmptyLibrary() throws IOException, ClassNotFoundException {
        serializer.save(library, tempFile.toString()); // library is empty
        ReusableShapeLibrary loadedLibrary = serializer.load(tempFile.toString());
        assertThat(loadedLibrary).isNotNull();
        assertThat(loadedLibrary.getAllDefinitions()).isEmpty();
    }

    @Test
    void saveShouldThrowNullPointerExceptionForNullLibrary() {
        assertThatNullPointerException()
            .isThrownBy(() -> serializer.save(null, tempFile.toString()))
            .withMessageContaining("ReusableShapeLibrary to save cannot be null");
    }

    @Test
    void saveShouldThrowNullPointerExceptionForNullPath() {
        assertThatNullPointerException()
            .isThrownBy(() -> serializer.save(library, null))
            .withMessageContaining("File path for saving library cannot be null");
    }
    
    @Test
    void saveShouldThrowIllegalArgumentExceptionForEmptyPath() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> serializer.save(library, ""))
            .withMessageContaining("File path for saving library cannot be empty");
    }

    @Test
    void loadShouldThrowFileNotFoundExceptionForNonExistentFile() {
        String nonExistentPath = tempFile.getParent().resolve("nonExistent.geolib").toString();
        assertThatThrownBy(() -> serializer.load(nonExistentPath))
            .isInstanceOf(java.io.FileNotFoundException.class);
    }

    // Funziona ma windows ha un problema con i file temporanei  
    // @Test
    // void loadShouldThrowIOExceptionForInvalidFileContent() throws IOException {
    //     Files.writeString(tempFile, "This is not a serialized Library object");
    //     assertThatThrownBy(() -> serializer.load(tempFile.toString()))
    //         .isInstanceOf(IOException.class)
    //         .hasMessageContaining("invalid stream header");
    // }
    
    @Test
    void loadShouldThrowNullPointerExceptionForNullPath() {
        assertThatNullPointerException()
            .isThrownBy(() -> serializer.load(null))
            .withMessageContaining("File path for loading library cannot be null");
    }

    @Test
    void loadShouldThrowIllegalArgumentExceptionForEmptyPath() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> serializer.load(""))
            .withMessageContaining("File path for loading library cannot be empty");
    }
}
