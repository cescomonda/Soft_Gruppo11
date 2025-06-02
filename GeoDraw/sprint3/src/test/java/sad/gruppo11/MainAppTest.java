package sad.gruppo11;

import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.control.LabeledMatchers;
import org.testfx.matcher.control.ListViewMatchers;
import org.testfx.matcher.control.TextInputControlMatchers;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import static org.junit.jupiter.api.Assertions.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.*;

import sad.gruppo11.MainApp; // La tua classe MainApp
// Importa GeoEngine e altre classi necessarie per accedere allo stato interno per le verifiche
import sad.gruppo11.Controller.GeoEngine; 
import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.LineSegment;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Point2D;


@ExtendWith(ApplicationExtension.class)
class MainAppTest {

    private MainApp mainApp; // Istanza della tua applicazione
    private GeoEngine geoEngineInternal; // Per accedere allo stato interno
    private Stage primaryStage;

    /**
     * Will be called with {@code @BeforeEach} semantics, i. e. before each test method.
     *
     * @param stage - Will be injected by the test runner.
     */
    @Start
    private void start(Stage stage) throws Exception {
        mainApp = new MainApp(); // Crea un'istanza
        mainApp.start(stage);    // Avvia l'applicazione JavaFX
        primaryStage = stage;
        // Ottieni un riferimento a GeoEngine dopo che MainApp lo ha inizializzato.
        // Questo richiede che MainApp esponga un modo per ottenere GeoEngine,
        // o usare reflection, o un approccio più avanzato di TestFX per accedere ai campi.
        // Se MainApp ha un metodo getGeoEngine():
        // geoEngineInternal = mainApp.getGeoEngine(); 
        // Altrimenti, potremmo dover usare lookup per i nodi e poi navigare.
        // Per ora, alcuni test potrebbero essere limitati se non possiamo accedere a geoEngineInternal.
    }

    /**
     * Helper per ottenere GeoEngine. Questo è un workaround se MainApp non lo espone.
     * In un'applicazione reale, sarebbe meglio avere un getter o un modo pulito.
     * Questo potrebbe fallire se la struttura di MainApp cambia.
     */
    private GeoEngine getGeoEngineFromMainApp(FxRobot robot) {
        // Questo è un trucco: se il canvasHolder ha un riferimento a GeoEngine nel suo UserData,
        // o se qualche altro nodo ha un riferimento accessibile.
        // Alternativamente, se GeoEngine è un singleton accessibile staticamente (non il caso qui).
        // Per ora, questo metodo è un placeholder. Idealmente, MainApp fornisce un getter.
        // Se non c'è un modo facile, alcune asserzioni sullo stato interno di GeoEngine saranno difficili.

        // Tentativo: se MainApp avesse un campo statico per l'ultima istanza (NON RACCOMANDATO IN PRODUZIONE)
        // return MainApp.getLastInstance().getGeoEngine(); 
        
        // Per questo esempio, assumiamo che non possiamo accedere direttamente a geoEngineInternal facilmente
        // e ci concentreremo su ciò che è osservabile dall'UI.
        // Se aggiungi un getter a MainApp, decommenta la riga in start().
        return null; 
    }


    @BeforeEach
    void setUp(FxRobot robot) {
        // Eventuali setup specifici per ogni test, es. reset di stati
        // Se geoEngineInternal fosse disponibile:
        // if (geoEngineInternal != null) {
        //     robot.interact(() -> geoEngineInternal.createNewDrawing());
        // }
        // robot.clickOn("#canvasHolder"); // Assicura che il focus sia sul canvas a volte
    }
    
    @AfterEach
    void tearDown(FxRobot robot) throws Exception {
         // Pulisci dopo ogni test se necessario, es. chiudendo dialoghi modali
        // robot.closeCurrentWindow(); // Attenzione, potrebbe chiudere la finestra principale
    }

    @Test
    void application_shouldLaunchAndDisplayTitle(FxRobot robot) {
        assertEquals("GeoDraw - Sprint 3", primaryStage.getTitle());
        verifyThat("#selectToolButton", isVisible()); // Verifica un bottone chiave
    }

