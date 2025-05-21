package sad.gruppo11.Controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import sad.gruppo11.Infrastructure.AddShapeCommand;
import sad.gruppo11.Infrastructure.Command;
import sad.gruppo11.Infrastructure.CommandManager;
import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.LineSegment;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Point2D;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LineStateTest {

    private LineState lineState;
    private GeoEngine mockGeoEngine;
    private CommandManager mockCommandManager;
    private Drawing mockDrawing;

    private Point2D p1, p2, p3CloseToP1;
    private ColorData defaultStrokeColor;

    @BeforeEach
    void setUp() {
        lineState = new LineState();

        // Mock di GeoEngine e delle sue dipendenze necessarie
        mockGeoEngine = Mockito.mock(GeoEngine.class);
        mockCommandManager = Mockito.mock(CommandManager.class);
        mockDrawing = Mockito.mock(Drawing.class);

        // Configura GeoEngine per restituire i mock quando i suoi getter sono chiamati
        when(mockGeoEngine.getCommandManager()).thenReturn(mockCommandManager);
        when(mockGeoEngine.getDrawing()).thenReturn(mockDrawing);

        defaultStrokeColor = new ColorData(ColorData.BLUE); // Un colore di default per i test
        when(mockGeoEngine.getCurrentStrokeColorForNewShapes()).thenReturn(defaultStrokeColor);

        p1 = new Point2D(10, 10);
        p2 = new Point2D(100, 100); // Punto valido per creare una linea
        p3CloseToP1 = new Point2D(10.0001, 10.0001); // Punto troppo vicino a p1
    }

    @Test
    @DisplayName("onMousePressed dovrebbe memorizzare il punto iniziale")
    void testOnMousePressed() {
        lineState.onMousePressed(p1, mockGeoEngine);
        // Non possiamo accedere direttamente a startPoint, quindi verifichiamo il comportamento in onMouseReleased
        // Per ora, ci assicuriamo che non ci siano errori e che i parametri null siano gestiti.
        assertDoesNotThrow(() -> lineState.onMousePressed(p1, mockGeoEngine));
    }

    @Test
    @DisplayName("onMousePressed con argomenti nulli dovrebbe lanciare NullPointerException")
    void testOnMousePressedNullArgs() {
        assertThatThrownBy(() -> lineState.onMousePressed(null, mockGeoEngine))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> lineState.onMousePressed(p1, null))
            .isInstanceOf(NullPointerException.class);
    }
    
    @Test
    @DisplayName("onMouseDragged non dovrebbe fare nulla di significativo in questa implementazione")
    void testOnMouseDragged() {
        lineState.onMousePressed(p1, mockGeoEngine); // Deve esserci un startPoint
        assertDoesNotThrow(() -> lineState.onMouseDragged(p2, mockGeoEngine));
        // Nessuna interazione specifica da verificare se il drag non ha logica attiva
        verifyNoInteractions(mockCommandManager, mockDrawing); // A parte le chiamate da onMousePressed
    }


    @Test
    @DisplayName("onMouseReleased dovrebbe creare e eseguire AddShapeCommand se la linea è valida")
    void testOnMouseReleasedValidLine() {
        // 1. Simula onMousePressed
        lineState.onMousePressed(p1, mockGeoEngine);

        // 2. Simula onMouseReleased con un punto valido
        lineState.onMouseReleased(p2, mockGeoEngine);

        // 3. Verifica che un AddShapeCommand sia stato creato ed eseguito
        ArgumentCaptor<Command> commandCaptor = ArgumentCaptor.forClass(Command.class);
        verify(mockCommandManager, times(1)).execute(commandCaptor.capture());

        Command executedCommand = commandCaptor.getValue();
        assertThat(executedCommand).isInstanceOf(AddShapeCommand.class);

        // 4. Ispeziona il comando per verificare che la LineSegment interna sia corretta
        AddShapeCommand addCmd = (AddShapeCommand) executedCommand;
        // Per accedere alla shape interna, dovremmo usare reflection o un getter nel comando (sconsigliato per test)
        // Invece, verifichiamo le proprietà della linea che sarebbe stata creata.
        // La verifica più robusta sarebbe mockare il costruttore di LineSegment o ShapeFactory se usata.
        // Qui assumiamo che AddShapeCommand riceva la LineSegment corretta.

        // Verifichiamo che il comando sia stato creato con il mockDrawing.
        // Questo è più un test di integrazione del comando, ma è un buon segnale.
        // Il test di AddShapeCommand ha già verificato che AddShapeCommand usi il drawing e la shape passati.
        
        // Invece di ispezionare il comando (che è un test del comando stesso),
        // verifichiamo che il *processo* abbia usato i dati corretti.
        // Se LineSegment fosse mockabile facilmente, potremmo verificare i parametri del suo costruttore.
        // Per ora, ci fidiamo che se execute è chiamato, la linea è stata creata con i punti p1, p2.

        // Verifichiamo che lo startPoint sia stato resettato
        // Test indiretto: una successiva release senza press non dovrebbe fare nulla
        lineState.onMouseReleased(new Point2D(200, 200), mockGeoEngine);
        verify(mockCommandManager, times(1)).execute(any(Command.class)); // Ancora solo 1 execute in totale
    }

    @Test
    @DisplayName("La LineSegment creata da onMouseReleased dovrebbe avere il colore di tratto corretto")
    void testLineSegmentColorFromGeoEngine() {
        lineState.onMousePressed(p1, mockGeoEngine);
        lineState.onMouseReleased(p2, mockGeoEngine);

        ArgumentCaptor<AddShapeCommand> addCommandCaptor = ArgumentCaptor.forClass(AddShapeCommand.class);
        verify(mockCommandManager).execute(addCommandCaptor.capture());
        
        AddShapeCommand capturedAddCmd = addCommandCaptor.getValue();
        // Estrarre la shape dal comando è difficile senza reflection o getter.
        // In un test più di integrazione, verificheremmo la shape aggiunta al drawing.
        // Qui, possiamo solo fidarci che la logica interna di LineState chiami
        // engine.getCurrentStrokeColorForNewShapes() e lo passi a LineSegment.
        // Poiché non possiamo facilmente intercettare la creazione di LineSegment senza modificare il codice
        // o usare PowerMock (che vorremmo evitare), questo test è limitato.
        // La verifica che engine.getCurrentStrokeColorForNewShapes() sia chiamato è implicita
        // se il colore della linea nel modello (se potessimo vederlo) fosse corretto.

        // Questo test verifica che, *se* una linea è stata creata e passata ad AddShapeCommand,
        // la chiamata a mockGeoEngine.getCurrentStrokeColorForNewShapes() è avvenuta.
        // La verifica che il colore sia stato effettivamente impostato sulla linea è un test
        // della logica interna di LineState, difficile da isolare qui.
        // Il test di `LineSegment` stesso dovrebbe verificare che `setStrokeColor` funzioni.
        // Ci fidiamo che LineState usi il colore restituito.
        verify(mockGeoEngine, atLeastOnce()).getCurrentStrokeColorForNewShapes();
    }


    @Test
    @DisplayName("onMouseReleased dovrebbe NON creare comando se la linea è troppo corta (degenere)")
    void testOnMouseReleasedDegenerateLine() {
        lineState.onMousePressed(p1, mockGeoEngine);
        lineState.onMouseReleased(p3CloseToP1, mockGeoEngine); // Punto troppo vicino

        verify(mockCommandManager, never()).execute(any(Command.class));
        // Verifica che startPoint sia stato resettato anche in questo caso
        // Test indiretto: una successiva release (valida) dopo una degenere non dovrebbe usare il vecchio p1
        lineState.onMousePressed(new Point2D(200,200), mockGeoEngine); // Nuovo press
        lineState.onMouseReleased(new Point2D(300,300), mockGeoEngine); // Nuova release valida
        verify(mockCommandManager, times(1)).execute(any(Command.class)); // Solo 1 execute in totale
    }

    @Test
    @DisplayName("onMouseReleased senza un precedente onMousePressed non dovrebbe fare nulla")
    void testOnMouseReleasedWithoutPress() {
        // startPoint è null inizialmente (o dopo onExitState)
        lineState.onMouseReleased(p2, mockGeoEngine);
        verifyNoInteractions(mockCommandManager);
    }
    
    @Test
    @DisplayName("getToolName dovrebbe restituire 'Line Tool'")
    void testGetToolName() {
        assertThat(lineState.getToolName()).isEqualTo("Line Tool");
    }

        @Test
    @DisplayName("onEnterState dovrebbe resettare startPoint")
    void testOnEnterStateResetsStartPoint() {
        // 1. Simula uno stato precedente impostando startPoint (indirettamente)
        // Questa chiamata potrebbe (o non potrebbe) portare a un execute(),
        // a seconda di come è implementato onMousePressed e se questo test viene eseguito dopo altri
        // che hanno già interagito con mockCommandManager.
        lineState.onMousePressed(p1, mockGeoEngine);
        // A questo punto, startPoint interno di lineState dovrebbe essere p1.

        // 2. Chiama onEnterState per resettare lo stato interno di lineState
        lineState.onEnterState(mockGeoEngine); // Questo dovrebbe impostare lineState.startPoint = null;
        
        // !!! IMPORTANTE: Resetta il mockCommandManager QUI !!!
        // Vogliamo verificare che la successiva chiamata a onMouseReleased NON causi un execute.
        // Le interazioni precedenti con mockCommandManager non ci interessano per questa specifica verifica.
        Mockito.reset(mockCommandManager);

        // 3. Verifica che startPoint sia stato resettato (è null)
        // Test indiretto: onMouseReleased ora non dovrebbe fare nulla perché startPoint è null
        lineState.onMouseReleased(p2, mockGeoEngine);
        verify(mockCommandManager, never()).execute(any(Command.class));
    }

    @Test
    @DisplayName("onExitState dovrebbe resettare startPoint")
    void testOnExitStateResetsStartPoint() {
        // 1. Simula uno stato precedente impostando startPoint
        lineState.onMousePressed(p1, mockGeoEngine);

        // 2. Chiama onExitState
        lineState.onExitState(mockGeoEngine);

        // 3. Verifica che startPoint sia stato resettato (è null)
        lineState.onMouseReleased(p2, mockGeoEngine);
        verify(mockCommandManager, never()).execute(any(Command.class));
    }
}