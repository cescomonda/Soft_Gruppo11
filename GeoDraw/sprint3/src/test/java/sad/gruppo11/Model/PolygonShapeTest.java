package sad.gruppo11.Model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Rect;
import sad.gruppo11.Model.geometry.Vector2D;
import sad.gruppo11.View.ShapeVisitor;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.verify;

public class PolygonShapeTest {
    private List<Point2D> vertices;
    private ColorData stroke, fill;
    private PolygonShape polygon;

    @BeforeEach
    void setUp() {
        vertices = Arrays.asList(new Point2D(0, 0), new Point2D(30, 0), new Point2D(15, 20));
        stroke = ColorData.GREEN;
        fill = ColorData.YELLOW;
        polygon = new PolygonShape(new ArrayList<>(vertices), stroke, fill); // Pass a copy
    }

    @Test
    void constructorShouldSetPropertiesAndCopyVertices() {
        assertThat(polygon.getVertices()).containsExactlyElementsOf(vertices);
        assertThat(polygon.getVertices().get(0)).isNotSameAs(vertices.get(0)); // Copied
        assertThat(polygon.getStrokeColor()).isEqualTo(stroke);
        assertThat(polygon.getFillColor()).isEqualTo(fill);
        assertThat(polygon.getRotation()).isEqualTo(0.0);
    }

    @Test
    void constructorShouldThrowForInvalidVerticesList() {
        assertThatNullPointerException().isThrownBy(() -> new PolygonShape(null, stroke, fill));
        List<Point2D> tooFewVertices = Arrays.asList(new Point2D(0,0), new Point2D(1,1));
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new PolygonShape(tooFewVertices, stroke, fill))
            .withMessage("PolygonShape must have at least 3 vertices.");
    }

    @Test
    void getVerticesShouldReturnDefensiveCopy() {
        List<Point2D> retrievedVertices = polygon.getVertices();
        assertThat(retrievedVertices).isNotSameAs(polygon.getVertices()); // Ensure it's a copy from the internal list
        retrievedVertices.add(new Point2D(100,100)); // Modify copy
        assertThat(polygon.getVertices()).containsExactlyElementsOf(vertices); // Original unchanged
    }

    @Test
    void moveShouldTranslateAllVertices() {
        Vector2D v = new Vector2D(10, 5);
        polygon.move(v);
        List<Point2D> expectedVertices = Arrays.asList(
            new Point2D(10, 5), new Point2D(40, 5), new Point2D(25, 25)
        );
        assertThat(polygon.getVertices()).containsExactlyElementsOf(expectedVertices);
    }
    
    @Test
    void resizeDegeneratePolygonShouldNotFail() {
        List<Point2D> degenVertices = Arrays.asList(new Point2D(0,0), new Point2D(10,0), new Point2D(20,0)); // Collinear
        PolygonShape degenPoly = new PolygonShape(degenVertices, stroke, fill);
        Rect oldBounds = degenPoly.getBounds(); // width=20, height=0
        
        Rect newBounds = new Rect(0,0, 40, 10); // Try to give it height
        // The current resize logic might produce odd results if oldBounds.height is 0.
        // For now, just ensure it doesn't throw an exception.
        assertThatCode(() -> degenPoly.resize(newBounds)).doesNotThrowAnyException();
        // If oldBounds.getWidth() or oldBounds.getHeight() is 0, scaleX/scaleY can be problematic.
        // The implementation has a check to avoid division by zero.
    }


    @Test
    void containsShouldWorkForUnrotatedPolygon() {
        // Triangle (0,0), (30,0), (15,20)
        assertThat(polygon.contains(new Point2D(15, 10))).isTrue();  // Inside
        assertThat(polygon.contains(new Point2D(0, 0))).isTrue();    // On vertex
        assertThat(polygon.contains(new Point2D(15, 0))).isTrue();   // On edge
        assertThat(polygon.contains(new Point2D(15, 25))).isFalse(); // Outside
    }

    @Test
    void getBoundsShouldBeCorrect() {
        Rect bounds = polygon.getBounds();
        assertThat(bounds.getX()).isEqualTo(0);
        assertThat(bounds.getY()).isEqualTo(0);
        assertThat(bounds.getWidth()).isEqualTo(30);
        assertThat(bounds.getHeight()).isEqualTo(20);
    }

    @Test
    void getRotatedBoundsUnrotated() {
        assertThat(polygon.getRotatedBounds()).isEqualTo(polygon.getBounds());
    }
    
    @Test
    void getRotatedBounds90Degrees() {
        // vertices (0,0), (30,0), (15,20) -> bounds (0,0, w:30, h:20), center (15,10)
        polygon.setRotation(90);
        Rect rotatedBounds = polygon.getRotatedBounds();
        // Rotated vertices around (15,10):
        // (0,0) -> rel (-15,-10) -> rot (10,-15) -> abs (25,-5)
        // (30,0) -> rel (15,-10) -> rot (10,15) -> abs (25,25)
        // (15,20) -> rel (0,10) -> rot (-10,0) -> abs (5,10)
        // MinX=5, MaxX=25 => w=20
        // MinY=-5, MaxY=25 => h=30
        // TopLeft (5,-5)
        assertThat(rotatedBounds.getX()).isEqualTo(5, within(1e-6));
        assertThat(rotatedBounds.getY()).isEqualTo(-5, within(1e-6));
        assertThat(rotatedBounds.getWidth()).isEqualTo(20, within(1e-6));
        assertThat(rotatedBounds.getHeight()).isEqualTo(30, within(1e-6));
    }


    // Other standard tests (clone, accept, colors, etc.) are similar to other shapes.
    @Test
    void cloneShouldReturnIdenticalCopyWithSameId() {
        Shape clonedShape = polygon.clone();
        assertThat(clonedShape).isInstanceOf(PolygonShape.class);
        PolygonShape clonedPoly = (PolygonShape) clonedShape;
        assertThat(clonedPoly).isEqualTo(polygon); // Checks ID
        assertThat(clonedPoly.getVertices()).containsExactlyElementsOf(polygon.getVertices());
        assertThat(clonedPoly.getStrokeColor()).isEqualTo(polygon.getStrokeColor());
        assertThat(clonedPoly.getFillColor()).isEqualTo(polygon.getFillColor());
        assertThat(clonedPoly.getRotation()).isEqualTo(polygon.getRotation());
        assertThat(clonedPoly.getId()).isEqualTo(polygon.getId());
    }
}