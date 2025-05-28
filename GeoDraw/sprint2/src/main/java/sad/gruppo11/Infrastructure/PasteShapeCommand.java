package sad.gruppo11.Infrastructure;

import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.geometry.Vector2D;
import java.util.Objects;

public class PasteShapeCommand extends AbstractDrawingCommand {
    private final Clipboard clipboard;
    private Shape pastedShapeInstance;
    private final Vector2D pasteOffset;

    public PasteShapeCommand(Drawing drawing, Clipboard clipboard, Vector2D offset) {
        super(drawing);
        Objects.requireNonNull(clipboard, "Clipboard cannot be null for PasteShapeCommand.");
        Objects.requireNonNull(offset, "Paste offset cannot be null.");
        this.clipboard = clipboard;
        this.pasteOffset = new Vector2D(offset);
    }

    @Override
    public void execute() {
        Shape shapeFromClipboard = clipboard.get();
        if (shapeFromClipboard != null) {
            this.pastedShapeInstance = shapeFromClipboard.cloneWithNewId(); 
            this.pastedShapeInstance.move(pasteOffset);
            receiverDrawing.addShape(this.pastedShapeInstance);
        } else {
            this.pastedShapeInstance = null;
        }
    }

    @Override
    public void undo() {
        if (this.pastedShapeInstance != null) {
            receiverDrawing.removeShape(this.pastedShapeInstance);
        }
    }

    @Override
    public String toString() {
        String idStr = (pastedShapeInstance != null) ? pastedShapeInstance.getId().toString() : "none_pasted";
        return "PasteShapeCommand{pastedShapeId=" + idStr + ", offset=" + pasteOffset + "}";
    }
}