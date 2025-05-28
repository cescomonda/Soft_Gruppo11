
package sad.gruppo11.Model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Rect;
import sad.gruppo11.Model.geometry.Vector2D;
import sad.gruppo11.View.ShapeVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

// Test common Shape behavior and specifics of implementations
public class ShapeImplementationsTest {

    private RectangleShape rectShape;
    private EllipseShape ellipseShape;
    private LineSegment lineSegment;
    private PolygonShape polygonShape;
    private TextShape textShape;

    private ShapeVisitor mockVisitor;

    @BeforeEach
    void setUp() {
        Rect bounds = new Rect(10, 20, 100, 50);
        rectShape = new RectangleShape(bounds, ColorData.RED, ColorData.BLUE);
        ellipseShape = new EllipseShape(bounds, ColorData.GREEN, ColorData.YELLOW);
        
        Point2D p1 = new Point2D(0, 0);
        Point2D p2 = new Point2D(10, 10);
        lineSegment = new LineSegment(p1, p2, ColorData.BLACK);

        List<Point2D> vertices = new ArrayList<>();
        vertices.add(new Point2D(0,0));
        vertices.add(new Point2D(10,0));
        vertices.add(new Point2D(5,10));
        polygonShape = new PolygonShape(vertices, ColorData.BLUE, ColorData.YELLOW);
        
        textShape = new TextShape("Hello", new Point2D(5, 5), 12, "Arial", ColorData.BLACK);
        // Default drawingBounds for textShape: Rect{topLeft=Point2D{x=5.0, y=5.0}, width=36.00, height=12.00} (approx)

        mockVisitor = Mockito.mock(ShapeVisitor.class);
    }

    // Common Shape Tests (applied to one type, e.g., RectangleShape, extensible)
    @Test
    void idIsNotNull() {
        assertNotNull(rectShape.getId());
    }

    @Test
    void moveShape() {
        Rect initialBounds = rectShape.getBounds();
        Vector2D moveVec = new Vector2D(5, -5);
        rectShape.move(moveVec);
        Rect newBounds = rectShape.getBounds();

        assertEquals(initialBounds.getX() + 5, newBounds.getX(), 0.001);
        assertEquals(initialBounds.getY() - 5, newBounds.getY(), 0.001);
        assertEquals(initialBounds.getWidth(), newBounds.getWidth(), 0.001);
        assertEquals(initialBounds.getHeight(), newBounds.getHeight(), 0.001);
    }
    
    @Test
    void moveTextShape() {
        Point2D initialPos = textShape.getPosition(); // Top-left of drawingBounds
        Vector2D moveVec = new Vector2D(3, 7);
        textShape.move(moveVec);
        Point2D newPos = textShape.getPosition();
        
        assertEquals(initialPos.getX() + 3, newPos.getX(), 0.001);
        assertEquals(initialPos.getY() + 7, newPos.getY(), 0.001);
    }

    @Test
    void resizeShape() {
        Rect newDesignatedBounds = new Rect(0, 0, 200, 150);
        rectShape.resize(newDesignatedBounds);
        assertEquals(newDesignatedBounds, rectShape.getBounds());
    }
    
    @Test
    void resizeTextShape() {
        Rect newDrawingBounds = new Rect(0,0,50,20);
        textShape.resize(newDrawingBounds);
        assertEquals(newDrawingBounds, textShape.getDrawingBounds());
        assertEquals(newDrawingBounds, textShape.getBounds()); // getBounds should return drawingBounds
    }

    @Test
    void colorOperations() {
        rectShape.setStrokeColor(ColorData.GREEN);
        assertEquals(ColorData.GREEN, rectShape.getStrokeColor());
        rectShape.setFillColor(ColorData.YELLOW);
        assertEquals(ColorData.YELLOW, rectShape.getFillColor());
    }
    
    @Test
    void textShapeColorOperations() {
        textShape.setStrokeColor(ColorData.BLUE); // Text color is treated as stroke
        assertEquals(ColorData.BLUE, textShape.getStrokeColor());
        textShape.setFillColor(ColorData.RED); // Should be no-op for fill, returns transparent
        assertEquals(ColorData.TRANSPARENT, textShape.getFillColor());
    }

    @Test
    void lineSegmentColorOperations() {
        lineSegment.setStrokeColor(ColorData.GREEN);
        assertEquals(ColorData.GREEN, lineSegment.getStrokeColor());
        lineSegment.setFillColor(ColorData.YELLOW); // No-op
        assertEquals(ColorData.TRANSPARENT, lineSegment.getFillColor());
    }
    
    @Test
    void rotation() {
        rectShape.setRotation(45.0);
        assertEquals(45.0, rectShape.getRotation(), 0.001);
        rectShape.setRotation(405.0); // Should be modulo 360
        assertEquals(45.0, rectShape.getRotation(), 0.001);
    }

