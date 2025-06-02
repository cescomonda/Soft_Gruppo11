
package sad.gruppo11.Persistence;

import sad.gruppo11.Controller.ReusableShapeLibrary; // Assumendo che sia in Controller

import java.io.*;
import java.util.Objects;

public class ReusableShapeLibrarySerializer implements IReusableShapeLibrarySerializer {

    @Override
    public void save(ReusableShapeLibrary library, String path) throws IOException {
        Objects.requireNonNull(library, "ReusableShapeLibrary to save cannot be null.");
        Objects.requireNonNull(path, "File path for saving library cannot be null.");
        if (path.isEmpty()) {
            throw new IllegalArgumentException("File path for saving library cannot be empty.");
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
            oos.writeObject(library);
        }
    }

    @Override
    public ReusableShapeLibrary load(String path) throws IOException, ClassNotFoundException {
        Objects.requireNonNull(path, "File path for loading library cannot be null.");
        if (path.isEmpty()) {
            throw new IllegalArgumentException("File path for loading library cannot be empty.");
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))) {
            Object obj = ois.readObject();
            if (obj instanceof ReusableShapeLibrary) {
                return (ReusableShapeLibrary) obj;
            } else {
                throw new IOException("Invalid file content: expected ReusableShapeLibrary, found " +
                                      (obj != null ? obj.getClass().getName() : "null"));
            }
        }
    }
}
