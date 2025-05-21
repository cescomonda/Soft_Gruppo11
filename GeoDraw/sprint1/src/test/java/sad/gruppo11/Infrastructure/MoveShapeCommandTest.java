package sad.gruppo11.Infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.EllipseShape; // Usiamo una classe concreta per il mock di Shape
import sad.gruppo11.Model.geometry.Vector2D;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class MoveShapeCommandTest {

    private Shape mockShapeToMove;
    private Vector2D testDisplacement;
    private MoveShapeCommand moveShapeCommand;

    @BeforeEach
    void setUp() {
        mockShapeToMove = Mockito.mock(EllipseShape.class);
        testDisplacement = new Vector2D(10.0, -5.0);

        // Il costruttore di MoveShapeCommand fa una copia del Vector2D,
        // quindi non dobbiamo preoccuparci di modifiche esterne a testDisplacement dopo la creazione del comando.
        moveShapeCommand = new MoveShapeCommand(mockShapeToMove, testDisplacement);
    }

    @Test
    @DisplayName("Il costruttore dovrebbe lanciare NullPointerException per argomenti nulli")
    void testConstructorNullArguments() {
        assertThatThrownBy(() -> new MoveShapeCommand(null, testDisplacement))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Shape to move cannot be null");

        assertThatThrownBy(() -> new MoveShapeCommand(mockShapeToMove, null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Displacement vector cannot be null");
    }

    @Test
    @DisplayName("Il costruttore dovrebbe creare una copia difensiva del Vector2D displacement")
    void testConstructorDefensiveCopyOfDisplacement() {
        Vector2D originalDisplacement = new Vector2D(1.0, 1.0);
        MoveShapeCommand cmd = new MoveShapeCommand(mockShapeToMove, originalDisplacement);

        // Modifica il vettore originale DOPO la creazione del comando
        originalDisplacement.setDx(99.0);
        originalDisplacement.setDy(99.0);

        // Esegui il comando: dovrebbe usare la copia interna, non il vettore modificato
        cmd.execute();

        // Cattura l'argomento passato a shapeToMove.move()
        ArgumentCaptor<Vector2D> vectorCaptor = ArgumentCaptor.forClass(Vector2D.class);
        verify(mockShapeToMove).move(vectorCaptor.capture());

        // Verifica che il vettore catturato sia quello originale non modificato (1.0, 1.0)
        assertThat(vectorCaptor.getValue().getDx()).isEqualTo(1.0);
        assertThat(vectorCaptor.getValue().getDy()).isEqualTo(1.0);
    }


    @Test
    @DisplayName("execute dovrebbe chiamare move sulla Shape con il displacement corretto")
    void testExecute() {
        moveShapeCommand.execute();

        // Verifica che shapeToMove.move() sia stato chiamato con il displacement originale
        // Poiché il costruttore di MoveShapeCommand crea una copia di testDisplacement,
        // dovremmo verificare con un Vector2D che sia .equals() a testDisplacement.
        ArgumentCaptor<Vector2D> vectorArgCaptor = ArgumentCaptor.forClass(Vector2D.class);
        verify(mockShapeToMove, times(1)).move(vectorArgCaptor.capture());

        Vector2D capturedVector = vectorArgCaptor.getValue();
        assertThat(capturedVector).isEqualTo(testDisplacement); // Confronta il contenuto
        assertThat(capturedVector).isNotSameAs(testDisplacement); // Dovrebbe essere la copia interna
    }

    @Test
    @DisplayName("undo dovrebbe chiamare move sulla Shape con il displacement inverso")
    void testUndo() {
        moveShapeCommand.undo();

        // Cattura l'argomento passato a shapeToMove.move()
        ArgumentCaptor<Vector2D> vectorArgCaptor = ArgumentCaptor.forClass(Vector2D.class);
        verify(mockShapeToMove, times(1)).move(vectorArgCaptor.capture());

        Vector2D capturedVector = vectorArgCaptor.getValue();
        Vector2D expectedInverseDisplacement = testDisplacement.inverse();

        // Verifica che il vettore catturato sia l'inverso del displacement originale
        assertThat(capturedVector.getDx()).isEqualTo(expectedInverseDisplacement.getDx(), within(1e-9));
        assertThat(capturedVector.getDy()).isEqualTo(expectedInverseDisplacement.getDy(), within(1e-9));
        // Alternativamente, se Vector2D.equals è robusto:
        assertThat(capturedVector).isEqualTo(expectedInverseDisplacement);
    }

    @Test
    @DisplayName("toString dovrebbe restituire una stringa significativa")
    void testToString() {
        // when(mockShapeToMove.getId()).thenReturn(java.util.UUID.randomUUID()); // Opzionale
        String commandString = moveShapeCommand.toString();

        assertThat(commandString).startsWith("MoveShapeCommand{");
        if (mockShapeToMove.getId() != null) {
            assertThat(commandString).contains("shapeId=" + mockShapeToMove.getId().toString());
        } else {
            assertThat(commandString).contains("shapeId=null"); // Assumendo che Shape.getId() possa essere null per un mock non configurato
        }
        assertThat(commandString).contains("displacement=" + testDisplacement.toString());
        assertThat(commandString).endsWith("}");
    }
}