package sad.gruppo11.Model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Rect;
import sad.gruppo11.Model.geometry.Vector2D;
import sad.gruppo11.View.ShapeVisitor;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.verify;

public class EllipseShapeTest {
    private Rect bounds;
    private ColorData stroke, fill;
    private EllipseShape ellipseShape;

    @BeforeEach
    void setUp() {
        bounds = new Rect(10, 20, 40, 20); // rx=20, ry=10, center (30,30)
        stroke = ColorData.RED;
        fill = ColorData.BLUE;
        ellipseShape = new EllipseShape(bounds, stroke, fill);
    }

    @Test
    void constructorShouldSetProperties() {
        assertThat(ellipseShape.getBounds()).isEqualTo(bounds);
        assertThat(ellipseShape.getStrokeColor()).isEqualTo(stroke);
        assertThat(ellipseShape.getFillColor()).isEqualTo(fill);
        assertThat(ellipseShape.getRotation()).isEqualTo(0.0);
    }

    @Test
    void containsShouldWorkForUnrotatedEllipse() {
        assertThat(ellipseShape.contains(new Point2D(30, 30))).isTrue(); // Center
        assertThat(ellipseShape.contains(new Point2D(10, 30))).isTrue(); // Left edge (x=center-rx)
        assertThat(ellipseShape.contains(new Point2D(50, 30))).isTrue(); // Right edge (x=center+rx)
        assertThat(ellipseShape.contains(new Point2D(30, 20))).isTrue(); // Top edge (y=center-ry)
        assertThat(ellipseShape.contains(new Point2D(30, 40))).isTrue(); // Bottom edge (y=center+ry)
        assertThat(ellipseShape.contains(new Point2D(10, 20))).isFalse(); // Corner of bounding box, outside ellipse
        assertThat(ellipseShape.contains(new Point2D(0,0))).isFalse(); // Far outside
    }

    @Test
    void containsShouldWorkForRotatedEllipse() {
        // Ellipse bounds (10,20) w:40, h:20. Center (30,30), rx=20, ry=10
        ellipseShape.setRotation(90); // Rotated, rx and ry effectively swap for axis-aligned tests
        assertThat(ellipseShape.contains(new Point2D(30, 30))).isTrue(); // Center is always in
        // Original point on ellipse: (30,20) (top edge) -> (cx, cy-ry) -> dx=0, dy=-10
        // Rotate -90 deg: rotX = 0*cos(-90) - (-10)*sin(-90) = -10*(-1) = -10
        //                 rotY = 0*sin(-90) + (-10)*cos(-90) = 0
        // Test point (30-10, 30) = (20,30)
        assertThat(ellipseShape.contains(new Point2D(20, 30))).isTrue();

        // Original point (10,30) (left edge) -> (cx-rx, cy) -> dx=-20, dy=0
        // Rotate -90 deg: rotX = -20*0 - 0*(-1) = 0
        //                 rotY = -20*(-1) + 0*0 = 20
        // Test point (30, 30+20) = (30,50)
        assertThat(ellipseShape.contains(new Point2D(30, 50))).isTrue();
        
        assertThat(ellipseShape.contains(new Point2D(0,0))).isFalse();
    }
    
    @Test
    void containsShouldReturnFalseForDegenerateEllipse() {
        EllipseShape degenerate = new EllipseShape(new Rect(0,0,0,10), stroke, fill);
        assertThat(degenerate.contains(new Point2D(0,0))).isFalse();
        degenerate = new EllipseShape(new Rect(0,0,10,0), stroke, fill);
        assertThat(degenerate.contains(new Point2D(0,0))).isFalse();
    }

    @Test
    void reflectHorizontalShouldChangeRotation() {
        ellipseShape.setRotation(30);
        ellipseShape.reflectHorizontal(); // (180 - 30) = 150
        assertThat(ellipseShape.getRotation()).isEqualTo(150.0, within(1e-9));
    }

    @Test
    void reflectVerticalShouldChangeRotation() {
        ellipseShape.setRotation(30);
        ellipseShape.reflectVertical(); // -30 => 330
        assertThat(ellipseShape.getRotation()).isEqualTo(330.0, within(1e-9));
    }
    
    @Test
    void getRotatedBoundsForUnrotatedEllipse() {
        Rect bounds = ellipseShape.getBounds();
        Rect rotatedBounds = ellipseShape.getRotatedBounds();
        assertThat(rotatedBounds).isEqualTo(bounds);
    }

    @Test
    void getRotatedBoundsFor90DegreeRotation() {
        // Ellipse bounds (10,20) w:40, h:20. rx=20, ry=10. Center (30,30)
        ellipseShape.setRotation(90);
        Rect rotatedBounds = ellipseShape.getRotatedBounds();
        // After 90 deg rotation, AABB width is 2*ry, AABB height is 2*rx
        // New AABB center is still (30,30)
        // New AABB width is 2*10=20, height is 2*20=40
        // New AABB top-left: (30 - 20/2, 30 - 40/2) = (30-10, 30-20) = (20,10)
        assertThat(rotatedBounds.getX()).isEqualTo(20, within(1e-6));
        assertThat(rotatedBounds.getY()).isEqualTo(10, within(1e-6));
        assertThat(rotatedBounds.getWidth()).isEqualTo(20, within(1e-6));
        assertThat(rotatedBounds.getHeight()).isEqualTo(40, within(1e-6));
    }

    @Test
    void getRotatedBoundsForDegenerateEllipse() {
        EllipseShape degen = new EllipseShape(new Rect(1,1,0,10), stroke, fill);
        degen.setRotation(45);
        Rect rb = degen.getRotatedBounds();
        assertThat(rb.getWidth()).isEqualTo(0, within(1e-6));
        assertThat(rb.getHeight()).isEqualTo(0, within(1e-6));
        assertThat(rb.getCenter()).isEqualTo(new Point2D(1,6));
    }

    // Other tests (move, resize, colors, clone, accept, etc.) would be similar to RectangleShapeTest
    // and are omitted for brevity but should be included in a full test suite.
     @Test
    void constructorShouldThrowForNullArgs() {
        assertThatNullPointerException().isThrownBy(() -> new EllipseShape(null, stroke, fill));
        assertThatNullPointerException().isThrownBy(() -> new EllipseShape(bounds, null, fill));
        assertThatNullPointerException().isThrownBy(() -> new EllipseShape(bounds, stroke, null));
    }
    
    @Test
    void moveShouldTranslateBounds() {
        Vector2D v = new Vector2D(5, -5);
        ellipseShape.move(v);
        Rect expectedBounds = bounds.translated(v);
        assertThat(ellipseShape.getBounds()).isEqualTo(expectedBounds);
    }

    @Test
    void cloneShouldReturnIdenticalCopyWithSameId() {
        Shape clonedShape = ellipseShape.clone();
        assertThat(clonedShape).isInstanceOf(EllipseShape.class);
        EllipseShape clonedEllipse = (EllipseShape) clonedShape;
        assertThat(clonedEllipse).isEqualTo(ellipseShape); // Checks ID
        assertThat(clonedEllipse.getBounds()).isEqualTo(ellipseShape.getBounds());
        assertThat(clonedEllipse.getStrokeColor()).isEqualTo(ellipseShape.getStrokeColor());
        assertThat(clonedEllipse.getFillColor()).isEqualTo(ellipseShape.getFillColor());
        assertThat(clonedEllipse.getRotation()).isEqualTo(ellipseShape.getRotation());
        assertThat(clonedEllipse.getId()).isEqualTo(ellipseShape.getId());
    }
}