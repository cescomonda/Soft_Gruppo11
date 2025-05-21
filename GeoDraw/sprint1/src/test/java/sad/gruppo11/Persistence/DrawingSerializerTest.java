package sad.gruppo11.Persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir; // Per directory temporanee
import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.RectangleShape;
import sad.gruppo11.Model.LineSegment;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Rect;
import sad.gruppo11.Model.geometry.ColorData;


import java.io.File;
import java.io.IOException;
import java.nio.file.Path; // Per @TempDir
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

class DrawingSerializerTest {

    private DrawingSerializer serializer;
    private Drawing testDrawing;

    @TempDir
    Path tempDir; // JUnit 5 inietterà una directory temporanea qui

    @BeforeEach
    void setUp() {
        serializer = new DrawingSerializer();
        testDrawing = new Drawing();

        // Popola il testDrawing con alcune forme
        RectangleShape rect = new RectangleShape(new Rect(10, 10, 50, 30));
        rect.setFillColor(ColorData.RED);
        rect.setStrokeColor(ColorData.BLUE);

        LineSegment line = new LineSegment(new Point2D(0, 0), new Point2D(100, 100));
        line.setStrokeColor(ColorData.GREEN);

        testDrawing.addShape(rect);
        testDrawing.addShape(line);
    }

    @Test
    @DisplayName("save dovrebbe lanciare eccezione per argomenti nulli o path vuoto")
    void testSaveWithInvalidArguments() {
        File testFile = tempDir.resolve("test.ser").toFile();

        assertThatThrownBy(() -> serializer.save(null, testFile.getAbsolutePath()))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Drawing to save cannot be null");

        assertThatThrownBy(() -> serializer.save(testDrawing, null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("File path cannot be null");

        assertThatThrownBy(() -> serializer.save(testDrawing, ""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("File path cannot be empty");
    }

    @Test
    @DisplayName("load dovrebbe lanciare eccezione per path nullo, vuoto o file non esistente")
    void testLoadWithInvalidPath() {
        assertThatThrownBy(() -> serializer.load(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("File path for loading cannot be null");

        assertThatThrownBy(() -> serializer.load(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("File path for loading cannot be empty");

        // Path di un file non esistente
        String nonExistentFilePath = tempDir.resolve("nonexistent.ser").toString();
        assertThatThrownBy(() -> serializer.load(nonExistentFilePath))
            .isInstanceOf(IOException.class); // O FileNotFoundException, che è una IOException
    }

    @Test
    @DisplayName("Un Drawing salvato e poi caricato dovrebbe essere identico (stesse forme e proprietà)")
    void testSaveAndLoadDrawing() throws IOException, ClassNotFoundException {
        File testFile = tempDir.resolve("drawing.ser").toFile();
        String filePath = testFile.getAbsolutePath();

        // 1. Salva il disegno
        serializer.save(testDrawing, filePath);
        assertThat(testFile).exists(); // Verifica che il file sia stato creato

        // 2. Carica il disegno
        Drawing loadedDrawing = serializer.load(filePath);

        // 3. Verifiche sul disegno caricato
        assertThat(loadedDrawing).isNotNull();
        assertThat(loadedDrawing.getShapes()).hasSameSizeAs(testDrawing.getShapes());

        // Per un confronto più profondo, dovremmo iterare sulle forme e confrontarle.
        // Questo richiede che Shape e le sue sottoclassi (e ColorData, Rect, Point2D)
        // abbiano un metodo equals() ben definito che non si basi solo sull'ID se vogliamo
        // confrontare il contenuto dopo la deserializzazione (gli ID saranno gli stessi
        // perché fanno parte dell'oggetto serializzato).
        // Il tuo equals in Shape si basa sull'ID, il che è ok per questo test.

        List<Shape> originalShapes = testDrawing.getShapes();
        List<Shape> loadedShapes = loadedDrawing.getShapes();

        for (int i = 0; i < originalShapes.size(); i++) {
            Shape originalShape = originalShapes.get(i);
            Shape loadedShape = loadedShapes.stream()
                                .filter(s -> s.getId().equals(originalShape.getId()))
                                .findFirst()
                                .orElse(null);
            
            assertThat(loadedShape).as("Shape con ID %s dovrebbe esistere nel disegno caricato", originalShape.getId()).isNotNull();
            
            // Confronta tipo
            assertThat(loadedShape.getClass()).isEqualTo(originalShape.getClass());

            // Confronta bounds
            assertThat(loadedShape.getBounds()).isEqualTo(originalShape.getBounds());

            // Confronta colori
            assertThat(loadedShape.getStrokeColor()).isEqualTo(originalShape.getStrokeColor());
            if (originalShape.getFillColor() != null) { // Le linee hanno fillColor null
                assertThat(loadedShape.getFillColor()).isEqualTo(originalShape.getFillColor());
            } else {
                assertThat(loadedShape.getFillColor()).isNull();
            }

            // Potresti aggiungere confronti più specifici per tipo di forma se necessario
            if (originalShape instanceof LineSegment) {
                LineSegment originalLine = (LineSegment) originalShape;
                LineSegment loadedLine = (LineSegment) loadedShape;
                assertThat(loadedLine.getStartPoint()).isEqualTo(originalLine.getStartPoint());
                assertThat(loadedLine.getEndPoint()).isEqualTo(originalLine.getEndPoint());
            }
        }
        // Verifica che il campo transient 'observers' sia stato reinizializzato e sia vuoto
        // Questo è un po' un test di implementazione, ma importante per la logica di readObject.
        // Non possiamo accedere direttamente alla lista observers, ma possiamo aggiungere un observer
        // e vedere se viene notificato, o semplicemente fidarci che readObject funzioni.
        // Per ora, ci fidiamo che readObject in Drawing faccia il suo dovere.
        // Se volessimo testarlo, potremmo fare:
        // Observer mockObserver = Mockito.mock(Observer.class);
        // loadedDrawing.addObserver(mockObserver);
        // loadedDrawing.addShape(new RectangleShape(new Rect(0,0,1,1))); // Modifica per notificare
        // verify(mockObserver).update(loadedDrawing);
    }

    /* Se non funziona e ritorna un problema con le cartelle temporanee è normale.  */
    @Test
    @DisplayName("load dovrebbe lanciare IOException per un file con formato non valido")
    void testLoadInvalidFileFormat(@TempDir Path tempDir) throws IOException {
        Path corruptedFilePathObj = tempDir.resolve("corrupted.ser"); // Usa Path
        String corruptedFileAbsolutePath = corruptedFilePathObj.toAbsolutePath().toString();

        // Scrivi qualcosa di non serializzabile o un oggetto diverso da Drawing
        try (java.io.FileWriter writer = new java.io.FileWriter(corruptedFileAbsolutePath)) {
            writer.write("Questo non è un Drawing serializzato.");
        }

        DrawingSerializer localSerializer = new DrawingSerializer(); // Usa un serializer locale se i test sono paralleli

        assertThatThrownBy(() -> localSerializer.load(corruptedFileAbsolutePath))
            .isInstanceOf(IOException.class); // Aspettati IOException (StreamCorruptedException, etc.)
    }
}