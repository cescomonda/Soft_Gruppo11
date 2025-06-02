
package sad.gruppo11.Persistence;

import sad.gruppo11.Model.Drawing;
import java.io.IOException;

public interface IDrawingSerializer {
    void save(Drawing d, String path) throws IOException;
    Drawing load(String path) throws IOException, ClassNotFoundException;
}
