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

public class RectangleShapeTest {
    private Rect bounds;
    private ColorData stroke, fill;
    private RectangleShape rectShape;

    @BeforeEach
    void setUp() {
        bounds = new Rect(10, 20, 30, 40);
        stroke = ColorData.RED;
        fill = ColorData.BLUE;
        rectShape = new RectangleShape(bounds, stroke, fill);
    }

    @Test
    void constructorShouldSetProperties() {
        assertThat(rectShape.getBounds()).isEqualTo(bounds);
        assertThat(rectShape.getBounds()).isNotSameAs(bounds); // Defensive copy
        assertThat(rectShape.getStrokeColor()).isEqualTo(stroke);
        assertThat(rectShape.getFillColor()).isEqualTo(fill);
        assertThat(rectShape.getRotation()).isEqualTo(0.0);
        assertThat(rectShape.getId()).isNotNull();
    }
    
    @Test
    void constructorShouldThrowForNullArgs() {
        assertThatNullPointerException().isThrownBy(() -> new RectangleShape(null, stroke, fill));
        assertThatNullPointerException().isThrownBy(() -> new RectangleShape(bounds, null, fill));
        assertThatNullPointerException().isThrownBy(() -> new RectangleShape(bounds, stroke, null));
    }

    @Test
    void moveShouldTranslateBounds() {
        Vector2D v = new Vector2D(5, -5);
        rectShape.move(v);
        Rect expectedBounds = bounds.translated(v);
        assertThat(rectShape.getBounds()).isEqualTo(expectedBounds);
    }

    @Test
    void resizeShouldSetNewBounds() {
        Rect newBounds = new Rect(0, 0, 10, 10);
        rectShape.resize(newBounds);
        assertThat(rectShape.getBounds()).isEqualTo(newBounds);
        assertThat(rectShape.getBounds()).isNotSameAs(newBounds); //Defensive copy
    }

    @Test
    void colorGettersSettersShouldWork() {
        ColorData newStroke = ColorData.GREEN;
        ColorData newFill = ColorData.YELLOW;
        rectShape.setStrokeColor(newStroke);
        rectShape.setFillColor(newFill);
        assertThat(rectShape.getStrokeColor()).isEqualTo(newStroke);
        assertThat(rectShape.getFillColor()).isEqualTo(newFill);
    }

    @Test
    void containsShouldWorkForUnrotatedRect() {
        assertThat(rectShape.contains(new Point2D(15, 25))).isTrue(); // Inside
        assertThat(rectShape.contains(new Point2D(10, 20))).isTrue(); // Top-left corner
        assertThat(rectShape.contains(new Point2D(40, 60))).isTrue(); // Bottom-right corner
        assertThat(rectShape.contains(new Point2D(5, 25))).isFalse();  // Outside
    }

    @Test
    void containsShouldWorkForRotatedRect() {
        // Rect (10,20) width 30, height 40. Center (25, 40)
        rectShape.setRotation(90);
        // Point (15,25) is (-10, -15) relative to center.
        // Rotated by -90 deg: (-15, 10) relative.
        // Local coords: (25-15, 40+10) = (10, 50)
        // Check if (10,50) is in rect (-15, -20) to (15,20) -> halfWidth=15, halfHeight=20
        // abs(10) <= 15 (true), abs(50) <= 20 (false) -> Corrected logic in contains
        // Rotated X = dx * cos(-a) - dy * sin(-a)
        // Rotated Y = dx * sin(-a) + dy * cos(-a)
        // Point (15,25) -> dx = -10, dy = -15. angle = 90, -angle = -90.
        // cos(-90)=0, sin(-90)=-1
        // rotX = -10*0 - (-15)*(-1) = -15
        // rotY = -10*(-1) + (-15)*0 = 10
        // abs(-15) <= 15 (true), abs(10) <= 20 (true). So (15,25) should be inside.
        assertThat(rectShape.contains(new Point2D(15, 25))).isTrue();
        assertThat(rectShape.contains(new Point2D(30, 30))).isTrue(); // Test another point

        // Check a point clearly outside after rotation
        assertThat(rectShape.contains(new Point2D(0, 0))).isFalse();
    }

    @Test
    void acceptShouldCallVisitor() {
        ShapeVisitor visitor = Mockito.mock(ShapeVisitor.class);
        rectShape.accept(visitor);
        verify(visitor).visit(rectShape);
    }

