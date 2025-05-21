package sad.gruppo11.Model;

import java.util.UUID;
import java.io.Serializable;
import java.util.Objects;

import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Rect;
import sad.gruppo11.Model.geometry.Vector2D;
import sad.gruppo11.View.ShapeVisitor;

/*
 * Rappresenta una forma geometrica di tipo rettangolo.
 * Implementa l'interfaccia Shape e supporta:
 * - Clonazione
 * - Movimento e ridimensionamento
 * - Pattern Visitor
 * - Colori di contorno e riempimento
 */
public class RectangleShape implements Shape, Serializable {

    /* Identificativo unico della forma */
    private final UUID id;

    /* Rettangolo che descrive i limiti della forma */
    private Rect bounds;

    /* Colori */
    private ColorData strokeColor;
    private ColorData fillColor;

    /*
     * Costruttore base con bounds.
     */
    public RectangleShape(Rect bounds) {
        this.id = UUID.randomUUID();
        if (bounds == null) {
            throw new IllegalArgumentException("Bounds Rect cannot be null.");
        }
        this.bounds = new Rect(bounds);
        this.strokeColor = new ColorData(ColorData.BLACK);
        this.fillColor = new ColorData(ColorData.TRANSPARENT);
    }

    /*
     * Costruttore alternativo da coordinate.
     */
    public RectangleShape(Point2D topLeft, double width, double height) {
        this(new Rect(topLeft, width, height));
    }

    /*
     * Costruttore privato per clonazione completa.
     */
    private RectangleShape(UUID id, Rect bounds, ColorData stroke, ColorData fill) {
        this.id = id;
        this.bounds = new Rect(bounds);
        this.strokeColor = new ColorData(stroke);
        this.fillColor = new ColorData(fill);
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void move(Vector2D v) {
        if (v == null) return;
        bounds.translate(v.getDx(), v.getDy());
    }

    @Override
    public void resize(Rect newBounds) {
        if (newBounds == null) {
            throw new IllegalArgumentException("New bounds cannot be null for resize.");
        }
        this.bounds = new Rect(newBounds);
    }

    @Override
    public void setStrokeColor(ColorData c) {
        if (c == null) throw new IllegalArgumentException("Stroke color cannot be null.");
        this.strokeColor = new ColorData(c);
    }

    @Override
    public void setFillColor(ColorData c) {
        if (c == null) throw new IllegalArgumentException("Fill color cannot be null.");
        this.fillColor = new ColorData(c);
    }

    @Override
    public boolean contains(Point2D p) {
        if (p == null) return false;
        return bounds.contains(p);
    }

    @Override
    public void accept(ShapeVisitor v) {
        if (v != null) v.visit(this);
    }

    @Override
    public Shape cloneShape() {
        return new RectangleShape(this.id, this.bounds, this.strokeColor, this.fillColor);
    }

    @Override
    public Shape cloneWithNewId() {
        RectangleShape cloned = new RectangleShape(new Rect(this.bounds));
        cloned.setFillColor(new ColorData(this.fillColor));
        cloned.setStrokeColor(new ColorData(this.strokeColor));
        return cloned;
    }

    @Override
    public Rect getBounds() {
        return new Rect(this.bounds);
    }

    @Override
    public ColorData getStrokeColor() {
        return new ColorData(strokeColor);
    }

    @Override
    public ColorData getFillColor() {
        return new ColorData(fillColor);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RectangleShape)) return false;
        RectangleShape that = (RectangleShape) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "RectangleShape{" +
               "id=" + id +
               ", bounds=" + bounds +
               ", strokeColor=" + strokeColor +
               ", fillColor=" + fillColor +
               '}';
    }
}
