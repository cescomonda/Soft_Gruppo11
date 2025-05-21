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
 * Rappresenta una forma ellittica inscritta in un rettangolo.
 * Implementa l'interfaccia Shape e supporta clonazione, movimento, resize,
 * selezione (contains), Visitor pattern e serializzazione.
 */
public class EllipseShape implements Shape, Serializable {

    /* Identificatore univoco della forma */
    private final UUID id;

    /* Rettangolo che delimita l’ellisse */
    private Rect bounds;

    /* Colore del bordo */
    private ColorData strokeColor;

    /* Colore di riempimento */
    private ColorData fillColor;

    /*
     * Costruttore principale con bounding box.
     */
    public EllipseShape(Rect bounds) {
        this.id = UUID.randomUUID();
        if (bounds == null) {
            throw new IllegalArgumentException("Bounds Rect cannot be null.");
        }
        this.bounds = new Rect(bounds);
        this.strokeColor = new ColorData(ColorData.BLACK);
        this.fillColor = new ColorData(ColorData.TRANSPARENT);
    }

    /*
     * Costruttore alternativo con centro e raggi orizzontale/verticale.
     */
    public EllipseShape(Point2D center, double radiusX, double radiusY) {
        this.id = UUID.randomUUID();
        if (center == null) throw new IllegalArgumentException("Center point cannot be null.");
        if (radiusX < 0 || radiusY < 0) throw new IllegalArgumentException("Radii cannot be negative.");
        this.bounds = new Rect(
            new Point2D(center.getX() - radiusX, center.getY() - radiusY),
            radiusX * 2,
            radiusY * 2
        );
        this.strokeColor = new ColorData(ColorData.BLACK);
        this.fillColor = new ColorData(ColorData.TRANSPARENT);
    }

    /*
     * Costruttore privato per clonazione.
     */
    private EllipseShape(UUID id, Rect bounds, ColorData stroke, ColorData fill) {
        this.id = id;
        this.bounds = new Rect(bounds);
        this.strokeColor = new ColorData(stroke);
        this.fillColor = new ColorData(fill);
    }

    /* --- Metodi dell'interfaccia Shape --- */

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void move(Vector2D v) {
        if (v == null) return;
        this.bounds.translate(v.getDx(), v.getDy());
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
        if (c == null) {
            throw new IllegalArgumentException("Stroke color cannot be null.");
        }
        this.strokeColor = new ColorData(c);
    }

    @Override
    public void setFillColor(ColorData c) {
        if (c == null) {
            throw new IllegalArgumentException("Fill color cannot be null.");
        }
        this.fillColor = new ColorData(c);
    }

    @Override
    public boolean contains(Point2D p) {
        if (p == null) return false;

        /* Verifica se il punto è dentro l'ellisse usando l'equazione standard */
        Point2D center = bounds.getCenter();
        double radiusX = bounds.getWidth() / 2.0;
        double radiusY = bounds.getHeight() / 2.0;

        if (radiusX <= 0 || radiusY <= 0) return false;

        double term1 = Math.pow((p.getX() - center.getX()) / radiusX, 2);
        double term2 = Math.pow((p.getY() - center.getY()) / radiusY, 2);

        return term1 + term2 <= 1.0;
    }

    @Override
    public void accept(ShapeVisitor v) {
        if (v == null) return;
        v.visit(this);
    }

    @Override
    public Shape cloneShape() {
        return new EllipseShape(this.id, this.bounds, this.strokeColor, this.fillColor);
    }

    @Override
    public Rect getBounds() {
        return new Rect(this.bounds);
    }

    @Override
    public ColorData getStrokeColor() {
        return new ColorData(strokeColor);
    }

    public ColorData getFillColor() {
        return new ColorData(fillColor);
    }

    @Override
    public Shape cloneWithNewId() {
        EllipseShape cloned = new EllipseShape(new Rect(this.bounds));
        cloned.setFillColor(new ColorData(this.fillColor));
        cloned.setStrokeColor(new ColorData(this.strokeColor));
        return cloned;
    }

    /* --- Equals, HashCode, ToString --- */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EllipseShape that = (EllipseShape) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "EllipseShape{" +
               "id=" + id +
               ", bounds=" + bounds +
               ", strokeColor=" + strokeColor +
               ", fillColor=" + fillColor +
               '}';
    }
}