    // Specific contains tests
    @Test
    void rectangleContains() {
        // rectShape: 10,20 to 110,70 (unrotated)
        assertTrue(rectShape.contains(new Point2D(15, 25))); // Inside
        assertFalse(rectShape.contains(new Point2D(5, 25))); // Outside
        
        rectShape.setRotation(90); // Rotate 90 deg around center (60,45)
        // Original corners: (10,20), (110,20), (10,70), (110,70)
        // Center: (60,45)
        // (10,20) -> relative (-50, -25) -> rotated (25, -50) -> absolute (60+25, 45-50) = (85, -5)
        // Point (15,25) is original point, need to test against rotated.
        // A point originally at (15,25) is relative (-45, -20) to center (60,45)
        // Inverse rotate (15,25) by -90 deg around (60,45)
        // dx = 15-60 = -45; dy = 25-45 = -20
        // rad = -PI/2; cos = 0, sin = -1
        // rotX = dx*0 - dy*(-1) = dy = -20
        // rotY = dx*(-1) + dy*0 = -dx = 45
        // Rotated point relative to center: (-20, 45)
        // This point should be outside the unrotated bounds width 100 (half 50), height 50 (half 25)
        // abs(-20) <= 50 (true), abs(45) <= 25 (false) -> so (15,25) should be outside
        assertFalse(rectShape.contains(new Point2D(15,25)), "Point (15,25) should be outside after 90deg rotation");

        // A point that would be inside rotated rect.
        // E.g. a point near the new "top-left" like (60-25+5, 45-50+5) = (40,0)
        assertTrue(rectShape.contains(new Point2D(40,0)), "Point (40,0) should be inside rotated rect");
    }
    
    @Test
    void ellipseContains() {
        // ellipseShape: bounds 10,20 to 110,70 (unrotated)
        assertTrue(ellipseShape.contains(new Point2D(60, 45))); // Center
        assertTrue(ellipseShape.contains(new Point2D(10, 45))); // Middle left edge
        assertFalse(ellipseShape.contains(new Point2D(9, 45))); // Just outside left edge
    }

    @Test
    void lineSegmentContains() {
        // lineSegment: (0,0) to (10,10)
        assertTrue(lineSegment.contains(new Point2D(5,5)));
        assertTrue(lineSegment.contains(new Point2D(0,0)));
        assertTrue(lineSegment.contains(new Point2D(10,10)));
        assertTrue(lineSegment.contains(new Point2D(1,0.5))); // Close enough with epsilon
        assertFalse(lineSegment.contains(new Point2D(5,0)));

        // Test with rotation
        lineSegment.setRotation(90); // Rotated around its center (5,5)
        // Original (0,0) is now at (10,0) relative to origin, if rotation was around origin.
        // Center (5,5). Point (0,0) relative to center: (-5,-5).
        // Rotated by 90 deg: (5,-5). Absolute: (5+5, 5-5) = (10,0)
        // Point (10,0) should be on the rotated line
        assertTrue(lineSegment.contains(new Point2D(10,0)));
        // Original point (5,5) is the center, should still be on the line
        assertTrue(lineSegment.contains(new Point2D(5,5)));
    }

    @Test
    void polygonContains() {
        // polygonShape: (0,0), (10,0), (5,10)
        assertTrue(polygonShape.contains(new Point2D(5,1))); // Inside
        assertFalse(polygonShape.contains(new Point2D(0,10)));// Outside
    }
    
    @Test
    void textShapeContains() {
        // textShape: "Hello", pos(5,5), fontSize 12. Bounds approx (5,5) w=36, h=12
        // drawingBounds = Rect{topLeft=Point2D{x=5.0, y=5.0}, width=36.0, height=12.0}
        assertTrue(textShape.contains(new Point2D(6,6))); // Inside (5,5) to (41,17)
        assertFalse(textShape.contains(new Point2D(0,0)));
        textShape.setRotation(90);
        // Center of drawingBounds is (5+18, 5+6) = (23, 11)
        // Test a point that should be outside after rotation, e.g. (6,6)
        assertFalse(textShape.contains(new Point2D(6,6)), "Point (6,6) should be outside after 90deg rotation");
    }

    // Visitor tests
    @Test
    void acceptRectangle() {
        rectShape.accept(mockVisitor);
        Mockito.verify(mockVisitor).visit(rectShape);
    }
    @Test
    void acceptEllipse() {
        ellipseShape.accept(mockVisitor);
        Mockito.verify(mockVisitor).visit(ellipseShape);
    }
    @Test
    void acceptLineSegment() {
        lineSegment.accept(mockVisitor);
        Mockito.verify(mockVisitor).visit(lineSegment);
    }
    @Test
    void acceptPolygon() {
        polygonShape.accept(mockVisitor);
        Mockito.verify(mockVisitor).visit(polygonShape);
    }
    @Test
    void acceptTextShape() {
        textShape.accept(mockVisitor);
        Mockito.verify(mockVisitor).visit(textShape);
    }

