package sad.gruppo11.Model.geometry;

import org.junit.jupiter.api.Test;

import sad.gruppo11.Model.geometry.ColorData;

import static org.assertj.core.api.Assertions.*;

public class ColorDataTest {

    @Test
    void constructorShouldSetValuesAndClamp() {
        ColorData color = new ColorData(100, 150, 200, 0.5);
        assertThat(color.getR()).isEqualTo(100);
        assertThat(color.getG()).isEqualTo(150);
        assertThat(color.getB()).isEqualTo(200);
        assertThat(color.getA()).isEqualTo(0.5);

        ColorData clampedColor = new ColorData(-10, 300, 255, 1.5);
        assertThat(clampedColor.getR()).isEqualTo(0);
        assertThat(clampedColor.getG()).isEqualTo(255);
        assertThat(clampedColor.getB()).isEqualTo(255);
        assertThat(clampedColor.getA()).isEqualTo(1.0);

        ColorData clampedAlphaNegative = new ColorData(50, 50, 50, -0.5);
        assertThat(clampedAlphaNegative.getA()).isEqualTo(0.0);
    }

    @Test
    void copyConstructorShouldCopyValues() {
        ColorData original = new ColorData(10, 20, 30, 0.25);
        ColorData copy = new ColorData(original);

        assertThat(copy.getR()).isEqualTo(original.getR());
        assertThat(copy.getG()).isEqualTo(original.getG());
        assertThat(copy.getB()).isEqualTo(original.getB());
        assertThat(copy.getA()).isEqualTo(original.getA());
        assertThat(copy).isEqualTo(original);
        assertThat(copy).isNotSameAs(original);
    }

    @Test
    void copyConstructorShouldThrowNullPointerExceptionForNullInput() {
        assertThatNullPointerException().isThrownBy(() -> new ColorData(null))
            .withMessageContaining("Other ColorData cannot be null");
    }

    @Test
    void staticColorInstancesShouldBeCorrect() {
        assertThat(ColorData.BLACK).isEqualTo(new ColorData(0, 0, 0, 1.0));
        assertThat(ColorData.WHITE).isEqualTo(new ColorData(255, 255, 255, 1.0));
        assertThat(ColorData.RED).isEqualTo(new ColorData(255, 0, 0, 1.0));
        assertThat(ColorData.GREEN).isEqualTo(new ColorData(0, 255, 0, 1.0));
        assertThat(ColorData.BLUE).isEqualTo(new ColorData(0, 0, 255, 1.0));
        assertThat(ColorData.YELLOW).isEqualTo(new ColorData(255, 255, 0, 1.0));
        assertThat(ColorData.TRANSPARENT).isEqualTo(new ColorData(0, 0, 0, 0.0));
    }

    @Test
    void equalsAndHashCodeShouldWorkCorrectly() {
        ColorData color1 = new ColorData(50, 100, 150, 0.75);
        ColorData color2 = new ColorData(50, 100, 150, 0.75);
        ColorData color3 = new ColorData(0, 0, 0, 1.0);
        ColorData color4 = new ColorData(50, 100, 150, 0.5); // Different alpha

        assertThat(color1).isEqualTo(color2)
                         .hasSameHashCodeAs(color2);
        assertThat(color1).isNotEqualTo(color3);
        assertThat(color1.hashCode()).isNotEqualTo(color3.hashCode());
        assertThat(color1).isNotEqualTo(color4);
        assertThat(color1.hashCode()).isNotEqualTo(color4.hashCode());
        assertThat(color1).isNotEqualTo(null);
        assertThat(color1).isNotEqualTo(new Object());
    }

    @Test
    void toStringShouldReturnFormattedString() {
        ColorData color = new ColorData(255, 128, 0, 0.75);
        // Using a lenient check for floating point formatting
        assertThat(color.toString()).startsWith("ColorData{r=255, g=128, b=0, a=")
                                     .endsWith("}");
        assertThat(color.toString()).containsAnyOf("0,75", "0.75"); // Locale dependent for comma/dot
    }
}