package sad.gruppo11.Factory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mockito;
import sad.gruppo11.Controller.EllipseState;
import sad.gruppo11.Controller.LineState;
import sad.gruppo11.Controller.RectangleState;
import sad.gruppo11.Controller.SelectState; // Per testare un ToolState non supportato
import sad.gruppo11.Controller.ToolState;
import sad.gruppo11.Model.EllipseShape;
import sad.gruppo11.Model.LineSegment;
import sad.gruppo11.Model.RectangleShape;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Rect;


import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ShapeFactoryTest {

    private ShapeFactory shapeFactory;
    private Point2D p1;
    private Point2D p2;
    private Point2D p3_degenerate; // Per creare forme degeneri

    // Mock degli stati, poiché la loro logica interna non è rilevante per la factory,
    // solo il loro tipo (instanceof).
    private LineState mockLineState;
    private RectangleState mockRectangleState;
    private EllipseState mockEllipseState;
    private SelectState mockSelectState; // Uno stato non gestito dalla factory per creare forme

    @BeforeEach
    void setUp() {
        shapeFactory = new ShapeFactory();
        p1 = new Point2D(10, 20);
        p2 = new Point2D(110, 70); // w=100, h=50
        p3_degenerate = new Point2D(10.0001, 20.0001); // Molto vicino a p1

        mockLineState = Mockito.mock(LineState.class);
        mockRectangleState = Mockito.mock(RectangleState.class);
        mockEllipseState = Mockito.mock(EllipseState.class);
        mockSelectState = Mockito.mock(SelectState.class); // Non dovrebbe creare forme
    }

    @Test
    @DisplayName("createShape con argomenti nulli dovrebbe lanciare NullPointerException")
    void testCreateShapeNullArguments() {
        assertThatThrownBy(() -> shapeFactory.createShape(null, p1, p2))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("ToolState cannot be null");

        assertThatThrownBy(() -> shapeFactory.createShape(mockLineState, null, p2))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Point p1 cannot be null");

        assertThatThrownBy(() -> shapeFactory.createShape(mockLineState, p1, null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Point p2 cannot be null");
    }

    @Test
    @DisplayName("createShape con LineState dovrebbe creare una LineSegment")
    void testCreateShapeLine() {
        Shape shape = shapeFactory.createShape(mockLineState, p1, p2);

        assertThat(shape).isNotNull();
        assertThat(shape).isInstanceOf(LineSegment.class);
        LineSegment line = (LineSegment) shape;
        // Verifica che i punti siano copie e corretti
        assertThat(line.getStartPoint()).isEqualTo(p1);
        assertThat(line.getEndPoint()).isEqualTo(p2);
        assertThat(line.getStartPoint()).isNotSameAs(p1);
    }

    @Test
    @DisplayName("createShape con LineState e punti degeneri dovrebbe restituire null")
    void testCreateShapeLineDegenerate() {
        Shape shape = shapeFactory.createShape(mockLineState, p1, p3_degenerate);
        assertThat(shape).isNull();
    }

    @Test
    @DisplayName("createShape con RectangleState dovrebbe creare un RectangleShape")
    void testCreateShapeRectangle() {
        Shape shape = shapeFactory.createShape(mockRectangleState, p1, p2);

        assertThat(shape).isNotNull();
        assertThat(shape).isInstanceOf(RectangleShape.class);
        RectangleShape rectShape = (RectangleShape) shape;
        Rect bounds = rectShape.getBounds();

        double expectedX = Math.min(p1.getX(), p2.getX());
        double expectedY = Math.min(p1.getY(), p2.getY());
        double expectedWidth = Math.abs(p1.getX() - p2.getX());
        double expectedHeight = Math.abs(p1.getY() - p2.getY());

        assertThat(bounds.getX()).isEqualTo(expectedX, within(1e-9));
        assertThat(bounds.getY()).isEqualTo(expectedY, within(1e-9));
        assertThat(bounds.getWidth()).isEqualTo(expectedWidth, within(1e-9));
        assertThat(bounds.getHeight()).isEqualTo(expectedHeight, within(1e-9));
    }

    @Test
    @DisplayName("createShape con RectangleState e punti degeneri dovrebbe restituire null")
    void testCreateShapeRectangleDegenerate() {
        Shape shape = shapeFactory.createShape(mockRectangleState, p1, p3_degenerate);
        assertThat(shape).isNull();
    }

    @Test
    @DisplayName("createShape con EllipseState dovrebbe creare una EllipseShape")
    void testCreateShapeEllipse() {
        Shape shape = shapeFactory.createShape(mockEllipseState, p1, p2);

        assertThat(shape).isNotNull();
        assertThat(shape).isInstanceOf(EllipseShape.class);
        EllipseShape ellipseShape = (EllipseShape) shape;
        Rect bounds = ellipseShape.getBounds();

        double expectedX = Math.min(p1.getX(), p2.getX());
        double expectedY = Math.min(p1.getY(), p2.getY());
        double expectedWidth = Math.abs(p1.getX() - p2.getX());
        double expectedHeight = Math.abs(p1.getY() - p2.getY());

        assertThat(bounds.getX()).isEqualTo(expectedX, within(1e-9));
        assertThat(bounds.getY()).isEqualTo(expectedY, within(1e-9));
        assertThat(bounds.getWidth()).isEqualTo(expectedWidth, within(1e-9));
        assertThat(bounds.getHeight()).isEqualTo(expectedHeight, within(1e-9));
    }

    @Test
    @DisplayName("createShape con EllipseState e punti degeneri dovrebbe restituire null")
    void testCreateShapeEllipseDegenerate() {
        Shape shape = shapeFactory.createShape(mockEllipseState, p1, p3_degenerate);
        assertThat(shape).isNull();
    }

    @Test
    @DisplayName("createShape con ToolState non riconosciuto dovrebbe restituire null")
    void testCreateShapeUnrecognizedToolState() {
        Shape shape = shapeFactory.createShape(mockSelectState, p1, p2); // SelectState non crea forme
        assertThat(shape).isNull();

        // Test con un mock generico di ToolState che non è instanceof nessuno degli stati noti
        ToolState unknownToolState = Mockito.mock(ToolState.class);
        Shape shape2 = shapeFactory.createShape(unknownToolState, p1, p2);
        assertThat(shape2).isNull();
    }
}