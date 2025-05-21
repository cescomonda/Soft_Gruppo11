package sad.gruppo11.Model.geometry; // Stesso package della classe da testare

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*; // Per assertEquals, assertNotEquals, assertTrue, ecc.
import static org.assertj.core.api.Assertions.*; // Per AssertJ (assertThat)

class Point2DTest {

    private static final double DELTA = 1e-9; // Tolleranza per confronti tra double

    @Test
    @DisplayName("Costruttore con coordinate dovrebbe inizializzare correttamente x e y")
    void testConstructorWithCoordinates() {
        Point2D p = new Point2D(10.5, -5.2);
        assertThat(p.getX()).isEqualTo(10.5, within(DELTA));
        assertThat(p.getY()).isEqualTo(-5.2, within(DELTA));
    }

    @Test
    @DisplayName("Costruttore di copia dovrebbe creare una copia identica ma distinta")
    void testCopyConstructor() {
        Point2D original = new Point2D(7.0, 3.0);
        Point2D copy = new Point2D(original);

        assertThat(copy.getX()).isEqualTo(original.getX(), within(DELTA));
        assertThat(copy.getY()).isEqualTo(original.getY(), within(DELTA));
        assertThat(copy).isNotSameAs(original); // Oggetti diversi
        assertThat(copy).isEqualTo(original);   // Contenuto uguale (grazie a equals)
    }

    @Test
    @DisplayName("Getter e Setter per X dovrebbero funzionare correttamente")
    void testGetSetX() {
        Point2D p = new Point2D(0, 0);
        p.setX(15.75);
        assertThat(p.getX()).isEqualTo(15.75, within(DELTA));
    }

    @Test
    @DisplayName("Getter e Setter per Y dovrebbero funzionare correttamente")
    void testGetSetY() {
        Point2D p = new Point2D(0, 0);
        p.setY(-20.0);
        assertThat(p.getY()).isEqualTo(-20.0, within(DELTA));
    }

    @Test
    @DisplayName("Il metodo distance dovrebbe calcolare correttamente la distanza Euclidea")
    void testDistance() {
        Point2D p1 = new Point2D(1, 1);
        Point2D p2 = new Point2D(4, 5); // dx=3, dy=4, distance=5
        Point2D p3 = new Point2D(1, 1); // Distanza zero

        assertThat(p1.distance(p2)).isEqualTo(5.0, within(DELTA));
        assertThat(p2.distance(p1)).isEqualTo(5.0, within(DELTA)); // Simmetria
        assertThat(p1.distance(p3)).isEqualTo(0.0, within(DELTA)); // Distanza da sé stesso (o punto coincidente)
    }

    @Test
    @DisplayName("Il metodo distance dovrebbe gestire coordinate negative")
    void testDistanceWithNegativeCoordinates() {
        Point2D p1 = new Point2D(-1, -2);
        Point2D p2 = new Point2D(-4, -6); // dx = -3, dy = -4, distance = 5
        assertThat(p1.distance(p2)).isEqualTo(5.0, within(DELTA));
    }

    @Test
    @DisplayName("Il metodo translate dovrebbe spostare il punto correttamente")
    void testTranslate() {
        Point2D p = new Point2D(10, 20);
        p.translate(5, -10);
        assertThat(p.getX()).isEqualTo(15.0, within(DELTA));
        assertThat(p.getY()).isEqualTo(10.0, within(DELTA));

        p.translate(0, 0); // Traslazione nulla
        assertThat(p.getX()).isEqualTo(15.0, within(DELTA));
        assertThat(p.getY()).isEqualTo(10.0, within(DELTA));

        p.translate(-15, -10); // Riporta all'origine
         assertThat(p.getX()).isEqualTo(0.0, within(DELTA));
        assertThat(p.getY()).isEqualTo(0.0, within(DELTA));
    }

    @Test
    @DisplayName("Il metodo equals dovrebbe confrontare correttamente i punti")
    void testEquals() {
        Point2D p1 = new Point2D(2.5, 3.5);
        Point2D p2 = new Point2D(2.5, 3.5);
        Point2D p3 = new Point2D(3.5, 2.5);
        Point2D p4 = new Point2D(2.5, 3.5000000001); // Leggermente diverso
        Point2D p5 = new Point2D(2.5, 3.50000001); // Ancora leggermente diverso

        assertThat(p1.equals(p2)).isTrue();       // p1 e p2 sono uguali
        assertThat(p2.equals(p1)).isTrue();       // Simmetria di equals
        assertThat(p1.equals(p1)).isTrue();       // Riflessività
        assertThat(p1.equals(null)).isFalse();    // Confronto con null
        assertThat(p1.equals("Not a Point2D")).isFalse(); // Confronto con tipo diverso
        assertThat(p1.equals(p3)).isFalse();      // p1 e p3 sono diversi

        // Test con piccole differenze nei double
        // L'equals standard con Double.compare() è preciso, quindi queste dovrebbero essere false
        // se la differenza è oltre la precisione intrinseca del double.
        // Se p4 e p5 fossero considerati uguali, l'equals dovrebbe usare una tolleranza.
        // Il tuo equals è preciso, quindi va bene così.
        assertThat(p1.equals(p4)).isFalse(); // Per via di Double.compare, se la rappresentazione binaria è la stessa
        assertThat(p1.equals(p5)).isFalse(); // Qui la differenza è abbastanza grande da essere distinta

    }

    @Test
    @DisplayName("Il metodo hashCode dovrebbe essere coerente con equals")
    void testHashCode() {
        Point2D p1 = new Point2D(2.5, 3.5);
        Point2D p2 = new Point2D(2.5, 3.5);
        Point2D p3 = new Point2D(3.5, 2.5);

        assertThat(p1.hashCode()).isEqualTo(p2.hashCode()); // Se p1.equals(p2) è true, allora p1.hashCode() == p2.hashCode()
        assertThat(p1.hashCode()).isNotEqualTo(p3.hashCode()); // Non è un requisito stretto, ma di solito è così se non uguali
    }

    @Test
    @DisplayName("toString dovrebbe restituire una rappresentazione stringa significativa")
    void testToString() {
        Point2D p = new Point2D(1.2, 3.4);
        String s = p.toString();
        assertThat(s).contains("Point2D");
        assertThat(s).contains("x=1.2");
        assertThat(s).contains("y=3.4");
    }
}