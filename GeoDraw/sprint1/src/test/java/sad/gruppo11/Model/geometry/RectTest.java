package sad.gruppo11.Model.geometry;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

class RectTest {

    private static final double DELTA = 1e-9;

    @Test
    @DisplayName("Costruttore con Point2D, width, height dovrebbe inizializzare correttamente e clampare dimensioni")
    void testConstructorWithPointAndDimensions() {
        Point2D tl = new Point2D(10, 20);
        Rect r1 = new Rect(tl, 100, 50);
        assertThat(r1.getTopLeft()).isEqualTo(tl); // Confronta il contenuto, non l'istanza
        assertThat(r1.getWidth()).isEqualTo(100, within(DELTA));
        assertThat(r1.getHeight()).isEqualTo(50, within(DELTA));

        // Testa il clamping per width/height negativi
        Rect r2 = new Rect(tl, -10, -5);
        assertThat(r2.getWidth()).isEqualTo(0, within(DELTA));
        assertThat(r2.getHeight()).isEqualTo(0, within(DELTA));

        // Verifica copia difensiva di topLeft
        Point2D originalTopLeft = new Point2D(1, 1);
        Rect r3 = new Rect(originalTopLeft, 10, 10);
        originalTopLeft.setX(99); // Modifica l'originale
        assertThat(r3.getTopLeft().getX()).isEqualTo(1, within(DELTA)); // Il rect non dovrebbe cambiare
    }

    @Test
    @DisplayName("Costruttore con x, y, width, height dovrebbe inizializzare correttamente")
    void testConstructorWithCoordinatesAndDimensions() {
        Rect r = new Rect(5, 15, 200, 150);
        assertThat(r.getX()).isEqualTo(5, within(DELTA));
        assertThat(r.getY()).isEqualTo(15, within(DELTA));
        assertThat(r.getTopLeft()).isEqualTo(new Point2D(5, 15));
        assertThat(r.getWidth()).isEqualTo(200, within(DELTA));
        assertThat(r.getHeight()).isEqualTo(150, within(DELTA));
    }

    @Test
    @DisplayName("Costruttore con Point2D nullo dovrebbe lanciare IllegalArgumentException")
    void testConstructorWithNullPoint() {
        assertThatThrownBy(() -> new Rect(null, 10, 10))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("TopLeft point cannot be null");
    }

    @Test
    @DisplayName("Costruttore di copia dovrebbe creare una copia identica ma distinta")
    void testCopyConstructor() {
        Rect original = new Rect(new Point2D(1, 2), 30, 40);
        Rect copy = new Rect(original);

        assertThat(copy.getTopLeft()).isEqualTo(original.getTopLeft());
        assertThat(copy.getWidth()).isEqualTo(original.getWidth(), within(DELTA));
        assertThat(copy.getHeight()).isEqualTo(original.getHeight(), within(DELTA));
        assertThat(copy).isNotSameAs(original);
        assertThat(copy.getTopLeft()).isNotSameAs(original.getTopLeft()); // Verifica copia difensiva nel costruttore di copia
        assertThat(copy).isEqualTo(original);
    }

    @Test
    @DisplayName("getTopLeft dovrebbe restituire una copia difensiva")
    void testGetTopLeftDefensiveCopy() {
        Rect rect = new Rect(10, 20, 30, 40);
        Point2D tl1 = rect.getTopLeft();
        tl1.setX(999); // Modifica la copia
        Point2D tl2 = rect.getTopLeft();
        assertThat(tl2.getX()).isEqualTo(10, within(DELTA)); // L'originale nel rect non dovrebbe essere cambiato
    }

