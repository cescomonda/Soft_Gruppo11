package sad.gruppo11.Infrastructure;

import java.io.Serializable;

public interface Command extends Serializable {
    void execute();
    void undo();
}