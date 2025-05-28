// File: sad/gruppo11/MainApp.java
package sad.gruppo11;

import sad.gruppo11.Controller.GeoEngine;
import sad.gruppo11.Factory.ShapeFactory;
import sad.gruppo11.Infrastructure.Clipboard;
import sad.gruppo11.Infrastructure.CommandManager;
import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.TextShape;
import sad.gruppo11.Model.LineSegment;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Rect;
// import sad.gruppo11.Model.geometry.Vector2D; // Usato solo da GeoEngine.pasteShape(offset) se lo chiami con offset
import sad.gruppo11.Persistence.DrawingSerializer;
import sad.gruppo11.Persistence.IDrawingSerializer;
import sad.gruppo11.Persistence.PersistenceController;
import sad.gruppo11.View.CanvasPanel;
import sad.gruppo11.View.DrawingView;
import sad.gruppo11.View.Observer;
import sad.gruppo11.Model.Observable;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
// import javafx.scene.control.ToggleButton; // Solo se aggiungi controlli grid/zoom
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent; // Corretto import per KeyEvent

import java.io.File; // Per path.substring
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainApp extends Application implements Observer {

    // --- Sidebar Sinistra ---
    @FXML private Button selectToolButton;
    @FXML private Button lineToolButton;
    @FXML private Button ellipseToolButton;
    @FXML private Button rectangleToolButton;
    @FXML private Button polygonToolButton;
    @FXML private Button textToolButton;

    @FXML private Button undoButton;
    @FXML private Button redoButton;
    @FXML private Button copyButton;
    @FXML private Button pasteButton;
    @FXML private Button cutButton;
    @FXML private Button bringToFrontButton;
    @FXML private Button sendToBackButton;
    @FXML private Button openButton;
    @FXML private Button saveButton;
    @FXML private Button deleteButton;

    @FXML private StackPane canvasHolder;

    // --- Sidebar Destra (Property Panel) ---
    @FXML private TextField shapeNamePropertyField;
    @FXML private TextField shapeXPositionField;
    @FXML private TextField shapeYPositionField;
    @FXML private TextField shapeRotationField;
    // Questi fx:id sono dal tuo mockup per "Scale", ma li useremo per Width e Height
    @FXML private TextField shapeStretchXField;   // Sarà interpretato come Width
    @FXML private TextField shapeStretchYField;   // Sarà interpretato come Height
    @FXML private TextField shapeScaleField;   // Sarà interpretato come Height
    @FXML private ColorPicker fillColorPicker;
    @FXML private ColorPicker strokeColorPicker;

    @FXML private TextField textContentField;
    @FXML private TextField textFontSizeField;
    @FXML private RadioButton gridStatus;
    @FXML private TextField gridSize;
    
    private List<Button> toolButtonsList;
    private GeoEngine geoEngine;
    private DrawingView drawingView;
    private Stage primaryStage;

    private boolean anUIUpdateIsInProgress = false;
    private final double DEFAULT_ZOOM_LEVEL = 5.0; // Zoom iniziale predefinito


    @Override
    public void start(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("GeoDraw - UI Centralizzata!");

        Drawing drawingModel = new Drawing();
        CommandManager commandManager = new CommandManager(drawingModel);
        Clipboard clipboard = Clipboard.getInstance();
        ShapeFactory shapeFactory = new ShapeFactory();
        IDrawingSerializer drawingSerializer = new DrawingSerializer();
        PersistenceController persistenceController = new PersistenceController(drawingSerializer);

        geoEngine = new GeoEngine(drawingModel, commandManager, persistenceController, clipboard, shapeFactory);
        
        geoEngine.attach(this);
        drawingModel.attach(this); 

        String fxmlFile = "mockup.fxml";
        URL fxmlUrl = getClass().getResource(fxmlFile);
        if (fxmlUrl == null) {
            System.err.println("ERRORE FATALE: Impossibile caricare il file FXML: " + fxmlFile + ". Assicurati che sia nel package sad.gruppo11.");
            Platform.exit(); return;
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        loader.setController(this);
        Parent root = loader.load();

        toolButtonsList = new ArrayList<>();
        if (selectToolButton != null) toolButtonsList.add(selectToolButton); else System.err.println("selectToolButton non iniettato da FXML!");
        if (lineToolButton != null)   toolButtonsList.add(lineToolButton); else System.err.println("lineToolButton non iniettato da FXML!");
        if (rectangleToolButton != null) toolButtonsList.add(rectangleToolButton); else System.err.println("rectangleToolButton non iniettato da FXML!");
        if (ellipseToolButton != null)   toolButtonsList.add(ellipseToolButton); else System.err.println("ellipseToolButton non iniettato da FXML!");
        if (polygonToolButton != null)   toolButtonsList.add(polygonToolButton); else System.out.println("WARN: polygonToolButton (fx:id=\"polygonToolButton\") non trovato/iniettato da FXML.");
        if (textToolButton != null)      toolButtonsList.add(textToolButton); else System.out.println("WARN: textToolButton (fx:id=\"textToolButton\") non trovato/iniettato da FXML.");
        
        if(gridStatus != null) {
            gridStatus.setSelected(geoEngine.isGridEnabled());
            gridStatus.setOnAction(e -> geoEngine.setGridEnabled(gridStatus.isSelected()));
        } else {
            System.out.println("WARN: gridStatus (RadioButton con fx:id=\"gridStatus\") non trovato/iniettato da FXML.");
        }

        if (canvasHolder == null) {
            System.err.println("ERRORE FATALE: canvasHolder (StackPane con fx:id=\"canvasHolder\") non è stato iniettato dall'FXML.");
            Platform.exit(); return;
        }
        
        Canvas actualCanvas = new Canvas();
        actualCanvas.widthProperty().bind(canvasHolder.widthProperty());
        actualCanvas.heightProperty().bind(canvasHolder.heightProperty());
        actualCanvas.setId("Canvas");
        canvasHolder.getChildren().add(actualCanvas);
        
        CanvasPanel localCanvasPanel = new CanvasPanel(actualCanvas);
        drawingView = new DrawingView(geoEngine, localCanvasPanel, primaryStage);
        geoEngine.setView(drawingView);
        
        setupButtonActions();
        setupPropertyPanelListeners();
        setupGlobalKeyHandlers(root);
        
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        Platform.runLater(this::refreshUIState); // Chiamata iniziale per impostare lo stato UI

        geoEngine.setZoomLevel(DEFAULT_ZOOM_LEVEL);
    }

    private void refreshUIState() {
        if (anUIUpdateIsInProgress) return;
        anUIUpdateIsInProgress = true;

        if (geoEngine != null) { // Assicura che geoEngine sia inizializzato
            updateActiveToolButtonVisuals(geoEngine.getCurrentToolName());
            updatePropertyPanelContent(geoEngine.getSelectedShape());

            if (undoButton != null) undoButton.setDisable(!geoEngine.canUndo());
            if (redoButton != null) redoButton.setDisable(!geoEngine.canRedo());

            if (pasteButton != null && geoEngine.getClipboard() != null) {
                pasteButton.setDisable(geoEngine.getClipboard().isEmpty());
            }

            boolean shapeIsSelected = (geoEngine.getSelectedShape() != null);
            if(deleteButton != null) deleteButton.setDisable(!shapeIsSelected);
            if(copyButton != null) copyButton.setDisable(!shapeIsSelected);
            if(cutButton != null) cutButton.setDisable(!shapeIsSelected);
            if(bringToFrontButton != null) bringToFrontButton.setDisable(!shapeIsSelected);
            if(sendToBackButton != null) sendToBackButton.setDisable(!shapeIsSelected);
            
            if(shapeScaleField != null)  shapeScaleField.setDisable(!shapeIsSelected);
            if(shapeStretchXField != null) shapeStretchXField.setDisable(!shapeIsSelected);
            if(shapeStretchYField != null) shapeStretchYField.setDisable(!shapeIsSelected);
            if(shapeRotationField != null) shapeRotationField.setDisable(!shapeIsSelected);
            if(textContentField != null) textContentField.setDisable(!shapeIsSelected || !(geoEngine.getSelectedShape() instanceof TextShape));
            if(textFontSizeField != null) textFontSizeField.setDisable(!shapeIsSelected || !(geoEngine.getSelectedShape() instanceof TextShape));
            
            if (strokeColorPicker != null) {
                strokeColorPicker.setDisable(!shapeIsSelected);
                if (shapeIsSelected) {
                    strokeColorPicker.setValue(convertModelToFxColor(geoEngine.getSelectedShape().getStrokeColor()));
                } else {
                    strokeColorPicker.setValue(convertModelToFxColor(geoEngine.getCurrentStrokeColorForNewShapes()));
                }
            }
            if (fillColorPicker != null) {
                fillColorPicker.setDisable(!shapeIsSelected || geoEngine.getSelectedShape() instanceof LineSegment);
                if (shapeIsSelected && !(geoEngine.getSelectedShape() instanceof LineSegment)) {
                    fillColorPicker.setValue(convertModelToFxColor(geoEngine.getSelectedShape().getFillColor()));
                } else if (!shapeIsSelected) {
                    fillColorPicker.setValue(convertModelToFxColor(geoEngine.getCurrentFillColorForNewShapes()));
                } else { // È una linea selezionata
                    fillColorPicker.setValue(Color.TRANSPARENT); // Mostra trasparente per le linee
                }
            }

            if(gridSize != null) {
                gridSize.setDisable(!geoEngine.isGridEnabled());
                gridSize.setText(String.valueOf(geoEngine.getGridSize()));
            }
        }
        
        // Il render della DrawingView è gestito dalla DrawingView stessa quando osserva il Drawing model
        // if (drawingView != null) drawingView.render(); // Potrebbe essere ridondante
        anUIUpdateIsInProgress = false;
    }

    @Override
    public void update(Observable source, Object arg) {
        if (source == geoEngine) {
            if (arg instanceof String) {
                 String message = (String) arg;
                 if (message.contains("Feedback:") || message.contains("Activated:") || message.contains("Deactivated:")) {
                     System.out.println("GEO_ENGINE_UI_MSG: " + message); // Stampa messaggi di feedback
                 }
                 // Il cambio di tool attivo e la selezione sono gestiti da refreshUIState leggendo da geoEngine
            }
        }
        // Indipendentemente dalla fonte o dall'argomento, un refresh completo dell'UI
        // assicura che tutti i componenti siano sincronizzati con lo stato attuale.
        Platform.runLater(this::refreshUIState);
    }

    public GeoEngine getGeoEngine() {
        return geoEngine;
    }
    

    private void setupGlobalKeyHandlers(Parent root){
        root.setOnKeyPressed(event -> {
            boolean consumed = false;
            if(event.getCode() == KeyCode.PLUS || event.getCode() == KeyCode.EQUALS) {
                geoEngine.zoomIn(canvasHolder.getWidth()/2, canvasHolder.getHeight()/2); consumed = true;
            } else if(event.getCode() == KeyCode.MINUS || event.getCode() == KeyCode.UNDERSCORE) {
                geoEngine.zoomOut(canvasHolder.getWidth()/2, canvasHolder.getHeight()/2); consumed = true;
            } else if(event.getCode() == KeyCode.DIGIT0 || event.getCode() == KeyCode.NUMPAD0) {
                geoEngine.setZoomLevel(DEFAULT_ZOOM_LEVEL); consumed = true;
            } else if (event.isControlDown() || event.isShortcutDown()) {
                switch (event.getCode()) {
                    case Z: geoEngine.undoLastCommand(); consumed = true; break;
                    case Y: geoEngine.redoLastCommand(); consumed = true; break;
                    case C: if (geoEngine.getSelectedShape() != null) geoEngine.copySelectedShape(); consumed = true; break;
                    case X: if (geoEngine.getSelectedShape() != null) geoEngine.cutSelectedShape(); consumed = true; break;
                    case V: geoEngine.pasteShape(); consumed = true; break;
                    default: break;
                }
            } else {
                 switch (event.getCode()) {
                    case DELETE: case BACK_SPACE:
                        if (geoEngine.getSelectedShape() != null) geoEngine.removeSelectedShapeFromDrawing();
                        consumed = true; break;
                    case ENTER:
                        if ("PolygonTool".equals(geoEngine.getCurrentToolName())) {
                            geoEngine.attemptToolFinishAction();
                            consumed = true;
                        }
                        break;
                    default: break;
                 }
            }
            if (consumed) {
                // refreshUIState() sarà chiamato tramite il meccanismo Observer se lo stato cambia
                event.consume();
            }
        });
    }

    private void setupButtonActions() {
        // L'aggiornamento UI (es. stato bottoni undo/redo) avverrà tramite Observer e refreshUIState()
        if(selectToolButton != null) selectToolButton.setOnAction(e -> geoEngine.setState("SelectTool"));
        if(lineToolButton != null)   lineToolButton.setOnAction(e -> geoEngine.setState("LineTool"));
        if(rectangleToolButton != null) rectangleToolButton.setOnAction(e -> geoEngine.setState("RectangleTool"));
        if(ellipseToolButton != null)   ellipseToolButton.setOnAction(e -> geoEngine.setState("EllipseTool"));
        if(polygonToolButton != null)   polygonToolButton.setOnAction(e -> geoEngine.setState("PolygonTool"));
        if(textToolButton != null)      textToolButton.setOnAction(e -> geoEngine.setState("TextTool"));

        if(undoButton != null) undoButton.setOnAction(e -> geoEngine.undoLastCommand());
        if(redoButton != null) redoButton.setOnAction(e -> geoEngine.redoLastCommand());
        if(saveButton != null) saveButton.setOnAction(e -> handleSaveAction());
        if(openButton != null) openButton.setOnAction(e -> handleOpenAction());
        if(deleteButton != null) deleteButton.setOnAction(e -> geoEngine.removeSelectedShapeFromDrawing());
        if(copyButton != null) copyButton.setOnAction(e -> geoEngine.copySelectedShape()); // copy notifica geoEngine, che notifica MainApp per refreshUIState (per pasteButton)
        if(pasteButton != null) pasteButton.setOnAction(e -> geoEngine.pasteShape());
        if(cutButton != null) cutButton.setOnAction(e -> geoEngine.cutSelectedShape());
        
        if(bringToFrontButton != null) bringToFrontButton.setOnAction(e -> geoEngine.bringSelectedShapeToFront());
        if(sendToBackButton != null)   sendToBackButton.setOnAction(e -> geoEngine.sendSelectedShapeToBack());
    }
    
    private void handleSaveAction() {
        String path = drawingView.getUserInputForPath(true);
        if (path != null) {
            try {
                geoEngine.saveDrawing(path);
                System.out.println("Drawing saved: " + new File(path).getName());
            } catch (Exception e) {
                drawingView.showError("Failed to save drawing: " + e.getMessage());
            }
        }
    }
    private void handleOpenAction() {
        String path = drawingView.getUserInputForPath(false);
        if (path != null) {
            try {
                geoEngine.loadDrawing(path);
                 System.out.println("Drawing loaded: " + new File(path).getName());
                 // loadDrawing in GeoEngine dovrebbe notificare il modello,
                 // che notifica MainApp, che chiama refreshUIState().
            } catch (Exception e) {
                drawingView.showError("Failed to load drawing: " + e.getMessage());
            }
        }
    }
    
    private void setupPropertyPanelListeners() {
        if (strokeColorPicker != null) {
            strokeColorPicker.setOnAction(event -> {
                if (anUIUpdateIsInProgress) return;
                ColorData newModelColor = convertFxToModelColor(strokeColorPicker.getValue());
                if (geoEngine.getSelectedShape() != null) {
                    geoEngine.changeSelectedShapeStrokeColor(newModelColor);
                } else {
                    geoEngine.setCurrentStrokeColorForNewShapes(newModelColor);
                    // Aggiorna il picker per riflettere il default, se la selezione è nulla
                    Platform.runLater(() -> {
                        if (geoEngine.getSelectedShape() == null) { // Ricontrolla nel caso sia cambiato
                           anUIUpdateIsInProgress = true;
                           strokeColorPicker.setValue(convertModelToFxColor(geoEngine.getCurrentStrokeColorForNewShapes()));
                           anUIUpdateIsInProgress = false;
                        }
                    });
                }
            });
        }

        if (fillColorPicker != null) {
            fillColorPicker.setOnAction(event -> {
                if (anUIUpdateIsInProgress) return;
                ColorData newModelColor = convertFxToModelColor(fillColorPicker.getValue());
                Shape selected = geoEngine.getSelectedShape();
                if (selected != null && !(selected instanceof LineSegment)) { 
                    geoEngine.changeSelectedShapeFillColor(newModelColor);
                } else if (selected == null) {
                    geoEngine.setCurrentFillColorForNewShapes(newModelColor);
                    Platform.runLater(() -> {
                        if (geoEngine.getSelectedShape() == null) {
                           anUIUpdateIsInProgress = true;
                           fillColorPicker.setValue(convertModelToFxColor(geoEngine.getCurrentFillColorForNewShapes()));
                           anUIUpdateIsInProgress = false;
                        }
                    });
                }
            });
        }

        if (shapeRotationField != null) {
            shapeRotationField.setOnAction(event -> { if (!anUIUpdateIsInProgress) applyRotationFromField();});
            shapeRotationField.focusedProperty().addListener((obs, o, n) -> { if(!n && !anUIUpdateIsInProgress) applyRotationFromField(); });
        }

        if (shapeStretchXField != null) { // Width
            shapeStretchXField.setOnAction(event -> { if (!anUIUpdateIsInProgress) applyStretchChangeFromFields();});
            shapeStretchXField.focusedProperty().addListener((obs, o, n) -> { if(!n && !anUIUpdateIsInProgress) applyStretchChangeFromFields(); });
        }
        if (shapeStretchYField != null) { // Height
            shapeStretchYField.setOnAction(event -> { if (!anUIUpdateIsInProgress) applyStretchChangeFromFields();});
            shapeStretchYField.focusedProperty().addListener((obs, o, n) -> { if(!n && !anUIUpdateIsInProgress) applyStretchChangeFromFields(); });
        }
        if(shapeScaleField != null) { // Non usato nel mockup, ma lo lasciamo per compatibilità
            shapeScaleField.setOnAction(event -> { if (!anUIUpdateIsInProgress) applyScaleChangeFromFields();});
            shapeScaleField.focusedProperty().addListener((obs, o, n) -> { if(!n && !anUIUpdateIsInProgress) applyScaleChangeFromFields(); });
        }
        if (textContentField != null) {
            textContentField.setOnAction(event -> {if (!anUIUpdateIsInProgress) applyTextContentChange();});
            textContentField.focusedProperty().addListener((obs, o, n) -> { if(!n && !anUIUpdateIsInProgress) applyTextContentChange(); });
        }

        if (textFontSizeField != null) {
            textFontSizeField.setOnAction(event -> {if (!anUIUpdateIsInProgress) applyTextFontSizeChange();});
            textFontSizeField.focusedProperty().addListener((obs, o, n) -> { if(!n && !anUIUpdateIsInProgress) applyTextFontSizeChange(); });
        }

        if (gridSize != null) {
            gridSize.setOnAction(event -> {
                if (anUIUpdateIsInProgress) return;
                try {
                    double newSize = Double.parseDouble(gridSize.getText());
                    if (newSize > 0) {
                        geoEngine.setGridSize(newSize);
                    } else {
                        throw new NumberFormatException("Grid size must be positive.");
                    }
                } catch (NumberFormatException e) {
                    // Se il formato non è valido, ripristina il valore corrente
                    gridSize.setText(String.valueOf(geoEngine.getGridSize()));
                }
            });
            gridSize.focusedProperty().addListener((obs, o, n) -> { if(!n && !anUIUpdateIsInProgress) geoEngine.setGridSize(Double.parseDouble(gridSize.getText())); });
        }
    }
        
    private void applyRotationFromField() { 
        if (geoEngine.getSelectedShape() == null || shapeRotationField == null) return;
        try {
            double newRotation = Double.parseDouble(shapeRotationField.getText());
            geoEngine.rotateSelectedShape(newRotation); 
        } catch (NumberFormatException e) { /* refreshUIState() ripristinerà */ }
    }

    private void applyStretchChangeFromFields() {
        if (geoEngine.getSelectedShape() == null || shapeStretchXField == null || shapeStretchYField == null) return;
        Shape selected = geoEngine.getSelectedShape();
        try {
            Point2D center;
            double newWidth;
            double newHeight;
            Rect newUnrotatedBounds;
            
            newWidth = Double.parseDouble(shapeStretchXField.getText()); 
            newHeight = Double.parseDouble(shapeStretchYField.getText());
            if(newHeight != selected.getBounds().getHeight() || newWidth != selected.getBounds().getWidth()) {
                if (newWidth <= 0 || newHeight <= 0) return;
                
                center = selected.getBounds().getCenter();
                newUnrotatedBounds = new Rect(
                    new Point2D(center.getX() - newWidth / 2.0, center.getY() - newHeight / 2.0),
                    newWidth, newHeight
                );
                geoEngine.resizeSelectedShape(newUnrotatedBounds);
            }
        } catch (NumberFormatException e) { /* refreshUIState() ripristinerà */ }
    }

     private void applyScaleChangeFromFields() {
        if (geoEngine.getSelectedShape() == null || shapeScaleField == null) return;
        Shape selected = geoEngine.getSelectedShape();
        try {
            Point2D center;
            double newWidth;
            double newHeight;
            Rect newUnrotatedBounds;
            double newScale = Double.parseDouble(shapeScaleField.getText());
            if(newScale != 1.0){
                if (newScale <= 0) return; // Non permettere scale non positive

                center = selected.getBounds().getCenter();
                newWidth = selected.getBounds().getWidth() * newScale; 
                newHeight = selected.getBounds().getHeight() * newScale;
                newUnrotatedBounds = new Rect(
                    new Point2D(
                        center.getX() - newWidth / 2.0, 
                        center.getY() - newHeight / 2.0),
                    newWidth, newHeight
                );
                geoEngine.resizeSelectedShape(newUnrotatedBounds);
            }
        } catch (NumberFormatException e) { /* refreshUIState() ripristinerà */ }
    }

    private void applyTextContentChange() { 
        if (!(geoEngine.getSelectedShape() instanceof TextShape) || textContentField == null) return;
        TextShape selectedText = (TextShape) geoEngine.getSelectedShape();
        String newText = textContentField.getText();
        if (!selectedText.getText().equals(newText)) {
            geoEngine.changeSelectedTextContent(newText);
            geoEngine.getDrawing().notifyObservers(new Drawing.DrawingChangeEvent(selectedText, Drawing.DrawingChangeEvent.ChangeType.MODIFY));
        }
    }
    private void applyTextFontSizeChange() { 
        if (!(geoEngine.getSelectedShape() instanceof TextShape) || textFontSizeField == null) return;
        try {
            double newSize = Double.parseDouble(textFontSizeField.getText());
            if (newSize > 0) {
                geoEngine.changeSelectedTextSize(newSize);
            }
        } catch (NumberFormatException e) { /* refreshUIState() ripristinerà */ }
    }

    private Color convertModelToFxColor(ColorData modelColor) { 
        if (modelColor == null) return Color.TRANSPARENT;
        return Color.rgb(modelColor.getR(), modelColor.getG(), modelColor.getB(), modelColor.getA());
    }
    private ColorData convertFxToModelColor(Color fxColor) {
        if (fxColor == null) return ColorData.TRANSPARENT;
         return new ColorData(
                (int) (fxColor.getRed() * 255), (int) (fxColor.getGreen() * 255),
                (int) (fxColor.getBlue() * 255), fxColor.getOpacity()
        );
    }
 
    private void updatePropertyPanelContent(Shape selectedShape) {
        boolean shapeIsSelected = (selectedShape != null);
        boolean isText = (selectedShape instanceof TextShape);
        boolean isLine = (selectedShape instanceof LineSegment);

        if (shapeNamePropertyField != null) shapeNamePropertyField.setText(shapeIsSelected ? selectedShape.getClass().getSimpleName() : "Nessuna Selezione");
        
        Rect bounds = shapeIsSelected ? selectedShape.getBounds() : null; 
        if (shapeXPositionField != null) shapeXPositionField.setText(shapeIsSelected ? String.format("%.0f", bounds.getX()) : "");
        if (shapeYPositionField != null) shapeYPositionField.setText(shapeIsSelected ? String.format("%.0f", bounds.getY()) : "");
        
        if (shapeStretchXField != null) shapeStretchXField.setText(shapeIsSelected ? String.format("%.1f", bounds.getWidth()) : "");
        if (shapeStretchYField != null) shapeStretchYField.setText(shapeIsSelected ? String.format("%.1f", bounds.getHeight()) : "");

        if (shapeScaleField != null) shapeScaleField.setText(String.format("%.1f", 1.0));

        if (shapeRotationField != null) shapeRotationField.setText(shapeIsSelected ? String.format("%.1f", selectedShape.getRotation()) : "0.0");
        
        if (strokeColorPicker != null) strokeColorPicker.setValue(shapeIsSelected ? convertModelToFxColor(selectedShape.getStrokeColor()) : convertModelToFxColor(geoEngine.getCurrentStrokeColorForNewShapes()));
        if (fillColorPicker != null) {
            fillColorPicker.setDisable(isLine); // Le linee non hanno fill
            if (shapeIsSelected && !isLine) {
                fillColorPicker.setValue(convertModelToFxColor(selectedShape.getFillColor()));
            } else if (!shapeIsSelected) { // Nessuna selezione, mostra default
                fillColorPicker.setValue(convertModelToFxColor(geoEngine.getCurrentFillColorForNewShapes()));
            } else { // È una linea selezionata
                fillColorPicker.setValue(Color.TRANSPARENT); // Mostra trasparente per le linee
            }
        }

        if (textContentField != null) {
            textContentField.setDisable(!isText);
            textContentField.setText(isText ? ((TextShape) selectedShape).getText() : "");
        }
        if (textFontSizeField != null) {
            textFontSizeField.setDisable(!isText);
            textFontSizeField.setText(isText ? String.format("%.1f", ((TextShape) selectedShape).getFontSize()) : "");
        }
        
        // L'abilitazione/disabilitazione dei campi che dipendono dalla selezione
        // è gestita in refreshUIState() per centralizzazione.
        // Qui impostiamo solo i valori.
    }

    private void updateActiveToolButtonVisuals(String activeToolName) {
        String inactiveStyle = "-fx-background-color: #e0e0e0; -fx-background-radius: 15;"; 
        String activeStyle = "-fx-background-color: #c0c0c0; -fx-background-radius: 15; -fx-border-color: #707070; -fx-border-width: 1.5px;";
        // Lo stile di default per selectToolButton quando è attivo (dal tuo FXML originale)
        String selectToolActiveStyle = "-fx-background-color: #d0d0d0; -fx-background-radius: 15; -fx-border-color: #707070; -fx-border-width: 1.5px;";


        for (Button button : toolButtonsList) {
            if (button != null) {
                // Se il bottone è selectToolButton, usa lo stile inattivo base, altrimenti activeStyle lo sovrascriverà se è il tool corrente
                if (button == selectToolButton) {
                    button.setStyle(selectToolActiveStyle.replace("#d0d0d0", "#e0e0e0").replace("-fx-border-color: #707070; -fx-border-width: 1.5px;", "")); // Stile base per select
                } else {
                    button.setStyle(inactiveStyle); 
                }
            }
        }

        Button currentActiveButton = null;
        if (activeToolName != null) {
            switch (activeToolName) {
                case "SelectTool": currentActiveButton = selectToolButton; break;
                case "LineTool": currentActiveButton = lineToolButton; break;
                case "RectangleTool": currentActiveButton = rectangleToolButton; break;
                case "EllipseTool": currentActiveButton = ellipseToolButton; break;
                case "PolygonTool": currentActiveButton = polygonToolButton; break;
                case "TextTool": currentActiveButton = textToolButton; break;
            }
        }
        
        if (currentActiveButton != null) {
            if (currentActiveButton == selectToolButton) {
                currentActiveButton.setStyle(selectToolActiveStyle);
            } else {
                currentActiveButton.setStyle(activeStyle);
            }
        } else if (selectToolButton != null) { // Fallback se nessun tool attivo
             selectToolButton.setStyle(selectToolActiveStyle);
        }
    }
        
    public static void main(String[] args) {
        launch(args);
    }
}