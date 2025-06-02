
package sad.gruppo11.Controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import sad.gruppo11.Factory.ShapeFactory;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.PolygonShape;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.View.DrawingView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

class PolygonStateTest {

    private PolygonState polygonState;
    private GeoEngine mockGeoEngine;
    private DrawingView mockDrawingView;
    private ShapeFactory mockShapeFactory;
    private Point2D p1, p2, p3, p4, p1_close;
    private ColorData strokeColor, fillColor;
    private PolygonShape createdPolygon;

    @BeforeEach
    void setUp() {
        polygonState = new PolygonState();
        mockGeoEngine = mock(GeoEngine.class);
        mockDrawingView = mock(DrawingView.class);
        mockShapeFactory = mock(ShapeFactory.class);

        p1 = new Point2D(10, 10);
        p2 = new Point2D(100, 10);
        p3 = new Point2D(100, 100);
        p4 = new Point2D(10, 100); // A 4th point
        p1_close = new Point2D(10 + PolygonState.CLOSE_POLYGON_THRESHOLD_WORLD / 2, 10); // Close to p1

        strokeColor = ColorData.YELLOW;
        fillColor = ColorData.BLACK;
        // createdPolygon will be set if factory successfully creates it
        List<Point2D> defaultVertices = new ArrayList<>(Arrays.asList(p1, p2, p3));
        createdPolygon = new PolygonShape(defaultVertices, strokeColor, fillColor);


        when(mockGeoEngine.getView()).thenReturn(mockDrawingView);
        when(mockGeoEngine.getShapeFactory()).thenReturn(mockShapeFactory);
        when(mockGeoEngine.getCurrentStrokeColorForNewShapes()).thenReturn(strokeColor);
        when(mockGeoEngine.getCurrentFillColorForNewShapes()).thenReturn(fillColor);
        when(mockGeoEngine.getCurrentZoom()).thenReturn(1.0); // For CLOSE_POLYGON_THRESHOLD_WORLD scaling
    }

    @Test
    void getName_shouldReturnCorrectName() {
        assertEquals("PolygonTool", polygonState.getName());
    }

    @Test
    void deactivate_shouldClearVisualsMessageAndState() {
        polygonState.activate(mockGeoEngine);
        polygonState.onMousePressed(mockGeoEngine, p1); // Add a point

        polygonState.deactivate(mockGeoEngine);

        verify(mockDrawingView, times(1)).clearTemporaryVisuals();
        verify(mockDrawingView, times(1)).clearUserMessage();
        // Verify internal state reset by trying an action that depends on it
        polygonState.tryFinishPolygonOnAction(mockGeoEngine); // Should not finish if state reset
        verify(mockShapeFactory, never()).createShape(anyString(), any(), any(), any(), any(), anyMap());
    }

    @Test
    void onMousePressed_firstClick_startsDrawingAndAddsPoint() {
        polygonState.activate(mockGeoEngine);
        polygonState.onMousePressed(mockGeoEngine, p1);

        assertTrue(polygonState.isDrawing());
        assertEquals(1, polygonState.getCurrentPoints().size());
        assertEquals(p1, polygonState.getCurrentPoints().get(0));
        verify(mockDrawingView).drawTemporaryPolygonGuide(eq(polygonState.getCurrentPoints()), eq(p1));
        verify(mockGeoEngine).notifyViewToRefresh();
    }

    @Test
    void onMousePressed_subsequentClicks_addPoints() {
        polygonState.activate(mockGeoEngine);
        polygonState.onMousePressed(mockGeoEngine, p1);
        polygonState.onMousePressed(mockGeoEngine, p2);
        polygonState.onMousePressed(mockGeoEngine, p3);

        assertEquals(3, polygonState.getCurrentPoints().size());
        assertTrue(polygonState.getCurrentPoints().containsAll(Arrays.asList(p1, p2, p3)));
        verify(mockDrawingView, times(3)).drawTemporaryPolygonGuide(anyList(), any(Point2D.class));
        verify(mockGeoEngine, times(3)).notifyViewToRefresh();
    }

