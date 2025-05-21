package sad.gruppo11.Infrastructure;

import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.geometry.Rect;
import java.util.Objects;

/*
 * Comando per ridimensionare una Shape.
 * Supporta undo tramite memorizzazione dei limiti originali (Rect).
 */
public class ResizeShapeCommand implements Command {

    /* La forma da ridimensionare */
    private final Shape targetShape;

    /* I nuovi limiti da applicare alla forma */
    private final Rect newBounds;

    /* I limiti originali, salvati per permettere l'undo */
    private Rect oldBounds;

    /*
     * Costruttore del comando.
     *
     * @param shape      La forma da ridimensionare.
     * @param newBounds  I nuovi limiti da assegnare alla forma.
     */
    public ResizeShapeCommand(Shape shape, Rect newBounds) {
        Objects.requireNonNull(shape, "Target shape cannot be null for ResizeShapeCommand.");
        Objects.requireNonNull(newBounds, "New bounds cannot be null for ResizeShapeCommand.");
        this.targetShape = shape;
        this.newBounds = new Rect(newBounds); // Copia difensiva
    }

    /**
     * Cattura i limiti attuali della forma, per poterli ripristinare in undo.
     * Se la forma non ha bounds definiti (getBounds() restituisce null),
     * oldBounds sarà impostato/rimarrà null.
     */
    public void captureOldBounds() {
        if (this.targetShape != null) {
            Rect currentShapeBounds = this.targetShape.getBounds(); // Chiamata una sola volta
            if (currentShapeBounds != null) {
                this.oldBounds = new Rect(currentShapeBounds); // Copia difensiva
            } else {
                // La forma non ha bounds attualmente, o getBounds() ha restituito null.
                // oldBounds sarà/rimarrà null, indicando che non c'era uno stato precedente da salvare.
                this.oldBounds = null;
            }
        }
        // Se targetShape è null (non dovrebbe accadere dopo il costruttore), oldBounds non viene modificato.
    }

    /*
     * Esegue il ridimensionamento della forma.
     * Se oldBounds non è stato catturato prima, viene salvato ora.
     */
    @Override
    public void execute() {
        if (this.oldBounds == null) {
            captureOldBounds();
        }
        targetShape.resize(newBounds);
    }

    /*
     * Ripristina i limiti originali della forma.
     */
    @Override
    public void undo() {
        if (this.oldBounds != null) {
            targetShape.resize(oldBounds);
        }
    }
}
