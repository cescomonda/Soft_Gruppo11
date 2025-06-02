
package sad.gruppo11.Model;

import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Rect;
import sad.gruppo11.Model.geometry.Vector2D;
import sad.gruppo11.View.ShapeVisitor;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class TextShape extends AbstractBaseShape implements Serializable {
    private static final long serialVersionUID = 3L; 

    private final UUID id;
    private String text;
    private double baseFontSize; 
    private String fontName;
    private ColorData textColor; // Colore del testo
    private double rotationAngle;
    private Rect drawingBounds; // Rettangolo target per il disegno, NON ruotato
    private boolean horizontallyFlipped = false; // Indica se il testo è stato riflesso orizzontalmente
    private boolean verticallyFlipped = false; // Indica se il testo è stato riflesso verticalmente

    public TextShape(String text, Point2D initialPosition, double initialFontSize, String fontName, ColorData color) {
        this.id = UUID.randomUUID();
        Objects.requireNonNull(text, "Text cannot be null for TextShape.");
        Objects.requireNonNull(initialPosition, "Initial position cannot be null for TextShape.");
        if (initialFontSize <= 0) throw new IllegalArgumentException("Initial font size must be positive.");
        Objects.requireNonNull(fontName, "Font name cannot be null.");
        Objects.requireNonNull(color, "Text color cannot be null.");

        this.text = text;
        this.baseFontSize = initialFontSize;
        this.fontName = fontName;
        this.textColor = new ColorData(color);
        this.rotationAngle = 0.0;

        // Stima iniziale dei bounds (potrebbe essere raffinata dal renderer o da un resize)
        // Questa è una stima molto grezza. Il renderer dovrà gestire la scala effettiva.
        double estimatedWidth = text.length() * baseFontSize * 0.65; // Stima approssimativa
        double estimatedHeight = baseFontSize * 1.2; // Stima approssimativa per una riga
        this.drawingBounds = new Rect(initialPosition, estimatedWidth, estimatedHeight);
    }

    private TextShape(UUID id, String text, Rect drawingBounds, double baseFontSize, String fontName, ColorData textColor, double rotationAngle, boolean horizontallyFlipped, boolean verticallyFlipped) {
        this.id = id;
        this.text = text;
        this.drawingBounds = new Rect(drawingBounds);
        this.baseFontSize = baseFontSize;
        this.fontName = fontName;
        this.textColor = new ColorData(textColor);
        this.rotationAngle = rotationAngle;
        this.horizontallyFlipped = horizontallyFlipped;
        this.verticallyFlipped = verticallyFlipped;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void move(Vector2D v) {
        Objects.requireNonNull(v, "Movement vector cannot be null.");
        this.drawingBounds.translate(v);
    }

    @Override
    public void resize(Rect newBounds) {
        Objects.requireNonNull(newBounds, "New bounds cannot be null for resize.");
         if (newBounds.getWidth() < 0 || newBounds.getHeight() < 0) {
             System.err.println("TextShape resize: new bounds have non-positive width or height. Ignoring.");
             return;
        }
        this.drawingBounds = new Rect(newBounds);
        // Il baseFontSize rimane lo stesso; la scala effettiva è gestita dal renderer
        // per adattare il testo ai nuovi drawingBounds.
    }

    @Override
    public void setStrokeColor(ColorData c) {
        // Per TextShape, strokeColor è interpretato come il colore del testo stesso.
        Objects.requireNonNull(c, "Text color (via setStrokeColor) cannot be null.");
        this.textColor = new ColorData(c);
    }

    @Override
    public ColorData getStrokeColor() {
        // Restituisce il colore del testo.
        return new ColorData(this.textColor);
    }

    @Override
    public void setFillColor(ColorData c) { 
        // No-op, il "fill" di un testo è il colore del testo stesso, gestito da strokeColor/textColor.
        // Oppure, si potrebbe interpretare come un colore di sfondo del bounding box del testo.
        // Per coerenza con l'interfaccia, e dato che strokeColor è il colore del testo,
        // fillColor per TextShape potrebbe non avere un significato diretto o essere trasparente.
    }

    @Override
    public ColorData getFillColor() {
        return ColorData.TRANSPARENT; // Testo non ha un "fill" separato dal colore del testo.
    }

    @Override
    public boolean contains(Point2D p) {
        Objects.requireNonNull(p, "Point p cannot be null for contains check.");
        // Il contenimento si basa sui drawingBounds (che definiscono il rettangolo visibile) e sulla rotazione.
        // Questo è identico al contains di RectangleShape, usando this.drawingBounds.
        Point2D center = drawingBounds.getCenter();
        double dx = p.getX() - center.getX();
        double dy = p.getY() - center.getY();

        double rad = Math.toRadians(-rotationAngle);
        double cosRad = Math.cos(rad);
        double sinRad = Math.sin(rad);

        double rotatedX = dx * cosRad - dy * sinRad;
        double rotatedY = dx * sinRad + dy * cosRad;

        double halfWidth = drawingBounds.getWidth() / 2.0;
        double halfHeight = drawingBounds.getHeight() / 2.0;

        return Math.abs(rotatedX) <= halfWidth && Math.abs(rotatedY) <= halfHeight;
    }

    @Override
    public void accept(ShapeVisitor v) {
        Objects.requireNonNull(v, "ShapeVisitor cannot be null.");
        v.visit(this);
    }

    @Override
    public Shape clone() { 
        return new TextShape(this.id, this.text, this.drawingBounds, this.baseFontSize, this.fontName, this.textColor, this.rotationAngle, 
                             this.horizontallyFlipped, this.verticallyFlipped);
    }

    @Override
    public Shape cloneWithNewId() { 
        TextShape newShape = new TextShape(UUID.randomUUID(), this.text, this.drawingBounds, this.baseFontSize, this.fontName, this.textColor, this.rotationAngle, 
                                            this.horizontallyFlipped, this.verticallyFlipped);
        // Il costruttore privato copia i valori, incluso drawingBounds.
        return newShape;
    }
    
    @Override
    public Rect getBounds() {
        // Restituisce i drawingBounds, che sono i bounds target non ruotati.
        return new Rect(this.drawingBounds);
    }

    public Point2D getPosition() { 
        return new Point2D(this.drawingBounds.getTopLeft()); 
    } 
    public String getFontName() { return fontName; }
    
    public double getBaseFontSize() { return baseFontSize; }

    public boolean isHorizontallyFlipped() {
        return horizontallyFlipped;
    }

    public boolean isVerticallyFlipped() {
        return verticallyFlipped;
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
    public void setText(String text) {
        this.text = Objects.requireNonNullElse(text, "");
        // Nota: Cambiare il testo potrebbe invalidare la stima dei drawingBounds se si vuole
        // che i bounds si adattino automaticamente al nuovo testo.
        // Per ora, i drawingBounds rimangono fissi a meno di un resize esplicito.
        // Il renderer adatterà il nuovo testo ai bounds esistenti.
    }

    @Override
    public String getText() {
        return this.text;
    }

    @Override
    public void setFontSize(double size) {
        // Questo ora imposta il baseFontSize. I drawingBounds potrebbero necessitare
        // di un aggiornamento se si vuole che il testo cambi dimensione visiva
        // mantenendo un aspect ratio "naturale" del font.
        // Se l'utente cambia il font size dal pannello proprietà, idealmente
        // i drawingBounds dovrebbero adattarsi.
        if (size <= 0) throw new IllegalArgumentException("Font size must be positive.");
        this.baseFontSize = size;
        
        // Opzionale: ricalcola i drawingBounds per mantenere l'aspetto se questo metodo è chiamato
        // dall'utente per cambiare esplicitamente la "grandezza" del testo.
        // Questo è un punto delicato: se i bounds sono stati fissati da un resize, cambiarli qui
        // potrebbe essere controintuitivo.
        // Per ora, modifichiamo solo baseFontSize. Il renderer lo userà.
        // Se vuoi che i bounds si adattino al nuovo baseFontSize:
        double oldHeight = this.drawingBounds.getHeight();
        double oldEstimatedBaseHeight = (oldHeight > 0 && this.text != null && !this.text.isEmpty()) ? oldHeight / (this.text.split("\\n").length) : this.baseFontSize;
        double scaleFactor = size / oldEstimatedBaseHeight; // o size / this.baseFontSize se baseFontSize era la base per i bounds
        this.drawingBounds = new Rect(this.drawingBounds.getTopLeft(), this.drawingBounds.getWidth() * scaleFactor, this.drawingBounds.getHeight() * scaleFactor);
    }

    @Override
    public double getFontSize() { 
        return this.baseFontSize;
    }
    
    public Rect getDrawingBounds() { // Usato dal renderer
        return new Rect(this.drawingBounds);
    }

    @Override
    public void reflectHorizontal() {
        this.horizontallyFlipped = !this.horizontallyFlipped;
    }

    @Override
    public void reflectVertical() {
        this.verticallyFlipped = !this.verticallyFlipped;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TextShape textShape = (TextShape) o;
        return id.equals(textShape.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("TextShape{id=%s, text='%s', drawingBounds=%s, baseSize=%.1f, font='%s', color=%s, rotation=%.1f}",
                id, text, drawingBounds, baseFontSize, fontName, textColor, rotationAngle);
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
        // getBounds() per TextShape restituisce i drawingBounds non ruotati
        Rect unrotatedDrawingBounds = getDrawingBounds(); 
        double angle = getRotation();

        if (angle == 0.0) {
            return new Rect(unrotatedDrawingBounds); // Copia difensiva
        }

        Point2D center = unrotatedDrawingBounds.getCenter();

        Point2D topLeft = unrotatedDrawingBounds.getTopLeft();
        Point2D topRight = new Point2D(unrotatedDrawingBounds.getRight(), unrotatedDrawingBounds.getY());
        Point2D bottomLeft = new Point2D(unrotatedDrawingBounds.getX(), unrotatedDrawingBounds.getBottom());
        Point2D bottomRight = unrotatedDrawingBounds.getBottomRight();

        Point2D rTL = rotatePoint(topLeft, center, angle);
        Point2D rTR = rotatePoint(topRight, center, angle);
        Point2D rBL = rotatePoint(bottomLeft, center, angle);
        Point2D rBR = rotatePoint(bottomRight, center, angle);

        double minX = Math.min(Math.min(rTL.getX(), rTR.getX()), Math.min(rBL.getX(), rBR.getX()));
        double minY = Math.min(Math.min(rTL.getY(), rTR.getY()), Math.min(rBL.getY(), rBR.getY()));
        double maxX = Math.max(Math.max(rTL.getX(), rTR.getX()), Math.max(rBL.getX(), rBR.getX()));
        double maxY = Math.max(Math.max(rTL.getY(), rTR.getY()), Math.max(rBL.getY(), rBR.getY()));

        return new Rect(new Point2D(minX, minY), maxX - minX, maxY - minY);
    }
}
