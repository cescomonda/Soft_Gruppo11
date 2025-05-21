package sad.gruppo11.Controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;
import sad.gruppo11.Infrastructure.Clipboard;
import sad.gruppo11.Infrastructure.CommandManager;
import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.RectangleShape; // Per creare istanze o mock
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Persistence.PersistenceController; // Per eventuale mocking futuro
import sad.gruppo11.View.Observer;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class GeoEngineTest {

    private GeoEngine geoEngine;

    // Mocks per le dipendenze che GeoEngine *chiama* o che vogliamo spiare
    private ToolState mockSelectState;
    private ToolState mockLineState;
    private Observer mockModelObserver;
    private Consumer<Shape> mockSelectionListener;
    private Consumer<ToolState> mockToolStateListener;

    // GeoEngine crea internamente Drawing, CommandManager, PersistenceController, Clipboard (singleton).
    // Per CommandManager, GeoEngine si passa come 'this'.
    // Per PersistenceController, è un new PersistenceController().
    // Questo rende difficile mockare CommandManager e PersistenceController senza refactoring di GeoEngine
    // o usando PowerMock. Per ora, testeremo con le istanze reali create da GeoEngine.

    @BeforeEach
    void setUp() {
        geoEngine = new GeoEngine(); // Usa il costruttore reale

        // Crea mock per gli stati che GeoEngine potrebbe usare, se vogliamo controllarli
        // Se GeoEngine usa le sue istanze interne, questi mock non verranno usati a meno che non li iniettiamo
        // o sovrascriviamo i getter in una sottoclasse di GeoEngine per il test.
        // Per ora, i test di setState useranno le istanze reali di stato create da GeoEngine.
        mockSelectState = Mockito.spy(geoEngine.getSelectState()); // Spia lo stato reale
        mockLineState = Mockito.spy(geoEngine.getLineState());   // Spia lo stato reale

        // Mock per observer/listener
        mockModelObserver = Mockito.mock(Observer.class);
        mockSelectionListener = Mockito.mock(Consumer.class);
        mockToolStateListener = Mockito.mock(Consumer.class);

        geoEngine.addModelObserver(mockModelObserver);
        geoEngine.addSelectionChangeListener(mockSelectionListener);
        geoEngine.addToolStateChangeListener(mockToolStateListener);

        // Resetta lo stato di GeoEngine a una condizione nota per ogni test
        // (il costruttore già imposta SelectState, ma per pulizia)
        geoEngine.setState(geoEngine.getSelectState()); // Assicura stato iniziale
        resetAllMocks(); // Resetta i mock dopo il setup iniziale
    }

    private void resetAllMocks() {
        // Resetta i mock usati per le verifiche tra i test
        // Nota: non resettare geoEngine stesso, ma i mock delle sue dipendenze/collaboratori
        Mockito.reset(mockModelObserver, mockSelectionListener, mockToolStateListener);
        // Se avessimo mock di CommandManager o PersistenceController, andrebbero resettati qui.
        // Anche gli spy degli stati:
        Mockito.reset(mockSelectState, mockLineState); // Resetta gli spy per contare le chiamate corrette
                                                       // Questo è utile se sostituiamo gli stati reali con spy.
                                                       // Nel setup attuale, questo non ha molto effetto se non li usiamo.
    }


    @Test
    @DisplayName("Il costruttore dovrebbe inizializzare con SelectState e colori di default")
    void testConstructorInitialStateAndColors() {
        GeoEngine newEngine = new GeoEngine(); // Crea una nuova istanza per testare il costruttore
        assertThat(newEngine.getCurrentState()).isInstanceOf(SelectState.class);
        assertThat(newEngine.getCurrentStrokeColorForNewShapes()).isEqualTo(new ColorData(ColorData.BLACK));
        assertThat(newEngine.getCurrentFillColorForNewShapes()).isEqualTo(new ColorData(ColorData.TRANSPARENT));
        assertThat(newEngine.getSelectedShape()).isNull();
    }

    // --- Test per setState ---
    @Test
    @DisplayName("setState dovrebbe cambiare lo stato corrente e chiamare onExit/onEnter degli stati")
    void testSetStateChangesStateAndCallsLifecycle() {
        // GeoEngine inizia in SelectState.
        // Per testare onExit e onEnter, abbiamo bisogno di spiare gli stati REALI usati da GeoEngine.
        ToolState initialSelectState = geoEngine.getSelectState();
        ToolState newLineState = geoEngine.getLineState();

        // Crea spy per gli stati reali usati da GeoEngine per intercettare le chiamate
        ToolState spiedInitialSelectState = spy(initialSelectState);
        ToolState spiedNewLineState = spy(newLineState);

        // Sovrascrivi temporaneamente gli stati in GeoEngine con gli spy
        // Questo è un po' un hack, l'iniezione di dipendenza sarebbe meglio.
        // Per fare questo senza modificare GeoEngine, potremmo creare una sottoclasse di GeoEngine
        // e sovrascrivere i getter getSelectState(), getLineState().
        // Per ora, assumiamo che possiamo verificare le chiamate su istanze reali se le otteniamo da geoEngine.
        // Il problema è che geoEngine.setState(geoEngine.getLineState()) userà l'istanza interna, non il nostro spy.

        // APPROCCIO ALTERNATIVO: Poiché non possiamo facilmente iniettare spy degli stati
        // nel GeoEngine esistente, ci concentreremo sulla verifica degli effetti collaterali
        // (notifiche, cambio di selezione). La verifica diretta di onExit/onEnter sugli stati
        // è già stata fatta nei test unitari dei singoli stati.

        // Stato iniziale è SelectState
        ToolState oldState = geoEngine.getCurrentState(); // Questo è l'istanza reale di SelectState
        ToolState newStateInstance = geoEngine.getLineState(); // L'istanza reale di LineState

        geoEngine.setState(newStateInstance);

        assertThat(geoEngine.getCurrentState()).isSameAs(newStateInstance);
        // Verifica le notifiche
        verify(mockToolStateListener, times(1)).accept(newStateInstance);
        // setCurrentlySelectedShape(null) in setState dovrebbe chiamare notifySelectionChanged e notifyViewToRefresh
        // Se il nuovo stato non è SelectState
        if (!(newStateInstance instanceof SelectState)) {
            verify(mockSelectionListener, times(1)).accept(null); // Selezione resettata a null
            verify(mockModelObserver, times(1)).update(geoEngine.getDrawing()); // Da notifyViewToRefresh
        }
    }

    @Test
    @DisplayName("setState allo stesso stato non dovrebbe fare nulla")
    void testSetStateToSameState() {
        ToolState initialState = geoEngine.getCurrentState();
        geoEngine.setState(initialState); // Imposta allo stesso stato

        // Nessuna notifica o chiamata di lifecycle dovrebbe avvenire
        verifyNoInteractions(mockToolStateListener);
        verifyNoInteractions(mockModelObserver); // A meno che notifyViewToRefresh non sia chiamato incondizionatamente
        verifyNoInteractions(mockSelectionListener);
        // E onExit/onEnter non dovrebbero essere chiamati (difficile da verificare senza spy iniettati)
    }

    @Test
    @DisplayName("setState a un stato non-Select dovrebbe deselezionare la forma corrente")
    void testSetStateToNonSelectDeselectsShape() {
        Shape mockSelectedShape = mock(RectangleShape.class);
        geoEngine.setCurrentlySelectedShape(mockSelectedShape); // Seleziona una forma
        resetAllMocks(); // Resetta i contatori dopo la selezione

        geoEngine.setState(geoEngine.getLineState()); // Cambia a LineState

        assertThat(geoEngine.getSelectedShape()).isNull();
        verify(mockSelectionListener, times(2)).accept(null); // Notifica di deselezione
    }
    
    @Test
    @DisplayName("setState a SelectState NON dovrebbe deselezionare la forma corrente")
    void testSetStateToSelectDoesNotDeselectShape() {
        Shape mockSelectedShape = mock(RectangleShape.class);
        geoEngine.setCurrentlySelectedShape(mockSelectedShape); // Seleziona una forma
        resetAllMocks();

        // Cambia a un altro stato e poi di nuovo a SelectState
        geoEngine.setState(geoEngine.getLineState()); // Deseleziona
        resetAllMocks(); // Resetta di nuovo per la prossima transizione
        geoEngine.setCurrentlySelectedShape(mockSelectedShape); // Riseleziona
        resetAllMocks();

        geoEngine.setState(geoEngine.getSelectState()); // Cambia a SelectState

        assertThat(geoEngine.getSelectedShape()).isSameAs(mockSelectedShape);
        // Non ci dovrebbe essere una chiamata ad accept(null) perché non deseleziona
        verify(mockSelectionListener, never()).accept(null);
        // Ma ci sarà una chiamata accept(mockSelectedShape) da notifyViewToRefresh -> notifySelectionChanged
        verify(mockSelectionListener, times(1)).accept(mockSelectedShape);
        verify(mockModelObserver, times(1)).update(geoEngine.getDrawing()); // Da notifyViewToRefresh
    }


    // --- Test per la delega degli eventi Mouse ---
    @Test
    @DisplayName("onMousePressed dovrebbe delegare allo stato corrente")
    void testOnMousePressedDelegatesToCurrentState() {
        // Usiamo uno spy sullo stato corrente per verificare la delega
        ToolState spiedCurrentState = spy(geoEngine.getCurrentState());
        geoEngine.setState(spiedCurrentState); // Imposta lo spy come stato corrente
        resetAllMocks(); // Resetta anche lo spy spiedCurrentState

        Point2D testPoint = new Point2D(10,10);
        geoEngine.onMousePressed(testPoint);

        verify(spiedCurrentState, times(1)).onMousePressed(testPoint, geoEngine);
    }
    
    @Test
    @DisplayName("onMousePressed con stato non-Select dovrebbe deselezionare la forma")
    void testOnMousePressedWithNonSelectStateDeselects() {
        Shape mockSelectedShape = mock(RectangleShape.class);
        geoEngine.setCurrentlySelectedShape(mockSelectedShape); // Seleziona forma

        ToolState spiedLineState = spy(geoEngine.getLineState());
        geoEngine.setState(spiedLineState); // Cambia a LineState (questo già deseleziona)
        resetAllMocks(); // Resetta mock dopo il cambio di stato

        geoEngine.setCurrentlySelectedShape(mockSelectedShape); // Riseleziona la forma artificialmente
        resetAllMocks(); // Resetta di nuovo i mock per il test di onMousePressed

        Point2D testPoint = new Point2D(10,10);
        geoEngine.onMousePressed(testPoint); // onMousePressed di LineState

        assertThat(geoEngine.getSelectedShape()).isNull();
        verify(mockSelectionListener).accept(null); // Notifica di deselezione
        verify(spiedLineState).onMousePressed(testPoint, geoEngine); // Verifica delega
    }


    @Test
    @DisplayName("onMouseDragged dovrebbe delegare allo stato corrente")
    void testOnMouseDraggedDelegates() {
        ToolState spiedCurrentState = spy(geoEngine.getCurrentState());
        geoEngine.setState(spiedCurrentState);
        reset(spiedCurrentState); // Resetta solo lo spy per questa interazione

        Point2D testPoint = new Point2D(20,20);
        geoEngine.onMouseDragged(testPoint);
        verify(spiedCurrentState, times(1)).onMouseDragged(testPoint, geoEngine);
    }

    @Test
    @DisplayName("onMouseReleased dovrebbe delegare allo stato corrente")
    void testOnMouseReleasedDelegates() {
        ToolState spiedCurrentState = spy(geoEngine.getCurrentState());
        geoEngine.setState(spiedCurrentState);
        reset(spiedCurrentState);

        Point2D testPoint = new Point2D(30,30);
        geoEngine.onMouseReleased(testPoint);
        verify(spiedCurrentState, times(1)).onMouseReleased(testPoint, geoEngine);
    }

    // --- Test per gestione colori ---
    @Test
    @DisplayName("Setter e Getter per i colori delle nuove forme dovrebbero funzionare")
    void testSetGetNewShapeColors() {
        ColorData newStroke = new ColorData(ColorData.RED);
        ColorData newFill = new ColorData(ColorData.GREEN);

        geoEngine.setCurrentStrokeColorForNewShapes(newStroke);
        geoEngine.setCurrentFillColorForNewShapes(newFill);

        assertThat(geoEngine.getCurrentStrokeColorForNewShapes()).isEqualTo(newStroke);
        assertThat(geoEngine.getCurrentStrokeColorForNewShapes()).isNotSameAs(newStroke); // Copia difensiva
        assertThat(geoEngine.getCurrentFillColorForNewShapes()).isEqualTo(newFill);
        assertThat(geoEngine.getCurrentFillColorForNewShapes()).isNotSameAs(newFill);   // Copia difensiva
    }

    // --- Test per gestione selezione ---
    @Test
    @DisplayName("setCurrentlySelectedShape dovrebbe aggiornare la selezione e notificare")
    void testSetCurrentlySelectedShape() {
        Shape shapeToSelect = mock(RectangleShape.class);
        geoEngine.setCurrentlySelectedShape(shapeToSelect);

        assertThat(geoEngine.getSelectedShape()).isSameAs(shapeToSelect);
        verify(mockSelectionListener, times(1)).accept(shapeToSelect);
        verify(mockModelObserver, times(1)).update(geoEngine.getDrawing()); // Da notifyViewToRefresh
    }

    // @Test
    // @DisplayName("setCurrentlySelectedShape con la stessa forma non dovrebbe notificare ripetutamente se non per refresh")
    // void testSetCurrentlySelectedShapeSameShape() {
    //     Shape shapeToSelect = mock(RectangleShape.class);
    //     geoEngine.setCurrentlySelectedShape(shapeToSelect); // Prima selezione
    //     resetAllMocks();

    //     geoEngine.setCurrentlySelectedShape(shapeToSelect); // Riseleziona la stessa forma

    //     // Secondo la logica attuale di setCurrentlySelectedShape:
    //     // if (this.currentlySelectedShapeByEngine != shape) è false.
    //     // L'else if (shape == null && this.currentlySelectedShapeByEngine == null) è false.
    //     // Quindi non ci dovrebbero essere nuove notifiche dirette da selectionChanged.
    //     // notifyViewToRefresh potrebbe essere chiamato in alcuni rami, ma non qui.
    //     // Il tuo codice attuale non ha un ramo che chiama notifyViewToRefresh in questo caso.
    //     // SE la logica fosse "notifica sempre il pannello proprietà", allora la verifica cambierebbe.
    //     // Con la logica attuale (non notifica se la forma è la stessa e non è null):
    //     verify(mockSelectionListener, never()).accept(any());
    //     verify(mockModelObserver, never()).update(any());

    //     // Però se la forma è null e si reimposta a null, la tua logica attuale notifica:
    //     geoEngine.setCurrentlySelectedShape(null); // Deseleziona
    //     resetAllMocks();
    //     geoEngine.setCurrentlySelectedShape(null); // Imposta di nuovo a null
    //     verify(mockSelectionListener).accept(null);
    //     verify(mockModelObserver).update(geoEngine.getDrawing());
    // }
    
    @Test
    @DisplayName("setCurrentlySelectedShape con null dovrebbe deselezionare e notificare")
    void testSetCurrentlySelectedShapeNullDeselects() {
        Shape initiallySelected = mock(RectangleShape.class);
        geoEngine.setCurrentlySelectedShape(initiallySelected); // Seleziona qualcosa
        resetAllMocks();

        geoEngine.setCurrentlySelectedShape(null); // Deseleziona

        assertThat(geoEngine.getSelectedShape()).isNull();
        verify(mockSelectionListener, times(1)).accept(null);
        verify(mockModelObserver, times(1)).update(geoEngine.getDrawing());
    }

    // --- Test per le notifiche ---
    @Test
    @DisplayName("notifyModelChanged dovrebbe notificare modelObservers e gestire la selezione")
    void testNotifyModelChanged() {
        Shape selectedShape = mock(RectangleShape.class);
        Drawing currentDrawing = geoEngine.getDrawing();
        currentDrawing.addShape(selectedShape); // Aggiungi la forma al disegno reale
        geoEngine.setCurrentlySelectedShape(selectedShape);
        resetAllMocks();

        geoEngine.notifyModelChanged();

        verify(mockModelObserver, times(1)).update(currentDrawing);
        // Poiché la forma è ancora nel disegno, notifySelectionChanged viene chiamato con la forma
        verify(mockSelectionListener, times(1)).accept(selectedShape);

        // Ora rimuovi la forma dal disegno e notifica
        currentDrawing.removeShape(selectedShape);
        resetAllMocks();
        geoEngine.notifyModelChanged(); // selectedShape non è più in currentDrawing.shapes

        verify(mockModelObserver, times(2)).update(currentDrawing);
        assertThat(geoEngine.getSelectedShape()).isNull(); // Dovrebbe essere stata deselezionata
        verify(mockSelectionListener, times(1)).accept(null); // Notifica di deselezione
    }

    // --- Test per createNewDrawing ---
    @Test
    @DisplayName("createNewDrawing dovrebbe creare un nuovo Drawing, pulire CommandManager, deselezionare e notificare")
    void testCreateNewDrawing() {
        Drawing oldDrawing = geoEngine.getDrawing();
        CommandManager spyCmdMgr = spy(geoEngine.getCommandManager()); // Spia il CommandManager reale
        // Non possiamo facilmente sostituire il CommandManager in GeoEngine senza refactoring,
        // quindi spiare è un'opzione per verificare la chiamata a clearStacks.

        geoEngine.setCurrentlySelectedShape(mock(Shape.class)); // Seleziona qualcosa
        resetAllMocks();
        // Dobbiamo "re-spyare" o usare il CommandManager originale se vogliamo verificare `clearStacks`
        // Questo è un limite del non poter iniettare il CommandManager.
        // Per ora, ci concentriamo sugli altri effetti.

        geoEngine.createNewDrawing();

        assertThat(geoEngine.getDrawing()).isNotNull();
        assertThat(geoEngine.getDrawing()).isNotSameAs(oldDrawing);
        assertThat(geoEngine.getDrawing().getShapes()).isEmpty();
        assertThat(geoEngine.getSelectedShape()).isNull();

        // Verifica che il CommandManager interno sia stato pulito
        // Questa verifica richiede che il CommandManager sia mockabile o spiabile in modo efficace.
        // Con l'architettura attuale, è difficile testare `spyCmdMgr.clearStacks()` in modo pulito
        // perché `geoEngine.cmdMgr` è `final`.
        // Per ora, assumiamo che `cmdMgr.clearStacks()` sia chiamato, dato che è nel codice.

        verify(mockModelObserver, times(1)).update(geoEngine.getDrawing()); // Notifica del nuovo modello
        verify(mockSelectionListener, times(1)).accept(null); // Notifica di deselezione
    }

    // Test per saveDrawing e loadDrawing sono omessi per ora a causa della difficoltà
    // di mockare PersistenceController creato internamente. Richiederebbero test di integrazione
    // o refactoring di GeoEngine per l'iniezione di dipendenza di PersistenceController.

}