package sad.gruppo11;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.control.LabeledMatchers;

import java.io.IOException; // Per la firma di MainApp.start
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

// Importa i tuoi stati specifici
import sad.gruppo11.Controller.GeoEngine;
import sad.gruppo11.Controller.LineState;
import sad.gruppo11.Controller.RectangleState;
import sad.gruppo11.Controller.EllipseState;
import sad.gruppo11.Controller.SelectState;
import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.EllipseShape;
import sad.gruppo11.Model.LineSegment;
import sad.gruppo11.Model.RectangleShape;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Rect;
import sad.gruppo11.Model.geometry.Vector2D;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;


@ExtendWith(ApplicationExtension.class)
class MainAppUITest extends ApplicationTest {

    private Stage primaryStage;
    private MainApp mainAppInstance; // Riferimento all'istanza di MainApp

    // Non è necessario @BeforeAll per FxToolkit.registerPrimaryStage() se si estende ApplicationTest
    // e si usa l'estensione ApplicationExtension.class, che lo gestisce.

    @Override
    public void start(Stage stage) throws Exception {
        this.primaryStage = stage;
        mainAppInstance = new MainApp(); // Crea e salva l'istanza
        mainAppInstance.start(stage);    // Avvia l'applicazione
    }

    @AfterEach
    void tearDown() throws TimeoutException {
        FxToolkit.cleanupStages();
    }

    @Test
    @DisplayName("L'applicazione dovrebbe avviarsi e avere il titolo corretto")
    void testApplicationLaunchesAndHasCorrectTitle(FxRobot robot) {
        assertThat(primaryStage.getTitle()).isEqualTo("GeoDraw");
        assertNotNull(primaryStage.getScene(), "La scena primaria non dovrebbe essere nulla.");
        verifyThat("#selectToolButton", Node::isVisible);
    }

    private GeoEngine getActiveGeoEngine() {
        // Metodo helper per ottenere GeoEngine in modo sicuro
        assertNotNull(mainAppInstance, "MainApp instance non è stata inizializzata.");
        GeoEngine engine = mainAppInstance.getGeoEngineInstance();
        assertNotNull(engine, "GeoEngine instance non è stata inizializzata in MainApp.");
        return engine;
    }

    @Test
    @DisplayName("Cliccare Line Tool Button dovrebbe cambiare lo stato di GeoEngine in LineState")
    void testClickLineToolButtonChangesState(FxRobot robot) {
        robot.clickOn("#lineToolButton");
        waitForFxEvents();

        GeoEngine engine = getActiveGeoEngine();
        assertThat(engine.getCurrentState()).isInstanceOf(LineState.class);
        // Opzionale: verifica lo stile del bottone se implementato
        verifyThat("#lineToolButton", (Button btn) -> btn.getStyle().toLowerCase().contains("lightblue")); // Adatta allo stile reale
        verifyThat("#selectToolButton", (Button btn) -> !btn.getStyle().toLowerCase().contains("lightblue"));
    }

    @Test
    @DisplayName("Cliccare Rectangle Tool Button dovrebbe cambiare lo stato di GeoEngine in RectangleState")
    void testClickRectangleToolButtonChangesState(FxRobot robot) {
        robot.clickOn("#rectangleToolButton");
        waitForFxEvents();

        GeoEngine engine = getActiveGeoEngine();
        assertThat(engine.getCurrentState()).isInstanceOf(RectangleState.class);
        verifyThat("#rectangleToolButton", (Button btn) -> btn.getStyle().toLowerCase().contains("lightblue"));
    }

    @Test
    @DisplayName("Cliccare Ellipse Tool Button dovrebbe cambiare lo stato di GeoEngine in EllipseState")
    void testClickEllipseToolButtonChangesState(FxRobot robot) {
        robot.clickOn("#ellipseToolButton");
        waitForFxEvents();

        GeoEngine engine = getActiveGeoEngine();
        assertThat(engine.getCurrentState()).isInstanceOf(EllipseState.class);
        verifyThat("#ellipseToolButton", (Button btn) -> btn.getStyle().toLowerCase().contains("lightblue"));
    }

    @Test
    @DisplayName("Cliccare Select Tool Button dovrebbe cambiare lo stato di GeoEngine in SelectState")
    void testClickSelectToolButtonChangesState(FxRobot robot) {
        // Prima cambia a un altro stato per assicurarsi che il click su Select abbia effetto
        robot.clickOn("#lineToolButton");
        waitForFxEvents();
        assertThat(getActiveGeoEngine().getCurrentState()).isInstanceOf(LineState.class); // Pre-condizione

        // Ora clicca su Select Tool
        robot.clickOn("#selectToolButton");
        waitForFxEvents();

        GeoEngine engine = getActiveGeoEngine();
        assertThat(engine.getCurrentState()).isInstanceOf(SelectState.class);
        verifyThat("#selectToolButton", (Button btn) -> btn.getStyle().toLowerCase().contains("lightblue"));
    }

    private Point2D fromLocalToScreen(Node node, javafx.geometry.Point2D point) {
        // Metodo helper per convertire le coordinate locali in coordinate dello schermo
        javafx.geometry.Point2D screenPoint = node.localToScreen(point);
        return new Point2D(screenPoint.getX(), screenPoint.getY());
    }

