package sad.gruppo11.Model;

import sad.gruppo11.View.Observer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class Drawing implements Observable, Serializable {
    private static final long serialVersionUID = 20240115L;

    private List<Shape> shapes;
    private transient List<Observer> observers;

    public Drawing() {
        this.shapes = new ArrayList<>();
        this.observers = new CopyOnWriteArrayList<>();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.observers = new CopyOnWriteArrayList<>();
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
            List<Shape> oldShapes = new ArrayList<>(this.shapes);
            this.shapes.clear();
            notifyObservers(new DrawingChangeEvent(oldShapes, DrawingChangeEvent.ChangeType.CLEAR));
        }
    }
    
    public List<Shape> getShapesInZOrder() {
        return Collections.unmodifiableList(new ArrayList<>(this.shapes));
    }
    
    public List<Shape> getModifiableShapesList() {
        return this.shapes;
    }

    public Shape findShapeById(UUID id) {
        Objects.requireNonNull(id, "ID cannot be null for findShapeById.");
        for (Shape shape : this.shapes) {
            if (id.equals(shape.getId())) {
                return shape;
            }
        }
        return null;
    }

    public int getShapeIndex(Shape shape) {
        Objects.requireNonNull(shape, "Shape cannot be null for getShapeIndex.");
        return this.shapes.indexOf(shape);
    }

    public void bringToFront(Shape shape) {
        Objects.requireNonNull(shape, "Shape cannot be null for bringToFront.");
        if (this.shapes.remove(shape)) {
            this.shapes.add(shape);
            notifyObservers(new DrawingChangeEvent(shape, DrawingChangeEvent.ChangeType.Z_ORDER));
        }
    }

    public void sendToBack(Shape shape) {
        Objects.requireNonNull(shape, "Shape cannot be null for sendToBack.");
        if (this.shapes.remove(shape)) {
            this.shapes.add(0, shape);
            notifyObservers(new DrawingChangeEvent(shape, DrawingChangeEvent.ChangeType.Z_ORDER));
        }
    }

    @Override
    public void attach(Observer o) {
        Objects.requireNonNull(o, "Observer to attach cannot be null.");
        if (this.observers == null) {
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
            this.observers = new CopyOnWriteArrayList<>();
        }
        for (Observer observer : this.observers) {
            observer.update(this, arg);
        }
    }
    
    public static class DrawingChangeEvent implements Serializable {
        public enum ChangeType { ADD, REMOVE, MODIFY, Z_ORDER, CLEAR, LOAD, TRANSFORM, GRID }
        public final ChangeType type;
        public final Shape changedShape;
        public final List<Shape> allShapes;
        public DrawingChangeEvent(Shape shape, ChangeType type) {
            this.changedShape = shape; this.allShapes = null; this.type = type;
        }
        public DrawingChangeEvent(List<Shape> shapes, ChangeType type) {
            this.changedShape = null; this.allShapes = shapes; this.type = type;
        }
        public DrawingChangeEvent(ChangeType type) {
            this.changedShape = null; this.allShapes = null; this.type = type;
        }
    }

    @Override
    public String toString() {
        int obsCount = (observers != null) ? observers.size() : 0;
        return "Drawing{shapesCount=" + shapes.size() + ", observersCount=" + obsCount + '}';
    }
}