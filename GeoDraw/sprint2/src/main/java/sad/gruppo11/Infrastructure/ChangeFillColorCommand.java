package sad.gruppo11.Infrastructure;

import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.geometry.ColorData;
import java.util.Objects;

public class ChangeFillColorCommand extends AbstractShapeCommand {
    private final ColorData newColor;
    private ColorData oldColor;

    public ChangeFillColorCommand(Shape shape, ColorData newColor) {
        super(shape);
        Objects.requireNonNull(newColor, "New color cannot be null for ChangeFillColorCommand.");
        this.newColor = new ColorData(newColor);
    }

    @Override
    public void execute() {
        if (this.oldColor == null) {
            ColorData currentFill = this.receiverShape.getFillColor();
            this.oldColor = (currentFill == null) ? ColorData.TRANSPARENT : new ColorData(currentFill);
        }
        this.receiverShape.setFillColor(newColor);
    }

    @Override
    public void undo() {
        this.receiverShape.setFillColor(oldColor);
    }
    
    @Override
    public String toString() {
        return "ChangeFillColorCommand{shapeId=" + receiverShape.getId().toString() + 
               ", newColor=" + newColor.toString() + "}";
    }
}