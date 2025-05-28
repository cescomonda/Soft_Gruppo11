package sad.gruppo11.Persistence;

import sad.gruppo11.Model.Drawing;
import java.util.Objects;

public class PersistenceController {
    private final IDrawingSerializer serializer;

    public PersistenceController(IDrawingSerializer serializer) {
        Objects.requireNonNull(serializer, "Serializer cannot be null for PersistenceController.");
        this.serializer = serializer;
    }

    public void saveDrawing(Drawing d, String path) throws Exception {
        Objects.requireNonNull(d, "Drawing to save cannot be null.");
        Objects.requireNonNull(path, "File path cannot be null.");
        if (path.isEmpty()) throw new IllegalArgumentException("File path cannot be empty for saving.");
        serializer.save(d, path);
    }

    public Drawing loadDrawing(String path) throws Exception {
        Objects.requireNonNull(path, "File path cannot be null.");
        if (path.isEmpty()) throw new IllegalArgumentException("File path cannot be empty for loading.");
        return serializer.load(path);
    }
}