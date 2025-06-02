
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
import sad.gruppo11.Model.LineSegment;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Vector2D;
import sad.gruppo11.Persistence.PersistenceController;
import sad.gruppo11.View.DrawingView;
import sad.gruppo11.View.Observer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Test per Clipboard, Z-Ordering, Riflessione
class GeoEngineTest_Part3 {

    private GeoEngine geoEngine;

    @Mock private Drawing mockDrawing;
    @Mock private CommandManager mockCmdMgr;
    @Mock private PersistenceController mockPersistenceCtrl;
    @Mock private Clipboard mockClipboard;
    @Mock private ShapeFactory mockShapeFactory;
    @Mock private DrawingView mockView;
    @Mock private Observer mockObserver;

    private Shape mockShape1;
    private Shape mockShape2;

    @BeforeEach
    void setUp() {
        openMocks(this);
        geoEngine = new GeoEngine(mockDrawing, mockCmdMgr, mockPersistenceCtrl, mockClipboard, mockShapeFactory);
        geoEngine.attach(mockObserver);
        geoEngine.setView(mockView);

        mockShape1 = new LineSegment(new Point2D(0,0), new Point2D(1,1), ColorData.BLACK);
        mockShape2 = new LineSegment(new Point2D(10,10), new Point2D(11,11), ColorData.RED);
    }

    // --- Clipboard Tests ---
    @Test
    void cutSelectedShape_withSelection_shouldExecuteCutCommandAndRemoveFromSelection() {
        geoEngine.setSingleSelectedShape(mockShape1);
        clearInvocations(mockCmdMgr, mockObserver);

        geoEngine.cutSelectedShape();

        ArgumentCaptor<Command> commandCaptor = ArgumentCaptor.forClass(Command.class);
        verify(mockCmdMgr, times(1)).executeCommand(commandCaptor.capture());
        assertTrue(commandCaptor.getValue() instanceof CutShapeCommand);
        // CutShapeCommand constructor should get (mockDrawing, mockShape1, mockClipboard)

        assertTrue(geoEngine.getSelectedShapes().isEmpty(), "Shape should be removed from selection after cut.");
        
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(mockObserver, atLeastOnce()).update(eq(geoEngine), eventCaptor.capture());
        assertTrue(eventCaptor.getAllValues().stream().anyMatch(e -> 
            e instanceof Drawing.DrawingChangeEvent && 
            ((Drawing.DrawingChangeEvent)e).type == Drawing.DrawingChangeEvent.ChangeType.SELECTION
        ), "Selection change should be notified after cut.");
    }

    @Test
    void cutSelectedShape_noSelection_shouldDoNothing() {
        geoEngine.clearSelection();
        geoEngine.cutSelectedShape();
        verify(mockCmdMgr, never()).executeCommand(any());
    }

    @Test
    void copySelectedShape_withSelection_shouldExecuteCopyCommandAndNotify() {
        geoEngine.setSingleSelectedShape(mockShape1);
        clearInvocations(mockCmdMgr, mockObserver);

        geoEngine.copySelectedShape();

        ArgumentCaptor<Command> commandCaptor = ArgumentCaptor.forClass(Command.class);
        verify(mockCmdMgr, times(1)).executeCommand(commandCaptor.capture());
        assertTrue(commandCaptor.getValue() instanceof CopyShapeCommand);

        verify(mockObserver, times(1)).update(geoEngine, "ClipboardUpdated");
    }

    @Test
    void copySelectedShape_noSelection_shouldDoNothing() {
        geoEngine.clearSelection();
        geoEngine.copySelectedShape();
        verify(mockCmdMgr, never()).executeCommand(any());
        verify(mockObserver, never()).update(geoEngine, "ClipboardUpdated");
    }

    @Test
    void pasteShape_clipboardNotEmpty_shouldExecutePasteCommand() {
        when(mockClipboard.isEmpty()).thenReturn(false); // Simulate clipboard has content
        geoEngine.pasteShape();

        ArgumentCaptor<Command> commandCaptor = ArgumentCaptor.forClass(Command.class);
        verify(mockCmdMgr, times(1)).executeCommand(commandCaptor.capture());
        assertTrue(commandCaptor.getValue() instanceof PasteShapeCommand);
        // Verify default offset is used: (10,10)
        // This requires PasteShapeCommand to expose its offset or test its effect.
    }
    
