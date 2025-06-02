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

public class TextShapeTest {
    private TextShape textShape;
    private String text = "Hello";
    private Point2D position = new Point2D(10, 20);
    private double fontSize = 12;
    private String fontName = "Arial";
    private ColorData color = ColorData.BLUE;

    @BeforeEach
    void setUp() {
        textShape = new TextShape(text, position, fontSize, fontName, color);
    }

    @Test
    void constructorShouldSetProperties() {
        assertThat(textShape.getText()).isEqualTo(text);
        // Initial drawingBounds are estimated, so we check position related to it.
        assertThat(textShape.getDrawingBounds().getTopLeft()).isEqualTo(position);
        assertThat(textShape.getBaseFontSize()).isEqualTo(fontSize);
        assertThat(textShape.getFontName()).isEqualTo(fontName);
        assertThat(textShape.getStrokeColor()).isEqualTo(color); // StrokeColor is textColor
        assertThat(textShape.getFillColor()).isEqualTo(ColorData.TRANSPARENT);
        assertThat(textShape.getRotation()).isEqualTo(0.0);
        assertThat(textShape.getId()).isNotNull();
        assertThat(textShape.isHorizontallyFlipped()).isFalse();
        assertThat(textShape.isVerticallyFlipped()).isFalse();
    }

    @Test
    void constructorShouldThrowForInvalidArgs() {
        assertThatNullPointerException().isThrownBy(() -> new TextShape(null, position, fontSize, fontName, color));
        assertThatNullPointerException().isThrownBy(() -> new TextShape(text, null, fontSize, fontName, color));
        assertThatIllegalArgumentException().isThrownBy(() -> new TextShape(text, position, 0, fontName, color));
        assertThatIllegalArgumentException().isThrownBy(() -> new TextShape(text, position, -5, fontName, color));
        assertThatNullPointerException().isThrownBy(() -> new TextShape(text, position, fontSize, null, color));
        assertThatNullPointerException().isThrownBy(() -> new TextShape(text, position, fontSize, fontName, null));
    }

    @Test
    void resizeShouldSetNewDrawingBounds() {
        Rect newBounds = new Rect(0,0,100,50);
        textShape.resize(newBounds);
        assertThat(textShape.getDrawingBounds()).isEqualTo(newBounds);
    }

    @Test
    void setStrokeColorShouldSetTextColor() {
        ColorData newColor = ColorData.GREEN;
        textShape.setStrokeColor(newColor);
        assertThat(textShape.getStrokeColor()).isEqualTo(newColor);
        assertThat(textShape.getStrokeColor()).isEqualTo(newColor); // Assuming a getter for textColor
    }

    @Test
    void setFillColorShouldBeNoOpAndGetFillColorTransparent() {
        textShape.setFillColor(ColorData.RED);
        assertThat(textShape.getFillColor()).isEqualTo(ColorData.TRANSPARENT);
    }
    
    @Test
    void containsShouldWorkBasedOnDrawingBoundsAndRotation() {
        // Default estimated bounds for "Hello", size 12: approx 12*5*0.65 = 39 width, 12*1.2 = 14.4 height
        // at (10,20). So bounds (10,20) to (49, 34.4)
        Rect currentBounds = textShape.getDrawingBounds(); // Use the actual estimated bounds
        Point2D center = currentBounds.getCenter();
        assertThat(textShape.contains(center)).isTrue();

        textShape.setRotation(90);
        assertThat(textShape.contains(center)).isTrue(); // Center should still be contained
        
        // A point that was outside, then rotated in (or vice-versa)
        Point2D outsidePoint = new Point2D(0,0);
        assertThat(textShape.contains(outsidePoint)).isFalse(); // Assuming (0,0) is outside rotated bounds
    }

    @Test
    void setTextAndGetTextShouldWork() {
        String newText = "World";
        textShape.setText(newText);
        assertThat(textShape.getText()).isEqualTo(newText);
        textShape.setText(null); // Should set to ""
        assertThat(textShape.getText()).isEqualTo("");
    }

    @Test
    void reflectHorizontalAndVerticalShouldToggleFlags() {
        assertThat(textShape.isHorizontallyFlipped()).isFalse();
        textShape.reflectHorizontal();
        assertThat(textShape.isHorizontallyFlipped()).isTrue();
        textShape.reflectHorizontal();
        assertThat(textShape.isHorizontallyFlipped()).isFalse();

        assertThat(textShape.isVerticallyFlipped()).isFalse();
        textShape.reflectVertical();
        assertThat(textShape.isVerticallyFlipped()).isTrue();
        textShape.reflectVertical();
        assertThat(textShape.isVerticallyFlipped()).isFalse();
    }
    
    @Test
    void getRotatedBoundsShouldWork() {
        // Initial bounds (estimated): (10,20) w=39, h=14.4. Center approx (29.5, 27.2)
        Rect db = textShape.getDrawingBounds();
        
        textShape.setRotation(0);
        assertThat(textShape.getRotatedBounds()).isEqualTo(db);

        textShape.setRotation(90);
        Rect rotatedBounds = textShape.getRotatedBounds();
        // AABB of the rotated drawingBounds
        assertThat(rotatedBounds.getWidth()).isEqualTo(db.getHeight(), within(1e-6));
        assertThat(rotatedBounds.getHeight()).isEqualTo(db.getWidth(), within(1e-6));
        assertThat(rotatedBounds.getCenter()).usingRecursiveComparison()
            .withComparatorForType((a, b) -> Math.abs(a.getX() - b.getX()) < 1e-6 && Math.abs(a.getY() - b.getY()) < 1e-6 ? 0 : 1, Point2D.class)
            .isEqualTo(db.getCenter());
    }


    // Helper method to access internal textColor (if needed, or make it package-private in TextShape)
    private ColorData getTextColorForTest(TextShape ts) {
        return ts.getStrokeColor(); // As per current implementation
    }
}