    // Clone tests
    @Test
    void cloneShape() {
        Shape original = rectShape;
        Shape cloned = original.clone();

        assertNotSame(original, cloned);
        assertEquals(original.getId(), cloned.getId()); // clone() keeps ID
        assertEquals(original.getBounds(), cloned.getBounds());
        assertEquals(original.getStrokeColor(), cloned.getStrokeColor());
        assertEquals(original.getFillColor(), cloned.getFillColor());
        assertEquals(original.getRotation(), cloned.getRotation());
    }

    @Test
    void cloneShapeWithNewId() {
        Shape original = rectShape;
        Shape cloned = original.cloneWithNewId();

        assertNotSame(original, cloned);
        assertNotEquals(original.getId(), cloned.getId()); // cloneWithNewId() changes ID
        assertEquals(original.getBounds(), cloned.getBounds());
        assertEquals(original.getStrokeColor(), cloned.getStrokeColor());
        assertEquals(original.getFillColor(), cloned.getFillColor());
        assertEquals(original.getRotation(), cloned.getRotation());
    }

    // TextShape specific tests
    @Test
    void textShapeProperties() {
        assertEquals("Hello", textShape.getText());
        assertEquals(12, textShape.getBaseFontSize(), 0.001); // Renamed to getBaseFontSize
        assertEquals("Arial", textShape.getFontName());
        
        textShape.setText("World");
        assertEquals("World", textShape.getText());
        
        textShape.setFontSize(24); // This also resizes drawingBounds in current impl
        assertEquals(24, textShape.getBaseFontSize(), 0.001);

        // Check if drawingBounds scaled as expected. Original H=12, new baseFontSize=24. Scale factor 2.
        // Original drawingBounds for "Hello", baseFontSize 12: Rect{topLeft=Point2D{x=5.0, y=5.0}, width=36.0, height=12.0}
        // New drawingBounds after setFontSize(24):
        // oldHeight = 12. text="Hello" (1 line). oldEstimatedBaseHeight = 12/1 = 12.
        // scaleFactor = newSize(24) / oldEstimatedBaseHeight(12) = 2.
        // new drawingBounds should be w = 36*2=72, h = 12*2=24. Position remains (5,5).
        assertEquals(5.0, textShape.getDrawingBounds().getX());
        assertEquals(5.0, textShape.getDrawingBounds().getY());
        assertEquals(72.0, textShape.getDrawingBounds().getWidth(), 0.1); // Allow small tolerance due to font metrics
        assertEquals(24.0, textShape.getDrawingBounds().getHeight(), 0.1);

    }
    
    @Test
    void textShapeSetFontSizeWithEmptyText() {
        TextShape emptyText = new TextShape("", new Point2D(0,0), 10, "Arial", ColorData.BLACK);
        // Initial bounds might be width=0, height=10 or similar
        Rect initialBounds = emptyText.getDrawingBounds();

        emptyText.setFontSize(20);
        assertEquals(20, emptyText.getBaseFontSize());
        
        assertEquals(0.0, emptyText.getDrawingBounds().getWidth(), 0.1);
        assertEquals(initialBounds.getHeight() * 2.0, emptyText.getDrawingBounds().getHeight(), 0.1);
    }


    // getBounds() for rotated shapes (current implementations return unrotated AABB)
    @Test
    void getBoundsUnrotated() {
        assertEquals(new Rect(10,20,100,50), rectShape.getBounds());
        rectShape.setRotation(45);
        // Current impl returns unrotated bounds, so test this behavior.
        assertEquals(new Rect(10,20,100,50), rectShape.getBounds());
    }

    // equals and hashCode (based on ID)
    @Test
    void shapeEqualsAndHashCode() {
        // Create a new rect shape with same content but different ID
        RectangleShape rectShapeCloneAttrs = new RectangleShape(rectShape.getBounds(), rectShape.getStrokeColor(), rectShape.getFillColor());
        
        assertNotEquals(rectShape, rectShapeCloneAttrs); // Different IDs
        assertNotEquals(rectShape.hashCode(), rectShapeCloneAttrs.hashCode());

        Shape clonedById = rectShape.clone(); // Same ID
        assertEquals(rectShape, clonedById);
        assertEquals(rectShape.hashCode(), clonedById.hashCode());
    }
    
    @Test
    void testToStrings() {
        assertNotNull(rectShape.toString());
        assertNotNull(ellipseShape.toString());
        assertNotNull(lineSegment.toString());
        assertNotNull(polygonShape.toString());
        assertNotNull(textShape.toString());
        
        assertTrue(rectShape.toString().startsWith("RectangleShape"));
        assertTrue(ellipseShape.toString().startsWith("EllipseShape"));
        assertTrue(lineSegment.toString().startsWith("LineSegment"));
        assertTrue(polygonShape.toString().startsWith("PolygonShape"));
        assertTrue(textShape.toString().startsWith("TextShape"));
    }
}