    @Test
    void pasteShape_withOffset_clipboardNotEmpty_shouldExecutePasteCommandWithOffset() {
        when(mockClipboard.isEmpty()).thenReturn(false);
        Vector2D customOffset = new Vector2D(20, 20);
        geoEngine.pasteShape(customOffset);

        ArgumentCaptor<Command> commandCaptor = ArgumentCaptor.forClass(Command.class);
        verify(mockCmdMgr, times(1)).executeCommand(commandCaptor.capture());
        Command cmd = commandCaptor.getValue();
        assertTrue(cmd instanceof PasteShapeCommand);
        // If PasteShapeCommand had a getter for offset:
        // assertEquals(customOffset, ((PasteShapeCommand)cmd).getOffset()); 
    }

    @Test
    void pasteShape_clipboardEmpty_shouldDoNothing() {
        when(mockClipboard.isEmpty()).thenReturn(true);
        geoEngine.pasteShape();
        verify(mockCmdMgr, never()).executeCommand(any());
    }
    
    @Test
    void pasteShape_withOffset_nullOffset_clipboardNotEmpty_shouldDoNothing() {
        // Current implementation of pasteShape(offset) checks for null offset
        when(mockClipboard.isEmpty()).thenReturn(false);
        geoEngine.pasteShape(null);
        verify(mockCmdMgr, never()).executeCommand(any());
    }

    // --- Z-Ordering Tests ---
    @Test
    void bringSelectedShapeToFront_withSelection_shouldExecuteCommand() {
        geoEngine.setSingleSelectedShape(mockShape1);
        geoEngine.bringSelectedShapeToFront();
        ArgumentCaptor<Command> captor = ArgumentCaptor.forClass(Command.class);
        verify(mockCmdMgr).executeCommand(captor.capture());
        assertTrue(captor.getValue() instanceof BringToFrontCommand);
    }

    @Test
    void bringSelectedShapeToFront_noSelection_shouldDoNothing() {
        geoEngine.clearSelection();
        geoEngine.bringSelectedShapeToFront();
        verify(mockCmdMgr, never()).executeCommand(any());
    }

    @Test
    void sendSelectedShapeToBack_withSelection_shouldExecuteCommand() {
        geoEngine.setSingleSelectedShape(mockShape1);
        geoEngine.sendSelectedShapeToBack();
        ArgumentCaptor<Command> captor = ArgumentCaptor.forClass(Command.class);
        verify(mockCmdMgr).executeCommand(captor.capture());
        assertTrue(captor.getValue() instanceof SendToBackCommand);
    }

    @Test
    void sendSelectedShapeToBack_noSelection_shouldDoNothing() {
        geoEngine.clearSelection();
        geoEngine.sendSelectedShapeToBack();
        verify(mockCmdMgr, never()).executeCommand(any());
    }

    // --- Reflection Tests (Sprint 3) ---
    @Test
    void reflectSelectedShapesHorizontal_noSelection_shouldDoNothing() {
        geoEngine.clearSelection();
        geoEngine.reflectSelectedShapesHorizontal();
        verify(mockCmdMgr, never()).executeCommand(any());
    }

    @Test
    void reflectSelectedShapesHorizontal_withSelection_shouldExecuteReflectCommands() {
        List<Shape> selection = Arrays.asList(mockShape1, mockShape2);
        geoEngine.setSelectedShapes(selection);

        geoEngine.reflectSelectedShapesHorizontal();

        ArgumentCaptor<Command> commandCaptor = ArgumentCaptor.forClass(Command.class);
        verify(mockCmdMgr, times(2)).executeCommand(commandCaptor.capture());
        List<Command> executedCommands = commandCaptor.getAllValues();
        assertTrue(executedCommands.get(0) instanceof ReflectHorizontalCommand);
        assertTrue(executedCommands.get(1) instanceof ReflectHorizontalCommand);
        // Further checks could verify the correct shapes were in the commands.
    }

    @Test
    void reflectSelectedShapesVertical_noSelection_shouldDoNothing() {
        geoEngine.clearSelection();
        geoEngine.reflectSelectedShapesVertical();
        verify(mockCmdMgr, never()).executeCommand(any());
    }

    @Test
    void reflectSelectedShapesVertical_withSelection_shouldExecuteReflectCommands() {
        List<Shape> selection = Arrays.asList(mockShape1, mockShape2);
        geoEngine.setSelectedShapes(selection);

        geoEngine.reflectSelectedShapesVertical();

        ArgumentCaptor<Command> commandCaptor = ArgumentCaptor.forClass(Command.class);
        verify(mockCmdMgr, times(2)).executeCommand(commandCaptor.capture());
        List<Command> executedCommands = commandCaptor.getAllValues();
        assertTrue(executedCommands.get(0) instanceof ReflectVerticalCommand);
        assertTrue(executedCommands.get(1) instanceof ReflectVerticalCommand);
    }
}
            