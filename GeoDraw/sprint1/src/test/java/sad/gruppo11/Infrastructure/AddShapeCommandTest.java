package sad.gruppo11.Infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mockito;
import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.RectangleShape; // Usiamo una classe concreta per il mock di Shape

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AddShapeCommandTest {

    private Drawing mockDrawing;
    private Shape mockShape;
    private AddShapeCommand addShapeCommand;

    @BeforeEach
    void setUp() {
        // Crea mock per le dipendenze
        mockDrawing = Mockito.mock(Drawing.class);
        mockShape = Mockito.mock(RectangleShape.class); // Puoi usare una qualsiasi implementazione di Shape

        // Crea l'istanza del comando da testare
        addShapeCommand = new AddShapeCommand(mockDrawing, mockShape);
    }

    @Test
    @DisplayName("Il costruttore dovrebbe lanciare NullPointerException per argomenti nulli")
    void testConstructorNullArguments() {
        assertThatThrownBy(() -> new AddShapeCommand(null, mockShape))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Drawing cannot be null");

        assertThatThrownBy(() -> new AddShapeCommand(mockDrawing, null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Shape to add cannot be null");
    }

    @Test
    @DisplayName("execute dovrebbe chiamare addShape sul Drawing")
    void testExecute() {
        addShapeCommand.execute();

        // Verifica che il metodo addShape del mockDrawing sia stato chiamato esattamente una volta
        // con mockShape come argomento.
        verify(mockDrawing, times(1)).addShape(mockShape);
        verifyNoMoreInteractions(mockDrawing); // Assicura che non siano state fatte altre chiamate a mockDrawing
    }

    @Test
    @DisplayName("undo dovrebbe chiamare removeShape sul Drawing")
    void testUndo() {
        addShapeCommand.undo();

        // Verifica che il metodo removeShape del mockDrawing sia stato chiamato esattamente una volta
        // con mockShape come argomento.
        verify(mockDrawing, times(1)).removeShape(mockShape);
        verifyNoMoreInteractions(mockDrawing); // Assicura che non siano state fatte altre chiamate a mockDrawing
    }

    @Test
    @DisplayName("toString dovrebbe restituire una stringa significativa")
    void testToString() {
        // Configura il mockShape per restituire un ID fittizio per il test di toString
        // Se l'ID fosse nullo, toString potrebbe avere un comportamento diverso o lanciare eccezione.
        // Dal codice, gestisce shapeToAdd.getId() nullo, quindi va bene.
        // when(mockShape.getId()).thenReturn(java.util.UUID.randomUUID()); // Opzionale se l'ID non è cruciale per il formato

        String commandString = addShapeCommand.toString();

        assertThat(commandString).startsWith("AddShapeCommand{");
        // Se l'ID fosse sempre non nullo:
        // assertThat(commandString).contains("shapeId=" + mockShape.getId().toString());
        // Dato che potrebbe essere nullo (anche se il costruttore lo impedisce per shapeToAdd):
        if (mockShape.getId() != null) {
            assertThat(commandString).contains("shapeId=" + mockShape.getId().toString());
        } else {
             assertThat(commandString).contains("shapeId=null");
        }
        assertThat(commandString).endsWith("}");
    }
}