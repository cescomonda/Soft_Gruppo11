package sad.gruppo11.Infrastructure;

import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.Shape;
import java.util.Objects;

public class RotateShapeCommand extends AbstractShapeCommand {
    private final double targetAngle; // Store absolute target angle
    private double oldAngle;

    public RotateShapeCommand(Drawing drawing, Shape shape, double targetAngle) {
        super(drawing, shape);
        this.targetAngle = targetAngle;
    }

    @Override
    public void execute() {
        this.oldAngle = receiverShape.getRotation();
        this.drawing.setShapeRotation(receiverShape, this.targetAngle);
    }

    @Override
    public void undo() {
        this.drawing.setShapeRotation(receiverShape, this.oldAngle);
    }
    @Override
    public String toString() {
        return "RotateShapeCommand{shapeId=" + receiverShape.getId().toString() + ", targetAngle=" + targetAngle + "}";
    }
}