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

public class LineSegmentTest {
    private Point2D p1, p2;
    private ColorData color;
    private LineSegment line;

    @BeforeEach
    void setUp() {
        p1 = new Point2D(10, 20);
        p2 = new Point2D(40, 60);
        color = ColorData.RED;
        line = new LineSegment(p1, p2, color);
    }

    @Test
    void constructorShouldSetPropertiesAndCreateCopies() {
        assertThat(line.getStartPoint()).isEqualTo(p1);
        assertThat(line.getStartPoint()).isNotSameAs(p1);
        assertThat(line.getEndPoint()).isEqualTo(p2);
        assertThat(line.getEndPoint()).isNotSameAs(p2);
        assertThat(line.getStrokeColor()).isEqualTo(color);
        assertThat(line.getRotation()).isEqualTo(0.0);
        assertThat(line.getId()).isNotNull();
    }

    @Test
    void constructorShouldThrowExceptionForNullPointsOrColor() {
        assertThatNullPointerException().isThrownBy(() -> new LineSegment(null, p2, color));
        assertThatNullPointerException().isThrownBy(() -> new LineSegment(p1, null, color));
        assertThatNullPointerException().isThrownBy(() -> new LineSegment(p1, p2, null));
    }

    @Test
    void moveShouldTranslatePoints() {
        Vector2D v = new Vector2D(5, -5);
        line.move(v);
        assertThat(line.getStartPoint()).isEqualTo(new Point2D(15, 15));
        assertThat(line.getEndPoint()).isEqualTo(new Point2D(45, 55));
    }

    @Test
    void resizeShouldSetNewStartAndEndPoints() {
        Rect newBounds = new Rect(0, 0, 10, 10);
        line.resize(newBounds);
        assertThat(line.getStartPoint()).isEqualTo(new Point2D(0, 0));
        assertThat(line.getEndPoint()).isEqualTo(new Point2D(10, 10));
    }

    @Test
    void strokeColorGetterSetterShouldWork() {
        ColorData newColor = ColorData.BLUE;
        line.setStrokeColor(newColor);
        assertThat(line.getStrokeColor()).isEqualTo(newColor);
    }

    @Test
    void fillColorShouldBeTransparentAndSetterNoOp() {
        assertThat(line.getFillColor()).isEqualTo(ColorData.TRANSPARENT);
        line.setFillColor(ColorData.GREEN); // Should be no-op
        assertThat(line.getFillColor()).isEqualTo(ColorData.TRANSPARENT);
    }

    @Test
    void containsShouldDetectPointsOnOrNearLine() {
        // Unrotated line
        assertThat(line.contains(new Point2D(10, 20))).isTrue(); // Start point
        assertThat(line.contains(new Point2D(40, 60))).isTrue(); // End point
        assertThat(line.contains(new Point2D(25, 40))).isTrue(); // Midpoint
        assertThat(line.contains(new Point2D(25, 41))).isTrue(); // Near midpoint (within epsilon)
        assertThat(line.contains(new Point2D(0, 0))).isFalse();   // Far point

        // Rotated line
        line.setRotation(90); // Rotates around bounds center (25,40)
        // Original line: (10,20) to (40,60). Length sqrt(30^2+40^2) = 50.
        // Center (25,40)
        // After 90 deg rotation:
        // Start (10,20) -> relative (-15, -20) -> rotated (20, -15) -> absolute (45, 25)
        // End (40,60) -> relative (15, 20) -> rotated (-20, 15) -> absolute (5, 55)
        assertThat(line.contains(new Point2D(45, 25))).isTrue();
        assertThat(line.contains(new Point2D(5, 55))).isTrue();
        assertThat(line.contains(new Point2D(25, 40))).isTrue(); // Center remains on line
    }
    
    @Test
    void containsShouldHandleZeroLengthLine() {
        LineSegment pointLine = new LineSegment(new Point2D(5,5), new Point2D(5,5), ColorData.BLACK);
        assertThat(pointLine.contains(new Point2D(5,5))).isTrue();
        assertThat(pointLine.contains(new Point2D(5.1,5.1))).isTrue(); // Epsilon
        assertThat(pointLine.contains(new Point2D(10,10))).isFalse();
    }


    @Test
    void acceptShouldCallVisitor() {
        ShapeVisitor visitor = Mockito.mock(ShapeVisitor.class);
        line.accept(visitor);
        verify(visitor).visit(line);
    }

    @Test
    void cloneShouldReturnIdenticalCopyWithSameId() {
        Shape clonedShape = line.clone();
        assertThat(clonedShape).isInstanceOf(LineSegment.class);
        LineSegment clonedLine = (LineSegment) clonedShape;
        assertThat(clonedLine).isEqualTo(line); // Checks ID
        assertThat(clonedLine.getStartPoint()).isEqualTo(line.getStartPoint());
        assertThat(clonedLine.getEndPoint()).isEqualTo(line.getEndPoint());
        assertThat(clonedLine.getStrokeColor()).isEqualTo(line.getStrokeColor());
        assertThat(clonedLine.getRotation()).isEqualTo(line.getRotation());
        assertThat(clonedLine.getId()).isEqualTo(line.getId());
    }

    @Test
    void cloneWithNewIdShouldReturnCopyWitDifferentId() {
        Shape clonedShape = line.cloneWithNewId();
        assertThat(clonedShape).isInstanceOf(LineSegment.class);
        LineSegment clonedLine = (LineSegment) clonedShape;
        assertThat(clonedLine.getId()).isNotEqualTo(line.getId());
        assertThat(clonedLine.getStartPoint()).isEqualTo(line.getStartPoint());
        assertThat(clonedLine.getEndPoint()).isEqualTo(line.getEndPoint());
        assertThat(clonedLine.getStrokeColor()).isEqualTo(line.getStrokeColor());
        assertThat(clonedLine.getRotation()).isEqualTo(line.getRotation());
    }