    @Test
    @DisplayName("Disegnare un rettangolo dovrebbe aggiungere un RectangleShape al modello")
    void testDrawRectangleOnCanvas(FxRobot robot) {
        GeoEngine engine = getActiveGeoEngine();
        Drawing drawing = engine.getDrawing(); // Ottieni il modello Drawing

        // 0. Salva il numero iniziale di forme per la verifica successiva
        int initialShapeCount = drawing.getShapes().size();

        // 1. Clicca sul bottone "Rectangle Tool"
        robot.clickOn("#rectangleToolButton");
        waitForFxEvents();
        assertThat(engine.getCurrentState()).isInstanceOf(RectangleState.class); // Conferma stato

        // 2. Identifica il Canvas. Assicurati che il tuo Canvas abbia fx:id="actualCanvas"
        //    nel FXML, o un ID impostato programmaticamente se non usi FXML per esso.
        //    Il tuo MainApp crea il canvas e lo aggiunge a canvasHolder.
        //    Dobbiamo trovare il Canvas all'interno di canvasHolder.
        //    Se canvasHolder è lo StackPane e il Canvas è il suo unico figlio (o il primo rilevante):
        Node canvasNode = robot.lookup("#actualDrawingCanvas").query(); // Selettore CSS per Canvas dentro canvasHolder
        // Alternativa se l'ID è direttamente sul Canvas (preferibile se possibile):
        // Node canvasNode = robot.lookup("#actualCanvas").query();

        assertNotNull(canvasNode, "Canvas non trovato nella UI.");
        assertTrue(canvasNode instanceof javafx.scene.canvas.Canvas, "Il nodo trovato non è un Canvas.");

        // 3. Definisci i punti per il disegno del rettangolo sul canvas
        javafx.geometry.Point2D pressPoint = new javafx.geometry.Point2D(50, 50);
        javafx.geometry.Point2D releasePoint = new javafx.geometry.Point2D(150, 100); // width=100, height=50

        // 4. Simula l'operazione di disegno
        // moveTo() sposta il cursore, press() preme il pulsante, drag() sposta tenendo premuto, release() rilascia.
        // Le coordinate sono relative al nodo canvasNode.
        robot.moveTo(canvasNode, pressPoint)
            .press(MouseButton.PRIMARY)
            .moveTo(canvasNode, releasePoint)
            .release(MouseButton.PRIMARY);
        waitForFxEvents(); // Importante per processare gli eventi mouse e l'eventuale creazione del comando

        // 5. Verifica che una nuova forma sia stata aggiunta al modello
        List<Shape> shapesAfterDraw = drawing.getShapes();
        assertThat(shapesAfterDraw.size()).isEqualTo(initialShapeCount + 1);

        // 6. Verifica che l'ultima forma aggiunta sia un RectangleShape
        Shape newShape = shapesAfterDraw.get(shapesAfterDraw.size() - 1); // Prendi l'ultima forma
        assertThat(newShape).isInstanceOf(RectangleShape.class);

        // 7. (Opzionale) Verifica i punti del rettangolo aggiunto
        RectangleShape drawnRectangle = (RectangleShape) newShape;
        /*
        javafx.geometry.Point2D startPoint = canvasNode.localToScreen(pressPoint);
        javafx.geometry.Point2D endPoint = canvasNode.localToScreen(releasePoint);
        Rect bounds = drawnRectangle.getBounds();
        double expectedX = Math.min(startPoint.getX(), endPoint.getX());
        double expectedY = Math.min(startPoint.getY(), endPoint.getY());
        double expectedWidth = Math.abs(startPoint.getX() - endPoint.getX());
        double expectedHeight = Math.abs(startPoint.getY() - endPoint.getY());

        assertThat(bounds.getX()).isEqualTo(expectedX, within(0.1));
        assertThat(bounds.getY()).isEqualTo(expectedY, within(0.1));
        assertThat(bounds.getWidth()).isEqualTo(expectedWidth, within(0.1));
        assertThat(bounds.getHeight()).isEqualTo(expectedHeight, within(0.1));
        */


        // 8. (Opzionale) Verifica i colori di default della nuova forma
        ColorData expectedStroke = engine.getCurrentStrokeColorForNewShapes(); // Colore al momento del test
        ColorData expectedFill = engine.getCurrentFillColorForNewShapes();   // Colore al momento del test

        // Il confronto diretto con == funziona per ColorData.BLACK/TRANSPARENT se sono le istanze statiche.
        // Se i colori sono creati dinamicamente, usa isEqualTo().
        assertThat(drawnRectangle.getStrokeColor()).isEqualTo(expectedStroke);
        assertThat(drawnRectangle.getFillColor()).isEqualTo(expectedFill);

        // Pulisci per il prossimo test (opzionale, ma buona pratica se si modifica il modello)
        // Potresti voler creare un comando di clear o rimuovere la forma aggiunta.
        // Per ora, lo lasciamo. Il @AfterEach con FxToolkit.cleanupStages() resetta la UI.
    }

    @Test
    @DisplayName("Disegnare una linea dovrebbe aggiungere un LineSegment al modello")
    void testDrawLineSegmentOnCanvas(FxRobot robot) {
        GeoEngine engine = getActiveGeoEngine();
        Drawing drawing = engine.getDrawing();

        // 0. Salva il numero iniziale di forme
        int initialShapeCount = drawing.getShapes().size();

        // 1. Clicca sul bottone "Line Tool"
        // Assicurati che fx:id="lineToolButton" esista nel tuo FXML
        robot.clickOn("#lineToolButton");
        waitForFxEvents();
        assertThat(engine.getCurrentState()).isInstanceOf(LineState.class);

        // 2. Identifica il Canvas
        Node canvasNode = robot.lookup("#actualDrawingCanvas").query(); // O il tuo selettore per il canvas
        assertNotNull(canvasNode, "Canvas non trovato nella UI.");

        // 3. Definisci i punti per il disegno della linea sul canvas
        javafx.geometry.Point2D pressPoint = new javafx.geometry.Point2D(20, 30);
        javafx.geometry.Point2D releasePoint = new javafx.geometry.Point2D(120, 180);

        // 4. Simula l'operazione di disegno
        robot.moveTo(canvasNode, pressPoint)
            .press(MouseButton.PRIMARY)
            .moveTo(canvasNode, releasePoint)
            .release(MouseButton.PRIMARY);
        waitForFxEvents();

        // 5. Verifica che una nuova forma sia stata aggiunta al modello
        List<Shape> shapesAfterDraw = drawing.getShapes();
        assertThat(shapesAfterDraw.size()).isEqualTo(initialShapeCount + 1);

        // 6. Verifica che l'ultima forma aggiunta sia un LineSegment
        Shape newShape = shapesAfterDraw.get(shapesAfterDraw.size() - 1);
        assertThat(newShape).isInstanceOf(LineSegment.class);

        // 7. (Opzionale più dettagliato) Verifica i punti della linea aggiunta
        LineSegment addedLine = (LineSegment) newShape;
        /*
        // LineSegment memorizza copie dei punti, quindi confronta i valori
        javafx.geometry.Point2D startPoint = pressPoint;  // canvasNode.screenToLocal(pressPoint);
        javafx.geometry.Point2D endPoint = releasePoint;  //canvasNode.screenToLocal(releasePoint);

        Rect bounds = addedLine.getBounds();
        javafx.geometry.Point2D startPoint_drawn = new javafx.geometry.Point2D(bounds.getTopLeft().getX(), bounds.getTopLeft().getY());
        javafx.geometry.Point2D endPoint_drawn = new javafx.geometry.Point2D(bounds.getBottomRight().getX(), bounds.getBottomRight().getY());

        startPoint_drawn = canvasNode.screenToLocal(startPoint_drawn);
        endPoint_drawn = canvasNode.screenToLocal(endPoint_drawn);

        System.out.println("[TEST_OUTPUT] Robot Input Press: " + pressPoint);
        System.out.println("[TEST_OUTPUT] Robot Input Release: " + releasePoint);
        System.out.println("[TEST_OUTPUT] Actual Line Created - Start: " + startPoint_drawn + ", End: " + endPoint_drawn);


        assertThat(startPoint.getX()).isEqualTo(startPoint_drawn.getX(), within(0.1));
        assertThat(startPoint.getY()).isEqualTo(startPoint_drawn.getY(), within(0.1));
        assertThat(endPoint.getX()).isEqualTo(endPoint_drawn.getX(), within(0.1));
        assertThat(endPoint.getY()).isEqualTo(endPoint_drawn.getY(), within(0.1));
         */

        // 8. (Opzionale) Verifica il colore di stroke (le linee non hanno fill)
        ColorData expectedStroke = engine.getCurrentStrokeColorForNewShapes();
        assertThat(addedLine.getStrokeColor()).isEqualTo(expectedStroke);
        assertThat(addedLine.getFillColor()).isNull(); // Le linee non dovrebbero avere colore di riempimento
    }

