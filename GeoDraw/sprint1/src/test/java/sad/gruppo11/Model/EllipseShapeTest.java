package sad.gruppo11.Model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mockito;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Rect;
import sad.gruppo11.Model.geometry.Vector2D;
import sad.gruppo11.View.ShapeVisitor;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

class EllipseShapeTest {

    private static final double DELTA = 1e-9;

    private Rect defaultBounds = new Rect(new Point2D(0, 0), 20, 10); // Center (10,5), rX=10, rY=5

    @Test
    @DisplayName("Costruttore con Rect dovrebbe inizializzare bounds, ID, e colori default")
    void testConstructorWithRect() {
        EllipseShape ellipse = new EllipseShape(defaultBounds);

        assertThat(ellipse.getId()).isNotNull();
        assertThat(ellipse.getBounds()).isEqualTo(defaultBounds);
        assertThat(ellipse.getBounds()).isNotSameAs(defaultBounds); // Copia difensiva
        assertThat(ellipse.getStrokeColor()).isEqualTo(ColorData.BLACK);
        assertThat(ellipse.getFillColor()).isEqualTo(ColorData.TRANSPARENT);
    }

    @Test
    @DisplayName("Costruttore con Rect nullo dovrebbe lanciare IllegalArgumentException")
    void testConstructorNullRect() {
        assertThatThrownBy(() -> new EllipseShape((Rect) null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Bounds Rect cannot be null");
    }

    @Test
    @DisplayName("Costruttore con centro e raggi dovrebbe calcolare i bounds corretti")
    void testConstructorWithCenterAndRadii() {
        Point2D center = new Point2D(10, 10);
        double radiusX = 5;
        double radiusY = 3;
        EllipseShape ellipse = new EllipseShape(center, radiusX, radiusY);

        Rect expectedBounds = new Rect(new Point2D(5, 7), 10, 6); // x=10-5, y=10-3, w=5*2, h=3*2
        assertThat(ellipse.getBounds()).isEqualTo(expectedBounds);
        assertThat(ellipse.getStrokeColor()).isEqualTo(ColorData.BLACK);
        assertThat(ellipse.getFillColor()).isEqualTo(ColorData.TRANSPARENT);
    }

    @Test
    @DisplayName("Costruttore con centro nullo dovrebbe lanciare IllegalArgumentException")
    void testConstructorNullCenter() {
        assertThatThrownBy(() -> new EllipseShape(null, 5, 5))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Center point cannot be null");
    }

    @Test
    @DisplayName("Costruttore con raggi negativi dovrebbe lanciare IllegalArgumentException")
    void testConstructorNegativeRadii() {
        Point2D center = new Point2D(0,0);
        assertThatThrownBy(() -> new EllipseShape(center, -1, 5))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Radii cannot be negative");
        assertThatThrownBy(() -> new EllipseShape(center, 5, -1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Radii cannot be negative");
    }


    @Test
    @DisplayName("move dovrebbe traslare i bounds dell'ellisse")
    void testMove() {
        EllipseShape ellipse = new EllipseShape(defaultBounds);
        Vector2D v = new Vector2D(5, -10);
        ellipse.move(v);

        Rect expectedMovedBounds = defaultBounds.translated(5, -10);
        assertThat(ellipse.getBounds()).isEqualTo(expectedMovedBounds);
    }

    @Test
    @DisplayName("move con vettore nullo non dovrebbe fare nulla")
    void testMoveNullVector() {
        EllipseShape ellipse = new EllipseShape(defaultBounds);
        Rect originalBounds = ellipse.getBounds();
        ellipse.move(null);
        assertThat(ellipse.getBounds()).isEqualTo(originalBounds);
    }

    @Test
    @DisplayName("resize dovrebbe sostituire i bounds dell'ellisse")
    void testResize() {
        EllipseShape ellipse = new EllipseShape(defaultBounds);
        Rect newBounds = new Rect(new Point2D(1, 1), 5, 5);
        ellipse.resize(newBounds);

        assertThat(ellipse.getBounds()).isEqualTo(newBounds);
        assertThat(ellipse.getBounds()).isNotSameAs(newBounds); // Copia difensiva
    }

    @Test
    @DisplayName("resize con bounds nulli dovrebbe lanciare eccezione")
    void testResizeNullBounds() {
        EllipseShape ellipse = new EllipseShape(defaultBounds);
        assertThatThrownBy(() -> ellipse.resize(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("New bounds cannot be null");
    }


    @Test
    @DisplayName("setStrokeColor dovrebbe cambiare il colore del tratto")
    void testSetStrokeColor() {
        EllipseShape ellipse = new EllipseShape(defaultBounds);
        ColorData newColor = ColorData.RED;
        ellipse.setStrokeColor(newColor);
        assertThat(ellipse.getStrokeColor()).isEqualTo(newColor);
        assertThat(ellipse.getStrokeColor()).isNotSameAs(newColor);
    }

    @Test
    @DisplayName("setStrokeColor con colore nullo dovrebbe lanciare eccezione")
    void testSetStrokeColorNull() {
        EllipseShape ellipse = new EllipseShape(defaultBounds);
        assertThatThrownBy(() -> ellipse.setStrokeColor(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Stroke color cannot be null");
    }

    @Test
    @DisplayName("setFillColor dovrebbe cambiare il colore di riempimento")
    void testSetFillColor() {
        EllipseShape ellipse = new EllipseShape(defaultBounds);
        ColorData newColor = ColorData.GREEN;
        ellipse.setFillColor(newColor);
        assertThat(ellipse.getFillColor()).isEqualTo(newColor);
        assertThat(ellipse.getFillColor()).isNotSameAs(newColor);
    }

    @Test
    @DisplayName("setFillColor con colore nullo dovrebbe lanciare eccezione")
    void testSetFillColorNull() {
        EllipseShape ellipse = new EllipseShape(defaultBounds);
        assertThatThrownBy(() -> ellipse.setFillColor(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Fill color cannot be null");
    }

    @Test
    @DisplayName("contains dovrebbe identificare punti dentro, sul bordo e fuori dall'ellisse")
    void testContains() {
        // Ellisse con bounds (0,0) w=20, h=10. Center (10,5), rX=10, rY=5
        EllipseShape ellipse = new EllipseShape(new Rect(0,0,20,10));

        // Punti interni
        assertThat(ellipse.contains(new Point2D(10, 5))).isTrue(); // Centro
        assertThat(ellipse.contains(new Point2D(5, 5))).isTrue();  // Interno (x=5, y=5 -> (5-10)^2/100 + (5-5)^2/25 = 25/100 = 0.25 <= 1)
        assertThat(ellipse.contains(new Point2D(10, 2.5))).isTrue(); // Interno (x=10, y=2.5 -> (10-10)^2/100 + (2.5-5)^2/25 = (-2.5)^2/25 = 6.25/25 = 0.25 <= 1)

        // Punti sul bordo
        assertThat(ellipse.contains(new Point2D(0, 5))).isTrue();   // (0,5) -> (-10)^2/100 + 0 = 1
        assertThat(ellipse.contains(new Point2D(20, 5))).isTrue();  // (20,5) -> (10)^2/100 + 0 = 1
        assertThat(ellipse.contains(new Point2D(10, 0))).isTrue();  // (10,0) -> 0 + (-5)^2/25 = 1
        assertThat(ellipse.contains(new Point2D(10, 10))).isTrue(); // (10,10) -> 0 + (5)^2/25 = 1
        // Punto su un quadrante del bordo (approx)
        // Se x = 10 + 10*cos(PI/4), y = 5 + 5*sin(PI/4)
        double x_on_edge = 10 + 10 * Math.cos(Math.PI / 4); // ~17.07
        double y_on_edge = 5 + 5 * Math.sin(Math.PI / 4);   // ~8.53
        assertThat(ellipse.contains(new Point2D(x_on_edge, y_on_edge))).isFalse();


        // Punti esterni
        assertThat(ellipse.contains(new Point2D(0, 0))).isFalse();     // Esterno ((-10)^2/100 + (-5)^2/25 = 1+1 = 2 > 1) se non strettamente sul bordo
        assertThat(ellipse.contains(new Point2D(21, 5))).isFalse();
        assertThat(ellipse.contains(new Point2D(10, 11))).isFalse();
        assertThat(ellipse.contains(null)).isFalse();

        // Ellisse degenere (raggio X o Y zero o negativo - gestito da radiusX/Y <= 0)
        EllipseShape degenerateEllipseWidth = new EllipseShape(new Rect(0,0,0,10));
        assertThat(degenerateEllipseWidth.contains(new Point2D(0,5))).isFalse();
        EllipseShape degenerateEllipseHeight = new EllipseShape(new Rect(0,0,10,0));
        assertThat(degenerateEllipseHeight.contains(new Point2D(5,0))).isFalse();
    }


    @Test
    @DisplayName("accept dovrebbe chiamare il metodo visit corretto del visitor")
    void testAcceptVisitor() {
        EllipseShape ellipse = new EllipseShape(defaultBounds);
        ShapeVisitor mockVisitor = Mockito.mock(ShapeVisitor.class);
        ellipse.accept(mockVisitor);
        Mockito.verify(mockVisitor).visit(ellipse);
        Mockito.verifyNoMoreInteractions(mockVisitor);

        ellipse.accept(null); // Non dovrebbe lanciare eccezioni
    }

    @Test
    @DisplayName("cloneShape dovrebbe creare una copia con lo stesso ID e proprietà")
    void testCloneShape() {
        EllipseShape original = new EllipseShape(defaultBounds);
        original.setStrokeColor(ColorData.BLUE);
        original.setFillColor(ColorData.YELLOW);
        Shape clonedShape = original.cloneShape();

        assertThat(clonedShape).isInstanceOf(EllipseShape.class);
        EllipseShape clonedEllipse = (EllipseShape) clonedShape;

        assertThat(clonedEllipse.getId()).isEqualTo(original.getId());
        assertThat(clonedEllipse.getBounds()).isEqualTo(original.getBounds());
        assertThat(clonedEllipse.getStrokeColor()).isEqualTo(original.getStrokeColor());
        assertThat(clonedEllipse.getFillColor()).isEqualTo(original.getFillColor());
        assertThat(clonedEllipse).isNotSameAs(original);
        assertThat(clonedEllipse.getBounds()).isNotSameAs(original.getBounds());
    }

    @Test
    @DisplayName("cloneWithNewId dovrebbe creare una copia con un nuovo ID e stesse proprietà")
    void testCloneWithNewId() {
        EllipseShape original = new EllipseShape(defaultBounds);
        original.setStrokeColor(ColorData.BLUE);
        original.setFillColor(ColorData.YELLOW);
        Shape clonedShape = original.cloneWithNewId();

        assertThat(clonedShape).isInstanceOf(EllipseShape.class);
        EllipseShape clonedEllipse = (EllipseShape) clonedShape;

        assertThat(clonedEllipse.getId()).isNotNull();
        assertThat(clonedEllipse.getId()).isNotEqualTo(original.getId());
        assertThat(clonedEllipse.getBounds()).isEqualTo(original.getBounds());
        assertThat(clonedEllipse.getStrokeColor()).isEqualTo(original.getStrokeColor());
        assertThat(clonedEllipse.getFillColor()).isEqualTo(original.getFillColor());
        assertThat(clonedEllipse).isNotSameAs(original);
    }

    @Test
    @DisplayName("getBounds dovrebbe restituire una copia difensiva dei bounds")
    void testGetBoundsDefensiveCopy() {
        EllipseShape ellipse = new EllipseShape(defaultBounds);
        Rect b1 = ellipse.getBounds();
        b1.setWidth(999);
        Rect b2 = ellipse.getBounds();
        assertThat(b2.getWidth()).isEqualTo(defaultBounds.getWidth(), within(DELTA));
    }

    @Test
    @DisplayName("getStrokeColor e getFillColor dovrebbero restituire copie difensive")
    void testGetColorsDefensiveCopy() {
        EllipseShape ellipse = new EllipseShape(defaultBounds);
        ellipse.setStrokeColor(ColorData.RED);
        ellipse.setFillColor(ColorData.GREEN);

        ColorData stroke1 = ellipse.getStrokeColor();
        ColorData fill1 = ellipse.getFillColor();

        stroke1.setR(0);
        fill1.setG(0);

        ColorData stroke2 = ellipse.getStrokeColor();
        ColorData fill2 = ellipse.getFillColor();

        assertThat(stroke2.getR()).isEqualTo(ColorData.RED.getR());
        assertThat(fill2.getG()).isEqualTo(ColorData.GREEN.getG());
    }

    @Test
    @DisplayName("equals dovrebbe confrontare basandosi sull'ID")
    void testEquals() {
        EllipseShape ellipse1 = new EllipseShape(defaultBounds);
        EllipseShape ellipse2 = (EllipseShape) ellipse1.cloneShape(); // Stesso ID
        EllipseShape ellipse3 = new EllipseShape(defaultBounds);   // ID diverso

        assertThat(ellipse1.equals(ellipse2)).isTrue();
        assertThat(ellipse1.equals(ellipse3)).isFalse();
        assertThat(ellipse1.equals(null)).isFalse();
        assertThat(ellipse1.equals("Not an EllipseShape")).isFalse();
    }

    @Test
    @DisplayName("hashCode dovrebbe essere coerente con equals (basato su ID)")
    void testHashCode() {
        EllipseShape ellipse1 = new EllipseShape(defaultBounds);
        EllipseShape ellipse2 = (EllipseShape) ellipse1.cloneShape();
        EllipseShape ellipse3 = new EllipseShape(defaultBounds);

        assertThat(ellipse1.hashCode()).isEqualTo(ellipse2.hashCode());
        assertThat(ellipse1.hashCode()).isNotEqualTo(ellipse3.hashCode());
    }

    @Test
    @DisplayName("toString dovrebbe restituire una stringa significativa")
    void testToString() {
        EllipseShape ellipse = new EllipseShape(new Rect(1,2,4,6)); // Center (3,5), rX=2, rY=3
        String s = ellipse.toString();
        assertThat(s).startsWith("EllipseShape{");
        assertThat(s).contains("id=" + ellipse.getId().toString());
        assertThat(s).contains("bounds=" + ellipse.getBounds().toString());
        assertThat(s).contains("strokeColor=" + ellipse.getStrokeColor().toString());
        assertThat(s).contains("fillColor=" + ellipse.getFillColor().toString());
        assertThat(s).endsWith("}");
    }
}