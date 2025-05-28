package sad.gruppo11.View;

import sad.gruppo11.Controller.GeoEngine;
import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.Observable;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.geometry.Point2D;

import javafx.scene.canvas.Canvas;
import javafx.scene.control.TextInputDialog;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.Alert;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class DrawingView implements Observer {
    private GeoEngine controller;
    private final CanvasPanel canvasPanel;
    private Drawing currentDrawingModel;
    private Stage primaryStage;

    public DrawingView(GeoEngine controller, CanvasPanel canvasPanel, Stage primaryStage) {
        Objects.requireNonNull(controller, "Controller cannot be null for DrawingView.");
        Objects.requireNonNull(canvasPanel, "CanvasPanel cannot be null for DrawingView.");
        Objects.requireNonNull(primaryStage, "PrimaryStage cannot be null for DrawingView.");
        this.controller = controller;
        this.canvasPanel = canvasPanel;
        this.primaryStage = primaryStage;
        this.currentDrawingModel = controller.getDrawing();
        if (this.currentDrawingModel != null) {
            this.currentDrawingModel.attach(this);
        }
        setupMouseHandlers();
        updateCanvasPanelTransform();
    }
    
    public void setController(GeoEngine newController) {
        Objects.requireNonNull(newController, "New Controller cannot be null.");
        if (this.currentDrawingModel != null) this.currentDrawingModel.detach(this);
        this.controller = newController;
        this.currentDrawingModel = controller.getDrawing();
        if (this.currentDrawingModel != null) this.currentDrawingModel.attach(this);
        updateCanvasPanelTransform();
        render();
    }

    public void setDrawingModel(Drawing newDrawingModel) {
        if (this.currentDrawingModel != null) this.currentDrawingModel.detach(this);
        this.currentDrawingModel = newDrawingModel;
        if (this.currentDrawingModel != null) this.currentDrawingModel.attach(this);
        render();
    }

    private void setupMouseHandlers() {
        Canvas actualCanvas = canvasPanel.getCanvas();
        actualCanvas.setOnMousePressed(event -> {
            if (controller != null) {
                Point2D worldPoint = canvasPanel.screenToWorld(new Point2D(event.getX(), event.getY()));
                controller.onMousePressed(worldPoint);
            }
        });
        actualCanvas.setOnMouseDragged(event -> {
            if (controller != null && event.isPrimaryButtonDown()) {
                Point2D worldPoint = canvasPanel.screenToWorld(new Point2D(event.getX(), event.getY()));
                controller.onMouseDragged(worldPoint);
            }
        });
        actualCanvas.setOnMouseReleased(event -> {
            if (controller != null) {
                Point2D worldPoint = canvasPanel.screenToWorld(new Point2D(event.getX(), event.getY()));
                controller.onMouseReleased(worldPoint);
            }
        });
        actualCanvas.setOnScroll(event -> {
            if (controller != null) {
                if (event.getDeltaY() > 0) controller.zoomIn(event.getX(), event.getY());
                else if (event.getDeltaY() < 0) controller.zoomOut(event.getX(), event.getY());
                event.consume();
            }
        });
    }
    
    private void updateCanvasPanelTransform() {
        if (controller != null && canvasPanel != null) {
            canvasPanel.setTransform(controller.getCurrentZoom(), controller.getScrollOffsetX(), controller.getScrollOffsetY());
            canvasPanel.setGrid(controller.isGridEnabled(), controller.getGridSize());
        }
    }

    @Override
    public void update(Observable source, Object arg) {
        boolean needsRender = false;
        if (source instanceof Drawing) {
            if (this.currentDrawingModel != source && source != null) {
                 this.currentDrawingModel = (Drawing) source;
            }
            needsRender = true;
        }
        // Check if GeoEngine is notifying about UI property changes (like zoom/grid)
        if (arg instanceof Drawing.DrawingChangeEvent) {
            Drawing.DrawingChangeEvent event = (Drawing.DrawingChangeEvent) arg;
            if (event.type == Drawing.DrawingChangeEvent.ChangeType.TRANSFORM || event.type == Drawing.DrawingChangeEvent.ChangeType.GRID) {
                 updateCanvasPanelTransform(); // Update transform before render
                 needsRender = true;
            }
        } else if (arg instanceof String && ("transformChange".equals(arg) || "gridChange".equals(arg))) {
            // Fallback for simple string notifications if GeoEngine uses them
            updateCanvasPanelTransform();
            needsRender = true;
        }


        if(needsRender) render();
    }

    public void render() {
        if (canvasPanel == null) return;
        Shape selectedShape = (controller != null) ? controller.getSelectedShape() : null;
        canvasPanel.setSelectedShapeForRenderer(selectedShape);
        if (currentDrawingModel != null) {
            canvasPanel.drawShapes(currentDrawingModel.getShapesInZOrder());
        } else {
            canvasPanel.clear();
        }
    }

    public String getUserInputForPath(boolean saveDialog) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("GeoDraw Files (*.ser)", "*.ser"));
        File file = saveDialog ? fileChooser.showSaveDialog(primaryStage) : fileChooser.showOpenDialog(primaryStage);
        return (file != null) ? file.getAbsolutePath() : null;
    }

    public void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(primaryStage);
        alert.showAndWait();
    }

    public String promptForText(String promptMessage) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Text Input");
        dialog.setHeaderText(promptMessage);
        dialog.setContentText("Text:");
        dialog.initOwner(primaryStage);
        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }
    
    public double getCurrentZoomFactor() { return (canvasPanel != null) ? canvasPanel.getZoomFactor() : 1.0; }
    public double getScrollOffsetX() { return (canvasPanel != null) ? canvasPanel.getOffsetX() : 0.0; }
    public double getScrollOffsetY() { return (canvasPanel != null) ? canvasPanel.getOffsetY() : 0.0; }

    public void setRendererLineWidth(double lineWidth) {
        if (canvasPanel != null) {
            canvasPanel.setRendererLineWidth(lineWidth);
            render();
        }
    }

    /**
     * Inoltra i dati per disegnare una guida poligonale temporanea a CanvasPanel.
     * @param points I vertici del poligono definiti finora.
     * @param currentMouse La posizione corrente del mouse per la linea guida.
     * @param isDefiningShape True se si sta attivamente definendo la forma (per feedback visivo).
     */
    public void drawTemporaryPolygonGuide(List<Point2D> points, Point2D currentMouse) {
        if (canvasPanel != null) {
            canvasPanel.setTemporaryPolygonGuide(points, currentMouse);
            // Non chiamare render() qui direttamente, dovrebbe essere chiamato da GeoEngine o da update()
            // per evitare cicli di rendering. GeoEngine chiamerà notifyViewToRefresh().
        }
    }

    /**
     * Inoltra una forma fantasma completa a CanvasPanel.
     * @param ghostShape La forma fantasma da disegnare, o null per cancellarla.
     */
    public void drawTemporaryGhostShape(Shape ghostShape) {
        if (canvasPanel != null) {
            canvasPanel.setTemporaryGhostShape(ghostShape);
            // GeoEngine chiamerà notifyViewToRefresh().
        }
    }


    /**
     * Inoltra la richiesta di cancellare tutte le visuali temporanee a CanvasPanel.
     */
    public void clearTemporaryVisuals() {
        if (canvasPanel != null) {
            canvasPanel.clearTemporaryVisuals();
            // GeoEngine chiamerà notifyViewToRefresh().
        }
    }
    
    // Questi metodi per userMessage rimangono se MainApp li usa tramite Observer su GeoEngine,
    // o vengono rimossi/modificati se DrawingView li gestisce direttamente.
    // Per ora, li lascio come prima (MainApp li gestisce tramite Observer).
    public void showUserMessage(String message) {
        // Implementazione in MainApp tramite Observer
        System.out.println("DrawingView-Info: " + message);
    }

    public void clearUserMessage() {
        // Implementazione in MainApp tramite Observer
         System.out.println("DrawingView-Info: Message Cleared");
    }
}