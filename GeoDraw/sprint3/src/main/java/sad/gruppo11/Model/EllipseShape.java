
package sad.gruppo11.Model;

import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Rect;
import sad.gruppo11.Model.geometry.Vector2D;
import sad.gruppo11.View.ShapeVisitor;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class EllipseShape extends AbstractBaseShape implements Serializable {
    private final UUID id;
    private Rect bounds; // Il rettangolo di delimitazione in cui l'ellisse è inscritta (non ruotato)
    private ColorData strokeColor;
    private ColorData fillColor;
    private double rotationAngle; // In gradi

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
        // Algoritmo per punto in ellisse ruotata:
        // (x*cos(a) + y*sin(a))^2 / rx^2 + (x*sin(a) - y*cos(a))^2 / ry^2 <= 1
        // Dove (x,y) sono le coordinate del punto rispetto al centro dell'ellisse,
        // a è l'angolo di rotazione, rx e ry sono i semiassi.
        Point2D center = bounds.getCenter();
        double dx = p.getX() - center.getX(); // x rispetto al centro
        double dy = p.getY() - center.getY(); // y rispetto al centro

        double rad = Math.toRadians(-rotationAngle); // Angolo di rotazione inverso
        double cosRad = Math.cos(rad);
        double sinRad = Math.sin(rad);

        double rotatedX = dx * cosRad - dy * sinRad;
        double rotatedY = dx * sinRad + dy * cosRad;

        double rx = bounds.getWidth() / 2.0;
        double ry = bounds.getHeight() / 2.0;

        if (rx <= 0 || ry <= 0) return false; // Ellisse degenere non contiene punti

        double term1 = (rotatedX * rotatedX) / (rx * rx);
        double term2 = (rotatedY * rotatedY) / (ry * ry);
        
        // Aggiungi piccola tolleranza per problemi di precisione floating point
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
        return new Rect(this.bounds); // Bounds intrinseci non ruotati
    }

    @Override
    public void setRotation(double angle) {
        this.rotationAngle = angle % 360.0;
        if (this.rotationAngle < 0) this.rotationAngle += 360.0;
        this.rotationAngle = this.rotationAngle == -0.0 ? 0.0 : this.rotationAngle;
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

    // --- Metodi per Riflessione (US 27) ---
    @Override
    public void reflectHorizontal() {
        // L'ellisse è simmetrica. Per riflettere l'orientamento orizzontale
        // della forma visiva ruotata (come se l'asse Y del mondo fosse uno specchio):
        // Angolo alfa -> Angolo (180 - alfa)
        double currentRotation = getRotation();
        setRotation( (180.0 - currentRotation) % 360.0 );
    }

    @Override
    public void reflectVertical() {
        // Per riflettere l'orientamento verticale della forma visiva ruotata
        // (come se l'asse X del mondo fosse uno specchio):
        // Angolo alfa -> Angolo (-alfa)
        double currentRotation = getRotation();
        setRotation( -currentRotation );
    }

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

    @Override
    public Rect getRotatedBounds() {
        Rect unrotatedBounds = getBounds(); // this.bounds
        double angleDegrees = getRotation();

        if (angleDegrees == 0.0) {
            return new Rect(unrotatedBounds); // Copia difensiva
        }

        Point2D center = unrotatedBounds.getCenter();
        double rx = unrotatedBounds.getWidth() / 2.0;
        double ry = unrotatedBounds.getHeight() / 2.0;

        if (rx <= 0 || ry <= 0) { // Ellisse degenere
            return new Rect(center, 0, 0);
        }

        double angleRad = Math.toRadians(angleDegrees);
        double cosA = Math.cos(angleRad);
        double sinA = Math.sin(angleRad);

        // Calcola la larghezza e l'altezza dell'AABB dell'ellisse ruotata
        double aabbWidth = 2 * Math.sqrt(rx*rx * cosA*cosA + ry*ry * sinA*sinA);
        double aabbHeight = 2 * Math.sqrt(rx*rx * sinA*sinA + ry*ry * cosA*cosA);
        
        // L'AABB è centrato nello stesso punto dell'ellisse
        double minX = center.getX() - aabbWidth / 2.0;
        double minY = center.getY() - aabbHeight / 2.0;

        return new Rect(new Point2D(minX, minY), aabbWidth, aabbHeight);
    }
}