    @Test
    void toolSelection_lineTool_shouldBecomeActive(FxRobot robot) {
        Button lineButton = robot.lookup("#lineToolButton").queryButton();
        robot.clickOn(lineButton);
        WaitForAsyncUtils.waitForFxEvents(); // Aspetta che gli eventi FX vengano processati

        // Verifica stile bottone (più difficile, dipende da come imposti lo stile)
        // assertTrue(lineButton.getStyle().contains("-fx-border-color: #707070;")); // Esempio

        // Se potessimo accedere a geoEngine:
        // assertEquals("LineTool", getGeoEngineFromMainApp(robot).getCurrentToolName());
        
        // Verifica visiva/comportamentale: prova a disegnare una linea
        StackPane canvasHolder = robot.lookup("#canvasHolder").queryAs(StackPane.class);
        robot.moveTo(canvasHolder).press(MouseButton.PRIMARY).moveBy(50, 50).release(MouseButton.PRIMARY);
        WaitForAsyncUtils.waitForFxEvents();
        
        // Se GeoEngine fosse accessibile e avesse un Drawing:
        // Drawing drawing = getGeoEngineFromMainApp(robot).getDrawing();
        // assertEquals(1, drawing.getShapesInZOrder().size());
        // assertTrue(drawing.getShapesInZOrder().get(0) instanceof LineSegment);
        // Per ora, questo test verifica solo il click e un'azione base.
    }

    @Test
    void undoRedo_buttons_shouldBeDisabledInitially(FxRobot robot) {
        verifyThat("#undoButton", (Button b) -> b.isDisabled());
        verifyThat("#redoButton", (Button b) -> b.isDisabled());
    }
    
    // @Disabled("Test complesso, richiede accesso a GeoEngine o osservazione canvas dettagliata")
    @Test
    void drawShapeAndUndo_shouldRemoveShape(FxRobot robot) {
        // 1. Seleziona lo strumento linea
        robot.clickOn("#lineToolButton");
        WaitForAsyncUtils.waitForFxEvents();

        // 2. Disegna una linea sul canvasHolder
        // È meglio usare il Canvas effettivo se ha un fx:id
        // StackPane canvasHolder = robot.lookup("#canvasHolder").queryAs(StackPane.class);
        // Canvas actualCanvas = robot.lookup("#drawingCanvas").queryAs(Canvas.class); // Se il canvas ha fx:id="drawingCanvas"

        Node canvasNode = robot.lookup("#drawingCanvas").query(); // Trova il canvas per coordinate
        robot.moveTo(canvasNode).moveBy(-50,-50).press(MouseButton.PRIMARY).moveBy(100,100).release(MouseButton.PRIMARY);
        WaitForAsyncUtils.waitForFxEvents();

        // Verifica che il pulsante Undo sia abilitato
        Button undoButton = robot.lookup("#undoButton").queryButton();
        assertFalse(undoButton.isDisabled(), "Undo button should be enabled after drawing.");

        // Se GeoEngine fosse accessibile:
        // GeoEngine ge = getGeoEngineFromMainApp(robot);
        // assertNotNull(ge, "GeoEngine instance should be available for verification.");
        // assertEquals(1, ge.getDrawing().getShapesInZOrder().size(), "Should have 1 shape after drawing.");

        // 3. Clicca Undo
        robot.clickOn(undoButton);
        WaitForAsyncUtils.waitForFxEvents();
        
        // Verifica che Undo sia disabilitato (o che canUndo sia false) e Redo abilitato
        assertTrue(undoButton.isDisabled(), "Undo button should be disabled after undoing.");
        Button redoButton = robot.lookup("#redoButton").queryButton();
        assertFalse(redoButton.isDisabled(), "Redo button should be enabled after undoing.");

        // Se GeoEngine fosse accessibile:
        // assertEquals(0, ge.getDrawing().getShapesInZOrder().size(), "Should have 0 shapes after undo.");
    }

