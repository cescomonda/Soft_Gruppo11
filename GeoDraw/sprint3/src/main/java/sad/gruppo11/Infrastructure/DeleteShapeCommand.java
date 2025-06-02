package sad.gruppo11.Infrastructure;

import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.Shape;
import java.util.Objects;

public class DeleteShapeCommand extends AbstractDrawingCommand {
    private final Shape shapeToRemove;
    private int originalIndex = -1;

    public DeleteShapeCommand(Drawing drawing, Shape shape) {
        super(drawing);
        Objects.requireNonNull(shape, "Shape to remove cannot be null for DeleteShapeCommand.");
        this.shapeToRemove = shape;
    }

    @Override
    public void execute() {
        this.originalIndex = receiverDrawing.getShapeIndex(shapeToRemove);
        receiverDrawing.removeShape(shapeToRemove);
    }

    @Override
    public void undo() {
        if (this.originalIndex != -1 && this.originalIndex <= receiverDrawing.getModifiableShapesList().size()) {
            receiverDrawing.addShapeAtIndex(shapeToRemove, this.originalIndex);
        } else {
            receiverDrawing.addShape(shapeToRemove);
        }
    }

    @Override
    public String toString() {
        return "DeleteShapeCommand{shapeId=" + (shapeToRemove != null ? shapeToRemove.getId().toString() : "null") + "}";
    }
}