    @Test
    void cloneShouldReturnIdenticalCopyWithSameId() {
        Shape clonedShape = rectShape.clone();
        assertThat(clonedShape).isInstanceOf(RectangleShape.class);
        RectangleShape clonedRect = (RectangleShape) clonedShape;
        assertThat(clonedRect).isEqualTo(rectShape); // Checks ID
        assertThat(clonedRect.getBounds()).isEqualTo(rectShape.getBounds());
        assertThat(clonedRect.getStrokeColor()).isEqualTo(rectShape.getStrokeColor());
        assertThat(clonedRect.getFillColor()).isEqualTo(rectShape.getFillColor());
        assertThat(clonedRect.getRotation()).isEqualTo(rectShape.getRotation());
        assertThat(clonedRect.getId()).isEqualTo(rectShape.getId());
    }

    @Test
    void cloneWithNewIdShouldReturnCopyWitDifferentId() {
        Shape clonedShape = rectShape.cloneWithNewId();
        assertThat(clonedShape).isInstanceOf(RectangleShape.class);
        RectangleShape clonedRect = (RectangleShape) clonedShape;
        assertThat(clonedRect.getId()).isNotEqualTo(rectShape.getId());
        assertThat(clonedRect.getBounds()).isEqualTo(rectShape.getBounds());
    }

    @Test
    void rotationGetterSetterShouldWork() {
        rectShape.setRotation(45.0);
        assertThat(rectShape.getRotation()).isEqualTo(45.0);
    }

    @Test
    void reflectHorizontalShouldChangeRotation() {
        rectShape.setRotation(30);
        rectShape.reflectHorizontal(); // (180 - 30) = 150
        assertThat(rectShape.getRotation()).isEqualTo(150.0, within(1e-9));

        rectShape.setRotation(200);
        rectShape.reflectHorizontal(); // (180 - 200) = -20 => 340
        assertThat(rectShape.getRotation()).isEqualTo(340.0, within(1e-9));
    }

    @Test
    void reflectVerticalShouldChangeRotation() {
        rectShape.setRotation(30);
        rectShape.reflectVertical(); // -30 => 330
        assertThat(rectShape.getRotation()).isEqualTo(330.0, within(1e-9));

        rectShape.setRotation(-45); // = 315
        rectShape.reflectVertical(); // -(-45) = 45
        assertThat(rectShape.getRotation()).isEqualTo(45.0, within(1e-9));
    }
    
    @Test
    void getRotatedBoundsForUnrotatedRect() {
        Rect bounds = rectShape.getBounds();
        Rect rotatedBounds = rectShape.getRotatedBounds();
        assertThat(rotatedBounds).isEqualTo(bounds);
    }
    
    @Test
    void getRotatedBoundsFor90DegreeRotation() {
        // Rect (10,20) w:30, h:40. Center (25,40)
        rectShape.setRotation(90);
        Rect rotatedBounds = rectShape.getRotatedBounds();
        // After 90 deg rotation, width becomes height and vice-versa for AABB
        // New AABB center is still (25,40)
        // New AABB width is 40, height is 30
        // New AABB top-left: (25 - 40/2, 40 - 30/2) = (25-20, 40-15) = (5,25)
        assertThat(rotatedBounds.getX()).isEqualTo(5, within(1e-6));
        assertThat(rotatedBounds.getY()).isEqualTo(25, within(1e-6));
        assertThat(rotatedBounds.getWidth()).isEqualTo(40, within(1e-6));
        assertThat(rotatedBounds.getHeight()).isEqualTo(30, within(1e-6));
    }
    
    @Test
    void getRotatedBoundsFor45DegreeRotation() {
        RectangleShape square = new RectangleShape(new Rect(0,0,10,10), stroke, fill);
        square.setRotation(45); // Center (5,5)
        Rect rotatedBounds = square.getRotatedBounds();
        // For a square 10x10 rotated by 45 deg, the AABB is sqrt(10^2+10^2) = sqrt(200) = 10*sqrt(2) approx 14.14
        double expectedSide = 10 * Math.sqrt(2);
        assertThat(rotatedBounds.getWidth()).isEqualTo(expectedSide, within(1e-6));
        assertThat(rotatedBounds.getHeight()).isEqualTo(expectedSide, within(1e-6));
        assertThat(rotatedBounds.getCenter().getX()).isEqualTo(5, within(1e-6));
        assertThat(rotatedBounds.getCenter().getY()).isEqualTo(5, within(1e-6));
    }
}