    @Test
    @DisplayName("Disegnare un'ellisse dovrebbe aggiungere una EllipseShape al modello")
    void testDrawEllipseOnCanvas(FxRobot robot) {
        GeoEngine engine = getActiveGeoEngine();
        Drawing drawing = engine.getDrawing();

        // 0. Salva il numero iniziale di forme
        int initialShapeCount = drawing.getShapes().size();

        // 1. Clicca sul bottone "Ellipse Tool"
        // Assicurati che fx:id="ellipseToolButton" esista nel tuo FXML
        robot.clickOn("#ellipseToolButton");
        waitForFxEvents();
        assertThat(engine.getCurrentState()).isInstanceOf(EllipseState.class);

        // 2. Identifica il Canvas
        Node canvasNode = robot.lookup("#canvasHolder > Canvas").query(); // O il tuo selettore
        assertNotNull(canvasNode, "Canvas non trovato nella UI.");

        // 3. Definisci i punti per il disegno dell'ellisse (definiranno il bounding box)
        javafx.geometry.Point2D pressPoint = new javafx.geometry.Point2D(70, 80);
        javafx.geometry.Point2D releasePoint = new javafx.geometry.Point2D(170, 140); // width=100, height=60

        // 4. Simula l'operazione di disegno
        robot.moveTo(canvasNode, pressPoint)
            .press(MouseButton.PRIMARY)
            .moveTo(canvasNode, releasePoint)
            .release(MouseButton.PRIMARY);
        waitForFxEvents();

        // 5. Verifica che una nuova forma sia stata aggiunta al modello
        List<Shape> shapesAfterDraw = drawing.getShapes();
        assertThat(shapesAfterDraw.size()).isEqualTo(initialShapeCount + 1);

        // 6. Verifica che l'ultima forma aggiunta sia una EllipseShape
        Shape newShape = shapesAfterDraw.get(shapesAfterDraw.size() - 1);
        assertThat(newShape).isInstanceOf(EllipseShape.class);

        // 7. (Opzionale più dettagliato) Verifica i bounds dell'ellisse aggiunta
        EllipseShape addedEllipse = (EllipseShape) newShape;
        /* 
        Rect bounds = addedEllipse.getBounds();
        double expectedX = Math.min(pressPoint.getX(), releasePoint.getX());
        double expectedY = Math.min(pressPoint.getY(), releasePoint.getY());
        double expectedWidth = Math.abs(pressPoint.getX() - releasePoint.getX());
        double expectedHeight = Math.abs(pressPoint.getY() - releasePoint.getY());

        assertThat(bounds.getX()).isEqualTo(expectedX, within(0.1));
        assertThat(bounds.getY()).isEqualTo(expectedY, within(0.1));
        assertThat(bounds.getWidth()).isEqualTo(expectedWidth, within(0.1));
        assertThat(bounds.getHeight()).isEqualTo(expectedHeight, within(0.1));
        */

        // 8. (Opzionale) Verifica i colori di default
        ColorData expectedStroke = engine.getCurrentStrokeColorForNewShapes();
        ColorData expectedFill = engine.getCurrentFillColorForNewShapes();
        assertThat(addedEllipse.getStrokeColor()).isEqualTo(expectedStroke);
        assertThat(addedEllipse.getFillColor()).isEqualTo(expectedFill);
    }

    @Test
    @DisplayName("Cliccare su una forma con lo strumento Select dovrebbe selezionarla nel modello")
    void testSelectShapeOnCanvas(FxRobot robot) {
        GeoEngine engine = getActiveGeoEngine();
        // Opzionale: pulisci il disegno per un test isolato
        // engine.createNewDrawing();
        // waitForFxEvents();

        // --- Fase 1: Disegna una forma (un rettangolo) ---
        robot.clickOn("#rectangleToolButton");
        waitForFxEvents();

        Node canvasNode = robot.lookup("#actualDrawingCanvas").query(); // Assumendo fx:id="actualDrawingCanvas"

        // Punti per disegnare il rettangolo. Assumiamo che questi siano interpretati correttamente
        // dal tuo sistema ora, o che useresti screenToLocal per le verifiche dei bounds se necessario.
        javafx.geometry.Point2D rectPressPoint = new javafx.geometry.Point2D(50, 50);
        javafx.geometry.Point2D rectReleasePoint = new javafx.geometry.Point2D(150, 100); // Rettangolo 100x50 a (50,50)

        robot.moveTo(canvasNode, rectPressPoint)
            .press(MouseButton.PRIMARY)
            .moveTo(canvasNode, rectReleasePoint)
            .release(MouseButton.PRIMARY);
        waitForFxEvents();

        // Verifica che il rettangolo sia stato aggiunto (e prendi un riferimento ad esso)
        assertThat(engine.getDrawing().getShapes()).hasSize(1); // Assumendo disegno pulito all'inizio
        Shape drawnShape = engine.getDrawing().getShapes().get(0);
        assertThat(drawnShape).isInstanceOf(RectangleShape.class);
        // System.out.println("Disegnato: " + drawnShape + " con bounds: " + drawnShape.getBounds());

        // --- Fase 2: Attiva lo strumento Select ---
        robot.clickOn("#selectToolButton");
        waitForFxEvents();
        assertThat(engine.getCurrentState()).isInstanceOf(SelectState.class);
        // Inizialmente, nessuna forma dovrebbe essere selezionata o la forma appena disegnata non è selezionata
        // (SelectState non seleziona automaticamente al cambio di stato)
        // engine.setCurrentlySelectedShape(null); // Assicurati che nulla sia selezionato prima del click di selezione
        // waitForFxEvents();
        // assertThat(engine.getSelectedShape()).isNull();

        // --- Fase 3: Clicca sulla forma disegnata per selezionarla ---
        // Dobbiamo cliccare su un punto DENTRO i bounds della forma disegnata.
        // Il rettangolo è stato disegnato tra (50,50) e (150,100) nel sistema di coordinate LOCALE del canvas.
        // Usiamo un punto centrale.
        // Se i punti di input di FxRobot sono "schermo/scena", dobbiamo convertirli.
        // Per semplicità, se il canvas è all'origine e non scalato,
        // un click a (75,75) locale dovrebbe essere su un rettangolo che va da (50,50) a (150,100).

        // Punto di click per la selezione (in coordinate relative al canvasNode)
        // Il centro del nostro rettangolo (50,50) w=100,h=50 è (50+50, 50+25) = (100,75)
        javafx.geometry.Point2D clickToSelectPoint = new javafx.geometry.Point2D(100, 75);

        // Se c'è discrepanza tra coordinate robot e coordinate canvas, potresti aver bisogno di:
        // clickToSelectPoint = canvasNode.localToScreen(new javafx.geometry.Point2D(100,75)); // Se input robot è schermo
        // Ma di solito per un click semplice, le coordinate relative al nodo funzionano.

        // System.out.println("Tentativo di click per selezione a: " + clickToSelectPoint + " su canvasNode");
        robot.moveTo(canvasNode, clickToSelectPoint)
              .clickOn(MouseButton.PRIMARY); // Click sul punto
        waitForFxEvents();

        // --- Fase 4: Verifica che la forma sia selezionata ---
        assertThat(engine.getSelectedShape()).isNotNull();
        assertThat(engine.getSelectedShape().getId()).isEqualTo(drawnShape.getId());
        assertThat(engine.getSelectedShape()).isSameAs(drawnShape); // Verifica che sia la stessa istanza

        // --- Fase 5: (Opzionale) Clicca su un'area vuota per deselezionare ---
        javafx.geometry.Point2D clickEmptyPoint = new javafx.geometry.Point2D(10, 10); // Assumendo che sia fuori dalla forma
        // Stampa i bounds per assicurarti che il punto sia effettivamente fuori
        // System.out.println("Bounds della forma disegnata: " + drawnShape.getBounds());
        // System.out.println("Click per deselezionare a: " + clickEmptyPoint);

        robot.moveTo(canvasNode, clickEmptyPoint)
              .clickOn(MouseButton.PRIMARY);
        waitForFxEvents();
        assertThat(engine.getSelectedShape()).isNull();
    }

