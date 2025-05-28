// File: sad/gruppo11/Model/TextShape.java
package sad.gruppo11.Model;

import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Rect;
import sad.gruppo11.Model.geometry.Vector2D;
import sad.gruppo11.View.ShapeVisitor;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class TextShape implements Shape, Serializable {
    private static final long serialVersionUID = 2L; // Version bump

    private final UUID id;
    private String text;
    // 'position' ora si riferisce all'angolo in alto a sinistra dei 'drawingBounds'
    // private Point2D position; // Non più usato direttamente come prima, derivato da drawingBounds
    private double baseFontSize; // Dimensione del font di riferimento
    private String fontName;
    private ColorData textColor;
    private double rotationAngle;

    // Memorizza i bounds desiderati per il disegno, a cui il testo verrà scalato
    private Rect drawingBounds;

    public TextShape(String text, Point2D initialPosition, double initialFontSize, String fontName, ColorData color) {
        this.id = UUID.randomUUID();
        Objects.requireNonNull(text, "Text cannot be null for TextShape.");
        Objects.requireNonNull(initialPosition, "Initial position cannot be null for TextShape.");
        if (initialFontSize <= 0) throw new IllegalArgumentException("Initial font size must be positive.");
        Objects.requireNonNull(fontName, "Font name cannot be null.");
        Objects.requireNonNull(color, "Text color cannot be null.");

        this.text = text;
        this.baseFontSize = initialFontSize; // Questo è il font size "nominale"
        this.fontName = fontName;
        this.textColor = new ColorData(color);
        this.rotationAngle = 0.0;

        // All'inizio, i drawingBounds sono stimati basati sul testo e fontSize iniziale
        // Il renderer userà questo per la prima visualizzazione, ma il resize li sovrascriverà.
        // Questa è una stima MOLTO GREZZA che il renderer dovrà affinare o usare per la prima scala.
        double estimatedWidth = text.length() * baseFontSize * 0.6; // Stima
        double estimatedHeight = baseFontSize * 1.0; // Stima
        this.drawingBounds = new Rect(initialPosition, estimatedWidth, estimatedHeight);
    }

    // Costruttore privato per la clonazione
    private TextShape(UUID id, String text, Rect drawingBounds, double baseFontSize, String fontName, ColorData textColor, double rotationAngle) {
        this.id = id;
        this.text = text;
        this.drawingBounds = new Rect(drawingBounds);
        this.baseFontSize = baseFontSize;
        this.fontName = fontName;
        this.textColor = new ColorData(textColor);
        this.rotationAngle = rotationAngle;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void move(Vector2D v) {
        Objects.requireNonNull(v, "Movement vector cannot be null.");
        // Sposta i drawingBounds
        this.drawingBounds.translate(v);
    }

    @Override
    public void resize(Rect newBounds) {
        Objects.requireNonNull(newBounds, "New bounds cannot be null for resize.");
        if (newBounds.getWidth() < 0 || newBounds.getHeight() < 0) { // Permetti dimensioni zero per collassare? Per ora no.
             System.err.println("TextShape resize: new bounds have non-positive width or height. Ignoring.");
             return;
        }
        // Il testo verrà ora scalato per adattarsi a questi nuovi bounds dal renderer.
        this.drawingBounds = new Rect(newBounds);
        // baseFontSize rimane lo stesso, la scala effettiva è gestita dal renderer.
    }

    @Override
    public void setStrokeColor(ColorData c) {
        Objects.requireNonNull(c, "Text color (via setStrokeColor) cannot be null.");
        this.textColor = new ColorData(c);
    }

    @Override
    public ColorData getStrokeColor() {
        return new ColorData(this.textColor);
    }

    @Override
    public void setFillColor(ColorData c) { /* No-op */ }

    @Override
    public ColorData getFillColor() {
        return ColorData.TRANSPARENT;
    }

    @Override
    public boolean contains(Point2D p) {
        Objects.requireNonNull(p, "Point p cannot be null for contains check.");
        
        // Il test di contenimento ora si basa sui drawingBounds (che definiscono il rettangolo visibile)
        // e sulla rotazione.
        Rect currentUnrotatedBounds = this.drawingBounds; // I bounds a cui il testo è scalato, non ruotati

        Point2D center = currentUnrotatedBounds.getCenter();
        
        double translatedPx = p.getX() - center.getX();
        double translatedPy = p.getY() - center.getY();

        double angleRad = Math.toRadians(-this.rotationAngle);
        double cosAngle = Math.cos(angleRad);
        double sinAngle = Math.sin(angleRad);

        double rotatedTranslatedPx = translatedPx * cosAngle - translatedPy * sinAngle;
        double rotatedTranslatedPy = translatedPx * sinAngle + translatedPy * cosAngle;
        
        double halfWidth = currentUnrotatedBounds.getWidth() / 2.0;
        double halfHeight = currentUnrotatedBounds.getHeight() / 2.0;

        return Math.abs(rotatedTranslatedPx) <= halfWidth && 
               Math.abs(rotatedTranslatedPy) <= halfHeight;
    }

    @Override
    public void accept(ShapeVisitor v) {
        Objects.requireNonNull(v, "ShapeVisitor cannot be null.");
        v.visit(this);
    }

    @Override
    public Shape clone() { // Usato dal Clipboard per la copia originale
        return new TextShape(this.id, this.text, this.drawingBounds, this.baseFontSize, this.fontName, this.textColor, this.rotationAngle);
    }

    @Override
    public Shape cloneWithNewId() { // Usato per Paste
        TextShape newShape = new TextShape(this.text, new Point2D(this.drawingBounds.getTopLeft()), this.baseFontSize, this.fontName, new ColorData(this.textColor));
        // Quando si incolla, i bounds potrebbero dover essere ricalcolati o il clone prende i bounds dell'originale.
        // Per ora, il costruttore principale stima i bounds, il che potrebbe essere OK per un paste.
        // O meglio, copiare i drawingBounds dell'originale:
        newShape.drawingBounds = new Rect(this.drawingBounds);
        newShape.setRotation(this.rotationAngle);
        return newShape;
    }
    
    @Override
    public Rect getBounds() {
        return this.drawingBounds;
    }

    // Getters specifici
    public Point2D getPosition() { // Ora restituisce l'angolo in alto a sinistra dei drawingBounds
        return new Point2D(this.drawingBounds.getTopLeft()); 
    } 
    public String getFontName() { return fontName; }
    
    // Questo getter ora restituisce la dimensione "base" del font, non quella effettivamente visualizzata
    // che dipende dalla scala implicita data da drawingBounds.
    public double getBaseFontSize() { return baseFontSize; }


    @Override
    public void setRotation(double angle) {
        this.rotationAngle = angle % 360.0;
    }

    @Override
    public double getRotation() {
        return this.rotationAngle;
    }

    @Override
    public void setText(String text) {
        this.text = Objects.requireNonNullElse(text, "");
        // Nota: se il testo cambia, i drawingBounds potrebbero non essere più appropriati
        // se erano stati stimati. Se sono stati impostati da un resize, rimangono quelli.
        // Per un comportamento "paint-like", il testo si adatta ai bounds.
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
    public double getFontSize() { // Restituisce il baseFontSize
        return this.baseFontSize;
    }
    
    // Metodo per ottenere i bounds a cui il testo deve essere scalato per il disegno
    public Rect getDrawingBounds() {
        return new Rect(this.drawingBounds);
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
}