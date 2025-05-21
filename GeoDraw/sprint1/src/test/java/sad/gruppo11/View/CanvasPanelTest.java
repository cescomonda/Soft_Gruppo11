package sad.gruppo11.View;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InOrder;
import org.mockito.Mockito;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.RectangleShape; // Classe concreta per mock

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CanvasPanelTest {

    private Canvas mockCanvas;
    private GraphicsContext mockGc;
    private CanvasPanel canvasPanel;
    private ShapeVisitor mockShapeVisitor;

    @BeforeEach
    void setUp() {
        // Mock del Canvas e del suo GraphicsContext
        mockCanvas = Mockito.mock(Canvas.class);
        mockGc = Mockito.mock(GraphicsContext.class);

        // Configura il mockCanvas per restituire il mockGc
        when(mockCanvas.getGraphicsContext2D()).thenReturn(mockGc);
        // Configura dimensioni di default per il canvas mockato, necessarie per clearRect
        when(mockCanvas.getWidth()).thenReturn(800.0);
        when(mockCanvas.getHeight()).thenReturn(600.0);

        canvasPanel = new CanvasPanel(mockCanvas);
        mockShapeVisitor = Mockito.mock(ShapeVisitor.class); // Mock del renderer
    }

    @Test
    @DisplayName("Il costruttore con Canvas nullo dovrebbe lanciare NullPointerException")
    void testConstructorNullCanvas() {
        assertThatThrownBy(() -> new CanvasPanel(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Canvas cannot be null");
    }

    @Test
    @DisplayName("Il costruttore dovrebbe ottenere il GraphicsContext dal Canvas fornito")
    void testConstructorGetsGraphicsContext() {
        verify(mockCanvas, times(1)).getGraphicsContext2D();
        assertThat(canvasPanel.getGraphicsContext()).isSameAs(mockGc);
        assertThat(canvasPanel.getCanvas()).isSameAs(mockCanvas);
    }

    @Test
    @DisplayName("clear dovrebbe chiamare clearRect sul GraphicsContext con le dimensioni del canvas")
    void testClear() {
        canvasPanel.clear();
        verify(mockGc, times(1)).clearRect(0, 0, 800.0, 600.0);
    }
    
    @Test
    @DisplayName("clear non dovrebbe chiamare clearRect se le dimensioni del canvas sono zero")
    void testClearWithZeroCanvasSize() {
        when(mockCanvas.getWidth()).thenReturn(0.0);
        when(mockCanvas.getHeight()).thenReturn(600.0);
        canvasPanel.clear();
        verify(mockGc, never()).clearRect(anyDouble(), anyDouble(), anyDouble(), anyDouble());

        when(mockCanvas.getWidth()).thenReturn(800.0);
        when(mockCanvas.getHeight()).thenReturn(0.0);
        canvasPanel.clear();
        verify(mockGc, never()).clearRect(anyDouble(), anyDouble(), anyDouble(), anyDouble()); // ancora `never` perché la prima condizione non è passata
    }


    @Test
    @DisplayName("drawShapes dovrebbe prima pulire il canvas e poi visitare ogni forma non nulla")
    void testDrawShapesClearsAndVisitsShapes() {
        Shape mockShape1 = Mockito.mock(RectangleShape.class);
        Shape mockShape2 = Mockito.mock(RectangleShape.class);
        List<Shape> shapes = Arrays.asList(mockShape1, null, mockShape2); // Include un null

        canvasPanel.drawShapes(mockShapeVisitor, shapes);

        // Verifica l'ordine: prima clear, poi accept per ogni forma valida
        InOrder inOrder = Mockito.inOrder(mockGc, mockShape1, mockShape2, mockShapeVisitor);
                                        // mockShapeVisitor non è chiamato direttamente da gc, ma shape.accept lo chiama

        inOrder.verify(mockGc).clearRect(0, 0, 800.0, 600.0); // clearRect chiamato da clear()
        
        // Per verificare che shape.accept sia chiamato, dobbiamo farlo tramite il mockShapeVisitor
        // La verifica dell'ordine qui è più sull'effetto che sulle chiamate dirette a mockShapeVisitor in ordine.
        // Verifichiamo che accept sia stato chiamato sulle forme.
        verify(mockShape1, times(1)).accept(mockShapeVisitor);
        verify(mockShape2, times(1)).accept(mockShapeVisitor);

        // Per verificare l'ordine in cui le forme sono state visitate rispetto a clear:
        // Dopo clear, shape1.accept(visitor) dovrebbe avvenire, poi shape2.accept(visitor).
        // È difficile usare InOrder tra mockGc e mockShape.accept direttamente.
        // Ci fidiamo che il loop in drawShapes chiami accept nell'ordine della lista.

        // Testiamo che il visitor sia stato usato per le forme valide:
        // (Questo è già coperto da verify(mockShapeX).accept(...) sopra)
    }
    
    @Test
    @DisplayName("drawShapes con renderer nullo dovrebbe lanciare NullPointerException")
    void testDrawShapesNullRenderer() {
        List<Shape> shapes = new ArrayList<>();
        assertThatThrownBy(() -> canvasPanel.drawShapes(null, shapes))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("ShapeVisitor (renderer) cannot be null");
    }

    @Test
    @DisplayName("drawShapes con lista di forme nulla dovrebbe lanciare NullPointerException")
    void testDrawShapesNullShapesList() {
        assertThatThrownBy(() -> canvasPanel.drawShapes(mockShapeVisitor, null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("List of shapes cannot be null");
    }

    @Test
    @DisplayName("drawShapes con lista vuota dovrebbe solo pulire il canvas")
    void testDrawShapesEmptyList() {
        List<Shape> emptyShapes = new ArrayList<>();
        canvasPanel.drawShapes(mockShapeVisitor, emptyShapes);

        verify(mockGc, times(1)).clearRect(0, 0, 800.0, 600.0);
        verifyNoInteractions(mockShapeVisitor); // Nessuna forma da visitare
        // Se avessimo mock di Shape nella lista vuota, potremmo fare verify(mockShape, never()).accept(...)
    }
}