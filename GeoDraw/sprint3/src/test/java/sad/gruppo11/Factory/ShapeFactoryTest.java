
package sad.gruppo11.Factory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sad.gruppo11.Model.*;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Rect;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;


import static org.assertj.core.api.Assertions.*;

public class ShapeFactoryTest {
    private ShapeFactory factory;
    private Point2D p1, p2;
    private ColorData strokeColor, fillColor;

    @BeforeEach
    void setUp() {
        factory = new ShapeFactory();
        p1 = new Point2D(10, 20);
        p2 = new Point2D(40, 60);
        strokeColor = ColorData.RED;
        fillColor = ColorData.BLUE;
    }

    @Test
    void createShapeShouldThrowForNullRequiredParameters() {
        assertThatNullPointerException().isThrownBy(() -> factory.createShape(null, p1, p2, strokeColor, fillColor, null))
            .withMessageContaining("Tool name cannot be null");
        assertThatNullPointerException().isThrownBy(() -> factory.createShape("LineTool", p1, p2, null, fillColor, null))
            .withMessageContaining("Stroke color cannot be null");
        assertThatNullPointerException().isThrownBy(() -> factory.createShape("LineTool", p1, p2, strokeColor, null, null))
            .withMessageContaining("Fill color cannot be null");
    }

    @Test
    void createLineSegmentShouldReturnCorrectShape() {
        Shape shape = factory.createShape("LineTool", p1, p2, strokeColor, fillColor);
        assertThat(shape).isInstanceOf(LineSegment.class);
        LineSegment line = (LineSegment) shape;
        assertThat(line.getStartPoint()).isEqualTo(p1);
        assertThat(line.getEndPoint()).isEqualTo(p2);
        assertThat(line.getStrokeColor()).isEqualTo(strokeColor);
    }
    
    @Test
    void createLineSegmentWithAliasShouldReturnCorrectShape() {
        Shape shape = factory.createShape("LineSegment", p1, p2, strokeColor, fillColor);
        assertThat(shape).isInstanceOf(LineSegment.class);
    }

    @Test
    void createLineSegmentShouldReturnNullIfPointsTooClose() {
        Point2D closeP1 = new Point2D(10, 10);
        Point2D closeP2 = new Point2D(10.001, 10.001); // Distance less than 1e-2
        Shape shape = factory.createShape("LineTool", closeP1, closeP2, strokeColor, fillColor);
        assertThat(shape).isNull();
    }
    
    @Test
    void createLineSegmentShouldReturnNullIfPointsAreNull() {
        Shape shape = factory.createShape("LineTool", null, p2, strokeColor, fillColor);
        assertThat(shape).isNull();
        shape = factory.createShape("LineTool", p1, null, strokeColor, fillColor);
        assertThat(shape).isNull();
    }

    @Test
    void createRectangleShapeShouldReturnCorrectShape() {
        Shape shape = factory.createShape("RectangleTool", p1, p2, strokeColor, fillColor);
        assertThat(shape).isInstanceOf(RectangleShape.class);
        RectangleShape rect = (RectangleShape) shape;
        Rect expectedBounds = new Rect(
            Math.min(p1.getX(), p2.getX()), Math.min(p1.getY(), p2.getY()),
            Math.abs(p1.getX() - p2.getX()), Math.abs(p1.getY() - p2.getY())
        );
        assertThat(rect.getBounds()).isEqualTo(expectedBounds);
        assertThat(rect.getStrokeColor()).isEqualTo(strokeColor);
        assertThat(rect.getFillColor()).isEqualTo(fillColor);
    }
    
    @Test
    void createRectangleShapeWithAliasShouldReturnCorrectShape() {
        Shape shape = factory.createShape("RectangleShape", p1, p2, strokeColor, fillColor);
        assertThat(shape).isInstanceOf(RectangleShape.class);
    }


    @Test
    void createRectangleShapeShouldReturnNullIfTooSmall() {
        Point2D smallP1 = new Point2D(10, 10);
        Point2D smallP2_width = new Point2D(10.001, 20);
        Point2D smallP2_height = new Point2D(20, 10.001);
        assertThat(factory.createShape("RectangleTool", smallP1, smallP2_width, strokeColor, fillColor)).isNull();
        assertThat(factory.createShape("RectangleTool", smallP1, smallP2_height, strokeColor, fillColor)).isNull();
    }
    
    @Test
    void createRectangleShapeShouldReturnNullIfPointsAreNull() {
         Shape shape = factory.createShape("RectangleTool", null, p2, strokeColor, fillColor);
        assertThat(shape).isNull();
        shape = factory.createShape("RectangleTool", p1, null, strokeColor, fillColor);
        assertThat(shape).isNull();
    }


