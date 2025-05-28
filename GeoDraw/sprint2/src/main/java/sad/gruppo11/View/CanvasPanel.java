package sad.gruppo11.View;

import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Point2D;

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

    private double zoomFactor = 1.0;
    private double offsetX = 0.0;
    private double offsetY = 0.0;
    private boolean gridEnabled = false;
    private double gridSize = 20.0;

    private List<Point2D> temporaryPolygonPoints = null; // Per poligono in costruzione
    private Point2D temporaryRubberBandEnd = null;      // Per la linea dall'ultimo punto al mouse
    private Shape temporaryGhostShape = null;           // Per forme complete come linea/rettangolo/ellisse durante il drag

    public CanvasPanel(Canvas canvas) {
        Objects.requireNonNull(canvas, "Canvas cannot be null for CanvasPanel.");
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        this.renderer = new JavaFXShapeRenderer(this.gc); 
    }
    
    public Canvas getCanvas() {
        return this.canvas;
    }

    public void clear() {
        gc.clearRect(0, 0, this.canvas.getWidth(), this.canvas.getHeight());
    }

    private void applyTransform() {
        gc.setTransform(new Affine());
        gc.translate(offsetX, offsetY);
        gc.scale(zoomFactor, zoomFactor);
    }
    
    public void drawShapes(Iterable<Shape> shapes) {
        Objects.requireNonNull(shapes, "Shapes iterable cannot be null.");
        clear();
        gc.save();
        applyTransform();
        if (gridEnabled) {
            renderGridTransformed();
        }
        for (Shape shape : shapes) {
            if (shape != null) {
                shape.accept(this.renderer);
            }
        }

        drawCurrentTemporaryVisuals();

        gc.restore();
    }

    /**
     * Imposta i punti per un poligono temporaneo in costruzione e un punto finale per una linea guida.
     * @param points Lista dei vertici già definiti del poligono.
     * @param rubberBandEnd Posizione corrente del mouse, a cui disegnare una linea dall'ultimo punto.
     */
    public void setTemporaryPolygonGuide(List<Point2D> points, Point2D rubberBandEnd) {
        this.temporaryPolygonPoints = (points != null) ? new ArrayList<>(points) : null; // Copia difensiva
        this.temporaryRubberBandEnd = (rubberBandEnd != null) ? new Point2D(rubberBandEnd) : null; // Copia difensiva
        this.temporaryGhostShape = null; // Assicura che solo un tipo di visuale temporanea sia attiva
    }

    /**
     * Imposta una forma "fantasma" completa da disegnare temporaneamente (es. rettangolo durante il drag).
     * @param ghostShape La forma da disegnare.
     */
    public void setTemporaryGhostShape(Shape ghostShape) {
        this.temporaryGhostShape = ghostShape; // Può essere null per cancellare
        this.temporaryPolygonPoints = null; // Assicura che solo un tipo di visuale temporanea sia attiva
        this.temporaryRubberBandEnd = null;
    }

    /**
     * Cancella tutte le visuali temporanee.
     */
    public void clearTemporaryVisuals() {
        this.temporaryPolygonPoints = null;
        this.temporaryRubberBandEnd = null;
        this.temporaryGhostShape = null;
    }

    /**
     * Disegna le visuali temporanee correnti (poligono guida o forma fantasma).
     * Questo metodo è chiamato internamente da drawShapes.
     */
    private void drawCurrentTemporaryVisuals() {
        gc.save();
        // Stile per le linee guida/forme fantasma
        gc.setStroke(Color.DARKSLATEGRAY);
        gc.setLineWidth(1.0 / zoomFactor); // Linee sottili indipendenti dallo zoom
        gc.setLineDashes(3, 3);
        gc.setLineCap(StrokeLineCap.ROUND);

        if (temporaryPolygonPoints != null && !temporaryPolygonPoints.isEmpty()) {
            // Disegna i segmenti del poligono già definiti
            for (int i = 0; i < temporaryPolygonPoints.size() - 1; i++) {
                Point2D p1 = temporaryPolygonPoints.get(i);
                Point2D p2 = temporaryPolygonPoints.get(i + 1);
                gc.strokeLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
            }
            // Disegna la linea guida dall'ultimo punto al mouse (rubberBandEnd)
            if (temporaryRubberBandEnd != null) {
                Point2D lastPoint = temporaryPolygonPoints.get(temporaryPolygonPoints.size() - 1);
                gc.strokeLine(lastPoint.getX(), lastPoint.getY(), temporaryRubberBandEnd.getX(), temporaryRubberBandEnd.getY());
            }
        } else if (temporaryGhostShape != null) {
            // Disegna la forma fantasma.
            // Potremmo voler usare un renderer diverso o uno stile specifico qui,
            // ma per semplicità usiamo lo stesso renderer con lo stile di stroke già impostato.
            // Il fill per le ghost shapes è solitamente trasparente o molto leggero.
            ColorData originalFill = temporaryGhostShape.getFillColor();
            ColorData originalStroke = temporaryGhostShape.getStrokeColor();

            // Applica colori temporanei per la ghost shape
            temporaryGhostShape.setFillColor(ColorData.TRANSPARENT); // No fill per la ghost
            temporaryGhostShape.setStrokeColor(new ColorData(100, 100, 100, 0.8)); // Grigio semi-trasparente

            temporaryGhostShape.accept(renderer); // Usa il renderer standard

            // Ripristina i colori originali della forma (importante se la forma è riutilizzata)
            temporaryGhostShape.setFillColor(originalFill);
            temporaryGhostShape.setStrokeColor(originalStroke);
        }
        gc.restore();
    }
    
