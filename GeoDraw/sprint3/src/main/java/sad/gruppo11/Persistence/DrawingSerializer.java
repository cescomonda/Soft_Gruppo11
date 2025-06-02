
package sad.gruppo11.Persistence;

import sad.gruppo11.Model.Drawing;
import java.io.*;
import java.util.Objects;

public class DrawingSerializer implements IDrawingSerializer {
    @Override
    public void save(Drawing d, String path) throws IOException {
        Objects.requireNonNull(d, "Drawing to save cannot be null.");
        Objects.requireNonNull(path, "File path cannot be null.");
        if (path.isEmpty()) throw new IllegalArgumentException("File path cannot be empty for save.");
        
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
            oos.writeObject(d);
        }
    }

    @Override
    public Drawing load(String path) throws IOException, ClassNotFoundException {
        Objects.requireNonNull(path, "File path cannot be null.");
        if (path.isEmpty()) throw new IllegalArgumentException("File path cannot be empty for load.");
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))) {
            Object obj = ois.readObject();
            if (obj instanceof Drawing) {
                return (Drawing) obj;
            } else {
                throw new IOException("Invalid file content: expected Drawing, found " + 
                                      (obj != null ? obj.getClass().getName() : "null"));
            }
        }
    }
}