    @Test
    void createEllipseShapeShouldReturnCorrectShape() {
        Shape shape = factory.createShape("EllipseTool", p1, p2, strokeColor, fillColor);
        assertThat(shape).isInstanceOf(EllipseShape.class);
        EllipseShape ellipse = (EllipseShape) shape;
        Rect expectedBounds = new Rect(
            Math.min(p1.getX(), p2.getX()), Math.min(p1.getY(), p2.getY()),
            Math.abs(p1.getX() - p2.getX()), Math.abs(p1.getY() - p2.getY())
        );
        assertThat(ellipse.getBounds()).isEqualTo(expectedBounds);
        assertThat(ellipse.getStrokeColor()).isEqualTo(strokeColor);
        assertThat(ellipse.getFillColor()).isEqualTo(fillColor);
    }
    
    @Test
    void createEllipseShapeWithAliasShouldReturnCorrectShape() {
        Shape shape = factory.createShape("EllipseShape", p1, p2, strokeColor, fillColor);
        assertThat(shape).isInstanceOf(EllipseShape.class);
    }

    @Test
    void createEllipseShapeShouldReturnNullIfTooSmall() {
        Point2D smallP1 = new Point2D(10, 10);
        Point2D smallP2_width = new Point2D(10.001, 20);
        Point2D smallP2_height = new Point2D(20, 10.001);
        assertThat(factory.createShape("EllipseTool", smallP1, smallP2_width, strokeColor, fillColor)).isNull();
        assertThat(factory.createShape("EllipseTool", smallP1, smallP2_height, strokeColor, fillColor)).isNull();
    }
    
     @Test
    void createEllipseShapeShouldReturnNullIfPointsAreNull() {
         Shape shape = factory.createShape("EllipseTool", null, p2, strokeColor, fillColor);
        assertThat(shape).isNull();
        shape = factory.createShape("EllipseTool", p1, null, strokeColor, fillColor);
        assertThat(shape).isNull();
    }

    @Test
    void createPolygonShapeShouldReturnCorrectShape() {
        List<Point2D> vertices = Arrays.asList(new Point2D(0,0), new Point2D(10,0), new Point2D(5,10));
        Map<String, Object> params = new HashMap<>();
        params.put("vertices", vertices);
        Shape shape = factory.createShape("PolygonTool", null, null, strokeColor, fillColor, params);
        assertThat(shape).isInstanceOf(PolygonShape.class);
        PolygonShape poly = (PolygonShape) shape;
        assertThat(poly.getVertices()).isEqualTo(vertices);
        assertThat(poly.getStrokeColor()).isEqualTo(strokeColor);
        assertThat(poly.getFillColor()).isEqualTo(fillColor);
    }
    
    @Test
    void createPolygonShapeWithAliasShouldReturnCorrectShape() {
        List<Point2D> vertices = Arrays.asList(new Point2D(0,0), new Point2D(10,0), new Point2D(5,10));
        Map<String, Object> params = new HashMap<>();
        params.put("vertices", vertices);
        Shape shape = factory.createShape("PolygonShape", null, null, strokeColor, fillColor, params);
        assertThat(shape).isInstanceOf(PolygonShape.class);
    }

    @Test
    void createPolygonShapeShouldReturnNullIfVerticesParamMissingOrInvalid() {
        assertThat(factory.createShape("PolygonTool", null, null, strokeColor, fillColor, null)).isNull();
        
        Map<String, Object> paramsNoVertices = new HashMap<>();
        assertThat(factory.createShape("PolygonTool", null, null, strokeColor, fillColor, paramsNoVertices)).isNull();

        Map<String, Object> paramsWrongType = new HashMap<>();
        paramsWrongType.put("vertices", "not a list");
        assertThat(factory.createShape("PolygonTool", null, null, strokeColor, fillColor, paramsWrongType)).isNull();
        
        Map<String, Object> paramsTooFewVertices = new HashMap<>();
        paramsTooFewVertices.put("vertices", Arrays.asList(new Point2D(0,0), new Point2D(1,1)));
        assertThat(factory.createShape("PolygonTool", null, null, strokeColor, fillColor, paramsTooFewVertices)).isNull();

        Map<String, Object> paramsNullVerticesList = new HashMap<>();
        paramsNullVerticesList.put("vertices", null);
        assertThat(factory.createShape("PolygonTool", null, null, strokeColor, fillColor, paramsNullVerticesList)).isNull();
    }

