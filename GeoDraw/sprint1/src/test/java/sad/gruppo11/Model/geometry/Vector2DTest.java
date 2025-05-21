package sad.gruppo11.Model.geometry;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

class Vector2DTest {

    private static final double DELTA = 1e-9; // Tolleranza per confronti tra double

    @Test
    @DisplayName("Costruttore con componenti dx, dy dovrebbe inizializzare correttamente")
    void testConstructorWithComponents() {
        Vector2D v = new Vector2D(3.0, -4.0);
        assertThat(v.getDx()).isEqualTo(3.0, within(DELTA));
        assertThat(v.getDy()).isEqualTo(-4.0, within(DELTA));
    }

    @Test
    @DisplayName("Costruttore di copia dovrebbe creare una copia identica ma distinta")
    void testCopyConstructor() {
        Vector2D original = new Vector2D(1.5, 2.5);
        Vector2D copy = new Vector2D(original);

        assertThat(copy.getDx()).isEqualTo(original.getDx(), within(DELTA));
        assertThat(copy.getDy()).isEqualTo(original.getDy(), within(DELTA));
        assertThat(copy).isNotSameAs(original);
        assertThat(copy).isEqualTo(original);
    }

    @Test
    @DisplayName("Getter e Setter per Dx dovrebbero funzionare correttamente")
    void testGetSetDx() {
        Vector2D v = new Vector2D(0, 0);
        v.setDx(5.5);
        assertThat(v.getDx()).isEqualTo(5.5, within(DELTA));
    }

    @Test
    @DisplayName("Getter e Setter per Dy dovrebbero funzionare correttamente")
    void testGetSetDy() {
        Vector2D v = new Vector2D(0, 0);
        v.setDy(-7.5);
        assertThat(v.getDy()).isEqualTo(-7.5, within(DELTA));
    }

    @Test
    @DisplayName("Il metodo length dovrebbe calcolare correttamente la magnitudine")
    void testLength() {
        Vector2D v1 = new Vector2D(3, 4); // Lunghezza 5
        Vector2D v2 = new Vector2D(0, 0); // Lunghezza 0
        Vector2D v3 = new Vector2D(-6, 8); // Lunghezza 10
        Vector2D v4 = new Vector2D(1, 0); // Lunghezza 1
        Vector2D v5 = new Vector2D(0, -1); // Lunghezza 1


        assertThat(v1.length()).isEqualTo(5.0, within(DELTA));
        assertThat(v2.length()).isEqualTo(0.0, within(DELTA));
        assertThat(v3.length()).isEqualTo(10.0, within(DELTA));
        assertThat(v4.length()).isEqualTo(1.0, within(DELTA));
        assertThat(v5.length()).isEqualTo(1.0, within(DELTA));
    }

    @Test
    @DisplayName("Il metodo normalize dovrebbe modificare il vettore rendendolo unitario")
    void testNormalize() {
        Vector2D v1 = new Vector2D(3, 4);
        v1.normalize();
        assertThat(v1.length()).isEqualTo(1.0, within(DELTA));
        assertThat(v1.getDx()).isEqualTo(3.0/5.0, within(DELTA));
        assertThat(v1.getDy()).isEqualTo(4.0/5.0, within(DELTA));

        Vector2D v2 = new Vector2D(0, 0);
        v2.normalize(); // Normalizzare un vettore nullo non dovrebbe cambiarlo (o causare errore)
        assertThat(v2.getDx()).isEqualTo(0.0, within(DELTA));
        assertThat(v2.getDy()).isEqualTo(0.0, within(DELTA));
        assertThat(v2.length()).isEqualTo(0.0, within(DELTA));
    }

