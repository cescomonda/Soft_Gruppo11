package sad.gruppo11.Infrastructure;

import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.geometry.Rect;
import java.util.Objects;

public class ResizeShapeCommand extends AbstractShapeCommand {
    private final Rect newBounds;
    private Rect oldBounds;

    public ResizeShapeCommand(Shape shape, Rect newBounds) {
        super(shape);
        Objects.requireNonNull(newBounds, "New bounds cannot be null for ResizeShapeCommand.");
        this.newBounds = new Rect(newBounds);
    }

    @Override
    public void execute() {
        if (this.oldBounds == null) {
            this.oldBounds = new Rect(this.receiverShape.getBounds());
        }
        this.receiverShape.resize(newBounds);
    }

    @Override
    public void undo() {
        if (this.oldBounds != null) {
            this.receiverShape.resize(oldBounds);
        }
    }

    @Override
    public String toString() {
        return "ResizeShapeCommand{shapeId=" + receiverShape.getId().toString() + 
               ", newBounds=" + newBounds.toString() + "}";
    }
}