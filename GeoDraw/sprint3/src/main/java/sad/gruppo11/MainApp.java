// File: sad/gruppo11/MainApp.java
package sad.gruppo11;

import sad.gruppo11.Controller.GeoEngine;
import sad.gruppo11.Controller.ReusableShapeDefinition; // Per la ListView
import sad.gruppo11.Factory.ShapeFactory;
import sad.gruppo11.Infrastructure.Clipboard;
import sad.gruppo11.Infrastructure.CommandManager;
import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.GroupShape; // Per controllare se la selezione è un gruppo
import sad.gruppo11.Model.TextShape;
import sad.gruppo11.Model.LineSegment;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Rect;
import sad.gruppo11.Persistence.DrawingSerializer;
import sad.gruppo11.Persistence.IDrawingSerializer;
import sad.gruppo11.Persistence.IReusableShapeLibrarySerializer; 
import sad.gruppo11.Persistence.PersistenceController;
import sad.gruppo11.Persistence.ReusableShapeLibrarySerializer; 
import sad.gruppo11.View.CanvasPanel;
import sad.gruppo11.View.DrawingView;
import sad.gruppo11.View.Observer;
import sad.gruppo11.Model.Observable;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label; 
import javafx.scene.control.ListView; 
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog; 
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser; 
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination; 
import javafx.scene.input.KeyEvent; 

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class MainApp extends Application implements Observer {

    // --- Sidebar Sinistra (Esistenti) ---
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
    
    // --- Nuovi pulsanti per Sprint 3 (da aggiungere a FXML) ---
    @FXML private Button reflectHorizontalButton;
    @FXML private Button reflectVerticalButton;
    @FXML private Button groupButton;
    @FXML private Button ungroupButton;
    @FXML private Button saveAsReusableButton;
    @FXML private Button removeReusableButton;
    @FXML private ListView<String> reusableShapesListView; 
    @FXML private Button placeReusableButton;
    @FXML private Button exportReusableLibraryButton; // Per US 33
    @FXML private Button importReusableLibraryButton; // Per US 34


    @FXML private StackPane canvasHolder;

    // --- Sidebar Destra (Property Panel Esistenti) ---
    @FXML private TextField shapeNamePropertyField;
    @FXML private TextField shapeXPositionField;
    @FXML private TextField shapeYPositionField;
    @FXML private TextField shapeRotationField;
    @FXML private TextField shapeStretchXField;  
    @FXML private TextField shapeStretchYField;  
    @FXML private TextField shapeScaleField;   
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
    private final double DEFAULT_ZOOM_LEVEL = 2.0; 


    @Override
    public void start(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("GeoDraw - Sprint 3");

        Drawing drawingModel = new Drawing();
        CommandManager commandManager = new CommandManager(drawingModel);
        Clipboard clipboard = Clipboard.getInstance();
        ShapeFactory shapeFactory = new ShapeFactory();
        
        IDrawingSerializer drawingSerializer = new DrawingSerializer();
        IReusableShapeLibrarySerializer librarySerializer = new ReusableShapeLibrarySerializer(); 
        PersistenceController persistenceController = new PersistenceController(drawingSerializer, librarySerializer); 

        geoEngine = new GeoEngine(drawingModel, commandManager, persistenceController, clipboard, shapeFactory);
        
        geoEngine.attach(this); 
        drawingModel.attach(this); 

        String fxmlFile = "mockup.fxml"; 
        URL fxmlUrl = getClass().getResource(fxmlFile);
        if (fxmlUrl == null) {
            System.err.println("ERRORE FATALE: Impossibile caricare il file FXML: " + fxmlFile + ". Assicurati che sia nel classpath e il percorso sia corretto (es. /sad/gruppo11/mockup.fxml se è nella stessa cartella di MainApp.class).");
            Platform.exit(); return;
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        loader.setController(this); 
        Parent root = loader.load();

        toolButtonsList = new ArrayList<>();
        if (selectToolButton != null) toolButtonsList.add(selectToolButton); else System.err.println("WARN: selectToolButton non iniettato da FXML!");
        if (lineToolButton != null)   toolButtonsList.add(lineToolButton); else System.err.println("WARN: lineToolButton non iniettato da FXML!");
        if (rectangleToolButton != null) toolButtonsList.add(rectangleToolButton); else System.err.println("WARN: rectangleToolButton non iniettato da FXML!");
        if (ellipseToolButton != null)   toolButtonsList.add(ellipseToolButton); else System.err.println("WARN: ellipseToolButton non iniettato da FXML!");
        if (polygonToolButton != null)   toolButtonsList.add(polygonToolButton); else System.out.println("INFO: polygonToolButton (fx:id=\"polygonToolButton\") non trovato/iniettato da FXML.");
        if (textToolButton != null)      toolButtonsList.add(textToolButton); else System.out.println("INFO: textToolButton (fx:id=\"textToolButton\") non trovato/iniettato da FXML.");

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
        actualCanvas.setId("drawingCanvas"); 
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
        
        Platform.runLater(this::refreshUIState); 
        geoEngine.setZoomLevel(DEFAULT_ZOOM_LEVEL);
        updateReusableShapesListView(); 
    }

    private void refreshUIState() {
        if (anUIUpdateIsInProgress) return;
        anUIUpdateIsInProgress = true;

        if (geoEngine == null) {
            anUIUpdateIsInProgress = false;
            return;
        }
        
        updateActiveToolButtonVisuals(geoEngine.getCurrentToolName());
        
        if (undoButton != null) undoButton.setDisable(!geoEngine.canUndo());
        if (redoButton != null) redoButton.setDisable(!geoEngine.canRedo());

        if (pasteButton != null && geoEngine.getClipboard() != null) {
            pasteButton.setDisable(geoEngine.getClipboard().isEmpty());
        }

        List<Shape> selectedShapes = geoEngine.getSelectedShapes();
        boolean anyShapeSelected = !selectedShapes.isEmpty();
        boolean singleShapeSelected = selectedShapes.size() == 1;
        boolean multipleShapesSelected = selectedShapes.size() >= 2;
        Shape primarySelected = singleShapeSelected ? selectedShapes.get(0) : null;

        if(deleteButton != null) deleteButton.setDisable(!anyShapeSelected);
        if(copyButton != null) copyButton.setDisable(!anyShapeSelected); 
        if(cutButton != null) cutButton.setDisable(!anyShapeSelected);   
        
        if(bringToFrontButton != null) bringToFrontButton.setDisable(!singleShapeSelected);
        if(sendToBackButton != null) sendToBackButton.setDisable(!singleShapeSelected);   

        if(reflectHorizontalButton != null) reflectHorizontalButton.setDisable(!anyShapeSelected);
        if(reflectVerticalButton != null) reflectVerticalButton.setDisable(!anyShapeSelected);
        if(groupButton != null) groupButton.setDisable(!multipleShapesSelected); 
        if(ungroupButton != null) ungroupButton.setDisable(!(singleShapeSelected && primarySelected instanceof GroupShape));
        if(saveAsReusableButton != null) saveAsReusableButton.setDisable(!singleShapeSelected);
        
        if(removeReusableButton != null) removeReusableButton.setDisable(reusableShapesListView.getSelectionModel().isEmpty());

        boolean propertyPanelEnabled = singleShapeSelected;
        if(shapeNamePropertyField != null) shapeNamePropertyField.setDisable(!propertyPanelEnabled);
        if(shapeXPositionField != null) shapeXPositionField.setDisable(!propertyPanelEnabled);
        if(shapeYPositionField != null) shapeYPositionField.setDisable(!propertyPanelEnabled);
        if(shapeStretchXField != null) shapeStretchXField.setDisable(!propertyPanelEnabled);
        if(shapeStretchYField != null) shapeStretchYField.setDisable(!propertyPanelEnabled);
        if(shapeScaleField != null) shapeScaleField.setDisable(!propertyPanelEnabled);
        if(shapeRotationField != null) shapeRotationField.setDisable(!propertyPanelEnabled);
        if(textContentField != null) textContentField.setDisable(!(propertyPanelEnabled && primarySelected instanceof TextShape));
        if(textFontSizeField != null) textFontSizeField.setDisable(!(propertyPanelEnabled && primarySelected instanceof TextShape));
            
        if (strokeColorPicker != null) {
            strokeColorPicker.setDisable(false); // Sempre abilitato per cambiare il default o la forma
            if (propertyPanelEnabled) {
                strokeColorPicker.setValue(convertModelToFxColor(primarySelected.getStrokeColor()));
            } else { 
                strokeColorPicker.setValue(convertModelToFxColor(geoEngine.getCurrentStrokeColorForNewShapes()));
            }
        }
        if (fillColorPicker != null) {
            fillColorPicker.setDisable(propertyPanelEnabled && (primarySelected instanceof LineSegment)); 
            if (propertyPanelEnabled && !(primarySelected instanceof LineSegment)) {
                fillColorPicker.setValue(convertModelToFxColor(primarySelected.getFillColor()));
            } else if (!propertyPanelEnabled) {
                 fillColorPicker.setDisable(false);
                 fillColorPicker.setValue(convertModelToFxColor(geoEngine.getCurrentFillColorForNewShapes()));
            } else { 
                 fillColorPicker.setValue(Color.TRANSPARENT);
            }
        }

        
        updatePropertyPanelContent(primarySelected); 

        if(gridStatus != null) gridStatus.setSelected(geoEngine.isGridEnabled()); 
        if(gridSize != null) {
            gridSize.setDisable(!geoEngine.isGridEnabled());
            if (geoEngine.isGridEnabled() && !gridSize.isFocused()) { 
                 gridSize.setText(String.format("%.1f", geoEngine.getGridSize()));
            }
        }
        
        if (reusableShapesListView != null) {
            String selectedReusableItem = reusableShapesListView.getSelectionModel().getSelectedItem();
            // Non chiamare updateReusableShapesListView() qui direttamente per evitare cicli,
            // viene chiamato da update() quando la libreria cambia o all'inizio.
            // Solo lo stato del pulsante placeReusableButton dipende dalla selezione corrente nella lista.
             if(placeReusableButton != null) {
                placeReusableButton.setDisable(selectedReusableItem == null);
            }
        }

        if (exportReusableLibraryButton != null)
            exportReusableLibraryButton.setDisable(geoEngine.getReusableShapeDefinitions().isEmpty());


        anUIUpdateIsInProgress = false;
    }

    @Override
    public void update(Observable source, Object arg) {
        Platform.runLater(() -> {
            if (source == geoEngine) {
                if ("ReusableLibraryChanged".equals(arg) || 
                    (arg instanceof Drawing.DrawingChangeEvent && ((Drawing.DrawingChangeEvent)arg).type == Drawing.DrawingChangeEvent.ChangeType.LOAD)) {
                    updateReusableShapesListView();
                }
            }
            refreshUIState(); // Un refresh generale sincronizza tutto
        });
    }
    
    private void setupGlobalKeyHandlers(Parent root){
        root.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.SHIFT) {
                if (canvasHolder != null) canvasHolder.setUserData(Boolean.TRUE);
            }

            boolean consumed = false;
            KeyCombination shortcutY = KeyCombination.valueOf("Shortcut+Y");
            KeyCombination shortcutZ = KeyCombination.valueOf("Shortcut+Z");
            KeyCombination shortcutC = KeyCombination.valueOf("Shortcut+C");
            KeyCombination shortcutX = KeyCombination.valueOf("Shortcut+X");
            KeyCombination shortcutV = KeyCombination.valueOf("Shortcut+V");
            KeyCombination shortcutG = KeyCombination.valueOf("Shortcut+G");
            KeyCombination shortcutShiftG = KeyCombination.valueOf("Shortcut+Shift+G");

            geoEngine.setIsShiftKeyPressed(event.getCode() == KeyCode.SHIFT);
            if(event.getCode() == KeyCode.SHIFT)
            {
                consumed = true;
            } else if (shortcutY.match(event)) {
                geoEngine.redoLastCommand(); consumed = true;
            } else if (shortcutZ.match(event)) {
                geoEngine.undoLastCommand(); consumed = true;
            } else if (shortcutC.match(event)) {
                if (!geoEngine.getSelectedShapes().isEmpty()) geoEngine.copySelectedShape(); consumed = true;
            } else if (shortcutX.match(event)) {
                if (!geoEngine.getSelectedShapes().isEmpty()) geoEngine.cutSelectedShape(); consumed = true;
            } else if (shortcutV.match(event)) {
                geoEngine.pasteShape(); consumed = true;
            } else if (shortcutShiftG.match(event)) {
                if (geoEngine.getSelectedShape() instanceof GroupShape) geoEngine.ungroupSelectedShape();
                consumed = true;
            } else if (shortcutG.match(event)) {
                 if (geoEngine.getSelectedShapes().size() >=2) geoEngine.groupSelectedShapes();
                 consumed = true;
            } else { 
                 switch (event.getCode()) {
                    case DELETE: case BACK_SPACE:
                        if (!geoEngine.getSelectedShapes().isEmpty()) geoEngine.removeSelectedShapesFromDrawing();
                        consumed = true; break;
                    case ENTER:
                        if ("PolygonTool".equals(geoEngine.getCurrentToolName())) {
                            geoEngine.attemptToolFinishAction();
                            consumed = true;
                        }
                        break;
                    case ADD: case EQUALS: 
                         geoEngine.zoomIn(canvasHolder.getWidth()/2, canvasHolder.getHeight()/2); consumed = true; break;
                    case SUBTRACT: case MINUS:
                         geoEngine.zoomOut(canvasHolder.getWidth()/2, canvasHolder.getHeight()/2); consumed = true; break;
                    case DIGIT0: case NUMPAD0: 
                         geoEngine.setZoomLevel(DEFAULT_ZOOM_LEVEL); consumed = true; break;
                    default: break;
                 }
            }
            if (consumed) {
                event.consume();
                refreshUIState(); 
            }
        });

        root.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.SHIFT) {
                geoEngine.setIsShiftKeyPressed(false);
            }
        });
    }

    private void setupButtonActions() {
        if(selectToolButton != null) selectToolButton.setOnAction(e -> geoEngine.setState("SelectTool"));
        if(lineToolButton != null)   lineToolButton.setOnAction(e -> geoEngine.setState("LineTool"));
        if(rectangleToolButton != null) rectangleToolButton.setOnAction(e -> geoEngine.setState("RectangleTool"));
        if(ellipseToolButton != null)   ellipseToolButton.setOnAction(e -> geoEngine.setState("EllipseTool"));
        if(polygonToolButton != null)   polygonToolButton.setOnAction(e -> geoEngine.setState("PolygonTool"));
        if(textToolButton != null)      textToolButton.setOnAction(e -> geoEngine.setState("TextTool"));

        if(undoButton != null) undoButton.setOnAction(e -> { geoEngine.undoLastCommand(); /*refreshUIState();*/ }); // refreshUIState chiamato da update
        if(redoButton != null) redoButton.setOnAction(e -> { geoEngine.redoLastCommand(); /*refreshUIState();*/ });
        if(saveButton != null) saveButton.setOnAction(e -> handleSaveAction());
        if(openButton != null) openButton.setOnAction(e -> handleOpenAction());
        if(deleteButton != null) deleteButton.setOnAction(e -> { geoEngine.removeSelectedShapesFromDrawing(); /*refreshUIState();*/ });
        if(copyButton != null) copyButton.setOnAction(e -> { geoEngine.copySelectedShape(); /*refreshUIState();*/ });
        if(pasteButton != null) pasteButton.setOnAction(e -> { geoEngine.pasteShape(); /*refreshUIState();*/ });
        if(cutButton != null) cutButton.setOnAction(e -> { geoEngine.cutSelectedShape(); /*refreshUIState();*/ });
        
        if(bringToFrontButton != null) bringToFrontButton.setOnAction(e -> { geoEngine.bringSelectedShapeToFront(); /*refreshUIState();*/ });
        if(sendToBackButton != null)   sendToBackButton.setOnAction(e -> { geoEngine.sendSelectedShapeToBack(); /*refreshUIState();*/ });

        if (reflectHorizontalButton != null) {
            reflectHorizontalButton.setOnAction(e -> {
                geoEngine.reflectSelectedShapesHorizontal(); /*refreshUIState();*/
            });
        } else System.err.println("WARN: reflectHorizontalButton non iniettato.");
        
        if (reflectVerticalButton != null) {
            reflectVerticalButton.setOnAction(e -> {
                geoEngine.reflectSelectedShapesVertical(); /*refreshUIState();*/
            });
        } else System.err.println("WARN: reflectVerticalButton non iniettato.");
        
        if (groupButton != null) {
            groupButton.setOnAction(e -> {
                geoEngine.groupSelectedShapes(); /*refreshUIState();*/
            });
        } else System.err.println("WARN: groupButton non iniettato.");
        
        if (ungroupButton != null) {
            ungroupButton.setOnAction(e -> {
                geoEngine.ungroupSelectedShape(); /*refreshUIState();*/
            });
        } else System.err.println("WARN: ungroupButton non iniettato.");
        
        if (saveAsReusableButton != null) {
            saveAsReusableButton.setOnAction(e -> handleSaveAsReusableAction());
        } else System.err.println("WARN: saveAsReusableButton non iniettato.");

        if (removeReusableButton != null) {
            removeReusableButton.setOnAction(e -> handleDeleteReusableAction());
        } else System.err.println("WARN: removeReusableButton non iniettato.");
        
        if (placeReusableButton != null) {
            placeReusableButton.setOnAction(e -> handlePlaceReusableAction());
        } else System.err.println("WARN: placeReusableButton non iniettato.");

        if (exportReusableLibraryButton != null) {
            exportReusableLibraryButton.setOnAction(e -> handleExportReusableLibraryAction());
        } else System.err.println("WARN: exportReusableLibraryButton non iniettato.");

        if (importReusableLibraryButton != null) {
            importReusableLibraryButton.setOnAction(e -> handleImportReusableLibraryAction());
        } else System.err.println("WARN: importReusableLibraryButton non iniettato.");
        
        if (reusableShapesListView != null) {
            reusableShapesListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                 refreshUIState(); // Per abilitare/disabilitare placeReusableButton
            });
        } else System.err.println("WARN: reusableShapesListView non iniettato.");
    }
    
    private void handleSaveAction() {
        String path = drawingView.getUserInputForPath(true, "Save Drawing As", "drawing.ser");
        if (path != null) {
            try {
                geoEngine.saveDrawing(path);
                primaryStage.setTitle("GeoDraw - " + new File(path).getName());
            } catch (Exception e) {
                drawingView.showError("Failed to save drawing: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    private void handleOpenAction() {
        String path = drawingView.getUserInputForPath(false, "Open Drawing", null);
        if (path != null) {
            try {
                geoEngine.loadDrawing(path);
                 primaryStage.setTitle("GeoDraw - " + new File(path).getName());
                 // La notifica da geoEngine e drawingModel dovrebbe causare refreshUIState
            } catch (Exception e) {
                drawingView.showError("Failed to load drawing: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private void setupPropertyPanelListeners() {
        //Stroke Color
        if (strokeColorPicker != null) {
            strokeColorPicker.setOnAction(event -> {
                if (anUIUpdateIsInProgress) return;
                ColorData newModelColor = convertFxToModelColor(strokeColorPicker.getValue());
                if (!geoEngine.getSelectedShapes().isEmpty()) { // Se c'è una selezione
                    geoEngine.changeSelectedShapeStrokeColor(newModelColor); // Agisce sulla selezione primaria
                } else { // Altrimenti imposta il default per nuove forme
                    geoEngine.setCurrentStrokeColorForNewShapes(newModelColor);
                }
                // refreshUIState() non è necessario qui se ChangeStrokeColorCommand notifica il modello
                // e l'update di MainApp chiama refreshUIState. Ma per il cambio default, forziamo.
                Platform.runLater(this::refreshUIState);
            });
        }

        //Fill Color
        if (fillColorPicker != null) {
            fillColorPicker.setOnAction(event -> {
                if (anUIUpdateIsInProgress) return;
                ColorData newModelColor = convertFxToModelColor(fillColorPicker.getValue());
                Shape primarySelected = geoEngine.getSelectedShape();
                if (primarySelected != null && !(primarySelected instanceof LineSegment)) { 
                    geoEngine.changeSelectedShapeFillColor(newModelColor);
                } else if (geoEngine.getSelectedShapes().isEmpty()) { // Nessuna selezione, imposta default
                    geoEngine.setCurrentFillColorForNewShapes(newModelColor);
                }
                Platform.runLater(this::refreshUIState);
            });
        }

        // Rotation
        if (shapeRotationField != null) {
            shapeRotationField.setOnAction(event -> { if (!anUIUpdateIsInProgress) applyRotationFromField();});
            shapeRotationField.focusedProperty().addListener((obs, oldVal, newVal) -> { 
                if (!newVal && !anUIUpdateIsInProgress) applyRotationFromField(); // Applica quando perde il focus
            });
        }

        // Width (shapeStretchXField)
        if (shapeStretchXField != null) { 
            shapeStretchXField.setOnAction(event -> { if (!anUIUpdateIsInProgress) applySizeChangeFromFields();});
            shapeStretchXField.focusedProperty().addListener((obs, o, n) -> { if(!n && !anUIUpdateIsInProgress) applySizeChangeFromFields(); });
        }
        // Height (shapeStretchYField)
        if (shapeStretchYField != null) { 
            shapeStretchYField.setOnAction(event -> { if (!anUIUpdateIsInProgress) applySizeChangeFromFields();});
            shapeStretchYField.focusedProperty().addListener((obs, o, n) -> { if(!n && !anUIUpdateIsInProgress) applySizeChangeFromFields(); });
        }
        // Scale (shapeScaleField) - Se vuoi usarlo per scalare proporzionalmente
        if(shapeScaleField != null) { 
            shapeScaleField.setOnAction(event -> { if (!anUIUpdateIsInProgress) applyProportionalScaleFromField();});
            shapeScaleField.focusedProperty().addListener((obs, o, n) -> { if(!n && !anUIUpdateIsInProgress) applyProportionalScaleFromField(); });
        }

        // Text Content
        if (textContentField != null) {
            textContentField.setOnAction(event -> {if (!anUIUpdateIsInProgress) applyTextContentChange();});
            textContentField.focusedProperty().addListener((obs, o, n) -> { if(!n && !anUIUpdateIsInProgress) applyTextContentChange(); });
        }

        // Text Font Size
        if (textFontSizeField != null) {
            textFontSizeField.setOnAction(event -> {if (!anUIUpdateIsInProgress) applyTextFontSizeChange();});
            textFontSizeField.focusedProperty().addListener((obs, o, n) -> { if(!n && !anUIUpdateIsInProgress) applyTextFontSizeChange(); });
        }

        // Grid Size
        if (gridSize != null) {
            gridSize.setOnAction(event -> {
                if (anUIUpdateIsInProgress) return;
                tryApplyGridSize();
            });
            gridSize.focusedProperty().addListener((obs, o, n) -> { 
                if (!n && !anUIUpdateIsInProgress) tryApplyGridSize();
            });
        }
    }
    
    private void tryApplyGridSize() {
        if (gridSize == null) return;
        try {
            double newSizeValue = Double.parseDouble(gridSize.getText());
            geoEngine.setGridSize(newSizeValue); // GeoEngine gestirà la validazione (es. > 0)
        } catch (NumberFormatException e) {
            // Valore non valido, ripristina dal geoEngine
            gridSize.setText(String.format("%.1f", geoEngine.getGridSize()));
        }
        // refreshUIState() sarà chiamato se GeoEngine notifica un cambiamento
    }
        
    private void applyRotationFromField() { 
        if (geoEngine.getSelectedShape() == null || shapeRotationField == null) return;
        try {
            double newRotation = Double.parseDouble(shapeRotationField.getText());
            geoEngine.rotateSelectedShape(newRotation); 
        } catch (NumberFormatException e) { 
            // Se il formato non è valido, refreshUIState ripristinerà il valore corretto
            Platform.runLater(this::refreshUIState);
        }
    }

    private void applySizeChangeFromFields() { // Per Width e Height
        Shape selected = geoEngine.getSelectedShape();
        if (selected == null || shapeStretchXField == null || shapeStretchYField == null) return;
        try {
            double newWidth = Double.parseDouble(shapeStretchXField.getText()); 
            double newHeight = Double.parseDouble(shapeStretchYField.getText());

            if (newWidth <= 0 || newHeight <= 0) { // Dimensioni non valide
                 Platform.runLater(this::refreshUIState); // Ripristina
                return;
            }
            
            Rect oldBounds = selected.getBounds(); // Bounds NON ruotati attuali
            // Il resize avviene mantenendo il centro, se possibile, o il topLeft.
            // Per mantenere il centro:
            Point2D oldCenter = oldBounds.getCenter();
            Rect newUnrotatedBounds = new Rect(
                oldCenter.getX() - newWidth / 2.0, 
                oldCenter.getY() - newHeight / 2.0,
                newWidth, 
                newHeight
            );
            geoEngine.resizeSelectedShape(newUnrotatedBounds); // GeoEngine usa ResizeShapeCommand
        } catch (NumberFormatException e) { 
            Platform.runLater(this::refreshUIState); // Ripristina in caso di errore di formato
        }
    }

     private void applyProportionalScaleFromField() { // Se shapeScaleField è per scala proporzionale
        Shape selected = geoEngine.getSelectedShape();
        if (selected == null || shapeScaleField == null) return;
        try {
            double scaleFactor = Double.parseDouble(shapeScaleField.getText());
            if (scaleFactor <= 0) { // Scala non valida
                Platform.runLater(this::refreshUIState);
                return;
            }

            Rect oldBounds = selected.getBounds();
            double newWidth = oldBounds.getWidth() * scaleFactor;
            double newHeight = oldBounds.getHeight() * scaleFactor;
            
            Point2D oldCenter = oldBounds.getCenter();
            Rect newUnrotatedBounds = new Rect(
                oldCenter.getX() - newWidth / 2.0, 
                oldCenter.getY() - newHeight / 2.0,
                newWidth, 
                newHeight
            );
            geoEngine.resizeSelectedShape(newUnrotatedBounds);
            // Dopo l'applicazione, il campo scala dovrebbe tornare a "1.0" perché la scala è applicata
            // Platform.runLater(() -> shapeScaleField.setText("1.0")); // O gestito da refreshUIState
        } catch (NumberFormatException e) { 
            Platform.runLater(this::refreshUIState);
        }
    }

    private void applyTextContentChange() { 
        Shape selected = geoEngine.getSelectedShape();
        if (!(selected instanceof TextShape) || textContentField == null) return;
        TextShape selectedText = (TextShape) selected;
        String newText = textContentField.getText();
        // Confronta con il testo attuale per evitare comandi non necessari
        if (!Objects.equals(selectedText.getText(), newText)) { // Usa Objects.equals per gestire null
            geoEngine.changeSelectedTextContent(newText);
        }
    }
    private void applyTextFontSizeChange() { 
        Shape selected = geoEngine.getSelectedShape();
        if (!(selected instanceof TextShape) || textFontSizeField == null) return;
        try {
            double newSize = Double.parseDouble(textFontSizeField.getText());
            if (newSize > 0) { // Valida la dimensione
                // Confronta con la dimensione attuale per evitare comandi non necessari
                if (Math.abs(((TextShape)selected).getFontSize() - newSize) > 1e-2) {
                    geoEngine.changeSelectedTextSize(newSize);
                }
            } else {
                 Platform.runLater(this::refreshUIState); // Ripristina se dimensione non valida
            }
        } catch (NumberFormatException e) { 
            Platform.runLater(this::refreshUIState);
        }
    }

    private Color convertModelToFxColor(ColorData modelColor) { 
        if (modelColor == null) return Color.TRANSPARENT; // Default a trasparente se null
        return Color.rgb(modelColor.getR(), modelColor.getG(), modelColor.getB(), modelColor.getA());
    }
    private ColorData convertFxToModelColor(Color fxColor) {
        if (fxColor == null) return ColorData.TRANSPARENT; // Default
         return new ColorData(
                (int) (fxColor.getRed() * 255), 
                (int) (fxColor.getGreen() * 255),
                (int) (fxColor.getBlue() * 255), 
                fxColor.getOpacity()
        );
    }
 
    private void updatePropertyPanelContent(Shape selectedShape) {
        boolean shapeIsSelected = (selectedShape != null);
        // La disabilitazione dei campi è gestita in refreshUIState()
        // Qui impostiamo solo i valori se una forma è selezionata e il campo non è focused.
        
        if (shapeNamePropertyField != null && !shapeNamePropertyField.isFocused()) {
            shapeNamePropertyField.setText(shapeIsSelected ? selectedShape.getClass().getSimpleName() : "Nessuna Selezione");
        }
        
        if (shapeIsSelected) {
            Rect bounds = selectedShape.getBounds(); 
            if (shapeXPositionField != null && !shapeXPositionField.isFocused()) 
                shapeXPositionField.setText(String.format("%.0f", bounds.getX()));
            if (shapeYPositionField != null && !shapeYPositionField.isFocused()) 
                shapeYPositionField.setText(String.format("%.0f", bounds.getY()));
            if (shapeStretchXField != null && !shapeStretchXField.isFocused()) 
                shapeStretchXField.setText(String.format("%.1f", bounds.getWidth()));
            if (shapeStretchYField != null && !shapeStretchYField.isFocused()) 
                shapeStretchYField.setText(String.format("%.1f", bounds.getHeight()));
            if (shapeRotationField != null && !shapeRotationField.isFocused()) 
                shapeRotationField.setText(String.format("%.1f", selectedShape.getRotation()));
            if (shapeScaleField != null && !shapeScaleField.isFocused()) // La scala è sempre 1.0 finché non si edita
                shapeScaleField.setText("1.0");


            if (selectedShape instanceof TextShape) {
                TextShape textShape = (TextShape) selectedShape;
                if (textContentField != null && !textContentField.isFocused()) 
                    textContentField.setText(textShape.getText());
                if (textFontSizeField != null && !textFontSizeField.isFocused()) 
                    textFontSizeField.setText(String.format("%.1f", textShape.getFontSize()));
            } else { // Non è TextShape, pulisci i campi testo
                if (textContentField != null && !textContentField.isFocused()) textContentField.setText("");
                if (textFontSizeField != null && !textFontSizeField.isFocused()) textFontSizeField.setText("");
            }
        } else { // Nessuna forma selezionata, pulisci tutti i campi proprietà (se non focused)
            if (shapeXPositionField != null && !shapeXPositionField.isFocused()) shapeXPositionField.setText("");
            if (shapeYPositionField != null && !shapeYPositionField.isFocused()) shapeYPositionField.setText("");
            if (shapeStretchXField != null && !shapeStretchXField.isFocused()) shapeStretchXField.setText("");
            if (shapeStretchYField != null && !shapeStretchYField.isFocused()) shapeStretchYField.setText("");
            if (shapeRotationField != null && !shapeRotationField.isFocused()) shapeRotationField.setText("0.0");
            if (shapeScaleField != null && !shapeScaleField.isFocused()) shapeScaleField.setText("1.0");
            if (textContentField != null && !textContentField.isFocused()) textContentField.setText("");
            if (textFontSizeField != null && !textFontSizeField.isFocused()) textFontSizeField.setText("");
        }
    }

    private void updateActiveToolButtonVisuals(String activeToolName) {
        String inactiveStyle = "-fx-background-color: #e0e0e0; -fx-background-radius: 15;"; 
        String activeStyle = "-fx-background-color: #c0c0c0; -fx-background-radius: 15; -fx-border-color: #707070; -fx-border-width: 1.5px; -fx-border-radius: 14;";
        String selectToolActiveStyleFromFXML = "-fx-background-color: #d0d0d0; -fx-background-radius: 15; -fx-border-color: #707070; -fx-border-width: 1.5px; -fx-border-radius: 14;"; // Preso dal tuo FXML

        for (Button button : toolButtonsList) {
            if (button != null) {
                button.setStyle(inactiveStyle); // Stile di default per inattivo
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
                currentActiveButton.setStyle(selectToolActiveStyleFromFXML);
            } else {
                currentActiveButton.setStyle(activeStyle);
            }
        } else if (selectToolButton != null) { // Fallback se nessun tool attivo (improbabile ma sicuro)
             selectToolButton.setStyle(selectToolActiveStyleFromFXML); // Rendi SelectTool attivo di default
        }
    }
        
    // --- Nuovi Handler per Sprint 3 ---
    private void handleSaveAsReusableAction() {
        List<Shape> selectedShapes = geoEngine.getSelectedShapes(); 
        if (selectedShapes == null) {
            if(drawingView!=null) drawingView.showError("No shape selected to save as reusable.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog("MyShape");
        dialog.setTitle("Save Reusable Shape");
        dialog.setHeaderText("Enter a name for this reusable shape (must be unique):");
        dialog.setContentText("Name:");
        dialog.initOwner(primaryStage);

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (name.trim().isEmpty()) {
                if(drawingView!=null) drawingView.showError("Shape name cannot be empty.");
            } else {
                geoEngine.saveSelectedAsReusableShape(name.trim()); // GeoEngine sovrascriverà   
            }
        });
    }

    private void handleDeleteReusableAction() {
        if (reusableShapesListView == null) return;
        String selectedName = reusableShapesListView.getSelectionModel().getSelectedItem();
        if (selectedName == null) {
            if(drawingView!=null) drawingView.showError("No reusable shape selected from the library to delete.");
            return;
        }
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION, 
            "Are you sure you want to delete the reusable shape '" + selectedName + "' from the library?", 
            ButtonType.YES, ButtonType.NO);
        confirmDialog.setTitle("Confirm Delete");
        confirmDialog.initOwner(primaryStage);
        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            geoEngine.deleteReusableShapeDefinition(selectedName);
            // updateReusableShapesListView() sarà chiamato da update() -> refreshUIState()
        }
    }

    private void handlePlaceReusableAction() {
        if (reusableShapesListView == null) return;
        String selectedName = reusableShapesListView.getSelectionModel().getSelectedItem();
        if (selectedName == null) {
            if(drawingView!=null) drawingView.showError("No reusable shape selected from the library.");
            return;
        }
        
        // Posizionamento semplificato al centro della vista corrente
        double viewCenterX = 0;
        double viewCenterY = 0;
        if (canvasHolder != null && geoEngine != null) {
             viewCenterX = (canvasHolder.getWidth() / 2.0 - geoEngine.getScrollOffsetX()) / geoEngine.getCurrentZoom();
             viewCenterY = (canvasHolder.getHeight() / 2.0 - geoEngine.getScrollOffsetY()) / geoEngine.getCurrentZoom();
        }

        geoEngine.placeReusableShape(selectedName, new Point2D(viewCenterX, viewCenterY));
        // refreshUIState(); // Chiamato da update()
    }

    private void updateReusableShapesListView() {
        if (reusableShapesListView == null || geoEngine == null) return;
        
        // Salva la selezione corrente se esiste
        String previouslySelected = reusableShapesListView.getSelectionModel().getSelectedItem();

        List<String> names = geoEngine.getReusableShapeDefinitions().stream()
                                .map(ReusableShapeDefinition::getName)
                                .sorted() // Ordina alfabeticamente
                                .collect(Collectors.toList());
        reusableShapesListView.setItems(FXCollections.observableArrayList(names));

        // Ripristina la selezione se l'elemento esiste ancora
        if (previouslySelected != null && names.contains(previouslySelected)) {
            reusableShapesListView.getSelectionModel().select(previouslySelected);
        } else {
            reusableShapesListView.getSelectionModel().clearSelection();
        }
    }

    private void handleExportReusableLibraryAction() {
        if (geoEngine.getReusableShapeDefinitions().isEmpty()) {
            if(drawingView!=null) drawingView.showError("Reusable shape library is empty. Nothing to export.");
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Reusable Shape Library");
        fileChooser.setInitialFileName("my_shapes_library.geolib");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("GeoDraw Library (*.geolib)", "*.geolib"));
        File file = fileChooser.showSaveDialog(primaryStage);

        if (file != null) {
            try {
                geoEngine.exportReusableLibrary(file.getAbsolutePath());
                if(drawingView!=null) drawingView.showUserMessage("Library exported to " + file.getName());
            } catch (Exception e) {
                if(drawingView!=null) drawingView.showError("Failed to export library: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void handleImportReusableLibraryAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Reusable Shape Library");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("GeoDraw Library (*.geolib)", "*.geolib"));
        File file = fileChooser.showOpenDialog(primaryStage);

        if (file != null) {
            try {
                geoEngine.importReusableLibrary(file.getAbsolutePath());
                // La notifica "ReusableLibraryChanged" da GeoEngine dovrebbe aggiornare la ListView
            } catch (Exception e) {
                 if(drawingView!=null) drawingView.showError("Failed to import library: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
        
    public static void main(String[] args) {
        launch(args);
    }
}