package sad.gruppo11.Infrastructure;

import sad.gruppo11.Model.Shape;
import java.util.Objects;

public class RotateShapeCommand extends AbstractShapeCommand {
    private final double targetAngle; // Store absolute target angle
    private double oldAngle;

    public RotateShapeCommand(Shape shape, double targetAngle) {
        super(shape);
        this.targetAngle = targetAngle;
    }

    @Override
    public void execute() {
        this.oldAngle = receiverShape.getRotation();
        receiverShape.setRotation(this.targetAngle);
    }

    @Override
    public void undo() {
        receiverShape.setRotation(this.oldAngle);
    }
    @Override
    public String toString() {
        return "RotateShapeCommand{shapeId=" + receiverShape.getId().toString() + ", targetAngle=" + targetAngle + "}";
    }
}