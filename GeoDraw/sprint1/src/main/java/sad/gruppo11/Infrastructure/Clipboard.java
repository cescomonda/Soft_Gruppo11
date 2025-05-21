package sad.gruppo11.Infrastructure;

import java.util.Objects;
import sad.gruppo11.Model.Shape;

/*
 * Clipboard implementa un semplice meccanismo di appunti per memorizzare
 * una singola Shape.
 * È implementato come Singleton.
 *
 * Quando una forma viene impostata nel clipboard, viene salvata una sua copia (clone).
 * Ogni recupero restituisce un nuovo clone, evitando effetti collaterali tra più copie.
 */
public final class Clipboard {

    /* Istanza unica del singleton (inizializzazione pigra e thread-safe) */
    private static volatile Clipboard instance;

    /* Forma attualmente memorizzata nel clipboard */
    private Shape storedShape;

    /* Costruttore privato: impedisce l'istanziazione diretta */
    private Clipboard() {
        // Nessuna inizializzazione necessaria
    }

    /*
     * Restituisce l'istanza singleton del Clipboard.
     * Usa il double-checked locking per garantire thread-safety ed efficienza.
     *
     * @return L'istanza globale del Clipboard.
     */
    public static Clipboard getInstance() {
        if (instance == null) {
            synchronized (Clipboard.class) {
                if (instance == null) {
                    instance = new Clipboard();
                }
            }
        }
        return instance;
    }

    /*
     * Imposta (copia) una forma nel clipboard.
     * Se la forma è null, il clipboard viene svuotato.
     * Altrimenti viene salvata una copia (clone) della forma.
     *
     * @param shape La forma da copiare nel clipboard.
     */
    public void set(Shape shape) {
        if (shape != null) {
            this.storedShape = shape.cloneShape(); // Salva sempre un clone
        } else {
            this.storedShape = null;
        }
    }

    /*
     * Restituisce una copia (clone) della forma attualmente nel clipboard.
     * Se il clipboard è vuoto, restituisce null.
     *
     * @return Una nuova Shape clonata oppure null se vuoto.
     */
    public Shape get() {
        if (this.storedShape != null) {
            return this.storedShape.cloneShape(); // Ritorna un nuovo clone ogni volta
        }
        return null;
    }

    /*
     * Verifica se il clipboard è vuoto.
     *
     * @return true se non contiene forme, false altrimenti.
     */
    public boolean isEmpty() {
        return this.storedShape == null;
    }

    /*
     * Svuota il clipboard, rimuovendo ogni forma memorizzata.
     */
    public void clear() {
        set(null);
    }
}
