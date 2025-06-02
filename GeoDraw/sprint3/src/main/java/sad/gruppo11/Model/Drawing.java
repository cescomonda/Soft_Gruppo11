
package sad.gruppo11.Model;

import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Rect;
import sad.gruppo11.Model.geometry.Vector2D;
import sad.gruppo11.View.Observer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList; // Per gestione concorrente degli observer

public class Drawing implements Observable, Serializable {
    private static final long serialVersionUID = 20240115L; // Mantieni o aggiorna se cambi campi serializzati

    private List<Shape> shapes;
    // protected per i test
    protected transient List<Observer> observers; // Marcato transient per non serializzarlo

    public Drawing() {
        this.shapes = new ArrayList<>(); // Inizializza sempre la lista delle forme
        this.observers = new CopyOnWriteArrayList<>(); // Usa una lista thread-safe per gli observer
    }

    // Metodo custom per la deserializzazione per reinizializzare la lista observers
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject(); // Deserializza i campi non transient (come 'shapes')
        this.observers = new CopyOnWriteArrayList<>(); // Reinizializza la lista transient
    }

    public void addShape(Shape s) {
        Objects.requireNonNull(s, "Shape to add cannot be null.");
        this.shapes.add(s);
        notifyObservers(new DrawingChangeEvent(s, DrawingChangeEvent.ChangeType.ADD));
    }

    public void addShapeAtIndex(Shape shape, int index) {
        Objects.requireNonNull(shape, "Shape to add cannot be null.");
        if (index < 0 || index > shapes.size()) {
            throw new IndexOutOfBoundsException("Index " + index + " is out of bounds for shapes list size " + shapes.size());
        }
        this.shapes.add(index, shape);
        notifyObservers(new DrawingChangeEvent(shape, DrawingChangeEvent.ChangeType.ADD));
    }

    public boolean removeShape(Shape s) {
        Objects.requireNonNull(s, "Shape to remove cannot be null.");
        boolean removed = this.shapes.remove(s);
        if (removed) {
            notifyObservers(new DrawingChangeEvent(s, DrawingChangeEvent.ChangeType.REMOVE));
        }
        return removed;
    }
    
    public Shape removeShapeById(UUID shapeId) {
        Objects.requireNonNull(shapeId, "Shape ID cannot be null for removal.");
        Shape removedShape = null;
        for (int i = 0; i < shapes.size(); i++) {
            if (shapes.get(i).getId().equals(shapeId)) {
                removedShape = shapes.remove(i);
                break;
            }
        }
        if (removedShape != null) {
            notifyObservers(new DrawingChangeEvent(removedShape, DrawingChangeEvent.ChangeType.REMOVE));
        }
        return removedShape;
    }

    public void clear() {
        if (!this.shapes.isEmpty()) {
            // Crea una copia delle forme prima di pulire per l'evento CLEAR, se necessario
            List<Shape> oldShapes = new ArrayList<>(this.shapes); 
            this.shapes.clear();
            notifyObservers(new DrawingChangeEvent(oldShapes, DrawingChangeEvent.ChangeType.CLEAR));
        } else {
            // Notifica anche se era già vuoto, così la UI può aggiornarsi (es. deselezionare)
            notifyObservers(new DrawingChangeEvent(Collections.emptyList(), DrawingChangeEvent.ChangeType.CLEAR));
        }
    }
    
    /**
     * Restituisce una lista non modificabile delle forme nell'ordine Z corretto (dal basso verso l'alto).
     */
    public List<Shape> getShapesInZOrder() {
        return Collections.unmodifiableList(new ArrayList<>(this.shapes));
    }
    
    /**
     * Restituisce una riferimento alla lista interna modificabile delle forme.
     * Usare con cautela, preferibilmente solo da comandi o logica interna che
     * sa come gestire le notifiche agli observer.
     */
    public List<Shape> getModifiableShapesList() {
        return this.shapes;
    }

    public Shape findShapeById(UUID id) {
        Objects.requireNonNull(id, "ID cannot be null for findShapeById.");
        for (Shape shape : this.shapes) {
            if (id.equals(shape.getId())) {
                return shape;
            }
            // Se le forme possono essere GroupShape, potresti voler cercare ricorsivamente
            if (shape.isComposite()) {
                Shape foundInChild = findShapeInGroup(shape, id);
                if (foundInChild != null) return foundInChild;
            }
        }
        return null;
    }
    
    private Shape findShapeInGroup(Shape group, UUID id) {
        for (Shape child : group.getChildren()) {
            if (id.equals(child.getId())) {
                return child;
            }
            if (child.isComposite()) {
                Shape foundInGrandChild = findShapeInGroup(child, id);
                if (foundInGrandChild != null) return foundInGrandChild;
            }
        }
        return null;
    }


    public int getShapeIndex(Shape shape) {
        Objects.requireNonNull(shape, "Shape cannot be null for getShapeIndex.");
        return this.shapes.indexOf(shape);
    }

    // ----------------------- Shapes Modifiers --------------------------------

    public void setShapeFillColor(Shape shape, ColorData color) {
        Objects.requireNonNull(shape, "Shape cannot be null for setShapeFillColor.");
        Objects.requireNonNull(color, "Color cannot be null for setShapeFillColor.");
        if( !this.shapes.contains(shape)) {
            throw new IllegalArgumentException("Shape not found in the drawing.");
        }
        shape.setFillColor(color);
        notifyObservers(new DrawingChangeEvent(shape, DrawingChangeEvent.ChangeType.MODIFY));
    }

    public void setShapeRotation(Shape shape, double angle) {
        Objects.requireNonNull(shape, "Shape cannot be null for setShapeRotation.");
        if( !this.shapes.contains(shape)) {
            throw new IllegalArgumentException("Shape not found in the drawing.");
        }
        shape.setRotation(angle);
        notifyObservers(new DrawingChangeEvent(shape, DrawingChangeEvent.ChangeType.MODIFY));
    }

    public void setShapeText(Shape shape, String text) {
        Objects.requireNonNull(shape, "Shape cannot be null for setShapeText.");
        Objects.requireNonNull(text, "Text cannot be null for setShapeText.");
        if( !this.shapes.contains(shape)) {
            throw new IllegalArgumentException("Shape not found in the drawing.");
        }
        if (!(shape instanceof TextShape)) {
            throw new IllegalArgumentException("Shape is not a TextShape.");
        }
        ((TextShape) shape).setText(text);
        notifyObservers(new DrawingChangeEvent(shape, DrawingChangeEvent.ChangeType.MODIFY));
    }

    public void setShapeFontSize(Shape shape, double size) {
        Objects.requireNonNull(shape, "Shape cannot be null for setShapeFontSize.");
        if( !this.shapes.contains(shape)) {
            throw new IllegalArgumentException("Shape not found in the drawing.");
        }
        if (!(shape instanceof TextShape)) {
            throw new IllegalArgumentException("Shape is not a TextShape.");
        }
        ((TextShape) shape).setFontSize(size);
        notifyObservers(new DrawingChangeEvent(shape, DrawingChangeEvent.ChangeType.MODIFY));
    }

    public void setShapeStrokeColor(Shape shape, ColorData color) {
        Objects.requireNonNull(shape, "Shape cannot be null for setShapeStrokeColor.");
        Objects.requireNonNull(color, "Color cannot be null for setShapeStrokeColor.");
        if( !this.shapes.contains(shape)) {
            throw new IllegalArgumentException("Shape not found in the drawing.");
        }
        shape.setStrokeColor(color);
        notifyObservers(new DrawingChangeEvent(shape, DrawingChangeEvent.ChangeType.MODIFY));
    }

    public void moveShape(Shape shape, Vector2D v) {
        Objects.requireNonNull(shape, "Shape cannot be null for moveShape.");
        Objects.requireNonNull(v, "Vector cannot be null for moveShape.");
        if( !this.shapes.contains(shape)) {
            throw new IllegalArgumentException("Shape not found in the drawing.");
        }
        shape.move(v);
        notifyObservers(new DrawingChangeEvent(shape, DrawingChangeEvent.ChangeType.MODIFY));
    }

    public void resizeShape(Shape shape, Rect bounds) {
        Objects.requireNonNull(shape, "Shape cannot be null for resizeShape.");
        Objects.requireNonNull(bounds, "Bounds cannot be null for resizeShape.");
        if( !this.shapes.contains(shape)) {
            throw new IllegalArgumentException("Shape not found in the drawing.");
        }
        shape.resize(bounds);
        notifyObservers(new DrawingChangeEvent(shape, DrawingChangeEvent.ChangeType.MODIFY));
    }

    public void reflectShapeHorizontal(Shape shape) {
        Objects.requireNonNull(shape, "Shape cannot be null for reflectShapeHorizontal.");
        if( !this.shapes.contains(shape)) {
            throw new IllegalArgumentException("Shape not found in the drawing.");
        }
        shape.reflectHorizontal();
        notifyObservers(new DrawingChangeEvent(shape, DrawingChangeEvent.ChangeType.MODIFY));
    }

    public void reflectShapeVertical(Shape shape) {
        Objects.requireNonNull(shape, "Shape cannot be null for reflectShapeVertical.");
        if( !this.shapes.contains(shape)) {
            throw new IllegalArgumentException("Shape not found in the drawing.");
        }
        shape.reflectVertical();
        notifyObservers(new DrawingChangeEvent(shape, DrawingChangeEvent.ChangeType.MODIFY));
    }

    public void bringToFront(Shape shape) {
        Objects.requireNonNull(shape, "Shape cannot be null for bringToFront.");
        if (this.shapes.remove(shape)) { // Rimuove e restituisce true se presente
            this.shapes.add(shape); // Aggiunge alla fine (in cima)
            notifyObservers(new DrawingChangeEvent(shape, DrawingChangeEvent.ChangeType.Z_ORDER));
        }
    }

    public void sendToBack(Shape shape) {
        Objects.requireNonNull(shape, "Shape cannot be null for sendToBack.");
        if (this.shapes.remove(shape)) {
            this.shapes.add(0, shape); // Aggiunge all'inizio (in fondo)
            notifyObservers(new DrawingChangeEvent(shape, DrawingChangeEvent.ChangeType.Z_ORDER));
        }
    }

    // ------------------------------------------------------------------------

    @Override
    public void attach(Observer o) {
        Objects.requireNonNull(o, "Observer to attach cannot be null.");
        if (this.observers == null) { // Salvaguardia in caso di problemi di deserializzazione imprevisti
            this.observers = new CopyOnWriteArrayList<>();
        }
        if (!this.observers.contains(o)) {
            this.observers.add(o);
        }
    }

    @Override
    public void detach(Observer o) {
        Objects.requireNonNull(o, "Observer to detach cannot be null.");
        if (this.observers != null) {
            this.observers.remove(o);
        }
    }

    @Override
    public void notifyObservers(Object arg) {
        if (this.observers == null) {
            return; // Non ci sono observer da notificare
        }
        for (Observer observer : this.observers) {
            observer.update(this, arg);
        }
    }
    
    /**
     * Evento che descrive un cambiamento nel Drawing.
     * Può essere usato per notificare gli Observer con dettagli specifici.
     */
    public static class DrawingChangeEvent implements Serializable {
        public enum ChangeType { 
            ADD, REMOVE, MODIFY, 
            Z_ORDER, CLEAR, LOAD, 
            TRANSFORM, GRID, SELECTION 
        }
        public final ChangeType type;
        public final Shape changedShape;       // Per ADD, REMOVE, MODIFY, Z_ORDER, SELECTION (può essere null)
        public final List<Shape> allShapes;    // Per CLEAR (lista delle forme rimosse), LOAD (tutte le nuove forme)
        
        public DrawingChangeEvent(Shape shape, ChangeType type) {
            this.changedShape = shape; 
            this.allShapes = null; 
            this.type = type;
        }
        public DrawingChangeEvent(List<Shape> shapes, ChangeType type) {
            this.changedShape = null; 
            this.allShapes = shapes; 
            this.type = type;
        }
        // Costruttore per eventi generici senza una forma specifica o una lista di forme
        // (es. TRANSFORM, GRID, o un MODIFY generico del disegno)
        public DrawingChangeEvent(ChangeType type) { 
            this.changedShape = null; 
            this.allShapes = null; 
            this.type = type;
        }
    }

    @Override
    public String toString() {
        int obsCount = (observers != null) ? observers.size() : 0;
        return "Drawing{shapesCount=" + shapes.size() + ", observersCount=" + obsCount + '}';
    }
}
