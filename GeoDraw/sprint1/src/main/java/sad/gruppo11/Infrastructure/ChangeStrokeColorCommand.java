/*
 * Comando per cambiare il colore del bordo (stroke) di una Shape.
 * Implementa l'interfaccia Command e supporta undo/redo.
 */
package sad.gruppo11.Infrastructure;

import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.geometry.ColorData;
import java.util.Objects;

/*
 * Classe che rappresenta il comando per modificare il colore del bordo di una forma.
 */
public class ChangeStrokeColorCommand implements Command {

    /*
     * Forma a cui applicare la modifica.
     */
    private final Shape targetShape;

    /*
     * Nuovo colore da applicare al bordo.
     */
    private final ColorData newStrokeColor;

    /*
     * Colore precedente del bordo, utilizzato per l'operazione di undo.
     */
    private ColorData oldStrokeColor;

    boolean oldColorWasCaptured = false;

    /*
     * Costruttore del comando.
     *
     * @param shape     La forma su cui applicare il cambiamento.
     * @param newColor  Il nuovo colore del bordo.
     */
    public ChangeStrokeColorCommand(Shape shape, ColorData newColor) {
        Objects.requireNonNull(shape, "Target shape cannot be null for ChangeStrokeColorCommand.");
        Objects.requireNonNull(newColor, "New stroke color cannot be null for ChangeStrokeColorCommand.");
        this.targetShape = shape;
        this.newStrokeColor = new ColorData(newColor); // Memorizza una copia del nuovo colore
    }

    /*
     * Metodo per catturare il colore precedente prima di eseguire il comando.
     * Deve essere chiamato prima di execute(), se si vuole assicurare l'undo.
     */
    public void captureOldColor() {
        this.oldStrokeColor = targetShape.getStrokeColor() != null ? new ColorData(targetShape.getStrokeColor()) : null;
        this.oldColorWasCaptured = true;
    }


    /*
     * Applica il nuovo colore del bordo alla forma.
     */
    @Override
    public void execute() {
        if (this.oldStrokeColor == null) {
            captureOldColor(); // Fallback se non è stato catturato prima
        }
        targetShape.setStrokeColor(newStrokeColor);
    }

    /*
     * Ripristina il colore del bordo precedente, se disponibile.
     */
    @Override
    public void undo() {
        if (oldColorWasCaptured) {
            targetShape.setStrokeColor(oldStrokeColor); // Qui oldStrokeColor può essere null
        } else {
            System.err.println("ChangeStrokeColorCommand: Cannot undo, old color state was not properly captured.");
        }
    }

    @Override
    public String toString() {
        String oldColorStr = oldColorWasCaptured ? (oldStrokeColor != null ? oldStrokeColor.toString() : "null (captured)") : "not captured";
        return "ChangeStrokeColorCommand{" +
               "shapeId=" + targetShape.getId() + // Assicurati che getId() sia mockato se lo usi
               ", newColor=" + newStrokeColor +
               ", oldColor=" + oldColorStr +
               '}';
    }
}