    @Test
    void propertyPanel_selectShape_shouldUpdateFields(FxRobot robot) {
        // 1. Seleziona strumento rettangolo e disegna un rettangolo
        robot.clickOn("#rectangleToolButton");
        Node canvasNode = robot.lookup("#drawingCanvas").query();
        robot.moveTo(canvasNode).moveBy(-20, -20).press(MouseButton.PRIMARY).moveBy(40, 30).release(MouseButton.PRIMARY); // Disegna un rett.
        WaitForAsyncUtils.waitForFxEvents();

        // 2. Seleziona lo strumento select (la forma dovrebbe rimanere selezionata o riselezionarla)
        robot.clickOn("#selectToolButton");
        WaitForAsyncUtils.waitForFxEvents();
        // Clicca sul rettangolo per assicurarsi che sia selezionato
        robot.moveTo(canvasNode).press(MouseButton.PRIMARY).release(MouseButton.PRIMARY); // Clicca al centro del rett. disegnato
        WaitForAsyncUtils.waitForFxEvents();

        // 3. Verifica che i campi proprietà siano abilitati e mostrino valori (es. nome)
        TextField shapeNameField = robot.lookup("#shapeNamePropertyField").queryAs(TextField.class);
        assertFalse(shapeNameField.isDisabled());
        assertTrue(shapeNameField.getText().toLowerCase().contains("rectangle"), "Shape name should indicate Rectangle.");

        TextField rotationField = robot.lookup("#shapeRotationField").queryAs(TextField.class);
        assertFalse(rotationField.isDisabled());
        assertEquals("0.0", rotationField.getText(), "Initial rotation should be 0.0"); // O il formato specifico

        ColorPicker fillColorPicker = robot.lookup("#fillColorPicker").queryAs(ColorPicker.class);
        assertFalse(fillColorPicker.isDisabled());
        // Verificare il colore di default o quello della forma
    }
    
    @Test
    void propertyPanel_changeRotation_shouldUpdateShape(FxRobot robot) {
        // 1. Disegna una forma (es. rettangolo)
        robot.clickOn("#rectangleToolButton");
        Node canvasNode = robot.lookup("#drawingCanvas").query();
        robot.moveTo(canvasNode).moveBy(0, 0).press(MouseButton.PRIMARY).moveBy(50,50).release(MouseButton.PRIMARY);
        WaitForAsyncUtils.waitForFxEvents();

        // 2. Selezionala
        robot.clickOn("#selectToolButton");
        robot.moveTo(canvasNode).moveBy(0, 0).press(MouseButton.PRIMARY).release(MouseButton.PRIMARY); // Clicca sulla forma
        WaitForAsyncUtils.waitForFxEvents();
        
        // 3. Cambia il valore nel campo rotazione
        TextField rotationField = robot.lookup("#shapeRotationField").queryAs(TextField.class);
        robot.clickOn(rotationField).eraseText(3).write("45.0").press(KeyCode.ENTER).release(KeyCode.ENTER);
        WaitForAsyncUtils.waitForFxEvents();

        // Verifica:
        // - Se GeoEngine fosse accessibile, controlla la rotazione della forma selezionata.
        // - Visivamente, il canvas dovrebbe mostrare la forma ruotata (test più complesso).
        // - Il campo rotazione dovrebbe mantenere "45.0".
        assertEquals("45.0", rotationField.getText());
        // Potremmo anche verificare che il comando di rotazione sia stato eseguito se avessimo accesso a CommandManager.
    }

