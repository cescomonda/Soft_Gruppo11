package sad.gruppo11.Model;

import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Rect;
import sad.gruppo11.Model.geometry.Vector2D;
import sad.gruppo11.View.ShapeVisitor;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class LineSegment implements Shape, Serializable {
    private final UUID id;
    private Point2D startPoint;
    private Point2D endPoint;
    private ColorData strokeColor;
    private double rotationAngle;

    public LineSegment(Point2D p1, Point2D p2, ColorData stroke) {
        this.id = UUID.randomUUID();
        Objects.requireNonNull(p1, "Start point (p1) cannot be null for LineSegment.");
        Objects.requireNonNull(p2, "End point (p2) cannot be null for LineSegment.");
        Objects.requireNonNull(stroke, "Stroke color cannot be null for LineSegment.");
        this.startPoint = new Point2D(p1);
        this.endPoint = new Point2D(p2);
        this.strokeColor = new ColorData(stroke);
        this.rotationAngle = 0.0;
    }

    private LineSegment(UUID id, Point2D startPoint, Point2D endPoint, ColorData strokeColor, double rotationAngle) {
        this.id = id;
        this.startPoint = new Point2D(startPoint);
        this.endPoint = new Point2D(endPoint);
        this.strokeColor = new ColorData(strokeColor);
        this.rotationAngle = rotationAngle;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void move(Vector2D v) {
        Objects.requireNonNull(v, "Movement vector cannot be null.");
        this.startPoint.translate(v);
        this.endPoint.translate(v);
    }

    @Override
    public void resize(Rect newBounds) {
        Objects.requireNonNull(newBounds, "New bounds cannot be null for resize.");
        this.startPoint = new Point2D(newBounds.getTopLeft());
        this.endPoint = new Point2D(newBounds.getBottomRight());
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
        // No-op
    }

    @Override
    public ColorData getFillColor() {
        return ColorData.TRANSPARENT;
    }

    @Override
    public boolean contains(Point2D p) {
        Objects.requireNonNull(p, "Point p cannot be null for contains check.");

        if (this.rotationAngle == 0.0) { // Se non c'è rotazione, usa la logica semplice
            double distToStart = startPoint.distance(p);
            double distToEnd = endPoint.distance(p);
            double segmentLength = startPoint.distance(endPoint);
            double epsilon = 2.0; // Tolleranza per il click (in coordinate mondo)
            if (segmentLength < 1e-3) { // Praticamente un punto
                return startPoint.distance(p) < epsilon;
            }
            // Verifica se la somma delle distanze da p agli estremi è vicina alla lunghezza del segmento
            return Math.abs((distToStart + distToEnd) - segmentLength) < epsilon;
        } else {
            // Logica per linea ruotata:
            // 1. Trova il centro di rotazione (punto medio della linea originale)
            Point2D center = new Point2D(
                (startPoint.getX() + endPoint.getX()) / 2.0,
                (startPoint.getY() + endPoint.getY()) / 2.0
            );

            // 2. Trasla il punto p in modo che il centro di rotazione sia all'origine
            double translatedPx = p.getX() - center.getX();
            double translatedPy = p.getY() - center.getY();

            // 3. Applica la rotazione inversa al punto p traslato
            // L'angolo di rotazione della forma è this.rotationAngle
            // La rotazione inversa è -this.rotationAngle
            double angleRad = Math.toRadians(-this.rotationAngle);
            double cosAngle = Math.cos(angleRad);
            double sinAngle = Math.sin(angleRad);

            double rotatedPx = translatedPx * cosAngle - translatedPy * sinAngle;
            double rotatedPy = translatedPx * sinAngle + translatedPy * cosAngle;

            // 4. Ora abbiamo le coordinate del punto p come se la linea NON fosse ruotata
            //    e fosse centrata all'origine (se il suo centro originale era l'origine).
            //    Dobbiamo confrontare (rotatedPx, rotatedPy) con gli estremi della linea
            //    come se fossero anch'essi traslati e centrati.

            // Estremi della linea originale traslati come se il centro fosse (0,0)
            double localStartX = startPoint.getX() - center.getX();
            double localStartY = startPoint.getY() - center.getY();
            double localEndX = endPoint.getX() - center.getX();
            double localEndY = endPoint.getY() - center.getY();

            // Ora (rotatedPx, rotatedPy) è il punto di click nello spazio locale (non ruotato) della linea.
            // (localStartX, localStartY) e (localEndX, localEndY) sono gli estremi della linea in questo spazio.
            Point2D transformedP = new Point2D(rotatedPx, rotatedPy);
            Point2D localStart = new Point2D(localStartX, localStartY);
            Point2D localEnd = new Point2D(localEndX, localEndY);
            
            double distToLocalStart = localStart.distance(transformedP);
            double distToLocalEnd = localEnd.distance(transformedP);
            double localSegmentLength = localStart.distance(localEnd);
            
            double epsilon = 2.0; // Tolleranza per il click

            if (localSegmentLength < 1e-3) { // Praticamente un punto
                return localStart.distance(transformedP) < epsilon;
            }
            return Math.abs((distToLocalStart + distToLocalEnd) - localSegmentLength) < epsilon;
        }
    }  
      
    @Override
    public void accept(ShapeVisitor v) {
        Objects.requireNonNull(v, "ShapeVisitor cannot be null.");
        v.visit(this);
    }

    @Override
    public Shape clone() {
        return new LineSegment(this.id, this.startPoint, this.endPoint, this.strokeColor, this.rotationAngle);
    }
    
    @Override
    public Shape cloneWithNewId() {
        LineSegment newShape = new LineSegment(new Point2D(this.startPoint), new Point2D(this.endPoint), new ColorData(this.strokeColor));
        newShape.setRotation(this.rotationAngle); // Copy rotation
        return newShape;
    }

    @Override
    public Rect getBounds() {
        // TODO: Calculate AABB of rotated line segment.
        double minX = Math.min(startPoint.getX(), endPoint.getX());
        double minY = Math.min(startPoint.getY(), endPoint.getY());
        double maxX = Math.max(startPoint.getX(), endPoint.getX());
        double maxY = Math.max(startPoint.getY(), endPoint.getY());
        return new Rect(new Point2D(minX, minY), maxX - minX, maxY - minY);
    }

    public Point2D getStartPoint() {
        return new Point2D(startPoint);
    }

    public Point2D getEndPoint() {
        return new Point2D(endPoint);
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
        LineSegment that = (LineSegment) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("LineSegment{id=%s, start=%s, end=%s, stroke=%s, rotation=%.1f}",
                id, startPoint, endPoint, strokeColor, rotationAngle);
    }
}