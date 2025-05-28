package sad.gruppo11.Infrastructure;

import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.Shape;
import java.util.Objects;

public class AddShapeCommand extends AbstractDrawingCommand {
    private final Shape shapeToAdd;

    public AddShapeCommand(Drawing drawing, Shape shape) {
        super(drawing);
        Objects.requireNonNull(shape, "Shape to add cannot be null for AddShapeCommand.");
        this.shapeToAdd = shape;
    }

    @Override
    public void execute() {
        // Ensure shape is not re-added if already present due to undo/redo logic
        if (receiverDrawing.getShapeIndex(shapeToAdd) == -1) {
            receiverDrawing.addShape(shapeToAdd);
        } else { // If redoing an add, it might have been removed by undo.
                 // Or if it's somehow still there, ensure it's at the top.
            receiverDrawing.removeShape(shapeToAdd); // Ensure no duplicates before adding
            receiverDrawing.addShape(shapeToAdd);
        }
    }

    @Override
    public void undo() {
        receiverDrawing.removeShape(shapeToAdd);
    }

    @Override
    public String toString() {
        return "AddShapeCommand{shapeId=" + (shapeToAdd != null ? shapeToAdd.getId().toString() : "null") + "}";
    }
}