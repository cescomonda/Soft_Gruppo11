
package sad.gruppo11.View;

import sad.gruppo11.Controller.GeoEngine;
import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.GroupShape;
import sad.gruppo11.Model.Observable;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.geometry.Point2D;

import javafx.application.Platform; // Per Platform.runLater
import javafx.scene.canvas.Canvas; // Rimosso, CanvasPanel lo gestisce
import javafx.scene.control.TextInputDialog;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.Alert;

import java.io.File;
import java.util.List; // Per drawTemporaryPolygonGuide
import java.util.Objects;
import java.util.Optional;

public class DrawingView implements Observer {
    private GeoEngine controller; // Riferimento al controller
    private final CanvasPanel canvasPanel; // Il pannello su cui si disegna
    private Drawing currentDrawingModel; // Il modello del disegno corrente osservato
    private Stage primaryStage; // Per dialogs

    public DrawingView(GeoEngine controller, CanvasPanel canvasPanel, Stage primaryStage) {
        Objects.requireNonNull(controller, "Controller cannot be null for DrawingView.");
        Objects.requireNonNull(canvasPanel, "CanvasPanel cannot be null for DrawingView.");
        Objects.requireNonNull(primaryStage, "PrimaryStage cannot be null for DrawingView.");
        
        this.controller = controller;
        this.canvasPanel = canvasPanel;
        this.primaryStage = primaryStage;
        
        // Ottieni il modello iniziale dal controller e registrati come observer
        this.currentDrawingModel = controller.getDrawing();
        if (this.currentDrawingModel != null) {
            this.currentDrawingModel.attach(this);
        }
        // GeoEngine stesso potrebbe essere Observable per proprietà come zoom/grid.
        // Se DrawingView deve reagire a questi, si registra anche a GeoEngine.
        this.controller.attach(this); // DrawingView osserva anche GeoEngine

        setupMouseHandlers(); // Configura gli input del mouse sul canvas
        updateCanvasPanelFromController(); // Imposta trasformazioni iniziali
        render(); // Render iniziale
    }
    
    public CanvasPanel getCanvasPanel() {
        return canvasPanel;
    }


    private void setupMouseHandlers() {
        javafx.scene.canvas.Canvas actualCanvas = canvasPanel.getCanvas();
        
        actualCanvas.setOnMousePressed(event -> {
            if (controller != null && event.isPrimaryButtonDown()) { // Gestisce solo il pulsante primario
                Point2D worldPoint = canvasPanel.screenToWorld(new Point2D(event.getX(), event.getY()));
                controller.onMousePressed(worldPoint);
                event.consume(); // Consuma l'evento per evitare propagazione
            }
        });

        actualCanvas.setOnMouseDragged(event -> {
            if (controller != null && event.isPrimaryButtonDown()) {
                Point2D worldPoint = canvasPanel.screenToWorld(new Point2D(event.getX(), event.getY()));
                controller.onMouseDragged(worldPoint);
                event.consume();
            }
        });

        actualCanvas.setOnMouseReleased(event -> {
            if (controller != null && event.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                Point2D worldPoint = canvasPanel.screenToWorld(new Point2D(event.getX(), event.getY()));
                controller.onMouseReleased(worldPoint);
                event.consume();
            }
        });
        
        actualCanvas.setOnScroll(event -> {
            if (controller != null) {
                if (event.getDeltaY() > 0) controller.zoomIn(event.getX(), event.getY());
                else if (event.getDeltaY() < 0) controller.zoomOut(event.getX(), event.getY());
                event.consume(); // L'evento scroll è gestito qui
            }
        });
    }
    
    /**
     * Aggiorna i parametri di trasformazione e griglia del CanvasPanel
     * leggendoli dal GeoEngine.
     */
    private void updateCanvasPanelFromController() {
        if (controller != null && canvasPanel != null) {
            canvasPanel.setTransform(
                controller.getCurrentZoom(), 
                controller.getScrollOffsetX(), 
                controller.getScrollOffsetY()
            );
            canvasPanel.setGridEnabled(controller.isGridEnabled());
            canvasPanel.setGridSize(controller.getGridSize());
        }
    }

    @Override
    public void update(Observable source, Object arg) {
        // Platform.runLater per assicurarsi che gli aggiornamenti UI avvengano sul thread JavaFX
        Platform.runLater(() -> {
            boolean needsRender = false;
            boolean transformChanged = false;

            if (source == currentDrawingModel) {
                // Il modello del disegno è cambiato (aggiunta/rimozione/modifica forma, z-order, clear, load)
                needsRender = true;
            } else if (source == controller) {
                // GeoEngine ha notificato un cambiamento
                if (arg instanceof Drawing.DrawingChangeEvent) {
                    Drawing.DrawingChangeEvent event = (Drawing.DrawingChangeEvent) arg;
                    if (event.type == Drawing.DrawingChangeEvent.ChangeType.TRANSFORM ||
                        event.type == Drawing.DrawingChangeEvent.ChangeType.GRID) {
                        transformChanged = true;
                        needsRender = true;
                    }
                } else if (arg instanceof Shape || arg == null) { 
                    // Potrebbe essere una notifica di cambio selezione da GeoEngine
                    needsRender = true;
                } else if (arg instanceof String) {
                    // Potrebbe essere un messaggio di cambio tool o altro feedback
                    // showUserMessage((String) arg); // MainApp gestisce questo se è observer
                }
            }

            if (transformChanged) {
                updateCanvasPanelFromController(); // Aggiorna prima le trasformazioni
            }
            if (needsRender) {
                render();
            }
        });
    }