    @Test
    void getBoundsShouldReturnCorrectAABB() {
        Rect bounds = line.getBounds();
        assertThat(bounds.getX()).isEqualTo(10);
        assertThat(bounds.getY()).isEqualTo(20);
        assertThat(bounds.getWidth()).isEqualTo(30);
        assertThat(bounds.getHeight()).isEqualTo(40);
    }

    @Test
    void rotationGetterSetterShouldWork() {
        line.setRotation(45.0);
        assertThat(line.getRotation()).isEqualTo(45.0);
        line.setRotation(405.0); // Should be normalized to 45
        assertThat(line.getRotation()).isEqualTo(45.0);
        line.setRotation(-45.0); // Should be normalized to 315
        assertThat(line.getRotation()).isEqualTo(315.0);
    }

    @Test
    void textMethodsShouldBeNoOpOrReturnDefaults() {
        assertThat(line.getText()).isNull();
        line.setText("test"); // No-op
        assertThat(line.getText()).isNull();
        assertThat(line.getFontSize()).isEqualTo(0);
        line.setFontSize(12); // No-op
        assertThat(line.getFontSize()).isEqualTo(0);
    }

    @Test
    void reflectHorizontalShouldFlipPointsAroundVerticalAxisOfBounds() {
        line.reflectHorizontal();
        // Bounds center X is (10+40)/2 = 25
        // New p1.x = 2*25 - 10 = 40
        // New p2.x = 2*25 - 40 = 10
        assertThat(line.getStartPoint()).isEqualTo(new Point2D(40, 20));
        assertThat(line.getEndPoint()).isEqualTo(new Point2D(10, 60));
    }

    @Test
    void reflectVerticalShouldFlipPointsAroundHorizontalAxisOfBounds() {
        line.reflectVertical();
        // Bounds center Y is (20+60)/2 = 40
        // New p1.y = 2*40 - 20 = 60
        // New p2.y = 2*40 - 60 = 20
        assertThat(line.getStartPoint()).isEqualTo(new Point2D(10, 60));
        assertThat(line.getEndPoint()).isEqualTo(new Point2D(40, 20));
    }

    @Test
    void equalsAndHashCodeShouldBeIdBased() {
        LineSegment line2 = new LineSegment(p1, p2, color); // Different object, different ID
        LineSegment lineCopy = (LineSegment) line.clone();    // Same ID

        assertThat(line).isNotEqualTo(line2);
        assertThat(line.hashCode()).isNotEqualTo(line2.hashCode());
        assertThat(line).isEqualTo(lineCopy);
        assertThat(line.hashCode()).isEqualTo(lineCopy.hashCode());
        assertThat(line).isNotEqualTo(null);
        assertThat(line).isNotEqualTo(new Object());
    }
    
    @Test
    void getRotatedBoundsShouldReturnCorrectAABBForRotatedLine() {
        // Line from (10,10) to (30,10) -> horizontal line, length 20
        LineSegment hLine = new LineSegment(new Point2D(10,10), new Point2D(30,10), ColorData.BLACK);
        hLine.setRotation(90); // Rotate 90 degrees around its center (20,10)
        // After rotation, it becomes a vertical line from (20,0) to (20,20)
        Rect rotatedBounds = hLine.getRotatedBounds();
        assertThat(rotatedBounds.getX()).isEqualTo(20, within(1e-6));
        assertThat(rotatedBounds.getY()).isEqualTo(0, within(1e-6));
        assertThat(rotatedBounds.getWidth()).isEqualTo(0, within(1e-6)); // AABB of a vertical line
        assertThat(rotatedBounds.getHeight()).isEqualTo(20, within(1e-6));

        // Line from (0,0) to (0,10) -> vertical line
        LineSegment vLine = new LineSegment(new Point2D(0,0), new Point2D(0,10), ColorData.BLACK);
        vLine.setRotation(90); // Rotate 90 degrees around (0,5)
        // Becomes horizontal line from (-5,5) to (5,5)
        rotatedBounds = vLine.getRotatedBounds();
        assertThat(rotatedBounds.getX()).isEqualTo(-5, within(1e-6));
        assertThat(rotatedBounds.getY()).isEqualTo(5, within(1e-6));
        assertThat(rotatedBounds.getWidth()).isEqualTo(10, within(1e-6));
        assertThat(rotatedBounds.getHeight()).isEqualTo(0, within(1e-6));
        
        // Original line (10,20) to (40,60), center (25,40)
        line.setRotation(90);
        rotatedBounds = line.getRotatedBounds();
        // Expected: p1'(45,25), p2'(5,55)
        assertThat(rotatedBounds.getX()).isEqualTo(5, within(1e-6));
        assertThat(rotatedBounds.getY()).isEqualTo(25, within(1e-6));
        assertThat(rotatedBounds.getWidth()).isEqualTo(40, within(1e-6));
        assertThat(rotatedBounds.getHeight()).isEqualTo(30, within(1e-6));
    }
    
    @Test
    void getRotatedBoundsForUnrotatedLine() {
        Rect bounds = line.getBounds();
        Rect rotatedBounds = line.getRotatedBounds();
        assertThat(rotatedBounds).isEqualTo(bounds);
    }
}