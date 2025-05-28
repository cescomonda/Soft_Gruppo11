package sad.gruppo11.Infrastructure;

import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.Shape;
import java.util.Objects;

public class CutShapeCommand extends AbstractShapeCommand {
    private final Drawing drawing;
    private final Clipboard clipboard;
    private int originalIndex = -1;

    public CutShapeCommand(Shape shapeToCut, Drawing drawing, Clipboard clipboard) {
        super(shapeToCut);
        Objects.requireNonNull(drawing, "Drawing cannot be null for CutShapeCommand.");
        Objects.requireNonNull(clipboard, "Clipboard cannot be null for CutShapeCommand.");
        this.drawing = drawing;
        this.clipboard = clipboard;
    }

    @Override
    public void execute() {
        clipboard.set(receiverShape);
        this.originalIndex = drawing.getShapeIndex(receiverShape);
        drawing.removeShape(receiverShape);
    }

    @Override
    public void undo() {
        if (this.originalIndex != -1 && this.originalIndex <= drawing.getModifiableShapesList().size()) {
            drawing.addShapeAtIndex(receiverShape, this.originalIndex);
        } else {
            drawing.addShape(receiverShape);
        }
    }

    @Override
    public String toString() {
        return "CutShapeCommand{shapeId=" + receiverShape.getId().toString() + "}";
    }
}