package sad.gruppo11.Infrastructure;

import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.TextShape;
import java.util.Objects;

public class ChangeTextSizeCommand extends AbstractShapeCommand {
    private final double newSize;
    private Double oldSize = null;

    public ChangeTextSizeCommand(Drawing drawing, Shape textShape, double newSize) {
        super(drawing, textShape);
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
        if(this.oldSize == null)
            this.oldSize = ts.getFontSize();
        drawing.setShapeFontSize(ts, newSize);
    }

    @Override
    public void undo() {
        TextShape ts = (TextShape) receiverShape;
        drawing.setShapeFontSize(ts, oldSize);
    }
    @Override
    public String toString() {
        return "ChangeTextSizeCommand{shapeId=" + receiverShape.getId().toString() + ", newSize=" + newSize + "}";
    }
}