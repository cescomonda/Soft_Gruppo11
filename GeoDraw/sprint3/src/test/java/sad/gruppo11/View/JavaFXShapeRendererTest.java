
package sad.gruppo11.View;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;

import static org.mockito.MockitoAnnotations.openMocks;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import javafx.geometry.VPos;


import sad.gruppo11.Model.*;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Rect;

import java.util.Arrays;
import java.util.List;

class JavaFXShapeRendererTest {

    @Mock private GraphicsContext mockGc;
    private JavaFXShapeRenderer renderer;

    private final double DEFAULT_LINE_WIDTH = 1.5;

    @BeforeEach
    void setUp() {
        mockGc = mock(GraphicsContext.class); 
        renderer = new JavaFXShapeRenderer(mockGc);
        renderer.setDefaultLineWidth(DEFAULT_LINE_WIDTH);
    }

    
    @Test
    void constructor_nullGraphicsContext_shouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> new JavaFXShapeRenderer(null));
    }

    @Test
    void setDefaultLineWidth_validWidth_shouldSet() {
        renderer.setDefaultLineWidth(2.5);
        assertEquals(2.5, renderer.getDefaultLineWidth());
    }

    @Test
    void setDefaultLineWidth_invalidWidth_shouldNotSet() {
        renderer.setDefaultLineWidth(0.0);
        assertEquals(DEFAULT_LINE_WIDTH, renderer.getDefaultLineWidth());
        renderer.setDefaultLineWidth(-1.0);
        assertEquals(DEFAULT_LINE_WIDTH, renderer.getDefaultLineWidth());
    }

    @Test
    void convertModelToFxColor_validColorData_shouldConvert() {
        ColorData modelColor = new ColorData(255, 0, 0, 0.5); // Red, 50% transparent
        Color fxColor = JavaFXShapeRenderer.convertModelToFxColor(modelColor);
        assertEquals(1.0, fxColor.getRed());
        assertEquals(0.0, fxColor.getGreen());
        assertEquals(0.0, fxColor.getBlue());
        assertEquals(0.5, fxColor.getOpacity());
    }

    @Test
    void convertModelToFxColor_nullColorData_shouldReturnTransparent() {
        Color fxColor = JavaFXShapeRenderer.convertModelToFxColor(null);
        assertEquals(Color.TRANSPARENT, fxColor);
    }

    @Test
    void visit_rectangleShape_noRotation_shouldDrawCorrectly() {
        Rect bounds = new Rect(10, 20, 30, 40);
        ColorData stroke = ColorData.BLACK;
        ColorData fill = ColorData.RED;
        RectangleShape rectShape = new RectangleShape(bounds, stroke, fill);

        rectShape.accept(renderer);

        InOrder inOrder = inOrder(mockGc);
        inOrder.verify(mockGc).save();
        // No rotation, so no gc.transform(Rotate) call
        inOrder.verify(mockGc).setFill(JavaFXShapeRenderer.convertModelToFxColor(fill));
        inOrder.verify(mockGc).fillRect(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
        inOrder.verify(mockGc).setStroke(JavaFXShapeRenderer.convertModelToFxColor(stroke));
        inOrder.verify(mockGc).setLineWidth(DEFAULT_LINE_WIDTH);
        inOrder.verify(mockGc).setLineCap(StrokeLineCap.SQUARE);
        inOrder.verify(mockGc).setLineJoin(StrokeLineJoin.MITER);
        inOrder.verify(mockGc).strokeRect(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
        inOrder.verify(mockGc).restore();
    }

    @Test
    void visit_rectangleShape_withRotation_shouldApplyRotation() {
        Rect bounds = new Rect(10, 20, 30, 40);
        ColorData stroke = ColorData.BLACK;
        ColorData fill = ColorData.RED;
        RectangleShape rectShape = new RectangleShape(bounds, stroke, fill);
        rectShape.setRotation(90);

        rectShape.accept(renderer);

        InOrder inOrder = inOrder(mockGc);
        inOrder.verify(mockGc).save();
        // Verify transform for rotation is called
        Point2D center = bounds.getCenter();
        Rotate expectedRotate = new Rotate(90, center.getX(), center.getY());
        inOrder.verify(mockGc).transform(
            anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble()
        );
        inOrder.verify(mockGc, times(1)).fillRect(anyDouble(), anyDouble(), anyDouble(), anyDouble());
        inOrder.verify(mockGc, times(1)).strokeRect(anyDouble(), anyDouble(), anyDouble(), anyDouble());
        inOrder.verify(mockGc).restore();
    }
    
    @Test
    void visit_rectangleShape_transparentFill_shouldNotCallFillRect() {
        Rect bounds = new Rect(10, 20, 30, 40);
        RectangleShape rectShape = new RectangleShape(bounds, ColorData.BLACK, ColorData.TRANSPARENT);
        rectShape.accept(renderer);
        verify(mockGc, never()).fillRect(anyDouble(), anyDouble(), anyDouble(), anyDouble());
        verify(mockGc).strokeRect(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
    }

    @Test
    void visit_selectedRectangleShape_shouldDrawSelectionIndicator() {
        Rect bounds = new Rect(10, 10, 5, 5);
        RectangleShape rectShape = new RectangleShape(bounds, ColorData.BLACK, ColorData.RED);
        renderer.setSelectedShapeForRendering(rectShape); // Mark as selected

        rectShape.accept(renderer);

        // After shape drawing and its gc.restore()
        // Another gc.save(), setStroke, setLineWidth, setLineDashes, strokeRect, gc.restore() for indicator
        verify(mockGc, times(2)).save(); // Once for shape, once for indicator
        verify(mockGc, times(2)).restore();
        verify(mockGc).setLineDashes(4,4); // Characteristic of selection indicator
        verify(mockGc).strokeRect(eq(bounds.getX() - 2.0), eq(bounds.getY() - 2.0), 
                                  eq(bounds.getWidth() + 4.0), eq(bounds.getHeight() + 4.0));
    }

    @Test
    void visit_ellipseShape_shouldDrawCorrectly() {
        Rect bounds = new Rect(5, 10, 15, 25);
        EllipseShape ellipse = new EllipseShape(bounds, ColorData.BLUE, ColorData.YELLOW);
        ellipse.setRotation(45);
        
        ellipse.accept(renderer);

        InOrder inOrder = inOrder(mockGc);
        inOrder.verify(mockGc).save();
        Point2D center = bounds.getCenter();
        Rotate expectedRotate = new Rotate(45, center.getX(), center.getY());
        inOrder.verify(mockGc).transform(anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble()); // Rotation
        inOrder.verify(mockGc).setFill(JavaFXShapeRenderer.convertModelToFxColor(ColorData.YELLOW));
        inOrder.verify(mockGc).fillOval(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
        inOrder.verify(mockGc).setStroke(JavaFXShapeRenderer.convertModelToFxColor(ColorData.BLUE));
        inOrder.verify(mockGc).setLineWidth(DEFAULT_LINE_WIDTH);
        inOrder.verify(mockGc).strokeOval(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
        inOrder.verify(mockGc).restore();
    }

    @Test
    void visit_lineSegment_shouldDrawCorrectly() {
        Point2D start = new Point2D(0,0);
        Point2D end = new Point2D(50,50);
        LineSegment line = new LineSegment(start, end, ColorData.GREEN);
        line.setRotation(30);

        line.accept(renderer);

        InOrder inOrder = inOrder(mockGc);
        inOrder.verify(mockGc).save();
        Point2D center = line.getBounds().getCenter();
        Rotate expectedRotate = new Rotate(30, center.getX(), center.getY());
        inOrder.verify(mockGc).transform(anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble()); // Rotation
        inOrder.verify(mockGc).setStroke(JavaFXShapeRenderer.convertModelToFxColor(ColorData.GREEN));
        inOrder.verify(mockGc).setLineWidth(DEFAULT_LINE_WIDTH);
        inOrder.verify(mockGc).setLineCap(StrokeLineCap.ROUND);
        inOrder.verify(mockGc).strokeLine(start.getX(), start.getY(), end.getX(), end.getY());
        inOrder.verify(mockGc).restore();
    }
    
    @Test
    void visit_selectedLineSegment_shouldDrawSelectionIndicatorForLine() {
        Point2D start = new Point2D(1,1);
        Point2D end = new Point2D(2,2);
        LineSegment line = new LineSegment(start, end, ColorData.BLACK);
        renderer.setSelectedShapeForRendering(line);

        line.accept(renderer);

        verify(mockGc, times(2)).save();
        verify(mockGc, times(2)).restore();
        verify(mockGc).setLineDashes(4,4);
        // Specific check for the bounding box of the line indicator
        double padding = 3.0;
        double minX = Math.min(start.getX(), end.getX()) - padding;
        double minY = Math.min(start.getY(), end.getY()) - padding;
        double width = Math.abs(start.getX() - end.getX()) + 2 * padding;
        double height = Math.abs(start.getY() - end.getY()) + 2 * padding;
        verify(mockGc).strokeRect(eq(minX), eq(minY), eq(width), eq(height));
    }

    @Test
    void visit_polygonShape_shouldDrawCorrectly() {
        List<Point2D> vertices = Arrays.asList(new Point2D(0,0), new Point2D(10,0), new Point2D(5,10));
        PolygonShape polygon = new PolygonShape(vertices, ColorData.BLACK, ColorData.BLUE);
        polygon.setRotation(-30);
        
        polygon.accept(renderer);

        double[] xPoints = vertices.stream().mapToDouble(Point2D::getX).toArray();
        double[] yPoints = vertices.stream().mapToDouble(Point2D::getY).toArray();

        InOrder inOrder = inOrder(mockGc);
        inOrder.verify(mockGc).save();
        Point2D center = polygon.getBounds().getCenter();
        Rotate expectedRotate = new Rotate(-30, center.getX(), center.getY());
        inOrder.verify(mockGc).transform(anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble()); // Rotation
        inOrder.verify(mockGc).setFill(JavaFXShapeRenderer.convertModelToFxColor(ColorData.BLUE));
        inOrder.verify(mockGc).fillPolygon(xPoints, yPoints, vertices.size());
        inOrder.verify(mockGc).setStroke(JavaFXShapeRenderer.convertModelToFxColor(ColorData.BLACK));
        inOrder.verify(mockGc).setLineWidth(DEFAULT_LINE_WIDTH);
        inOrder.verify(mockGc).setLineJoin(StrokeLineJoin.MITER);
        inOrder.verify(mockGc).strokePolygon(xPoints, yPoints, vertices.size());
        inOrder.verify(mockGc).restore();
    }

    @Test
    void visit_textShape_shouldDrawAndScaleTextCorrectly() {
        String textContent = "Hello";
        Rect targetBounds = new Rect(10, 10, 100, 20); // Text should fit into this
        double baseFontSize = 12;
        String fontName = "Arial";
        TextShape textShape = new TextShape(textContent, targetBounds.getTopLeft(), baseFontSize, fontName, ColorData.BLACK);
        textShape.resize(targetBounds); // Ensure drawingBounds are set to targetBounds
        textShape.setRotation(15);
        
        // Mocking text measurement is complex. We'll focus on GC calls.
        // Assume naturalWidth and naturalHeight are positive.
        // The renderer calculates scaleX and scaleY.

        textShape.accept(renderer);

        InOrder inOrder = inOrder(mockGc);
        inOrder.verify(mockGc).save();
        // Transform: translate to center, rotate, scale
        Point2D center = targetBounds.getCenter();
        inOrder.verify(mockGc).translate(center.getX(), center.getY());
        inOrder.verify(mockGc).rotate(15);
        // Flip scale values will be 1.0 if not flipped
        verify(mockGc, never()).rotate(0);
        inOrder.verify(mockGc).scale(anyDouble(), anyDouble()); // Overall scale to fit bounds

        // Text drawing properties
        inOrder.verify(mockGc).setFont(Font.font(fontName, baseFontSize));
        inOrder.verify(mockGc).setFill(JavaFXShapeRenderer.convertModelToFxColor(ColorData.BLACK));
        inOrder.verify(mockGc).setTextAlign(TextAlignment.LEFT);
        inOrder.verify(mockGc).setTextBaseline(VPos.TOP);
        inOrder.verify(mockGc).fillText(eq(textContent), anyDouble(), anyDouble());
        inOrder.verify(mockGc).restore();
    }
    
    @Test
    void visit_textShape_emptyText_shouldNotDraw() {
        TextShape textShape = new TextShape("", new Point2D(0,0), 12, "Arial", ColorData.BLACK);
        textShape.accept(renderer);
        verify(mockGc, never()).save(); // Should return early
        verify(mockGc, never()).fillText(anyString(), anyDouble(), anyDouble());
    }
    
    @Test
    void visit_textShape_invalidBoundsOrSize_shouldAttemptFallbackDrawing() {
        TextShape textShape = new TextShape("Test", new Point2D(0,0), 1, "Arial", ColorData.BLACK); // Invalid font size
        textShape.resize(new Rect(0,0,0,0)); // Invalid bounds
        
        textShape.accept(renderer);
        
        // Verify fallback drawing attempts
        verify(mockGc).save();
        verify(mockGc).setFont(Font.font("Arial", 1)); // Fallback font size
        verify(mockGc).fillText(eq("Test"), anyDouble(), anyDouble());
        verify(mockGc).restore();
    }
    
    @Test
    void visit_textShape_withFlip_shouldApplyScaleFlip() {
        TextShape textShape = new TextShape("Flip", new Point2D(0,0), 12, "Arial", ColorData.BLACK);
        textShape.resize(new Rect(0,0,50,20));
        textShape.reflectHorizontal(); // hFlip = true
        textShape.reflectVertical();   // vFlip = true
        
        textShape.accept(renderer);
        
        InOrder inOrder = inOrder(mockGc);
        inOrder.verify(mockGc).save();
        inOrder.verify(mockGc).translate(anyDouble(), anyDouble());
        verify(mockGc, never()).rotate(0);
        inOrder.verify(mockGc).scale(-1.0, -1.0); // Flip transform
        inOrder.verify(mockGc).scale(anyDouble(), anyDouble()); // Overall scale
        inOrder.verify(mockGc).fillText(eq("Flip"), anyDouble(), anyDouble());
        inOrder.verify(mockGc).restore();
    }


    @Test
    void visit_groupShape_shouldApplyGroupTransformAndVisitChildren() {
        Shape mockChild1 = mock(LineSegment.class);
        Shape mockChild2 = mock(RectangleShape.class);
        List<Shape> children = Arrays.asList(mockChild1, mockChild2);
        GroupShape group = new GroupShape(children);
        group.setRotation(30); // Group has rotation

        // Bounds are needed for the GroupShape to calculate its center for rotation
        // The real GroupShape.getBounds() iterates children.getRotatedBounds().
        // So, the children's getRotatedBounds() must be stubbed.
        when(mockChild1.getRotatedBounds()).thenReturn(new Rect(0, 0, 10, 10));
        when(mockChild2.getRotatedBounds()).thenReturn(new Rect(10, 10, 10, 10));
        // This implies group bounds would be Rect(0,0,20,20) and center (10,10)

        group.accept(renderer);

        InOrder inOrder = inOrder(mockGc, mockChild1, mockChild2); // mockGc deve essere prima se le sue chiamate sono prima
        
        inOrder.verify(mockGc).save();
        
        // Verifica la chiamata a transform in modo più generico per ora
        inOrder.verify(mockGc).transform(anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble());
        
        inOrder.verify(mockChild1).accept(renderer); // Chiamata su mockChild1
        inOrder.verify(mockChild2).accept(renderer); // Chiamata su mockChild2
        
        inOrder.verify(mockGc).restore();
    }
    
    @Test
    void visit_selectedGroupShape_shouldDrawSelectionIndicatorForGroupBounds() {
        Shape mockChild = mock(LineSegment.class);
        GroupShape group = new GroupShape(Arrays.asList(mockChild));
        Rect groupBounds = new Rect(5,5,20,20); // Assume these are the calculated bounds
        // Need to use a spy or stub getBounds() if GroupShape calculates it dynamically and children are pure mocks
        GroupShape spiedGroup = spy(group);
        doReturn(groupBounds).when(spiedGroup).getBounds(); // Stub getBounds for the spied group
        
        renderer.setSelectedShapeForRendering(spiedGroup);
        
        spiedGroup.accept(renderer);

        // After group's children are drawn and its main gc.restore()
        verify(mockGc, times(2)).save(); // Once for group transform, once for its selection indicator
        verify(mockGc, times(2)).restore();
        verify(mockGc).setLineDashes(4,4);
        verify(mockGc).strokeRect(eq(groupBounds.getX() - 2.0), eq(groupBounds.getY() - 2.0), 
                                  eq(groupBounds.getWidth() + 4.0), eq(groupBounds.getHeight() + 4.0));
    }
}

            