package sad.gruppo11.Infrastructure;

import sad.gruppo11.Model.Drawing;
import java.util.Stack;
import java.util.Objects;

public class CommandManager {
    private final Stack<Command> undoStack;
    private final Stack<Command> redoStack;
    private Drawing drawingModel; // To notify after command execution

    public CommandManager(Drawing drawingModel) {
        this.undoStack = new Stack<>();
        this.redoStack = new Stack<>();
        this.drawingModel = drawingModel; // Can be null if direct notification not used
    }
    
    public CommandManager() {
        this(null);
    }

    public void executeCommand(Command cmd) {
        Objects.requireNonNull(cmd, "Command to execute cannot be null.");
        cmd.execute();
        undoStack.push(cmd);
        redoStack.clear();
        notifyModelChanged();
    }

    public void undo() {
        if (canUndo()) {
            Command cmdToUndo = undoStack.pop();
            cmdToUndo.undo();
            redoStack.push(cmdToUndo);
            notifyModelChanged();
        }
    }

    public void redo() {
        if (canRedo()) {
            Command cmdToRedo = redoStack.pop();
            cmdToRedo.execute();
            undoStack.push(cmdToRedo);
            notifyModelChanged();
        }
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public void clearStacks() {
        undoStack.clear();
        redoStack.clear();
    }
    
    private void notifyModelChanged() {
        // The model (Drawing) should notify its observers when its state changes
        // due to command execution. If commands directly call model methods that notify,
        // this explicit call might be redundant OR a safeguard.
        // For simplicity, assume model methods handle their own notifications.
        // If a more generic "something changed" notification is needed from CommandManager:
        if (drawingModel != null) {
           // The specific DrawingChangeEvent(Shape, Type) is better if applicable
           // For a generic update after any command:
           drawingModel.notifyObservers(new Drawing.DrawingChangeEvent(Drawing.DrawingChangeEvent.ChangeType.MODIFY));
        }
    }
    
    // Setter for drawing model if it's not provided at construction
    public void setDrawingModel(Drawing model){
        this.drawingModel = model;
    }
}