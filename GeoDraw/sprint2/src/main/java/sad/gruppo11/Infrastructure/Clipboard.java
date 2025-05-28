package sad.gruppo11.Infrastructure;

import sad.gruppo11.Model.Shape;
import java.util.Objects;

public final class Clipboard {
    private static volatile Clipboard instance;
    private Shape content;

    private Clipboard() {}

    public static Clipboard getInstance() {
        if (instance == null) {
            synchronized (Clipboard.class) {
                if (instance == null) {
                    instance = new Clipboard();
                }
            }
        }
        return instance;
    }

    public void set(Shape shape) {
        Objects.requireNonNull(shape, "Shape to set on clipboard cannot be null.");
        this.content = shape.clone(); 
    }

    public Shape get() {
        return (this.content != null) ? this.content.clone() : null;
    }

    public boolean isEmpty() {
        return this.content == null;
    }

    public void clear() {
        this.content = null;
    }
}