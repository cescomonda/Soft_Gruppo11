
package sad.gruppo11.Controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import sad.gruppo11.Factory.ShapeFactory;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.EllipseShape;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Rect;
import sad.gruppo11.View.DrawingView;

class EllipseStateTest {

    private EllipseState ellipseState;
    private GeoEngine mockGeoEngine;
    private DrawingView mockDrawingView;
    private ShapeFactory mockShapeFactory;
    private Point2D p1, p2;
    private ColorData strokeColor, fillColor;
    private EllipseShape createdEllipse;
    private Rect expectedRect;

    @BeforeEach
    void setUp() {
        ellipseState = new EllipseState();
        mockGeoEngine = mock(GeoEngine.class);
        mockDrawingView = mock(DrawingView.class);
        mockShapeFactory = mock(ShapeFactory.class);

        p1 = new Point2D(5, 15);
        p2 = new Point2D(105, 115); // width 100, height 100

        strokeColor = ColorData.RED;
        fillColor = ColorData.BLUE;
        expectedRect = new Rect(Math.min(p1.getX(),p2.getX()), Math.min(p1.getY(),p2.getY()), Math.abs(p1.getX()-p2.getX()), Math.abs(p1.getY()-p2.getY()));
        createdEllipse = new EllipseShape(expectedRect, strokeColor, fillColor);

        when(mockGeoEngine.getView()).thenReturn(mockDrawingView);
        when(mockGeoEngine.getShapeFactory()).thenReturn(mockShapeFactory);
        when(mockGeoEngine.getCurrentStrokeColorForNewShapes()).thenReturn(strokeColor);
        when(mockGeoEngine.getCurrentFillColorForNewShapes()).thenReturn(fillColor);
        when(mockGeoEngine.getCurrentZoom()).thenReturn(1.0);
    }

    @Test
    void getName_shouldReturnCorrectName() {
        assertEquals("EllipseTool", ellipseState.getName());
    }

    @Test
    void activate_shouldClearSelectionAndShowMessage() {
        ellipseState.activate(mockGeoEngine);
        verify(mockGeoEngine).clearSelection();
        verify(mockDrawingView).showUserMessage(contains("Ellipse Tool"));
    }

    @Test
    void deactivate_shouldClearTemporaryVisualsAndMessage() {
        ellipseState.activate(mockGeoEngine);
        ellipseState.deactivate(mockGeoEngine);
        verify(mockDrawingView).clearTemporaryVisuals();
        verify(mockDrawingView).clearUserMessage();
    }

    @Test
    void onMouseDragged_withFirstCornerSet_shouldDrawGhostShapeAndRefresh() {
        ellipseState.activate(mockGeoEngine);
        ellipseState.onMousePressed(mockGeoEngine, p1);
        ellipseState.onMouseDragged(mockGeoEngine, p2);

        ArgumentCaptor<Shape> shapeCaptor = ArgumentCaptor.forClass(Shape.class);
        verify(mockDrawingView).drawTemporaryGhostShape(shapeCaptor.capture());
        Shape ghost = shapeCaptor.getValue();
        assertTrue(ghost instanceof EllipseShape);
        assertEquals(expectedRect, ghost.getBounds());
        assertEquals(strokeColor, ghost.getStrokeColor());
        assertEquals(fillColor, ghost.getFillColor());

        verify(mockGeoEngine).notifyViewToRefresh();
    }
    
    @Test
    void onMouseDragged_dragTooSmall_shouldClearGhostAndRefresh() {
        ellipseState.activate(mockGeoEngine);
        ellipseState.onMousePressed(mockGeoEngine, p1); // p1 = (5,15)
        Point2D p_close = new Point2D(5.05, 15.05); // 0.05 width/height, threshold is 0.1
        
        ellipseState.onMouseDragged(mockGeoEngine, p_close);

        verify(mockDrawingView).clearTemporaryVisuals();
        verify(mockGeoEngine).notifyViewToRefresh();
        verify(mockDrawingView, never()).drawTemporaryGhostShape(any(Shape.class));
    }

    @Test
    void onMouseReleased_withValidDrag_shouldCreateAndAddEllipse() {
        when(mockShapeFactory.createShape("EllipseTool", p1, p2, strokeColor, fillColor, null))
            .thenReturn(createdEllipse);

        ellipseState.activate(mockGeoEngine);
        ellipseState.onMousePressed(mockGeoEngine, p1);
        ellipseState.onMouseReleased(mockGeoEngine, p2);

        InOrder inOrder = inOrder(mockDrawingView, mockGeoEngine, mockShapeFactory);
        inOrder.verify(mockDrawingView).clearTemporaryVisuals();
        inOrder.verify(mockGeoEngine).getShapeFactory();
        inOrder.verify(mockShapeFactory).createShape("EllipseTool", p1, p2, strokeColor, fillColor, null);
        inOrder.verify(mockGeoEngine).addShapeToDrawing(createdEllipse);
        inOrder.verify(mockDrawingView).showUserMessage(contains("Ellipse created"));
    }

    @Test
    void onMouseReleased_widthOrHeightTooSmall_shouldNotCreateEllipse() {
        Point2D p_small_width = new Point2D(p1.getX() + 0.05, p2.getY()); // width 0.05
        ellipseState.activate(mockGeoEngine);
        ellipseState.onMousePressed(mockGeoEngine, p1);
        ellipseState.onMouseReleased(mockGeoEngine, p_small_width);

        verify(mockGeoEngine, never()).addShapeToDrawing(any(Shape.class));
        
        reset(mockGeoEngine, mockDrawingView, mockShapeFactory); // Reset for next part of test
        when(mockGeoEngine.getView()).thenReturn(mockDrawingView);
        when(mockGeoEngine.getShapeFactory()).thenReturn(mockShapeFactory);
        when(mockGeoEngine.getCurrentStrokeColorForNewShapes()).thenReturn(strokeColor);
        when(mockGeoEngine.getCurrentFillColorForNewShapes()).thenReturn(fillColor);
        when(mockGeoEngine.getCurrentZoom()).thenReturn(1.0);

        Point2D p_small_height = new Point2D(p2.getX(), p1.getY() + 0.05); // height 0.05
        ellipseState.onMousePressed(mockGeoEngine, p1);
        ellipseState.onMouseReleased(mockGeoEngine, p_small_height);
        
        verify(mockGeoEngine, never()).addShapeToDrawing(any(Shape.class));
    }
}
            