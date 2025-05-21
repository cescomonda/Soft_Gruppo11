package sad.gruppo11; // Assicurati che il package sia corretto

import sad.gruppo11.Controller.GeoEngine;
import sad.gruppo11.Controller.ToolState;
import sad.gruppo11.Infrastructure.AddShapeCommand;
import sad.gruppo11.Infrastructure.ChangeFillColorCommand;
import sad.gruppo11.Infrastructure.ChangeStrokeColorCommand; // Da creare/completare
import sad.gruppo11.Infrastructure.CopyShapeToClipboardCommand;
import sad.gruppo11.Infrastructure.CutShapeCommand;
// import sad.gruppo11.Infrastructure.ChangeFillColorCommand; // Da creare/completare
import sad.gruppo11.Infrastructure.DeleteShapeCommand;
import sad.gruppo11.Infrastructure.PasteShapeCommand;
import sad.gruppo11.Infrastructure.ResizeShapeCommand;
import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.EllipseShape;
import sad.gruppo11.Model.LineSegment;
import sad.gruppo11.Model.RectangleShape;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Rect;
import sad.gruppo11.View.CanvasPanel;
import sad.gruppo11.View.DrawingView;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.paint.Color; // Per javafx.scene.paint.Color

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;


public class MainApp extends Application {

    @FXML private Button selectToolButton;
    @FXML private Button lineToolButton;
    @FXML private Button ellipseToolButton;
    @FXML private Button rectangleToolButton;
    @FXML private Button combinedShapeToolButton;
    @FXML private TextField newShapeNameField;
    @FXML private Button createShapeButton;
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
    @FXML private TextField shapeNamePropertyField;
    @FXML private TextField shapeXPositionField;
    @FXML private TextField shapeYPositionField;
    @FXML private TextField shapeRotationField;
    @FXML private TextField shapeScaleField;
    @FXML private ColorPicker fillColorPicker;
    @FXML private ColorPicker strokeColorPicker;

    @FXML private TextField shapeScaleXField;
    @FXML private TextField shapeScaleYField;

    private List<Button> toolButtons;
    private GeoEngine geoEngine;
    private DrawingView drawingView;
    
    // private CanvasPanel canvasPanel; // CanvasPanel è ora interno a DrawingView se non serve altrove
    private Canvas actualCanvas;
    private Stage primaryStage;


    public GeoEngine getGeoEngineInstance() {
        return this.geoEngine;
    }
    
