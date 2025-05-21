package sad.gruppo11.View;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.canvas.Canvas;

import java.util.List;
import java.util.Objects;

import sad.gruppo11.Model.Shape;

/**
 * {@code CanvasPanel} gestisce l'area di disegno su un {@link Canvas} JavaFX
 * e si occupa del rendering delle forme tramite un {@link ShapeVisitor}.
 */
public class CanvasPanel {
    private final Canvas canvas;
    private final GraphicsContext gc;

    /**
     * Costruisce un {@code CanvasPanel} associato a un {@link Canvas} JavaFX esistente.
     *
     * @param canvas Il canvas su cui disegnare. Non può essere {@code null}.
     */
    public CanvasPanel(Canvas canvas) {
        Objects.requireNonNull(canvas, "Canvas cannot be null.");
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
    }

    /**
     * Pulisce l'intera area del canvas.
     */
    public void clear() {
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        if (width > 0 && height > 0) {
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        }
    }

    /**
     * Ridisegna tutte le forme specificate sul canvas.
     * Prima pulisce il canvas, poi invoca il renderer su ciascuna forma.
     *
     * @param renderer Il visitatore che esegue il rendering. Non può essere {@code null}.
     * @param shapes   Le forme da disegnare. Non può essere {@code null}.
     */
    public void drawShapes(ShapeVisitor renderer, List<Shape> shapes) {
        Objects.requireNonNull(renderer, "ShapeVisitor (renderer) cannot be null.");
        Objects.requireNonNull(shapes, "List of shapes cannot be null.");

        clear();
        for (Shape shape : shapes) {
            if (shape != null) {
                shape.accept(renderer);
            }
        }
    }

    /**
     * @return Il {@link GraphicsContext} del canvas.
     */
    public GraphicsContext getGraphicsContext() {
        return gc;
    }

    /**
     * @return Il {@link Canvas} sottostante.
     */
    public Canvas getCanvas() {
        return canvas;
    }
}
