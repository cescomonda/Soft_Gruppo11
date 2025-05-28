
package sad.gruppo11.Controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sad.gruppo11.Factory.ShapeFactory;
import sad.gruppo11.Infrastructure.*;
import sad.gruppo11.Model.*;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Vector2D;
import sad.gruppo11.View.DrawingView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class ToolStatesTest {

    @Mock private GeoEngine mockEngine;
    @Mock private Drawing mockDrawing;
    @Mock private CommandManager mockCmdMgr;
    @Mock private DrawingView mockView;
    @Mock private ShapeFactory mockFactory;
    @Mock private Shape mockShape;

    private Point2D p10_10 = new Point2D(10,10);
    private Point2D p20_20 = new Point2D(20,20);
    private Point2D p30_30 = new Point2D(30,30);


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockEngine.getDrawing()).thenReturn(mockDrawing);
        when(mockEngine.getCommandManager()).thenReturn(mockCmdMgr);
        when(mockEngine.getView()).thenReturn(mockView);
        when(mockEngine.getShapeFactory()).thenReturn(mockFactory);
        when(mockEngine.getCurrentStrokeColorForNewShapes()).thenReturn(ColorData.BLACK);
        when(mockEngine.getCurrentFillColorForNewShapes()).thenReturn(ColorData.TRANSPARENT);
        when(mockEngine.getCurrentZoom()).thenReturn(1.0); // For PolygonState threshold
    }

    // --- SelectState ---
    @Test
    void selectState_MousePressed_OnShape_SelectsShapeAndSetsDragMode() {
        SelectState state = new SelectState();
        List<Shape> shapes = Collections.singletonList(mockShape);
        when(mockDrawing.getShapesInZOrder()).thenReturn(shapes);
        when(mockShape.contains(p10_10)).thenReturn(true);

        state.onMousePressed(mockEngine, p10_10);

        verify(mockEngine).setSelectedShape(mockShape);
        verify(mockEngine).notifyViewToRefresh();
        // Internal mode check is harder without exposing state, but drag should work
    }

    @Test
    void selectState_MousePressed_EmptySpace_DeselectsAndSetsPanMode() {
        SelectState state = new SelectState();
        when(mockDrawing.getShapesInZOrder()).thenReturn(Collections.emptyList());

        state.onMousePressed(mockEngine, p10_10);

        verify(mockEngine).setSelectedShape(null);
        verify(mockEngine).notifyViewToRefresh();
        // Internal mode check for PAN_VIEW
    }

    @Test
    void selectState_MouseDragged_DragShapeMode_MovesSelectedShape() {
        SelectState state = new SelectState();
        // Setup for DRAG_SHAPE mode
        when(mockDrawing.getShapesInZOrder()).thenReturn(Collections.singletonList(mockShape));
        when(mockShape.contains(p10_10)).thenReturn(true);
        when(mockEngine.getSelectedShape()).thenReturn(mockShape);
        state.onMousePressed(mockEngine, p10_10); // Sets mode to DRAG_SHAPE

        state.onMouseDragged(mockEngine, p20_20); // p10_10 to p20_20, delta (10,10)

        verify(mockShape).move(any(Vector2D.class)); // Immediate feedback move
        verify(mockEngine, times(2)).notifyViewToRefresh(); // Once for press, once for drag
    }
    
    @Test
    void selectState_MouseDragged_PanViewMode_ScrollsEngine() {
        SelectState state = new SelectState();
        // Setup for PAN_VIEW mode
        when(mockDrawing.getShapesInZOrder()).thenReturn(Collections.emptyList());
        state.onMousePressed(mockEngine, p10_10); // Sets mode to PAN_VIEW

        state.onMouseDragged(mockEngine, p20_20); // p10_10 to p20_20, delta (10,10)

        verify(mockEngine).scroll(eq(10.0), eq(10.0));
    }
    
    @Test
    void selectState_MouseReleased_DragShapeMode_CreatesMoveCommand() {
        SelectState state = new SelectState();
        // Setup for DRAG_SHAPE mode and shapeAtPress
        when(mockDrawing.getShapesInZOrder()).thenReturn(Collections.singletonList(mockShape));
        when(mockShape.contains(p10_10)).thenReturn(true);
        state.onMousePressed(mockEngine, p10_10); // pressPos = (10,10), shapeAtPress = mockShape

        state.onMouseReleased(mockEngine, p30_30); // released at (30,30)
        // Total delta (20,20)
        
        verify(mockShape).move(argThat(v -> v.getDx() == -20.0 && v.getDy() == -20.0)); // Revert feedback
        verify(mockCmdMgr).executeCommand(argThat(cmd -> cmd instanceof MoveShapeCommand));
    }
    
    @Test
    void selectState_MouseReleased_DragShapeMode_NoSignificantMove_NoCommand() {
        SelectState state = new SelectState();
        when(mockDrawing.getShapesInZOrder()).thenReturn(Collections.singletonList(mockShape));
        when(mockShape.contains(p10_10)).thenReturn(true);
        state.onMousePressed(mockEngine, p10_10);

        Point2D slightlyMoved = new Point2D(p10_10.getX() + 0.0001, p10_10.getY());
        state.onMouseReleased(mockEngine, slightlyMoved); 
        
        // Revert move IS NOT called because total.length() is too small
        verify(mockShape, never()).move(any(Vector2D.class)); 
        verify(mockCmdMgr, never()).executeCommand(any(MoveShapeCommand.class));
        verify(mockEngine, times(2)).notifyViewToRefresh(); // press + release
    }


    // --- LineState ---
    @Test
    void lineState_LifecycleAndName() {
        LineState state = new LineState();
        assertEquals("LineTool", state.getName());
        state.activate(mockEngine);
        verify(mockView).showUserMessage(contains("Line Tool"));
        state.deactivate(mockEngine);
        verify(mockView).clearTemporaryVisuals();
        verify(mockView).clearUserMessage();
    }

    @Test
    void lineState_DrawLine_CreatesAddCommand() {
        LineState state = new LineState();
        Shape createdLine = mock(LineSegment.class);
        when(mockFactory.createShape(eq("LineTool"), eq(p10_10), eq(p20_20), any(), any(), eq(null)))
            .thenReturn(createdLine);

        state.onMousePressed(mockEngine, p10_10);
        state.onMouseDragged(mockEngine, new Point2D(15,15)); // Ghost drawn
        verify(mockView).drawTemporaryGhostShape(any(LineSegment.class));
        verify(mockEngine).notifyViewToRefresh();

        state.onMouseReleased(mockEngine, p20_20);
        verify(mockView, times(1)).clearTemporaryVisuals(); // Called on release
        verify(mockCmdMgr).executeCommand(any(AddShapeCommand.class));
        verify(mockView).showUserMessage(contains("Line created"));
    }
    
    @Test
    void lineState_DrawLine_TooShort_NoCommand() {
        LineState state = new LineState();
        Point2D pClose = new Point2D(p10_10.getX() + 0.1, p10_10.getY() + 0.1);

        state.onMousePressed(mockEngine, p10_10);
        state.onMouseReleased(mockEngine, pClose);

        verify(mockFactory, never()).createShape(anyString(), any(), any(), any(), any(), any());
        verify(mockCmdMgr, never()).executeCommand(any());
        verify(mockEngine, times(1)).notifyViewToRefresh(); // For clearing ghost
    }


    // --- RectangleState ---
    @Test
    void rectangleState_DrawRectangle_CreatesAddCommand() {
        RectangleState state = new RectangleState();
        Shape createdRect = mock(RectangleShape.class);
        when(mockFactory.createShape(eq("RectangleTool"), eq(p10_10), eq(p20_20), any(), any(), eq(null)))
            .thenReturn(createdRect);
            
        state.onMousePressed(mockEngine, p10_10);
        state.onMouseDragged(mockEngine, new Point2D(15,15));
        verify(mockView).drawTemporaryGhostShape(any(RectangleShape.class));

        state.onMouseReleased(mockEngine, p20_20);
        verify(mockCmdMgr).executeCommand(any(AddShapeCommand.class));
    }

    // --- EllipseState ---
    @Test
    void ellipseState_DrawEllipse_CreatesAddCommand() {
        EllipseState state = new EllipseState();
        Shape createdEllipse = mock(EllipseShape.class);
        when(mockFactory.createShape(eq("EllipseTool"), eq(p10_10), eq(p20_20), any(), any(), eq(null)))
            .thenReturn(createdEllipse);

        state.onMousePressed(mockEngine, p10_10);
        state.onMouseDragged(mockEngine, new Point2D(15,15));
        verify(mockView).drawTemporaryGhostShape(any(EllipseShape.class));

        state.onMouseReleased(mockEngine, p20_20);
        verify(mockCmdMgr).executeCommand(any(AddShapeCommand.class));
    }
    
    // --- PolygonState ---
    @Test
    void polygonState_AddPointsAndFinish_CreatesAddCommand() {
        PolygonState state = new PolygonState();
        Shape createdPolygon = mock(PolygonShape.class);
        
        // Capture the vertices passed to the factory
        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        when(mockFactory.createShape(eq("PolygonTool"), eq(null), eq(null), any(), any(), paramsCaptor.capture()))
            .thenReturn(createdPolygon);

        state.activate(mockEngine); // Resets points
        state.onMousePressed(mockEngine, p10_10); // 1st point
        verify(mockView).drawTemporaryPolygonGuide(anyList(), eq(p10_10));
        
        state.onMousePressed(mockEngine, p20_20); // 2nd point
        verify(mockView).drawTemporaryPolygonGuide(anyList(), eq(p20_20));

        state.onMouseDragged(mockEngine, new Point2D(25,25)); // Rubber band for 3rd point
        verify(mockView).drawTemporaryPolygonGuide(anyList(), eq(new Point2D(25,25)));

        state.onMousePressed(mockEngine, p30_30); // 3rd point
        
        // Finish by action (e.g., Enter key)
        state.tryFinishPolygonOnAction(mockEngine);

        verify(mockCmdMgr).executeCommand(any(AddShapeCommand.class));
        verify(mockView, times(1)).clearTemporaryVisuals(); // activate calls it, finishPolygon calls it
        
        Map<String, Object> capturedParams = paramsCaptor.getValue();
        assertTrue(capturedParams.containsKey("vertices"));
        List<Point2D> capturedVertices = (List<Point2D>) capturedParams.get("vertices");
        assertEquals(3, capturedVertices.size());
        assertTrue(capturedVertices.contains(p10_10));
        assertTrue(capturedVertices.contains(p20_20));
        assertTrue(capturedVertices.contains(p30_30));
    }

    @Test
    void polygonState_FinishByClickingNearStart() {
        PolygonState state = new PolygonState();
        Shape createdPolygon = mock(PolygonShape.class);
        when(mockFactory.createShape(eq("PolygonTool"), any(), any(), any(), any(), any(Map.class)))
            .thenReturn(createdPolygon);

        state.activate(mockEngine);
        state.onMousePressed(mockEngine, p10_10); // (10,10)
        state.onMousePressed(mockEngine, p20_20); // (20,20)
        state.onMousePressed(mockEngine, new Point2D(10,20)); // (10,20) - 3rd point
        
        // Click near start point (10,10) -> e.g. (10+2, 10+2) = (12,12)
        // Threshold is 10.0 / zoom (1.0) = 10.0. Distance from (12,12) to (10,10) is sqrt(8) approx 2.8 < 10.
        state.onMousePressed(mockEngine, new Point2D(12,12)); 
        
        verify(mockCmdMgr).executeCommand(any(AddShapeCommand.class));
    }
    
    @Test
    void polygonState_TryFinish_NotEnoughPoints() {
        PolygonState state = new PolygonState();
        state.activate(mockEngine);
        state.onMousePressed(mockEngine, p10_10);
        state.onMousePressed(mockEngine, p20_20); // Only 2 points
        
        state.tryFinishPolygonOnAction(mockEngine);
        verify(mockCmdMgr, never()).executeCommand(any());
        verify(mockView).showUserMessage(contains("Not enough points"));
    }

    // --- TextState ---
    @Test
    void textState_MousePressed_CreatesTextAndAddCommand() {
        TextState state = new TextState();
        String inputText = "Test Text";
        double fontSize = 12.0;
        String fontName = "Arial";
        Shape createdText = mock(TextShape.class);

        when(mockView.promptForText(anyString())).thenReturn(inputText);
        when(mockEngine.getCurrentDefaultFontSize()).thenReturn(fontSize);
        when(mockEngine.getCurrentDefaultFontName()).thenReturn(fontName);
        
        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        when(mockFactory.createShape(eq("TextTool"), eq(null), eq(null), any(), any(), paramsCaptor.capture()))
            .thenReturn(createdText);

        state.onMousePressed(mockEngine, p10_10);
        
        verify(mockCmdMgr).executeCommand(any(AddShapeCommand.class));
        
        Map<String, Object> capturedParams = paramsCaptor.getValue();
        assertEquals(inputText, capturedParams.get("text"));
        assertEquals(fontSize, (Double) capturedParams.get("fontSize"));
        assertEquals(fontName, (String) capturedParams.get("fontName"));
        assertEquals(p10_10, (Point2D) capturedParams.get("position"));
    }
    
    @Test
    void textState_MousePressed_PromptCancelled_NoCommand() {
        TextState state = new TextState();
        when(mockView.promptForText(anyString())).thenReturn(null); // User cancelled

        state.onMousePressed(mockEngine, p10_10);
        
        verify(mockFactory, never()).createShape(anyString(), any(), any(), any(), any(), any());
        verify(mockCmdMgr, never()).executeCommand(any());
    }
    
    @Test
    void textState_MousePressed_PromptEmpty_NoCommand() {
        TextState state = new TextState();
        when(mockView.promptForText(anyString())).thenReturn("  "); // Empty or whitespace

        state.onMousePressed(mockEngine, p10_10);
        
        verify(mockFactory, never()).createShape(anyString(), any(), any(), any(), any(), any());
        verify(mockCmdMgr, never()).executeCommand(any());
    }
}
