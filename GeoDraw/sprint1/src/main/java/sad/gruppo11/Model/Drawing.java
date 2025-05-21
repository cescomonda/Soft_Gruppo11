package sad.gruppo11.Model;

import java.util.List;
import java.io.IOException;
import java.lang.reflect.Field;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import sad.gruppo11.View.Observer;

/*
 * Rappresenta il modello principale che contiene le forme geometriche disegnate.
 * Implementa il pattern Observer per notificare le view (es. DrawingView) in caso di cambiamenti.
 * Supporta la serializzazione e la deserializzazione personalizzata, incluso il reintegro degli observer (che sono transient).
 */
public class Drawing implements Serializable {

    private static final long serialVersionUID = 2L;

    /* Lista delle forme presenti nel disegno */
    private List<Shape> shapes;

    /*
     * Lista degli observer (es. view) che osservano il modello.
     * È transient perché non va serializzata e final per evitare riassegnazioni.
     */
    private transient final List<Observer> observers;

    /*
     * Costruttore di default. Inizializza la lista di forme e la lista thread-safe di observer.
     */
    public Drawing() {
        this.shapes = new ArrayList<>();
        this.observers = new CopyOnWriteArrayList<>();
    }

    /*
     * Metodo speciale per reinizializzare i campi transient dopo la deserializzazione.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        try {
            Field observersField = Drawing.class.getDeclaredField("observers");
            observersField.setAccessible(true);
            observersField.set(this, new CopyOnWriteArrayList<>());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IOException("Failed to reinitialize transient final field 'observers'", e);
        }
    }

    /* --- Metodi di manipolazione delle forme --- */

    public void addShape(Shape s) {
        Objects.requireNonNull(s, "Shape to add cannot be null");
        shapes.add(s);
        notifyObservers();
    }

    public boolean removeShape(Shape s) {
        if (s == null) return false;
        boolean removed = shapes.remove(s);
        if (removed) notifyObservers();
        return removed;
    }

    public boolean removeShapeById(UUID shapeId) {
        if (shapeId == null) return false;
        boolean removed = shapes.removeIf(shape -> shape.getId().equals(shapeId));
        if (removed) notifyObservers();
        return removed;
    }

    public void clear() {
        if (!shapes.isEmpty()) {
            shapes.clear();
            notifyObservers();
        }
    }

    /*
     * Sostituisce tutte le forme nel disegno con una nuova lista (clonata).
     */
    public void setShapes(List<Shape> newShapes) {
        Objects.requireNonNull(newShapes, "New shapes list cannot be null.");
        this.shapes.clear();
        for (Shape s : newShapes) {
            if (s != null) this.shapes.add(s.cloneShape());
        }
        notifyObservers();
    }

    /* --- Accesso alle forme --- */

    public Iterator<Shape> iterator() {
        return Collections.unmodifiableList(new ArrayList<>(shapes)).iterator();
    }

    public List<Shape> getShapes() {
        return Collections.unmodifiableList(new ArrayList<>(shapes));
    }

    public Shape getShapeById(UUID shapeId) {
        if (shapeId == null) return null;
        for (Shape shape : shapes) {
            if (shape.getId().equals(shapeId)) return shape;
        }
        return null;
    }

    /* --- Pattern Observer --- */

    public void addDrawingViewObserver(Observer observer) {
        Objects.requireNonNull(observer, "Observer to add cannot be null");
        if (!observers.contains(observer)) observers.add(observer);
    }

    public void removeDrawingViewObserver(Observer observer) {
        if (observer == null) return;
        observers.remove(observer);
    }

    public void addObserver(Observer observer) {
        Objects.requireNonNull(observer, "Observer cannot be null");
        if (!observers.contains(observer)) observers.add(observer);
    }

    public void removeObserver(Observer observer) {
        if (observer == null) return;
        observers.remove(observer);
    }

    /*
     * Notifica tutti gli observer registrati.
     */
    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.update(this);
        }
    }

    /* --- Debug e utilità --- */

    @Override
    public String toString() {
        return "Drawing{shapesCount=" + shapes.size() + ", observersCount=" + observers.size() + "}";
    }
}
