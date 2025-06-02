
package sad.gruppo11.Controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import sad.gruppo11.Factory.ShapeFactory;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.TextShape;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.View.DrawingView;

import java.util.Map;

class TextStateTest {

    private TextState textState;
    private GeoEngine mockGeoEngine;
    private DrawingView mockDrawingView;
    private ShapeFactory mockShapeFactory;
    private Point2D clickPosition;
    private String inputText;
    private double fontSize;
    private String fontName;
    private ColorData textColor;
    private TextShape createdTextShape;

    @BeforeEach
    void setUp() {
        textState = new TextState();
        mockGeoEngine = mock(GeoEngine.class);
        mockDrawingView = mock(DrawingView.class);
        mockShapeFactory = mock(ShapeFactory.class);

        clickPosition = new Point2D(50, 50);
        inputText = "Hello World";
        fontSize = 16.0;
        fontName = "Verdana";
        textColor = ColorData.BLACK; // Text uses stroke color by default in state

        // createdTextShape will be null if factory returns null, or a TextShape instance
        // For simplicity in verifying, we'll assume factory returns a valid TextShape if params are good.
        createdTextShape = new TextShape(inputText, clickPosition, fontSize, fontName, textColor);

        when(mockGeoEngine.getView()).thenReturn(mockDrawingView);
        when(mockGeoEngine.getShapeFactory()).thenReturn(mockShapeFactory);
        when(mockGeoEngine.getCurrentStrokeColorForNewShapes()).thenReturn(textColor);
        when(mockGeoEngine.getCurrentDefaultFontSize()).thenReturn(fontSize);
        when(mockGeoEngine.getCurrentDefaultFontName()).thenReturn(fontName);
    }

    @Test
    void getName_shouldReturnCorrectName() {
        assertEquals("TextTool", textState.getName());
    }

    @Test
    void activate_shouldClearSelectionAndShowMessage() {
        textState.activate(mockGeoEngine);
        verify(mockGeoEngine).clearSelection();
        verify(mockDrawingView).showUserMessage(contains("Text Tool"));
    }

    @Test
    void deactivate_shouldClearUserMessage() {
        textState.activate(mockGeoEngine); // Activate first
        textState.deactivate(mockGeoEngine);
        verify(mockDrawingView).clearUserMessage();
    }

    @Test
    void onMousePressed_withValidInput_shouldCreateAndAddTextShape() {
        when(mockDrawingView.promptForText(anyString(), anyString())).thenReturn(inputText);
        // Capture the map passed to the factory
        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        
        when(mockShapeFactory.createShape(eq("TextTool"), eq(null), eq(null), eq(textColor), eq(ColorData.TRANSPARENT), paramsCaptor.capture()))
            .thenReturn(createdTextShape);

        textState.onMousePressed(mockGeoEngine, clickPosition);

        InOrder inOrder = inOrder(mockDrawingView, mockGeoEngine, mockShapeFactory);
        inOrder.verify(mockDrawingView).promptForText(contains("Enter text:"), eq(""));
        inOrder.verify(mockGeoEngine).getShapeFactory();
        inOrder.verify(mockShapeFactory).createShape(eq("TextTool"), eq(null), eq(null), eq(textColor), eq(ColorData.TRANSPARENT), any(Map.class));
        
        Map<String, Object> capturedParams = paramsCaptor.getValue();
        assertEquals(inputText, capturedParams.get("text"));
        assertEquals(fontSize, (Double) capturedParams.get("fontSize"), 0.001);
        assertEquals(fontName, capturedParams.get("fontName"));
        assertEquals(clickPosition, capturedParams.get("position"));
        
        inOrder.verify(mockGeoEngine).addShapeToDrawing(createdTextShape);
        inOrder.verify(mockDrawingView).showUserMessage(contains("Text added"));
    }

    @Test
    void onMousePressed_userInputIsNull_shouldNotCreateShape() {
        when(mockDrawingView.promptForText(anyString(), anyString())).thenReturn(null); // User cancels

        textState.onMousePressed(mockGeoEngine, clickPosition);

        verify(mockDrawingView).promptForText(anyString(), anyString());
        verify(mockShapeFactory, never()).createShape(anyString(), any(), any(), any(), any(), any());
        verify(mockGeoEngine, never()).addShapeToDrawing(any(Shape.class));
        verify(mockDrawingView).showUserMessage(contains("Text input cancelled or empty"));
    }

    @Test
    void onMousePressed_userInputIsEmpty_shouldNotCreateShape() {
        when(mockDrawingView.promptForText(anyString(), anyString())).thenReturn(""); // User enters empty string

        textState.onMousePressed(mockGeoEngine, clickPosition);

        verify(mockDrawingView).promptForText(anyString(), anyString());
        verify(mockShapeFactory, never()).createShape(anyString(), any(), any(), any(), any(), any());
        verify(mockGeoEngine, never()).addShapeToDrawing(any(Shape.class));
        verify(mockDrawingView).showUserMessage(contains("Text input cancelled or empty"));
    }

    @Test
    void onMousePressed_shapeFactoryReturnsNull_shouldNotAddShape() {
        when(mockDrawingView.promptForText(anyString(), anyString())).thenReturn(inputText);
        when(mockShapeFactory.createShape(eq("TextTool"), eq(null), eq(null), eq(textColor), eq(ColorData.TRANSPARENT), any(Map.class)))
            .thenReturn(null);

        textState.onMousePressed(mockGeoEngine, clickPosition);

        verify(mockGeoEngine, never()).addShapeToDrawing(any(Shape.class));
        // The "Text added" message might still be shown if not conditional on newLine != null in state
        // TextState code: if (newTextShape != null) { ... showUserMessage("Text added.") }
        // So, the message should not be shown here.
        verify(mockDrawingView, never()).showUserMessage(contains("Text added"));
    }
    
    @Test
    void onMousePressed_drawingViewIsNull_shouldLogErrorAndNotProceed() {
        // This tests robustness if view is somehow not set on GeoEngine
        when(mockGeoEngine.getView()).thenReturn(null);
        
        // We can't directly assert System.err output easily without custom streams,
        // but we can verify no further interactions that depend on view occur.
        textState.onMousePressed(mockGeoEngine, clickPosition);
        
        verify(mockGeoEngine, never()).getShapeFactory(); // Should not proceed to factory if view is null for prompt
        verify(mockGeoEngine, never()).addShapeToDrawing(any(Shape.class));
    }


    @Test
    void onMouseDragged_shouldDoNothing() {
        textState.onMouseDragged(mockGeoEngine, clickPosition);
        verifyNoInteractions(mockDrawingView, mockShapeFactory); // Besides initial activate/getView
        verify(mockGeoEngine, atMost(1)).getView(); // Might be called in activate
    }

    @Test
    void onMouseReleased_shouldDoNothing() {
        textState.onMouseReleased(mockGeoEngine, clickPosition);
        verifyNoInteractions(mockDrawingView, mockShapeFactory);
        verify(mockGeoEngine, atMost(1)).getView();
    }
}
            