    @Test
    @DisplayName("setTopLeft(Point2D) dovrebbe impostare il topLeft e fare copia difensiva")
    void testSetTopLeftWithPoint() {
        Rect rect = new Rect(0, 0, 10, 10);
        Point2D newTl = new Point2D(50, 60);
        rect.setTopLeft(newTl);
        assertThat(rect.getTopLeft()).isEqualTo(newTl);

        newTl.setX(999); // Modifica l'originale newTl
        assertThat(rect.getTopLeft().getX()).isEqualTo(50, within(DELTA)); // Il rect non dovrebbe cambiare

        assertThatThrownBy(() -> rect.setTopLeft((Point2D) null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("TopLeft point cannot be null");
    }

    @Test
    @DisplayName("setTopLeft(double, double) dovrebbe impostare le coordinate di topLeft")
    void testSetTopLeftWithCoordinates() {
        Rect rect = new Rect(0, 0, 10, 10);
        rect.setTopLeft(70, 80);
        assertThat(rect.getTopLeft()).isEqualTo(new Point2D(70, 80));
    }

    @ParameterizedTest
    @DisplayName("setWidth dovrebbe clampare la larghezza a valori non negativi")
    @CsvSource({ "100.0, 100.0", "-50.0, 0.0", "0.0, 0.0" })
    void testSetWidth(double inputValue, double expectedValue) {
        Rect rect = new Rect(0, 0, 10, 10);
        rect.setWidth(inputValue);
        assertThat(rect.getWidth()).isEqualTo(expectedValue, within(DELTA));
    }

    @ParameterizedTest
    @DisplayName("setHeight dovrebbe clampare l'altezza a valori non negativi")
    @CsvSource({ "75.0, 75.0", "-20.0, 0.0", "0.0, 0.0" })
    void testSetHeight(double inputValue, double expectedValue) {
        Rect rect = new Rect(0, 0, 10, 10);
        rect.setHeight(inputValue);
        assertThat(rect.getHeight()).isEqualTo(expectedValue, within(DELTA));
    }

    @Test
    @DisplayName("Metodi getX, getY, getRight, getBottom dovrebbero restituire valori corretti")
    void testDerivedCoordinates() {
        Rect rect = new Rect(10, 20, 100, 50);
        assertThat(rect.getX()).isEqualTo(10, within(DELTA));
        assertThat(rect.getY()).isEqualTo(20, within(DELTA));
        assertThat(rect.getRight()).isEqualTo(110, within(DELTA)); // 10 + 100
        assertThat(rect.getBottom()).isEqualTo(70, within(DELTA));  // 20 + 50
    }

    @Test
    @DisplayName("getBottomRight dovrebbe restituire il punto corretto")
    void testGetBottomRight() {
        Rect rect = new Rect(10, 20, 100, 50);
        assertThat(rect.getBottomRight()).isEqualTo(new Point2D(110, 70));
    }

    @Test
    @DisplayName("getCenter dovrebbe restituire il punto centrale corretto")
    void testGetCenter() {
        Rect rect = new Rect(10, 20, 100, 50); // Center (10+50, 20+25) = (60, 45)
        assertThat(rect.getCenter()).isEqualTo(new Point2D(60, 45));

        Rect rectZeroSize = new Rect(10, 20, 0, 0);
        assertThat(rectZeroSize.getCenter()).isEqualTo(new Point2D(10, 20));
    }

    @Test
    @DisplayName("contains dovrebbe verificare correttamente se un punto è nel rettangolo (bordi inclusi)")
    void testContains() {
        Rect rect = new Rect(0, 0, 100, 50); // x from 0 to 100, y from 0 to 50

        // Punti interni
        assertThat(rect.contains(new Point2D(50, 25))).isTrue();
        assertThat(rect.contains(new Point2D(0, 0))).isTrue();   // Top-left corner
        assertThat(rect.contains(new Point2D(100, 0))).isTrue(); // Top-right corner
        assertThat(rect.contains(new Point2D(0, 50))).isTrue();   // Bottom-left corner
        assertThat(rect.contains(new Point2D(100, 50))).isTrue();// Bottom-right corner
        assertThat(rect.contains(new Point2D(100, 25))).isTrue();// On right edge
        assertThat(rect.contains(new Point2D(0, 25))).isTrue();  // On left edge
        assertThat(rect.contains(new Point2D(50, 0))).isTrue();  // On top edge
        assertThat(rect.contains(new Point2D(50, 50))).isTrue(); // On bottom edge

        // Punti esterni
        assertThat(rect.contains(new Point2D(-1, 25))).isFalse();
        assertThat(rect.contains(new Point2D(101, 25))).isFalse();
        assertThat(rect.contains(new Point2D(50, -1))).isFalse();
        assertThat(rect.contains(new Point2D(50, 51))).isFalse();
        assertThat(rect.contains(new Point2D(100.000001, 25))).isFalse();

        // Punto null
        assertThat(rect.contains(null)).isFalse();

        // Rettangolo di dimensione zero
        Rect zeroRect = new Rect(10, 10, 0, 0);
        assertThat(zeroRect.contains(new Point2D(10,10))).isTrue(); // Un punto può essere contenuto
        assertThat(zeroRect.contains(new Point2D(10,11))).isFalse();
    }

    @Test
    @DisplayName("translate dovrebbe spostare il topLeft del rettangolo")
    void testTranslate() {
        Rect rect = new Rect(10, 20, 30, 40);
        rect.translate(5, -10);
        assertThat(rect.getTopLeft()).isEqualTo(new Point2D(15, 10));
        assertThat(rect.getWidth()).isEqualTo(30, within(DELTA)); // Le dimensioni non cambiano
        assertThat(rect.getHeight()).isEqualTo(40, within(DELTA));
    }

    @Test
    @DisplayName("translated dovrebbe restituire un nuovo rettangolo traslato senza modificare l'originale")
    void testTranslated() {
        Rect original = new Rect(10, 20, 30, 40);
        Rect translatedRect = original.translated(5, -10);

        // Verifica originale
        assertThat(original.getTopLeft()).isEqualTo(new Point2D(10, 20));
        assertThat(original.getWidth()).isEqualTo(30, within(DELTA));
        assertThat(original.getHeight()).isEqualTo(40, within(DELTA));

        // Verifica nuovo rettangolo
        assertThat(translatedRect.getTopLeft()).isEqualTo(new Point2D(15, 10));
        assertThat(translatedRect.getWidth()).isEqualTo(30, within(DELTA));
        assertThat(translatedRect.getHeight()).isEqualTo(40, within(DELTA));
        assertThat(translatedRect).isNotSameAs(original);
    }

    @Test
    @DisplayName("equals dovrebbe confrontare correttamente i rettangoli")
    void testEquals() {
        Rect r1 = new Rect(1, 1, 10, 10);
        Rect r2 = new Rect(new Point2D(1, 1), 10, 10);
        Rect r3 = new Rect(2, 1, 10, 10); // topLeft.x diverso
        Rect r4 = new Rect(1, 1, 11, 10); // width diverso
        Rect r5 = new Rect(1, 1, 10, 11); // height diverso

        assertThat(r1.equals(r2)).isTrue();
        assertThat(r2.equals(r1)).isTrue();
        assertThat(r1.equals(r1)).isTrue();
        assertThat(r1.equals(null)).isFalse();
        assertThat(r1.equals("Not a Rect")).isFalse();

        assertThat(r1.equals(r3)).isFalse();
        assertThat(r1.equals(r4)).isFalse();
        assertThat(r1.equals(r5)).isFalse();
    }

    @Test
    @DisplayName("hashCode dovrebbe essere coerente con equals")
    void testHashCode() {
        Rect r1 = new Rect(1, 1, 10, 10);
        Rect r2 = new Rect(new Point2D(1, 1), 10, 10);
        Rect r3 = new Rect(2, 1, 10, 10);

        assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
        assertThat(r1.hashCode()).isNotEqualTo(r3.hashCode());
    }

    @Test
    @DisplayName("toString dovrebbe restituire una rappresentazione stringa significativa")
    void testToString() {
        Rect r = new Rect(new Point2D(5.5, 6.6), 100.1, 200.2);
        String s = r.toString();
        assertThat(s).contains("Rect");
        assertThat(s).contains("topLeft=Point2D{x=5.5, y=6.6}");
        assertThat(s).contains("width=100.1");
        assertThat(s).contains("height=200.2");
    }
}