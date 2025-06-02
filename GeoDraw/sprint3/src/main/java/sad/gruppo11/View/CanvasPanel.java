
package sad.gruppo11.View;

import sad.gruppo11.Model.GroupShape;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Point2D;
// Rimuovi import Rect se non usato direttamente qui
// import sad.gruppo11.Model.geometry.Rect; 

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.transform.Affine;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.lang.Iterable;

public class CanvasPanel {
    private final Canvas canvas;
    private final GraphicsContext gc;
    private final JavaFXShapeRenderer renderer;

    // Stato della trasformazione della vista
    private double zoomFactor = 1.0;
    private double offsetX = 0.0; // Traslazione X della vista (pan)
    private double offsetY = 0.0; // Traslazione Y della vista (pan)
    
    // Stato della griglia
    private boolean gridEnabled = false;
    private double gridSize = 20.0; // Dimensione della cella della griglia in coordinate mondo

    // Visuali temporanee per feedback durante il disegno
    private List<Point2D> temporaryPolygonPoints = null;
    private Point2D temporaryRubberBandEnd = null;     
    private Shape temporaryGhostShape = null;          

    public CanvasPanel(Canvas canvas) {
        Objects.requireNonNull(canvas, "Canvas cannot be null for CanvasPanel.");
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        this.renderer = new JavaFXShapeRenderer(this.gc); 
        // Imposta una larghezza di linea di default per il renderer
        this.renderer.setDefaultLineWidth(1.5); // o leggi da una configurazione
    }
    
    public Canvas getCanvas() {
        return this.canvas;
    }

    public void clear() {
        // Pulisce il canvas con il colore di sfondo (o trasparente)
        gc.setTransform(new Affine()); // Resetta trasformazioni prima di pulire
        gc.clearRect(0, 0, this.canvas.getWidth(), this.canvas.getHeight());
    }

    /**
     * Applica la trasformazione corrente (zoom e pan) al GraphicsContext.
     */
    private void applyCurrentViewTransform() {
        gc.setTransform(new Affine()); // Resetta a identità
        gc.translate(offsetX, offsetY); // Applica pan
        gc.scale(zoomFactor, zoomFactor); // Applica zoom
    }
    
    public void drawShapes(Iterable<Shape> shapesToDraw, Shape selectedShapes) {
        Objects.requireNonNull(shapesToDraw, "Shapes iterable cannot be null.");
        clear(); // Pulisce il canvas
        
        gc.save(); // Salva lo stato del GC (trasformazioni, stili, ecc.)
        applyCurrentViewTransform(); // Applica zoom e pan

        if (gridEnabled) {
            renderGridTransformed(); // Disegna la griglia (già trasformata)
        }
        
        renderer.setSelectedShapeForRendering(selectedShapes); // Informa il renderer della selezione

        if(selectedShapes instanceof GroupShape) {
            // Se il gruppo è selezionato, disegna un contorno attorno a tutte le forme del gruppo
            GroupShape group = (GroupShape) selectedShapes;
            if (!group.getChildren().isEmpty()) {
                for (Shape child : group.getChildren()) {
                    if (child != null) {
                        renderer.setSelectedShapeForRendering(child); // Imposta il child come selezionato
                        child.accept(this.renderer); // Disegna ogni forma del gruppo
                    }
                }
            }
        }

        for (Shape shape : shapesToDraw) {
            if (shape != null) {
                shape.accept(this.renderer); // Il renderer gestisce il disegno di ogni forma
            }
        }

        drawCurrentTemporaryVisuals(); // Disegna feedback temporaneo (es. rubber banding)

        gc.restore(); // Ripristina lo stato del GC
    }

    public void setTemporaryPolygonGuide(List<Point2D> points, Point2D rubberBandEnd) {
        this.temporaryPolygonPoints = (points != null && !points.isEmpty()) ? new ArrayList<>(points) : null;
        this.temporaryRubberBandEnd = (rubberBandEnd != null && this.temporaryPolygonPoints != null) ? new Point2D(rubberBandEnd) : null;
        this.temporaryGhostShape = null; 
    }

    public void setTemporaryGhostShape(Shape ghostShape) {
        this.temporaryGhostShape = ghostShape; 
        this.temporaryPolygonPoints = null; 
        this.temporaryRubberBandEnd = null;
    }

    public void clearTemporaryVisuals() {
        this.temporaryPolygonPoints = null;
        this.temporaryRubberBandEnd = null;
        this.temporaryGhostShape = null;
    }

