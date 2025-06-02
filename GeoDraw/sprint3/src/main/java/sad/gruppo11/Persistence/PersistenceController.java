
package sad.gruppo11.Persistence;

import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Controller.ReusableShapeLibrary; // Assumendo che sia in Controller

import java.io.IOException;
import java.util.Objects;

public class PersistenceController {
    private final IDrawingSerializer drawingSerializer;
    private final IReusableShapeLibrarySerializer librarySerializer; // Aggiunto per Sprint 3

    // Costruttore aggiornato per accettare entrambi i serializer
    public PersistenceController(IDrawingSerializer drawingSerializer, IReusableShapeLibrarySerializer librarySerializer) {
        Objects.requireNonNull(drawingSerializer, "DrawingSerializer cannot be null for PersistenceController.");
        Objects.requireNonNull(librarySerializer, "ReusableShapeLibrarySerializer cannot be null for PersistenceController.");
        this.drawingSerializer = drawingSerializer;
        this.librarySerializer = librarySerializer;
    }
    
    // Costruttore precedente, potrebbe essere deprecato o rimosso se si usa sempre la libreria
    public PersistenceController(IDrawingSerializer drawingSerializer) {
        this(drawingSerializer, new ReusableShapeLibrarySerializer()); // Fornisce un default
        System.out.println("Warning: PersistenceController created without explicit ReusableShapeLibrarySerializer. Using default.");
    }


    public void saveDrawing(Drawing d, String path) throws IOException { // Modificato per lanciare IOException direttamente
        Objects.requireNonNull(d, "Drawing to save cannot be null.");
        Objects.requireNonNull(path, "File path cannot be null for saving drawing.");
        if (path.isEmpty()) throw new IllegalArgumentException("File path cannot be empty for saving drawing.");
        drawingSerializer.save(d, path);
    }

    public Drawing loadDrawing(String path) throws IOException, ClassNotFoundException { // Modificato
        Objects.requireNonNull(path, "File path cannot be null for loading drawing.");
        if (path.isEmpty()) throw new IllegalArgumentException("File path cannot be empty for loading drawing.");
        return drawingSerializer.load(path);
    }

    // --- Nuovi metodi per la persistenza della libreria di forme riutilizzabili (Sprint 3) ---

    /**
     * Esporta la libreria di forme riutilizzabili fornita in un file.
     * @param library La libreria da esportare.
     * @param path Il percorso del file di destinazione.
     * @throws IOException Se si verifica un errore di I/O durante il salvataggio.
     */
    public void exportReusableLibrary(ReusableShapeLibrary library, String path) throws IOException {
        Objects.requireNonNull(library, "Reusable library to export cannot be null.");
        Objects.requireNonNull(path, "File path for exporting library cannot be null.");
        if (path.isEmpty()) {
            throw new IllegalArgumentException("File path for exporting library cannot be empty.");
        }
        librarySerializer.save(library, path);
    }

    /**
     * Importa una libreria di forme riutilizzabili da un file.
     * @param path Il percorso del file da cui importare.
     * @return La ReusableShapeLibrary caricata.
     * @throws IOException Se si verifica un errore di I/O durante il caricamento.
     * @throws ClassNotFoundException Se la classe della libreria non viene trovata.
     */
    public ReusableShapeLibrary importReusableLibrary(String path) throws IOException, ClassNotFoundException {
        Objects.requireNonNull(path, "File path for importing library cannot be null.");
        if (path.isEmpty()) {
            throw new IllegalArgumentException("File path for importing library cannot be empty.");
        }
        return librarySerializer.load(path);
    }
}
