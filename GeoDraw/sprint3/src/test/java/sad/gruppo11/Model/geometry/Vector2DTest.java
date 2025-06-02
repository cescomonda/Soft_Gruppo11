package sad.gruppo11.Model.geometry;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

public class Vector2DTest {

    @Test
    void constructorShouldSetComponents() {
        Vector2D v = new Vector2D(3.5, -2.1);
        assertThat(v.getDx()).isEqualTo(3.5);
        assertThat(v.getDy()).isEqualTo(-2.1);
    }

    @Test
    void copyConstructorShouldCopyComponents() {
        Vector2D original = new Vector2D(1.0, 2.0);
        Vector2D copy = new Vector2D(original);
        assertThat(copy.getDx()).isEqualTo(1.0);
        assertThat(copy.getDy()).isEqualTo(2.0);
        assertThat(copy).isEqualTo(original);
        assertThat(copy).isNotSameAs(original);
    }

    @Test
    void copyConstructorShouldThrowNullPointerExceptionForNullInput() {
        assertThatNullPointerException().isThrownBy(() -> new Vector2D(null))
            .withMessageContaining("Other Vector2D cannot be null");
    }

    @Test
    void gettersAndSettersShouldWork() {
        Vector2D v = new Vector2D(0, 0);
        v.setDx(5.0);
        v.setDy(7.0);
        assertThat(v.getDx()).isEqualTo(5.0);
        assertThat(v.getDy()).isEqualTo(7.0);
    }

    @Test
    void lengthShouldCalculateCorrectly() {
        Vector2D v1 = new Vector2D(3, 4);
        assertThat(v1.length()).isEqualTo(5.0, within(1e-9));
        Vector2D v2 = new Vector2D(0, 0);
        assertThat(v2.length()).isEqualTo(0.0, within(1e-9));
        Vector2D v3 = new Vector2D(-1, 0);
        assertThat(v3.length()).isEqualTo(1.0, within(1e-9));
    }

    @Test
    void normalizeShouldModifyVectorInPlace() {
        Vector2D v = new Vector2D(3, 4);
        v.normalize();
        assertThat(v.getDx()).isEqualTo(0.6, within(1e-9));
        assertThat(v.getDy()).isEqualTo(0.8, within(1e-9));
        assertThat(v.length()).isEqualTo(1.0, within(1e-9));
    }

    @Test
    void normalizeOnZeroVectorShouldNotChangeIt() {
        Vector2D v = new Vector2D(0, 0);
        v.normalize();
        assertThat(v.getDx()).isEqualTo(0.0);
        assertThat(v.getDy()).isEqualTo(0.0);
    }
    
    @Test
    void normalizeOnAlreadyNormalizedVectorShouldNotChangeIt() {
        Vector2D v = new Vector2D(1.0, 0.0);
        v.normalize(); // len is 1.0, condition len != 1.0 fails
        assertThat(v.getDx()).isEqualTo(1.0);
        assertThat(v.getDy()).isEqualTo(0.0);

        Vector2D v2 = new Vector2D(0.6, 0.8); // length is 1.0
        v2.normalize();
        assertThat(v2.getDx()).isEqualTo(0.6, within(1e-9));
        assertThat(v2.getDy()).isEqualTo(0.8, within(1e-9));
    }

    @Test
    void normalizedShouldReturnNewNormalizedVector() {
        Vector2D original = new Vector2D(3, 4);
        Vector2D normalizedV = original.normalized();

        assertThat(original.getDx()).isEqualTo(3.0); // Original unchanged
        assertThat(original.getDy()).isEqualTo(4.0);

        assertThat(normalizedV.getDx()).isEqualTo(0.6, within(1e-9));
        assertThat(normalizedV.getDy()).isEqualTo(0.8, within(1e-9));
        assertThat(normalizedV.length()).isEqualTo(1.0, within(1e-9));
    }

    @Test
    void normalizedOnZeroVectorShouldReturnZeroVector() {
        Vector2D original = new Vector2D(0, 0);
        Vector2D normalizedV = original.normalized();
        assertThat(normalizedV.getDx()).isEqualTo(0.0);
        assertThat(normalizedV.getDy()).isEqualTo(0.0);
    }

    @Test
    void inverseShouldReturnNewInvertedVector() {
        Vector2D original = new Vector2D(2.5, -1.5);
        Vector2D invertedV = original.inverse();

        assertThat(original.getDx()).isEqualTo(2.5); // Original unchanged
        assertThat(original.getDy()).isEqualTo(-1.5);

        assertThat(invertedV.getDx()).isEqualTo(-2.5);
        assertThat(invertedV.getDy()).isEqualTo(1.5);
    }

    @Test
    void equalsAndHashCodeShouldWorkCorrectly() {
        Vector2D v1 = new Vector2D(1.2, 3.4);
        Vector2D v2 = new Vector2D(1.2, 3.4);
        Vector2D v3 = new Vector2D(5.6, 7.8);
        Vector2D v4 = new Vector2D(1.2, 0.0); // Different dy

        assertThat(v1).isEqualTo(v2)
                      .hasSameHashCodeAs(v2);
        assertThat(v1).isNotEqualTo(v3);
        assertThat(v1.hashCode()).isNotEqualTo(v3.hashCode());
        assertThat(v1).isNotEqualTo(v4);
        assertThat(v1.hashCode()).isNotEqualTo(v4.hashCode());
        assertThat(v1).isNotEqualTo(null);
        assertThat(v1).isNotEqualTo(new Object());
    }

    @Test
    void toStringShouldReturnFormattedString() {
        Vector2D v = new Vector2D(1.0, -2.5);
        assertThat(v.toString()).isEqualTo("Vector2D{dx=1.0, dy=-2.5}");
    }
}