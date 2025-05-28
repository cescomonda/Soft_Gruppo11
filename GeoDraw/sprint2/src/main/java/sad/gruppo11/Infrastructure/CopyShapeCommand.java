package sad.gruppo11.Infrastructure;

import sad.gruppo11.Model.Shape;
import java.util.Objects;

public class CopyShapeCommand extends AbstractShapeCommand {
    private final Clipboard clipboard;

    public CopyShapeCommand(Shape shapeToCopy, Clipboard clipboard) {
        super(shapeToCopy);
        Objects.requireNonNull(clipboard, "Clipboard cannot be null for CopyShapeCommand.");
        this.clipboard = clipboard;
    }

    @Override
    public void execute() {
        if (receiverShape != null) {
            clipboard.set(receiverShape);
        }
    }

    @Override
    public void undo() {
        // No-op
    }

    @Override
    public String toString() {
        return "CopyShapeCommand{shapeId=" + (receiverShape != null ? receiverShape.getId().toString() : "null") + "}";
    }
}