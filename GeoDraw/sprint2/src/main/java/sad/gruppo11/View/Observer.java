package sad.gruppo11.View;

import sad.gruppo11.Model.Observable;
import java.io.Serializable;

public interface Observer extends Serializable {
    void update(Observable source, Object arg);
}