    @Test
    @DisplayName("Il metodo normalized dovrebbe restituire un nuovo vettore unitario senza modificare l'originale")
    void testNormalized() {
        Vector2D original = new Vector2D(3, 4);
        Vector2D normalizedCopy = original.normalized();

        // Verifica che l'originale non sia cambiato
        assertThat(original.getDx()).isEqualTo(3.0, within(DELTA));
        assertThat(original.getDy()).isEqualTo(4.0, within(DELTA));
        assertThat(original.length()).isEqualTo(5.0, within(DELTA));

        // Verifica la copia normalizzata
        assertThat(normalizedCopy.length()).isEqualTo(1.0, within(DELTA));
        assertThat(normalizedCopy.getDx()).isEqualTo(3.0/5.0, within(DELTA));
        assertThat(normalizedCopy.getDy()).isEqualTo(4.0/5.0, within(DELTA));
        assertThat(normalizedCopy).isNotSameAs(original);

        Vector2D zeroVector = new Vector2D(0, 0);
        Vector2D normalizedZero = zeroVector.normalized();
        assertThat(normalizedZero.getDx()).isEqualTo(0.0, within(DELTA));
        assertThat(normalizedZero.getDy()).isEqualTo(0.0, within(DELTA));
        assertThat(normalizedZero.length()).isEqualTo(0.0, within(DELTA));
    }

    @Test
    @DisplayName("Il metodo inverse dovrebbe restituire un nuovo vettore con componenti negate")
    void testInverse() {
        Vector2D original = new Vector2D(2.5, -1.5);
        Vector2D inverted = original.inverse();

        // Verifica che l'originale non sia cambiato
        assertThat(original.getDx()).isEqualTo(2.5, within(DELTA));
        assertThat(original.getDy()).isEqualTo(-1.5, within(DELTA));

        // Verifica il vettore invertito
        assertThat(inverted.getDx()).isEqualTo(-2.5, within(DELTA));
        assertThat(inverted.getDy()).isEqualTo(1.5, within(DELTA));
        assertThat(inverted).isNotSameAs(original);

        Vector2D zeroVector = new Vector2D(0, 0);
        Vector2D invertedZero = zeroVector.inverse();
        assertThat(invertedZero.getDx()).isEqualTo(0.0, within(DELTA)); // -0.0 è 0.0
        assertThat(invertedZero.getDy()).isEqualTo(0.0, within(DELTA));
    }

    @Test
    @DisplayName("Il metodo equals dovrebbe confrontare correttamente i vettori")
    void testEquals() {
        Vector2D v1 = new Vector2D(10, 20);
        Vector2D v2 = new Vector2D(10, 20);
        Vector2D v3 = new Vector2D(20, 10);
        Vector2D v4 = new Vector2D(10, 20.000000001);
        Vector2D v5 = new Vector2D(10, 20.0000001);

        assertThat(v1.equals(v2)).isTrue();
        assertThat(v2.equals(v1)).isTrue();
        assertThat(v1.equals(v1)).isTrue();
        assertThat(v1.equals(null)).isFalse();
        assertThat(v1.equals("Not a Vector2D")).isFalse();
        assertThat(v1.equals(v3)).isFalse();

        assertThat(v1.equals(v4)).isFalse();  // Con Double.compare, questa uguaglianza può dipendere dalla precisione
        assertThat(v1.equals(v5)).isFalse(); // Questa differenza dovrebbe essere rilevata
    }

    @Test
    @DisplayName("Il metodo hashCode dovrebbe essere coerente con equals")
    void testHashCode() {
        Vector2D v1 = new Vector2D(10, 20);
        Vector2D v2 = new Vector2D(10, 20);
        Vector2D v3 = new Vector2D(20, 10);

        assertThat(v1.hashCode()).isEqualTo(v2.hashCode());
        assertThat(v1.hashCode()).isNotEqualTo(v3.hashCode());
    }

    @Test
    @DisplayName("toString dovrebbe restituire una rappresentazione stringa significativa")
    void testToString() {
        Vector2D v = new Vector2D(-1.0, 99.9);
        String s = v.toString();
        assertThat(s).contains("Vector2D");
        assertThat(s).contains("dx=-1.0");
        assertThat(s).contains("dy=99.9");
    }
}