
package sad.gruppo11.Model;

import java.util.Collections;
import java.util.List;

/**
 * Classe base astratta per le forme "foglia" (non composite)
 * Fornisce implementazioni di default per i metodi del pattern Composite.
 */
public abstract class AbstractBaseShape implements Shape {

    
    // Implementazioni di default per i metodi del Composite Pattern
    // Le forme foglia non supportano queste operazioni.
    
    @Override
    public void add(Shape s) {
        throw new UnsupportedOperationException("Cannot add to a leaf shape.");
    }
    
    @Override
    public void remove(Shape s) {
        throw new UnsupportedOperationException("Cannot remove from a leaf shape.");
    }
    
    @Override
    public Shape getChild(int i) {
        throw new UnsupportedOperationException("Leaf shapes do not have children.");
    }
    
    @Override
    public List<Shape> getChildren() {
        return Collections.emptyList(); // Le forme foglia non hanno figli
    }
    
    @Override
    public boolean isComposite() {
        return false; // Le forme foglia non sono composite
    }
    
    @Override
    public Shape clone() {
        try {
            return (AbstractBaseShape) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(); // Should never happen since we are Cloneable
        }
    }
}
