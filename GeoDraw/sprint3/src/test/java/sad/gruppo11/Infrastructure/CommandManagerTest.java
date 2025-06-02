
package sad.gruppo11.Infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import sad.gruppo11.Model.Drawing;

class CommandManagerTest {

    private CommandManager commandManager;
    private Command mockCommand1;
    private Command mockCommand2;
    private Drawing mockDrawing;

    @BeforeEach
    void setUp() {
        mockDrawing = mock(Drawing.class);
        commandManager = new CommandManager(mockDrawing); // Ora passa il mockDrawing
        mockCommand1 = mock(Command.class);
        mockCommand2 = mock(Command.class);
    }

    @Test
    void initialState_shouldHaveEmptyStacksAndNoUndoRedo() {
        assertFalse(commandManager.canUndo(), "Initially, should not be able to undo.");
        assertFalse(commandManager.canRedo(), "Initially, should not be able to redo.");
    }

    @Test
    void executeCommand_shouldExecuteAndPushToUndoStack() {
        commandManager.executeCommand(mockCommand1);

        verify(mockCommand1, times(1)).execute();
        assertTrue(commandManager.canUndo(), "Should be able to undo after executing a command.");
        assertFalse(commandManager.canRedo(), "Redo stack should be cleared after new command execution.");
    }

    @Test
    void executeCommand_nullCommand_shouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> {
            commandManager.executeCommand(null);
        }, "Executing a null command should throw NullPointerException.");
    }

    @Test
    void executeCommand_clearsRedoStack() {
        commandManager.executeCommand(mockCommand1);
        commandManager.undo(); // mockCommand1 is now on redo stack
        assertTrue(commandManager.canRedo(), "Should be able to redo.");

        commandManager.executeCommand(mockCommand2); // New command
        verify(mockCommand2, times(1)).execute();
        assertFalse(commandManager.canRedo(), "Redo stack should be cleared after executing a new command.");
        assertTrue(commandManager.canUndo(), "Should be able to undo the new command.");
    }

    @Test
    void undo_shouldCallUndoOnCommandAndMoveToRedoStack() {
        commandManager.executeCommand(mockCommand1);
        commandManager.undo();

        verify(mockCommand1, times(1)).undo();
        assertFalse(commandManager.canUndo(), "Should not be able to undo after undoing the only command.");
        assertTrue(commandManager.canRedo(), "Should be able to redo the undone command.");
    }

    @Test
    void undo_onEmptyStack_shouldDoNothing() {
        assertDoesNotThrow(() -> {
            commandManager.undo(); // Attempt to undo on an empty stack
        });
        assertFalse(commandManager.canUndo(), "Still cannot undo.");
        assertFalse(commandManager.canRedo(), "Still cannot redo.");
    }

    @Test
    void redo_shouldCallExecuteOnCommandAndMoveToUndoStack() {
        commandManager.executeCommand(mockCommand1);
        commandManager.undo(); // mockCommand1 moved to redo stack
        commandManager.redo(); // mockCommand1 re-executed and moved back to undo stack

        // execute() was called once initially, and once on redo
        verify(mockCommand1, times(2)).execute();
        assertTrue(commandManager.canUndo(), "Should be able to undo the redone command.");
        assertFalse(commandManager.canRedo(), "Should not be able to redo after redoing the only command.");
    }

    @Test
    void redo_onEmptyStack_shouldDoNothing() {
        assertDoesNotThrow(() -> {
            commandManager.redo(); // Attempt to redo on an empty stack
        });
        assertFalse(commandManager.canUndo(), "Still cannot undo.");
        assertFalse(commandManager.canRedo(), "Still cannot redo.");
    }

    @Test
    void multipleCommands_undoAndRedoOrder() {
        commandManager.executeCommand(mockCommand1);
        commandManager.executeCommand(mockCommand2);

        assertTrue(commandManager.canUndo());
        assertFalse(commandManager.canRedo());

        // Undo mockCommand2
        commandManager.undo();
        verify(mockCommand2, times(1)).undo();
        assertTrue(commandManager.canUndo(), "Can still undo mockCommand1.");
        assertTrue(commandManager.canRedo(), "Can redo mockCommand2.");

        // Undo mockCommand1
        commandManager.undo();
        verify(mockCommand1, times(1)).undo();
        assertFalse(commandManager.canUndo(), "No more commands to undo.");
        assertTrue(commandManager.canRedo(), "Can still redo (mockCommand1 is now at top of redo stack).");

        // Redo mockCommand1
        commandManager.redo();
        // execute on mockCommand1 called once initially, once on redo
        verify(mockCommand1, times(2)).execute();
        assertTrue(commandManager.canUndo(), "Can undo mockCommand1.");
        assertTrue(commandManager.canRedo(), "Can redo mockCommand2.");

        // Redo mockCommand2
        commandManager.redo();
        // execute on mockCommand2 called once initially, once on redo
        verify(mockCommand2, times(2)).execute();
        assertTrue(commandManager.canUndo(), "Can undo mockCommand2.");
        assertFalse(commandManager.canRedo(), "No more commands to redo.");

        // Verify order of operations if strict sequencing is important
        InOrder inOrder = inOrder(mockCommand1, mockCommand2);
        inOrder.verify(mockCommand1).execute();
        inOrder.verify(mockCommand2).execute();
        inOrder.verify(mockCommand2).undo();
        inOrder.verify(mockCommand1).undo();
        inOrder.verify(mockCommand1).execute(); // Redo
        inOrder.verify(mockCommand2).execute(); // Redo
    }

    @Test
    void clearStacks_shouldEmptyBothStacks() {
        commandManager.executeCommand(mockCommand1);
        commandManager.undo(); // mockCommand1 on redoStack
        commandManager.executeCommand(mockCommand2); // mockCommand2 on undoStack, redoStack cleared, then mockCommand1 added back by undo. Oops, logic error in test setup thinking.

        // Correct setup for clearStacks test:
        commandManager = new CommandManager(mockDrawing); // fresh manager
        commandManager.executeCommand(mockCommand1); // C1 on undo
        commandManager.executeCommand(mockCommand2); // C2 on undo, C1 below it
        commandManager.undo(); // C2 on redo, C1 on undo
        
        assertTrue(commandManager.canUndo());
        assertTrue(commandManager.canRedo());

        commandManager.clearStacks();

        assertFalse(commandManager.canUndo(), "Undo stack should be empty after clearStacks.");
        assertFalse(commandManager.canRedo(), "Redo stack should be empty after clearStacks.");
    }
    
    @Test
    void setDrawingModel_shouldUpdateModel() {
        CommandManager cm = new CommandManager(); // Create with default constructor (null model)
        // Potentially test behavior if model is null, though current implementation doesn't heavily rely on it beyond notifications
        // which are best tested via integration or by the Drawing model itself.

        Drawing newMockDrawing = mock(Drawing.class);
        cm.setDrawingModel(newMockDrawing);
        
        // No direct getter for drawingModel, so this test is more about ensuring the setter runs without error.
        // Further testing of drawingModel interaction would occur in command execution tests where drawingModel is used.
        // For now, just confirm no exception.
        assertDoesNotThrow(() -> cm.setDrawingModel(newMockDrawing));
    }
}
            