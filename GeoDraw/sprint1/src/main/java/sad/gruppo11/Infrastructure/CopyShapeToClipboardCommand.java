package sad.gruppo11.Infrastructure;

import sad.gruppo11.Model.Shape;
import java.util.Objects;

/**
 * Comando per copiare una Shape nel Clipboard.
 * L'operazione di undo ripristina il contenuto precedente del Clipboard.
 */
public class CopyShapeToClipboardCommand implements Command {

    private final Shape shapeToCopy; // La forma da copiare nel clipboard
    private final Clipboard clipboard;
    private Shape shapePreviouslyInClipboard; // Per memorizzare cosa c'era prima nel clipboard
    private boolean clipboardWasEmptyBefore;  // Flag per gestire il caso di clipboard inizialmente vuoto

    /**
     * Costruttore.
     * @param clipboard Il clipboard da usare.
     * @param shapeToCopy La forma da copiare. Se null, l'execute non farà nulla
     *                    ma l'undo si comporterà come se il clipboard fosse stato svuotato.
     *                    Idealmente, shapeToCopy non dovrebbe essere null se si intende copiare.
     */
    public CopyShapeToClipboardCommand(Clipboard clipboard, Shape shapeToCopy) {
        Objects.requireNonNull(clipboard, "Clipboard cannot be null for CopyShapeToClipboardCommand.");
        // shapeToCopy può essere null, se l'intenzione è di "copiare null" (cioè svuotare il clipboard)
        // ma di solito ci si aspetta una forma.
        this.clipboard = clipboard;
        this.shapeToCopy = shapeToCopy; // Non cloniamo qui, Clipboard.set() dovrebbe clonare.
                                        // O, se vogliamo che il comando abbia il clone, lo facciamo in execute.
    }

    @Override
    public void execute() {
        // 1. Salva lo stato attuale del clipboard per l'undo
        if (clipboard.isEmpty()) {
            this.clipboardWasEmptyBefore = true;
            this.shapePreviouslyInClipboard = null;
        } else {
            this.clipboardWasEmptyBefore = false;
            // clipboard.get() restituisce un clone, quindi lo stato è preservato
            this.shapePreviouslyInClipboard = clipboard.get();
        }

        // 2. Esegui l'azione di copia (imposta la nuova forma nel clipboard)
        // Clipboard.set() internamente clona shapeToCopy
        if (this.shapeToCopy != null) {
            clipboard.set(this.shapeToCopy);
        } else {
            // Se shapeToCopy è null, interpretiamo come "svuota il clipboard"
            // o potremmo lanciare un'eccezione se shapeToCopy non deve essere null.
            // Per coerenza con un'azione di "copia", shapeToCopy non dovrebbe essere null.
            // Se l'intento è svuotare, si dovrebbe usare un ClearClipboardCommand.
            // Per ora, se shapeToCopy è null, il clipboard viene effettivamente svuotato
            // perché this.shapeToCopy è null, e set(null) svuota.
            clipboard.set(null);
        }
    }

    @Override
    public void undo() {
        // Ripristina lo stato precedente del clipboard
        if (this.clipboardWasEmptyBefore) {
            clipboard.clear(); // O clipboard.set(null);
        } else {
            // shapePreviouslyInClipboard è già un clone, quindi possiamo passarlo direttamente a set.
            // Clipboard.set() farà un altro clone.
            clipboard.set(this.shapePreviouslyInClipboard);
        }
    }

    @Override
    public String toString() {
        String shapeIdStr = (shapeToCopy != null && shapeToCopy.getId() != null) ? shapeToCopy.getId().toString() : "null_shape";
        return "CopyShapeToClipboardCommand{shapeId=" + shapeIdStr + "}";
    }
}