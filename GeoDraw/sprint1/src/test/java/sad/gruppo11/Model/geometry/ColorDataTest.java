package sad.gruppo11.Model.geometry;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

class ColorDataTest {

    private static final double DELTA = 1e-9; // Tolleranza per confronti double (per alpha)

    @Test
    @DisplayName("Costruttore con componenti RGBA dovrebbe inizializzare e clampare correttamente")
    void testConstructorWithComponents() {
        // Valori validi
        ColorData c1 = new ColorData(100, 150, 200, 0.5);
        assertThat(c1.getR()).isEqualTo(100);
        assertThat(c1.getG()).isEqualTo(150);
        assertThat(c1.getB()).isEqualTo(200);
        assertThat(c1.getA()).isEqualTo(0.5, within(DELTA));

        // Valori RGB fuori range (dovrebbero essere clampati)
        ColorData c2 = new ColorData(300, -50, 255, 0.7);
        assertThat(c2.getR()).isEqualTo(255); // Clampato da 300
        assertThat(c2.getG()).isEqualTo(0);   // Clampato da -50
        assertThat(c2.getB()).isEqualTo(255);
        assertThat(c2.getA()).isEqualTo(0.7, within(DELTA));

        // Valori Alpha fuori range (dovrebbero essere clampati)
        ColorData c3 = new ColorData(10, 20, 30, 1.5);
        assertThat(c3.getA()).isEqualTo(1.0, within(DELTA)); // Clampato da 1.5

        ColorData c4 = new ColorData(10, 20, 30, -0.5);
        assertThat(c4.getA()).isEqualTo(0.0, within(DELTA)); // Clampato da -0.5
    }

    @Test
    @DisplayName("Costruttore di copia dovrebbe creare una copia identica ma distinta")
    void testCopyConstructor() {
        ColorData original = new ColorData(50, 100, 150, 0.8);
        ColorData copy = new ColorData(original);

        assertThat(copy.getR()).isEqualTo(original.getR());
        assertThat(copy.getG()).isEqualTo(original.getG());
        assertThat(copy.getB()).isEqualTo(original.getB());
        assertThat(copy.getA()).isEqualTo(original.getA(), within(DELTA));
        assertThat(copy).isNotSameAs(original);
        assertThat(copy).isEqualTo(original); // Grazie a equals
    }

    @ParameterizedTest
    @DisplayName("Setter per R dovrebbe clampare i valori")
    @CsvSource({
            "128, 128",   // In range
            "-10, 0",     // Sotto il range
            "300, 255"    // Sopra il range
    })
    void testSetR(int inputValue, int expectedValue) {
        ColorData color = new ColorData(0, 0, 0, 1.0);
        color.setR(inputValue);
        assertThat(color.getR()).isEqualTo(expectedValue);
    }

    @ParameterizedTest
    @DisplayName("Setter per G dovrebbe clampare i valori")
    @CsvSource({
            "128, 128",
            "-10, 0",
            "300, 255"
    })
    void testSetG(int inputValue, int expectedValue) {
        ColorData color = new ColorData(0, 0, 0, 1.0);
        color.setG(inputValue);
        assertThat(color.getG()).isEqualTo(expectedValue);
    }

    @ParameterizedTest
    @DisplayName("Setter per B dovrebbe clampare i valori")
    @CsvSource({
            "128, 128",
            "-10, 0",
            "300, 255"
    })
    void testSetB(int inputValue, int expectedValue) {
        ColorData color = new ColorData(0, 0, 0, 1.0);
        color.setB(inputValue);
        assertThat(color.getB()).isEqualTo(expectedValue);
    }

    @ParameterizedTest
    @DisplayName("Setter per A dovrebbe clampare i valori")
    @CsvSource({
            "0.5, 0.5",     // In range
            "-0.2, 0.0",    // Sotto il range
            "1.8, 1.0"      // Sopra il range
    })
    void testSetA(double inputValue, double expectedValue) {
        ColorData color = new ColorData(0, 0, 0, 1.0);
        color.setA(inputValue);
        assertThat(color.getA()).isEqualTo(expectedValue, within(DELTA));
    }

    @Test
    @DisplayName("Il metodo equals dovrebbe confrontare correttamente i ColorData")
    void testEquals() {
        ColorData c1 = new ColorData(10, 20, 30, 0.1);
        ColorData c2 = new ColorData(10, 20, 30, 0.1);
        ColorData c3 = new ColorData(11, 20, 30, 0.1); // R diverso
        ColorData c4 = new ColorData(10, 21, 30, 0.1); // G diverso
        ColorData c5 = new ColorData(10, 20, 31, 0.1); // B diverso
        ColorData c6 = new ColorData(10, 20, 30, 0.2); // A diverso

        assertThat(c1.equals(c2)).isTrue();
        assertThat(c2.equals(c1)).isTrue();
        assertThat(c1.equals(c1)).isTrue();
        assertThat(c1.equals(null)).isFalse();
        assertThat(c1.equals("Not a ColorData")).isFalse();

        assertThat(c1.equals(c3)).isFalse();
        assertThat(c1.equals(c4)).isFalse();
        assertThat(c1.equals(c5)).isFalse();
        assertThat(c1.equals(c6)).isFalse();
    }

    @Test
    @DisplayName("Il metodo hashCode dovrebbe essere coerente con equals")
    void testHashCode() {
        ColorData c1 = new ColorData(10, 20, 30, 0.1);
        ColorData c2 = new ColorData(10, 20, 30, 0.1);
        ColorData c3 = new ColorData(11, 20, 30, 0.1);

        assertThat(c1.hashCode()).isEqualTo(c2.hashCode());
        assertThat(c1.hashCode()).isNotEqualTo(c3.hashCode());
    }

    @Test
    @DisplayName("toString dovrebbe restituire una rappresentazione stringa significativa")
    void testToString() {
        ColorData c = new ColorData(255, 128, 0, 0.75);
        String s = c.toString();
        assertThat(s).contains("ColorData");
        assertThat(s).contains("r=255");
        assertThat(s).contains("g=128");
        assertThat(s).contains("b=0");
        assertThat(s).contains("a=0.75");
    }

    @Test
    @DisplayName("Le costanti colore predefinite dovrebbero avere i valori corretti")
    void testPredefinedColors() {
        assertThat(ColorData.BLACK).isEqualTo(new ColorData(0, 0, 0, 1.0));
        assertThat(ColorData.WHITE).isEqualTo(new ColorData(255, 255, 255, 1.0));
        assertThat(ColorData.RED).isEqualTo(new ColorData(255, 0, 0, 1.0));
        assertThat(ColorData.GREEN).isEqualTo(new ColorData(0, 255, 0, 1.0));
        assertThat(ColorData.BLUE).isEqualTo(new ColorData(0, 0, 255, 1.0));
        assertThat(ColorData.TRANSPARENT).isEqualTo(new ColorData(0, 0, 0, 0.0));
    }
}