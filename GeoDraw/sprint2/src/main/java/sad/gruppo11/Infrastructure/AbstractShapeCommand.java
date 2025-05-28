package sad.gruppo11.Infrastructure;

import sad.gruppo11.Model.Shape;
import java.util.Objects;

public abstract class AbstractShapeCommand implements Command {
    protected final Shape receiverShape;

    public AbstractShapeCommand(Shape receiver) {
        Objects.requireNonNull(receiver, "Receiver Shape cannot be null for AbstractShapeCommand.");
        this.receiverShape = receiver;
    }
}