package sad.gruppo11.Model.geometry;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

public class RectTest {

    @Test
    void constructorWithPointWidthHeightShouldSetValues() {
        Point2D topLeft = new Point2D(10, 20);
        Rect r = new Rect(topLeft, 100, 50);
        assertThat(r.getTopLeft()).isEqualTo(topLeft);
        assertThat(r.getTopLeft()).isNotSameAs(topLeft); // Should be a copy
        assertThat(r.getWidth()).isEqualTo(100);
        assertThat(r.getHeight()).isEqualTo(50);
    }
    
    @Test
    void constructorWithPointWidthHeightShouldClampNegativeDimensionsToZero() {
        Point2D topLeft = new Point2D(10, 20);
        Rect r = new Rect(topLeft, -100, -50);
        assertThat(r.getWidth()).isEqualTo(0);
        assertThat(r.getHeight()).isEqualTo(0);
    }


    @Test
    void constructorWithXYWidthHeightShouldSetValues() {
        Rect r = new Rect(5, 15, 80, 40);
        assertThat(r.getTopLeft()).isEqualTo(new Point2D(5, 15));
        assertThat(r.getWidth()).isEqualTo(80);
        assertThat(r.getHeight()).isEqualTo(40);
    }

    @Test
    void copyConstructorShouldCopyValues() {
        Rect original = new Rect(1, 2, 30, 40);
        Rect copy = new Rect(original);
        assertThat(copy.getTopLeft()).isEqualTo(original.getTopLeft());
        assertThat(copy.getWidth()).isEqualTo(original.getWidth());
        assertThat(copy.getHeight()).isEqualTo(original.getHeight());
        assertThat(copy).isEqualTo(original);
        assertThat(copy).isNotSameAs(original);
        assertThat(copy.getTopLeft()).isNotSameAs(original.getTopLeft());
    }

    @Test
    void constructorAndCopyConstructorShouldThrowNullPointerExceptionForNullInput() {
        assertThatNullPointerException().isThrownBy(() -> new Rect(null, 10, 10))
            .withMessageContaining("TopLeft point cannot be null");
        assertThatNullPointerException().isThrownBy(() -> new Rect(null))
            .withMessageContaining("Other Rect cannot be null");
    }

    @Test
    void gettersShouldReturnCorrectCalculatedValues() {
        Rect r = new Rect(10, 20, 100, 50);
        assertThat(r.getX()).isEqualTo(10);
        assertThat(r.getY()).isEqualTo(20);
        assertThat(r.getRight()).isEqualTo(110);
        assertThat(r.getBottom()).isEqualTo(70);
        assertThat(r.getBottomRight()).isEqualTo(new Point2D(110, 70));
        assertThat(r.getCenter()).isEqualTo(new Point2D(60, 45));
    }
    
    @Test
    void settersShouldModifyValues() {
        Rect r = new Rect(0,0,0,0);
        Point2D newTopLeft = new Point2D(5,5);
        r.setTopLeft(newTopLeft);
        assertThat(r.getTopLeft()).isEqualTo(newTopLeft);
        assertThat(r.getTopLeft()).isNotSameAs(newTopLeft); // Should be a copy

        r.setTopLeft(1,2);
        assertThat(r.getTopLeft()).isEqualTo(new Point2D(1,2));

        r.setWidth(10);
        assertThat(r.getWidth()).isEqualTo(10);
        r.setHeight(20);
        assertThat(r.getHeight()).isEqualTo(20);

        r.setWidth(-5); // Should be clamped
        assertThat(r.getWidth()).isEqualTo(0);
        r.setHeight(-10); // Should be clamped
        assertThat(r.getHeight()).isEqualTo(0);
    }
    
    @Test
    void setTopLeftPointShouldThrowNullPointerExceptionForNullInput(){
        Rect r = new Rect(0,0,1,1);
         assertThatNullPointerException().isThrownBy(() -> r.setTopLeft(null))
            .withMessageContaining("TopLeft point cannot be null");
    }

    @Test
    void containsShouldWorkCorrectly() {
        Rect r = new Rect(0, 0, 10, 10);
        assertThat(r.contains(new Point2D(5, 5))).isTrue();    // Inside
        assertThat(r.contains(new Point2D(0, 0))).isTrue();    // Top-left corner
        assertThat(r.contains(new Point2D(10, 10))).isTrue();  // Bottom-right corner
        assertThat(r.contains(new Point2D(15, 5))).isFalse();   // Outside (right)
        assertThat(r.contains(new Point2D(5, 15))).isFalse();   // Outside (bottom)
        assertThat(r.contains(new Point2D(-1, 5))).isFalse();  // Outside (left)
        assertThat(r.contains(new Point2D(5, -1))).isFalse();  // Outside (top)
    }

