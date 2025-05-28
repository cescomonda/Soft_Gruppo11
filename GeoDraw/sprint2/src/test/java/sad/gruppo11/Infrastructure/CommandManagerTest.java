
package sad.gruppo11.Infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sad.gruppo11.Model.Drawing;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CommandManagerTest {
    private CommandManager cmdMgr;
    private Command mockCommand;
    private Drawing mockDrawing;

    @BeforeEach
    void setUp() {
        mockDrawing = mock(Drawing.class);
        cmdMgr = new CommandManager(mockDrawing);
        mockCommand = Mockito.mock(Command.class);
    }

    @Test
    void executeCommand() {
        cmdMgr.executeCommand(mockCommand);
        verify(mockCommand).execute();
        assertTrue(cmdMgr.canUndo());
        assertFalse(cmdMgr.canRedo());
        verify(mockDrawing).notifyObservers(any(Drawing.DrawingChangeEvent.class));
    }

    @Test
    void undoCommand() {
        cmdMgr.executeCommand(mockCommand);
        cmdMgr.undo();
        verify(mockCommand).undo();
        assertFalse(cmdMgr.canUndo());
        assertTrue(cmdMgr.canRedo());
        verify(mockDrawing, times(2)).notifyObservers(any(Drawing.DrawingChangeEvent.class)); // 1 for exec, 1 for undo
    }

    @Test
    void redoCommand() {
        cmdMgr.executeCommand(mockCommand);
        cmdMgr.undo();
        cmdMgr.redo();
        verify(mockCommand, times(2)).execute(); // Once for initial, once for redo
        assertTrue(cmdMgr.canUndo());
        assertFalse(cmdMgr.canRedo());
        verify(mockDrawing, times(3)).notifyObservers(any(Drawing.DrawingChangeEvent.class));
    }

    @Test
    void cannotUndoEmptyStack() {
        assertFalse(cmdMgr.canUndo());
        cmdMgr.undo(); // Should do nothing, not throw error
        assertFalse(cmdMgr.canUndo());
        verify(mockDrawing, never()).notifyObservers(any());
    }

    @Test
    void cannotRedoEmptyStack() {
        assertFalse(cmdMgr.canRedo());
        cmdMgr.redo(); // Should do nothing
        assertFalse(cmdMgr.canRedo());
        verify(mockDrawing, never()).notifyObservers(any());
    }
    
    @Test
    void executeClearsRedoStack() {
        cmdMgr.executeCommand(mockCommand);
        cmdMgr.undo();
        assertTrue(cmdMgr.canRedo());
        
        Command mockCommand2 = Mockito.mock(Command.class);
        cmdMgr.executeCommand(mockCommand2); // This should clear redo stack
        
        assertFalse(cmdMgr.canRedo());
        verify(mockCommand2).execute();
        assertTrue(cmdMgr.canUndo()); // mockCommand2 is on undo stack
    }

    @Test
    void clearStacks() {
        cmdMgr.executeCommand(mockCommand);
        cmdMgr.undo();
        assertTrue(cmdMgr.canRedo());
        assertTrue(cmdMgr.canUndo()); // mockCommand is on redo, but execute placed it on undo initially

        cmdMgr.clearStacks();
        assertFalse(cmdMgr.canUndo());
        assertFalse(cmdMgr.canRedo());
    }
    
    @Test
    void commandManagerWithNullDrawing() {
        CommandManager cm = new CommandManager(null); // or new CommandManager()
        cm.executeCommand(mockCommand);
        verify(mockCommand).execute();
        // No NPE should occur for notifyModelChanged
        cm.undo();
        verify(mockCommand).undo();
    }
    
    @Test
    void setDrawingModel() {
        CommandManager cm = new CommandManager();
        Drawing newMockDrawing = mock(Drawing.class);
        cm.setDrawingModel(newMockDrawing);
        cm.executeCommand(mockCommand);
        verify(newMockDrawing).notifyObservers(any(Drawing.DrawingChangeEvent.class));
    }
}
