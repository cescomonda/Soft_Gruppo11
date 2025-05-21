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
 * Rappresenta un segmento di linea definito da due punti.
 * Supporta clonazione, movimento, ridimensionamento, controllo contenimento
 * e pattern Visitor. La linea ha solo strokeColor (nessun fill).
 */
public class LineSegment implements Shape, Serializable {

    /* Identificativo univoco */
    private final UUID id;

    /* Punti estremi della linea */
    private Point2D startPoint;
    private Point2D endPoint;

    /* Colore del bordo */
    private ColorData strokeColor;

    /*
     * Costruttore principale.
     */
    public LineSegment(Point2D startPoint, Point2D endPoint) {
        this.id = UUID.randomUUID();
        if (startPoint == null || endPoint == null) {
            throw new IllegalArgumentException("Start and end points cannot be null.");
        }
        this.startPoint = new Point2D(startPoint);
        this.endPoint = new Point2D(endPoint);
        this.strokeColor = new ColorData(ColorData.BLACK);
    }

    /*
     * Costruttore privato per clone con ID specifico.
     */
    private LineSegment(UUID id, Point2D start, Point2D end, ColorData stroke) {
        this.id = id;
        this.startPoint = new Point2D(start);
        this.endPoint = new Point2D(end);
        this.strokeColor = new ColorData(stroke);
    }

    /*
     * Costruttore privato per clone con nuovo ID.
     */
    private LineSegment(Point2D start, Point2D end, ColorData stroke) {
        this.id = UUID.randomUUID();
        this.startPoint = new Point2D(start);
        this.endPoint = new Point2D(end);
        this.strokeColor = new ColorData(stroke);
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void move(Vector2D v) {
        if (v == null) return;
        startPoint.translate(v.getDx(), v.getDy());
        endPoint.translate(v.getDx(), v.getDy());
    }

    @Override
    public void resize(Rect newBounds) {
        if (newBounds == null) {
            throw new IllegalArgumentException("New bounds cannot be null for resize.");
        }
        this.startPoint = new Point2D(newBounds.getTopLeft());
        this.endPoint = new Point2D(newBounds.getBottomRight());
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
        // Non applicabile a LineSegment
    }

    @Override
    public ColorData getFillColor() {
        // Non applicabile a LineSegment
        return null;
    }

    @Override
    public boolean contains(Point2D p) {
        if (p == null) return false;

        double epsilon = 0.5;
        double distToStart = startPoint.distance(p);
        double distToEnd = endPoint.distance(p);
        double segmentLength = startPoint.distance(endPoint);

        return Math.abs((distToStart + distToEnd) - segmentLength) < epsilon;
    }

    @Override
    public void accept(ShapeVisitor v) {
        if (v != null) v.visit(this);
    }

    @Override
    public Shape cloneShape() {
        return new LineSegment(this.id, this.startPoint, this.endPoint, this.strokeColor);
    }

    @Override
    public Shape cloneWithNewId() {
        return new LineSegment(this.startPoint, this.endPoint, this.strokeColor);
    }

    @Override
    public Rect getBounds() {
        double minX = Math.min(startPoint.getX(), endPoint.getX());
        double minY = Math.min(startPoint.getY(), endPoint.getY());
        double maxX = Math.max(startPoint.getX(), endPoint.getX());
        double maxY = Math.max(startPoint.getY(), endPoint.getY());
        return new Rect(new Point2D(minX, minY), maxX - minX, maxY - minY);
    }

    @Override
    public ColorData getStrokeColor() {
        return new ColorData(strokeColor);
    }

    /* --- Getters specifici per la Linea --- */
    public Point2D getStartPoint() {
        return new Point2D(startPoint);
    }

    public Point2D getEndPoint() {
        return new Point2D(endPoint);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LineSegment that = (LineSegment) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "LineSegment{" +
               "id=" + id +
               ", startPoint=" + startPoint +
               ", endPoint=" + endPoint +
               ", strokeColor=" + strokeColor +
               '}';
    }
}
