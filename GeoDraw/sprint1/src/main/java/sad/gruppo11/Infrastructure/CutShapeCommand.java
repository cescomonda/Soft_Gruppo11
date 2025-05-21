package sad.gruppo11.Infrastructure;

import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.Shape;
import java.util.Objects;

/*
 * Comando per tagliare (cut) una Shape dal disegno.
 * Rimuove la forma dal modello e la copia nel clipboard.
 * Supporta undo, che ripristina la forma nel disegno,
 * ma non ripristina il contenuto precedente del clipboard.
 */
public class CutShapeCommand implements Command {

    /* Il disegno da cui la forma viene rimossa */
    private final Drawing drawing;

    /* La forma da tagliare */
    private final Shape shapeToCut;

    /* Clipboard condiviso in cui la forma verrà copiata */
    private final Clipboard clipboard;

    /*
     * Costruttore del comando.
     *
     * @param drawing      Il disegno di origine.
     * @param shapeToCut   La forma da rimuovere.
     * @param clipboard    Il clipboard in cui salvare una copia.
     */
    public CutShapeCommand(Drawing drawing, Shape shapeToCut, Clipboard clipboard) {
        Objects.requireNonNull(drawing, "Drawing cannot be null for CutShapeCommand.");
        Objects.requireNonNull(shapeToCut, "Shape to cut cannot be null.");
        Objects.requireNonNull(clipboard, "Clipboard cannot be null.");

        this.drawing = drawing;
        this.shapeToCut = shapeToCut;
        this.clipboard = clipboard;
    }

    /*
     * Esegue il comando: copia la forma nel clipboard e la rimuove dal disegno.
     */
    @Override
    public void execute() {
        clipboard.set(shapeToCut);          // Salva un clone della forma
        drawing.removeShape(shapeToCut);    // Rimuove l'originale dal disegno
    }

    /*
     * Annulla il comando: ripristina la forma nel disegno.
     * Nota: il contenuto del clipboard non viene modificato.
     */
    @Override
    public void undo() {
        drawing.addShape(shapeToCut);
    }
}
