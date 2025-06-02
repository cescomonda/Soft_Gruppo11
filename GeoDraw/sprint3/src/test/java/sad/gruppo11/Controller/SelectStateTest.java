
package sad.gruppo11.Controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.GroupShape;
import sad.gruppo11.Model.LineSegment;
import sad.gruppo11.Model.RectangleShape;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Rect;
import sad.gruppo11.Model.geometry.Vector2D;
import sad.gruppo11.View.DrawingView;
import sad.gruppo11.Infrastructure.Command;
import sad.gruppo11.Infrastructure.CommandManager;
import sad.gruppo11.Infrastructure.MoveShapeCommand;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class SelectStateTest {

    private SelectState selectState;
    private GeoEngine mockGeoEngine;
    private Drawing mockDrawing;
    private DrawingView mockDrawingView;
    private CommandManager mockCommandManager;

    private Shape mockShape1, mockShape2, mockShapeInGroup, mockGroup;
    private Point2D p_onShape1, p_onShape2, p_onShapeInGroup, p_empty, p_dragEnd;

    @BeforeEach
    void setUp() {
        mockDrawing = mock(Drawing.class);
        // selectState viene inizializzato dopo che mockDrawing è pronto
        
        mockGeoEngine = mock(GeoEngine.class);
        mockDrawingView = mock(DrawingView.class);
        mockCommandManager = mock(CommandManager.class);

        // Inizializza i punti come prima
        p_onShape1 = new Point2D(5,5);
        p_onShape2 = new Point2D(25,25);
        p_onShapeInGroup = new Point2D(32,32);
        p_empty = new Point2D(50,50);
        p_dragEnd = new Point2D(60,60);

        // Crea istanze reali e poi wrappale con spy()
        Shape realShape1 = new LineSegment(new Point2D(0,0), new Point2D(10,10), ColorData.RED);
        mockShape1 = spy(realShape1);

        Shape realShape2 = new RectangleShape(new Rect(20,20,10,10), ColorData.BLUE, ColorData.GREEN);
        mockShape2 = spy(realShape2);
        
        Shape realShapeInGroup = new LineSegment(new Point2D(30,30), new Point2D(35,35), ColorData.YELLOW);
        mockShapeInGroup = spy(realShapeInGroup); // Spy anche per il figlio se necessario

        List<Shape> groupChildren = new ArrayList<>(Arrays.asList(mockShapeInGroup)); // Usa lo spy del figlio
        GroupShape realGroup = new GroupShape(groupChildren);
        mockGroup = spy(realGroup);

        // Ora inizializza selectState DOPO che mockDrawing è un mock valido.
        selectState = new SelectState(mockDrawing);


        when(mockGeoEngine.getDrawing()).thenReturn(mockDrawing);
        when(mockGeoEngine.getView()).thenReturn(mockDrawingView);
        when(mockGeoEngine.getCommandManager()).thenReturn(mockCommandManager);

        List<Shape> shapesInZOrder = new ArrayList<>(Arrays.asList(mockShape1, mockShape2, mockGroup));
        when(mockDrawing.getShapesInZOrder()).thenReturn(shapesInZOrder);

        // Ora lo stubbing su spy dovrebbe funzionare
        when(mockShape1.contains(p_onShape1)).thenReturn(true);
        when(mockShape2.contains(p_onShape2)).thenReturn(true);
        // Per mockGroup, se il suo contains() chiama contains() dei figli,
        // lo spy sul mockGroup chiamerà il metodo reale, che a sua volta chiamerà mockShapeInGroup.contains().
        // Potrebbe essere necessario stubbare mockShapeInGroup.contains() se il punto è specifico per esso.
        when(mockGroup.contains(p_onShapeInGroup)).thenReturn(true); // Puoi stubbare direttamente lo spy del gruppo
        // Oppure, se vuoi testare la logica interna di Group.contains:
        // when(mockShapeInGroup.contains(p_onShapeInGroup)).thenReturn(true); // E Group.contains userà questo.

        // Stub getBounds() per gli spy
        // Se usi spy(), i metodi reali vengono chiamati a meno che non siano stubbati.
        // Quindi getBounds() sulle istanze reali sottostanti funzionerà.
        // Ma se vuoi forzare un valore specifico per getBounds() per il test, puoi stubbarlo:
        // when(mockShape1.getBounds()).thenReturn(new Rect(0,0,10,10)); // Già corretto perché è uno spy
        // when(mockShape2.getBounds()).thenReturn(new Rect(20,20,10,10));
        // when(mockGroup.getBounds()).thenReturn(new Rect(30,30,5,5)); 
        // Non è strettamente necessario ri-stubbare getBounds() se gli oggetti reali sottostanti 
        // restituiscono già quei valori e non li modifichi. Ma per chiarezza e controllo, può essere utile.
        // Per ora, mi fido che gli oggetti reali abbiano questi bounds.

        when(mockGeoEngine.isShiftKeyPressed()).thenReturn(false);
        when(mockGeoEngine.getSelectedShapes()).thenReturn(Collections.emptyList());
    }

    @Test
    void getName_shouldReturnCorrectName() {
        assertEquals("SelectTool", selectState.getName());
    }

    @Test
    void activate_shouldShowMessage() {
        selectState.activate(mockGeoEngine);
        verify(mockDrawingView).showUserMessage(contains("Select Tool"));
        // Does not clear selection on activate
        verify(mockGeoEngine, never()).clearSelection();
    }

    @Test
    void deactivate_shouldClearTemporaryVisualsAndMessage() {
        selectState.activate(mockGeoEngine);
        selectState.deactivate(mockGeoEngine);
        verify(mockDrawingView).clearTemporaryVisuals();
        verify(mockDrawingView).clearUserMessage();
    }

    @Test
    void onMousePressed_onEmptySpace_noShift_startsSelectAreaAndClearsSelection() {
        // Simulate some shape was previously selected
        when(mockGeoEngine.getSelectedShapes()).thenReturn(Arrays.asList(mockShape1));

        selectState.onMousePressed(mockGeoEngine, p_empty);
        
        // SelectState itself doesn't clear selection directly on press for select area.
        // GeoEngine would handle clearing if a new selection context starts.
        // The provided SelectState.onMousePressed for empty space:
        // if (!isShiftDown) { currentMode = Mode.SELECT_AREA; ...}
        // It does NOT call engine.clearSelection() here.
        // GeoEngine's setSelectedShapes/setSingleSelectedShape handles clearing existing.
        // This test might need to verify that if SELECT_AREA implies new selection,
        // then onMouseReleased for SELECT_AREA with no shift *will* clear.
        // For onMousePressed itself, it just sets mode.

        // Let's assume the test implies GeoEngine.clearSelection is called by the *engine* logic
        // when a selection action begins on empty space without shift.
        // The SelectState does: if (!isShiftDown) { currentMode = Mode.SELECT_AREA; ... }
        // It doesn't directly call clearSelection. Let's test its direct actions.

        // If the intent is that a click on empty without shift always clears, then GeoEngine
        // or the state needs to do it earlier.
        // The current SelectState code *does not* call engine.clearSelection() on press in empty space without shift.
        // It sets mode = SELECT_AREA.
        // Then onMouseReleased: if (!isShiftDown) engine.clearSelection();
        // This test should focus on onMousePressed behavior:
        // No, looking at the provided SelectState:
        // line 200: if (selectionAreaRect != null && ...) { ... if (!isShiftDown) engine.clearSelection(); ... }
        // This happens onMouseReleased for area selection.
        // For simple click (no drag for area):
        // press -> release. If no drag, selectionAreaRect might be tiny.
        // The code onMousePressed on empty space:
        // else { // Click su area vuota
        //    if (!isShiftDown) { 
        //        currentMode = Mode.SELECT_AREA; // THIS IS THE KEY
        //        selectionAreaRect = new Rect(pressPosWorld, 0, 0); ...
        //    } else {
        //        engine.clearSelection(); // << THIS IS WITH SHIFT ON EMPTY
        //        currentMode = Mode.PAN_VIEW;
        //    }
        // }
        // So, no-shift on empty sets SELECT_AREA. Shift on empty clears and pans.

        // Corrected test for onMousePressed on empty WITHOUT shift:
        selectState.onMousePressed(mockGeoEngine, p_empty);
        verify(mockGeoEngine, never()).clearSelection(); // Not cleared on press for SELECT_AREA mode start
        verify(mockGeoEngine, times(2)).notifyViewToRefresh();
        // Ghost for selection area is handled in onMouseDragged. Here only selectionAreaRect is initialized.
    }
    
    @Test
    void onMousePressed_onEmptySpace_withShift_clearsSelectionAndSetsPanMode() {
        when(mockGeoEngine.isShiftKeyPressed()).thenReturn(true);
        selectState.onMousePressed(mockGeoEngine, p_empty);
        
        verify(mockGeoEngine, times(1)).clearSelection(); // With shift on empty, selection is cleared.
        verify(mockGeoEngine, times(1)).notifyViewToRefresh();
        // Mode is set to PAN_VIEW internally
    }


    @Test
    void onMousePressed_onShape_noShift_selectsShape() {
        when(mockGeoEngine.getSelectedShapes()).thenReturn(Collections.emptyList()); // Start with no selection

        selectState.onMousePressed(mockGeoEngine, p_onShape1);

        verify(mockGeoEngine, times(1)).setSingleSelectedShape(mockShape1);
        verify(mockGeoEngine, times(1)).notifyViewToRefresh();
    }
    
    @Test
    void onMousePressed_onAlreadySelectedShape_noShift_keepsItSelectedAndPreparesForDrag() {
        when(mockGeoEngine.getSelectedShapes()).thenReturn(Arrays.asList(mockShape1)); // shape1 already selected

        selectState.onMousePressed(mockGeoEngine, p_onShape1);

        verify(mockGeoEngine, never()).setSingleSelectedShape(any()); // Not re-selected
        verify(mockGeoEngine, never()).addShapeToSelection(any());
        verify(mockGeoEngine, times(1)).notifyViewToRefresh();
        // Mode is DRAG_SELECTION, shapesBeingDragged contains mockShape1
    }

    @Test
    void onMousePressed_onShape_withShift_addsToSelection() {
        when(mockGeoEngine.getSelectedShapes()).thenReturn(new ArrayList<>(Arrays.asList(mockShape2))); // shape2 selected
        when(mockGeoEngine.isShiftKeyPressed()).thenReturn(true);

        selectState.onMousePressed(mockGeoEngine, p_onShape1); // Click on shape1

        verify(mockGeoEngine, times(1)).addShapeToSelection(mockShape1);
        verify(mockGeoEngine, times(1)).notifyViewToRefresh();
    }

    @Test
    void onMousePressed_onAlreadySelectedShape_withShift_deselectsIt() {
        // Setup GeoEngine to return a mutable list for getSelectedShapes
        List<Shape> currentSelection = new ArrayList<>(Arrays.asList(mockShape1, mockShape2));
        when(mockGeoEngine.getSelectedShapes()).thenReturn(currentSelection);
        when(mockGeoEngine.isShiftKeyPressed()).thenReturn(true);
        
        // Argument captor for setSelectedShapes
        ArgumentCaptor<List<Shape>> selectionCaptor = ArgumentCaptor.forClass(List.class);

        selectState.onMousePressed(mockGeoEngine, p_onShape1); // Click on shape1 (which is selected)

        // Verify setSelectedShapes is called with a list NOT containing mockShape1
        verify(mockGeoEngine, times(1)).setSelectedShapes(selectionCaptor.capture());
        List<Shape> newSelection = selectionCaptor.getValue();
        assertFalse(newSelection.contains(mockShape1), "Shape1 should be deselected.");
        assertTrue(newSelection.contains(mockShape2), "Shape2 should remain selected.");
        assertEquals(1, newSelection.size());
        
        verify(mockGeoEngine, times(1)).notifyViewToRefresh();
    }
    
    @Test
    void onMousePressed_onShapeInGroup_selectsTheTopLevelGroup() {
        // Setup: mockGroup contains mockShapeInGroup. mockGroup is in drawing.
        // pickTopMostShapeOrGroup in SelectState should return mockGroup when p_onShapeInGroup is clicked.
        // To make this work correctly, the stubbing of mockDrawing.getShapesInZOrder() is important.
        // And the logic of getTopLevelParentGroup within SelectState.

        when(mockGeoEngine.getSelectedShapes()).thenReturn(Collections.emptyList());
        
        // Simulate pickTopMostShapeOrGroup finding mockGroup (because p_onShapeInGroup hits a child of mockGroup)
        // This means when(mockGroup.contains(p_onShapeInGroup)) must be true
        // And mockShapeInGroup might also be checked if iteration goes deeper.

        selectState.onMousePressed(mockGeoEngine, p_onShapeInGroup);

        verify(mockGeoEngine, times(1)).setSingleSelectedShape(mockGroup);
    }


    @Test
    void onMouseDragged_inDragSelectionMode_movesShapesInView() {
        // Setup: shape1 is selected and pressed on
        when(mockGeoEngine.getSelectedShapes()).thenReturn(Arrays.asList(mockShape1));
        selectState.onMousePressed(mockGeoEngine, p_onShape1); // Sets mode to DRAG_SELECTION, prepares shapesBeingDragged
        clearInvocations(mockGeoEngine, mockShape1); // Clear press interactions

        Vector2D dragDelta = new Vector2D(p_dragEnd.getX() - p_onShape1.getX(), p_dragEnd.getY() - p_onShape1.getY());
        // selectState.onMouseDragged will use (currentWorldPoint - lastDragPosWorld)
        // First drag: lastDragPosWorld is pressPosWorld (p_onShape1)
        
        selectState.onMouseDragged(mockGeoEngine, p_dragEnd);

        // Verify shape.move() was called on the shape being dragged for visual feedback
        // The actual delta passed to shape.move would be (p_dragEnd - p_onShape1)
        ArgumentCaptor<Vector2D> moveCaptor = ArgumentCaptor.forClass(Vector2D.class);
        verify(mockDrawing, times(1)).moveShape(eq(mockShape1), moveCaptor.capture());
        assertEquals(p_dragEnd.getX() - p_onShape1.getX(), moveCaptor.getValue().getDx(), 0.001);
        assertEquals(p_dragEnd.getY() - p_onShape1.getY(), moveCaptor.getValue().getDy(), 0.001);
        
    }

    @Test
    void onMouseDragged_inSelectAreaMode_drawsGhostRectangle() {
        selectState.onMousePressed(mockGeoEngine, p_empty); // Start SELECT_AREA
        clearInvocations(mockDrawingView, mockGeoEngine);

        selectState.onMouseDragged(mockGeoEngine, p_dragEnd); // Drag to create area

        ArgumentCaptor<Shape> ghostCaptor = ArgumentCaptor.forClass(Shape.class);
        verify(mockDrawingView, times(1)).drawTemporaryGhostShape(ghostCaptor.capture());
        assertTrue(ghostCaptor.getValue() instanceof RectangleShape);
        Rect expectedSelectionRect = new Rect(
            Math.min(p_empty.getX(), p_dragEnd.getX()),
            Math.min(p_empty.getY(), p_dragEnd.getY()),
            Math.abs(p_empty.getX() - p_dragEnd.getX()),
            Math.abs(p_empty.getY() - p_dragEnd.getY())
        );
        assertEquals(expectedSelectionRect, ghostCaptor.getValue().getBounds());
        verify(mockGeoEngine, times(1)).notifyViewToRefresh();
    }
    
    @Test
    void onMouseDragged_inPanMode_scrollsView() {
        when(mockGeoEngine.isShiftKeyPressed()).thenReturn(true);
        selectState.onMousePressed(mockGeoEngine, p_empty); // Sets PAN_VIEW mode
        clearInvocations(mockGeoEngine);

        selectState.onMouseDragged(mockGeoEngine, p_dragEnd);
        
        double deltaX = p_dragEnd.getX() - p_empty.getX();
        double deltaY = p_dragEnd.getY() - p_empty.getY();
        verify(mockGeoEngine, times(1)).scroll(deltaX, deltaY);
    }


    @Test
    void onMouseReleased_afterDragSelection_executesMoveCommands() {
        // Setup: shape1 selected and dragged from p_onShape1 to p_dragEnd
        when(mockGeoEngine.getSelectedShapes()).thenReturn(Arrays.asList(mockShape1));
        selectState.onMousePressed(mockGeoEngine, p_onShape1);
        selectState.onMouseDragged(mockGeoEngine, p_dragEnd); // Visual drag
        clearInvocations(mockGeoEngine, mockShape1, mockCommandManager); // Clear drag interactions

        // Before releasing, shape1 has been visually moved to p_dragEnd's relative position
        // We need to account for the visual move being undone before command
        Vector2D totalDragVector = new Vector2D(p_dragEnd.getX() - p_onShape1.getX(), p_dragEnd.getY() - p_onShape1.getY());

        selectState.onMouseReleased(mockGeoEngine, p_dragEnd);
        
        // 1. Verify visual move is undone
        ArgumentCaptor<Vector2D> undoVisualMoveCaptor = ArgumentCaptor.forClass(Vector2D.class);
        verify(mockShape1, times(1)).move(undoVisualMoveCaptor.capture());
        assertEquals(-totalDragVector.getDx(), undoVisualMoveCaptor.getValue().getDx(), 0.001);
        assertEquals(-totalDragVector.getDy(), undoVisualMoveCaptor.getValue().getDy(), 0.001);

        // 2. Verify MoveShapeCommand is executed
        ArgumentCaptor<Command> commandCaptor = ArgumentCaptor.forClass(Command.class);
        verify(mockCommandManager, times(1)).executeCommand(commandCaptor.capture());
        assertTrue(commandCaptor.getValue() instanceof MoveShapeCommand);
        // Further checks on the MoveShapeCommand content could be done if needed
    }
    
    @Test
    void onMouseReleased_afterClickOnShapeWithNoDrag_noMoveCommand() {
        when(mockGeoEngine.getSelectedShapes()).thenReturn(Arrays.asList(mockShape1));
        selectState.onMousePressed(mockGeoEngine, p_onShape1);
        // No drag
        clearInvocations(mockGeoEngine, mockShape1, mockCommandManager);

        selectState.onMouseReleased(mockGeoEngine, p_onShape1); // Released at same point (or very close)
        
        verify(mockShape1, never()).move(any(Vector2D.class)); // No visual move, no undo visual move
        verify(mockCommandManager, never()).executeCommand(any(MoveShapeCommand.class));
        verify(mockGeoEngine, times(1)).notifyViewToRefresh(); // Still refreshes for selection state
    }
    
    @Test
    void onMouseReleased_clickOnEmptySpace_noDrag_clearsSelection() {
        // Simulate some shape was selected
        when(mockGeoEngine.getSelectedShapes()).thenReturn(Arrays.asList(mockShape1));
        
        selectState.onMousePressed(mockGeoEngine, p_empty); // press on empty
        // No drag
        selectState.onMouseReleased(mockGeoEngine, p_empty); // release on empty
        
        // This path goes: press (mode=SELECT_AREA, selectionAreaRect is tiny)
        // release (selectionAreaRect is tiny, so shapesInArea is empty)
        // BUT, the provided code has:
        // if (selectionAreaRect != null && (selectionAreaRect.getWidth() > 1 || selectionAreaRect.getHeight() > 1))
        // This means a pure click (no drag) *will not* enter this block.
        // It will fall through, and resetState() is called. Selection is not cleared by this.
        // This seems like a potential bug/omission in SelectState for "click on empty deselects".
        //
        // Let's re-verify SelectState onMouseReleased:
        // case SELECT_AREA:
        //   if (view != null) view.clearTemporaryVisuals();
        //   if (selectionAreaRect != null && (selectionAreaRect.getWidth() > 1 || selectionAreaRect.getHeight() > 1)) {
        //     List<Shape> shapesInArea = findShapesInRect(...);
        //     if (!isShiftDown) engine.clearSelection(); // << THIS IS THE KEY
        //     for(Shape s : shapesInArea) engine.addShapeToSelection(getTopLevelParentGroup(drawing, s));
        //   } else { // This else block is where a click-no-drag on empty lands
        //     engine.clearSelection(); // << THIS IS THE FIX/MISSING PART IN ORIGINAL PROVIDED CODE
        //   }
        //   engine.notifyViewToRefresh();
        //   break;
        // The provided code for SelectState.java has this logic in "onMouseReleased" for "SELECT_AREA":
        //   } else { // This is the path for tiny selectionAreaRect (a click)
        //      engine.clearSelection(); //  <-- This line IS present in the provided code.
        //   }
        // So, a click on empty space (no drag, no shift) *should* clear selection.

        verify(mockGeoEngine, times(1)).clearSelection();
        verify(mockGeoEngine, times(2)).notifyViewToRefresh();
    }
}
            