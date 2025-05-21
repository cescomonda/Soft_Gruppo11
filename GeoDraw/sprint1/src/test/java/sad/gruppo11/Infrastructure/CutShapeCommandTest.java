package sad.gruppo11.Infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mockito;
import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.RectangleShape; // Classe concreta per mock

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CutShapeCommandTest {

    private Drawing mockDrawing;
    private Shape mockShapeToCut;
    private Clipboard mockClipboard; // Useremo un mock del Clipboard
    private CutShapeCommand cutShapeCommand;

    @BeforeEach
    void setUp() {
        mockDrawing = Mockito.mock(Drawing.class);
        mockShapeToCut = Mockito.mock(RectangleShape.class);
        mockClipboard = Mockito.mock(Clipboard.class); // Mockare il clipboard

        cutShapeCommand = new CutShapeCommand(mockDrawing, mockShapeToCut, mockClipboard);
    }

    @Test
    @DisplayName("Il costruttore dovrebbe lanciare NullPointerException per argomenti nulli")
    void testConstructorNullArguments() {
        assertThatThrownBy(() -> new CutShapeCommand(null, mockShapeToCut, mockClipboard))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Drawing cannot be null");

        assertThatThrownBy(() -> new CutShapeCommand(mockDrawing, null, mockClipboard))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Shape to cut cannot be null");

        assertThatThrownBy(() -> new CutShapeCommand(mockDrawing, mockShapeToCut, null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Clipboard cannot be null");
    }

    @Test
    @DisplayName("execute dovrebbe impostare la forma nel clipboard e rimuoverla dal drawing")
    void testExecute() {
        cutShapeCommand.execute();

        // Verifica che la forma sia stata impostata nel clipboard
        // Il clipboard.set() dovrebbe clonare internamente, quindi passiamo mockShapeToCut
        verify(mockClipboard, times(1)).set(mockShapeToCut);

        // Verifica che la forma sia stata rimossa dal drawing
        verify(mockDrawing, times(1)).removeShape(mockShapeToCut);

        // Verifica che non ci siano altre interazioni non previste
        verifyNoMoreInteractions(mockClipboard);
        verifyNoMoreInteractions(mockDrawing);
    }

    @Test
    @DisplayName("undo dovrebbe riaggiungere la forma al drawing")
    void testUndo() {
        cutShapeCommand.undo();

        // Verifica che la forma sia stata riaggiunta al drawing
        verify(mockDrawing, times(1)).addShape(mockShapeToCut);

        // L'undo di CutShapeCommand NON dovrebbe interagire con il clipboard.
        // Il clipboard mantiene la forma tagliata.
        verifyNoInteractions(mockClipboard); // Nessuna interazione con il clipboard durante l'undo
        verifyNoMoreInteractions(mockDrawing);
    }

    @Test
    @DisplayName("L'operazione di undo non dovrebbe modificare il contenuto del clipboard")
    void testUndoDoesNotAffectClipboard() {
        // Esegui per mettere qualcosa nel clipboard
        cutShapeCommand.execute();
        verify(mockClipboard).set(mockShapeToCut); // Verifica che sia stato messo
        reset(mockClipboard); // Resetta il mock del clipboard per il test di undo

        // Esegui undo
        cutShapeCommand.undo();

        // Verifica che il clipboard non sia stato toccato durante l'undo
        verifyNoInteractions(mockClipboard);
    }
}