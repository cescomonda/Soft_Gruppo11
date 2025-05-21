package sad.gruppo11.Infrastructure;

import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.geometry.Vector2D;
import java.util.Objects;

/*
 * Comando per incollare una forma dal clipboard al disegno.
 * Copia la forma con un nuovo ID, applica un offset per evitarne la sovrapposizione
 * e la aggiunge al modello. Supporta undo per rimuovere la forma incollata.
 */
public class PasteShapeCommand implements Command {

    /* Modello di disegno in cui incollare la forma */
    private final Drawing drawing;

    /* Clipboard da cui copiare la forma */
    private final Clipboard clipboard;

    /* Riferimento alla forma effettivamente incollata */
    private Shape pastedShape;

    /* Offset da applicare alla posizione della forma incollata */
    private final Vector2D offset = new Vector2D(10, 10);

    /*
     * Costruttore del comando.
     *
     * @param drawing    Il disegno di destinazione.
     * @param clipboard  Il clipboard da cui prelevare la forma.
     */
    public PasteShapeCommand(Drawing drawing, Clipboard clipboard) {
        Objects.requireNonNull(drawing, "Drawing cannot be null for PasteShapeCommand.");
        Objects.requireNonNull(clipboard, "Clipboard cannot be null for PasteShapeCommand.");

        this.drawing = drawing;
        this.clipboard = clipboard;
    }

    /*
     * Esegue l'incolla: clona la forma dal clipboard, assegna un nuovo ID,
     * applica un offset e la aggiunge al disegno.
     */
    @Override
    public void execute() {
        Shape shapeFromClipboard = clipboard.get();

        if (shapeFromClipboard != null) {
            this.pastedShape = shapeFromClipboard.cloneWithNewId();
            this.pastedShape.move(offset);
            drawing.addShape(this.pastedShape);
        } else {
            this.pastedShape = null;
        }
    }

    /*
     * Annulla l'incolla rimuovendo la forma aggiunta al disegno.
     */
    @Override
    public void undo() {
        if (this.pastedShape != null) {
            drawing.removeShape(this.pastedShape);
            this.pastedShape = null; // Resetta per evitare undo ripetuti
        }
    }
}
