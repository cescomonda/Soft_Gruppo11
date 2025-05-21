package sad.gruppo11.Persistence;

import sad.gruppo11.Model.Drawing;

import java.util.Objects;

/**
 * {@code PersistenceController} gestisce il salvataggio e il caricamento
 * di un oggetto {@link Drawing}, fungendo da facciata per il sottosistema di persistenza.
 */
public class PersistenceController {

    private final DrawingSerializer serializer;

    /**
     * Costruttore che inizializza il serializer.
     */
    public PersistenceController() {
        this.serializer = new DrawingSerializer();
    }

    /**
     * Salva il disegno specificato nel percorso fornito.
     *
     * @param drawing Il disegno da salvare. Non può essere {@code null}.
     * @param path    Il percorso del file. Non può essere {@code null} o vuoto.
     * @throws Exception Se il salvataggio fallisce.
     */
    public void saveDrawing(Drawing drawing, String path) throws Exception {
        Objects.requireNonNull(drawing, "Drawing to save cannot be null.");
        Objects.requireNonNull(path, "File path cannot be null.");
        if (path.isEmpty()) {
            throw new IllegalArgumentException("File path cannot be empty.");
        }

        serializer.save(drawing, path);
    }

    /**
     * Carica un disegno dal percorso specificato.
     *
     * @param path Il percorso del file. Non può essere {@code null} o vuoto.
     * @return Il disegno caricato.
     * @throws RuntimeException Se il caricamento fallisce.
     */
    public Drawing loadDrawing(String path) {
        Objects.requireNonNull(path, "File path cannot be null.");
        if (path.isEmpty()) {
            throw new IllegalArgumentException("File path cannot be empty.");
        }

        try {
            return serializer.load(path);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load drawing from " + path, e);
        }
    }
}
