
package sad.gruppo11.Model;

import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Rect;
import sad.gruppo11.Model.geometry.Vector2D;
import sad.gruppo11.View.ShapeVisitor;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class RectangleShape extends AbstractBaseShape implements Serializable {
    private final UUID id;
    private Rect bounds; // Rappresenta il rettangolo non ruotato
    private ColorData strokeColor;
    private ColorData fillColor;
    private double rotationAngle; // In gradi

    public RectangleShape(Rect bounds, ColorData stroke, ColorData fill) {
        this.id = UUID.randomUUID();
        Objects.requireNonNull(bounds, "Bounds cannot be null for RectangleShape.");
        Objects.requireNonNull(stroke, "Stroke color cannot be null for RectangleShape.");
        Objects.requireNonNull(fill, "Fill color cannot be null for RectangleShape.");
        this.bounds = new Rect(bounds);
        this.strokeColor = new ColorData(stroke);
        this.fillColor = new ColorData(fill);
        this.rotationAngle = 0.0;
    }

    private RectangleShape(UUID id, Rect bounds, ColorData strokeColor, ColorData fillColor, double rotationAngle) {
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
        this.bounds.translate(v); // Muove il rettangolo non ruotato
    }

    @Override
    public void resize(Rect newBounds) {
        Objects.requireNonNull(newBounds, "New bounds cannot be null for resize.");
        // Il resize cambia le dimensioni del rettangolo non ruotato.
        // La rotazione rimane la stessa e verrà applicata ai nuovi bounds.
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
        // Per verificare il contenimento di un punto in un rettangolo ruotato:
        // 1. Trasla il punto in modo che il centro del rettangolo sia all'origine.
        // 2. Applica la rotazione inversa al punto traslato.
        // 3. Verifica se il punto ruotato e traslato cade all'interno del rettangolo non ruotato (ora centrato all'origine).
        Point2D center = bounds.getCenter();
        double dx = p.getX() - center.getX();
        double dy = p.getY() - center.getY();

        double rad = Math.toRadians(-rotationAngle); // Angolo di rotazione inverso
        double cosRad = Math.cos(rad);
        double sinRad = Math.sin(rad);

        double rotatedX = dx * cosRad - dy * sinRad;
        double rotatedY = dx * sinRad + dy * cosRad;

        // Ora controlla se (rotatedX, rotatedY) è dentro un rettangolo di dimensioni bounds.getWidth(), bounds.getHeight()
        // centrato in (0,0)
        double halfWidth = bounds.getWidth() / 2.0;
        double halfHeight = bounds.getHeight() / 2.0;

        return Math.abs(rotatedX) <= halfWidth && Math.abs(rotatedY) <= halfHeight;
    }

    @Override
    public void accept(ShapeVisitor v) {
        Objects.requireNonNull(v, "ShapeVisitor cannot be null.");
        v.visit(this);
    }

    @Override
    public Shape clone() { // Per Clipboard
        return new RectangleShape(this.id, this.bounds, this.strokeColor, this.fillColor, this.rotationAngle);
    }
    
    @Override
    public Shape cloneWithNewId() { // Per Paste
        RectangleShape newShape = new RectangleShape(new Rect(this.bounds), new ColorData(this.strokeColor), new ColorData(this.fillColor));
        newShape.setRotation(this.rotationAngle);
        return newShape;
    }

    @Override
    public Rect getBounds() {
        // Restituisce il bounding box NON ruotato.
        // Per ottenere il bounding box del rettangolo ruotato, sarebbe necessario calcolare
        // le posizioni dei 4 vertici ruotati e poi trovare il min/max x/y.
        // Per ora, l'interfaccia Shape richiede il bounds "intrinseco".
        return new Rect(this.bounds);
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

    // Metodi non applicabili a RectangleShape
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
        // Per un rettangolo, che è simmetrico, riflettere i suoi 'bounds' non ruotati
        // rispetto al suo asse Y locale non cambia i 'bounds'.
        // Per riflettere la forma *visiva* se è ruotata, invertiamo l'angolo di rotazione.
        // Questo specchia la forma rispetto all'asse Y del mondo se la forma fosse allineata
        // e poi ruotata. Se la riflessione è rispetto all'asse Y *locale* della forma
        // (l'asse verticale che passa per il suo centro, orientato con la forma),
        // allora per un rettangolo non c'è cambiamento visivo nella sua geometria intrinseca.
        // L'US dice "specchiarsi rispetto al suo asse verticale centrale, invertendo la sua orientazione orizzontale".
        // Se l'asse verticale centrale è quello del mondo, e la forma è ruotata, l'effetto è complesso.
        // Se è l'asse verticale *locale* della forma (non ruotata), la forma non cambia.
        //
        // Assumiamo che l'intento sia un flip visivo.
        // Invertire l'angolo di rotazione riflette la forma rispetto all'asse X del sistema di coordinate
        // originale prima della rotazione. Per una riflessione orizzontale visiva,
        // se la forma è ruotata di A, la sua riflessione orizzontale è come ruotare di (180 - A)
        // o ruotare di -A e poi scalare X di -1 (ma non possiamo scalare X nel modello facilmente).
        //
        // La soluzione più semplice per un "flip" visivo orizzontale di una forma simmetrica
        // ruotata è invertire il segno della sua rotazione e poi normalizzare.
        // Questo riflette la forma rispetto a un asse (es. l'asse X del sistema di coordinate
        // del suo bounding box non ruotato, se la riflessione fosse prima della rotazione).
        // Per riflettere l'orientamento orizzontale della forma già ruotata,
        // si può pensare a cosa succederebbe se l'asse Y del canvas fosse uno specchio.
        // Angolo alfa -> Angolo (180 - alfa)
        double currentRotation = getRotation();
        setRotation( (180.0 - currentRotation) % 360.0 );
    }

    @Override
    public void reflectVertical() {
        // Simile a reflectHorizontal. Per un flip visivo verticale:
        // Angolo alfa -> Angolo (-alfa)
        double currentRotation = getRotation();
        setRotation( -currentRotation );
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RectangleShape that = (RectangleShape) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
         return String.format("RectangleShape{id=%s, bounds=%s, stroke=%s, fill=%s, rotation=%.1f}",
                id, bounds, strokeColor, fillColor, rotationAngle);
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
        Rect unrotatedBounds = getBounds(); // Questo è this.bounds
        double angle = getRotation();

        if (angle == 0.0) {
            return new Rect(unrotatedBounds); // Copia difensiva
        }

        Point2D center = unrotatedBounds.getCenter();

        // Ottieni i 4 vertici del rettangolo non ruotato
        Point2D topLeft = unrotatedBounds.getTopLeft();
        Point2D topRight = new Point2D(unrotatedBounds.getRight(), unrotatedBounds.getY());
        Point2D bottomLeft = new Point2D(unrotatedBounds.getX(), unrotatedBounds.getBottom());
        Point2D bottomRight = unrotatedBounds.getBottomRight();

        // Ruota ogni vertice
        Point2D rTL = rotatePoint(topLeft, center, angle);
        Point2D rTR = rotatePoint(topRight, center, angle);
        Point2D rBL = rotatePoint(bottomLeft, center, angle);
        Point2D rBR = rotatePoint(bottomRight, center, angle);

        // Trova min/max X e Y tra i vertici ruotati
        double minX = Math.min(Math.min(rTL.getX(), rTR.getX()), Math.min(rBL.getX(), rBR.getX()));
        double minY = Math.min(Math.min(rTL.getY(), rTR.getY()), Math.min(rBL.getY(), rBR.getY()));
        double maxX = Math.max(Math.max(rTL.getX(), rTR.getX()), Math.max(rBL.getX(), rBR.getX()));
        double maxY = Math.max(Math.max(rTL.getY(), rTR.getY()), Math.max(rBL.getY(), rBR.getY()));

        return new Rect(new Point2D(minX, minY), maxX - minX, maxY - minY);
    }
}
