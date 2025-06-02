
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

public class PolygonShape extends AbstractBaseShape implements Serializable {
    private final UUID id;
    private List<Point2D> vertices; // Vertici nello spazio del modello (non ruotati)
    private ColorData strokeColor;
    private ColorData fillColor;
    private double rotationAngle;

    public PolygonShape(List<Point2D> vertices, ColorData stroke, ColorData fill) {
        this.id = UUID.randomUUID();
        Objects.requireNonNull(vertices, "Vertices list cannot be null for PolygonShape.");
        if (vertices.size() < 3) {
            throw new IllegalArgumentException("PolygonShape must have at least 3 vertices.");
        }
        // Crea copie difensive dei vertici
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
        // Restituisce una copia difensiva per evitare modifiche esterne
        return this.vertices.stream().map(Point2D::new).collect(Collectors.toList());
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
        Rect oldBounds = getBounds(); // Bounds dei vertici attuali (non ruotati)

        if (oldBounds.getWidth() == 0 || oldBounds.getHeight() == 0) {
             // Se il poligono è degenere (es. tutti i punti collineari o coincidenti),
             // il ridimensionamento proporzionale è problematico.
             // Potremmo spostare tutti i vertici al topLeft dei newBounds,
             // ma questo collasserebbe il poligono.
             // Per ora, ignoriamo il resize di poligoni degeneri per evitare divisioni per zero.
            System.err.println("Cannot resize a degenerate polygon (zero width or height in its current bounds).");
            return;
        }

        double scaleX = newBounds.getWidth() / oldBounds.getWidth();
        double scaleY = newBounds.getHeight() / oldBounds.getHeight();

        // Punto di riferimento per la scala (es. il topLeft del vecchio bounding box)
        Point2D referencePoint = oldBounds.getTopLeft();
        Point2D newReferencePoint = newBounds.getTopLeft();

        List<Point2D> newVertices = new ArrayList<>();
        for (Point2D vertex : this.vertices) {
            // Vettore dal vertice al punto di riferimento vecchio
            double dx = vertex.getX() - referencePoint.getX();
            double dy = vertex.getY() - referencePoint.getY();

            // Scala il vettore
            double scaledDx = dx * scaleX;
            double scaledDy = dy * scaleY;

            // Aggiungi il vettore scalato al nuovo punto di riferimento
            newVertices.add(new Point2D(newReferencePoint.getX() + scaledDx, newReferencePoint.getY() + scaledDy));
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
        Objects.requireNonNull(p, "Point cannot be null for contains check.");

        // Trasforma il punto p nello spazio locale (non ruotato) del poligono
        Point2D center = getBounds().getCenter(); // Pivot di rotazione (centro del AABB non ruotato)
        double angleRadInverse = Math.toRadians(-this.rotationAngle);
        double cosA = Math.cos(angleRadInverse);
        double sinA = Math.sin(angleRadInverse);

        double translatedPx = p.getX() - center.getX();
        double translatedPy = p.getY() - center.getY();
        
        // Applica rotazione inversa al punto traslato per portarlo nello spazio non ruotato del poligono
        // ma mantenendo il centro del AABB come origine temporanea.
        double localRelativePx = translatedPx * cosA - translatedPy * sinA;
        double localRelativePy = translatedPx * sinA + translatedPy * cosA;

        // Ritrasla sommando le coordinate del centro per avere il punto nello stesso sistema dei vertici.
        Point2D localPoint = new Point2D(localRelativePx + center.getX(), localRelativePy + center.getY());

        // Algoritmo Ray Casting (even-odd rule) sui vertici originali (non ruotati)
        int crossings = 0;
        int n = vertices.size();
        for (int i = 0; i < n; i++) {
            Point2D v1 = vertices.get(i);
            Point2D v2 = vertices.get((i + 1) % n);

            // Controlla se il raggio orizzontale dal punto interseca il lato (v1, v2)
            if (((v1.getY() <= localPoint.getY() && v2.getY() > localPoint.getY()) ||
                 (v1.getY() > localPoint.getY() && v2.getY() <= localPoint.getY())) &&
                (localPoint.getX() < (v2.getX() - v1.getX()) * (localPoint.getY() - v1.getY()) / (v2.getY() - v1.getY()) + v1.getX())) {
                crossings++;
            }
        }
        return (crossings % 2) == 1; // Se il numero di incroci è dispari, il punto è dentro
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
        if (vertices.isEmpty()) {
            return new Rect(0, 0, 0, 0);
        }
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
        Point2D center = getBounds().getCenter(); // Centro del bounding box NON ruotato
        double cX = center.getX();
        
        List<Point2D> newVertices = new ArrayList<>();
        for (Point2D vertex : this.vertices) {
            newVertices.add(new Point2D(2 * cX - vertex.getX(), vertex.getY()));
        }
        // L'ordine dei vertici deve essere invertito per mantenere l'orientamento corretto (es. winding order)
        // se la riflessione è considerata una trasformazione che inverte l'orientamento.
        // Per un poligono riempito, questo potrebbe non essere visivamente ovvio, ma per lo stroke
        // o algoritmi basati sull'ordine, è importante.
        // Se l'ordine non viene invertito, la forma potrebbe apparire "inside-out".
        // Tuttavia, la semplice riflessione delle coordinate dei vertici è spesso sufficiente per la visualizzazione.
        // Per una riflessione geometrica pura, l'ordine dei vertici dovrebbe essere invertito.
        // Esempio: A,B,C -> C',B',A' (dove ' indica il vertice riflesso)
        // Per semplicità, qui riflettiamo solo le coordinate e non invertiamo l'ordine.
        // Se si notano problemi (es. con il fill), si dovrà considerare l'inversione dell'ordine.
        this.vertices = newVertices;
    }

    @Override
    public void reflectVertical() {
        Point2D center = getBounds().getCenter(); // Centro del bounding box NON ruotato
        double cY = center.getY();

        List<Point2D> newVertices = new ArrayList<>();
        for (Point2D vertex : this.vertices) {
            newVertices.add(new Point2D(vertex.getX(), 2 * cY - vertex.getY()));
        }
        // Anche qui, considerare l'inversione dell'ordine dei vertici se necessario.
        this.vertices = newVertices;
    }

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

    private Point2D rotatePoint(Point2D point, Point2D pivot, double angleDegrees) { // Copia helper
        double angleRad = Math.toRadians(angleDegrees);
        double cosA = Math.cos(angleRad);
        double sinA = Math.sin(angleRad);
        double dx = point.getX() - pivot.getX();
        double dy = point.getY() - pivot.getY();
        double newX = pivot.getX() + (dx * cosA - dy * sinA);
        double newY = pivot.getY() + (dx * sinA + dy * cosA);
        return new Point2D(newX, newY);
    }

    @Override
    public Rect getRotatedBounds() {
        List<Point2D> currentVertices = getVertices(); // Ottiene una copia dei vertici
        double angle = getRotation();

        if (currentVertices.isEmpty()) {
            return new Rect(0,0,0,0);
        }
        if (angle == 0.0) {
            return getBounds(); // Usa il calcolo AABB esistente per vertici non ruotati
        }

        Point2D center = getBounds().getCenter(); // Pivot di rotazione (centro dell'AABB non ruotato)
        
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        for (Point2D vertex : currentVertices) {
            Point2D rotatedVertex = rotatePoint(vertex, center, angle);
            minX = Math.min(minX, rotatedVertex.getX());
            minY = Math.min(minY, rotatedVertex.getY());
            maxX = Math.max(maxX, rotatedVertex.getX());
            maxY = Math.max(maxY, rotatedVertex.getY());
        }
        
        if (minX > maxX) { // Caso degenere, nessun punto o punti collineari
            return new Rect(new Point2D(0,0),0,0); // O il centro con dimensioni 0
        }

        return new Rect(new Point2D(minX, minY), maxX - minX, maxY - minY);
    }
}