    @Test
    @DisplayName("Spostare una forma selezionata dovrebbe aggiornare i suoi bounds nel modello")
    void testMoveSelectedShapeOnCanvas(FxRobot robot) {
        GeoEngine engine = getActiveGeoEngine();
        // engine.createNewDrawing(); // Opzionale: per stato pulito
        // waitForFxEvents();
        Drawing drawing = engine.getDrawing();

        // --- Fase 1: Disegna una forma (un rettangolo) ---
        robot.clickOn("#rectangleToolButton");
        waitForFxEvents();
        Node canvasNode = robot.lookup("#actualDrawingCanvas").query();

        javafx.geometry.Point2D rectPressPoint = new javafx.geometry.Point2D(50, 50);
        javafx.geometry.Point2D rectReleasePoint = new javafx.geometry.Point2D(150, 100); // Rettangolo a (50,50) w=100, h=50

        robot.moveTo(canvasNode, rectPressPoint)
            .press(MouseButton.PRIMARY)
            .moveTo(canvasNode, rectReleasePoint)
            .release(MouseButton.PRIMARY);
        waitForFxEvents();

        assertThat(drawing.getShapes()).hasSize(1);
        Shape drawnShape = drawing.getShapes().get(0);
        Rect initialBounds = drawnShape.getBounds(); // Salva i bounds iniziali (locali)

        // --- Fase 2: Seleziona la forma ---
        robot.clickOn("#selectToolButton");
        waitForFxEvents();

        // Punto di click per selezionare la forma (centro del rettangolo)
        javafx.geometry.Point2D selectClickPoint = new javafx.geometry.Point2D(
            rectPressPoint.getX() + Math.abs(rectPressPoint.getX()-rectReleasePoint.getX()) / 2,
            rectReleasePoint.getY() - Math.abs(rectPressPoint.getY()-rectReleasePoint.getY()) / 2
        );
        // Se necessario, converti in coordinate "schermo/scena" per FxRobot se i tuoi input sono così
        // javafx.geometry.Point2D robotSelectClickInput = canvasNode.localToScreen(selectClickPoint);
        // robot.clickOn(canvasNode, robotSelectClickInput);
        robot.moveTo(canvasNode, selectClickPoint).clickOn(MouseButton.PRIMARY); // Assumendo che le coordinate del click siano locali al canvasNode
        waitForFxEvents();
        assertThat(engine.getSelectedShape()).isSameAs(drawnShape); // Verifica selezione

        // --- Fase 3: Sposta la forma selezionata ---
        // Definisci da dove a dove vuoi trascinare la forma.
        // Il drag inizia dal punto in cui è stata selezionata (o vicino).
        // Qui, per semplicità, iniziamo il drag dal punto `selectClickPoint`.
        // Spostiamo la forma di (dx=60, dy=50)
        double dx = 60;
        double dy = 50;
        javafx.geometry.Point2D dragEndPoint = new javafx.geometry.Point2D(
            selectClickPoint.getX() + dx,
            selectClickPoint.getY() + dy
        );

        // Se necessario, converti in coordinate "schermo/scena" per FxRobot
        // javafx.geometry.Point2D robotDragStartInput = canvasNode.localToScreen(selectClickPoint);
        // javafx.geometry.Point2D robotDragEndInput = canvasNode.localToScreen(dragEndPoint);
        // robot.moveTo(canvasNode, robotDragStartInput)
        //     .press(MouseButton.PRIMARY)
        //     .moveTo(canvasNode, robotDragEndInput) // o .dragTo(canvasNode, robotDragEndInput)
        //     .release(MouseButton.PRIMARY);

        // Assumendo che l'input di FxRobot sia locale al canvasNode per il drag
        robot.moveTo(canvasNode, selectClickPoint) // Muovi al punto di inizio del drag (dove è selezionata)
            .press(MouseButton.PRIMARY)          // Premi
            .moveTo(canvasNode, dragEndPoint)    // Trascina al punto finale
            .release(MouseButton.PRIMARY);         // Rilascia
        waitForFxEvents(); // Permetti al MoveShapeCommand di essere eseguito

        // --- Fase 4: Verifica i nuovi bounds della forma nel modello ---
        Shape movedShape = engine.getDrawing().getShapes().get(0); // Prendi la forma (dovrebbe essere la stessa istanza)
        assertThat(movedShape.getId()).isEqualTo(drawnShape.getId()); // Assicurati che sia la stessa forma

        Rect finalBounds = movedShape.getBounds();

        double expectedFinalX = initialBounds.getX() + dx;
        double expectedFinalY = initialBounds.getY() + dy;

        // System.out.println("Initial Bounds: " + initialBounds);
        // System.out.println("Expected dx,dy: " + dx + "," + dy);
        // System.out.println("Final Bounds: " + finalBounds);
        // System.out.println("Expected Final TopLeft: (" + expectedFinalX + "," + expectedFinalY + ")");


        assertThat(finalBounds.getX()).isEqualTo(expectedFinalX, within(0.1));
        assertThat(finalBounds.getY()).isEqualTo(expectedFinalY, within(0.1));
        // La larghezza e l'altezza non dovrebbero cambiare durante uno spostamento
        assertThat(finalBounds.getWidth()).isEqualTo(initialBounds.getWidth(), within(0.1));
        assertThat(finalBounds.getHeight()).isEqualTo(initialBounds.getHeight(), within(0.1));

        // --- Fase 5: (Opzionale) Verifica che un MoveShapeCommand sia stato usato ---
        // Questo richiederebbe di spiare il CommandManager o avere un modo per ispezionare lo stack undo.
        // Per ora, ci fidiamo che se i bounds sono cambiati, il comando è stato eseguito.
        // Se hai un CommandManager accessibile e vuoi verificarlo:
        // ArgumentCaptor<Command> cmdCaptor = ArgumentCaptor.forClass(Command.class);
        // verify(engine.getCommandManager(), atLeastOnce()).execute(cmdCaptor.capture()); // atLeastOnce perché c'è anche AddShapeCommand
        // boolean moveCommandExecuted = cmdCaptor.getAllValues().stream().anyMatch(cmd -> cmd instanceof MoveShapeCommand);
        // assertTrue(moveCommandExecuted, "Un MoveShapeCommand avrebbe dovuto essere eseguito.");
    }