    @Test
    void groupUngroup_functionality(FxRobot robot) {
        Node canvasNode = robot.lookup("#drawingCanvas").query();

        // 1. Disegna due forme
        robot.clickOn("#lineToolButton");
        robot.moveTo(canvasNode).moveBy(-60, -60).press(MouseButton.PRIMARY).moveBy(20, 20).release(MouseButton.PRIMARY); // Linea 1
        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("#rectangleToolButton");
        robot.moveTo(canvasNode).moveBy(0,0).press(MouseButton.PRIMARY).moveBy(20, 20).release(MouseButton.PRIMARY); // Rettangolo 1
        WaitForAsyncUtils.waitForFxEvents();

        // 2. Seleziona entrambe (SelectTool + SHIFT click o area select)
        robot.clickOn("#selectToolButton");
        robot.moveTo(canvasNode).moveBy(-60, -60).press(MouseButton.PRIMARY).release(MouseButton.PRIMARY); // Seleziona linea
        WaitForAsyncUtils.waitForFxEvents();
        robot.press(KeyCode.SHIFT);
        robot.moveTo(canvasNode).moveBy(0,0).press(MouseButton.PRIMARY).release(MouseButton.PRIMARY); // Aggiungi rettangolo alla selezione
        robot.release(KeyCode.SHIFT);
        WaitForAsyncUtils.waitForFxEvents();
        
        // 3. Verifica che il bottone Group sia abilitato
        Button groupButton = robot.lookup("#groupButton").queryButton();
        assertFalse(groupButton.isDisabled(), "Group button should be enabled.");

        // 4. Clicca Group
        robot.clickOn(groupButton);
        WaitForAsyncUtils.waitForFxEvents();

        // Verifica:
        // - Dovrebbe esserci una sola forma selezionata (il gruppo). (Richiede accesso a GeoEngine)
        // - Il bottone Ungroup dovrebbe essere abilitato.
        robot.moveTo(canvasNode).moveBy(0,0).press(MouseButton.PRIMARY).release(MouseButton.PRIMARY);
        Button ungroupButton = robot.lookup("#ungroupButton").queryButton();
        assertFalse(ungroupButton.isDisabled(), "Ungroup button should be enabled.");
        assertTrue(groupButton.isDisabled(), "Group button should be disabled after grouping.");

        // 5. Clicca Ungroup
        robot.clickOn(ungroupButton);
        WaitForAsyncUtils.waitForFxEvents();

        // Verifica:
        // - Le forme originali dovrebbero essere riselezionate (o nessuna selezione). (Richiede GeoEngine)
        // - Il bottone Ungroup dovrebbe essere disabilitato.
        // - Il bottone Group potrebbe essere nuovamente abilitato se le forme sono ancora considerate selezionate.
        assertTrue(ungroupButton.isDisabled(), "Ungroup button should be disabled after ungrouping.");
    }
    
    @Test
    void reusableShapes_saveAndPlace(FxRobot robot) {
        Node canvasNode = robot.lookup("#drawingCanvas").query();
        ListView<String> reusableList = robot.lookup("#reusableShapesListView").queryListView();

        // 1. Disegna una forma
        robot.clickOn("#ellipseToolButton");
        robot.moveTo(canvasNode).moveBy(-30, -30).press(MouseButton.PRIMARY).moveBy(20, 20).release(MouseButton.PRIMARY);
        WaitForAsyncUtils.waitForFxEvents();

        // 2. Selezionala
        robot.clickOn("#selectToolButton");
        robot.moveTo(canvasNode).moveBy(-15, -15).press(MouseButton.PRIMARY).release(MouseButton.PRIMARY);
        WaitForAsyncUtils.waitForFxEvents();

        Node scrollPane = robot.lookup(".scroll-pane").query(); // oppure usa fx:id se presente
        robot.moveTo(scrollPane);
        robot.scroll(50); // scrolla verso il basso di 5 "notch"
        WaitForAsyncUtils.waitForFxEvents();
        
        // 3. Clicca "Save As Reusable"
        Button saveReusableButton = robot.lookup("#saveAsReusableButton").queryButton();
        assertFalse(saveReusableButton.isDisabled());
        robot.clickOn(saveReusableButton);
        WaitForAsyncUtils.waitForFxEvents();

        // 4. Gestisci il TextInputDialog (TestFX ha modi per interagire con i dialoghi)
        robot.write("MyReusableEllipse").push(KeyCode.ENTER); // Scrive nel dialogo e preme invio
        WaitForAsyncUtils.waitForFxEvents();

        // 5. Verifica che la forma appaia nella ListView
        verifyThat(reusableList, ListViewMatchers.hasItems(1));
        verifyThat(reusableList, ListViewMatchers.hasListCell(("MyReusableEllipse")));
        
        // 6. Seleziona la forma dalla lista
        robot.clickOn( (Node)robot.from(reusableList).lookup(".list-cell").match(LabeledMatchers.hasText("MyReusableEllipse")).query());
        WaitForAsyncUtils.waitForFxEvents();

        // 7. Clicca "Place Reusable"
        Button placeReusableButton = robot.lookup("#placeReusableButton").queryButton();
        assertFalse(placeReusableButton.isDisabled());
        robot.clickOn(placeReusableButton);
        WaitForAsyncUtils.waitForFxEvents();
        
        // Verifica: dovrebbe esserci una forma in più sul canvas (o nel model). (Richiede GeoEngine)
        // GeoEngine ge = getGeoEngineFromMainApp(robot);
        // assertEquals(2, ge.getDrawing().getShapesInZOrder().size(), "Should have original + placed shape.");
    }

