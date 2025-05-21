package sad.gruppo11.Infrastructure;

import java.util.Objects;
import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.Shape;

/*
 * Comando per rimuovere una forma dal modello Drawing.
 * Supporta l'undo per ripristinare la forma precedentemente rimossa.
 */
public class DeleteShapeCommand implements Command {

    /* Riferimento al modello di disegno */
    private final Drawing drawing;

    /* La forma da rimuovere */
    private final Shape shapeToDelete;

    /*
     * Costruttore del comando.
     *
     * @param drawing        Il disegno da cui rimuovere la forma.
     * @param shapeToDelete  La forma da eliminare.
     */
    public DeleteShapeCommand(Drawing drawing, Shape shapeToDelete) {
        Objects.requireNonNull(drawing, "Drawing cannot be null for DeleteShapeCommand.");
        Objects.requireNonNull(shapeToDelete, "Shape to delete cannot be null for DeleteShapeCommand.");

        this.drawing = drawing;
        this.shapeToDelete = shapeToDelete;
    }

    /*
     * Esegue la cancellazione della forma dal disegno.
     */
    @Override
    public void execute() {
        drawing.removeShape(shapeToDelete);
    }

    /*
     * Ripristina la forma nel disegno.
     * Nota: viene aggiunta in fondo alla lista.
     * Se l'ordine Z fosse importante, bisognerebbe memorizzare l'indice originale.
     */
    @Override
    public void undo() {
        drawing.addShape(shapeToDelete);
    }

    /*
     * Restituisce una descrizione testuale del comando.
     */
    @Override
    public String toString() {
        return "DeleteShapeCommand{shapeId=" + (shapeToDelete != null ? shapeToDelete.getId() : "null") + "}";
    }
}