    private void drawCurrentTemporaryVisuals() {
        // Questo metodo viene chiamato dopo che la trasformazione principale (zoom/pan) è stata applicata.
        // Quindi, le coordinate dei punti/forme temporanee sono già in coordinate mondo.
        gc.save();
        gc.setStroke(Color.DARKSLATEGRAY); // Colore per le guide
        gc.setLineWidth(1.0 / zoomFactor); // Linee sottili, indipendenti dallo zoom visivo
        gc.setLineDashes(3 * (1.0 / zoomFactor), 3 * (1.0 / zoomFactor)); // Tratteggio scalato
        gc.setLineCap(StrokeLineCap.ROUND);

        if (temporaryPolygonPoints != null && !temporaryPolygonPoints.isEmpty()) {
            for (int i = 0; i < temporaryPolygonPoints.size() - 1; i++) {
                Point2D p1 = temporaryPolygonPoints.get(i);
                Point2D p2 = temporaryPolygonPoints.get(i + 1);
                gc.strokeLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
            }
            if (temporaryRubberBandEnd != null) {
                Point2D lastPoint = temporaryPolygonPoints.get(temporaryPolygonPoints.size() - 1);
                gc.strokeLine(lastPoint.getX(), lastPoint.getY(), temporaryRubberBandEnd.getX(), temporaryRubberBandEnd.getY());
            }
        } else if (temporaryGhostShape != null) {
            // Per la ghost shape, potremmo volerla disegnare con stili specifici (es. solo contorno)
            // Il renderer attuale la disegnerebbe con i suoi colori. Modifichiamo temporaneamente la ghost.
            ColorData originalFill = temporaryGhostShape.getFillColor();
            ColorData originalStroke = temporaryGhostShape.getStrokeColor();
            double originalLineWidth = renderer.getDefaultLineWidth(); // Salva la larghezza di linea del renderer

            temporaryGhostShape.setFillColor(ColorData.TRANSPARENT); // No fill per la ghost
            // Usa un colore di stroke fisso per la ghost, o prendilo da una config
            temporaryGhostShape.setStrokeColor(new ColorData(100,100,100, 0.7)); 
            renderer.setDefaultLineWidth(1.0); // Linea sottile per la ghost

            temporaryGhostShape.accept(renderer);

            // Ripristina la forma e il renderer
            temporaryGhostShape.setFillColor(originalFill);
            temporaryGhostShape.setStrokeColor(originalStroke);
            renderer.setDefaultLineWidth(originalLineWidth);
        }
        gc.restore();
    }
    
    private void renderGridTransformed() {
        if (!gridEnabled || gridSize * zoomFactor < 2) { // Non disegnare se troppo fitta
            return;
        }

        final double invZoom = 1.0 / zoomFactor;
        final double viewW = canvas.getWidth();
        final double viewH = canvas.getHeight();

        // Calcola i limiti del mondo visibili attraverso la viewport
        final double worldX0Visible = -offsetX * invZoom;
        final double worldY0Visible = -offsetY * invZoom;
        final double worldX1Visible = (viewW - offsetX) * invZoom;
        final double worldY1Visible = (viewH - offsetY) * invZoom;

        // Trova le prime linee della griglia che cadono dentro o ai bordi dell'area visibile
        final double startGridX = Math.floor(worldX0Visible / gridSize) * gridSize;
        final double endGridX   = Math.ceil(worldX1Visible / gridSize) * gridSize;
        final double startGridY = Math.floor(worldY0Visible / gridSize) * gridSize;
        final double endGridY   = Math.ceil(worldY1Visible / gridSize) * gridSize;

        gc.save();
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(0.5 * invZoom); // Spessore linea costante a schermo

        gc.beginPath();
        for (double x = startGridX; x <= endGridX; x += gridSize) {
            // Disegna linee verticali all'interno delle coordinate del mondo visibili
            gc.moveTo(x, worldY0Visible);
            gc.lineTo(x, worldY1Visible);
        }
        for (double y = startGridY; y <= endGridY; y += gridSize) {
            // Disegna linee orizzontali all'interno delle coordinate del mondo visibili
            gc.moveTo(worldX0Visible, y);
            gc.lineTo(worldX1Visible, y);
        }
        gc.stroke();
        gc.closePath();
        gc.restore();
    }

    public void setTransform(double zoom, double newOffsetX, double newOffsetY) {
        this.zoomFactor = Math.max(0.1, Math.min(zoom, 10.0)); // Limita lo zoom
        this.offsetX = newOffsetX;
        this.offsetY = newOffsetY;
    }
    
    public double getZoomFactor() { return zoomFactor; }
    public double getOffsetX() { return offsetX; }
    public double getOffsetY() { return offsetY; }

    public void setGridEnabled(boolean enabled) {
        this.gridEnabled = enabled;
    }
    public void setGridSize(double size) {
        if (size > 0) this.gridSize = size;
    }
    public boolean isGridEnabled() { return gridEnabled; }
    public double getGridSize() { return gridSize; }
    
    public Point2D screenToWorld(Point2D screenPoint) {
        Objects.requireNonNull(screenPoint, "Screen point cannot be null.");
        double worldX = (screenPoint.getX() - offsetX) / zoomFactor;
        double worldY = (screenPoint.getY() - offsetY) / zoomFactor;
        return new Point2D(worldX, worldY);
    }

    public Point2D worldToScreen(Point2D worldPoint) {
        Objects.requireNonNull(worldPoint, "World point cannot be null.");
        double screenX = worldPoint.getX() * zoomFactor + offsetX;
        double screenY = worldPoint.getY() * zoomFactor + offsetY;
        return new Point2D(screenX, screenY);
    }
}