    @Override
    public void start(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("GeoDraw");
        primaryStage.setResizable(false);

        geoEngine = new GeoEngine();

        String fxmlFile = "mockup.fxml";
        URL fxmlUrl = getClass().getResource(fxmlFile);
        if (fxmlUrl == null) {
            System.err.println("Cannot load FXML file: " + fxmlFile);
            Platform.exit();
            return;
        }
        
        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        loader.setController(this);
        Parent root = loader.load();

        toolButtons = List.of(
            Objects.requireNonNullElse(selectToolButton, new Button("DummySelect")),
            Objects.requireNonNullElse(lineToolButton, new Button("DummyLine")),
            Objects.requireNonNullElse(rectangleToolButton, new Button("DummyRect")),
            Objects.requireNonNullElse(ellipseToolButton, new Button("DummyEllipse"))
        );

        if (canvasHolder == null) {
            System.err.println("canvasHolder is null. Check fx:id in FXML or FXML loading.");
            Platform.exit();
            return;
        }
        
        actualCanvas = new Canvas();
        actualCanvas.setId("actualDrawingCanvas");
        canvasHolder.getChildren().add(actualCanvas);
        actualCanvas.toFront(); 
        actualCanvas.widthProperty().bind(canvasHolder.widthProperty());
        actualCanvas.heightProperty().bind(canvasHolder.heightProperty());
        
        CanvasPanel localCanvasPanel = new CanvasPanel(actualCanvas); // Creato localmente se solo DrawingView lo usa
        drawingView = new DrawingView(localCanvasPanel, geoEngine.getDrawing());
        drawingView.setController(geoEngine);
        geoEngine.addModelObserver(drawingView); 

        // Registra MainApp per ascoltare i cambi di selezione da GeoEngine
        geoEngine.addSelectionChangeListener(selectedShape -> {
            Platform.runLater(() -> updatePropertyPanel(selectedShape));
        });

        geoEngine.addToolStateChangeListener(activeState -> {
            Platform.runLater(() -> updateActiveToolButton(activeState));
        });


        setupButtonActions();
        setupPropertyPanelListeners(); 

        Scene scene = new Scene(root);
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.DELETE || event.getCode() == javafx.scene.input.KeyCode.BACK_SPACE) {
                // Il tasto CANC (Delete) o BACKSPACE è stato premuto
                Shape selected = geoEngine.getSelectedShape();
                if (selected != null) {
                    System.out.println("MainApp: Delete key pressed for shape " + selected.getId()); // DEBUG
                    geoEngine.getCommandManager().execute(
                        new DeleteShapeCommand(geoEngine.getDrawing(), selected)
                    );
                    // La deselezione avverrà tramite GeoEngine.notifyModelChanged()
                    // -> GeoEngine.setCurrentlySelectedShape(null) se la forma non c'è più
                } else {
                    System.out.println("MainApp: Delete key pressed, but no shape selected."); // DEBUG
                }
            }
            // TODO: Potresti aggiungere altri shortcut da tastiera qui (es. CTRL+Z per Undo)
        });

        primaryStage.setScene(scene);
        primaryStage.show();
            
        Platform.runLater(() -> {
            drawingView.render(); 
            updateActiveToolButton(geoEngine.getCurrentState()); 
            updatePropertyPanel(geoEngine.getSelectedShape()); 
        });
    }
        
    private void setupButtonActions() {
        if (selectToolButton != null) selectToolButton.setOnAction(e -> geoEngine.setState(geoEngine.getSelectState()));
        if (lineToolButton != null) lineToolButton.setOnAction(e -> geoEngine.setState(geoEngine.getLineState()));
        if (rectangleToolButton != null) rectangleToolButton.setOnAction(e -> geoEngine.setState(geoEngine.getRectangleState()));
        if (ellipseToolButton != null) ellipseToolButton.setOnAction(e -> geoEngine.setState(geoEngine.getEllipseState()));

        if (undoButton != null) undoButton.setOnAction(e -> geoEngine.getCommandManager().undo());
        if (redoButton != null) redoButton.setOnAction(e -> geoEngine.getCommandManager().redo());
        if (saveButton != null) saveButton.setOnAction(e -> handleSaveAction());
        if (openButton != null) openButton.setOnAction(e -> handleOpenAction());
        
        if (deleteButton != null) {
            deleteButton.setOnAction(e -> {
                Shape selected = geoEngine.getSelectedShape();
                if (selected != null) {
                    // USA geoEngine.getDrawing() per ottenere il modello CORRENTE
                    geoEngine.getCommandManager().execute(
                        new DeleteShapeCommand(geoEngine.getDrawing(), selected)
                    );
                    // geoEngine.setCurrentlySelectedShape(null); // Gestito da notifyModelChanged
                } else {
                    System.out.println("Nessuna forma selezionata da eliminare.");
                }
            });
        }
        if (copyButton != null) copyButton.setOnAction(e -> handleCopyAction());

        if (pasteButton != null) {
            pasteButton.setOnAction(e -> {
                handlePasteAction();
            });
        }
    
        if (cutButton != null) cutButton.setOnAction(e -> handleCutAction());
    }

    private void handleSaveAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Drawing");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("GeoDraw Files (*.ser)", "*.ser"),
                new FileChooser.ExtensionFilter("All Files (*.*)", "*.*"));
        File file = fileChooser.showSaveDialog(primaryStage);
        if (file != null) {
            try {
                geoEngine.saveDrawing(file.getAbsolutePath());
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                alert.setTitle("Salvataggio Completato");
                alert.setHeaderText(null);
                alert.setContentText("Disegno salvato con successo in:\n" + file.getAbsolutePath());
                alert.showAndWait();
                System.out.println("Disegno salvato in: " + file.getAbsolutePath());
            } catch (Exception e) {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("Errore di Salvataggio");
                alert.setHeaderText("Impossibile salvare il disegno.");
                alert.setContentText("Si è verificato un errore durante il salvataggio del file:\n" + e.getMessage());
                alert.showAndWait();
                e.printStackTrace();
            }
        }
    }

    private void handleOpenAction() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            try {
                geoEngine.loadDrawing(file.getAbsolutePath());
            } catch (Exception e) { // Cattura la RuntimeException (o custom) da GeoEngine
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("Errore di Caricamento");
                alert.setHeaderText("Impossibile caricare il disegno.");
                alert.setContentText("Il file potrebbe essere corrotto o non compatibile.\nDettaglio: " + e.getMessage());
                alert.showAndWait();
                e.printStackTrace(); // Per debug console
            }
        }
    }


    private void handleCopyAction() {
        Shape selected = geoEngine.getSelectedShape();
        if (selected != null) {
            // Ora creiamo ed eseguiamo il comando
            CopyShapeToClipboardCommand cmd = new CopyShapeToClipboardCommand(geoEngine.getClipboard(), selected.cloneWithNewId());
            geoEngine.getCommandManager().execute(cmd); // Esegui tramite CommandManager
            System.out.println("Comando Copia eseguito per forma: " + selected.getId());
        } else {
            System.out.println("Nessuna forma selezionata da copiare.");
        }
    }

    private void handlePasteAction() {
        if (!geoEngine.getClipboard().isEmpty()) { // Controlla se c'è qualcosa da incollare
            PasteShapeCommand cmd = new PasteShapeCommand(geoEngine.getDrawing(), geoEngine.getClipboard());
            geoEngine.getCommandManager().execute(cmd);
        } else {
            System.out.println("Appunti vuoti. Nulla da incollare.");
        }
    }

    private void handleCutAction() {
        Shape selected = geoEngine.getSelectedShape();
        if (selected != null) {
            CutShapeCommand cmd = new CutShapeCommand(geoEngine.getDrawing(), selected, geoEngine.getClipboard());
            geoEngine.getCommandManager().execute(cmd);
            // La deselezione avverrà tramite il flusso di notifica di GeoEngine
            // perché la forma viene rimossa dal modello.
        } else {
            System.out.println("Nessuna forma selezionata da tagliare.");
        }
    }

    private void setupPropertyPanelListeners() {
        if (strokeColorPicker != null) {
           strokeColorPicker.setOnAction(event -> {
                Shape selected = geoEngine.getSelectedShape();
                Color fxColor = strokeColorPicker.getValue();
                ColorData newModelColor = convertFxToModelColor(fxColor);
                if (selected != null) {
                    
                    // Assumendo che Shape abbia getStrokeColor()
                    ChangeStrokeColorCommand cmd = new ChangeStrokeColorCommand(selected, newModelColor);
                    cmd.captureOldColor(); 
                    geoEngine.getCommandManager().execute(cmd);
                }
                else
                {
                    geoEngine.setCurrentStrokeColorForNewShapes(newModelColor); 
                }
            });
        }

        if (fillColorPicker != null) {
            fillColorPicker.setOnAction(event -> {
                Shape selected = geoEngine.getSelectedShape();
                if (selected != null && !(selected instanceof LineSegment)) { // Non per le linee
                    javafx.scene.paint.Color fxColor = fillColorPicker.getValue();
                    ColorData newModelColor = convertFxToModelColor(fxColor);

                    // Assumendo che Shape abbia getFillColor()
                    // ColorData oldModelColor = selected.getFillColor(); // Non più necessario catturarlo qui

                    ChangeFillColorCommand cmd = new ChangeFillColorCommand(selected, newModelColor);
                    cmd.captureOldColor(); // Il comando cattura lo stato attuale prima della modifica
                    geoEngine.getCommandManager().execute(cmd);
                } else if (selected == null) { // Nessuna forma selezionata, aggiorna il colore di default
                    javafx.scene.paint.Color fxColor = fillColorPicker.getValue();
                    ColorData newModelColor = convertFxToModelColor(fxColor);
                    geoEngine.setCurrentFillColorForNewShapes(newModelColor);
                }
            });
        }

        if (shapeScaleXField != null) {
            shapeScaleXField.setOnAction(event -> applyScaleFromFields());
            shapeScaleXField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal && shapeScaleXField.isFocused() == false ) { // Ha perso il focus
                    applyScaleFromFields();
                }
            });
        }

        if (shapeScaleYField != null) {
            shapeScaleYField.setOnAction(event -> applyScaleFromFields());
            shapeScaleYField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal && shapeScaleXField.isFocused() == false ) { // Ha perso il focus
                    applyScaleFromFields();
                }
            });
        }
    }

    private void applyScaleFromFields() {
        Shape selected = geoEngine.getSelectedShape();
        if (selected == null) return;

        double scaleXFactor = 1.0; // Default: nessuna scala
        double scaleYFactor = 1.0; // Default: nessuna scala
        boolean scaleAttempted = false;

        try {
            if (shapeScaleXField != null && !shapeScaleXField.getText().isEmpty()) {
            double scaleXPercent = Double.parseDouble(shapeScaleXField.getText());
            if (scaleXPercent <= 0) {
                System.err.println("Scale X percentage must be positive.");
                updatePropertyPanel(selected); // Resetta il campo a 100
                return;
            }
            scaleXFactor = scaleXPercent / 100.0;
            scaleAttempted = true;
        }

        if (shapeScaleYField != null && !shapeScaleYField.getText().isEmpty()) {
            double scaleYPercent = Double.parseDouble(shapeScaleYField.getText());
            if (scaleYPercent <= 0) {
                System.err.println("Scale Y percentage must be positive.");
                updatePropertyPanel(selected); // Resetta il campo a 100
                return;
            }
            scaleYFactor = scaleYPercent / 100.0;
            scaleAttempted = true;
        }

        if (!scaleAttempted) { // Nessun input nei campi scala
            return;
        }

        // Se l'utente ha inserito 100 in entrambi, non c'è bisogno di fare un comando
        if (Math.abs(scaleXFactor - 1.0) < 1e-5 && Math.abs(scaleYFactor - 1.0) < 1e-5) {
            // System.out.println("ApplyScale: Scale is 100% for both axes, no change.");
            // Assicurati che i campi UI siano resettati a "100" se sono stati modificati e poi riportati a 100
            Platform.runLater(() -> { // Per evitare problemi di focus ciclico
                if (shapeScaleXField != null) shapeScaleXField.setText("100");
                if (shapeScaleYField != null) shapeScaleYField.setText("100");
            });
            return;
        }


        Rect currentBounds = selected.getBounds();
        double currentWidth = currentBounds.getWidth();
        double currentHeight = currentBounds.getHeight();

        double newWidth = currentWidth * scaleXFactor;
        double newHeight = currentHeight * scaleYFactor;

        // Ridimensiona mantenendo il centro
        double centerX = currentBounds.getX() + currentWidth / 2.0;
        double centerY = currentBounds.getY() + currentHeight / 2.0;

        double newTopLeftX = centerX - newWidth / 2.0;
        double newTopLeftY = centerY - newHeight / 2.0;

        // Prevenire dimensioni negative o nulle se la scala è troppo piccola o negativa (già controllato > 0)
        if (newWidth < 1.0) newWidth = 1.0; // Dimensione minima
        if (newHeight < 1.0) newHeight = 1.0;

        Rect newBounds = new Rect(new Point2D(newTopLeftX, newTopLeftY), newWidth, newHeight);

        ResizeShapeCommand cmd = new ResizeShapeCommand(selected, newBounds);
        cmd.captureOldBounds(); // Cattura i bounds attuali PRIMA di eseguire
        geoEngine.getCommandManager().execute(cmd);

        // Dopo l'esecuzione del comando, la forma è cambiata.
        // Il pannello proprietà dovrebbe aggiornarsi per riflettere che la nuova scala è 100%
        // della *nuova* dimensione. Questo è gestito da GeoEngine.notifyModelChanged()
        // -> MainApp (listener su selectionChange) -> updatePropertyPanel(selectedShape).
        // updatePropertyPanel(selectedShape) dovrebbe già reimpostare i campi scala a "100".
        } catch (NumberFormatException e) {
            System.err.println("Invalid scale input: Not a number. " + e.getMessage());
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
            alert.setTitle("Input Invalido");
            alert.setHeaderText("Valore di scala non valido.");
            alert.setContentText("Inserire un numero per la percentuale di scala (es. 150).");
            alert.showAndWait();
            updatePropertyPanel(selected); // Resetta i campi a 100
        }
    }

    private Color convertModelToFxColor(ColorData modelColor) {
        if (modelColor == null) return Color.BLACK; 
        return Color.rgb(modelColor.getR(), modelColor.getG(), modelColor.getB(), modelColor.getA());
    }

    private ColorData convertFxToModelColor(Color fxColor) {
        if (fxColor == null) return new ColorData(0,0,0,1);
         return new ColorData(
                (int) (fxColor.getRed() * 255),
                (int) (fxColor.getGreen() * 255),
                (int) (fxColor.getBlue() * 255),
                fxColor.getOpacity()
        );
    }

    private void updatePropertyPanel(Shape selectedShape) {
        
        boolean shapeIsSelected = (selectedShape != null);
        boolean isLine = (selectedShape instanceof LineSegment);
        if (shapeNamePropertyField != null) {
            // shapeNamePropertyField.setDisable(!shapeIsSelected); // Per ora, è sempre disabilitato
            shapeNamePropertyField.setText(shapeIsSelected ? selectedShape.getClass().getSimpleName() : "");
        }
        if (shapeXPositionField != null) {
            // shapeXPositionField.setDisable(!shapeIsSelected); // Per ora, è sempre disabilitato

            shapeXPositionField.setText(shapeIsSelected ? String.format("%.0f", selectedShape.getBounds().getX()) : "");
        }
        if (shapeYPositionField != null) {
            // shapeYPositionField.setDisable(!shapeIsSelected); // Per ora, è sempre disabilitato
            shapeYPositionField.setText(shapeIsSelected ? String.format("%.0f", selectedShape.getBounds().getY()) : "");
        }
        if (shapeRotationField != null) {
            shapeRotationField.setDisable(true); shapeRotationField.setText("");
        }
        if (shapeScaleField != null) {
            shapeScaleField.setDisable(true); shapeScaleField.setText("");
        }
        if (shapeRotationField != null) { shapeRotationField.setDisable(true); shapeRotationField.setText(shapeIsSelected ? "" : ""); } // Rotazione non implementata


        if (strokeColorPicker != null) {
            // Il picker del tratto è sempre attivo
            strokeColorPicker.setDisable(false); 
            if (shapeIsSelected && selectedShape.getStrokeColor() != null) {
                strokeColorPicker.setValue(convertModelToFxColor(selectedShape.getStrokeColor()));
            } else { // Nessuna selezione, mostra il colore di default per le nuove forme
                strokeColorPicker.setValue(convertModelToFxColor(geoEngine.getCurrentStrokeColorForNewShapes()));
            }
        }

        if (fillColorPicker != null) {
            fillColorPicker.setDisable(isLine); // Disabilita per linee o se nulla è selezionato
            if (shapeIsSelected && !isLine) {
                ColorData modelFill = selectedShape.getFillColor();
                fillColorPicker.setValue(convertModelToFxColor(Objects.requireNonNullElse(modelFill, ColorData.TRANSPARENT)));
            } else if (!shapeIsSelected) { // Nessuna selezione, mostra il colore di default per le nuove forme
                fillColorPicker.setValue(convertModelToFxColor(geoEngine.getCurrentFillColorForNewShapes()));
            } else { // È una linea selezionata
                fillColorPicker.setValue(javafx.scene.paint.Color.TRANSPARENT); // Default per linee
            }
        }


        // Gestione Scala
        if (shapeScaleXField != null) {
            shapeScaleXField.setDisable(!shapeIsSelected);
            shapeScaleXField.setText("100"); // Mostra sempre 100% come base per la prossima operazione di scala
        }
        if (shapeScaleYField != null) {
            shapeScaleYField.setDisable(!shapeIsSelected);
            shapeScaleYField.setText("100"); // Mostra sempre 100% come base per la prossima operazione di scala
        }

    }

    private void updateActiveToolButton(ToolState activeState) {
        String defaultStyle = "-fx-background-color: #e0e0e0; -fx-background-radius: 15;";
        String activeStyle = "-fx-background-color: lightblue; -fx-border-color: blue; -fx-background-radius: 15;";

        // Usa la lista toolButtons inizializzata in start()
        for (Button button : toolButtons) {
            if (button != null) { 
                button.setStyle(defaultStyle);
            }
        }

        if (activeState == geoEngine.getSelectState() && selectToolButton != null) selectToolButton.setStyle(activeStyle);
        else if (activeState == geoEngine.getLineState() && lineToolButton != null) lineToolButton.setStyle(activeStyle);
        else if (activeState == geoEngine.getRectangleState() && rectangleToolButton != null) rectangleToolButton.setStyle(activeStyle);
        else if (activeState == geoEngine.getEllipseState() && ellipseToolButton != null) ellipseToolButton.setStyle(activeStyle);
    }

    public static void main(String[] args) {
        launch(args);
    }
}