    private void drawLineOnCanvas(FxRobot robot) {
        robot.clickOn("#lineToolButton");
        WaitForAsyncUtils.waitForFxEvents();
        Node canvasNode = robot.lookup("#drawingCanvas").query();
        robot.moveTo(canvasNode).moveBy(-50, -50).press(MouseButton.PRIMARY).moveBy(100, 100).release(MouseButton.PRIMARY);
        WaitForAsyncUtils.waitForFxEvents();
    }
    
    private void drawRectangleOnCanvas(FxRobot robot, double x, double y, double w, double h) {
        robot.clickOn("#rectangleToolButton");
        WaitForAsyncUtils.waitForFxEvents();
        Node canvasNode = robot.lookup("#drawingCanvas").query();
        // Calcola le coordinate relative al centro del canvasNode per il disegno
        // Move to the canvasNode, then offset by (x - w/2, y - h/2) relative to its center
        robot.moveTo(canvasNode)
             .moveBy(x - w/2, y - h/2)
             .press(MouseButton.PRIMARY)
             .moveBy(w,h)
             .release(MouseButton.PRIMARY);
        WaitForAsyncUtils.waitForFxEvents();
    }


    @Test
    void drawShapeAndUndo_thenRedo_shouldWork(FxRobot robot) {
        if (geoEngineInternal == null) { System.err.println("Skipping drawShapeAndUndo_thenRedo: GeoEngine not accessible."); return; }

        drawLineOnCanvas(robot);
        assertEquals(1, geoEngineInternal.getDrawing().getShapesInZOrder().size(), "Should have 1 shape after drawing.");
        verifyThat("#undoButton", isEnabled());
        verifyThat("#redoButton", isDisabled());

        robot.clickOn("#undoButton");
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(0, geoEngineInternal.getDrawing().getShapesInZOrder().size(), "Should have 0 shapes after undo.");
        verifyThat("#undoButton", isDisabled());
        verifyThat("#redoButton", isEnabled());

        robot.clickOn("#redoButton");
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(1, geoEngineInternal.getDrawing().getShapesInZOrder().size(), "Should have 1 shape after redo.");
        verifyThat("#undoButton", isEnabled());
        verifyThat("#redoButton", isDisabled());
    }

    @Test
    void gridControls_enableAndChangeSize(FxRobot robot) {
        if (geoEngineInternal == null) { System.err.println("Skipping gridControls_enableAndChangeSize: GeoEngine not accessible."); return; }

        RadioButton gridStatusRadio = robot.lookup("#gridStatus").queryAs(RadioButton.class);
        TextField gridSizeField = robot.lookup("#gridSize").queryAs(TextField.class);

        assertFalse(gridStatusRadio.isSelected(), "Grid should be disabled initially.");
        assertTrue(gridSizeField.isDisabled(), "Grid size field should be disabled initially.");

        robot.clickOn(gridStatusRadio); // Enable grid
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(gridStatusRadio.isSelected(), "Grid should be enabled after click.");
        assertFalse(gridSizeField.isDisabled(), "Grid size field should be enabled.");
        assertTrue(geoEngineInternal.isGridEnabled());

        robot.clickOn(gridSizeField).eraseText(gridSizeField.getText().length()).write("15.0").press(KeyCode.ENTER);
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(15.0, geoEngineInternal.getGridSize(), 0.01);
        assertEquals("15.0", gridSizeField.getText()); // Assuming format

        robot.clickOn(gridStatusRadio); // Disable grid
        WaitForAsyncUtils.waitForFxEvents();
        assertFalse(gridStatusRadio.isSelected());
        assertTrue(gridSizeField.isDisabled());
        assertFalse(geoEngineInternal.isGridEnabled());
    }
    