    private Shape drawAndSelectRectangle(FxRobot robot, GeoEngine engine) {
        // Helper per disegnare e selezionare un rettangolo
        // engine.createNewDrawing(); // Opzionale per pulire
        // waitForFxEvents();

        robot.clickOn("#rectangleToolButton");
        waitForFxEvents();
        Node canvasNode = robot.lookup("#actualDrawingCanvas").query();

        javafx.geometry.Point2D rectPressPoint = new javafx.geometry.Point2D(50, 50);
        javafx.geometry.Point2D rectReleasePoint = new javafx.geometry.Point2D(150, 100);

        robot.moveTo(canvasNode, rectPressPoint)
            .press(MouseButton.PRIMARY)
            .moveTo(canvasNode, rectReleasePoint)
            .release(MouseButton.PRIMARY);
        waitForFxEvents();

        assertThat(engine.getDrawing().getShapes()).hasSize(1); // Assumendo disegno pulito o conteggio relativo
        Shape drawnShape = engine.getDrawing().getShapes().get(0);

        robot.clickOn("#selectToolButton");
        waitForFxEvents();

        javafx.geometry.Point2D selectClickPoint = new javafx.geometry.Point2D(100, 75); // Centro del rettangolo disegnato
        // Se necessario, converti selectClickPoint per FxRobot input
        // robot.clickOn(canvasNode, canvasNode.localToScreen(selectClickPoint));
        robot.moveTo(canvasNode, selectClickPoint).clickOn(MouseButton.PRIMARY);
        waitForFxEvents();
        assertThat(engine.getSelectedShape()).isSameAs(drawnShape);
        return drawnShape;
    }

    @Test
    @DisplayName("Cambiare il colore del bordo tramite ColorPicker dovrebbe aggiornare la forma selezionata")
    void testChangeStrokeColorViaPicker(FxRobot robot) {
        GeoEngine engine = getActiveGeoEngine();
        Shape selectedShape = drawAndSelectRectangle(robot, engine); // Disegna e seleziona

        // Colori attesi (usa i tuoi oggetti ColorData per il confronto)
        ColorData initialStrokeColor = selectedShape.getStrokeColor();
        sad.gruppo11.Model.geometry.ColorData newModelStrokeColor = new sad.gruppo11.Model.geometry.ColorData(ColorData.BLUE); // Blu
        javafx.scene.paint.Color newFxStrokeColor = Color.BLUE; // Il colore JavaFX corrispondente

        // Assicurati che il colore iniziale non sia già quello nuovo
        assertThat(initialStrokeColor).isNotEqualTo(newModelStrokeColor);

        // 1. Trova il ColorPicker per lo stroke
        // Assicurati che fx:id="strokeColorPicker" esista nel tuo FXML
        ColorPicker strokePicker = robot.lookup("#strokeColorPicker").query();

        // 2. Imposta il nuovo colore e scatena l'evento action
        // È importante eseguire queste modifiche UI sul thread JavaFX se interagiscono con listener
        robot.interact(() -> {
            strokePicker.setValue(newFxStrokeColor);
            // Scatenare l'evento action è cruciale per far partire l'handler di MainApp
            strokePicker.fireEvent(new ActionEvent(strokePicker, strokePicker));
        });
        waitForFxEvents(); // Attendi che il comando ChangeStrokeColorCommand venga processato

        // 3. Verifica che il colore del bordo della forma nel modello sia cambiato
        Shape shapeAfterChange = engine.getDrawing().getShapes().get(0); // Prendi la forma (dovrebbe essere la stessa istanza)
        assertThat(shapeAfterChange.getId()).isEqualTo(selectedShape.getId());
        assertThat(shapeAfterChange.getStrokeColor()).isEqualTo(newModelStrokeColor);

        // 4. (Opzionale) Verifica che il colore di riempimento non sia cambiato
        assertThat(shapeAfterChange.getFillColor()).isEqualTo(selectedShape.getFillColor());
    }

    @Test
    @DisplayName("Cambiare il colore di riempimento tramite ColorPicker dovrebbe aggiornare la forma selezionata")
    void testChangeFillColorViaPicker(FxRobot robot) {
        GeoEngine engine = getActiveGeoEngine();
        Shape selectedShape = drawAndSelectRectangle(robot, engine);

        ColorData initialFillColor = selectedShape.getFillColor();
        sad.gruppo11.Model.geometry.ColorData newModelFillColor = new sad.gruppo11.Model.geometry.ColorData(ColorData.GREEN); // Verde
        javafx.scene.paint.Color newFxFillColor = new javafx.scene.paint.Color(newModelFillColor.getR()/255, newModelFillColor.getG()/255, newModelFillColor.getB()/255, newModelFillColor.getA()); // Colore JavaFX corrispondente

        assertThat(initialFillColor).isNotEqualTo(newModelFillColor);

        // 1. Trova il ColorPicker per il fill
        // Assicurati che fx:id="fillColorPicker" esista
        ColorPicker fillPicker = robot.lookup("#fillColorPicker").query();

        // 2. Imposta il nuovo colore e scatena l'evento action
        robot.interact(() -> {
            fillPicker.setValue(newFxFillColor);
            fillPicker.fireEvent(new ActionEvent(fillPicker, fillPicker));
        });
        waitForFxEvents();

        // 3. Verifica che il colore di riempimento della forma nel modello sia cambiato
        Shape shapeAfterChange = engine.getDrawing().getShapes().get(0);
        assertThat(shapeAfterChange.getId()).isEqualTo(selectedShape.getId());
        assertThat(shapeAfterChange.getFillColor()).isEqualTo(newModelFillColor);

        // 4. (Opzionale) Verifica che il colore del bordo non sia cambiato
        assertThat(shapeAfterChange.getStrokeColor()).isEqualTo(selectedShape.getStrokeColor());
    }

