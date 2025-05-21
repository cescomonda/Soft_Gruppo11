package sad.gruppo11.Controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;
import sad.gruppo11.Infrastructure.Command;
import sad.gruppo11.Infrastructure.CommandManager;
import sad.gruppo11.Infrastructure.MoveShapeCommand;
import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.RectangleShape; // Classe concreta per mock
import sad.gruppo11.Model.LineSegment;    // Altra classe concreta
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Vector2D;


import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class SelectStateTest {

    private SelectState selectState;
    private GeoEngine mockGeoEngine;
    private CommandManager mockCommandManager;
    private Drawing mockDrawing;

    private Shape mockShape1, mockShape2; // Forme nel disegno
    private Point2D p_onShape1, p_onShape2, p_empty, p_dragEnd, p_noDragRelease;

    @BeforeEach
    void setUp() {
        selectState = new SelectState();

        mockGeoEngine = Mockito.mock(GeoEngine.class);
        mockCommandManager = Mockito.mock(CommandManager.class);
        mockDrawing = Mockito.mock(Drawing.class);

        when(mockGeoEngine.getDrawing()).thenReturn(mockDrawing);
        when(mockGeoEngine.getCommandManager()).thenReturn(mockCommandManager);

        mockShape1 = Mockito.mock(RectangleShape.class, "Shape1");
        mockShape2 = Mockito.mock(LineSegment.class, "Shape2");

        // Configura le forme per i test di contains()
        // Nota: i punti esatti non sono cruciali se mockiamo solo il risultato di contains()
        p_onShape1 = new Point2D(10, 10);
        p_onShape2 = new Point2D(50, 50); // Assumiamo che sia su shape2 e non shape1
        p_empty = new Point2D(100, 100);   // Un punto in un'area vuota

        p_dragEnd = new Point2D(20, 20);         // Per simulare la fine di un drag
        p_noDragRelease = new Point2D(10, 10); // Rilascio sullo stesso punto del press (no drag)


        // Configura il comportamento di contains per le forme
        when(mockShape1.contains(p_onShape1)).thenReturn(true);
        when(mockShape1.contains(p_onShape2)).thenReturn(false);
        when(mockShape1.contains(p_empty)).thenReturn(false);
        when(mockShape1.contains(p_dragEnd)).thenReturn(true); // Assumiamo che dopo il drag sia ancora su shape1
        when(mockShape1.contains(p_noDragRelease)).thenReturn(true);


        when(mockShape2.contains(p_onShape1)).thenReturn(false);
        when(mockShape2.contains(p_onShape2)).thenReturn(true);
        when(mockShape2.contains(p_empty)).thenReturn(false);


        // Configura il disegno per restituire le forme
        // L'ordine è importante per il test di selezione della forma "più in alto"
        when(mockDrawing.getShapes()).thenReturn(Arrays.asList(mockShape1, mockShape2));
    }

    @Test
    @DisplayName("onMousePressed su una forma dovrebbe selezionare quella forma")
    void testOnMousePressedSelectsShape() {
        selectState.onMousePressed(p_onShape1, mockGeoEngine);
        verify(mockGeoEngine, times(1)).setCurrentlySelectedShape(mockShape1);

        selectState.onMousePressed(p_onShape2, mockGeoEngine);
        verify(mockGeoEngine, times(1)).setCurrentlySelectedShape(mockShape2);
    }

    @Test
    @DisplayName("onMousePressed su area vuota dovrebbe deselezionare (selezionare null)")
    void testOnMousePressedEmptyAreaDeselects() {
        selectState.onMousePressed(p_empty, mockGeoEngine);
        verify(mockGeoEngine, times(1)).setCurrentlySelectedShape(null);
    }

    @Test
    @DisplayName("onMousePressed dovrebbe selezionare la forma più in alto (ultima nella lista getShapes prima del reverse)")
    void testOnMousePressedSelectsTopmostShape() {
        // Configura mockShape2 per contenere anche p_onShape1, ma mockShape1 è "sotto"
        when(mockShape2.contains(p_onShape1)).thenReturn(true);
        // mockDrawing.getShapes() restituisce [shape1, shape2]
        // Collections.reverse fa diventare [shape2, shape1]
        // Quindi shape2 viene controllata per prima.

        selectState.onMousePressed(p_onShape1, mockGeoEngine);
        verify(mockGeoEngine, times(1)).setCurrentlySelectedShape(mockShape2);
    }
    
    @Test
    @DisplayName("onMousePressed con Drawing nullo dovrebbe gestire graziosamente")
    void testOnMousePressedNullDrawing() {
        when(mockGeoEngine.getDrawing()).thenReturn(null);
        selectState.onMousePressed(p_onShape1, mockGeoEngine);
        verify(mockGeoEngine, times(1)).setCurrentlySelectedShape(null); // Se il disegno è nullo, nulla può essere selezionato
    }


    @Test
    @DisplayName("onMouseDragged con una forma selezionata dovrebbe impostare isDragging a true")
    void testOnMouseDraggedWhenShapeSelected() {
        // Simula una forma selezionata
        when(mockGeoEngine.getSelectedShape()).thenReturn(mockShape1);

        selectState.onMousePressed(p_onShape1, mockGeoEngine); // Imposta pressMousePosition
        selectState.onMouseDragged(p_dragEnd, mockGeoEngine);

        // Non possiamo verificare isDragging direttamente, ma onMouseReleased si comporterà diversamente
        // Verifica che non vengano creati comandi durante il drag
        verifyNoInteractions(mockCommandManager);
    }

    @Test
    @DisplayName("onMouseDragged senza forma selezionata o senza press non dovrebbe fare nulla di significativo")
    void testOnMouseDraggedNoSelectionOrPress() {
        // Caso 1: Nessuna forma selezionata
        when(mockGeoEngine.getSelectedShape()).thenReturn(null);
        selectState.onMousePressed(p_empty, mockGeoEngine); // pressMousePosition è impostato, ma nessuna selezione
        selectState.onMouseDragged(p_dragEnd, mockGeoEngine);
        verifyNoInteractions(mockCommandManager);

        // Caso 2: Nessun press precedente (pressMousePosition è null)
        // Questo stato non dovrebbe accadere se onEnterState/onExitState funzionano,
        // ma testiamo il comportamento di onMouseDragged isolatamente.
        // Per farlo, creiamo un nuovo SelectState per non avere pressMousePosition da setUp.
        SelectState freshSelectState = new SelectState();
        freshSelectState.onMouseDragged(p_dragEnd, mockGeoEngine); // pressMousePosition è null
        verifyNoInteractions(mockCommandManager);
    }

    @Test
    @DisplayName("onMouseReleased dopo un drag significativo dovrebbe creare e eseguire MoveShapeCommand")
    void testOnMouseReleasedAfterDrag() {
        // 1. Simula selezione e pressione
        when(mockGeoEngine.getSelectedShape()).thenReturn(mockShape1);
        selectState.onMousePressed(p_onShape1, mockGeoEngine); // press a (10,10)

        // 2. Simula drag
        selectState.onMouseDragged(new Point2D(15, 15), mockGeoEngine); // Drag intermedio
        selectState.onMouseDragged(p_dragEnd, mockGeoEngine);          // Drag finale a (20,20)

        // 3. Simula release
        selectState.onMouseReleased(p_dragEnd, mockGeoEngine);

        // 4. Verifica MoveShapeCommand
        ArgumentCaptor<Command> commandCaptor = ArgumentCaptor.forClass(Command.class);
        verify(mockCommandManager, times(1)).execute(commandCaptor.capture());

        Command executedCommand = commandCaptor.getValue();
        assertThat(executedCommand).isInstanceOf(MoveShapeCommand.class);

        // Verifica il displacement del MoveShapeCommand
        // Per fare ciò, avremmo bisogno di un modo per accedere ai campi di MoveShapeCommand,
        // o mockare il costruttore di MoveShapeCommand per catturare gli argomenti.
        // In questo test unitario di SelectState, ci concentriamo sul fatto che il comando GIUSTO
        // sia stato creato e passato al CommandManager. I test di MoveShapeCommand
        // verificano che MoveShapeCommand stesso funzioni.

        // Come compromesso, possiamo verificare che la Shape passata al MoveShapeCommand sia mockShape1.
        // Se potessimo ispezionare MoveShapeCommand:
        // MoveShapeCommand moveCmd = (MoveShapeCommand) executedCommand;
        // assertThat(moveCmd.getTargetShape()).isEqualTo(mockShape1); // Assumendo un getter o reflection
        // assertThat(moveCmd.getDisplacement()).isEqualTo(new Vector2D(10, 10)); // (20-10, 20-10)
    }

    @Test
    @DisplayName("onMouseReleased senza drag significativo (click) NON dovrebbe creare MoveShapeCommand")
    void testOnMouseReleasedClickNoDrag() {
        when(mockGeoEngine.getSelectedShape()).thenReturn(mockShape1);
        selectState.onMousePressed(p_onShape1, mockGeoEngine); // press a (10,10)
        // Nessun onMouseDragged significativo, isDragging rimane false o il delta è piccolo

        selectState.onMouseReleased(p_noDragRelease, mockGeoEngine); // release a (10,10)
        verify(mockCommandManager, never()).execute(any(MoveShapeCommand.class));

        // Test con un piccolo drag, sotto la tolleranza
        selectState.onMousePressed(p_onShape1, mockGeoEngine); // press a (10,10)
        Point2D slightlyMoved = new Point2D(p_onShape1.getX() + 0.0001, p_onShape1.getY());
        selectState.onMouseDragged(slightlyMoved, mockGeoEngine);
        selectState.onMouseReleased(slightlyMoved, mockGeoEngine);
        verify(mockCommandManager, never()).execute(any(MoveShapeCommand.class)); // Ancora nessuna esecuzione
    }

    @Test
    @DisplayName("onMouseReleased senza forma selezionata o senza press non dovrebbe fare nulla")
    void testOnMouseReleasedNoSelectionOrPress() {
        // Caso 1: Nessuna forma selezionata
        when(mockGeoEngine.getSelectedShape()).thenReturn(null);
        selectState.onMousePressed(p_empty, mockGeoEngine); // Imposta pressPosition e isDragging = false
        selectState.onMouseDragged(p_dragEnd, mockGeoEngine); // isDragging potrebbe diventare true, ma currentSelection è null
        selectState.onMouseReleased(p_dragEnd, mockGeoEngine);
        verifyNoInteractions(mockCommandManager);

        // Caso 2: Nessun press precedente (pressMousePosition è null)
        SelectState freshSelectState = new SelectState();
        when(mockGeoEngine.getSelectedShape()).thenReturn(mockShape1); // Anche se c'è una selezione
        freshSelectState.onMouseReleased(p_dragEnd, mockGeoEngine);   // Ma pressMousePosition è null
        verifyNoInteractions(mockCommandManager);
    }
    
    @Test
    @DisplayName("getToolName dovrebbe restituire 'Select Tool'")
    void testGetToolName() {
        assertThat(selectState.getToolName()).isEqualTo("Select Tool");
    }

    @Test
    @DisplayName("onEnterState dovrebbe resettare gli stati di drag")
    void testOnEnterStateResetsDragStates() {
        // 1. Simula uno stato di drag precedente
        when(mockGeoEngine.getSelectedShape()).thenReturn(mockShape1);
        selectState.onMousePressed(p_onShape1, mockGeoEngine);
        selectState.onMouseDragged(p_dragEnd, mockGeoEngine); // isDragging diventa true

        // 2. Entra nello stato
        selectState.onEnterState(mockGeoEngine);

        // 3. Verifica che un successivo onMouseReleased non crei un comando
        //    perché isDragging e pressMousePosition dovrebbero essere stati resettati.
        Mockito.reset(mockCommandManager);
        selectState.onMouseReleased(p_dragEnd, mockGeoEngine); // pressMousePosition è null
        verify(mockCommandManager, never()).execute(any(Command.class));
    }

    @Test
    @DisplayName("onExitState dovrebbe resettare gli stati di drag")
    void testOnExitStateResetsDragStates() {
        when(mockGeoEngine.getSelectedShape()).thenReturn(mockShape1);
        selectState.onMousePressed(p_onShape1, mockGeoEngine);
        selectState.onMouseDragged(p_dragEnd, mockGeoEngine);

        selectState.onExitState(mockGeoEngine);

        Mockito.reset(mockCommandManager);
        selectState.onMouseReleased(p_dragEnd, mockGeoEngine); // pressMousePosition è null
        verify(mockCommandManager, never()).execute(any(Command.class));
    }
}