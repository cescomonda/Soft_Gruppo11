package sad.gruppo11.Persistence;

import sad.gruppo11.Model.Drawing;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Objects;

/**
 * La classe {@code DrawingSerializer} si occupa della serializzazione e deserializzazione
 * dell'oggetto {@link Drawing}, utilizzando la serializzazione Java standard.
 */
public class DrawingSerializer {

    /**
     * Salva l'oggetto {@link Drawing} nel file specificato.
     *
     * @param drawing L'istanza di {@code Drawing} da salvare. Non deve essere {@code null}.
     * @param path    Il percorso del file dove salvare il disegno. Non deve essere {@code null} o vuoto.
     * @throws IOException Se si verifica un errore durante la scrittura su file.
     */
    public void save(Drawing drawing, String path) throws IOException {
        Objects.requireNonNull(drawing, "Drawing to save cannot be null.");
        Objects.requireNonNull(path, "File path cannot be null.");
        if (path.isEmpty()) throw new IllegalArgumentException("File path cannot be empty.");

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
            oos.writeObject(drawing);
        }
    }

    /**
     * Carica un oggetto {@link Drawing} da un file precedentemente serializzato.
     *
     * @param path Il percorso del file da cui caricare il disegno. Non deve essere {@code null} o vuoto.
     * @return Il disegno deserializzato.
     * @throws IOException            Se si verifica un errore durante la lettura dal file.
     * @throws ClassNotFoundException Se una classe necessaria per la deserializzazione non è disponibile.
     */
    public Drawing load(String path) throws IOException, ClassNotFoundException {
        Objects.requireNonNull(path, "File path for loading cannot be null.");
        if (path.isEmpty()) throw new IllegalArgumentException("File path for loading cannot be empty.");

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))) {
            Object obj = ois.readObject();
            if (obj instanceof Drawing) {
                return (Drawing) obj;
            } else {
                throw new IOException("Invalid file format: expected Drawing, found " +
                        (obj != null ? obj.getClass().getName() : "null"));
            }
        }
    }
}
