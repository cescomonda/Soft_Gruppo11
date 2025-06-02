
package sad.gruppo11.Controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import sad.gruppo11.Factory.ShapeFactory;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.RectangleShape;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Rect;
import sad.gruppo11.View.DrawingView;

class RectangleStateTest {

    private RectangleState rectangleState;
    private GeoEngine mockGeoEngine;
    private DrawingView mockDrawingView;
    private ShapeFactory mockShapeFactory;
    private Point2D p1, p2, p3_small_x, p3_small_y;
    private ColorData strokeColor, fillColor;
    private RectangleShape createdRectangle;
    private Rect expectedRect;

    @BeforeEach
    void setUp() {
        rectangleState = new RectangleState();
        mockGeoEngine = mock(GeoEngine.class);
        mockDrawingView = mock(DrawingView.class);
        mockShapeFactory = mock(ShapeFactory.class);

        p1 = new Point2D(10, 20);
        p2 = new Point2D(110, 120); // width 100, height 100
        p3_small_x = new Point2D(10.5, 120); // width 0.5, too small
        p3_small_y = new Point2D(110, 20.5); // height 0.5, too small


        strokeColor = ColorData.RED;
        fillColor = ColorData.GREEN;
        expectedRect = new Rect(Math.min(p1.getX(),p2.getX()), Math.min(p1.getY(),p2.getY()), Math.abs(p1.getX()-p2.getX()), Math.abs(p1.getY()-p2.getY()));
        createdRectangle = new RectangleShape(expectedRect, strokeColor, fillColor);

        when(mockGeoEngine.getView()).thenReturn(mockDrawingView);
        when(mockGeoEngine.getShapeFactory()).thenReturn(mockShapeFactory);
        when(mockGeoEngine.getCurrentStrokeColorForNewShapes()).thenReturn(strokeColor);
        when(mockGeoEngine.getCurrentFillColorForNewShapes()).thenReturn(fillColor);
        when(mockGeoEngine.getCurrentZoom()).thenReturn(1.0);
    }

    @Test
    void getName_shouldReturnCorrectName() {
        assertEquals("RectangleTool", rectangleState.getName());
    }

    @Test
    void activate_shouldClearSelectionAndShowMessage() {
        rectangleState.activate(mockGeoEngine);
        verify(mockGeoEngine).clearSelection();
        verify(mockDrawingView).showUserMessage(contains("Rectangle Tool"));
    }

    @Test
    void deactivate_shouldClearTemporaryVisualsAndMessage() {
        rectangleState.activate(mockGeoEngine);
        rectangleState.deactivate(mockGeoEngine);
        verify(mockDrawingView).clearTemporaryVisuals();
        verify(mockDrawingView).clearUserMessage();
    }

    @Test
    void onMousePressed_shouldStoreFirstCorner() {
        rectangleState.activate(mockGeoEngine);
        rectangleState.onMousePressed(mockGeoEngine, p1);
        // Internal state, verified by drag/release
    }

    @Test
    void onMouseDragged_withFirstCornerSet_shouldDrawGhostShapeAndRefresh() {
        rectangleState.activate(mockGeoEngine);
        rectangleState.onMousePressed(mockGeoEngine, p1);
        rectangleState.onMouseDragged(mockGeoEngine, p2);

        ArgumentCaptor<Shape> shapeCaptor = ArgumentCaptor.forClass(Shape.class);
        verify(mockDrawingView).drawTemporaryGhostShape(shapeCaptor.capture());
        Shape ghost = shapeCaptor.getValue();
        assertTrue(ghost instanceof RectangleShape);
        assertEquals(expectedRect, ghost.getBounds());
        assertEquals(strokeColor, ghost.getStrokeColor());
        assertEquals(fillColor, ghost.getFillColor());

        verify(mockGeoEngine).notifyViewToRefresh();
    }
    
    @Test
    void onMouseDragged_dragTooSmall_shouldClearGhostAndRefresh() {
        rectangleState.activate(mockGeoEngine);
        rectangleState.onMousePressed(mockGeoEngine, p1); // p1 = (10,20)
        Point2D p_close = new Point2D(10.05, 20.05); // very small drag, 0.05 width/height
                                                    // threshold is 0.1 / zoom (0.1 here)
        rectangleState.onMouseDragged(mockGeoEngine, p_close);

        verify(mockDrawingView).clearTemporaryVisuals();
        verify(mockGeoEngine).notifyViewToRefresh();
        verify(mockDrawingView, never()).drawTemporaryGhostShape(any(Shape.class)); // Ghost should not be drawn
    }


    @Test
    void onMouseReleased_withValidDrag_shouldCreateAndAddRectangle() {
        when(mockShapeFactory.createShape("RectangleTool", p1, p2, strokeColor, fillColor, null))
            .thenReturn(createdRectangle);

        rectangleState.activate(mockGeoEngine);
        rectangleState.onMousePressed(mockGeoEngine, p1);
        rectangleState.onMouseReleased(mockGeoEngine, p2);

        InOrder inOrder = inOrder(mockDrawingView, mockGeoEngine, mockShapeFactory);
        inOrder.verify(mockDrawingView).clearTemporaryVisuals();
        inOrder.verify(mockGeoEngine).getShapeFactory();
        inOrder.verify(mockShapeFactory).createShape("RectangleTool", p1, p2, strokeColor, fillColor, null);
        inOrder.verify(mockGeoEngine).addShapeToDrawing(createdRectangle);
        inOrder.verify(mockDrawingView).showUserMessage(contains("Rectangle created"));
    }

    @Test
    void onMouseReleased_widthTooSmall_shouldNotCreateRectangle() {
        rectangleState.activate(mockGeoEngine);
        rectangleState.onMousePressed(mockGeoEngine, p1);
        rectangleState.onMouseReleased(mockGeoEngine, p3_small_x); // Small width

        verify(mockDrawingView).clearTemporaryVisuals();
        verify(mockGeoEngine).notifyViewToRefresh();
        verify(mockShapeFactory, never()).createShape(anyString(), any(), any(), any(), any(), any());
        verify(mockGeoEngine, never()).addShapeToDrawing(any(Shape.class));
    }
    
    @Test
    void onMouseReleased_heightTooSmall_shouldNotCreateRectangle() {
        rectangleState.activate(mockGeoEngine);
        rectangleState.onMousePressed(mockGeoEngine, p1);
        rectangleState.onMouseReleased(mockGeoEngine, p3_small_y); // Small height

        verify(mockDrawingView).clearTemporaryVisuals();
        verify(mockGeoEngine).notifyViewToRefresh();
        verify(mockShapeFactory, never()).createShape(anyString(), any(), any(), any(), any(), any());
        verify(mockGeoEngine, never()).addShapeToDrawing(any(Shape.class));
    }
}
            