    @Test
    @DisplayName("Cambiare colore su LineSegment tramite FillColorPicker non dovrebbe avere effetto sul fill")
    void testChangeFillColorOnLineSegment(FxRobot robot) {
        GeoEngine engine = getActiveGeoEngine();
        // engine.createNewDrawing();
        // waitForFxEvents();

        // Disegna una linea
        robot.clickOn("#lineToolButton");
        waitForFxEvents();
        Node canvasNode = robot.lookup("#actualDrawingCanvas").query();
        javafx.geometry.Point2D lineP1 = new javafx.geometry.Point2D(10,10);
        javafx.geometry.Point2D lineP2 = new javafx.geometry.Point2D(20,20);
        robot.moveTo(canvasNode, lineP1).press(MouseButton.PRIMARY).moveTo(canvasNode, lineP2).release(MouseButton.PRIMARY);
        waitForFxEvents();
        
        assertThat(engine.getDrawing().getShapes()).hasSize(1);
        Shape drawnLine = engine.getDrawing().getShapes().get(0);
        assertThat(drawnLine).isInstanceOf(LineSegment.class);

        // Seleziona la linea
        robot.clickOn("#selectToolButton");
        waitForFxEvents();
        // Clicca sul punto medio della linea (o un punto noto per essere sulla linea)
        javafx.geometry.Point2D selectLinePoint = new javafx.geometry.Point2D(15,15);
        robot.moveTo(canvasNode, selectLinePoint).clickOn(MouseButton.PRIMARY);
        waitForFxEvents();
        assertThat(engine.getSelectedShape()).isSameAs(drawnLine);

        // Prova a cambiare il colore di riempimento
        ColorPicker fillPicker = robot.lookup("#fillColorPicker").query();
        javafx.scene.paint.Color newFxFillColor = Color.ORANGE;

        robot.interact(() -> {
            fillPicker.setValue(newFxFillColor);
            fillPicker.fireEvent(new ActionEvent(fillPicker, fillPicker));
        });
        waitForFxEvents();

        // Verifica che il colore di riempimento della linea sia ancora null (o non cambiato)
        Shape lineAfterAttempt = engine.getDrawing().getShapes().get(0);
        assertThat(lineAfterAttempt.getFillColor()).isNull();
    }

