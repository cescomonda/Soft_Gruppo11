package sad.gruppo11.View;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import sad.gruppo11.Controller.GeoEngine;
import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.RectangleShape;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DrawingViewTest {

    @Mock private CanvasPanel mockCanvasPanel;
    @Mock private Drawing mockInitialDrawing; // Usato nel costruttore di DrawingView
    @Mock private GeoEngine mockController;
    @Mock private Canvas mockActualCanvas;
    @Mock private GraphicsContext mockGc;

    private DrawingView drawingView;

    @BeforeEach
    void setUp() {
        // Stubbing NECESSARI per il COSTRUTTORE di DrawingView
        when(mockCanvasPanel.getCanvas()).thenReturn(mockActualCanvas); // Per setupMouseHandlers()
        when(mockCanvasPanel.getGraphicsContext()).thenReturn(mockGc);   // Per la creazione interna di JavaFXShapeRenderer

        // Istanzia DrawingView. mockInitialDrawing può essere null qui se il test
        // non si basa su di esso durante l'istanziazione e lo imposta dopo.
        // Per coerenza, lo passiamo, e i test che non lo usano lo ignoreranno.
        drawingView = new DrawingView(mockCanvasPanel, mockInitialDrawing);
        drawingView.setController(mockController); // Molti test assumono che un controller sia impostato.

        // NOTA: Niente Mockito.reset() qui. L'estensione JUnit 5 di Mockito
        // resetta i mock DOPO ogni test, il che è generalmente ciò che vogliamo.
        // Gli stubbing specifici verranno fatti all'inizio di ogni metodo di test.
    }

    @Test
    @DisplayName("Il costruttore con CanvasPanel nullo dovrebbe lanciare NullPointerException")
    void testConstructorNullCanvasPanel() {
        // Sovrascriviamo il mockCanvasPanel per questo test specifico se necessario,
        // o ci assicuriamo che il when() nel setup non interferisca.
        // Qui, l'oggetto sotto test è la chiamata al costruttore.
        assertThatThrownBy(() -> new DrawingView(null, mockInitialDrawing))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("CanvasPanel cannot be null");
    }

    @Test
    @DisplayName("Il costruttore dovrebbe registrarsi come observer al Drawing iniziale se non nullo")
    void testConstructorRegistersObserver() {
        // mockInitialDrawing è passato al costruttore in setUp.
        // Verifica la chiamata a addObserver fatta dal costruttore di DrawingView.
        // Poiché mockInitialDrawing è già stato usato nel setUp, la verifica è su quell'istanza.
        verify(mockInitialDrawing).addObserver(drawingView); // drawingView è l'observer atteso
    }

    @Test
    @DisplayName("Il costruttore con Drawing iniziale nullo non dovrebbe tentare di registrarsi")
    void testConstructorNullInitialDrawing() {
        // Istanziamo una nuova DrawingView con null per il disegno
        DrawingView dvWithNullDrawing = new DrawingView(mockCanvasPanel, null);
        // Non c'è un mockDrawing da verificare per addObserver, il test è che non ci sia NPE.
        assertNotNull(dvWithNullDrawing);
        // Potremmo verificare che mockInitialDrawing (dal setup) non sia stato chiamato una seconda volta.
        verify(mockInitialDrawing, times(1)).addObserver(any(DrawingView.class)); // Solo dal setup
    }

    @Test
    @DisplayName("setController dovrebbe impostare il controller")
    void testSetController() {
        GeoEngine newMockController = mock(GeoEngine.class);
        // Configura il nuovo controller per il test di render
        when(newMockController.getSelectedShape()).thenReturn(null);
        // Se drawingView ha un modello (mockInitialDrawing), configura le sue shapes
        when(mockInitialDrawing.getShapes()).thenReturn(new ArrayList<>());


        drawingView.setController(newMockController);
        drawingView.render(); // Chiama render per vedere se usa il nuovo controller

        verify(newMockController, atLeastOnce()).getSelectedShape();
    }

    @Test
    @DisplayName("setDrawingModel dovrebbe cambiare modello, gestire observer e chiamare render/clear")
    void testSetDrawingModel() {
        Drawing newMockDrawing = mock(Drawing.class);
        List<Shape> emptyList = new ArrayList<>();

        // --- Scenario 1: Imposta un nuovo modello valido ---
        // Stubbing necessari per quando setDrawingModel chiama render()
        lenient().when(newMockDrawing.getShapes()).thenReturn(emptyList);
        lenient().when(mockController.getSelectedShape()).thenReturn(null); // render() lo chiama
        // mockCanvasPanel.getGraphicsContext() è già stubbato in setUp e il renderer interno lo usa.

        drawingView.setDrawingModel(newMockDrawing);

        verify(mockInitialDrawing).removeObserver(drawingView); // mockInitialDrawing era il modello nel setUp
        verify(newMockDrawing).addObserver(drawingView);
        verify(mockCanvasPanel).drawShapes(any(JavaFXShapeRenderer.class), eq(emptyList)); // Da render()

        // --- Resetta i mock per il prossimo scenario ---
        // Resettiamo TUTTI i mock che sono stati usati o configurati nello Scenario 1
        // e che potrebbero interferire con lo Scenario 2.
        Mockito.reset(mockCanvasPanel, mockInitialDrawing, newMockDrawing, mockController);

        // --- Scenario 2: Imposta il modello a null ---
        // Dopo il reset, dobbiamo ri-stubbare le chiamate minime necessarie per evitare NPE

        drawingView.setDrawingModel(null); // Il modello precedente era newMockDrawing

        verify(newMockDrawing).removeObserver(drawingView); // Verifica che si deregistri da newMockDrawing
        verify(mockCanvasPanel).clear();

        // Verifica che non ci siano state interazioni impreviste con mockController
        // o mockInitialDrawing (che ora è irrilevante) dopo il reset.
        verifyNoInteractions(mockController);
        verifyNoInteractions(mockInitialDrawing); // Già resettato, ma per chiarezza
    }

    @Test
    @DisplayName("update dovrebbe chiamare render se l'oggetto osservato è un Drawing")
    void testUpdateCallsRender() {
        Drawing sourceDrawing = mock(Drawing.class);
        List<Shape> emptyList = new ArrayList<>();
        lenient().when(sourceDrawing.getShapes()).thenReturn(emptyList);
        // Necessario per la chiamata a render() dentro update()
        lenient().when(mockController.getSelectedShape()).thenReturn(null);


        drawingView.setDrawingModel(sourceDrawing); // Assicura che currentDrawingModel sia sourceDrawing
        Mockito.reset(mockCanvasPanel); // Resetta per isolare la chiamata a render da update
        // Ri-stubba se necessario per il renderer
        // when(mockCanvasPanel.getGraphicsContext()).thenReturn(mockGc);

        drawingView.update(sourceDrawing);

        verify(mockCanvasPanel).drawShapes(any(JavaFXShapeRenderer.class), eq(emptyList));
    }

    @Test
    @DisplayName("update non dovrebbe fare nulla se l'oggetto non è un Drawing")
    void testUpdateWithNonDrawingObject() {
        Object notADrawing = new Object();
        drawingView.update(notADrawing);
        verify(mockCanvasPanel, never()).drawShapes(any(), any());
        verify(mockCanvasPanel, never()).clear();
    }

    @Test
    @DisplayName("render dovrebbe impostare selectedShape nel renderer e chiamare drawShapes o clear")
    void testRenderLogic() {
        Shape mockSelected = mock(RectangleShape.class);
        List<Shape> shapes = Arrays.asList(mock(Shape.class));
 
        // Caso 1: Controller e DrawingModel validi
        lenient().when(mockController.getSelectedShape()).thenReturn(mockSelected);
        drawingView.setDrawingModel(mockInitialDrawing); // Usa il modello dal setup
        lenient().when(mockInitialDrawing.getShapes()).thenReturn(shapes);
        // mockCanvasPanel.getGraphicsContext() è già stubbato in setUp

        // Reset mockCanvasPanel to clear the invocation from setDrawingModel's render
        Mockito.reset(mockCanvasPanel);

        drawingView.render();
        verify(mockCanvasPanel).drawShapes(any(JavaFXShapeRenderer.class), eq(shapes));
        verify(mockController, atLeastOnce()).getSelectedShape(); // Verifica che sia stato consultato

        Mockito.reset(mockCanvasPanel, mockController, mockInitialDrawing); // Resetta per il prossimo caso
        // Ri-stubba le basi
        when(mockCanvasPanel.getGraphicsContext()).thenReturn(mockGc);
        when(mockInitialDrawing.getShapes()).thenReturn(shapes); // Ri-stubba per il prossimo render

        // Caso 2: Controller nullo
        drawingView.setController(null); // Controller è null
        drawingView.setDrawingModel(mockInitialDrawing); // Modello ancora valido
        Mockito.reset(mockCanvasPanel); 
        drawingView.render();
        verify(mockCanvasPanel).drawShapes(any(JavaFXShapeRenderer.class), eq(shapes));
        // mockController è null, quindi getSelectedShape non dovrebbe essere chiamato su di esso.
        // (verify(mockController) fallirebbe o darebbe 0 interazioni se resettato prima)

        Mockito.reset(mockCanvasPanel); // Resetta solo canvasPanel
        // when(mockCanvasPanel.getGraphicsContext()).thenReturn(mockGc);

        // Caso 3: DrawingModel nullo
        drawingView.setDrawingModel(null); // Questo chiama clear()
        // Mockito.reset(mockCanvasPanel); // Già fatto sopra
        // when(mockCanvasPanel.getGraphicsContext()).thenReturn(mockGc); // Già fatto sopra
        
        drawingView.render(); // currentDrawingModel è null
        // setDrawingModel(null) chiama clear una volta. render() lo chiama di nuovo.
        verify(mockCanvasPanel, times(2)).clear(); // Una da setDrawingModel, una da render
    }
    
    @Test
    @DisplayName("setRendererLineWidth dovrebbe impostare la larghezza nel renderer e chiamare render")
    void testSetRendererLineWidth() {
        List<Shape> testShapes = Arrays.asList(mock(Shape.class));
        // Configura i mock necessari per la chiamata a render()
        lenient().when(mockInitialDrawing.getShapes()).thenReturn(testShapes);
        drawingView.setDrawingModel(mockInitialDrawing); // Imposta un modello con forme
        lenient().when(mockController.getSelectedShape()).thenReturn(null); // Per la chiamata a render
        // mockCanvasPanel.getGraphicsContext() già stubbato in setUp

        Mockito.reset(mockCanvasPanel); // Resetta per la verifica di render
        
        double newLineWeight = 3.0;
        drawingView.setRendererLineWidth(newLineWeight);

        verify(mockCanvasPanel).drawShapes(any(JavaFXShapeRenderer.class), eq(testShapes));
    }

    @Test
    @DisplayName("setupMouseHandlers dovrebbe registrare listener sul canvas")
    void testSetupMouseHandlersRegistersListeners() {
        // Il costruttore in setUp chiama setupMouseHandlers.
        // mockActualCanvas è restituito da mockCanvasPanel.getCanvas() (stubbed in setUp).
        verify(mockActualCanvas, times(1)).setOnMousePressed(any());
        verify(mockActualCanvas, times(1)).setOnMouseDragged(any());
        verify(mockActualCanvas, times(1)).setOnMouseReleased(any());
    }
}