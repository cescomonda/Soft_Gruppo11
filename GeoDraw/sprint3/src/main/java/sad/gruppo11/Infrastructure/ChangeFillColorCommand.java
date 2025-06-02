package sad.gruppo11.Infrastructure;

import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.geometry.ColorData;
import java.util.Objects;

public class ChangeFillColorCommand extends AbstractShapeCommand {
    private final ColorData newColor;
    private ColorData oldColor;

    public ChangeFillColorCommand(Drawing drawing, Shape shape, ColorData newColor) {
        super(drawing, shape);
        Objects.requireNonNull(newColor, "New color cannot be null for ChangeFillColorCommand.");
        this.newColor = new ColorData(newColor);
    }

    @Override
    public void execute() {
        if (this.oldColor == null) {
            ColorData currentFill = this.receiverShape.getFillColor();
            this.oldColor = (currentFill == null) ? ColorData.TRANSPARENT : new ColorData(currentFill);
        }
        this.drawing.setShapeFillColor(receiverShape, newColor);
    }

    @Override
    public void undo() {
        this.drawing.setShapeFillColor(receiverShape, oldColor);
    }
    
    @Override
    public String toString() {
        return "ChangeFillColorCommand{shapeId=" + receiverShape.getId().toString() + 
               ", newColor=" + newColor.toString() + "}";
    }
}