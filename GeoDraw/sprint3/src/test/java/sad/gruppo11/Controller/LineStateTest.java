
package sad.gruppo11.Controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.internal.matchers.Null;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

import sad.gruppo11.Factory.ShapeFactory;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.LineSegment;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.View.DrawingView;

class LineStateTest {

    private LineState lineState;
    private GeoEngine mockGeoEngine;
    private DrawingView mockDrawingView;
    private ShapeFactory mockShapeFactory;
    private Point2D p1, p2, p3;
    private ColorData strokeColor;
    private LineSegment createdLine;

    @BeforeEach
    void setUp() {
        lineState = new LineState();
        mockGeoEngine = mock(GeoEngine.class);
        mockDrawingView = mock(DrawingView.class);
        mockShapeFactory = mock(ShapeFactory.class);

        p1 = new Point2D(10, 10);
        p2 = new Point2D(100, 100);
        p3 = new Point2D(10.5, 10.5); // Point very close to p1 for small drag test

        strokeColor = ColorData.BLUE;
        createdLine = new LineSegment(p1, p2, strokeColor);

        when(mockGeoEngine.getView()).thenReturn(mockDrawingView);
        when(mockGeoEngine.getShapeFactory()).thenReturn(mockShapeFactory);
        when(mockGeoEngine.getCurrentStrokeColorForNewShapes()).thenReturn(strokeColor);
        when(mockGeoEngine.getCurrentZoom()).thenReturn(1.0); // Default zoom for threshold checks
    }

    @Test
    void getName_shouldReturnCorrectName() {
        assertEquals("LineTool", lineState.getName());
    }

    @Test
    void activate_shouldClearSelectionAndShowMessage() {
        lineState.activate(mockGeoEngine);
        verify(mockGeoEngine, times(1)).clearSelection();
        verify(mockDrawingView, times(1)).showUserMessage(contains("Line Tool"));
        // Also verify firstPoint is reset (internal state, harder to verify directly without getter)
    }

    @Test
    void deactivate_shouldClearTemporaryVisualsAndMessage() {
        lineState.activate(mockGeoEngine); // Activate first
        lineState.deactivate(mockGeoEngine);
        verify(mockDrawingView, times(1)).clearTemporaryVisuals();
        verify(mockDrawingView, times(1)).clearUserMessage();
        // Also verify firstPoint is reset
    }

    @Test
    void onMousePressed_shouldStoreFirstPoint() {
        lineState.activate(mockGeoEngine);
        lineState.onMousePressed(mockGeoEngine, p1);
        // Internal state (firstPoint) is set. We verify its effect in onMouseDragged/Released.
        // No direct public method to check firstPoint.
        // For now, we assume it's correctly stored.
    }

    @Test
    void onMouseDragged_withFirstPointSet_shouldDrawGhostShapeAndRefresh() {
        lineState.activate(mockGeoEngine);
        lineState.onMousePressed(mockGeoEngine, p1); // Set firstPoint

        lineState.onMouseDragged(mockGeoEngine, p2);

        ArgumentCaptor<Shape> shapeCaptor = ArgumentCaptor.forClass(Shape.class);
        verify(mockDrawingView, times(1)).drawTemporaryGhostShape(shapeCaptor.capture());
        assertTrue(shapeCaptor.getValue() instanceof LineSegment);
        LineSegment ghost = (LineSegment) shapeCaptor.getValue();
        assertEquals(p1, ghost.getStartPoint());
        assertEquals(p2, ghost.getEndPoint());
        assertEquals(strokeColor, ghost.getStrokeColor());

        verify(mockGeoEngine, times(1)).notifyViewToRefresh();
    }

    @Test
    void onMouseDragged_withoutFirstPoint_shouldDoNothing() {
        lineState.activate(mockGeoEngine);
        // firstPoint is null
        lineState.onMouseDragged(mockGeoEngine, p2);
        verify(mockDrawingView, never()).drawTemporaryGhostShape(any(Shape.class));
        verify(mockGeoEngine, never()).notifyViewToRefresh();
    }