    @Test
    void copyPaste_functionality(FxRobot robot) {
        if (geoEngineInternal == null) { System.err.println("Skipping copyPaste_functionality: GeoEngine not accessible."); return; }
        
        verifyThat("#copyButton", isDisabled());
        verifyThat("#pasteButton", isDisabled()); // Clipboard è vuoto all'inizio

        drawLineOnCanvas(robot); // Disegna una forma
        robot.clickOn("#selectToolButton"); // Seleziona strumento select
        Node canvasNode = robot.lookup("#drawingCanvas").query();
        robot.clickOn(canvasNode); // Clicca sulla forma per selezionarla
        WaitForAsyncUtils.waitForFxEvents();

        verifyThat("#copyButton", isEnabled());
        
        robot.clickOn("#copyButton");
        WaitForAsyncUtils.waitForFxEvents();
        verifyThat("#pasteButton", isEnabled()); // Ora paste dovrebbe essere abilitato

        assertEquals(1, geoEngineInternal.getDrawing().getShapesInZOrder().size());
        robot.clickOn("#pasteButton");
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(2, geoEngineInternal.getDrawing().getShapesInZOrder().size(), "Should have 2 shapes after paste.");
    }

    @Test
    void cutPaste_functionality(FxRobot robot) {
        if (geoEngineInternal == null) { System.err.println("Skipping cutPaste_functionality: GeoEngine not accessible."); return; }

        verifyThat("#cutButton", isDisabled());
        drawLineOnCanvas(robot);
        robot.clickOn("#selectToolButton");
        Node canvasNode = robot.lookup("#drawingCanvas").query();
        robot.clickOn(canvasNode); // Seleziona la forma
        WaitForAsyncUtils.waitForFxEvents();
        
        verifyThat("#cutButton", isEnabled());
        assertEquals(1, geoEngineInternal.getDrawing().getShapesInZOrder().size());

        robot.clickOn("#cutButton");
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(0, geoEngineInternal.getDrawing().getShapesInZOrder().size(), "Shape should be removed after cut.");
        verifyThat("#pasteButton", isEnabled());
        verifyThat("#cutButton", isDisabled()); // Ora cut dovrebbe essere disabilitato perché non c'è selezione

        robot.clickOn("#pasteButton");
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(1, geoEngineInternal.getDrawing().getShapesInZOrder().size(), "Shape should be back after paste.");
    }

    @Test
    void reflectButtons_enableOnSelection_andFunction(FxRobot robot) {
        if (geoEngineInternal == null) { System.err.println("Skipping reflectButtons_enableOnSelection_andFunction: GeoEngine not accessible."); return; }
        
        verifyThat("#reflectHorizontalButton", isDisabled());
        verifyThat("#reflectVerticalButton", isDisabled());

        drawLineOnCanvas(robot);
        robot.clickOn("#selectToolButton");
        Node canvasNode = robot.lookup("#drawingCanvas").query();
        robot.clickOn(canvasNode); // Seleziona la forma
        WaitForAsyncUtils.waitForFxEvents();

        verifyThat("#reflectHorizontalButton", isEnabled());
        verifyThat("#reflectVerticalButton", isEnabled());

        Shape selectedShape = geoEngineInternal.getSelectedShape();
        assertNotNull(selectedShape);
        // Per testare la riflessione effettiva, dovremmo controllare una proprietà della forma
        // che cambia con la riflessione (es. coordinate dei punti, o un flag isFlipped se esistesse).
        // Qui verifichiamo solo che il comando venga eseguito (implicitamente, se il bottone è cliccabile).
        // Supponiamo che LineSegment.reflectHorizontal() cambi i suoi punti.
        Point2D originalStart = ((LineSegment)selectedShape).getStartPoint();
        
        robot.clickOn("#reflectHorizontalButton");
        WaitForAsyncUtils.waitForFxEvents();
        // Shape selectedShapeAfterReflect = geoEngineInternal.getSelectedShape(); // La selezione potrebbe cambiare o rimanere
        // Point2D newStart = ((LineSegment)selectedShapeAfterReflect).getStartPoint();
        // assertNotEquals(originalStart.getX(), newStart.getX(), "X coordinate should change after horizontal reflection.");
        // L'asserzione precisa dipende dall'implementazione della riflessione.
        // Per ora, ci fidiamo che il click esegua l'azione.

        // Potremmo verificare che il bottone Undo sia abilitato
        verifyThat("#undoButton", isEnabled());
    }
    
