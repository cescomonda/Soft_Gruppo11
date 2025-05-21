package sad.gruppo11.Model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mockito; // Per mockare ShapeVisitor
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Rect;
import sad.gruppo11.Model.geometry.Vector2D;
import sad.gruppo11.View.ShapeVisitor; // Importa l'interfaccia del visitor

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

class LineSegmentTest {

    private static final double DELTA = 1e-9;

    private Point2D p1 = new Point2D(10, 20);
    private Point2D p2 = new Point2D(40, 60); // dx=30, dy=40, length=50

    @Test
    @DisplayName("Costruttore dovrebbe inizializzare start, end, ID e strokeColor default")
    void testConstructor() {
        LineSegment line = new LineSegment(p1, p2);

        assertThat(line.getId()).isNotNull();
        assertThat(line.getStartPoint()).isEqualTo(p1);
        assertThat(line.getEndPoint()).isEqualTo(p2);
        assertThat(line.getStartPoint()).isNotSameAs(p1); // Verifica copia difensiva
        assertThat(line.getEndPoint()).isNotSameAs(p2);   // Verifica copia difensiva
        assertThat(line.getStrokeColor()).isEqualTo(ColorData.BLACK);
        assertThat(line.getFillColor()).isNull(); // Linee non hanno fillColor
    }

