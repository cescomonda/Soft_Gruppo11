
package sad.gruppo11.Model;

import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Rect;
import sad.gruppo11.Model.geometry.Vector2D;
import sad.gruppo11.View.ShapeVisitor;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public interface Shape extends Serializable {
    UUID getId();
    void move(Vector2D v);
    void resize(Rect bounds); // Bounds in coordinate del mondo, non ruotati
    
    void setStrokeColor(ColorData c);
    ColorData getStrokeColor();
    
    void setFillColor(ColorData c);
    ColorData getFillColor();
    
    boolean contains(Point2D p); // Punto in coordinate del mondo
    void accept(ShapeVisitor v);
    Shape clone(); // Crea una copia esatta (stesso ID)
    Rect getBounds(); // Restituisce il bounding box NON ruotato della forma

    void setRotation(double angle); // Angolo in gradi
    double getRotation(); // Angolo in gradi

    void setText(String text); // Per TextShape
    String getText(); // Per TextShape

    void setFontSize(double size); // Per TextShape
    double getFontSize(); // Per TextShape

    Shape cloneWithNewId(); // Crea una copia con un nuovo ID

    // --- Nuovi metodi per Sprint 3 ---

    /**
     * Riflette la forma orizzontalmente rispetto al centro del suo bounding box.
     */
    void reflectHorizontal();

    /**
     * Riflette la forma verticalmente rispetto al centro del suo bounding box.
     */
    void reflectVertical();

    /**
     * Aggiunge una forma figlia (per forme composite come GroupShape).
     * Lancia UnsupportedOperationException per forme foglia.
     * @param s La forma da aggiungere.
     */
    void add(Shape s);

    /**
     * Rimuove una forma figlia (per forme composite come GroupShape).
     * Lancia UnsupportedOperationException per forme foglia.
     * @param s La forma da rimuovere.
     */
    void remove(Shape s);

    /**
     * Restituisce una forma figlia all'indice specificato (per forme composite).
     * Lancia UnsupportedOperationException per forme foglia.
     * @param i L'indice della forma figlia.
     * @return La forma figlia.
     */
    Shape getChild(int i);

    /**
     * Restituisce una lista (non modificabile) di tutte le forme figlie (per forme composite).
     * Restituisce una lista vuota per forme foglia.
     * @return Lista delle forme figlie.
     */
    List<Shape> getChildren();
    
    /**
     * Indica se questa forma è una composita (cioè, può contenere altre forme).
     * @return true se è una forma composita, false altrimenti.
     */
    boolean isComposite();

    /**
     * Restituisce il bounding box allineato agli assi (AABB) che racchiude
     * la forma dopo che è stata applicata la sua rotazione.
     * @return Il Rect che rappresenta l'AABB della forma ruotata.
     */
    Rect getRotatedBounds();
}
