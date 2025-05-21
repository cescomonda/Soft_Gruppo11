package sad.gruppo11.Infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.RectangleShape; // Classe concreta per mock
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Rect;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;

class ResizeShapeCommandTest {

    private Shape mockTargetShape;
    private Rect initialBounds;
    private Rect newTestBounds;
    private ResizeShapeCommand resizeShapeCommand;

    @BeforeEach
    void setUp() {
        mockTargetShape = Mockito.mock(RectangleShape.class);
        initialBounds = new Rect(new Point2D(10, 10), 100, 50);
        newTestBounds = new Rect(new Point2D(0, 0), 200, 150);

        // Configura il mockTargetShape per restituire initialBounds quando getBounds() è chiamato
        // Questo è cruciale per captureOldBounds()
        when(mockTargetShape.getBounds()).thenReturn(initialBounds);

        resizeShapeCommand = new ResizeShapeCommand(mockTargetShape, newTestBounds);
    }

    @Test
    @DisplayName("Il costruttore dovrebbe lanciare NullPointerException per argomenti nulli")
    void testConstructorNullArguments() {
        assertThatThrownBy(() -> new ResizeShapeCommand(null, newTestBounds))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Target shape cannot be null");

        assertThatThrownBy(() -> new ResizeShapeCommand(mockTargetShape, null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("New bounds cannot be null");
    }

    @Test
    @DisplayName("Il costruttore dovrebbe creare una copia difensiva di newBounds")
    void testConstructorDefensiveCopyOfNewBounds() {
        Rect originalNewBounds = new Rect(1,1,1,1);
        ResizeShapeCommand cmd = new ResizeShapeCommand(mockTargetShape, originalNewBounds);

        // Modifica i bounds originali passati al costruttore
        originalNewBounds.setWidth(999);

        // Esegui il comando
        cmd.execute();

        ArgumentCaptor<Rect> rectCaptor = ArgumentCaptor.forClass(Rect.class);
        verify(mockTargetShape, atLeastOnce()).resize(rectCaptor.capture()); // atLeastOnce perché resize è chiamato anche in undo

        // Ci aspettiamo che il resize in execute() usi la copia interna di newBounds
        // Cerca la chiamata a resize con newBounds
        Rect usedNewBounds = null;
        for (Rect captured : rectCaptor.getAllValues()) {
            if (captured.getWidth() != 999) { // Troviamo la newBounds non modificata
                usedNewBounds = captured;
                break;
            }
        }
        assertThat(usedNewBounds).isNotNull();
        assertThat(usedNewBounds.getWidth()).isEqualTo(1.0); // Verifica che sia la copia originale
    }

    @Test
    @DisplayName("captureOldBounds dovrebbe memorizzare una copia dei bounds attuali della shape")
    void testCaptureOldBounds() {
        // `initialBounds` è già configurato per essere restituito da mockTargetShape.getBounds()
        resizeShapeCommand.captureOldBounds();

        // Per verificare internamente, dovremmo esporre oldBounds o testare tramite undo
        // Testiamo tramite undo:
        resizeShapeCommand.execute(); // Questo chiamerà resize(newTestBounds)
        reset(mockTargetShape); // Resetta interazioni per la verifica di undo
        when(mockTargetShape.getBounds()).thenReturn(newTestBounds); // Ora la forma ha i nuovi bounds

        resizeShapeCommand.undo();

        ArgumentCaptor<Rect> rectCaptor = ArgumentCaptor.forClass(Rect.class);
        verify(mockTargetShape).resize(rectCaptor.capture());
        assertThat(rectCaptor.getValue()).isEqualTo(initialBounds); // Undo dovrebbe usare gli oldBounds catturati
        assertThat(rectCaptor.getValue()).isNotSameAs(initialBounds); // Deve essere una copia
    }

    @Test
    @DisplayName("execute dovrebbe catturare oldBounds (se non già fatto) e chiamare resize sulla Shape con newBounds")
    void testExecute() {
        // `initialBounds` è già configurato in setUp per mockTargetShape.getBounds()

        resizeShapeCommand.execute();

        // Verifica che resize sia stato chiamato con newTestBounds
        ArgumentCaptor<Rect> rectCaptor = ArgumentCaptor.forClass(Rect.class);
        verify(mockTargetShape).resize(rectCaptor.capture());

        assertThat(rectCaptor.getValue()).isEqualTo(newTestBounds);
        // Poiché newTestBounds è stato clonato nel costruttore del comando,
        // il rect catturato dovrebbe essere uguale ma non la stessa istanza di newTestBounds passato a execute.
        // L'istanza newTestBounds nel test è quella che abbiamo passato al costruttore del comando.
        // Il comando ha la sua copia interna.
        assertThat(rectCaptor.getValue()).isNotSameAs(newTestBounds);

        // Verifica implicita che oldBounds sia stato catturato: lo testiamo esplicitamente in testUndoAfterExecute
    }

    @Test
    @DisplayName("undo dovrebbe chiamare resize sulla Shape con oldBounds")
    void testUndoAfterExecute() {
        // Configura getBounds per restituire initialBounds prima di execute
        when(mockTargetShape.getBounds()).thenReturn(initialBounds);
        resizeShapeCommand.execute(); // Esegue, cattura initialBounds come oldBounds, e chiama resize(newTestBounds)

        // Ora, per l'undo, simula che la forma abbia i newTestBounds
        // (questo non è strettamente necessario per verificare cosa passa undo a resize,
        // ma è più realistico per lo stato della shape)
        // reset(mockTargetShape); // Resetta le interazioni precedenti
        // when(mockTargetShape.getBounds()).thenReturn(newTestBounds); // Non necessario per questo test specifico di undo

        resizeShapeCommand.undo();

        // Verifica che resize sia stato chiamato con initialBounds (che era oldBounds)
        // La chiamata a resize in execute() è già avvenuta. Ora verifichiamo la seconda chiamata in undo().
        ArgumentCaptor<Rect> rectCaptor = ArgumentCaptor.forClass(Rect.class);
        verify(mockTargetShape, times(2)).resize(rectCaptor.capture()); // Una per execute, una per undo

        List<Rect> capturedRects = rectCaptor.getAllValues();
        assertThat(capturedRects.get(0)).isEqualTo(newTestBounds); // Chiamata da execute
        assertThat(capturedRects.get(1)).isEqualTo(initialBounds); // Chiamata da undo
        assertThat(capturedRects.get(1)).isNotSameAs(initialBounds); // Deve essere una copia
    }

    @Test
    @DisplayName("undo non dovrebbe fare nulla se oldBounds non è stato catturato (es. shape iniziale senza bounds)")
    void testUndoWhenOldBoundsNotCaptured() {
        // Simula uno scenario in cui getBounds() restituisce null la prima volta
        when(mockTargetShape.getBounds()).thenReturn(null);
        ResizeShapeCommand cmdWithNullInitialBounds = new ResizeShapeCommand(mockTargetShape, newTestBounds);

        
        cmdWithNullInitialBounds.execute(); // oldBounds sarà null, resize(newTestBounds) sarà chiamato
        reset(mockTargetShape); // Resetta per la verifica di undo

        cmdWithNullInitialBounds.undo(); // oldBounds è null, quindi resize non dovrebbe essere chiamato di nuovo
        verify(mockTargetShape, never()).resize(any(Rect.class)); // resize non deve essere chiamato in undo
    }

        @Test
    @DisplayName("undo non dovrebbe fare nulla se oldBounds non è stato catturato (es. shape iniziale senza bounds)")
    void testUndoWhenOldBoundsNotCaptured2() {
        // 1. Configura il mock per lo scenario specifico
        Shape localMockShape = Mockito.mock(RectangleShape.class);
        Rect localNewBounds = new Rect(1,1,1,1);
        when(localMockShape.getBounds()).thenReturn(null); // Importante: getBounds restituisce null

        // 2. Crea il comando con questa configurazione
        ResizeShapeCommand cmd = new ResizeShapeCommand(localMockShape, localNewBounds);

        // 3. Esegui execute()
        // Durante execute(), captureOldBounds() sarà chiamato.
        // Poiché localMockShape.getBounds() è null, cmd.oldBounds rimarrà null.
        // Poi localMockShape.resize(localNewBounds) sarà chiamato.
        assertDoesNotThrow(() -> cmd.execute(), "Execute should not throw even if initial bounds were null");

        // 4. Resetta le interazioni sul mock per isolare la verifica di undo()
        reset(localMockShape);

        // 5. Esegui undo()
        cmd.undo();

        // 6. Verifica che resize() NON sia stato chiamato su localMockShape durante undo()
        // perché oldBounds era null.
        verify(localMockShape, never()).resize(any(Rect.class));
    }

    @Test
    @DisplayName("Se captureOldBounds è chiamato esplicitamente, execute non dovrebbe ricatturarlo")
    void testExecuteAfterExplicitCaptureOldBounds() {
        // initialBounds è il valore di when(mockTargetShape.getBounds())
        resizeShapeCommand.captureOldBounds(); // Cattura esplicita

        // Modifichiamo cosa getBounds() restituirebbe ORA per vedere se viene ricatturato
        // Questo simula che la forma sia cambiata tra captureOldBounds() e execute() da un'altra parte (improbabile ma testabile)
        Rect intermediateBounds = new Rect(5,5,5,5);
        when(mockTargetShape.getBounds()).thenReturn(intermediateBounds);

        resizeShapeCommand.execute(); // Dovrebbe usare gli oldBounds già catturati da initialBounds

        // Verifichiamo l'undo per vedere quali oldBounds sono stati usati
        resizeShapeCommand.undo();

        ArgumentCaptor<Rect> rectCaptor = ArgumentCaptor.forClass(Rect.class);
        // Ci aspettiamo 2 chiamate a resize: execute(newTestBounds), undo(initialBounds)
        verify(mockTargetShape, times(2)).resize(rectCaptor.capture());
        List<Rect> allValues = rectCaptor.getAllValues();
        assertThat(allValues.get(0)).isEqualTo(newTestBounds);     // Da execute
        assertThat(allValues.get(1)).isEqualTo(initialBounds); // Da undo, usando gli oldBounds originali
    }
}