    @Test
    @DisplayName("Costruttore dovrebbe lanciare eccezione per punti null")
    void testConstructorNullPoints() {
        assertThatThrownBy(() -> new LineSegment(null, p2))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Start and end points cannot be null");
        assertThatThrownBy(() -> new LineSegment(p1, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Start and end points cannot be null");
    }

    @Test
    @DisplayName("move dovrebbe traslare startPoint e endPoint")
    void testMove() {
        LineSegment line = new LineSegment(p1, p2);
        Vector2D v = new Vector2D(5, -5);
        line.move(v);

        assertThat(line.getStartPoint()).isEqualTo(new Point2D(15, 15));
        assertThat(line.getEndPoint()).isEqualTo(new Point2D(45, 55));
    }

    @Test
    @DisplayName("move con vettore nullo non dovrebbe fare nulla")
    void testMoveNullVector() {
        LineSegment line = new LineSegment(p1, p2);
        Point2D originalStart = line.getStartPoint();
        Point2D originalEnd = line.getEndPoint();
        line.move(null); // Dovrebbe gestire null senza errori

        assertThat(line.getStartPoint()).isEqualTo(originalStart);
        assertThat(line.getEndPoint()).isEqualTo(originalEnd);
    }

    @Test
    @DisplayName("resize dovrebbe impostare start e end point basati sui bounds")
    void testResize() {
        LineSegment line = new LineSegment(p1, p2);
        Rect newBounds = new Rect(new Point2D(0, 0), 10, 10);
        line.resize(newBounds);

        assertThat(line.getStartPoint()).isEqualTo(new Point2D(0, 0));   // newBounds.getTopLeft()
        assertThat(line.getEndPoint()).isEqualTo(new Point2D(10, 10)); // newBounds.getBottomRight()
    }

    @Test
    @DisplayName("resize con bounds nulli dovrebbe lanciare eccezione")
    void testResizeNullBounds() {
        LineSegment line = new LineSegment(p1, p2);
        assertThatThrownBy(() -> line.resize(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("New bounds cannot be null");
    }

    @Test
    @DisplayName("setStrokeColor dovrebbe cambiare il colore del tratto")
    void testSetStrokeColor() {
        LineSegment line = new LineSegment(p1, p2);
        ColorData newColor = ColorData.RED;
        line.setStrokeColor(newColor);
        assertThat(line.getStrokeColor()).isEqualTo(newColor);
        assertThat(line.getStrokeColor()).isNotSameAs(newColor); // Verifica copia difensiva
    }

    @Test
    @DisplayName("setStrokeColor con colore nullo dovrebbe lanciare eccezione")
    void testSetStrokeColorNull() {
        LineSegment line = new LineSegment(p1, p2);
        assertThatThrownBy(() -> line.setStrokeColor(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Stroke color cannot be null");
    }

    @Test
    @DisplayName("setFillColor non dovrebbe avere effetti e getFillColor dovrebbe restituire null")
    void testFillColor() {
        LineSegment line = new LineSegment(p1, p2);
        ColorData initialStroke = line.getStrokeColor();

        line.setFillColor(ColorData.GREEN); // Non dovrebbe fare nulla
        assertThat(line.getFillColor()).isNull();
        assertThat(line.getStrokeColor()).isEqualTo(initialStroke); // Verifica che stroke non sia cambiato
    }

    @Test
    @DisplayName("contains dovrebbe identificare punti sulla linea e vicini, e punti esterni")
    void testContains() {
        LineSegment line = new LineSegment(new Point2D(0, 0), new Point2D(10, 10));

        // Punti sulla linea
        assertThat(line.contains(new Point2D(0, 0))).isTrue();   // Start point
        assertThat(line.contains(new Point2D(10, 10))).isTrue(); // End point
        assertThat(line.contains(new Point2D(5, 5))).isTrue();   // Midpoint

        // Punti vicini (entro epsilon, il tuo epsilon è 0.5)
        assertThat(line.contains(new Point2D(5.05, 5.0))).isTrue();
        assertThat(line.contains(new Point2D(4.95, 5.0))).isTrue();

        // Punti esterni
        assertThat(line.contains(new Point2D(11, 11))).isFalse();
        assertThat(line.contains(new Point2D(5, 8))).isFalse(); // Fuori dalla linea (distanza > epsilon)
        assertThat(line.contains(new Point2D(-1, -1))).isFalse();
        assertThat(line.contains(null)).isFalse();

        // Linea orizzontale
        LineSegment hLine = new LineSegment(new Point2D(0, 5), new Point2D(10, 5));
        assertThat(hLine.contains(new Point2D(5, 5))).isTrue();
        assertThat(hLine.contains(new Point2D(5, 5.05))).isTrue();
        assertThat(hLine.contains(new Point2D(5, 4.95))).isTrue();
        assertThat(hLine.contains(new Point2D(5, 7))).isFalse();

        // Linea verticale
        LineSegment vLine = new LineSegment(new Point2D(5, 0), new Point2D(5, 10));
        assertThat(vLine.contains(new Point2D(5, 5))).isTrue();
        assertThat(vLine.contains(new Point2D(5.05, 5))).isTrue();
        assertThat(vLine.contains(new Point2D(4.95, 5))).isTrue();
        assertThat(vLine.contains(new Point2D(6, 5))).isTrue();

        // Linea che è un punto
        LineSegment pointLine = new LineSegment(new Point2D(1,1), new Point2D(1,1));
        assertThat(pointLine.contains(new Point2D(1,1))).isTrue();
        assertThat(pointLine.contains(new Point2D(1.05,1.05))).isTrue(); // Entro epsilon
        assertThat(pointLine.contains(new Point2D(1.5,1.5))).isFalse(); // Fuori epsilon
    }

    @Test
    @DisplayName("accept dovrebbe chiamare il metodo visit corretto del visitor")
    void testAcceptVisitor() {
        LineSegment line = new LineSegment(p1, p2);
        ShapeVisitor mockVisitor = Mockito.mock(ShapeVisitor.class); // Usa Mockito
        line.accept(mockVisitor);
        Mockito.verify(mockVisitor).visit(line); // Verifica che visit(LineSegment) sia stato chiamato
        Mockito.verifyNoMoreInteractions(mockVisitor);

        line.accept(null); // Non dovrebbe lanciare eccezioni
    }

    @Test
    @DisplayName("cloneShape dovrebbe creare una copia con lo stesso ID e proprietà")
    void testCloneShape() {
        LineSegment original = new LineSegment(p1, p2);
        original.setStrokeColor(ColorData.BLUE);
        Shape clonedShape = original.cloneShape();

        assertThat(clonedShape).isInstanceOf(LineSegment.class);
        LineSegment clonedLine = (LineSegment) clonedShape;

        assertThat(clonedLine.getId()).isEqualTo(original.getId());
        assertThat(clonedLine.getStartPoint()).isEqualTo(original.getStartPoint());
        assertThat(clonedLine.getEndPoint()).isEqualTo(original.getEndPoint());
        assertThat(clonedLine.getStrokeColor()).isEqualTo(original.getStrokeColor());
        assertThat(clonedLine).isNotSameAs(original);
        assertThat(clonedLine.getStartPoint()).isNotSameAs(original.getStartPoint()); // Copie difensive
    }

    @Test
    @DisplayName("cloneWithNewId dovrebbe creare una copia con un nuovo ID e stesse proprietà")
    void testCloneWithNewId() {
        LineSegment original = new LineSegment(p1, p2);
        original.setStrokeColor(ColorData.GREEN);
        Shape clonedShape = original.cloneWithNewId();

        assertThat(clonedShape).isInstanceOf(LineSegment.class);
        LineSegment clonedLine = (LineSegment) clonedShape;

        assertThat(clonedLine.getId()).isNotNull();
        assertThat(clonedLine.getId()).isNotEqualTo(original.getId()); // ID deve essere diverso
        assertThat(clonedLine.getStartPoint()).isEqualTo(original.getStartPoint());
        assertThat(clonedLine.getEndPoint()).isEqualTo(original.getEndPoint());
        assertThat(clonedLine.getStrokeColor()).isEqualTo(original.getStrokeColor());
        assertThat(clonedLine).isNotSameAs(original);
    }


    @Test
    @DisplayName("getBounds dovrebbe restituire il rettangolo di delimitazione corretto")
    void testGetBounds() {
        LineSegment line1 = new LineSegment(new Point2D(10, 20), new Point2D(40, 60));
        Rect bounds1 = line1.getBounds();
        assertThat(bounds1.getX()).isEqualTo(10, within(DELTA));
        assertThat(bounds1.getY()).isEqualTo(20, within(DELTA));
        assertThat(bounds1.getWidth()).isEqualTo(30, within(DELTA)); // 40-10
        assertThat(bounds1.getHeight()).isEqualTo(40, within(DELTA)); // 60-20

        // Linea con punti invertiti
        LineSegment line2 = new LineSegment(new Point2D(40, 60), new Point2D(10, 20));
        Rect bounds2 = line2.getBounds();
        assertThat(bounds2).isEqualTo(bounds1); // Dovrebbe essere lo stesso bounds

        // Linea orizzontale
        LineSegment lineH = new LineSegment(new Point2D(5, 10), new Point2D(25, 10));
        Rect boundsH = lineH.getBounds();
        assertThat(boundsH.getX()).isEqualTo(5, within(DELTA));
        assertThat(boundsH.getY()).isEqualTo(10, within(DELTA));
        assertThat(boundsH.getWidth()).isEqualTo(20, within(DELTA));
        assertThat(boundsH.getHeight()).isEqualTo(0, within(DELTA));

        // Linea verticale
        LineSegment lineV = new LineSegment(new Point2D(15, 0), new Point2D(15, 30));
        Rect boundsV = lineV.getBounds();
        assertThat(boundsV.getX()).isEqualTo(15, within(DELTA));
        assertThat(boundsV.getY()).isEqualTo(0, within(DELTA));
        assertThat(boundsV.getWidth()).isEqualTo(0, within(DELTA));
        assertThat(boundsV.getHeight()).isEqualTo(30, within(DELTA));
    }

    @Test
    @DisplayName("getStrokeColor dovrebbe restituire una copia difensiva")
    void testGetStrokeColorDefensiveCopy() {
        LineSegment line = new LineSegment(p1, p2);
        ColorData color1 = line.getStrokeColor();
        color1.setR(123); // Modifica la copia
        ColorData color2 = line.getStrokeColor();
        assertThat(color2.getR()).isNotEqualTo(123); // L'originale non dovrebbe cambiare
        assertThat(color2).isEqualTo(ColorData.BLACK);
    }

    @Test
    @DisplayName("getStartPoint e getEndPoint dovrebbero restituire copie difensive")
    void testGetPointsDefensiveCopy() {
        LineSegment line = new LineSegment(new Point2D(1,1), new Point2D(2,2));
        Point2D start = line.getStartPoint();
        Point2D end = line.getEndPoint();

        start.setX(99);
        end.setX(88);

        assertThat(line.getStartPoint().getX()).isEqualTo(1, within(DELTA));
        assertThat(line.getEndPoint().getX()).isEqualTo(2, within(DELTA));
    }

    @Test
    @DisplayName("equals dovrebbe confrontare basandosi sull'ID")
    void testEquals() {
        LineSegment line1 = new LineSegment(p1, p2);
        // Per testare equals basato su ID, dobbiamo creare una linea con lo stesso ID,
        // il che è possibile solo attraverso cloneShape().
        LineSegment line2 = (LineSegment) line1.cloneShape(); // Stesso ID
        LineSegment line3 = new LineSegment(p1, p2);          // ID diverso

        assertThat(line1.equals(line2)).isTrue(); // Stesso ID
        assertThat(line2.equals(line1)).isTrue();
        assertThat(line1.equals(line1)).isTrue();
        assertThat(line1.equals(null)).isFalse();
        assertThat(line1.equals("Not a Line")).isFalse();
        assertThat(line1.equals(line3)).isFalse(); // ID diverso, quindi non uguali
    }

    @Test
    @DisplayName("hashCode dovrebbe essere coerente con equals (basato su ID)")
    void testHashCode() {
        LineSegment line1 = new LineSegment(p1, p2);
        LineSegment line2 = (LineSegment) line1.cloneShape();
        LineSegment line3 = new LineSegment(p1, p2);

        assertThat(line1.hashCode()).isEqualTo(line2.hashCode());
        assertThat(line1.hashCode()).isNotEqualTo(line3.hashCode());
    }

    @Test
    @DisplayName("toString dovrebbe restituire una stringa significativa")
    void testToString() {
        LineSegment line = new LineSegment(new Point2D(1,2), new Point2D(3,4));
        String s = line.toString();
        assertThat(s).startsWith("LineSegment{");
        assertThat(s).contains("id=" + line.getId().toString());
        assertThat(s).contains("startPoint=Point2D{x=1.0, y=2.0}");
        assertThat(s).contains("endPoint=Point2D{x=3.0, y=4.0}");
        assertThat(s).contains("strokeColor=" + ColorData.BLACK.toString());
        assertThat(s).endsWith("}");
    }
}