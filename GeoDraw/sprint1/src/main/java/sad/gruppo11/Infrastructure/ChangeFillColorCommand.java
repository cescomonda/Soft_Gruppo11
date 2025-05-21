package sad.gruppo11.Infrastructure;

import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.geometry.ColorData;
import java.util.Objects;

/*
 * Comando per cambiare il colore di riempimento di una Shape.
 * Implementa l'interfaccia Command e supporta undo/redo.
 */
public class ChangeFillColorCommand implements Command {

    /*
     * La forma su cui applicare il cambiamento.
     */
    private final Shape targetShape;

    /*
     * Il nuovo colore di riempimento da applicare.
     */
    private final ColorData newFillColor;

    /*
     * Il colore di riempimento precedente, usato per l'undo.
     */
    private ColorData oldFillColor;

    private boolean oldColorCaptured = false;

    /*
     * Costruttore del comando.
     *
     * @param shape     La forma da modificare.
     * @param newColor  Il nuovo colore di riempimento; se null, viene trattato come trasparente.
     */
    public ChangeFillColorCommand(Shape targetShape, ColorData newColor) {
        Objects.requireNonNull(targetShape, "Target shape cannot be null for ChangeFillColorCommand.");
        
        this.targetShape = targetShape;
        // If newFillColor is null, use ColorData.TRANSPARENT, else clone
        this.newFillColor = newColor == null ? ColorData.TRANSPARENT : new ColorData(newColor);
    }

    /*
     * Cattura il colore di riempimento attuale della shape, per poterlo ripristinare in undo.
     */
    public void captureOldColor() {
        ColorData current = targetShape.getFillColor();
        if (current == null) {
            oldFillColor = null;
        } else {
            oldFillColor = new ColorData(current);
        }
        oldColorCaptured = true;
    }

    /*
     * Applica il nuovo colore di riempimento, se la shape non è una linea.
     */
    @Override
    public void execute() {
        if (!oldColorCaptured) {
            captureOldColor();
        }
        // Only set fill color if supported (i.e., not a line)
        if (targetShape.getFillColor() != null || supportsFill(targetShape)) {
            targetShape.setFillColor(newFillColor);
        }
    }

    /*
     * Ripristina il colore di riempimento precedente, se disponibile.
     */
    @Override
    public void undo() {
        // Only set fill color if supported (i.e., not a line)
        if (targetShape.getFillColor() != null || supportsFill(targetShape)) {
            targetShape.setFillColor(oldFillColor == null ? null : new ColorData(oldFillColor));
        }
    }

    private boolean supportsFill(Shape shape) {
        // Heuristic: if getFillColor() is not null or the shape is not a line
        // You may want to improve this check based on your actual model
        return !(shape.getClass().getSimpleName().toLowerCase().contains("line"));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ChangeFillColorCommand{");
        sb.append("shapeId=").append(targetShape.getId());
        sb.append(", newColor=").append(newFillColor);
        if (!oldColorCaptured) {
            sb.append(", oldColor=not captured");
        } else if (oldFillColor == null) {
            sb.append(", oldColor=null (captured)");
        } else {
            sb.append(", oldColor=").append(oldFillColor);
        }
        sb.append(", isLine=").append(!supportsFill(targetShape));
        sb.append('}');
        return sb.toString();
    }
}