    @Test
    void onMouseReleased_withValidDrag_shouldCreateAndAddLine() {
        when(mockShapeFactory.createShape("LineTool", p1, p2, strokeColor, ColorData.TRANSPARENT, null))
            .thenReturn(createdLine);

        lineState.activate(mockGeoEngine);
        lineState.onMousePressed(mockGeoEngine, p1);
        lineState.onMouseDragged(mockGeoEngine, p2); // Optional, but good for full flow
        lineState.onMouseReleased(mockGeoEngine, p2);

        InOrder inOrder = inOrder(mockDrawingView, mockGeoEngine, mockShapeFactory);
        inOrder.verify(mockDrawingView).clearTemporaryVisuals();
        inOrder.verify(mockGeoEngine).getShapeFactory();
        inOrder.verify(mockShapeFactory).createShape("LineTool", p1, p2, strokeColor, ColorData.TRANSPARENT, null);
        inOrder.verify(mockGeoEngine).addShapeToDrawing(createdLine);
        inOrder.verify(mockDrawingView).showUserMessage(contains("Line created"));
    }

    @Test
    void onMouseReleased_withFirstPointNull_shouldDoNothing() {
        lineState.activate(mockGeoEngine);
        // firstPoint is null
        lineState.onMouseReleased(mockGeoEngine, p2);

        verify(mockDrawingView, times(1)).clearTemporaryVisuals(); // This is always called
        verify(mockGeoEngine, never()).getShapeFactory();
        verify(mockShapeFactory, never()).createShape(anyString(), any(), any(), any(), any(), any());
        verify(mockGeoEngine, never()).addShapeToDrawing(any(Shape.class));
    }

    @Test
    void onMouseReleased_dragDistanceTooSmall_shouldNotCreateLine() {
        // p3 is very close to p1
        when(mockGeoEngine.getCurrentZoom()).thenReturn(1.0); // Standard zoom

        lineState.activate(mockGeoEngine);
        lineState.onMousePressed(mockGeoEngine, p1);
        lineState.onMouseReleased(mockGeoEngine, p3); // Release at p3

        verify(mockDrawingView, times(1)).clearTemporaryVisuals();
        verify(mockGeoEngine, times(1)).notifyViewToRefresh(); // Called due to small drag logic
        verify(mockShapeFactory, never()).createShape(anyString(), any(), any(), any(), any(), any());
        verify(mockGeoEngine, never()).addShapeToDrawing(any(Shape.class));
    }
    
    @Test
    void onMouseReleased_dragDistanceTooSmallWithHighZoom_shouldNotCreateLine() {
        // p3 is very close to p1
        when(mockGeoEngine.getCurrentZoom()).thenReturn(10.0); // High zoom, threshold becomes smaller (0.1 world units)
                                                              // p1(10,10), p3(10.5,10.5), distance is sqrt(0.5^2+0.5^2) = sqrt(0.5) approx 0.7
                                                              // Threshold is 1.0 / 10.0 = 0.1. Since 0.7 > 0.1, it *should* create.

        // Let's adjust p3 for this test to be within the 0.1 threshold
        Point2D p3_zoomed = new Point2D(10.05, 10.05); // distance from p1 is sqrt(0.05^2+0.05^2) = sqrt(0.005) approx 0.07
                                                       // 0.07 < 0.1, so should NOT create

        lineState.activate(mockGeoEngine);
        lineState.onMousePressed(mockGeoEngine, p1);
        lineState.onMouseReleased(mockGeoEngine, p3_zoomed);

        verify(mockDrawingView, times(1)).clearTemporaryVisuals();
        verify(mockGeoEngine, times(1)).notifyViewToRefresh();
        verify(mockShapeFactory, never()).createShape(anyString(), any(), any(), any(), any(), any());
        verify(mockGeoEngine, never()).addShapeToDrawing(any(Shape.class));
    }
}

            