    @Test
    void onMousePressed_clickNearStartPointWithEnoughPoints_finishesPolygon() {
        // Simulate factory creating the polygon
        ArgumentCaptor<Map<String, Object>> factoryParamsCaptor = ArgumentCaptor.forClass(Map.class);
        when(mockShapeFactory.createShape(eq("PolygonTool"), eq(null), eq(null), eq(strokeColor), eq(fillColor), factoryParamsCaptor.capture()))
            .thenReturn(createdPolygon);
        
        polygonState.activate(mockGeoEngine);
        polygonState.onMousePressed(mockGeoEngine, p1);
        polygonState.onMousePressed(mockGeoEngine, p2);
        polygonState.onMousePressed(mockGeoEngine, p3); // 3 points
        polygonState.onMousePressed(mockGeoEngine, p1_close); // Click near start

        verify(mockDrawingView).clearTemporaryVisuals();
        verify(mockShapeFactory).createShape(anyString(), any(), any(), any(), any(), anyMap());
        Map<String, Object> capturedParams = factoryParamsCaptor.getValue();
        List<Point2D> verticesSentToFactory = (List<Point2D>) capturedParams.get("vertices");
        assertEquals(3, verticesSentToFactory.size()); // Should be p1, p2, p3
        assertTrue(verticesSentToFactory.containsAll(Arrays.asList(p1,p2,p3)));

        verify(mockGeoEngine).addShapeToDrawing(createdPolygon);
        verify(mockDrawingView).showUserMessage(contains("Polygon created!"));
        assertFalse(polygonState.isDrawing());
        assertTrue(polygonState.getCurrentPoints().isEmpty());
    }
    
    @Test
    void onMousePressed_clickNearStartPointWithHighZoom_finishesPolygon() {
        when(mockGeoEngine.getCurrentZoom()).thenReturn(0.1); // Low zoom, threshold becomes larger (100 world units)
        Point2D p1_far_but_close_on_screen = new Point2D(p1.getX() + 5, p1.getY()); // 5 units away
        // Screen distance for threshold: CLOSE_POLYGON_THRESHOLD_WORLD / zoom = 10.0 / 0.1 = 100.0
        // Distance is 5, which is < 100.
        
        ArgumentCaptor<Map<String, Object>> factoryParamsCaptor = ArgumentCaptor.forClass(Map.class);
        when(mockShapeFactory.createShape(eq("PolygonTool"), eq(null), eq(null), eq(strokeColor), eq(fillColor), factoryParamsCaptor.capture()))
            .thenReturn(createdPolygon);

        polygonState.activate(mockGeoEngine);
        polygonState.onMousePressed(mockGeoEngine, p1);
        polygonState.onMousePressed(mockGeoEngine, p2);
        polygonState.onMousePressed(mockGeoEngine, p3);
        polygonState.onMousePressed(mockGeoEngine, p1_far_but_close_on_screen);

        verify(mockGeoEngine).addShapeToDrawing(createdPolygon);
    }


    @Test
    void onMousePressed_clickNearStartPointNotEnoughPoints_showsMessage() {
        polygonState.activate(mockGeoEngine);
        polygonState.onMousePressed(mockGeoEngine, p1);
        polygonState.onMousePressed(mockGeoEngine, p2); // Only 2 points
        polygonState.onMousePressed(mockGeoEngine, p1_close); // Click near start

        verify(mockDrawingView, never()).clearTemporaryVisuals(); // Not finished
        verify(mockShapeFactory, never()).createShape(anyString(), any(), any(), any(), any(), anyMap());
        verify(mockGeoEngine, never()).addShapeToDrawing(any(Shape.class));
        verify(mockDrawingView).showUserMessage(contains("A polygon needs at least 3 points. Current: 2"));
        assertTrue(polygonState.isDrawing()); // Still drawing
        assertEquals(2, polygonState.getCurrentPoints().size()); // p1_close was not added
    }

