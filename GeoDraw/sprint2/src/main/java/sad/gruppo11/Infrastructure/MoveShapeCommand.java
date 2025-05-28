package sad.gruppo11.Infrastructure;

import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.geometry.Vector2D;
import java.util.Objects;

public class MoveShapeCommand extends AbstractShapeCommand {
    private final Vector2D moveVector;

    public MoveShapeCommand(Shape shape, Vector2D vector) {
        super(shape);
        Objects.requireNonNull(vector, "Move vector cannot be null for MoveShapeCommand.");
        this.moveVector = new Vector2D(vector);
    }

    @Override
    public void execute() {
        this.receiverShape.move(moveVector);
    }

    @Override
    public void undo() {
        this.receiverShape.move(moveVector.inverse());
    }

    @Override
    public String toString() {
        return "MoveShapeCommand{shapeId=" + receiverShape.getId().toString() + 
               ", vector=" + moveVector.toString() + "}";
    }
}