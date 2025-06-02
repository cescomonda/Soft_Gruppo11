package sad.gruppo11.Infrastructure;

import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.Shape;
import java.util.Objects;

public class BringToFrontCommand extends AbstractDrawingCommand {
    private final Shape shapeToModify;
    private int originalIndex = -1;

    public BringToFrontCommand(Drawing drawing, Shape shape) {
        super(drawing);
        Objects.requireNonNull(shape, "Shape to modify cannot be null.");
        this.shapeToModify = shape;
    }

    @Override
    public void execute() {
        this.originalIndex = receiverDrawing.getShapeIndex(shapeToModify);
        if (this.originalIndex != -1) {
            receiverDrawing.bringToFront(shapeToModify);
        }
    }

    @Override
    public void undo() {
        if (this.originalIndex != -1) {
            if (receiverDrawing.removeShape(shapeToModify)) {
                 if (this.originalIndex <= receiverDrawing.getModifiableShapesList().size()) { // Check against current size
                    receiverDrawing.addShapeAtIndex(shapeToModify, this.originalIndex);
                 } else { // If index is now out of bounds (e.g. list smaller than originalIndex)
                    receiverDrawing.addShape(shapeToModify); // Add to end as fallback
                 }
            }
        }
    }
    @Override
    public String toString() {
        return "BringToFrontCommand{shapeId=" + (shapeToModify != null ? shapeToModify.getId().toString() : "null") + "}";
    }
}