    /* TESTS PER UNDO/REDO */
    /*
    @Test
    @DisplayName("Undo dopo aver disegnato una forma dovrebbe rimuoverla dal modello")
    void testUndoDrawShape(FxRobot robot) {
        GeoEngine engine = getActiveGeoEngine();
        // engine.createNewDrawing(); // Assicura stato pulito se necessario
        // waitForFxEvents();
        Drawing drawing = engine.getDrawing();
        int initialShapeCount = drawing.getShapes().size();

        // --- Fase 1: Disegna una forma ---
        robot.clickOn("#rectangleToolButton");
        waitForFxEvents();
        Node canvasNode = robot.lookup("#actualDrawingCanvas").query();
        javafx.geometry.Point2D pressPoint = new javafx.geometry.Point2D(10, 10);
        javafx.geometry.Point2D releasePoint = new javafx.geometry.Point2D(60, 60); // Rettangolo 50x50

        robot.moveTo(canvasNode, pressPoint)
            .press(MouseButton.PRIMARY)
            .moveTo(canvasNode, releasePoint)
            .release(MouseButton.PRIMARY);
        waitForFxEvents();

        assertThat(drawing.getShapes().size()).isEqualTo(initialShapeCount + 1);
        Shape addedShape = drawing.getShapes().get(initialShapeCount); // Prendi la forma aggiunta

        // --- Fase 2: Clicca Undo ---
        // Assicurati che fx:id="undoButton" esista
        robot.clickOn("#undoButton");
        waitForFxEvents();

        // --- Fase 3: Verifica che la forma sia stata rimossa ---
        assertThat(drawing.getShapes().size()).isEqualTo(initialShapeCount);
        assertThat(drawing.getShapes()).doesNotContain(addedShape);
    }

    @Test
    @DisplayName("Redo dopo un Undo di disegno dovrebbe ripristinare la forma")
    void testRedoAfterUndoDrawShape(FxRobot robot) {
        GeoEngine engine = getActiveGeoEngine();
        Drawing drawing = engine.getDrawing();
        int initialShapeCount = drawing.getShapes().size();

        // --- Fase 1: Disegna una forma ---
        robot.clickOn("#rectangleToolButton");
        waitForFxEvents();
        Node canvasNode = robot.lookup("#actualDrawingCanvas").query();
        javafx.geometry.Point2D pressPoint = new javafx.geometry.Point2D(20, 20);
        javafx.geometry.Point2D releasePoint = new javafx.geometry.Point2D(70, 70);

        robot.moveTo(canvasNode, pressPoint)
            .press(MouseButton.PRIMARY)
            .moveTo(canvasNode, releasePoint)
            .release(MouseButton.PRIMARY);
        waitForFxEvents();

        Shape addedShape = drawing.getShapes().get(initialShapeCount);
        UUID shapeId = addedShape.getId(); // Salva l'ID per confronto dopo il redo

        // --- Fase 2: Undo ---
        robot.clickOn("#undoButton");
        waitForFxEvents();
        assertThat(drawing.getShapes().size()).isEqualTo(initialShapeCount);

        // --- Fase 3: Clicca Redo ---
        // Assicurati che fx:id="redoButton" esista
        robot.clickOn("#redoButton");
        waitForFxEvents();

        // --- Fase 4: Verifica che la forma sia stata ripristinata ---
        assertThat(drawing.getShapes().size()).isEqualTo(initialShapeCount + 1);
        Shape restoredShape = drawing.getShapes().get(initialShapeCount); // Prendi la forma ripristinata
        assertThat(restoredShape.getId()).isEqualTo(shapeId); // Verifica che sia la stessa forma (basato su ID)
        // Potresti anche verificare i bounds o altre proprietà se necessario
    }

    @Test
    @DisplayName("Undo dopo aver spostato una forma dovrebbe ripristinare la posizione originale")
    void testUndoMoveShape(FxRobot robot) {
        GeoEngine engine = getActiveGeoEngine();
        Drawing drawing = engine.getDrawing();

        // --- Fase 1: Disegna e seleziona una forma ---
        Shape selectedShape = drawAndSelectRectangle(robot, engine);
        Rect initialBounds = selectedShape.getBounds(); // Salva bounds originali

        // --- Fase 2: Sposta la forma ---
        Node canvasNode = robot.lookup("#actualDrawingCanvas").query();
        javafx.geometry.Point2D dragStartPoint = new javafx.geometry.Point2D(
            initialBounds.getX() + initialBounds.getWidth() / 2,
            initialBounds.getY() + initialBounds.getHeight() / 2
        );
        double dx = 25, dy = 35;
        javafx.geometry.Point2D dragEndPoint = new javafx.geometry.Point2D(
            dragStartPoint.getX() + dx,
            dragStartPoint.getY() + dy
        );

        robot.moveTo(canvasNode, dragStartPoint)
            .press(MouseButton.PRIMARY)
            .moveTo(canvasNode, dragEndPoint)
            .release(MouseButton.PRIMARY);
        waitForFxEvents();

        // Verifica che si sia spostata
        Rect movedBounds = selectedShape.getBounds(); // La forma è la stessa istanza, i suoi bounds sono cambiati
        assertThat(movedBounds.getX()).isEqualTo(initialBounds.getX() + dx, within(0.1));
        assertThat(movedBounds.getY()).isEqualTo(initialBounds.getY() + dy, within(0.1));

        // --- Fase 3: Clicca Undo ---
        robot.clickOn("#undoButton");
        waitForFxEvents();

        // --- Fase 4: Verifica che la forma sia tornata ai bounds originali ---
        Rect boundsAfterUndo = selectedShape.getBounds(); // La forma è sempre la stessa istanza
        assertThat(boundsAfterUndo.getX()).isEqualTo(initialBounds.getX(), within(0.1));
        assertThat(boundsAfterUndo.getY()).isEqualTo(initialBounds.getY(), within(0.1));
        assertThat(boundsAfterUndo.getWidth()).isEqualTo(initialBounds.getWidth(), within(0.1));
        assertThat(boundsAfterUndo.getHeight()).isEqualTo(initialBounds.getHeight(), within(0.1));
    }
    
    @Test
    @DisplayName("Una nuova azione dopo Undo dovrebbe pulire lo stack Redo")
    void testNewActionAfterUndoClearsRedoStack(FxRobot robot) {
        GeoEngine engine = getActiveGeoEngine();
        Drawing drawing = engine.getDrawing();

        // 1. Disegna forma 1
        robot.clickOn("#rectangleToolButton");
        waitForFxEvents();
        Node canvasNode = robot.lookup("#actualDrawingCanvas").query();
        robot.moveTo(canvasNode, new javafx.geometry.Point2D(10,10)).press(MouseButton.PRIMARY).moveTo(canvasNode, new javafx.geometry.Point2D(20,20)).release(MouseButton.PRIMARY);
        waitForFxEvents();
        assertThat(drawing.getShapes()).hasSize(1);

        // 2. Disegna forma 2
        robot.moveTo(canvasNode, new javafx.geometry.Point2D(30,30)).press(MouseButton.PRIMARY).moveTo(canvasNode, new javafx.geometry.Point2D(40,40)).release(MouseButton.PRIMARY);
        waitForFxEvents();
        assertThat(drawing.getShapes()).hasSize(2);

        // 3. Undo (rimuove forma 2)
        robot.clickOn("#undoButton");
        waitForFxEvents();
        assertThat(drawing.getShapes()).hasSize(1);
        // Ora, il CommandManager dovrebbe avere la rimozione della forma 2 nello stack di redo.
        // Possiamo verificarlo tentando un redo (ma non lo faremo per questo test).
        // Invece, verifichiamo che canRedo() sia true se CommandManager lo esponesse,
        // ma lo verifichiamo indirettamente.

        // 4. Disegna forma 3 (nuova azione)
        robot.moveTo(canvasNode, new javafx.geometry.Point2D(50,50)).press(MouseButton.PRIMARY).moveTo(canvasNode, new javafx.geometry.Point2D(60,60)).release(MouseButton.PRIMARY);
        waitForFxEvents();
        assertThat(drawing.getShapes()).hasSize(2); // Forma 1 e Forma 3

        // 5. Prova a fare Redo. Non dovrebbe succedere nulla perché lo stack di redo è stato pulito.
        // La forma 2 (che era stata annullata) non dovrebbe riapparire.
        robot.clickOn("#redoButton");
        waitForFxEvents();
        assertThat(drawing.getShapes()).hasSize(2); // Ancora solo Forma 1 e Forma 3
    }
    */

    @Test
    @DisplayName("Copiare e incollare una forma dovrebbe aggiungere un clone con offset al modello")
    void testCopyAndPasteShape(FxRobot robot) {
        GeoEngine engine = getActiveGeoEngine();
        Drawing drawing = engine.getDrawing();
        // engine.createNewDrawing(); waitForFxEvents(); // Stato pulito

        // --- Fase 1: Disegna e seleziona una forma ---
        Shape originalShape = drawAndSelectRectangle(robot, engine);
        int initialShapeCount = drawing.getShapes().size(); // Dovrebbe essere 1
        Rect originalBounds = originalShape.getBounds();
        UUID originalId = originalShape.getId();

        // --- Fase 2: Clicca Copy ---
        // Assicurati che fx:id="copyButton" esista
        robot.clickOn("#copyButton");
        waitForFxEvents();

        // Verifica indiretta del clipboard: il numero di forme non deve cambiare
        assertThat(drawing.getShapes().size()).isEqualTo(initialShapeCount);
        // La forma originale deve essere ancora lì e selezionata
        assertThat(engine.getSelectedShape()).isSameAs(originalShape);

        // --- Fase 3: Clicca Paste ---
        // Assicurati che fx:id="pasteButton" esista
        robot.clickOn("#pasteButton");
        waitForFxEvents();

        // --- Fase 4: Verifica ---
        assertThat(drawing.getShapes().size()).isEqualTo(initialShapeCount + 1); // Una nuova forma aggiunta
        
        // Trova la forma incollata (probabilmente l'ultima aggiunta)
        Shape pastedShape = null;
        for(Shape s : drawing.getShapes()){
            if(s.getId() != originalId){ // Cerca la forma con un ID diverso
                pastedShape = s;
                break;
            }
        }
        assertNotNull(pastedShape, "La forma incollata non è stata trovata o aveva lo stesso ID dell'originale.");
        assertThat(pastedShape).isInstanceOf(originalShape.getClass()); // Stesso tipo
        assertThat(pastedShape.getId()).isNotEqualTo(originalId); // ID Nuovo

        // Verifica l'offset (PasteShapeCommand applica un offset di (10,10))
        Rect pastedBounds = pastedShape.getBounds();
        Vector2D expectedOffset = new Vector2D(10, 10); // Come definito in PasteShapeCommand
        assertThat(pastedBounds.getX()).isEqualTo(originalBounds.getX() + expectedOffset.getDx(), within(0.1));
        assertThat(pastedBounds.getY()).isEqualTo(originalBounds.getY() + expectedOffset.getDy(), within(0.1));
        assertThat(pastedBounds.getWidth()).isEqualTo(originalBounds.getWidth(), within(0.1));
        assertThat(pastedBounds.getHeight()).isEqualTo(originalBounds.getHeight(), within(0.1));

        // Verifica che la forma originale sia ancora intatta
        Shape originalShapeAfterPaste = drawing.getShapeById(originalId);
        assertNotNull(originalShapeAfterPaste);
        assertThat(originalShapeAfterPaste.getBounds()).isEqualTo(originalBounds);
    }

