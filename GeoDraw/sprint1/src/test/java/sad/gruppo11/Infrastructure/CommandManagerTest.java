package sad.gruppo11.Infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mockito;
import sad.gruppo11.Controller.GeoEngine; // Import per mockare GeoEngine

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommandManagerTest {

    private CommandManager commandManagerWithoutEngine;
    private CommandManager commandManagerWithEngine;
    private GeoEngine mockGeoEngine;
    private Command mockCommand1;
    private Command mockCommand2;

    @BeforeEach
    void setUp() {
        mockGeoEngine = Mockito.mock(GeoEngine.class);

        // Due istanze di CommandManager: una senza engine, una con engine (mockato)
        commandManagerWithoutEngine = new CommandManager(null);
        commandManagerWithEngine = new CommandManager(mockGeoEngine);

        mockCommand1 = Mockito.mock(Command.class);
        mockCommand2 = Mockito.mock(Command.class);
    }

    @Test
    @DisplayName("Un nuovo CommandManager dovrebbe avere stack vuoti e canUndo/canRedo false")
    void testInitialState() {
        assertThat(commandManagerWithoutEngine.canUndo()).isFalse();
        assertThat(commandManagerWithoutEngine.canRedo()).isFalse();
    }

    @Test
    @DisplayName("execute dovrebbe eseguire il comando, aggiungerlo a undoStack, pulire redoStack e notificare engine")
    void testExecuteCommand() {
        // Test con commandManagerWithEngine per verificare la notifica
        commandManagerWithEngine.execute(mockCommand1);

        verify(mockCommand1, times(1)).execute();
        assertThat(commandManagerWithEngine.canUndo()).isTrue();
        assertThat(commandManagerWithEngine.canRedo()).isFalse(); // redoStack dovrebbe essere pulito
        verify(mockGeoEngine, times(1)).notifyModelChanged();

        // Esegui un altro comando per verificare la pulizia di redoStack
        commandManagerWithEngine.execute(mockCommand2);
        verify(mockCommand2, times(1)).execute();
        assertThat(commandManagerWithEngine.canRedo()).isFalse();
        verify(mockGeoEngine, times(2)).notifyModelChanged(); // Chiamato di nuovo
    }

    @Test
    @DisplayName("execute con comando nullo dovrebbe lanciare NullPointerException")
    void testExecuteNullCommand() {
        assertThatThrownBy(() -> commandManagerWithoutEngine.execute(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Command to execute cannot be null");
    }

    @Test
    @DisplayName("undo dovrebbe annullare il comando, spostarlo a redoStack e notificare engine")
    void testUndoCommand() {
        commandManagerWithEngine.execute(mockCommand1);
        reset(mockGeoEngine); // Resetta per contare solo la notifica di undo

        commandManagerWithEngine.undo();

        verify(mockCommand1, times(1)).undo();
        assertThat(commandManagerWithEngine.canUndo()).isFalse();
        assertThat(commandManagerWithEngine.canRedo()).isTrue();
        verify(mockGeoEngine, times(1)).notifyModelChanged();
    }

    @Test
    @DisplayName("undo su stack vuoto non dovrebbe fare nulla e non notificare")
    void testUndoOnEmptyStack() {
        assertThat(commandManagerWithEngine.canUndo()).isFalse();
        commandManagerWithEngine.undo(); // Non dovrebbe lanciare eccezioni

        verifyNoInteractions(mockCommand1); // Nessun comando da annullare
        verifyNoInteractions(mockGeoEngine); // Nessuna notifica
        assertThat(commandManagerWithEngine.canUndo()).isFalse();
        assertThat(commandManagerWithEngine.canRedo()).isFalse();
    }

    @Test
    @DisplayName("redo dovrebbe rieseguire il comando, spostarlo a undoStack e notificare engine")
    void testRedoCommand() {
        commandManagerWithEngine.execute(mockCommand1);
        commandManagerWithEngine.undo(); // mockCommand1 è ora in redoStack
        reset(mockGeoEngine); // Resetta per contare solo la notifica di redo

        commandManagerWithEngine.redo();

        // Poiché redo() chiama execute() sul comando:
        verify(mockCommand1, times(2)).execute(); // Una volta dall'execute iniziale, una dal redo
        assertThat(commandManagerWithEngine.canUndo()).isTrue();
        assertThat(commandManagerWithEngine.canRedo()).isFalse();
        verify(mockGeoEngine, times(1)).notifyModelChanged();
    }

    @Test
    @DisplayName("redo su stack vuoto non dovrebbe fare nulla e non notificare")
    void testRedoOnEmptyStack() {
        assertThat(commandManagerWithEngine.canRedo()).isFalse();
        commandManagerWithEngine.redo(); // Non dovrebbe lanciare eccezioni

        verifyNoInteractions(mockCommand1);
        verifyNoInteractions(mockGeoEngine);
        assertThat(commandManagerWithEngine.canUndo()).isFalse();
        assertThat(commandManagerWithEngine.canRedo()).isFalse();
    }

    @Test
    @DisplayName("Sequenza Execute-Undo-Execute dovrebbe pulire redoStack")
    void testExecuteAfterUndoClearsRedoStack() {
        commandManagerWithEngine.execute(mockCommand1); // cmd1 in undoStack
        commandManagerWithEngine.undo();          // cmd1 in redoStack, undoStack vuoto
        assertThat(commandManagerWithEngine.canRedo()).isTrue();

        commandManagerWithEngine.execute(mockCommand2); // cmd2 in undoStack, redoStack pulito
        assertThat(commandManagerWithEngine.canRedo()).isFalse(); // redoStack è stato pulito da mockCommand1
    }

    @Test
    @DisplayName("clearStacks dovrebbe svuotare entrambi gli stack")
    void testClearStacks() {
        commandManagerWithEngine.execute(mockCommand1);
        commandManagerWithEngine.undo();
        commandManagerWithEngine.execute(mockCommand2); // Ora undoStack ha cmd2, redoStack ha cmd1 (dopo la prima undo)
                                                        // NO, execute pulisce redo. Quindi undoStack=[cmd1,cmd2], redoStack=[] dopo il primo execute
                                                        // Poi undo(): undoStack=[cmd1], redoStack=[cmd2]
                                                        // Poi execute(mockCommand2) -> cmd2 originale, e ora nuovo mockCommand2_bis
                                                        // Questo è un po' confuso. Riscriviamo la preparazione:

        commandManagerWithEngine.execute(mockCommand1); // undoStack=[c1], redoStack=[]
        commandManagerWithEngine.execute(mockCommand2); // undoStack=[c1,c2], redoStack=[]
        commandManagerWithEngine.undo();                // undoStack=[c1], redoStack=[c2]
        
        assertThat(commandManagerWithEngine.canUndo()).isTrue();
        assertThat(commandManagerWithEngine.canRedo()).isTrue();
        reset(mockGeoEngine); // Resetta le notifiche precedenti

        commandManagerWithEngine.clearStacks();

        assertThat(commandManagerWithEngine.canUndo()).isFalse();
        assertThat(commandManagerWithEngine.canRedo()).isFalse();
        // Verifica se notifyEngine è chiamato da clearStacks (basato sul tuo codice è commentato)
        verifyNoInteractions(mockGeoEngine); // Se notifyEngine() in clearStacks è commentato
        // Se lo decommentassi, sarebbe: verify(mockGeoEngine, times(1)).notifyModelChanged();
    }
    
    @Test
    @DisplayName("notifyEngine non dovrebbe essere chiamato se engine è null")
    void testNotificationWithNullEngine() {
        // Usa commandManagerWithoutEngine
        commandManagerWithoutEngine.execute(mockCommand1);
        // Nessuna verifica su mockGeoEngine perché non dovrebbe essere chiamato
        // Implicitamente testato dal fatto che non configuriamo mockGeoEngine per questo commandManager
        // e non ci aspettiamo interazioni.

        commandManagerWithoutEngine.undo();
        commandManagerWithoutEngine.redo(); // Riesegue mockCommand1
        commandManagerWithoutEngine.clearStacks();

        // Se mockGeoEngine fosse passato e non fosse null, ci sarebbero state chiamate.
        // Qui ci aspettiamo che non ci siano state chiamate a notifyModelChanged su mockGeoEngine.
        verify(mockGeoEngine, never()).notifyModelChanged(); // Assicura che il mockGeoEngine (non usato) non sia stato toccato
    }
}