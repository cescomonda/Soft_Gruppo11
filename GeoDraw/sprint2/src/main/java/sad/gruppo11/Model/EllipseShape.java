package sad.gruppo11.Model;

import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Rect;
import sad.gruppo11.Model.geometry.Vector2D;
import sad.gruppo11.View.ShapeVisitor;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class EllipseShape implements Shape, Serializable {
    private final UUID id;
    private Rect bounds;
    private ColorData strokeColor;
    private ColorData fillColor;
    private double rotationAngle;

    public EllipseShape(Rect bounds, ColorData stroke, ColorData fill) {
        this.id = UUID.randomUUID();
        Objects.requireNonNull(bounds, "Bounds cannot be null for EllipseShape.");
        Objects.requireNonNull(stroke, "Stroke color cannot be null for EllipseShape.");
        Objects.requireNonNull(fill, "Fill color cannot be null for EllipseShape.");
        this.bounds = new Rect(bounds);
        this.strokeColor = new ColorData(stroke);
        this.fillColor = new ColorData(fill);
        this.rotationAngle = 0.0;
    }
    
    private EllipseShape(UUID id, Rect bounds, ColorData strokeColor, ColorData fillColor, double rotationAngle) {
        this.id = id;
        this.bounds = new Rect(bounds);
        this.strokeColor = new ColorData(strokeColor);
        this.fillColor = new ColorData(fillColor);
        this.rotationAngle = rotationAngle;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void move(Vector2D v) {
        Objects.requireNonNull(v, "Movement vector cannot be null.");
        this.bounds.translate(v);
    }

    @Override
    public void resize(Rect newBounds) {
        Objects.requireNonNull(newBounds, "New bounds cannot be null for resize.");
        this.bounds = new Rect(newBounds);
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
        Objects.requireNonNull(p, "Point p cannot be null for contains check.");
        Point2D center = bounds.getCenter();
        double dx = p.getX() - center.getX();
        double dy = p.getY() - center.getY();
        double rad = Math.toRadians(-rotationAngle);
        double cosRad = Math.cos(rad);
        double sinRad = Math.sin(rad);
        double rotatedX = dx * cosRad - dy * sinRad;
        double rotatedY = dx * sinRad + dy * cosRad;
        double a = bounds.getWidth() / 2.0;
        double b = bounds.getHeight() / 2.0;
        if (a <= 0 || b <= 0) return false;
        double term1 = (rotatedX * rotatedX) / (a * a);
        double term2 = (rotatedY * rotatedY) / (b * b);
        double epsilon = 1e-9;
        return (term1 + term2) <= 1.0 + epsilon;
    }

    @Override
    public void accept(ShapeVisitor v) {
        Objects.requireNonNull(v, "ShapeVisitor cannot be null.");
        v.visit(this);
    }

    @Override
    public Shape clone() {
        return new EllipseShape(this.id, this.bounds, this.strokeColor, this.fillColor, this.rotationAngle);
    }
    
    @Override
    public Shape cloneWithNewId() {
        EllipseShape newShape = new EllipseShape(new Rect(this.bounds), new ColorData(this.strokeColor), new ColorData(this.fillColor));
        newShape.setRotation(this.rotationAngle);
        return newShape;
    }

    @Override
    public Rect getBounds() {
        // TODO: Calculate AABB of rotated ellipse.
        return new Rect(this.bounds);
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
        EllipseShape that = (EllipseShape) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("EllipseShape{id=%s, bounds=%s, stroke=%s, fill=%s, rotation=%.1f}",
                id, bounds, strokeColor, fillColor, rotationAngle);
    }
}