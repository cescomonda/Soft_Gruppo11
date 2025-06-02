package sad.gruppo11.Infrastructure;

import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.Shape;
import java.util.Objects;

public class SendToBackCommand extends AbstractDrawingCommand {
    private final Shape shapeToModify;
    private int originalIndex = -1;

    public SendToBackCommand(Drawing drawing, Shape shape) {
        super(drawing);
        Objects.requireNonNull(shape, "Shape to modify cannot be null.");
        this.shapeToModify = shape;
    }

    @Override
    public void execute() {
        this.originalIndex = receiverDrawing.getShapeIndex(shapeToModify);
        if (this.originalIndex != -1) {
            receiverDrawing.sendToBack(shapeToModify);
        }
    }

    @Override
    public void undo() {
         if (this.originalIndex != -1) {
            if (receiverDrawing.removeShape(shapeToModify)) {
                if (this.originalIndex <= receiverDrawing.getModifiableShapesList().size()) {
                    receiverDrawing.addShapeAtIndex(shapeToModify, this.originalIndex);
                } else {
                    receiverDrawing.addShape(shapeToModify); 
                }
            }
        }
    }
    @Override
    public String toString() {
        return "SendToBackCommand{shapeId=" + (shapeToModify != null ? shapeToModify.getId().toString() : "null") + "}";
    }
}