package sad.gruppo11.Model;

import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Rect;
import sad.gruppo11.Model.geometry.Vector2D;
import sad.gruppo11.View.ShapeVisitor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class PolygonShape implements Shape, Serializable {
    private final UUID id;
    private List<Point2D> vertices;
    private ColorData strokeColor;
    private ColorData fillColor;
    private double rotationAngle;

    public PolygonShape(List<Point2D> vertices, ColorData stroke, ColorData fill) {
        this.id = UUID.randomUUID();
        Objects.requireNonNull(vertices, "Vertices list cannot be null for PolygonShape.");
        if (vertices.size() < 3) {
            throw new IllegalArgumentException("PolygonShape must have at least 3 vertices.");
        }
        this.vertices = vertices.stream().map(Point2D::new).collect(Collectors.toList());
        Objects.requireNonNull(stroke, "Stroke color cannot be null for PolygonShape.");
        Objects.requireNonNull(fill, "Fill color cannot be null for PolygonShape.");
        this.strokeColor = new ColorData(stroke);
        this.fillColor = new ColorData(fill);
        this.rotationAngle = 0.0;
    }

    private PolygonShape(UUID id, List<Point2D> vertices, ColorData strokeColor, ColorData fillColor, double rotationAngle) {
        this.id = id;
        this.vertices = vertices.stream().map(Point2D::new).collect(Collectors.toList());
        this.strokeColor = new ColorData(strokeColor);
        this.fillColor = new ColorData(fillColor);
        this.rotationAngle = rotationAngle;
    }

    public List<Point2D> getVertices() {
        return vertices.stream().map(Point2D::new).collect(Collectors.toList());
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void move(Vector2D v) {
        Objects.requireNonNull(v, "Movement vector cannot be null.");
        for (Point2D vertex : this.vertices) {
            vertex.translate(v);
        }
    }

    @Override
    public void resize(Rect newBounds) {
        Objects.requireNonNull(newBounds, "New bounds cannot be null for resize.");

        // 1. Bounding box attuale (locale, non ruotato)
        Rect oldBounds = getBounds();
        double oldW = oldBounds.getWidth();
        double oldH = oldBounds.getHeight();

        if (oldW == 0 || oldH == 0) {
            throw new IllegalStateException("Cannot resize a degenerate polygon (zero width/height).");
        }

        // 2. Fattori di scala
        double sx = newBounds.getWidth()  / oldW;
        double sy = newBounds.getHeight() / oldH;

        // 3. Trasforma tutti i vertici
        List<Point2D> newVertices = new ArrayList<>(vertices.size());
        for (Point2D v : vertices) {
            double newX = newBounds.getX() + (v.getX() - oldBounds.getX()) * sx;
            double newY = newBounds.getY() + (v.getY() - oldBounds.getY()) * sy;
            newVertices.add(new Point2D(newX, newY));
        }

        this.vertices = newVertices;
    }


    @Override
    public void setStrokeColor(ColorData c) {
        Objects.requireNonNull(c, "Stroke color cannot be null.");
        this.strokeColor = new ColorData(c);
    }

    @Override
    public ColorData getStrokeColor() {
        return new ColorData(this.strokeColor);
    }

    @Override
    public void setFillColor(ColorData c) {
        Objects.requireNonNull(c, "Fill color cannot be null.");
        this.fillColor = new ColorData(c);
    }

    @Override
    public ColorData getFillColor() {
        return new ColorData(this.fillColor);
    }

    @Override
    public boolean contains(Point2D p) {
        Objects.requireNonNull(p, "Point cannot be null");

        // 1. Porta il punto nel sistema locale NON ruotato
        Point2D q = toLocal(p);               // <-- adesso lo facciamo SUBITO

        // 2. Early-out sul bounding box
        Rect aabb = getBounds();         // calcolato sui vertici originali
        if (!aabb.contains(q)) {
            return false;
        }

        // 3. Ray-casting (even-odd rule)
        int crossings = 0;
        for (int i = 0, n = vertices.size(); i < n; i++) {
            Point2D v1 = vertices.get(i);
            Point2D v2 = vertices.get((i + 1) % n);

            boolean diffSide = (v1.getY() > q.getY()) != (v2.getY() > q.getY());
            if (diffSide) {
                double xInt = v1.getX() + (q.getY() - v1.getY()) * (v2.getX() - v1.getX()) / (v2.getY() - v1.getY());
                if (q.getX() < xInt) crossings++;
            }
        }
        return (crossings & 1) == 1;
    }

    /** Converte un punto del mondo nel sistema locale non ruotato. */
    private Point2D toLocal(Point2D p) {
        double rad = Math.toRadians(-rotationAngle);      // inverso
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);

        Point2D c = getRotationPivot();                   // usa LO STESSO pivot del rendering!

        double dx = p.getX() - c.getX();
        double dy = p.getY() - c.getY();

        return new Point2D(
                cos * dx - sin * dy + c.getX(),
                sin * dx + cos * dy + c.getY()
        );
    }

    /** Pivot di rotazione: adegua questo metodo al tuo renderer. */
    private Point2D getRotationPivot() {
        // se ruoti attorno al centro dell'AABB puoi fare:
        Rect b = getBounds();
        return new Point2D(b.getX() + b.getWidth() / 2.0, b.getY() + b.getHeight() / 2.0);

        // se invece ruoti attorno al centroide geometrico:
        // return getCentroid();
    }

    @Override
    public void accept(ShapeVisitor v) {
        Objects.requireNonNull(v, "ShapeVisitor cannot be null.");
        v.visit(this);
    }

    @Override
    public Shape clone() {
        return new PolygonShape(this.id, new ArrayList<>(this.vertices), new ColorData(this.strokeColor), new ColorData(this.fillColor), this.rotationAngle);
    }

    @Override
    public Shape cloneWithNewId() {
        PolygonShape newShape = new PolygonShape(new ArrayList<>(this.vertices), new ColorData(this.strokeColor), new ColorData(this.fillColor));
        newShape.setRotation(this.rotationAngle);
        return newShape;
    }

    @Override
    public Rect getBounds() {
        double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
        for (Point2D v : vertices) {
            minX = Math.min(minX, v.getX());
            minY = Math.min(minY, v.getY());
            maxX = Math.max(maxX, v.getX());
            maxY = Math.max(maxY, v.getY());
        }
        return new Rect(new Point2D(minX, minY), maxX - minX, maxY - minY);
    }

    @Override
    public void setRotation(double angle) {
        this.rotationAngle = angle % 360.0;
    }

    @Override
    public double getRotation() {
        return this.rotationAngle;
    }

    @Override
    public void setText(String text) {}
    @Override
    public String getText() { return null; }
    @Override
    public void setFontSize(double size) {}
    @Override
    public double getFontSize() { return 0; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PolygonShape that = (PolygonShape) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("PolygonShape{id=%s, vertices=%d, stroke=%s, fill=%s, rotation=%.1f}",
                id, vertices.size(), strokeColor, fillColor, rotationAngle);
    }
}