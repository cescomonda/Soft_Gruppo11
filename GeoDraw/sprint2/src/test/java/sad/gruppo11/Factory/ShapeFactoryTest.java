
package sad.gruppo11.Factory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sad.gruppo11.Model.*;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Point2D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ShapeFactoryTest {
    private ShapeFactory factory;
    private Point2D p1, p2;
    private ColorData stroke, fill;

    @BeforeEach
    void setUp() {
        factory = new ShapeFactory();
        p1 = new Point2D(10, 10);
        p2 = new Point2D(110, 60); // For rect/ellipse: w=100, h=50
        stroke = ColorData.BLACK;
        fill = ColorData.TRANSPARENT;
    }

    @Test
    void createLineSegment() {
        Shape shape = factory.createShape("LineTool", p1, p2, stroke, fill);
        assertTrue(shape instanceof LineSegment);
        LineSegment line = (LineSegment) shape;
        assertEquals(p1, line.getStartPoint());
        assertEquals(p2, line.getEndPoint());
        assertEquals(stroke, line.getStrokeColor());
    }
    
    @Test
    void createLineSegmentDegenerate() {
        Shape shape = factory.createShape("LineTool", p1, new Point2D(p1.getX()+0.001, p1.getY()), stroke, fill);
        assertNull(shape, "Degenerate line should not be created");
    }

    @Test
    void createRectangleShape() {
        Shape shape = factory.createShape("RectangleTool", p1, p2, stroke, fill);
        assertTrue(shape instanceof RectangleShape);
        RectangleShape rect = (RectangleShape) shape;
        assertEquals(10, rect.getBounds().getX());
        assertEquals(10, rect.getBounds().getY());
        assertEquals(100, rect.getBounds().getWidth());
        assertEquals(50, rect.getBounds().getHeight());
        assertEquals(stroke, rect.getStrokeColor());
        assertEquals(fill, rect.getFillColor());
    }
    
    @Test
    void createRectangleShapeDegenerate() {
        Shape shape = factory.createShape("RectangleTool", p1, new Point2D(p1.getX()+0.001, p1.getY()+50), stroke, fill); // Zero width
        assertNull(shape, "Degenerate rectangle (zero width) should not be created");
        shape = factory.createShape("RectangleTool", p1, new Point2D(p1.getX()+50, p1.getY()+0.001), stroke, fill); // Zero height
        assertNull(shape, "Degenerate rectangle (zero height) should not be created");
    }

    @Test
    void createEllipseShape() {
        Shape shape = factory.createShape("EllipseTool", p1, p2, stroke, fill);
        assertTrue(shape instanceof EllipseShape);
        EllipseShape ellipse = (EllipseShape) shape;
        assertEquals(10, ellipse.getBounds().getX());
        assertEquals(10, ellipse.getBounds().getY());
        assertEquals(100, ellipse.getBounds().getWidth());
        assertEquals(50, ellipse.getBounds().getHeight());
        assertEquals(stroke, ellipse.getStrokeColor());
        assertEquals(fill, ellipse.getFillColor());
    }

    @Test
    void createPolygonShape() {
        Map<String, Object> params = new HashMap<>();
        List<Point2D> vertices = new ArrayList<>();
        vertices.add(new Point2D(0,0));
        vertices.add(new Point2D(10,0));
        vertices.add(new Point2D(5,10));
        params.put("vertices", vertices);

        Shape shape = factory.createShape("PolygonTool", null, null, stroke, fill, params);
        assertTrue(shape instanceof PolygonShape);
        PolygonShape poly = (PolygonShape) shape;
        assertEquals(3, poly.getVertices().size());
        assertEquals(stroke, poly.getStrokeColor());
        assertEquals(fill, poly.getFillColor());
    }
    
    @Test
    void createPolygonShapeNotEnoughVertices() {
        Map<String, Object> params = new HashMap<>();
        List<Point2D> vertices = new ArrayList<>();
        vertices.add(new Point2D(0,0));
        vertices.add(new Point2D(10,0));
        params.put("vertices", vertices); // Only 2 vertices
        Shape shape = factory.createShape("PolygonTool", null, null, stroke, fill, params);
        assertNull(shape, "Polygon with less than 3 vertices should not be created");
    }
    
    @Test
    void createPolygonShapeMissingParams() {
        Shape shape = factory.createShape("PolygonTool", null, null, stroke, fill, null);
        assertNull(shape);
        shape = factory.createShape("PolygonTool", null, null, stroke, fill, new HashMap<>()); // Empty map
        assertNull(shape);
    }

    @Test
    void createTextShape() {
        Map<String, Object> params = new HashMap<>();
        params.put("text", "Test");
        params.put("fontSize", 16.0);
        params.put("fontName", "SansSerif");
        // params.put("position", p1); // ShapeFactory uses p1 if "position" not in params

        Shape shape = factory.createShape("TextTool", p1, null, stroke, fill, params);
        assertTrue(shape instanceof TextShape);
        TextShape text = (TextShape) shape;
        assertEquals("Test", text.getText());
        assertEquals(16.0, text.getBaseFontSize());
        assertEquals("SansSerif", text.getFontName());
        assertEquals(p1, text.getPosition()); // Check position
        assertEquals(stroke, text.getStrokeColor()); // Text color is stroke
    }
    
    @Test
    void createTextShapeWithPositionInParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("text", "Test");
        params.put("fontSize", 16.0);
        params.put("fontName", "SansSerif");
        Point2D textPosParam = new Point2D(100,100);
        params.put("position", textPosParam);

        Shape shape = factory.createShape("TextTool", p1, null, stroke, fill, params); // p1 is ignored
        assertTrue(shape instanceof TextShape);
        TextShape text = (TextShape) shape;
        assertEquals(textPosParam, text.getPosition());
    }
    
    @Test
    void createTextShapeMissingParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("text", "Test");
        // Missing fontSize or fontName
        Shape shape = factory.createShape("TextTool", p1, null, stroke, fill, params);
        assertNull(shape);
    }

    @Test
    void createUnknownShape() {
        Shape shape = factory.createShape("UnknownTool", p1, p2, stroke, fill);
        assertNull(shape);
    }

    @Test
    void createShapeNullToolName() {
        assertThrows(NullPointerException.class, () -> {
            factory.createShape(null, p1, p2, stroke, fill);
        });
    }
    
    @Test
    void createShapeNullColors() {
         assertThrows(NullPointerException.class, () -> {
            factory.createShape("LineTool", p1, p2, null, fill);
        });
         assertThrows(NullPointerException.class, () -> {
            factory.createShape("LineTool", p1, p2, stroke, null);
        });
    }
}
