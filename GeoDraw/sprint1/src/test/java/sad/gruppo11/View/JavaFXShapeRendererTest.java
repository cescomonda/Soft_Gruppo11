package sad.gruppo11.View;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;
import sad.gruppo11.Model.EllipseShape;
import sad.gruppo11.Model.LineSegment;
import sad.gruppo11.Model.RectangleShape;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Rect;


import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class JavaFXShapeRendererTest {

    private GraphicsContext mockGc;
    private JavaFXShapeRenderer renderer;

    private ColorData testStrokeColorData;
    private ColorData testFillColorData;
    private ColorData testTransparentFillColorData;
    private Color fxTestStrokeColor;
    private Color fxTestFillColor;
    private Color fxTransparentColor;

    private static final double defaultLineWidth = 1.5;


    @BeforeEach
    void setUp() {
        mockGc = Mockito.mock(GraphicsContext.class);
        renderer = new JavaFXShapeRenderer(mockGc);

        testStrokeColorData = new ColorData(255, 0, 0, 1.0); // Rosso opaco
        testFillColorData = new ColorData(0, 255, 0, 0.5); // Verde semi-trasparente
        testTransparentFillColorData = new ColorData(0,0,0,0.0); // Completamente trasparente

        fxTestStrokeColor = Color.rgb(255,0,0,1.0);
        fxTestFillColor = Color.rgb(0,255,0,0.5);
        fxTransparentColor = Color.TRANSPARENT; // o Color.rgb(0,0,0,0.0)
    }

    @Test
    @DisplayName("Il costruttore con GraphicsContext nullo dovrebbe lanciare NullPointerException")
    void testConstructorNullGc() {
        assertThatThrownBy(() -> new JavaFXShapeRenderer(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("GraphicsContext cannot be null");
    }

    @Test
    @DisplayName("setDefaultLineWidth dovrebbe impostare la larghezza di linea usata")
    void testSetDefaultLineWidth() {
        renderer.setDefaultLineWidth(5.0);
        
        Rect bounds = new Rect(10,10,10,10);
        RectangleShape rect = new RectangleShape(bounds);
        rect.setStrokeColor(testStrokeColorData);
        rect.setFillColor(testTransparentFillColorData); // No fill per semplificare

        renderer.visit(rect);
        verify(mockGc).setLineWidth(5.0); // Verifica che la nuova larghezza sia usata
    }
    
    @Test
    @DisplayName("setDefaultLineWidth con valore non positivo non dovrebbe cambiare la larghezza")
    void testSetDefaultLineWidthNonPositive() {
        renderer.setDefaultLineWidth(2.0); // Larghezza iniziale
        renderer.setDefaultLineWidth(0.0); // Tentativo con 0
        renderer.setDefaultLineWidth(-1.0); // Tentativo con negativo

        Rect bounds = new Rect(10,10,10,10);
        RectangleShape rect = new RectangleShape(bounds);
        rect.setStrokeColor(testStrokeColorData);
        rect.setFillColor(testTransparentFillColorData);

        renderer.visit(rect);
        verify(mockGc).setLineWidth(2.0); // Dovrebbe usare ancora 2.0
    }


    @Test
    @DisplayName("visit(RectangleShape) dovrebbe disegnare riempimento e bordo correttamente")
    void testVisitRectangleShape() {
        Rect bounds = new Rect(10, 20, 100, 50);
        RectangleShape rectShape = new RectangleShape(bounds);
        rectShape.setStrokeColor(testStrokeColorData);
        rectShape.setFillColor(testFillColorData);

        renderer.visit(rectShape);

        InOrder inOrder = Mockito.inOrder(mockGc); // Verifica l'ordine delle chiamate

        // 1. Riempimento
        inOrder.verify(mockGc).setFill(fxTestFillColor);
        inOrder.verify(mockGc).fillRect(10, 20, 100, 50);

        // 2. Bordo
        inOrder.verify(mockGc).setStroke(fxTestStrokeColor);
        inOrder.verify(mockGc).setLineWidth(defaultLineWidth); // Usa la larghezza di default
        inOrder.verify(mockGc).setLineCap(StrokeLineCap.SQUARE);
        inOrder.verify(mockGc).setLineJoin(StrokeLineJoin.MITER);
        inOrder.verify(mockGc).strokeRect(10, 20, 100, 50);

        verifyNoMoreInteractions(mockGc); // Assicura che non ci siano state altre chiamate (es. indicatore selezione)
    }

    @Test
    @DisplayName("visit(RectangleShape) con riempimento trasparente non dovrebbe chiamare fillRect")
    void testVisitRectangleShapeTransparentFill() {
        Rect bounds = new Rect(10, 20, 100, 50);
        RectangleShape rectShape = new RectangleShape(bounds);
        rectShape.setStrokeColor(testStrokeColorData);
        rectShape.setFillColor(testTransparentFillColorData); // Riempimento trasparente

        renderer.visit(rectShape);

        verify(mockGc, never()).setFill(any(Color.class)); // O specificamente fxTransparentColor se viene impostato
        verify(mockGc, never()).fillRect(anyDouble(), anyDouble(), anyDouble(), anyDouble());

        // Verifica che il bordo sia comunque disegnato
        verify(mockGc).setStroke(fxTestStrokeColor);
        verify(mockGc).strokeRect(10, 20, 100, 50);
    }
    
    @Test
    @DisplayName("visit(RectangleShape) selezionato dovrebbe disegnare l'indicatore di selezione dopo la forma")
    void testVisitSelectedRectangleShape() {
        Rect bounds = new Rect(10, 20, 100, 50);
        RectangleShape rectShape = new RectangleShape(bounds);
        rectShape.setStrokeColor(testStrokeColorData);
        rectShape.setFillColor(testFillColorData);

        renderer.setSelectedShapeForRendering(rectShape); // Imposta come selezionato
        renderer.visit(rectShape);

        InOrder inOrder = Mockito.inOrder(mockGc);
        // ... chiamate per fillRect e strokeRect ...
        inOrder.verify(mockGc).fillRect(anyDouble(), anyDouble(), anyDouble(), anyDouble());
        inOrder.verify(mockGc).strokeRect(anyDouble(), anyDouble(), anyDouble(), anyDouble());

        // Poi l'indicatore di selezione
        inOrder.verify(mockGc).setStroke(Color.CORNFLOWERBLUE);
        inOrder.verify(mockGc).setLineWidth(defaultLineWidth + 2.0);
        inOrder.verify(mockGc).setLineDashes(5, 5);
        inOrder.verify(mockGc).strokeRect(bounds.getX() - 2, bounds.getY() - 2, bounds.getWidth() + 4, bounds.getHeight() + 4);
        inOrder.verify(mockGc).setLineDashes(0);
    }


    @Test
    @DisplayName("visit(EllipseShape) dovrebbe disegnare riempimento e bordo correttamente")
    void testVisitEllipseShape() {
        Rect bounds = new Rect(5, 15, 80, 40);
        EllipseShape ellipse = new EllipseShape(bounds);
        ellipse.setStrokeColor(testStrokeColorData);
        ellipse.setFillColor(testFillColorData);

        renderer.visit(ellipse);

        InOrder inOrder = Mockito.inOrder(mockGc);
        inOrder.verify(mockGc).setFill(fxTestFillColor);
        inOrder.verify(mockGc).fillOval(5, 15, 80, 40);
        inOrder.verify(mockGc).setStroke(fxTestStrokeColor);
        inOrder.verify(mockGc).setLineWidth(defaultLineWidth);
        inOrder.verify(mockGc).strokeOval(5, 15, 80, 40);
        verifyNoMoreInteractions(mockGc);
    }
    
    @Test
    @DisplayName("visit(EllipseShape) selezionato dovrebbe disegnare l'indicatore di selezione")
    void testVisitSelectedEllipseShape() {
        Rect bounds = new Rect(5, 15, 80, 40);
        EllipseShape ellipse = new EllipseShape(bounds);
        ellipse.setStrokeColor(testStrokeColorData);
        ellipse.setFillColor(testFillColorData);

        renderer.setSelectedShapeForRendering(ellipse);
        renderer.visit(ellipse);

        // Verifica che drawSelectionIndicator sia chiamato dopo il disegno dell'ellisse
        // L'ordine preciso delle chiamate a fillOval/strokeOval e poi drawSelectionIndicator
        // può essere verificato con InOrder se necessario.
        // Qui verifichiamo solo che le chiamate per l'indicatore avvengano.
        verify(mockGc).strokeRect(bounds.getX() - 2, bounds.getY() - 2, bounds.getWidth() + 4, bounds.getHeight() + 4); // Parte di drawSelectionIndicator
    }


    @Test
    @DisplayName("visit(LineSegment) dovrebbe disegnare la linea correttamente")
    void testVisitLineSegment() {
        Point2D start = new Point2D(100, 120);
        Point2D end = new Point2D(200, 220);
        LineSegment line = new LineSegment(start, end);
        line.setStrokeColor(testStrokeColorData);

        renderer.visit(line);

        InOrder inOrder = Mockito.inOrder(mockGc);
        inOrder.verify(mockGc).setStroke(fxTestStrokeColor);
        inOrder.verify(mockGc).setLineWidth(defaultLineWidth);
        inOrder.verify(mockGc).setLineCap(StrokeLineCap.ROUND);
        inOrder.verify(mockGc).strokeLine(100, 120, 200, 220);
        verifyNoMoreInteractions(mockGc);
    }

    @Test
    @DisplayName("visit(LineSegment) selezionato dovrebbe disegnare l'indicatore di selezione per linea")
    void testVisitSelectedLineSegment() {
        Point2D start = new Point2D(100, 120);
        Point2D end = new Point2D(200, 220);
        LineSegment line = new LineSegment(start, end);
        line.setStrokeColor(testStrokeColorData);

        renderer.setSelectedShapeForRendering(line);
        renderer.visit(line);

        // Verifica che drawSelectionIndicatorForLine sia chiamato dopo il disegno della linea.
        // Calcola i bounds attesi per l'indicatore della linea
        double minX = Math.min(start.getX(), end.getX()) - 2;
        double minY = Math.min(start.getY(), end.getY()) - 2;
        double width = Math.abs(start.getX() - end.getX()) + 4;
        double height = Math.abs(start.getY() - end.getY()) + 4;

        verify(mockGc).strokeRect(minX, minY, width, height); // Parte di drawSelectionIndicatorForLine
    }

    @Test
    @DisplayName("visit con Shape nulla non dovrebbe fare nulla e non lanciare eccezioni")
    void testVisitNullShape() {
        assertDoesNotThrow(() -> renderer.visit((RectangleShape) null));
        assertDoesNotThrow(() -> renderer.visit((EllipseShape) null));
        assertDoesNotThrow(() -> renderer.visit((LineSegment) null));
        verifyNoInteractions(mockGc); // Nessuna interazione con GraphicsContext
    }
}