    public void render() {
        if (canvasPanel == null) return;
        
        List<Shape> selectedShapes = (controller != null) ? controller.getSelectedShapes() : null;
        // Se la selezione è un gruppo, potremmo voler passare l'intero gruppo o i suoi figli
        // a seconda di come vogliamo che la selezione sia gestita dal renderer.
        // Per ora, passiamo la forma selezionata così com'è.

        GroupShape groupSelectedShapes = new GroupShape(selectedShapes);
        
        if (currentDrawingModel != null) {
            // Passa le forme e la forma selezionata al CanvasPanel
            canvasPanel.drawShapes(currentDrawingModel.getShapesInZOrder(), groupSelectedShapes);
        } else {
            canvasPanel.clear(); // Nessun modello, pulisci il canvas
        }
    }

    public String getUserInputForPath(boolean saveDialog, String dialogTitle, String initialFileName) {
        FileChooser fileChooser = new FileChooser();
        if (dialogTitle != null) fileChooser.setTitle(dialogTitle);
        if (initialFileName != null) fileChooser.setInitialFileName(initialFileName);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("GeoDraw Files (*.ser)", "*.ser"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files (*.*)", "*.*"));

        File file = saveDialog ? fileChooser.showSaveDialog(primaryStage) : fileChooser.showOpenDialog(primaryStage);
        return (file != null) ? file.getAbsolutePath() : null;
    }
    
    // Sovraccarico per compatibilità con chiamate precedenti
    public String getUserInputForPath(boolean saveDialog) {
        return getUserInputForPath(saveDialog, saveDialog ? "Save Drawing" : "Open Drawing", "drawing.ser");
    }


    public void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null); // Nessun header
        alert.setContentText(message);
        alert.initOwner(primaryStage); // Assicura che il dialogo sia modale rispetto alla finestra principale
        alert.showAndWait();
    }

    public String promptForText(String promptMessage, String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue != null ? defaultValue : "");
        dialog.setTitle("Text Input");
        dialog.setHeaderText(promptMessage);
        dialog.setContentText("Text:");
        dialog.initOwner(primaryStage);
        Optional<String> result = dialog.showAndWait();
        return result.orElse(null); // Restituisce null se l'utente annulla
    }
    
    // Sovraccarico per compatibilità
    public String promptForText(String promptMessage) {
        return promptForText(promptMessage, "");
    }

    public String promptForShapeName(String promptMessage) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Shape Name");
        dialog.setHeaderText(promptMessage);
        dialog.setContentText("Name:");
        dialog.initOwner(primaryStage);
        Optional<String> result = dialog.showAndWait();
        return result.filter(name -> !name.trim().isEmpty()).orElse(null);
    }

    // Metodi per gestire le visuali temporanee, inoltrati al CanvasPanel
    public void drawTemporaryPolygonGuide(List<Point2D> points, Point2D currentMouse) {
        if (canvasPanel != null) {
            canvasPanel.setTemporaryPolygonGuide(points, currentMouse);
            // Il render() sarà chiamato dal controller/update se necessario un redraw completo
        }
    }

    public void drawTemporaryGhostShape(Shape ghostShape) {
        if (canvasPanel != null) {
            canvasPanel.setTemporaryGhostShape(ghostShape);
            // Il render() sarà chiamato dal controller/update
        }
    }

    public void clearTemporaryVisuals() {
        if (canvasPanel != null) {
            canvasPanel.clearTemporaryVisuals();
            // Il render() sarà chiamato dal controller/update
        }
    }
    
    // Mostra un messaggio utente (es. nella status bar o come notifica)
    // Questa responsabilità potrebbe essere di MainApp che osserva GeoEngine,
    // ma DrawingView potrebbe avere un'area dedicata se necessario.
    public void showUserMessage(String message) {
        // Se MainApp gestisce i messaggi tramite l'osservazione di GeoEngine,
        // questo metodo potrebbe non essere chiamato o potrebbe loggare.
        System.out.println("DrawingView UserMessage: " + message);
        // Esempio: se ci fosse un Label in DrawingView: statusLabel.setText(message);
    }

    public void clearUserMessage() {
        System.out.println("DrawingView UserMessage Cleared");
        // Esempio: statusLabel.setText("");
    }
}
