package sad.gruppo11.Infrastructure;

import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.geometry.Vector2D;
import java.util.Objects;

public class MoveShapeCommand extends AbstractShapeCommand {
    private final Vector2D moveVector;

    public MoveShapeCommand(Drawing drawing, Shape shape, Vector2D vector) {
        super(drawing, shape);
        Objects.requireNonNull(vector, "Move vector cannot be null for MoveShapeCommand.");
        this.moveVector = new Vector2D(vector);
    }

    @Override
    public void execute() {
        this.drawing.moveShape(receiverShape, moveVector);
    }

    @Override
    public void undo() {
        this.drawing.moveShape(receiverShape, moveVector.inverse());
    }

    @Override
    public String toString() {
        return "MoveShapeCommand{shapeId=" + receiverShape.getId().toString() + 
               ", vector=" + moveVector.toString() + "}";
    }
}