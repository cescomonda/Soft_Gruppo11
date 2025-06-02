package sad.gruppo11.Infrastructure;

import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.geometry.Rect;
import java.util.Objects;

public class ResizeShapeCommand extends AbstractShapeCommand {
    private final Rect newBounds;
    private Rect oldBounds;

    public ResizeShapeCommand(Drawing drawing, Shape shape, Rect newBounds) {
        super(drawing, shape);
        Objects.requireNonNull(newBounds, "New bounds cannot be null for ResizeShapeCommand.");
        this.newBounds = new Rect(newBounds);
    }

    @Override
    public void execute() {
        if (this.oldBounds == null) {
            this.oldBounds = new Rect(this.receiverShape.getBounds());
        }
        this.drawing.resizeShape(receiverShape, newBounds);
    }

    @Override
    public void undo() {
        if (this.oldBounds != null) {
            this.drawing.resizeShape(receiverShape, oldBounds);
        }
    }

    @Override
    public String toString() {
        return "ResizeShapeCommand{shapeId=" + receiverShape.getId().toString() + 
               ", newBounds=" + newBounds.toString() + "}";
    }
}