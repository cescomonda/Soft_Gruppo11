package sad.gruppo11.Infrastructure;

import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.TextShape;
import java.util.Objects;

public class ChangeTextSizeCommand extends AbstractShapeCommand {
    private final double newSize;
    private double oldSize;

    public ChangeTextSizeCommand(Shape textShape, double newSize) {
        super(textShape);
        if (!(textShape instanceof TextShape)) {
            throw new IllegalArgumentException("ChangeTextSizeCommand requires a TextShape.");
        }
        if (newSize <= 0) {
            throw new IllegalArgumentException("New font size must be positive.");
        }
        this.newSize = newSize;
    }

    @Override
    public void execute() {
        TextShape ts = (TextShape) receiverShape;
        this.oldSize = ts.getFontSize();
        ts.setFontSize(newSize);
    }

    @Override
    public void undo() {
        TextShape ts = (TextShape) receiverShape;
        ts.setFontSize(oldSize);
    }
    @Override
    public String toString() {
        return "ChangeTextSizeCommand{shapeId=" + receiverShape.getId().toString() + ", newSize=" + newSize + "}";
    }
}