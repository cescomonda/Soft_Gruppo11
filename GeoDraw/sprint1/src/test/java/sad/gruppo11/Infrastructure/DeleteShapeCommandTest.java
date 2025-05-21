package sad.gruppo11.Infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mockito;
import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.LineSegment; // Usiamo una classe concreta per il mock di Shape

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeleteShapeCommandTest {

    private Drawing mockDrawing;
    private Shape mockShapeToDelete;
    private DeleteShapeCommand deleteShapeCommand;

    @BeforeEach
    void setUp() {
        mockDrawing = Mockito.mock(Drawing.class);
        mockShapeToDelete = Mockito.mock(LineSegment.class); // Puoi usare qualsiasi implementazione

        deleteShapeCommand = new DeleteShapeCommand(mockDrawing, mockShapeToDelete);
    }

    @Test
    @DisplayName("Il costruttore dovrebbe lanciare NullPointerException per argomenti nulli")
    void testConstructorNullArguments() {
        assertThatThrownBy(() -> new DeleteShapeCommand(null, mockShapeToDelete))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Drawing cannot be null");

        assertThatThrownBy(() -> new DeleteShapeCommand(mockDrawing, null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Shape to delete cannot be null");
    }

    @Test
    @DisplayName("execute dovrebbe chiamare removeShape sul Drawing")
    void testExecute() {
        deleteShapeCommand.execute();

        // Verifica che removeShape sia stato chiamato con la forma corretta
        verify(mockDrawing, times(1)).removeShape(mockShapeToDelete);
        verifyNoMoreInteractions(mockDrawing);
    }

    @Test
    @DisplayName("undo dovrebbe chiamare addShape sul Drawing")
    void testUndo() {
        deleteShapeCommand.undo();

        // Verifica che addShape sia stato chiamato per ripristinare la forma
        verify(mockDrawing, times(1)).addShape(mockShapeToDelete);
        verifyNoMoreInteractions(mockDrawing);
    }

    @Test
    @DisplayName("toString dovrebbe restituire una stringa significativa")
    void testToString() {
        // when(mockShapeToDelete.getId()).thenReturn(java.util.UUID.randomUUID()); // Opzionale

        String commandString = deleteShapeCommand.toString();

        assertThat(commandString).startsWith("DeleteShapeCommand{");
        if (mockShapeToDelete.getId() != null) {
            assertThat(commandString).contains("shapeId=" + mockShapeToDelete.getId().toString());
        } else {
            assertThat(commandString).contains("shapeId=null");
        }
        assertThat(commandString).endsWith("}");
    }
}