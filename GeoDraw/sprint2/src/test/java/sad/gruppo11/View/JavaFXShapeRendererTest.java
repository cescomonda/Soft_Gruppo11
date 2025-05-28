
package sad.gruppo11.View;

import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sad.gruppo11.Model.*;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Rect;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


public class JavaFXShapeRendererTest {
    @Mock private GraphicsContext mockGc;
    private JavaFXShapeRenderer renderer;

    // Required for things like new Text().getLayoutBounds()
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
        // Platform.exit(); // Usually not needed for unit tests
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        renderer = new JavaFXShapeRenderer(mockGc);
    }

    @Test
    void convertModelToFxColorTest() {
        assertEquals(Color.rgb(255,0,0,1.0), JavaFXShapeRenderer.convertModelToFxColor(ColorData.RED));
        assertEquals(Color.TRANSPARENT, JavaFXShapeRenderer.convertModelToFxColor(ColorData.TRANSPARENT));
        assertEquals(Color.TRANSPARENT, JavaFXShapeRenderer.convertModelToFxColor(null));
    }

    @Test
    void visitRectangleShape() {
        RectangleShape rectShape = new RectangleShape(new Rect(10,20,100,50), ColorData.BLACK, ColorData.BLUE);
        rectShape.setRotation(45);
        
        renderer.visit(rectShape);

        verify(mockGc).save();
        verify(mockGc).transform(anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble()); // Rotation
        verify(mockGc).setFill(JavaFXShapeRenderer.convertModelToFxColor(ColorData.BLUE));
        verify(mockGc).fillRect(10,20,100,50);
        verify(mockGc).setStroke(JavaFXShapeRenderer.convertModelToFxColor(ColorData.BLACK));
        verify(mockGc).setLineWidth(anyDouble());
        verify(mockGc).strokeRect(10,20,100,50);
        verify(mockGc).restore();
    }

    @Test
    void visitEllipseShape() {
        EllipseShape ellipseShape = new EllipseShape(new Rect(10,20,100,50), ColorData.GREEN, ColorData.YELLOW);
        
        renderer.visit(ellipseShape);

        verify(mockGc).save();
        // No rotation here, so no transform call expected before fill/stroke
        verify(mockGc).setFill(JavaFXShapeRenderer.convertModelToFxColor(ColorData.YELLOW));
        verify(mockGc).fillOval(10,20,100,50);
        verify(mockGc).setStroke(JavaFXShapeRenderer.convertModelToFxColor(ColorData.GREEN));
        verify(mockGc).strokeOval(10,20,100,50);
        verify(mockGc).restore();
    }
    
    @Test
    void visitLineSegment() {
        Point2D start = new Point2D(0,0);
        Point2D end = new Point2D(30,40);
        LineSegment line = new LineSegment(start, end, ColorData.RED);
        line.setRotation(30);
        
        renderer.visit(line);

        verify(mockGc).save();
        verify(mockGc).transform(anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble()); // Rotation
        verify(mockGc).setStroke(JavaFXShapeRenderer.convertModelToFxColor(ColorData.RED));
        verify(mockGc).setLineCap(StrokeLineCap.ROUND);
        verify(mockGc).strokeLine(0,0,30,40);
        verify(mockGc).restore();
    }

    @Test
    void visitPolygonShape() {
        List<Point2D> vertices = Arrays.asList(new Point2D(0,0), new Point2D(10,0), new Point2D(5,10));
        PolygonShape poly = new PolygonShape(vertices, ColorData.BLUE, ColorData.BLACK);
        
        renderer.visit(poly);

        double[] expectedX = {0,10,5};
        double[] expectedY = {0,0,10};

        verify(mockGc).save();
        verify(mockGc).setFill(JavaFXShapeRenderer.convertModelToFxColor(ColorData.BLACK));
        verify(mockGc).fillPolygon(eq(expectedX), eq(expectedY), eq(3));
        verify(mockGc).setStroke(JavaFXShapeRenderer.convertModelToFxColor(ColorData.BLUE));
        verify(mockGc).strokePolygon(eq(expectedX), eq(expectedY), eq(3));
        verify(mockGc).restore();
    }
    
    @Test
    void visitTextShape() {
        TextShape textShape = new TextShape("Hi", new Point2D(5,25), 12, "Arial", ColorData.BLACK);
        // Default drawingBounds based on "Hi", 12pt Arial. E.g. Rect(5,25, width=W, height=H)
        // Let's assume W=15, H=15 for simplicity for this test setup, actual values depend on font metrics.
        textShape.resize(new Rect(5,25, 30, 30)); // Force drawing bounds for predictability
        textShape.setRotation(0);

        renderer.visit(textShape);

        verify(mockGc).save();
        // Verify translate, scale, rotate (if any) are called based on TextShape's state.
        // For 0 rotation, no gc.rotate() before fillText.
        // With scale:
        verify(mockGc).translate(eq(5.0 + 15.0), eq(25.0 + 15.0)); // Translate to center of drawingBounds (20,40)
        // Natural width/height of "Hi" at 12pt needs to be known for exact scale values.
        // Let's assume natural W_n=10, H_n=10. ScaleX = 30/10=3, ScaleY = 30/10=3
        verify(mockGc).scale(anyDouble(), anyDouble()); // e.g. scale(3,3)

        ArgumentCaptor<Font> fontCaptor = ArgumentCaptor.forClass(Font.class);
        verify(mockGc).setFont(fontCaptor.capture());
        assertEquals("Arial", fontCaptor.getValue().getFamily());
        assertEquals(12, fontCaptor.getValue().getSize()); // Base font size

        verify(mockGc).setFill(Color.BLACK);
        verify(mockGc).setTextAlign(TextAlignment.LEFT); // As per renderer
        // verify(mockGc).setTextBaseline(VPos.TOP); // As per renderer

        // fillText coordinates depend on naturalLayoutBounds of "Hi"
        // drawX = -naturalWidth/2 - naturalLayoutBounds.getMinX()
        // finalDrawY = -naturalHeight/2 - naturalLayoutBounds.getMinY() + verticalOffset
        verify(mockGc).fillText(eq("Hi"), anyDouble(), anyDouble());
        verify(mockGc).restore();
    }
    
    @Test
    void visitTextShape_withRotation() {
        TextShape textShape = new TextShape("Rot", new Point2D(10,10), 10, "Verdana", ColorData.RED);
        textShape.resize(new Rect(10,10,50,20)); // DrawingBounds
        textShape.setRotation(30);

        renderer.visit(textShape);
        
        verify(mockGc).save();
        verify(mockGc).translate(eq(10.0 + 25.0), eq(10.0 + 10.0)); // Center of drawingBounds (35,20)
        verify(mockGc).rotate(30);
        verify(mockGc).scale(anyDouble(), anyDouble());
        verify(mockGc).fillText(eq("Rot"), anyDouble(), anyDouble());
        verify(mockGc).restore();
    }
    
    @Test
    void visitTextShape_emptyText_noDraw() {
        TextShape textShape = new TextShape("", new Point2D(5,25), 12, "Arial", ColorData.BLACK);
        renderer.visit(textShape);
        verify(mockGc, never()).fillText(anyString(), anyDouble(), anyDouble());
        // Should not throw errors. Might draw selection indicator if selected.
    }
    
    @Test
    void visitTextShape_invalidBounds_fallbackDraw() {
        TextShape textShape = new TextShape("Fallback", new Point2D(5,5), 12, "Arial", ColorData.BLACK);
        textShape.resize(new Rect(5,5,0,0)); // Invalid bounds (width/height zero)

        renderer.visit(textShape);
        
        // Check for fallback drawing logic
        verify(mockGc).save();
        // fillText at original position (5,5) without scaling, possibly with rotation if set
        verify(mockGc).fillText(eq("Fallback"), eq(5.0), eq(5.0));
        verify(mockGc).restore();
    }


    @Test
    void selectionIndicatorDrawn() {
        RectangleShape rectShape = new RectangleShape(new Rect(10,20,100,50), ColorData.BLACK, ColorData.BLUE);
        renderer.setSelectedShapeForRendering(rectShape); // Mark as selected
        
        renderer.visit(rectShape);
        
        // After normal drawing, verify selection indicator drawing
        // This involves another save/restore, setStroke, setLineDashes, strokeRect
        verify(mockGc, times(2)).save(); // Once for shape, once for indicator
        verify(mockGc, times(2)).restore();
        verify(mockGc).setStroke(Color.CORNFLOWERBLUE);
        verify(mockGc).setLineDashes(4,4);
        verify(mockGc).strokeRect(eq(10.0-2), eq(20.0-2), eq(100.0+4), eq(50.0+4));
    }
    
    @Test
    void selectionIndicatorForLineDrawn() {
        LineSegment line = new LineSegment(new Point2D(10,10), new Point2D(50,50), ColorData.BLACK);
        renderer.setSelectedShapeForRendering(line);
        
        renderer.visit(line);
        
        verify(mockGc, times(2)).save();
        verify(mockGc, times(2)).restore();
        verify(mockGc).setStroke(Color.CORNFLOWERBLUE);
        verify(mockGc).setLineDashes(4,4);
        // For line (10,10)-(50,50): minX=10, minY=10, width=40, height=40
        // Indicator rect: (10-3, 10-3) w=40+6, h=40+6 => (7,7,46,46)
        verify(mockGc).strokeRect(eq(7.0), eq(7.0), eq(46.0), eq(46.0));
    }
}
