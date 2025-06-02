package sad.gruppo11.Infrastructure;

import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.geometry.ColorData;
import java.util.Objects;

public class ChangeStrokeColorCommand extends AbstractShapeCommand {
    private final ColorData newColor;
    private ColorData oldColor;

    public ChangeStrokeColorCommand(Drawing drawing, Shape shape, ColorData newColor) {
        super(drawing, shape);
        Objects.requireNonNull(newColor, "New color cannot be null for ChangeStrokeColorCommand.");
        this.newColor = new ColorData(newColor);
    }

    @Override
    public void execute() {
        if (this.oldColor == null) {
            this.oldColor = new ColorData(this.receiverShape.getStrokeColor());
        }
        this.drawing.setShapeStrokeColor(receiverShape, newColor);
    }

    @Override
    public void undo() {
        if (this.oldColor != null) {
            this.drawing.setShapeStrokeColor(receiverShape, this.oldColor);
        }
    }
    
    @Override
    public String toString() {
        return "ChangeStrokeColorCommand{shapeId=" + receiverShape.getId().toString() + 
               ", newColor=" + newColor.toString() + "}";
    }
}