/** Disegna la griglia tenendo conto di zoom e traslazione. */
    private void renderGridTransformed() {

        // 1. Evita di disegnare se, a questo livello di zoom, la griglia sarebbe più fitta di 1-2 px.
        if (gridSize * zoomFactor < 2) {
            return;
        }

        // 2. Pre-calcoli per ridurre divisioni ripetute a run-time.
        final double invZoom = 1.0 / zoomFactor;
        final double viewW   = canvas.getWidth();
        final double viewH   = canvas.getHeight();

        final double worldX0 = -offsetX * invZoom;
        final double worldY0 = -offsetY * invZoom;
        final double worldW  = viewW   * invZoom;
        final double worldH  = viewH   * invZoom;

        final double startX  = Math.floor(worldX0             / gridSize) * gridSize;
        final double endX    = Math.ceil((worldX0 + worldW)   / gridSize) * gridSize;
        final double startY  = Math.floor(worldY0             / gridSize) * gridSize;
        final double endY    = Math.ceil((worldY0 + worldH)   / gridSize) * gridSize;

        gc.save();
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(0.5 * invZoom);          // assicura lo spessore fisso a schermo

        // 3. Un’unica path: molte meno JNI call di gc.strokeLine(..) per ogni segmento
        gc.beginPath();
        for (double x = startX; x <= endX; x += gridSize) {
            gc.moveTo(x, startY);
            gc.lineTo(x, endY);
        }
        for (double y = startY; y <= endY; y += gridSize) {
            gc.moveTo(startX, y);
            gc.lineTo(endX, y);
        }
        gc.stroke();
        gc.restore();
    }

    public void renderGrid(GraphicsContext directGc) {
        if (!gridEnabled || directGc == null) return;
        directGc.save();
        directGc.setStroke(Color.LIGHTGRAY);
        directGc.setLineWidth(0.5);
        double width = directGc.getCanvas().getWidth();
        double height = directGc.getCanvas().getHeight();
        for (double x = 0; x < width; x += gridSize) {
            directGc.strokeLine(x, 0, x, height);
        }
        for (double y = 0; y < height; y += gridSize) {
            directGc.strokeLine(0, y, width, y);
        }
        directGc.restore();
    }

    public void setTransform(double zoom, double newOffsetX, double newOffsetY) {
        this.zoomFactor = zoom;
        this.offsetX = newOffsetX;
        this.offsetY = newOffsetY;
    }
    
    public double getZoomFactor() { return zoomFactor; }
    public double getOffsetX() { return offsetX; }
    public double getOffsetY() { return offsetY; }

    public void setGrid(boolean enabled, double size) {
        this.gridEnabled = enabled;
        if (size > 0) this.gridSize = size;
    }
    public boolean isGridEnabled() { return gridEnabled; }
    public double getGridSize() { return gridSize; }

    public void setSelectedShapeForRenderer(Shape selectedShape) {
        if (this.renderer != null) this.renderer.setSelectedShapeForRendering(selectedShape);
    }
    
    public void setRendererLineWidth(double lineWidth) {
        if (this.renderer != null) this.renderer.setDefaultLineWidth(lineWidth);
    }
    
    public Point2D screenToWorld(Point2D screenPoint) {
        double worldX = (screenPoint.getX() - offsetX) / zoomFactor;
        double worldY = (screenPoint.getY() - offsetY) / zoomFactor;
        return new Point2D(worldX, worldY);
    }

    public Point2D worldToScreen(Point2D worldPoint) {
        double screenX = worldPoint.getX() * zoomFactor + offsetX;
        double screenY = worldPoint.getY() * zoomFactor + offsetY;
        return new Point2D(screenX, screenY);
    }
}