package sad.gruppo11.Model.geometry;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

public class Point2DTest {

    @Test
    void constructorShouldSetCoordinates() {
        Point2D p = new Point2D(10.5, -5.2);
        assertThat(p.getX()).isEqualTo(10.5);
        assertThat(p.getY()).isEqualTo(-5.2);
    }

    @Test
    void copyConstructorShouldCopyCoordinates() {
        Point2D original = new Point2D(3.0, 7.0);
        Point2D copy = new Point2D(original);
        assertThat(copy.getX()).isEqualTo(3.0);
        assertThat(copy.getY()).isEqualTo(7.0);
        assertThat(copy).isEqualTo(original);
        assertThat(copy).isNotSameAs(original);
    }

    @Test
    void copyConstructorShouldThrowNullPointerExceptionForNullInput() {
        assertThatNullPointerException().isThrownBy(() -> new Point2D(null))
            .withMessageContaining("Other Point2D cannot be null");
    }

    @Test
    void gettersAndSettersShouldWork() {
        Point2D p = new Point2D(0, 0);
        p.setX(15.0);
        p.setY(25.0);
        assertThat(p.getX()).isEqualTo(15.0);
        assertThat(p.getY()).isEqualTo(25.0);
    }

    @Test
    void distanceShouldCalculateCorrectly() {
        Point2D p1 = new Point2D(0, 0);
        Point2D p2 = new Point2D(3, 4);
        assertThat(p1.distance(p2)).isEqualTo(5.0, within(1e-9));
        assertThat(p2.distance(p1)).isEqualTo(5.0, within(1e-9));
        assertThat(p1.distance(p1)).isEqualTo(0.0, within(1e-9));
    }

    @Test
    void distanceShouldThrowNullPointerExceptionForNullInput() {
        Point2D p1 = new Point2D(0, 0);
        assertThatNullPointerException().isThrownBy(() -> p1.distance(null))
            .withMessageContaining("Other Point2D cannot be null");
    }

    @Test
    void translateByDxDyShouldModifyCoordinates() {
        Point2D p = new Point2D(1, 2);
        p.translate(5, -3);
        assertThat(p.getX()).isEqualTo(6.0);
        assertThat(p.getY()).isEqualTo(-1.0);
    }

    @Test
    void translateByVectorShouldModifyCoordinates() {
        Point2D p = new Point2D(10, 20);
        Vector2D v = new Vector2D(-2, 7);
        p.translate(v);
        assertThat(p.getX()).isEqualTo(8.0);
        assertThat(p.getY()).isEqualTo(27.0);
    }

    @Test
    void translateByVectorShouldThrowNullPointerExceptionForNullInput() {
        Point2D p = new Point2D(0, 0);
        assertThatNullPointerException().isThrownBy(() -> p.translate((Vector2D)null))
            .withMessageContaining("Vector2D v cannot be null");
    }

    @Test
    void equalsAndHashCodeShouldWorkCorrectly() {
        Point2D p1 = new Point2D(1.2, 3.4);
        Point2D p2 = new Point2D(1.2, 3.4);
        Point2D p3 = new Point2D(5.6, 7.8);
        Point2D p4 = new Point2D(1.2, 0.0); // Different Y

        assertThat(p1).isEqualTo(p2)
                      .hasSameHashCodeAs(p2);
        assertThat(p1).isNotEqualTo(p3);
        assertThat(p1.hashCode()).isNotEqualTo(p3.hashCode());
        assertThat(p1).isNotEqualTo(p4);
        assertThat(p1.hashCode()).isNotEqualTo(p4.hashCode());
        assertThat(p1).isNotEqualTo(null);
        assertThat(p1).isNotEqualTo(new Object());
    }

    @Test
    void toStringShouldReturnFormattedString() {
        Point2D p = new Point2D(1.0, 2.5);
        assertThat(p.toString()).isEqualTo("Point2D{x=1.0, y=2.5}");
    }
}