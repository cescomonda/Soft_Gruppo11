package sad.gruppo11.View;

import sad.gruppo11.Controller.GeoEngine;
import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.geometry.Point2D;
import javafx.scene.canvas.Canvas;

import java.util.Objects;

/**
 * {@code DrawingView} funge da vista osservabile per il modello {@link Drawing}.
 * Riceve aggiornamenti e si occupa del rendering delle forme sul canvas associato.
 */
public class DrawingView implements Observer {
    private GeoEngine controller;
    private final CanvasPanel canvasPanel;
    private final JavaFXShapeRenderer shapeRenderer;
    private Drawing currentDrawingModel;

    public DrawingView(CanvasPanel canvasPanel, Drawing initialDrawing) {
        Objects.requireNonNull(canvasPanel, "CanvasPanel cannot be null.");
        this.canvasPanel = canvasPanel;
        this.shapeRenderer = new JavaFXShapeRenderer(canvasPanel.getGraphicsContext());
        this.currentDrawingModel = initialDrawing;

        if (this.currentDrawingModel != null) {
            this.currentDrawingModel.addObserver(this);
        }
        setupMouseHandlers();
    }

    public void setController(GeoEngine controller) {
        this.controller = controller;
    }

    public void setDrawingModel(Drawing newDrawingModel) {
        if (this.currentDrawingModel != null) {
            this.currentDrawingModel.removeObserver(this);
        }
        this.currentDrawingModel = newDrawingModel;
        if (this.currentDrawingModel != null) {
            this.currentDrawingModel.addObserver(this);
            render();
        } else {
            canvasPanel.clear();
        }
    }

    private void setupMouseHandlers() {
        Canvas actualCanvas = canvasPanel.getCanvas();

        actualCanvas.setOnMousePressed(event -> {
            if (controller != null) {
                controller.onMousePressed(new Point2D(event.getX(), event.getY()));
            }
        });

        actualCanvas.setOnMouseDragged(event -> {
            if (controller != null && event.isPrimaryButtonDown()) {
                controller.onMouseDragged(new Point2D(event.getX(), event.getY()));
            }
        });

        actualCanvas.setOnMouseReleased(event -> {
            if (controller != null) {
                controller.onMouseReleased(new Point2D(event.getX(), event.getY()));
            }
        });
    }

    @Override
    public void update(Object observableSubject) {
        if (observableSubject instanceof Drawing) {
            if (this.currentDrawingModel != observableSubject) {
                this.currentDrawingModel = (Drawing) observableSubject;
            }
            render();
        }
    }

    public void render() {
        if (controller != null) {
            shapeRenderer.setSelectedShapeForRendering(controller.getSelectedShape());
        } else {
            shapeRenderer.setSelectedShapeForRendering(null);
        }

        if (currentDrawingModel != null) {
            canvasPanel.drawShapes(shapeRenderer, currentDrawingModel.getShapes());
        } else {
            canvasPanel.clear();
        }
    }

    public void setRendererLineWidth(double lineWidth) {
        shapeRenderer.setDefaultLineWidth(lineWidth);
        if (currentDrawingModel != null && !currentDrawingModel.getShapes().isEmpty()) {
            render();
        }
    }
}
