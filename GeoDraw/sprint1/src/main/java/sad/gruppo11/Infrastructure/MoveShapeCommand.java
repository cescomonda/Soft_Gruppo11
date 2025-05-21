package sad.gruppo11.Infrastructure;

import java.util.Objects;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.geometry.Vector2D;

/*
 * Comando per spostare una Shape di un certo vettore.
 * Implementa l'interfaccia Command per supportare undo/redo.
 */
public class MoveShapeCommand implements Command {

    /* Forma da spostare */
    private final Shape shapeToMove;

    /* Vettore di spostamento */
    private final Vector2D displacement;

    /*
     * Costruttore del comando.
     *
     * @param shape        La forma da spostare.
     * @param displacement Il vettore che definisce lo spostamento.
     */
    public MoveShapeCommand(Shape shape, Vector2D displacement) {
        Objects.requireNonNull(shape, "Shape to move cannot be null.");
        Objects.requireNonNull(displacement, "Displacement vector cannot be null.");

        this.shapeToMove = shape;
        this.displacement = new Vector2D(displacement); // Copia per sicurezza
    }

    /*
     * Esegue lo spostamento applicando il vettore alla forma.
     */
    @Override
    public void execute() {
        shapeToMove.move(displacement);
    }

    /*
     * Annulla lo spostamento applicando il vettore inverso.
     */
    @Override
    public void undo() {
        Vector2D inverseDisplacement = displacement.inverse();
        shapeToMove.move(inverseDisplacement);
    }

    /*
     * Descrizione del comando utile per log o interfaccia utente.
     */
    @Override
    public String toString() {
        return "MoveShapeCommand{shapeId=" + shapeToMove.getId() + ", displacement=" + displacement + "}";
    }
}
