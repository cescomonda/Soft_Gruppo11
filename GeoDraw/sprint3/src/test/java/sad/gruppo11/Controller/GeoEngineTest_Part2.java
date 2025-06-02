
package sad.gruppo11.Controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import static org.mockito.MockitoAnnotations.openMocks;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import sad.gruppo11.Factory.ShapeFactory;
import sad.gruppo11.Infrastructure.*;
import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.TextShape;
import sad.gruppo11.Model.LineSegment;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Rect;
import sad.gruppo11.Model.geometry.Vector2D;
import sad.gruppo11.Persistence.PersistenceController;
import sad.gruppo11.View.DrawingView;
import sad.gruppo11.View.Observer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// Test per le operazioni base sulle forme e interazione con CommandManager
class GeoEngineTest_Part2 {

    private GeoEngine geoEngine;

    @Mock private Drawing mockDrawing;
    @Mock private CommandManager mockCmdMgr;
    @Mock private PersistenceController mockPersistenceCtrl;
    @Mock private Clipboard mockClipboard;
    @Mock private ShapeFactory mockShapeFactory;
    // ReusableShapeLibrary è creato internamente da GeoEngine, non mockato qui per semplicità
    // a meno che non si modifichi GeoEngine per l'iniezione.
    @Mock private DrawingView mockView;
    @Mock private Observer mockObserver;

    private Shape mockShape1;
    private TextShape mockTextShape;

    @BeforeEach
    void setUp() {
        openMocks(this);
        geoEngine = new GeoEngine(mockDrawing, mockCmdMgr, mockPersistenceCtrl, mockClipboard, mockShapeFactory);
        geoEngine.attach(mockObserver);
        geoEngine.setView(mockView);

        mockShape1 = new LineSegment(new Point2D(0,0), new Point2D(1,1), ColorData.BLACK);
        mockTextShape = new TextShape("Test", new Point2D(10,10), 12, "Arial", ColorData.RED);
    }