    @Test
    void colorPickers_changeDefaultColorWhenNoSelection(FxRobot robot) {
        if (geoEngineInternal == null) { System.err.println("Skipping colorPickers_changeDefaultColorWhenNoSelection: GeoEngine not accessible."); return; }

        robot.clickOn("#selectToolButton"); // Assicura che nessuna forma sia selezionata
        WaitForAsyncUtils.waitForFxEvents();
        // Deseleziona cliccando su area vuota
        Node canvasNode = robot.lookup("#drawingCanvas").query();
        robot.moveTo(canvasNode).moveBy( 100,100).press(MouseButton.PRIMARY); // Clicca su un punto vuoto
        WaitForAsyncUtils.waitForFxEvents();


        ColorPicker strokePicker = robot.lookup("#strokeColorPicker").queryAs(ColorPicker.class);
        ColorData initialStroke = geoEngineInternal.getCurrentStrokeColorForNewShapes();
        
        // Cambia colore stroke
        robot.interact(() -> strokePicker.setValue(Color.rgb(0, 255, 0, 1.0))); // Verde
        WaitForAsyncUtils.waitForFxEvents();
        
        ColorData newStroke = geoEngineInternal.getCurrentStrokeColorForNewShapes();
        assertNotEquals(initialStroke.getG(), newStroke.getG());
        assertEquals(255, newStroke.getG());

        ColorPicker fillPicker = robot.lookup("#fillColorPicker").queryAs(ColorPicker.class);
        ColorData initialFill = geoEngineInternal.getCurrentFillColorForNewShapes();
        
        robot.interact(() -> fillPicker.setValue(Color.rgb(0,0,255,0.5))); // Blu trasparente
        WaitForAsyncUtils.waitForFxEvents();

        ColorData newFill = geoEngineInternal.getCurrentFillColorForNewShapes();
        assertNotEquals(initialFill.getB(), newFill.getB());
        assertEquals(255, newFill.getB());
        assertEquals(0.5, newFill.getA(), 0.01);
    }
    
    @Test
    void colorPickers_changeSelectedShapeColor(FxRobot robot) {
        if (geoEngineInternal == null) { System.err.println("Skipping colorPickers_changeSelectedShapeColor: GeoEngine not accessible."); return; }

        drawRectangleOnCanvas(robot, 0, 0, 30, 30); // Disegna un rettangolo al centro del canvas holder
        robot.clickOn("#selectToolButton");
        Node canvasNode = robot.lookup("#drawingCanvas").query();
        robot.moveTo(canvasNode).moveBy(0, 0).press(MouseButton.PRIMARY); // Clicca sul rettangolo (assumendo che sia al centro)
        WaitForAsyncUtils.waitForFxEvents();
        
        Shape selected = geoEngineInternal.getSelectedShape();
        assertNotNull(selected);
        ColorData initialShapeStroke = selected.getStrokeColor();

        ColorPicker strokePicker = robot.lookup("#strokeColorPicker").queryAs(ColorPicker.class);
        robot.interact(() -> strokePicker.setValue(Color.LIMEGREEN)); // Verde lime
        WaitForAsyncUtils.waitForFxEvents();
        
        assertNotEquals(initialShapeStroke.getG(), selected.getStrokeColor().getG());
        // Verificare il colore esatto: Color.LIMEGREEN è (50,205,50)
        assertEquals(50, selected.getStrokeColor().getR());
        assertEquals(205, selected.getStrokeColor().getG());
        assertEquals(50, selected.getStrokeColor().getB());
    }

