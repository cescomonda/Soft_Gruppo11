
package sad.gruppo11.Infrastructure;

import sad.gruppo11.Model.Shape; // Può essere anche una GroupShape
import java.util.Objects;

public final class Clipboard {
    private static volatile Clipboard instance;
    private Shape content; // Può contenere una Shape o una GroupShape

    private Clipboard() {}

    public static Clipboard getInstance() {
        Clipboard result = instance;
        if (result == null) {
            synchronized (Clipboard.class) {
                result = instance;
                if (result == null) {
                    instance = result = new Clipboard();
                }
            }
        }
        return result;
    }

    /**
     * Imposta il contenuto della clipboard. La forma viene clonata.
     * @param shape La forma da copiare. Non può essere null.
     */
    public void set(Shape shape) {
        Objects.requireNonNull(shape, "Shape to set on clipboard cannot be null.");
        // Quando si copia una forma (o un gruppo) sulla clipboard,
        // vogliamo una copia indipendente con gli stessi ID originali.
        // Il metodo clone() di Shape dovrebbe fare questo.
        this.content = shape.clone(); 
    }

    /**
     * Ottiene una copia del contenuto della clipboard.
     * @return Un clone della forma nella clipboard, o null se vuota.
     *         Per l'operazione di "paste", di solito si usa shape.cloneWithNewId()
     *         sul risultato di questo get() per creare un'istanza con un nuovo ID.
     */
    public Shape get() {
        // Restituisce un ulteriore clone per assicurarsi che la clipboard
        // non venga modificata esternamente e che ogni "get" sia fresco.
        return (this.content != null) ? this.content.clone() : null;
    }

    public boolean isEmpty() {
        return this.content == null;
    }

    public void clear() {
        this.content = null;
    }
}
