package sad.gruppo11.Model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mockito;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Rect;
import sad.gruppo11.Model.geometry.Vector2D;
import sad.gruppo11.View.ShapeVisitor;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

class RectangleShapeTest {

    private static final double DELTA = 1e-9;

    private Rect defaultBounds = new Rect(new Point2D(10, 20), 100, 50);

    @Test
    @DisplayName("Costruttore con Rect dovrebbe inizializzare bounds, ID, e colori default")
    void testConstructorWithRect() {
        RectangleShape rectShape = new RectangleShape(defaultBounds);

        assertThat(rectShape.getId()).isNotNull();
        assertThat(rectShape.getBounds()).isEqualTo(defaultBounds);
        assertThat(rectShape.getBounds()).isNotSameAs(defaultBounds); // Verifica copia difensiva
        assertThat(rectShape.getStrokeColor()).isEqualTo(ColorData.BLACK);
        assertThat(rectShape.getFillColor()).isEqualTo(ColorData.TRANSPARENT);
    }

    @Test
    @DisplayName("Costruttore con Point2D, width, height dovrebbe inizializzare correttamente")
    void testConstructorWithPointAndDimensions() {
        Point2D tl = new Point2D(5,5);
        RectangleShape rectShape = new RectangleShape(tl, 30, 40);
        Rect expectedBounds = new Rect(tl, 30, 40);

        assertThat(rectShape.getBounds()).isEqualTo(expectedBounds);
        assertThat(rectShape.getStrokeColor()).isEqualTo(ColorData.BLACK);
        assertThat(rectShape.getFillColor()).isEqualTo(ColorData.TRANSPARENT);
    }


