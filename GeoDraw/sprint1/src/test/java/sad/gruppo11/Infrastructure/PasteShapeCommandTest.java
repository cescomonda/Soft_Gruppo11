package sad.gruppo11.Infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.RectangleShape; // Classe concreta per mock
import sad.gruppo11.Model.geometry.Vector2D;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PasteShapeCommandTest {

    private Drawing mockDrawing;
    private Clipboard mockClipboard;
    private PasteShapeCommand pasteShapeCommand;

    private Shape mockShapeFromClipboard;       // La forma "originale" che clipboard.get() restituirebbe
    private Shape mockShapeForPasting;          // Il clone di mockShapeFromClipboard con nuovo ID

    @BeforeEach
    void setUp() {
        mockDrawing = Mockito.mock(Drawing.class);
        mockClipboard = Mockito.mock(Clipboard.class);

        // Questo è ciò che clipboard.get() restituirà.
        // Ricorda che clipboard.get() stesso dovrebbe restituire un clone.
        mockShapeFromClipboard = Mockito.mock(RectangleShape.class);

        // Questo è ciò che mockShapeFromClipboard.cloneWithNewId() restituirà.
        // Sarà l'oggetto che viene effettivamente mosso e aggiunto al disegno.
        mockShapeForPasting = Mockito.mock(RectangleShape.class);

        pasteShapeCommand = new PasteShapeCommand(mockDrawing, mockClipboard);
    }

    @Test
    @DisplayName("Il costruttore dovrebbe lanciare NullPointerException per argomenti nulli")
    void testConstructorNullArguments() {
        assertThatThrownBy(() -> new PasteShapeCommand(null, mockClipboard))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Drawing cannot be null");

        assertThatThrownBy(() -> new PasteShapeCommand(mockDrawing, null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Clipboard cannot be null");
    }

    @Test
    @DisplayName("execute dovrebbe ottenere forma da clipboard, clonarla con nuovo ID, spostarla e aggiungerla al drawing")
    void testExecuteWhenClipboardNotEmpty() {
        // 1. Configura clipboard.get() per restituire la nostra forma mock
        when(mockClipboard.get()).thenReturn(mockShapeFromClipboard);

        // 2. Configura mockShapeFromClipboard.cloneWithNewId() per restituire mockShapeForPasting
        when(mockShapeFromClipboard.cloneWithNewId()).thenReturn(mockShapeForPasting);

        // Esegui il comando
        pasteShapeCommand.execute();

        // Verifiche:
        // a) clipboard.get() è stato chiamato
        verify(mockClipboard, times(1)).get();

        // b) mockShapeFromClipboard.cloneWithNewId() è stato chiamato
        verify(mockShapeFromClipboard, times(1)).cloneWithNewId();

        // c) mockShapeForPasting.move() è stato chiamato con l'offset corretto
        ArgumentCaptor<Vector2D> vectorCaptor = ArgumentCaptor.forClass(Vector2D.class);
        verify(mockShapeForPasting, times(1)).move(vectorCaptor.capture());
        assertThat(vectorCaptor.getValue()).isEqualTo(new Vector2D(10, 10)); // L'offset fisso

        // d) drawing.addShape() è stato chiamato con mockShapeForPasting
        verify(mockDrawing, times(1)).addShape(mockShapeForPasting);

        verifyNoMoreInteractions(mockDrawing, mockClipboard, mockShapeFromClipboard, mockShapeForPasting);
    }

    @Test
    @DisplayName("execute non dovrebbe fare nulla se il clipboard è vuoto")
    void testExecuteWhenClipboardIsEmpty() {
        // Configura clipboard.get() per restituire null
        when(mockClipboard.get()).thenReturn(null);

        pasteShapeCommand.execute();

        // Verifica che clipboard.get() sia stato chiamato
        verify(mockClipboard, times(1)).get();

        // Verifica che nessun'altra interazione sia avvenuta con le altre dipendenze
        verifyNoInteractions(mockShapeFromClipboard); // cloneWithNewId non dovrebbe essere chiamato
        verifyNoInteractions(mockShapeForPasting);   // move non dovrebbe essere chiamato
        verifyNoInteractions(mockDrawing);           // addShape non dovrebbe essere chiamato
        verifyNoMoreInteractions(mockClipboard);
    }

    @Test
    @DisplayName("undo dovrebbe rimuovere la forma incollata (pastedShape) dal drawing")
    void testUndoWhenShapeWasPasted() {
        // Simula un execute precedente che ha avuto successo
        when(mockClipboard.get()).thenReturn(mockShapeFromClipboard);
        when(mockShapeFromClipboard.cloneWithNewId()).thenReturn(mockShapeForPasting);
        pasteShapeCommand.execute(); // Questo imposta this.pastedShape nel comando a mockShapeForPasting

        reset(mockDrawing, mockClipboard, mockShapeFromClipboard, mockShapeForPasting); // Resetta per il test di undo

        // Esegui undo
        pasteShapeCommand.undo();

        // Verifica che drawing.removeShape() sia stato chiamato con mockShapeForPasting
        // (che è il valore di this.pastedShape dentro il comando)
        verify(mockDrawing, times(1)).removeShape(mockShapeForPasting);
        verifyNoMoreInteractions(mockDrawing);
        verifyNoInteractions(mockClipboard, mockShapeFromClipboard, mockShapeForPasting); // Nessuna interazione con questi
    }

    @Test
    @DisplayName("undo non dovrebbe fare nulla se nessuna forma è stata incollata (clipboard era vuoto)")
    void testUndoWhenNothingWasPasted() {
        // Simula un execute precedente con clipboard vuoto
        when(mockClipboard.get()).thenReturn(null);
        pasteShapeCommand.execute(); // this.pastedShape nel comando sarà null

        reset(mockDrawing, mockClipboard); // Resetta per il test di undo

        // Esegui undo
        pasteShapeCommand.undo();

        // Verifica che nessuna interazione sia avvenuta con il drawing
        verifyNoInteractions(mockDrawing);
        verifyNoInteractions(mockClipboard);
    }
}