    @Test
    void onMouseDragged_whileDrawing_updatesTemporaryGuide() {
        polygonState.activate(mockGeoEngine);
        polygonState.onMousePressed(mockGeoEngine, p1); // Start drawing
        polygonState.onMousePressed(mockGeoEngine, p2); // Add second point

        clearInvocations(mockDrawingView, mockGeoEngine); // Clear up to this point

        polygonState.onMouseDragged(mockGeoEngine, p3); // Drag to p3

        // getCurrentPoints() should be [p1, p2]. p3 is the rubberBandEnd.
        List<Point2D> expectedGuidePoints = new ArrayList<>(Arrays.asList(p1, p2));
        verify(mockDrawingView).drawTemporaryPolygonGuide(eq(expectedGuidePoints), eq(p3));
        verify(mockGeoEngine).notifyViewToRefresh();
    }

    @Test
    void onMouseDragged_notDrawing_doesNothing() {
        polygonState.activate(mockGeoEngine); // isDrawing() is false
        polygonState.onMouseDragged(mockGeoEngine, p1);
        verify(mockDrawingView, never()).drawTemporaryPolygonGuide(anyList(), any(Point2D.class));
        verify(mockGeoEngine, never()).notifyViewToRefresh();
    }

    @Test
    void tryFinishPolygonOnAction_withEnoughPoints_finishesPolygon() {
        when(mockShapeFactory.createShape(anyString(), any(), any(), any(), any(), anyMap()))
            .thenReturn(createdPolygon);

        polygonState.activate(mockGeoEngine);
        polygonState.onMousePressed(mockGeoEngine, p1);
        polygonState.onMousePressed(mockGeoEngine, p2);
        polygonState.onMousePressed(mockGeoEngine, p3); // 3 points, isDrawing() = true

        polygonState.tryFinishPolygonOnAction(mockGeoEngine);

        verify(mockDrawingView).clearTemporaryVisuals();
        verify(mockGeoEngine).addShapeToDrawing(createdPolygon);
        assertFalse(polygonState.isDrawing());
    }

    @Test
    void tryFinishPolygonOnAction_notEnoughPoints_showsMessage() {
        polygonState.activate(mockGeoEngine);
        polygonState.onMousePressed(mockGeoEngine, p1);
        polygonState.onMousePressed(mockGeoEngine, p2); // 2 points, isDrawing() = true

        polygonState.tryFinishPolygonOnAction(mockGeoEngine);

        verify(mockDrawingView).showUserMessage(contains("Not enough points for a polygon (2/3 min)"));
        verify(mockGeoEngine, never()).addShapeToDrawing(any(Shape.class));
        assertTrue(polygonState.isDrawing()); // Still drawing
    }
    
    @Test
    void tryFinishPolygonOnAction_notDrawing_doesNothing() {
        polygonState.activate(mockGeoEngine); // isDrawing() = false
        clearInvocations(mockDrawingView, mockGeoEngine); // Ignore activation interactions
        polygonState.tryFinishPolygonOnAction(mockGeoEngine);

        verify(mockDrawingView, never()).showUserMessage(anyString());
        verify(mockGeoEngine, never()).addShapeToDrawing(any(Shape.class));
    }

    @Test
    void onMouseReleased_doesNothing() {
        // PolygonState does not use onMouseReleased for its primary logic
        polygonState.activate(mockGeoEngine);
        polygonState.onMousePressed(mockGeoEngine, p1);
        clearInvocations(mockDrawingView, mockGeoEngine, mockShapeFactory);

        polygonState.onMouseReleased(mockGeoEngine, p2);
        verifyNoMoreInteractions(mockDrawingView, mockGeoEngine, mockShapeFactory);
    }
}
            