    @Test
    @DisplayName("Costruttore dovrebbe lanciare eccezione per bounds nulli")
    void testConstructorNullBounds() {
        assertThatThrownBy(() -> new RectangleShape((Rect) null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Bounds Rect cannot be null");
    }

    @Test
    @DisplayName("move dovrebbe traslare i bounds del rettangolo")
    void testMove() {
        RectangleShape rectShape = new RectangleShape(defaultBounds);
        Vector2D v = new Vector2D(5, -10);
        rectShape.move(v);

        Rect expectedMovedBounds = defaultBounds.translated(5, -10);
        assertThat(rectShape.getBounds()).isEqualTo(expectedMovedBounds);
    }

    @Test
    @DisplayName("move con vettore nullo non dovrebbe fare nulla")
    void testMoveNullVector() {
        RectangleShape rectShape = new RectangleShape(defaultBounds);
        Rect originalBounds = rectShape.getBounds();
        rectShape.move(null);
        assertThat(rectShape.getBounds()).isEqualTo(originalBounds);
    }

    @Test
    @DisplayName("resize dovrebbe sostituire i bounds del rettangolo")
    void testResize() {
        RectangleShape rectShape = new RectangleShape(defaultBounds);
        Rect newBounds = new Rect(new Point2D(0, 0), 10, 10);
        rectShape.resize(newBounds);

        assertThat(rectShape.getBounds()).isEqualTo(newBounds);
        assertThat(rectShape.getBounds()).isNotSameAs(newBounds); // Verifica copia difensiva
    }

    @Test
    @DisplayName("resize con bounds nulli dovrebbe lanciare eccezione")
    void testResizeNullBounds() {
        RectangleShape rectShape = new RectangleShape(defaultBounds);
        assertThatThrownBy(() -> rectShape.resize(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("New bounds cannot be null");
    }

    @Test
    @DisplayName("setStrokeColor dovrebbe cambiare il colore del tratto")
    void testSetStrokeColor() {
        RectangleShape rectShape = new RectangleShape(defaultBounds);
        ColorData newColor = ColorData.RED;
        rectShape.setStrokeColor(newColor);
        assertThat(rectShape.getStrokeColor()).isEqualTo(newColor);
        assertThat(rectShape.getStrokeColor()).isNotSameAs(newColor);
    }

    @Test
    @DisplayName("setStrokeColor con colore nullo dovrebbe lanciare eccezione")
    void testSetStrokeColorNull() {
        RectangleShape rectShape = new RectangleShape(defaultBounds);
        assertThatThrownBy(() -> rectShape.setStrokeColor(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Stroke color cannot be null");
    }

    @Test
    @DisplayName("setFillColor dovrebbe cambiare il colore di riempimento")
    void testSetFillColor() {
        RectangleShape rectShape = new RectangleShape(defaultBounds);
        ColorData newColor = ColorData.GREEN;
        rectShape.setFillColor(newColor);
        assertThat(rectShape.getFillColor()).isEqualTo(newColor);
        assertThat(rectShape.getFillColor()).isNotSameAs(newColor);
    }

    @Test
    @DisplayName("setFillColor con colore nullo dovrebbe lanciare eccezione")
    void testSetFillColorNull() {
        RectangleShape rectShape = new RectangleShape(defaultBounds);
        assertThatThrownBy(() -> rectShape.setFillColor(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Fill color cannot be null");
    }

    @Test
    @DisplayName("contains dovrebbe delegare al metodo contains dei bounds")
    void testContains() {
        Rect bounds = new Rect(0, 0, 100, 50);
        RectangleShape rectShape = new RectangleShape(bounds);

        // Test basati sulla logica di Rect.contains, qui verifichiamo la delega
        assertThat(rectShape.contains(new Point2D(50, 25))).isTrue();  // Interno
        assertThat(rectShape.contains(new Point2D(0, 0))).isTrue();    // Bordo
        assertThat(rectShape.contains(new Point2D(101, 25))).isFalse(); // Esterno
        assertThat(rectShape.contains(null)).isFalse();
    }

    @Test
    @DisplayName("accept dovrebbe chiamare il metodo visit corretto del visitor")
    void testAcceptVisitor() {
        RectangleShape rectShape = new RectangleShape(defaultBounds);
        ShapeVisitor mockVisitor = Mockito.mock(ShapeVisitor.class);
        rectShape.accept(mockVisitor);
        Mockito.verify(mockVisitor).visit(rectShape);
        Mockito.verifyNoMoreInteractions(mockVisitor);

        rectShape.accept(null); // Non dovrebbe lanciare eccezioni
    }

    @Test
    @DisplayName("cloneShape dovrebbe creare una copia con lo stesso ID e proprietà")
    void testCloneShape() {
        RectangleShape original = new RectangleShape(defaultBounds);
        original.setStrokeColor(ColorData.BLUE);
        original.setFillColor(ColorData.YELLOW);
        Shape clonedShape = original.cloneShape();

        assertThat(clonedShape).isInstanceOf(RectangleShape.class);
        RectangleShape clonedRect = (RectangleShape) clonedShape;

        assertThat(clonedRect.getId()).isEqualTo(original.getId());
        assertThat(clonedRect.getBounds()).isEqualTo(original.getBounds());
        assertThat(clonedRect.getStrokeColor()).isEqualTo(original.getStrokeColor());
        assertThat(clonedRect.getFillColor()).isEqualTo(original.getFillColor());
        assertThat(clonedRect).isNotSameAs(original);
        assertThat(clonedRect.getBounds()).isNotSameAs(original.getBounds()); // Copie difensive
    }

    @Test
    @DisplayName("cloneWithNewId dovrebbe creare una copia con un nuovo ID e stesse proprietà")
    void testCloneWithNewId() {
        RectangleShape original = new RectangleShape(defaultBounds);
        original.setStrokeColor(ColorData.BLUE);
        original.setFillColor(ColorData.YELLOW);
        Shape clonedShape = original.cloneWithNewId();

        assertThat(clonedShape).isInstanceOf(RectangleShape.class);
        RectangleShape clonedRect = (RectangleShape) clonedShape;

        assertThat(clonedRect.getId()).isNotNull();
        assertThat(clonedRect.getId()).isNotEqualTo(original.getId());
        assertThat(clonedRect.getBounds()).isEqualTo(original.getBounds());
        assertThat(clonedRect.getStrokeColor()).isEqualTo(original.getStrokeColor());
        assertThat(clonedRect.getFillColor()).isEqualTo(original.getFillColor());
        assertThat(clonedRect).isNotSameAs(original);
    }

    @Test
    @DisplayName("getBounds dovrebbe restituire una copia difensiva dei bounds")
    void testGetBoundsDefensiveCopy() {
        RectangleShape rectShape = new RectangleShape(defaultBounds);
        Rect b1 = rectShape.getBounds();
        b1.setWidth(999); // Modifica la copia
        Rect b2 = rectShape.getBounds();
        assertThat(b2.getWidth()).isEqualTo(defaultBounds.getWidth(), within(DELTA)); // L'originale non dovrebbe cambiare
    }

    @Test
    @DisplayName("getStrokeColor e getFillColor dovrebbero restituire copie difensive")
    void testGetColorsDefensiveCopy() {
        RectangleShape rectShape = new RectangleShape(defaultBounds);
        rectShape.setStrokeColor(ColorData.RED);
        rectShape.setFillColor(ColorData.GREEN);

        ColorData stroke1 = rectShape.getStrokeColor();
        ColorData fill1 = rectShape.getFillColor();

        stroke1.setR(0); // Modifica le copie
        fill1.setG(0);

        ColorData stroke2 = rectShape.getStrokeColor();
        ColorData fill2 = rectShape.getFillColor();

        assertThat(stroke2.getR()).isEqualTo(ColorData.RED.getR());
        assertThat(fill2.getG()).isEqualTo(ColorData.GREEN.getG());
    }

    @Test
    @DisplayName("equals dovrebbe confrontare basandosi sull'ID")
    void testEquals() {
        RectangleShape rect1 = new RectangleShape(defaultBounds);
        RectangleShape rect2 = (RectangleShape) rect1.cloneShape(); // Stesso ID
        RectangleShape rect3 = new RectangleShape(defaultBounds);   // ID diverso

        assertThat(rect1.equals(rect2)).isTrue();
        assertThat(rect1.equals(rect3)).isFalse();
        assertThat(rect1.equals(null)).isFalse();
        assertThat(rect1.equals("Not a RectangleShape")).isFalse();
    }

    @Test
    @DisplayName("hashCode dovrebbe essere coerente con equals (basato su ID)")
    void testHashCode() {
        RectangleShape rect1 = new RectangleShape(defaultBounds);
        RectangleShape rect2 = (RectangleShape) rect1.cloneShape();
        RectangleShape rect3 = new RectangleShape(defaultBounds);

        assertThat(rect1.hashCode()).isEqualTo(rect2.hashCode());
        assertThat(rect1.hashCode()).isNotEqualTo(rect3.hashCode());
    }

    @Test
    @DisplayName("toString dovrebbe restituire una stringa significativa")
    void testToString() {
        RectangleShape rectShape = new RectangleShape(new Rect(1,2,3,4));
        String s = rectShape.toString();
        assertThat(s).startsWith("RectangleShape{");
        assertThat(s).contains("id=" + rectShape.getId().toString());
        assertThat(s).contains("bounds=" + rectShape.getBounds().toString());
        assertThat(s).contains("strokeColor=" + rectShape.getStrokeColor().toString());
        assertThat(s).contains("fillColor=" + rectShape.getFillColor().toString());
        assertThat(s).endsWith("}");
    }
}