    @Test
    void createTextShapeShouldReturnCorrectShape() {
        Map<String, Object> params = new HashMap<>();
        params.put("text", "Hello");
        params.put("fontSize", 12.0);
        params.put("fontName", "Arial");
        params.put("position", p1); // Explicit position for text

        Shape shape = factory.createShape("TextTool", null, null, strokeColor, fillColor, params);
        assertThat(shape).isInstanceOf(TextShape.class);
        TextShape text = (TextShape) shape;
        assertThat(text.getText()).isEqualTo("Hello");
        assertThat(text.getBaseFontSize()).isEqualTo(12.0);
        assertThat(text.getFontName()).isEqualTo("Arial");
        assertThat(text.getDrawingBounds().getTopLeft()).isEqualTo(p1);
        assertThat(text.getStrokeColor()).isEqualTo(strokeColor); // Text color is stroke color
    }
    
    @Test
    void createTextShapeWithAliasShouldReturnCorrectShape() {
         Map<String, Object> params = new HashMap<>();
        params.put("text", "Hello");
        params.put("fontSize", 12.0);
        params.put("fontName", "Arial");
        params.put("position", p1);

        Shape shape = factory.createShape("TextShape", null, null, strokeColor, fillColor, params);
        assertThat(shape).isInstanceOf(TextShape.class);
    }

    @Test
    void createTextShapeWithP1AsPositionFallback() {
        Map<String, Object> params = new HashMap<>();
        params.put("text", "Test");
        params.put("fontSize", 10.0);
        params.put("fontName", "SansSerif");
        // "position" not in params, p1 should be used as fallback
        Shape shape = factory.createShape("TextTool", p1, null, strokeColor, fillColor, params);
        assertThat(shape).isInstanceOf(TextShape.class);
        TextShape text = (TextShape) shape;
        assertThat(text.getDrawingBounds().getTopLeft()).isEqualTo(p1);
    }

    @Test
    void createTextShapeShouldReturnNullIfParamsMissingOrInvalid() {
        Map<String, Object> params = new HashMap<>();
        // Missing text
        params.put("fontSize", 12.0);
        params.put("fontName", "Arial");
        params.put("position", p1);
        assertThat(factory.createShape("TextTool", null, null, strokeColor, fillColor, params)).isNull();

        // Missing fontSize
        params.clear();
        params.put("text", "Hello");
        params.put("fontName", "Arial");
        params.put("position", p1);
        assertThat(factory.createShape("TextTool", null, null, strokeColor, fillColor, params)).isNull();
        
        // Empty text
        params.clear();
        params.put("text", "");
        params.put("fontSize", 12.0);
        params.put("fontName", "Arial");
        params.put("position", p1);
        assertThat(factory.createShape("TextTool", null, null, strokeColor, fillColor, params)).isNull();

        // Invalid fontSize
        params.clear();
        params.put("text", "Hi");
        params.put("fontSize", 0.0);
        params.put("fontName", "Arial");
        params.put("position", p1);
        assertThat(factory.createShape("TextTool", null, null, strokeColor, fillColor, params)).isNull();
        
        // Null optionalParams
        assertThat(factory.createShape("TextTool", p1, null, strokeColor, fillColor, null)).isNull();

        // Null position (p1 is also null)
        params.clear();
        params.put("text", "Hi");
        params.put("fontSize", 12.0);
        params.put("fontName", "Arial");
        assertThat(factory.createShape("TextTool", null, null, strokeColor, fillColor, params)).isNull();

    }
    
    @Test
    void createTextShapeWithExplicitTextColorInParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("text", "Colored Text");
        params.put("fontSize", 14.0);
        params.put("fontName", "Verdana");
        params.put("position", p1);
        ColorData explicitTextColor = ColorData.GREEN;
        params.put("textColor", explicitTextColor);

        // Stroke color provided to factory is RED, but explicit textColor is GREEN
        Shape shape = factory.createShape("TextTool", null, null, strokeColor, fillColor, params);
        assertThat(shape).isInstanceOf(TextShape.class);
        TextShape text = (TextShape) shape;
        assertThat(text.getStrokeColor()).isEqualTo(explicitTextColor); // Should use textColor from params
    }


    @Test
    void createShapeWithUnknownToolNameShouldReturnNull() {
        Shape shape = factory.createShape("UnknownTool", p1, p2, strokeColor, fillColor);
        assertThat(shape).isNull();
    }
    
    @Test
    void createShapeWithoutOptionalParams() {
         Shape shape = factory.createShape("LineTool", p1, p2, strokeColor, fillColor); // Uses overloaded method
         assertThat(shape).isInstanceOf(LineSegment.class);
    }
}