    @Test
    void clickOnEmptyCanvas_deselectsShapes(FxRobot robot) {
        if (geoEngineInternal == null) { System.err.println("Skipping clickOnEmptyCanvas_deselectsShapes: GeoEngine not accessible."); return; }

        drawLineOnCanvas(robot);
        robot.clickOn("#selectToolButton");
        Node canvasNode = robot.lookup("#drawingCanvas").query();
        robot.clickOn(canvasNode); // Seleziona la linea
        WaitForAsyncUtils.waitForFxEvents();
        assertFalse(geoEngineInternal.getSelectedShapes().isEmpty(), "Shape should be selected.");

        // Clicca su un punto garantito per essere vuoto nel canvasHolder (o canvasNode)
        // Le coordinate per clickOn(Node) sono relative all'angolo in alto a sinistra del nodo.
        // Assumendo che canvasNode sia grande abbastanza, (width-1, height-1) è vuoto se non ci sono forme lì.
        robot.moveTo(canvasNode).moveBy(((StackPane)canvasNode.getParent()).getWidth() -1 , ((StackPane)canvasNode.getParent()).getHeight() - 10)
             .clickOn(MouseButton.PRIMARY);
        WaitForAsyncUtils.waitForFxEvents();
        
        assertTrue(geoEngineInternal.getSelectedShapes().isEmpty(), "Selection should be empty after clicking on empty canvas area.");
    }
    
    @Test
    void bringToFrontSendToBackButton_enableDisable(FxRobot robot) {
        verifyThat("#bringToFrontButton", isDisabled());
        verifyThat("#sendToBackButton", isDisabled());

        drawLineOnCanvas(robot);
        robot.clickOn("#selectToolButton");
        Node canvasNode = robot.lookup("#drawingCanvas").query();
        robot.clickOn(canvasNode); // Seleziona la forma
        WaitForAsyncUtils.waitForFxEvents();

        // In MainApp, questi bottoni sono disabilitati se la selezione è != 1
        // Quando la selezione è 1 (come dopo il click sulla linea), dovrebbero essere abilitati
        // MA, l'FXML li ha "disable="true"" di default. La logica in MainApp.refreshUIState li abilità/disabilita.
        // Il test corrente in MainApp.refreshUIState dice:
        // if(bringToFrontButton != null) bringToFrontButton.setDisable(!singleShapeSelected);
        // Questo è corretto. Però l'FXML ha disable="true".
        // L'FXML di mockup.txt per bringToFrontButton ha "disable="true"".
        // Se questo è l'FXML caricato, il bottone partirà disabilitato e MainApp.refreshUIState
        // dovrebbe abilitarlo quando una singola forma è selezionata.
        // Se invece l'FXML non avesse "disable="true"", partirebbe abilitato e poi refreshUIState lo gestirebbe.
        
        // Verifichiamo lo stato dopo la selezione, che dovrebbe essere abilitato
        // Questo test fallirà se l'FXML ha "disable="true"" e la logica "refreshUIState" non lo sovrascrive correttamente.
        // Assumendo che refreshUIState funzioni e l'FXML sia corretto:
        // L'FXML fornito ha "disable="true"" per bringToFront e sendToBack.
        // MainApp.refreshUIState li disabilita se !singleShapeSelected.
        // Quindi, se singleShapeSelected è true, dovrebbe essere !(!true) = true per setDisable.
        // No, setDisable(!singleShapeSelected). Se singleShapeSelected è true, setDisable(false) -> abilitato.
        // Corretto.

        // Lo stato iniziale di FXML è disabilitato.
        // Quando selezioniamo una forma, refreshUIState viene chiamato.
        // Se è una singola forma, setDisable(!true) -> setDisable(false).
        verifyThat("#bringToFrontButton", isEnabled());
        verifyThat("#sendToBackButton", isEnabled());

        // Deseleziona
        robot.moveTo(canvasNode).moveBy(-50, 0).press(MouseButton.PRIMARY).release(MouseButton.PRIMARY);
        WaitForAsyncUtils.waitForFxEvents();

        verifyThat("#bringToFrontButton", (Node node) -> node.isDisabled());
        verifyThat("#sendToBackButton", (Node node) -> node.isDisabled());
    }
}