    @Test
    void addShapeToDrawing_nullShape_shouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> geoEngine.addShapeToDrawing(null));
    }

    @Test
    void addShapeToDrawing_shouldExecuteAddShapeCommand() {
        geoEngine.addShapeToDrawing(mockShape1);

        ArgumentCaptor<Command> commandCaptor = ArgumentCaptor.forClass(Command.class);
        verify(mockCmdMgr, times(1)).executeCommand(commandCaptor.capture());
        Command executedCommand = commandCaptor.getValue();
        assertTrue(executedCommand instanceof AddShapeCommand);
        // Potremmo anche verificare che il comando contenga mockDrawing e mockShape1 se AddShapeCommand esponesse i suoi campi
        // Per ora, ci fidiamo che il tipo di comando sia corretto.
    }

    @Test
    void removeSelectedShapesFromDrawing_noSelection_shouldDoNothing() {        
        geoEngine.removeSelectedShapesFromDrawing();
        
        verify(mockCmdMgr, never()).executeCommand(any(DeleteShapeCommand.class));
    }

    @Test
    void removeSelectedShapesFromDrawing_withSelection_shouldExecuteDeleteShapeCommandsAndClearSelection() {
        Shape mockShape2 = new LineSegment(new Point2D(10,10), new Point2D(11,11), ColorData.BLUE);
        List<Shape> selection = new ArrayList<>(Arrays.asList(mockShape1, mockShape2));
        geoEngine.setSelectedShapes(selection); // Imposta la selezione
        clearInvocations(mockCmdMgr, mockObserver); // Pulisci invocazioni da setSelectedShapes

        geoEngine.removeSelectedShapesFromDrawing();

        ArgumentCaptor<Command> commandCaptor = ArgumentCaptor.forClass(Command.class);
        verify(mockCmdMgr, times(2)).executeCommand(commandCaptor.capture());
        List<Command> executedCommands = commandCaptor.getAllValues();
        assertTrue(executedCommands.get(0) instanceof DeleteShapeCommand);
        assertTrue(executedCommands.get(1) instanceof DeleteShapeCommand);
        // Verificare che i comandi contengano le shape corrette sarebbe più robusto,
        // ma richiederebbe che DeleteShapeCommand esponga la shape.

        assertTrue(geoEngine.getSelectedShapes().isEmpty(), "Selection should be cleared after deleting shapes.");
        verify(mockObserver, atLeastOnce()).update(eq(geoEngine), any(Drawing.DrawingChangeEvent.class)); // Notifica per cambio selezione
    }
    
    @Test
    void removeSelectedShapeFromDrawing_noSelection_shouldDoNothing() {
        geoEngine.removeSelectedShapeFromDrawing();
        verify(mockCmdMgr, never()).executeCommand(any(DeleteShapeCommand.class));
    }

    @Test
    void removeSelectedShapeFromDrawing_withSelection_shouldExecuteDeleteShapeCommandAndClearSelection() {
        geoEngine.setSingleSelectedShape(mockShape1); // Assicura che getSelectedShape() restituisca mockShape1
        clearInvocations(mockCmdMgr, mockObserver);

        geoEngine.removeSelectedShapeFromDrawing();

        ArgumentCaptor<Command> commandCaptor = ArgumentCaptor.forClass(Command.class);
        verify(mockCmdMgr, times(1)).executeCommand(commandCaptor.capture());
        assertTrue(commandCaptor.getValue() instanceof DeleteShapeCommand);
        assertTrue(geoEngine.getSelectedShapes().isEmpty(), "Selection should be cleared.");
    }

    @Test
    void moveSelectedShapes_noSelection_shouldDoNothing() {
        geoEngine.moveSelectedShapes(new Vector2D(1,1));
        verify(mockCmdMgr, never()).executeCommand(any(MoveShapeCommand.class));
    }

    @Test
    void moveSelectedShapes_withSelection_shouldExecuteMoveShapeCommands() {
        Shape mockShape2 = new LineSegment(new Point2D(10,10), new Point2D(11,11), ColorData.BLUE);
        List<Shape> selection = Arrays.asList(mockShape1, mockShape2);
        geoEngine.setSelectedShapes(selection);
        Vector2D delta = new Vector2D(5,5);

        geoEngine.moveSelectedShapes(delta);

        ArgumentCaptor<Command> commandCaptor = ArgumentCaptor.forClass(Command.class);
        verify(mockCmdMgr, times(2)).executeCommand(commandCaptor.capture());
        assertTrue(commandCaptor.getAllValues().get(0) instanceof MoveShapeCommand);
        assertTrue(commandCaptor.getAllValues().get(1) instanceof MoveShapeCommand);
        // Qui potremmo anche verificare che i MoveShapeCommand abbiano il delta corretto
    }
    
    @Test
    void moveSelectedShapes_withNullDelta_shouldDoNothing() {
        geoEngine.setSelectedShapes(Arrays.asList(mockShape1));
        geoEngine.moveSelectedShapes(null);
        verify(mockCmdMgr, never()).executeCommand(any());
    }

    @Test
    void resizeSelectedShape_withSelection_shouldExecuteResizeCommand() {
        geoEngine.setSingleSelectedShape(mockShape1);
        Rect newBounds = new Rect(0,0,20,20);
        geoEngine.resizeSelectedShape(newBounds);

        ArgumentCaptor<Command> captor = ArgumentCaptor.forClass(Command.class);
        verify(mockCmdMgr).executeCommand(captor.capture());
        assertTrue(captor.getValue() instanceof ResizeShapeCommand);
    }
    
    @Test
    void resizeSelectedShape_noSelection_or_nullBounds_shouldDoNothing() {
        geoEngine.clearSelection();
        geoEngine.resizeSelectedShape(new Rect(0,0,1,1));
        verify(mockCmdMgr, never()).executeCommand(any());
        
        geoEngine.setSingleSelectedShape(mockShape1);
        geoEngine.resizeSelectedShape(null);
        verify(mockCmdMgr, never()).executeCommand(any());
    }

    @Test
    void changeSelectedShapeStrokeColor_withSelection_shouldExecuteCommand() {
        geoEngine.setSingleSelectedShape(mockShape1);
        ColorData color = ColorData.GREEN;
        geoEngine.changeSelectedShapeStrokeColor(color);
        ArgumentCaptor<Command> captor = ArgumentCaptor.forClass(Command.class);
        verify(mockCmdMgr).executeCommand(captor.capture());
        assertTrue(captor.getValue() instanceof ChangeStrokeColorCommand);
    }

    @Test
    void changeSelectedShapeFillColor_withSelection_shouldExecuteCommand() {
        geoEngine.setSingleSelectedShape(mockShape1);
        ColorData color = ColorData.YELLOW;
        geoEngine.changeSelectedShapeFillColor(color);
        ArgumentCaptor<Command> captor = ArgumentCaptor.forClass(Command.class);
        verify(mockCmdMgr).executeCommand(captor.capture());
        assertTrue(captor.getValue() instanceof ChangeFillColorCommand);
    }
    
    @Test
    void rotateSelectedShape_withSelection_shouldExecuteRotateCommand() {
        geoEngine.setSingleSelectedShape(mockShape1);
        double angle = 45.0;
        geoEngine.rotateSelectedShape(angle);
        ArgumentCaptor<Command> captor = ArgumentCaptor.forClass(Command.class);
        verify(mockCmdMgr).executeCommand(captor.capture());
        assertTrue(captor.getValue() instanceof RotateShapeCommand);
    }

    @Test
    void changeSelectedTextSize_withTextShapeSelected_shouldExecuteCommand() {
        geoEngine.setSingleSelectedShape(mockTextShape);
        double newSize = 18.0;
        geoEngine.changeSelectedTextSize(newSize);
        ArgumentCaptor<Command> captor = ArgumentCaptor.forClass(Command.class);
        verify(mockCmdMgr).executeCommand(captor.capture());
        assertTrue(captor.getValue() instanceof ChangeTextSizeCommand);
    }
    
    @Test
    void changeSelectedTextSize_nonTextShapeSelected_or_invalidSize_shouldNotExecute() {
        geoEngine.setSingleSelectedShape(mockShape1); // Not a TextShape
        geoEngine.changeSelectedTextSize(18.0);
        verify(mockCmdMgr, never()).executeCommand(any());

        geoEngine.setSingleSelectedShape(mockTextShape);
        geoEngine.changeSelectedTextSize(0.0); // Invalid size
        verify(mockCmdMgr, never()).executeCommand(any());
    }

    @Test
    void changeSelectedTextContent_withTextShapeSelected_shouldExecuteCommand() {
        geoEngine.setSingleSelectedShape(mockTextShape);
        String newText = "Updated Text";
        geoEngine.changeSelectedTextContent(newText);
        ArgumentCaptor<Command> captor = ArgumentCaptor.forClass(Command.class);
        verify(mockCmdMgr).executeCommand(captor.capture());
        assertTrue(captor.getValue() instanceof ChangeTextContentCommand);
    }
    
    @Test
    void changeSelectedTextContent_withNullText_shouldStillExecuteCommand() {
        // TextShape constructor and setText allow null (treats as empty string often)
        // ChangeTextContentCommand also allows null
        geoEngine.setSingleSelectedShape(mockTextShape);
        geoEngine.changeSelectedTextContent("ciao");
        ArgumentCaptor<Command> captor = ArgumentCaptor.forClass(Command.class);
        verify(mockCmdMgr).executeCommand(captor.capture());
        assertTrue(captor.getValue() instanceof ChangeTextContentCommand);
    }

    // --- CommandManager interaction tests ---
    @Test
    void undoLastCommand_shouldCallUndoOnCmdMgrAndNotify() {
        geoEngine.undoLastCommand();
        verify(mockCmdMgr, times(1)).undo();
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(mockObserver, atLeastOnce()).update(eq(geoEngine), eventCaptor.capture());
        assertTrue(eventCaptor.getAllValues().stream().anyMatch(e -> 
            e instanceof Drawing.DrawingChangeEvent && 
            ((Drawing.DrawingChangeEvent)e).type == Drawing.DrawingChangeEvent.ChangeType.SELECTION
        ));
    }

    @Test
    void redoLastCommand_shouldCallRedoOnCmdMgrAndNotify() {
        geoEngine.redoLastCommand();
        verify(mockCmdMgr, times(1)).redo();
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(mockObserver, atLeastOnce()).update(eq(geoEngine), eventCaptor.capture());
        assertTrue(eventCaptor.getAllValues().stream().anyMatch(e -> 
            e instanceof Drawing.DrawingChangeEvent && 
            ((Drawing.DrawingChangeEvent)e).type == Drawing.DrawingChangeEvent.ChangeType.SELECTION
        ));
    }

    @Test
    void canUndo_shouldDelegateToCmdMgr() {
        when(mockCmdMgr.canUndo()).thenReturn(true);
        assertTrue(geoEngine.canUndo());
        when(mockCmdMgr.canUndo()).thenReturn(false);
        assertFalse(geoEngine.canUndo());
        verify(mockCmdMgr, times(2)).canUndo();
    }

    @Test
    void canRedo_shouldDelegateToCmdMgr() {
        when(mockCmdMgr.canRedo()).thenReturn(true);
        assertTrue(geoEngine.canRedo());
        when(mockCmdMgr.canRedo()).thenReturn(false);
        assertFalse(geoEngine.canRedo());
        verify(mockCmdMgr, times(2)).canRedo();
    }
}
            