    @Test
    @DisplayName("Tagliare e incollare una forma dovrebbe rimuovere l'originale e aggiungere un clone con offset")
    void testCutAndPasteShape(FxRobot robot) {
        GeoEngine engine = getActiveGeoEngine();
        Drawing drawing = engine.getDrawing();
        // engine.createNewDrawing(); waitForFxEvents();

        // --- Fase 1: Disegna e seleziona una forma ---
        Shape originalShape = drawAndSelectRectangle(robot, engine);
        int shapeCountBeforeCut = drawing.getShapes().size();
        Rect boundsOfShapeToCut = originalShape.getBounds(); // Salva i bounds per il confronto dopo l'incolla
        UUID idOfShapeToCut = originalShape.getId();

        // --- Fase 2: Clicca Cut ---
        // Assicurati che fx:id="cutButton" esista
        robot.clickOn("#cutButton");
        waitForFxEvents();

        // --- Fase 3: Verifica che l'originale sia stato rimosso e la selezione annullata ---
        assertThat(drawing.getShapes().size()).isEqualTo(shapeCountBeforeCut - 1);
        assertThat(drawing.getShapeById(idOfShapeToCut)).isNull(); // Non più nel disegno
        assertThat(engine.getSelectedShape()).isNull(); // Tagliare dovrebbe deselezionare

        // --- Fase 4: Clicca Paste ---
        robot.clickOn("#pasteButton");
        waitForFxEvents();

        // --- Fase 5: Verifica ---
        assertThat(drawing.getShapes().size()).isEqualTo(shapeCountBeforeCut); // Una forma rimossa, una aggiunta
        
        Shape pastedShape = drawing.getShapes().get(drawing.getShapes().size() -1); // L'ultima aggiunta
        assertThat(pastedShape).isInstanceOf(originalShape.getClass());
        assertThat(pastedShape.getId()).isNotEqualTo(idOfShapeToCut); // Nuovo ID

        // Verifica l'offset rispetto ai bounds della forma che era stata tagliata
        Rect pastedBounds = pastedShape.getBounds();
        Vector2D expectedOffset = new Vector2D(10, 10);
        assertThat(pastedBounds.getX()).isEqualTo(boundsOfShapeToCut.getX() + expectedOffset.getDx(), within(0.1));
        assertThat(pastedBounds.getY()).isEqualTo(boundsOfShapeToCut.getY() + expectedOffset.getDy(), within(0.1));
    }

    @Test
    @DisplayName("Incollare più volte dovrebbe creare copie multiple indipendenti")
    void testMultiplePasteOperations(FxRobot robot) {
        GeoEngine engine = getActiveGeoEngine();
        Drawing drawing = engine.getDrawing();
        // engine.createNewDrawing(); waitForFxEvents();

        Shape originalShape = drawAndSelectRectangle(robot, engine); // Disegna, seleziona
        int initialShapeCount = drawing.getShapes().size();

        robot.clickOn("#copyButton"); // Copia
        waitForFxEvents();

        robot.clickOn("#pasteButton"); // Incolla 1
        waitForFxEvents();
        assertThat(drawing.getShapes().size()).isEqualTo(initialShapeCount + 1);
        Shape pastedShape1 = drawing.getShapes().get(initialShapeCount); // Indice della prima forma incollata

        robot.clickOn("#pasteButton"); // Incolla 2
        waitForFxEvents();
        assertThat(drawing.getShapes().size()).isEqualTo(initialShapeCount + 2);
        Shape pastedShape2 = drawing.getShapes().get(initialShapeCount + 1); // Indice della seconda forma incollata

        assertThat(pastedShape1.getId()).isNotEqualTo(originalShape.getId());
        assertThat(pastedShape2.getId()).isNotEqualTo(originalShape.getId());
        assertThat(pastedShape1.getId()).isNotEqualTo(pastedShape2.getId()); // ID diversi tra le copie

        // Verifica che gli offset siano applicati cumulativamente o in modo diverso (dipende da PasteShapeCommand)
        // Il tuo PasteShapeCommand attuale applica lo stesso offset (10,10) relativo alla forma *nel clipboard*.
        // Quindi, se il clipboard.get() restituisce sempre un clone dell'originale, le due forme incollate
        // avranno lo stesso offset rispetto all'originale, e quindi potrebbero sovrapporsi.
        // Questo è un dettaglio di implementazione del tuo PasteShapeCommand.
        // Qui verifichiamo solo che siano state create due nuove forme.
    }

    @Test
    @DisplayName("Incollare con clipboard vuoto non dovrebbe aggiungere forme")
    void testPasteWithEmptyClipboard(FxRobot robot) {
        GeoEngine engine = getActiveGeoEngine();
        Drawing drawing = engine.getDrawing();
        // engine.createNewDrawing(); waitForFxEvents();
        
        // Assicurati che il clipboard sia vuoto (potrebbe essere necessario un modo per pulirlo via GeoEngine o MainApp)
        // Per ora, assumiamo che sia vuoto all'inizio del test o dopo un'azione che non lo popola.
        // Se Clipboard è un singleton, il suo stato persiste.
        // Potremmo fare un "taglia" e poi "undo" per lasciare il clipboard con qualcosa,
        // poi chiamare engine.getClipboard().clear() se esistesse un metodo del genere.
        // Per questo test, potremmo semplicemente NON copiare/tagliare nulla prima.
        // Se `drawAndSelectRectangle` è stato chiamato, il clipboard non è vuoto se il test precedente ha copiato.
        // Quindi, è meglio iniziare con un disegno pulito e assicurarsi che nessuna azione di copia/taglia sia fatta.
        
        engine.getClipboard().clear(); // Assumendo che Clipboard abbia un metodo clear() accessibile
        waitForFxEvents();


        int initialShapeCount = drawing.getShapes().size();

        robot.clickOn("#pasteButton");
        waitForFxEvents();

        assertThat(drawing.getShapes().size()).isEqualTo(initialShapeCount); // Nessuna forma aggiunta
    }

}