
package sad.gruppo11.Infrastructure;

import sad.gruppo11.Model.Drawing;
import java.util.Objects;

public abstract class AbstractDrawingCommand implements Command {
    protected final Drawing receiverDrawing;

    public AbstractDrawingCommand(Drawing receiver) {
        Objects.requireNonNull(receiver, "Receiver Drawing cannot be null for AbstractDrawingCommand.");
        this.receiverDrawing = receiver;
    }
}