    @Test
    void containsShouldThrowNullPointerExceptionForNullInput() {
        Rect r = new Rect(0, 0, 10, 10);
        assertThatNullPointerException().isThrownBy(() -> r.contains(null))
            .withMessageContaining("Point p cannot be null");
    }

    @Test
    void translateByDxDyShouldModifyTopLeft() {
        Rect r = new Rect(10, 20, 100, 50);
        r.translate(5, -5);
        assertThat(r.getTopLeft()).isEqualTo(new Point2D(15, 15));
        assertThat(r.getWidth()).isEqualTo(100); // Dimensions unchanged
        assertThat(r.getHeight()).isEqualTo(50);
    }

    @Test
    void translateByVectorShouldModifyTopLeft() {
        Rect r = new Rect(0, 0, 10, 10);
        r.translate(new Vector2D(2, 3));
        assertThat(r.getTopLeft()).isEqualTo(new Point2D(2, 3));
    }
    
    @Test
    void translateByVectorShouldThrowNullPointerExceptionForNullInput() {
        Rect r = new Rect(0,0,1,1);
        assertThatNullPointerException().isThrownBy(() -> r.translate((Vector2D)null))
           .withMessageContaining("Vector v cannot be null");
    }


    @Test
    void translatedByDxDyShouldReturnNewRect() {
        Rect original = new Rect(10, 20, 100, 50);
        Rect translated = original.translated(5, -5);

        assertThat(original.getTopLeft()).isEqualTo(new Point2D(10, 20)); // Original unchanged
        assertThat(translated.getTopLeft()).isEqualTo(new Point2D(15, 15));
        assertThat(translated.getWidth()).isEqualTo(100);
        assertThat(translated.getHeight()).isEqualTo(50);
    }

    @Test
    void translatedByVectorShouldReturnNewRect() {
        Rect original = new Rect(0, 0, 10, 10);
        Rect translated = original.translated(new Vector2D(2, 3));
        assertThat(original.getTopLeft()).isEqualTo(new Point2D(0, 0));
        assertThat(translated.getTopLeft()).isEqualTo(new Point2D(2, 3));
    }
    
    @Test
    void translatedByVectorShouldThrowNullPointerExceptionForNullInput() {
        Rect r = new Rect(0,0,1,1);
        assertThatNullPointerException().isThrownBy(() -> r.translated((Vector2D)null))
           .withMessageContaining("Vector v cannot be null");
    }

    @Test
    void equalsAndHashCodeShouldWorkCorrectly() {
        Rect r1 = new Rect(1, 2, 30, 40);
        Rect r2 = new Rect(1, 2, 30, 40);
        Rect r3 = new Rect(0, 0, 30, 40); // Different topLeft
        Rect r4 = new Rect(1, 2, 0, 40);  // Different width
        Rect r5 = new Rect(1, 2, 30, 0);  // Different height

        assertThat(r1).isEqualTo(r2)
                      .hasSameHashCodeAs(r2);
        assertThat(r1).isNotEqualTo(r3);
        assertThat(r1.hashCode()).isNotEqualTo(r3.hashCode());
        assertThat(r1).isNotEqualTo(r4);
        assertThat(r1.hashCode()).isNotEqualTo(r4.hashCode());
        assertThat(r1).isNotEqualTo(r5);
        assertThat(r1.hashCode()).isNotEqualTo(r5.hashCode());
        assertThat(r1).isNotEqualTo(null);
        assertThat(r1).isNotEqualTo(new Object());
    }

    @Test
    void toStringShouldReturnFormattedString() {
        Rect r = new Rect(1.5, 2.0, 10.25, 20.75);
        String str = r.toString(); // Output may vary due to locale for decimal format
        assertThat(str).startsWith("Rect{topLeft=Point2D{x=1.5, y=2.0}, width=")
                       .endsWith("}");
        assertThat(str).containsPattern("width=\\d{1,2}[.,]\\d{2}, height=\\d{1,2}[.,]\\d{2}");
    }
}