
package sad.gruppo11.Model;

import sad.gruppo11.View.Observer;

public interface Observable {
    void attach(Observer o);
    void detach(Observer o);
    void notifyObservers(Object arg);
}
