
package sad.gruppo11.Model;

import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Rect;
import sad.gruppo11.Model.geometry.Vector2D;
import sad.gruppo11.View.ShapeVisitor;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class LineSegment extends AbstractBaseShape implements Serializable { // Estende AbstractBaseShape
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
        // Se la linea è ruotata, il move semplice dei suoi estremi originali è corretto
        // perché la rotazione è applicata al momento del rendering attorno al centro attuale.
        this.startPoint.translate(v);
        this.endPoint.translate(v);
    }

    @Override
    public void resize(Rect newBounds) {
        Objects.requireNonNull(newBounds, "New bounds cannot be null for resize.");
        // Per una linea, il resize basato su un Rect implica che la linea diventi
        // la diagonale di quel rettangolo (o si adatti ai suoi estremi).
        // Se la linea è ruotata, questo diventa complesso.
        // Per semplicità, assumiamo che newBounds definisca i nuovi start/end point
        // nello spazio non ruotato. La rotazione sarà poi applicata.
        // Questo disaccoppia il "contenuto" della forma (i suoi punti) dalla sua rotazione.

        Point2D newStart = new Point2D(newBounds.getX(), newBounds.getY());
        Point2D newEnd = new Point2D(newBounds.getRight(), newBounds.getBottom());
        
        // Per mantenere l'orientamento originale, potremmo voler scalare i punti
        // rispetto al centro, ma questo renderebbe 'newBounds' meno diretto.
        // L'approccio attuale è che newBounds *ridefinisce* la linea nello spazio non ruotato.
        this.startPoint = newStart;
        this.endPoint = newEnd;
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
        // No-op per LineSegment
    }

    @Override
    public ColorData getFillColor() {
        return ColorData.TRANSPARENT;
    }

    @Override
    public boolean contains(Point2D p) {
        Objects.requireNonNull(p, "Point p cannot be null for contains check.");

        // Trasforma il punto p nello spazio locale (non ruotato) della linea
        Point2D center = getBounds().getCenter(); // Pivot di rotazione
        double angleRadInverse = Math.toRadians(-this.rotationAngle);
        double cosA = Math.cos(angleRadInverse);
        double sinA = Math.sin(angleRadInverse);

        double translatedPx = p.getX() - center.getX();
        double translatedPy = p.getY() - center.getY();

        double localPx = translatedPx * cosA - translatedPy * sinA + center.getX();
        double localPy = translatedPx * sinA + translatedPy * cosA + center.getY();
        Point2D localPoint = new Point2D(localPx, localPy);
        
        // Ora esegui il test di contenimento sulla linea originale (startPoint, endPoint)
        // con il punto trasformato localPoint.
        double distToStart = startPoint.distance(localPoint);
        double distToEnd = endPoint.distance(localPoint);
        double segmentLength = startPoint.distance(endPoint);
        
        double epsilon = 3.0; // Tolleranza per il click (in coordinate mondo, più generosa)
                              // Questa tolleranza dovrebbe essere idealmente scalata con lo zoom per la UI.
        
        if (segmentLength < 1e-3) { // Praticamente un punto
            return startPoint.distance(localPoint) < epsilon;
        }
        // Verifica se la somma delle distanze da p agli estremi è vicina alla lunghezza del segmento
        return Math.abs((distToStart + distToEnd) - segmentLength) < epsilon;
    }  
      
    @Override
    public void accept(ShapeVisitor v) {
        Objects.requireNonNull(v, "ShapeVisitor cannot be null.");
        v.visit(this);
    }

    @Override
    public Shape clone() { // Usato da Clipboard
        return new LineSegment(this.id, this.startPoint, this.endPoint, this.strokeColor, this.rotationAngle);
    }
    
    @Override
    public Shape cloneWithNewId() { // Usato da PasteCommand
        LineSegment newShape = new LineSegment(new Point2D(this.startPoint), new Point2D(this.endPoint), new ColorData(this.strokeColor));
        newShape.setRotation(this.rotationAngle);
        return newShape;
    }

    @Override
    public Rect getBounds() {
        // Questo è il bounding box NON ruotato dei punti start/end originali.
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
        if (this.rotationAngle < 0) this.rotationAngle += 360.0;
        this.rotationAngle = this.rotationAngle == -0.0 ? 0.0 : this.rotationAngle;
    }

    @Override
    public double getRotation() {
        return this.rotationAngle;
    }

    @Override
    public void setText(String text) { /* No-op */ }
    @Override
    public String getText() { return null; }
    @Override
    public void setFontSize(double size) { /* No-op */ }
    @Override
    public double getFontSize() { return 0; }

    // --- Metodi per Riflessione (US 27) ---
    @Override
    public void reflectHorizontal() {
        Point2D center = getBounds().getCenter(); // Centro del bounding box NON ruotato
        
        // Rifletti i punti start e end rispetto all'asse verticale passante per center.getX()
        // Le coordinate y rimangono invariate.
        // Le coordinate x diventano: centerX - (originalX - centerX) = 2*centerX - originalX
        
        double cX = center.getX();
        
        Point2D newStart = new Point2D(2 * cX - startPoint.getX(), startPoint.getY());
        Point2D newEnd = new Point2D(2 * cX - endPoint.getX(), endPoint.getY());
        
        this.startPoint = newStart;
        this.endPoint = newEnd;
    }

    @Override
    public void reflectVertical() {
        Point2D center = getBounds().getCenter(); // Centro del bounding box NON ruotato
        
        // Rifletti i punti start e end rispetto all'asse orizzontale passante per center.getY()
        // Le coordinate x rimangono invariate.
        // Le coordinate y diventano: centerY - (originalY - centerY) = 2*centerY - originalY
        
        double cY = center.getY();
        
        Point2D newStart = new Point2D(startPoint.getX(), 2 * cY - startPoint.getY());
        Point2D newEnd = new Point2D(endPoint.getX(), 2 * cY - endPoint.getY());
        
        this.startPoint = newStart;
        this.endPoint = newEnd;
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
        return String.format("LineSegment{id=%s, start=%s, end=%s, stroke=%s, rotation=%.1f}",
                id, startPoint, endPoint, strokeColor, rotationAngle);
    }

    @Override
    public Rect getRotatedBounds() {
        Point2D p1 = getStartPoint();
        Point2D p2 = getEndPoint();
        double angle = getRotation();

        if (angle == 0.0) {
            return getBounds(); // Nessuna rotazione, l'AABB è il bounds non ruotato
        }

        Point2D center = getBounds().getCenter(); // Pivot di rotazione

        // Ruota i punti di inizio e fine attorno al centro
        Point2D rotatedP1 = rotatePoint(p1, center, angle);
        Point2D rotatedP2 = rotatePoint(p2, center, angle);

        // Calcola il nuovo AABB dai punti ruotati
        double minX = Math.min(rotatedP1.getX(), rotatedP2.getX());
        double minY = Math.min(rotatedP1.getY(), rotatedP2.getY());
        double maxX = Math.max(rotatedP1.getX(), rotatedP2.getX());
        double maxY = Math.max(rotatedP1.getY(), rotatedP2.getY());

        return new Rect(new Point2D(minX, minY), maxX - minX, maxY - minY);
    }

    /**
     * Metodo helper per ruotare un punto attorno a un pivot.
     * @param point Il punto da ruotare.
     * @param pivot Il punto attorno al quale ruotare.
     * @param angleDegrees L'angolo di rotazione in gradi.
     * @return Il nuovo punto ruotato.
     */
    private Point2D rotatePoint(Point2D point, Point2D pivot, double angleDegrees) {
        double angleRad = Math.toRadians(angleDegrees);
        double cosA = Math.cos(angleRad);
        double sinA = Math.sin(angleRad);

        double dx = point.getX() - pivot.getX();
        double dy = point.getY() - pivot.getY();

        double newX = pivot.getX() + (dx * cosA - dy * sinA);
        double newY = pivot.getY() + (dx * sinA + dy * cosA);
        return new Point2D(newX, newY);
    }
}
