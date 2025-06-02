
package sad.gruppo11.Persistence;

import sad.gruppo11.Controller.ReusableShapeLibrary; // Assumendo che sia in Controller
import java.io.IOException;

public interface IReusableShapeLibrarySerializer {
    /**
     * Salva la libreria di forme riutilizzabili in un file.
     * @param library La libreria da salvare.
     * @param path Il percorso del file in cui salvare.
     * @throws IOException Se si verifica un errore di I/O.
     */
    void save(ReusableShapeLibrary library, String path) throws IOException;

    /**
     * Carica una libreria di forme riutilizzabili da un file.
     * @param path Il percorso del file da cui caricare.
     * @return La libreria caricata.
     * @throws IOException Se si verifica un errore di I/O.
     * @throws ClassNotFoundException Se la classe della libreria non viene trovata.
     */
    ReusableShapeLibrary load(String path) throws IOException, ClassNotFoundException;
}
