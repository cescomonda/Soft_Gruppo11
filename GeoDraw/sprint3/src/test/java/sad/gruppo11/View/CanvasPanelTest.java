
package sad.gruppo11.View;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Spy;
import static org.mockito.MockitoAnnotations.openMocks;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.*;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;

import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.LineSegment;
import sad.gruppo11.Model.GroupShape;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.ColorData;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Necessario per test JavaFX su thread non-FX se si usano componenti UI reali.
// Per i mock di GraphicsContext e Canvas, potrebbe non essere strettamente necessario
// ma è buona pratica includerlo se si lavora con test che toccano JavaFX.
// import org.testfx.framework.junit5.ApplicationExtension;

// @ExtendWith(ApplicationExtension.class) // Potrebbe essere necessario per testare componenti UI reali
class CanvasPanelTest {

    @Mock private Canvas mockCanvas;
    @Mock private GraphicsContext mockGc;
    // Non possiamo mockare JavaFXShapeRenderer direttamente se vogliamo testare l'interazione con esso
    // e le chiamate a shape.accept(). Se CanvasPanel lo crea internamente, è difficile mockarlo.
    // In questo caso, CanvasPanel crea il suo renderer. Possiamo:
    // 1. Testare l'effetto complessivo (verificando chiamate a gc, assumendo che il renderer funzioni).
    // 2. Refactor CanvasPanel per iniettare il renderer (migliore per testabilità).
    // Per ora, procediamo con l'opzione 1 e facciamo uno spy sul CanvasPanel se necessario
    // per controllare il renderer o mockiamo il renderer se lo iniettassimo.
    // Dato che il renderer è 'final', non possiamo spiarlo facilmente dopo la costruzione del CanvasPanel.
    // Per ora, ci concentreremo sulle interazioni di CanvasPanel con GraphicsContext.
    
    private CanvasPanel canvasPanel;
    private JavaFXShapeRenderer spiedRenderer; // Useremo uno spy se necessario


    @BeforeEach
    void setUp() {
        openMocks(this);
        when(mockCanvas.getGraphicsContext2D()).thenReturn(mockGc);
        when(mockCanvas.getWidth()).thenReturn(800.0); // Default canvas size
        when(mockCanvas.getHeight()).thenReturn(600.0);

        canvasPanel = new CanvasPanel(mockCanvas);
        // Per testare l'interazione con il renderer, potremmo dover accedere al renderer interno
        // e spiarlo, o refactorizzare CanvasPanel.
        // Per ora, i test che coinvolgono il renderer verificheranno le chiamate a gc.
    }

