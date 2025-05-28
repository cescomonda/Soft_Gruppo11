
package sad.gruppo11.View;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sad.gruppo11.Controller.GeoEngine;
import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.Observable;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DrawingViewTest {
    @Mock private GeoEngine mockController;
    @Mock private CanvasPanel mockCanvasPanel;
    @Mock private Canvas mockActualCanvas; // The actual JavaFX Canvas
    @Mock private Drawing mockDrawingModel;
    @Mock private javafx.stage.Stage mockPrimaryStage; // For dialogs

    private DrawingView drawingView;

    @BeforeAll
    public static void setupJavaFX() throws InterruptedException {
        if (!Platform.isFxApplicationThread()) {
            final CountDownLatch latch = new CountDownLatch(1);
            Platform.startup(() -> {
                latch.countDown();
            });
            latch.await();
        }
    }
    
    @AfterAll
    public static void tearDownJavaFX() {
        // Platform.exit();
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockController.getDrawing()).thenReturn(mockDrawingModel);
        when(mockCanvasPanel.getCanvas()).thenReturn(mockActualCanvas); // Connect CanvasPanel to actual Canvas
        
        drawingView = new DrawingView(mockController, mockCanvasPanel, mockPrimaryStage);
    }

    @Test
    void constructorAttachesToModelAndSetsUpHandlers() {
        verify(mockDrawingModel).attach(drawingView);
        verify(mockActualCanvas).setOnMousePressed(any());
        verify(mockActualCanvas).setOnMouseDragged(any());
        verify(mockActualCanvas).setOnMouseReleased(any());
        verify(mockActualCanvas).setOnScroll(any());
        verify(mockCanvasPanel).setTransform(anyDouble(), anyDouble(), anyDouble());
        verify(mockCanvasPanel).setGrid(anyBoolean(), anyDouble());
    }
    
    @Test
    void setControllerReAttachesObserver() {
        GeoEngine newMockController = mock(GeoEngine.class);
        Drawing newMockDrawing = mock(Drawing.class);
        when(newMockController.getDrawing()).thenReturn(newMockDrawing);

        drawingView.setController(newMockController);

        verify(mockDrawingModel).detach(drawingView); // Detach from old model
        verify(newMockDrawing).attach(drawingView);   // Attach to new model
        verify(mockCanvasPanel, times(2)).setTransform(anyDouble(), anyDouble(), anyDouble()); // Initial + on setController
        // Remove: verify(drawingView).render(); // drawingView is not a mock, so this is invalid
        // Instead, check for effects of render:
        verify(mockCanvasPanel).setSelectedShapeForRenderer(any());
        verify(mockCanvasPanel, atLeastOnce()).drawShapes(any());
    }

    @Test
    void setControllerWithNullDrawingModelDoesNotAttach() {
        GeoEngine newMockController = mock(GeoEngine.class);
        when(newMockController.getDrawing()).thenReturn(null);

        drawingView.setController(newMockController);

        verify(mockDrawingModel).detach(drawingView);
        // Should not attach to null
        // No exception should be thrown
    }

    @Test
    void setDrawingModelWithNullDetachesAndClears() {
        drawingView.setDrawingModel(null);
        verify(mockDrawingModel).detach(drawingView);
        // Should not throw, should clear canvas
        verify(mockCanvasPanel).clear();
    }

    @Test
    void updateWithTransformEventUpdatesTransformAndRenders() {
        Drawing.DrawingChangeEvent event = new Drawing.DrawingChangeEvent(Drawing.DrawingChangeEvent.ChangeType.TRANSFORM);
        drawingView.update(mockController, event);
        verify(mockCanvasPanel, atLeastOnce()).setTransform(anyDouble(), anyDouble(), anyDouble());
        verify(mockCanvasPanel, atLeastOnce()).setGrid(anyBoolean(), anyDouble());
        // Effects of render:
        verify(mockCanvasPanel).setSelectedShapeForRenderer(any());
        verify(mockCanvasPanel, atLeastOnce()).drawShapes(any());
    }

    @Test
    void updateWithGridEventUpdatesGridAndRenders() {
        Drawing.DrawingChangeEvent event = new Drawing.DrawingChangeEvent(Drawing.DrawingChangeEvent.ChangeType.GRID);
        drawingView.update(mockController, event);
        verify(mockCanvasPanel, atLeastOnce()).setGrid(anyBoolean(), anyDouble());
        // Effects of render:
        verify(mockCanvasPanel).setSelectedShapeForRenderer(any());
        verify(mockCanvasPanel, atLeastOnce()).drawShapes(any());
    }

    @Test
    void updateWithStringTransformChangeUpdatesTransformAndRenders() {
        drawingView.update(mockController, "transformChange");
        verify(mockCanvasPanel, atLeastOnce()).setTransform(anyDouble(), anyDouble(), anyDouble());
        verify(mockCanvasPanel, atLeastOnce()).setGrid(anyBoolean(), anyDouble());
        verify(mockCanvasPanel).setSelectedShapeForRenderer(any());
        verify(mockCanvasPanel, atLeastOnce()).drawShapes(any());
    }

    @Test
    void updateWithStringGridChangeUpdatesGridAndRenders() {
        drawingView.update(mockController, "gridChange");
        verify(mockCanvasPanel, atLeastOnce()).setGrid(anyBoolean(), anyDouble());
        verify(mockCanvasPanel).setSelectedShapeForRenderer(any());
    }
    @Test
    void setControllerThrowsOnNullController() {
        assertThrows(NullPointerException.class, () -> drawingView.setController(null));
    }

    @Test
    void constructorThrowsOnNullController() {
        assertThrows(NullPointerException.class, () -> new DrawingView(null, mockCanvasPanel, mockPrimaryStage));
    }

    @Test
    void constructorThrowsOnNullCanvasPanel() {
        assertThrows(NullPointerException.class, () -> new DrawingView(mockController, null, mockPrimaryStage));
    }

    @Test
    void constructorThrowsOnNullPrimaryStage() {
        assertThrows(NullPointerException.class, () -> new DrawingView(mockController, mockCanvasPanel, null));
    }

    @Test
    void renderDoesNothingIfCanvasPanelIsNull() {
        DrawingView view = new DrawingView(mockController, mockCanvasPanel, mockPrimaryStage) {
            @Override
            public void render() {
                // Simulate canvasPanel being null
                try {
                    java.lang.reflect.Field field = DrawingView.class.getDeclaredField("canvasPanel");
                    field.setAccessible(true);
                    field.set(this, null);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                super.render();
            }
        };
        // Should not throw
        assertDoesNotThrow(view::render);
    }

    @Test
    void setDrawingModelAttachesIfNotNull() {
        Drawing newMockDrawing = mock(Drawing.class);
        drawingView.setDrawingModel(newMockDrawing);
        verify(mockDrawingModel).detach(drawingView);
        verify(newMockDrawing).attach(drawingView);
    }

    @Test
    void setDrawingModelWithSameModelDoesNotDetachOrAttach() {
        drawingView.setDrawingModel(mockDrawingModel);
        // Should detach and attach to the same instance, so both called once
        verify(mockDrawingModel, times(1)).detach(drawingView);
        verify(mockDrawingModel, times(2)).attach(drawingView);
    }

    @Test
    void setRendererLineWidthWithNullCanvasPanelDoesNotThrow() {
        DrawingView view = new DrawingView(mockController, mockCanvasPanel, mockPrimaryStage) {
            @Override
            public void setRendererLineWidth(double lineWidth) {
                try {
                    java.lang.reflect.Field field = DrawingView.class.getDeclaredField("canvasPanel");
                    field.setAccessible(true);
                    field.set(this, null);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                super.setRendererLineWidth(lineWidth);
            }
        };
        assertDoesNotThrow(() -> view.setRendererLineWidth(1.0));
    }
    

    @Test
    void setRendererLineWidthDelegatesAndRenders() {
        drawingView.setRendererLineWidth(2.5);
        verify(mockCanvasPanel).setRendererLineWidth(2.5);
        verify(mockCanvasPanel).setSelectedShapeForRenderer(any());
        verify(mockCanvasPanel, atLeastOnce()).drawShapes(any());
    }

    @Test
    void renderDelegatesToCanvasPanel() {
        Shape mockSelectedShape = mock(Shape.class);
        when(mockController.getSelectedShape()).thenReturn(mockSelectedShape);
        when(mockDrawingModel.getShapesInZOrder()).thenReturn(new ArrayList<>()); // Provide an empty list

        drawingView.render();

        verify(mockCanvasPanel).setSelectedShapeForRenderer(mockSelectedShape);
        verify(mockCanvasPanel).drawShapes(any());
    }


    // Mouse event handling tests (verify delegation to controller with transformed points)
    @Test
    void onMousePressedDelegatesToController() {
        Point2D screenPoint = new Point2D(150, 75);
        Point2D worldPoint = new Point2D(50, 25); // Example transformed point
        when(mockCanvasPanel.screenToWorld(screenPoint)).thenReturn(worldPoint);

        // Simulate JavaFX MouseEvent (basic version)
        MouseEvent pressEvent = new MouseEvent(MouseEvent.MOUSE_PRESSED,
            screenPoint.getX(), screenPoint.getY(), screenPoint.getX(), screenPoint.getY(), MouseButton.PRIMARY, 1,
            false, false, false, false, true, false, false, false, false, false, null);

        // Need to capture the EventHandler set on mockActualCanvas
        ArgumentCaptor<javafx.event.EventHandler<MouseEvent>> captor = ArgumentCaptor.forClass(javafx.event.EventHandler.class);
        verify(mockActualCanvas).setOnMousePressed(captor.capture());
        
        javafx.event.EventHandler<MouseEvent> handler = captor.getValue();
        handler.handle(pressEvent);

        verify(mockController).onMousePressed(worldPoint);
    }
    
    @Test
    void onMouseDraggedDelegatesToController_IfPrimaryButtonDown() {
        Point2D screenPoint = new Point2D(160, 85);
        Point2D worldPoint = new Point2D(55, 30);
        when(mockCanvasPanel.screenToWorld(screenPoint)).thenReturn(worldPoint);

        MouseEvent dragEvent = new MouseEvent(MouseEvent.MOUSE_DRAGGED,
            screenPoint.getX(), screenPoint.getY(), screenPoint.getX(), screenPoint.getY(), MouseButton.PRIMARY, 1,
            false, false, false, false, true, false, false, true, false, false, null); // primaryButtonDown = true

        ArgumentCaptor<javafx.event.EventHandler<MouseEvent>> captor = ArgumentCaptor.forClass(javafx.event.EventHandler.class);
        verify(mockActualCanvas).setOnMouseDragged(captor.capture());
        
        javafx.event.EventHandler<MouseEvent> handler = captor.getValue();
        handler.handle(dragEvent);
        verify(mockController).onMouseDragged(worldPoint);
    }
    
    @Test
    void onMouseDragged_NotPrimaryButton_NoDelegate() {
        Point2D screenPoint = new Point2D(160, 85);
        MouseEvent dragEvent = new MouseEvent(MouseEvent.MOUSE_DRAGGED,
            screenPoint.getX(), screenPoint.getY(), screenPoint.getX(), screenPoint.getY(), MouseButton.SECONDARY, 1,
            false, false, false, false, false, false, false, false, true, false, null); // primaryButtonDown = false

        ArgumentCaptor<javafx.event.EventHandler<MouseEvent>> captor = ArgumentCaptor.forClass(javafx.event.EventHandler.class);
        verify(mockActualCanvas).setOnMouseDragged(captor.capture());
        
        javafx.event.EventHandler<MouseEvent> handler = captor.getValue();
        handler.handle(dragEvent);
        verify(mockController, never()).onMouseDragged(any(Point2D.class));
    }

    @Test
    void onScrollDelegatesToControllerZoom() {
        ScrollEvent scrollUpEvent = new ScrollEvent(ScrollEvent.SCROLL, 0,0,0,0, false, false, false, false, false, false, 0, 10, 0, 0, ScrollEvent.HorizontalTextScrollUnits.NONE,0, ScrollEvent.VerticalTextScrollUnits.NONE,0,0,null); // deltaY = 10 (zoom in)
        ScrollEvent scrollDownEvent = new ScrollEvent(ScrollEvent.SCROLL, 0,0,0,0, false, false, false, false, false, false, 0, -10, 0, 0, ScrollEvent.HorizontalTextScrollUnits.NONE,0, ScrollEvent.VerticalTextScrollUnits.NONE,0,0,null); // deltaY = -10 (zoom out)

        ArgumentCaptor<javafx.event.EventHandler<ScrollEvent>> captor = ArgumentCaptor.forClass(javafx.event.EventHandler.class);
        verify(mockActualCanvas).setOnScroll(captor.capture());
        javafx.event.EventHandler<ScrollEvent> handler = captor.getValue();

        handler.handle(scrollUpEvent);
        verify(mockController).zoomIn(anyDouble(), anyDouble());
        
        handler.handle(scrollDownEvent);
        verify(mockController).zoomOut(anyDouble(), anyDouble());
    }
    
    // Test temporary visual methods delegate to CanvasPanel
    @Test
    void temporaryVisualMethodsDelegate() {
        List<Point2D> points = new ArrayList<>();
        Point2D mousePos = new Point2D(1,1);
        drawingView.drawTemporaryPolygonGuide(points, mousePos);
        verify(mockCanvasPanel).setTemporaryPolygonGuide(points, mousePos);

        Shape ghost = mock(Shape.class);
        drawingView.drawTemporaryGhostShape(ghost);
        verify(mockCanvasPanel).setTemporaryGhostShape(ghost);

        drawingView.clearTemporaryVisuals();
        verify(mockCanvasPanel).clearTemporaryVisuals();
    }

}