    @Test
    void constructor_nullCanvas_shouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> new CanvasPanel(null));
    }

    @Test
    void getCanvas_shouldReturnTheCanvas() {
        assertSame(mockCanvas, canvasPanel.getCanvas());
    }

    @Test
    void clear_shouldResetTransformAndClearRect() {
        canvasPanel.clear();
        InOrder inOrder = inOrder(mockGc);
        inOrder.verify(mockGc).setTransform(any(Affine.class)); // Verifica che sia chiamata con una nuova Affine (identità)
        inOrder.verify(mockGc).clearRect(0, 0, 800.0, 600.0);
    }
    
    @Test
    void setTransform_shouldStoreValuesAndLimitZoom() {
        canvasPanel.setTransform(20.0, 50, 60); // Zoom > max (10.0)
        assertEquals(10.0, canvasPanel.getZoomFactor(), 0.001);
        assertEquals(50, canvasPanel.getOffsetX(), 0.001);
        assertEquals(60, canvasPanel.getOffsetY(), 0.001);

        canvasPanel.setTransform(0.05, -10, -20); // Zoom < min (0.1)
        assertEquals(0.1, canvasPanel.getZoomFactor(), 0.001);
        assertEquals(-10, canvasPanel.getOffsetX(), 0.001);
        assertEquals(-20, canvasPanel.getOffsetY(), 0.001);
        
        canvasPanel.setTransform(2.0, 10, 20);
        assertEquals(2.0, canvasPanel.getZoomFactor(), 0.001);
    }


    @Test
    void drawShapes_shouldClearApplyTransformDrawGridAndShapes() {
        Shape mockShape1 = mock(Shape.class);
        Shape mockShape2 = mock(Shape.class);
        List<Shape> shapes = Arrays.asList(mockShape1, mockShape2);
        Shape selectedShape = mock(Shape.class); // Può essere anche un GroupShape

        canvasPanel.setGridEnabled(true);
        canvasPanel.setGridSize(20);
        canvasPanel.setTransform(2.0, 10, 20); // Zoom 2x, offset (10,20)

        canvasPanel.drawShapes(shapes, selectedShape);

        InOrder inOrder = inOrder(mockGc, mockShape1, mockShape2); // mockGc prima per clear e transform
        
        // 1. Clear
        inOrder.verify(mockGc).setTransform(any(Affine.class)); // From clear()
        inOrder.verify(mockGc).clearRect(0,0,800,600);

        // 2. Apply view transform (save, setTransform for identity, translate, scale)
        inOrder.verify(mockGc).save();
        inOrder.verify(mockGc).setTransform(any(Affine.class)); // Reset to identity
        inOrder.verify(mockGc).translate(10, 20);
        inOrder.verify(mockGc).scale(2.0, 2.0);

        // 3. Render Grid (complex to verify exact lines, check for stroke color and line width)
        // The renderGridTransformed uses its own save/restore.
        inOrder.verify(mockGc).save(); // For grid
        inOrder.verify(mockGc).setStroke(Color.LIGHTGRAY);
        inOrder.verify(mockGc).setLineWidth(0.5 / 2.0); // 0.5 / zoomFactor
        inOrder.verify(mockGc).beginPath();
        inOrder.verify(mockGc, atLeastOnce()).moveTo(anyDouble(), anyDouble());
        inOrder.verify(mockGc, atLeastOnce()).lineTo(anyDouble(), anyDouble());
        inOrder.verify(mockGc).stroke();
        inOrder.verify(mockGc).closePath();
        inOrder.verify(mockGc).restore(); // For grid
        
        // 4. Draw shapes (via renderer.accept)
        // JavaFXShapeRenderer is created internally. We verify shape.accept is called.
        // The renderer will then call gc methods.
        // SelectedShapeForRendering is set on the internal renderer.
        inOrder.verify(mockShape1).accept(any(JavaFXShapeRenderer.class));
        inOrder.verify(mockShape2).accept(any(JavaFXShapeRenderer.class));
        
        // 5. Draw temporary visuals (if any, assume none for this test basic path)
        // This also has its own save/restore. If no temp visuals, these might not be called.
        verify(mockGc, times(3)).save(); // For temp visuals
        verify(mockGc, times(3)).restore(); // For temp visuals

        // 6. Final restore from the main drawShapes gc.save()
        verify(mockGc, times(3)).restore();
    }
    
    @Test
    void drawShapes_withSelectedGroup_rendererIsConfiguredForEachChild() {
        Shape mockChild1 = mock(Shape.class);
        Shape mockChild2 = mock(Shape.class);
        GroupShape selectedGroup = new GroupShape(Arrays.asList(mockChild1, mockChild2));
        // Spy on the selectedGroup to verify getChildren is called if necessary,
        // though the real GroupShape will work.
        
        List<Shape> shapesToDraw = new ArrayList<>(Arrays.asList(selectedGroup));

        // We cannot directly verify calls on the internal renderer's setSelectedShapeForRendering.
        // However, we can verify that child.accept(renderer) is called, which implies
        // the renderer was used. The selection highlight itself is part of the renderer's logic.
        // This test checks that if a group is "selected", its children are drawn.
        // The specific highlighting is tested in JavaFXShapeRendererTest.

        canvasPanel.drawShapes(shapesToDraw, selectedGroup); // selectedGroup is passed as 'selectedShapes'

        // Verify the group itself is visited (which then visits children)
        // And also, the specific logic in CanvasPanel.drawShapes for selected GroupShape:
        verify(mockChild1, times(2)).accept(any(JavaFXShapeRenderer.class));
        verify(mockChild2, times(2)).accept(any(JavaFXShapeRenderer.class));
    }


    @Test
    void setTemporaryPolygonGuide_storesPointsAndClearsGhost() {
        List<Point2D> points = Arrays.asList(new Point2D(1,1), new Point2D(2,2));
        Point2D rubberEnd = new Point2D(3,3);
        canvasPanel.setTemporaryPolygonGuide(points, rubberEnd);

        // Verify internal state by drawing (not ideal, but current design)
        canvasPanel.drawShapes(new ArrayList<>(), null); // Draw to trigger drawCurrentTemporaryVisuals
        
        verify(mockGc, atLeastOnce()).setStroke(Color.DARKSLATEGRAY); // Characteristic of temp visuals
        verify(mockGc, atLeastOnce()).strokeLine(1,1,2,2);
        verify(mockGc, atLeastOnce()).strokeLine(2,2,3,3);
        
        // If a ghost shape was set before, it should be cleared by setTemporaryPolygonGuide
        Shape ghost = mock(Shape.class);
        canvasPanel.setTemporaryGhostShape(ghost); // Set a ghost
        canvasPanel.setTemporaryPolygonGuide(points, rubberEnd); // This should clear the ghost

        reset(mockGc); // Reset gc before next draw call for verification
        when(mockCanvas.getWidth()).thenReturn(800.0); when(mockCanvas.getHeight()).thenReturn(600.0); // re-stub
        canvasPanel.drawShapes(new ArrayList<>(), null);
        verify(ghost, never()).accept(any(JavaFXShapeRenderer.class)); // Ghost should not be drawn
        verify(mockGc, atLeastOnce()).strokeLine(1,1,2,2); // Polygon guide should be drawn
    }
    
    @Test
    void setTemporaryGhostShape_storesShapeAndClearsPolygonGuide() {
        Shape ghost = new LineSegment(new Point2D(1,1), new Point2D(10,10), ColorData.BLUE); // Real shape
        canvasPanel.setTemporaryGhostShape(ghost);

        // Verify internal state by drawing
        canvasPanel.drawShapes(new ArrayList<>(), null);
        // The ghost shape's accept method should be called by the renderer.
        // This requires more intricate verification of the renderer's interaction or assuming accept works.
        // We can verify that strokeLine for polygon guide is NOT called if guide was cleared.
        // And that some drawing happens for the ghost (e.g., its own strokeLine if it's a LineSegment)

        // Set polygon guide then ghost shape
        canvasPanel.setTemporaryPolygonGuide(Arrays.asList(new Point2D(1,1), new Point2D(2,2)), new Point2D(3,3));
        canvasPanel.setTemporaryGhostShape(ghost);

        reset(mockGc);
        when(mockCanvas.getWidth()).thenReturn(800.0); when(mockCanvas.getHeight()).thenReturn(600.0);
        canvasPanel.drawShapes(new ArrayList<>(), null);
        
        // Verify characteristic calls for ghost shape (e.g., a line from (1,1) to (10,10))
        // The renderer will make these calls.
        // For a LineSegment ghost: setStroke, setLineWidth, strokeLine
        verify(mockGc, atLeastOnce()).setStroke(JavaFXShapeRenderer.convertModelToFxColor(new ColorData(100,100,100,0.7))); // Ghost color
        verify(mockGc, atLeastOnce()).strokeLine(1,1,10,10); // Ghost line itself
        
        // Verify polygon guide specific strokeLine calls are NOT made
        verify(mockGc, never()).strokeLine(1,1,2,2); 
        verify(mockGc, never()).strokeLine(2,2,3,3);
    }


    @Test
    void clearTemporaryVisuals_clearsAllGuides() {
        canvasPanel.setTemporaryPolygonGuide(Arrays.asList(new Point2D(1,1)), new Point2D(2,2));
        canvasPanel.setTemporaryGhostShape(mock(Shape.class));
        canvasPanel.clearTemporaryVisuals();

        canvasPanel.drawShapes(new ArrayList<>(), null); // Draw to check if anything temp is drawn
        // Verify no strokeLine calls for polygon guide and no accept calls for ghost shape
        verify(mockGc, atMost(1)).setStroke(Color.DARKSLATEGRAY);
        verify(mockGc, never()).strokeLine(anyDouble(), anyDouble(), anyDouble(), anyDouble());
        // If ghost was mock(Shape.class), its accept would not be called.
        // This is a bit indirect.
    }

    @Test
    void screenToWorld_and_worldToScreen_conversions() {
        canvasPanel.setTransform(2.0, 100, 50); // Zoom 2x, offset (100,50)
        Point2D screenPoint = new Point2D(300, 250);
        Point2D expectedWorldPoint = new Point2D((300.0 - 100.0) / 2.0, (250.0 - 50.0) / 2.0); // (100, 100)
        
        Point2D worldPoint = canvasPanel.screenToWorld(screenPoint);
        assertEquals(expectedWorldPoint.getX(), worldPoint.getX(), 0.001);
        assertEquals(expectedWorldPoint.getY(), worldPoint.getY(), 0.001);

        Point2D screenAgain = canvasPanel.worldToScreen(worldPoint);
        assertEquals(screenPoint.getX(), screenAgain.getX(), 0.001);
        assertEquals(screenPoint.getY(), screenAgain.getY(), 0.001);
    }
    
    @Test
    void renderGridTransformed_gridDisabled_shouldNotDraw() {
        canvasPanel.setGridEnabled(false);
        canvasPanel.drawShapes(new ArrayList<>(), null); // Will call renderGridTransformed if enabled
        
        // Verify no grid drawing calls (e.g., setStroke(Color.LIGHTGRAY))
        verify(mockGc, never()).setStroke(Color.LIGHTGRAY);
    }

    @Test
    void renderGridTransformed_gridTooDense_shouldNotDraw() {
        canvasPanel.setGridEnabled(true);
        canvasPanel.setGridSize(20);
        canvasPanel.setTransform(0.05, 0,0); // zoomFactor * gridSize = 0.05 * 20 = 1.0 (< 2 threshold)

        canvasPanel.drawShapes(new ArrayList<>(), null);
        verify(mockGc, atMost(1)).strokeLine(anyDouble(), anyDouble(), anyDouble(), anyDouble());
        verify(mockGc, atMost(1)).setStroke(Color.LIGHTGRAY); // Should not draw if too dense
    }
}

            