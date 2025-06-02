
package sad.gruppo11.Infrastructure;

import sad.gruppo11.Model.Drawing;
import java.util.Stack;
import java.util.Objects;

public class CommandManager {
    private final Stack<Command> undoStack;
    private final Stack<Command> redoStack;
    private Drawing drawingModel; // Per notificare dopo l'esecuzione del comando, se necessario

    public CommandManager(Drawing drawingModel) {
        this.undoStack = new Stack<>();
        this.redoStack = new Stack<>();
        this.drawingModel = drawingModel; 
    }
    
    public CommandManager() { // Costruttore di default se il modello viene impostato dopo
        this(null);
    }

    public void executeCommand(Command cmd) {
        Objects.requireNonNull(cmd, "Command to execute cannot be null.");
        cmd.execute();
        undoStack.push(cmd);
        redoStack.clear(); // Qualsiasi nuova azione invalida lo stack di redo
        
        // La notifica agli observer dovrebbe idealmente avvenire attraverso il modello stesso
        // quando la sua state cambia a causa dell'esecuzione del comando.
        // Se i comandi modificano direttamente il Drawing e il Drawing notifica i suoi observer,
        // una notifica esplicita qui potrebbe essere ridondante.
        // Tuttavia, se si vuole una notifica generale "un comando è stato eseguito":
        if (drawingModel != null) {
           // Invece di un evento generico, è meglio che il Drawing.notifyObservers
           // sia chiamato internamente dai metodi del Drawing modificati dai comandi.
           // Es. drawing.addShape() chiama notifyObservers.
           // Se un comando non modifica direttamente il Drawing ma qualcos'altro (es. selezione),
           // allora quel "qualcos'altro" dovrebbe essere Observable.
           // Per i comandi che modificano il Drawing, il Drawing stesso si occupa delle notifiche.
        }
    }

    public void undo() {
        if (canUndo()) {
            Command cmdToUndo = undoStack.pop();
            cmdToUndo.undo();
            redoStack.push(cmdToUndo);
            // Anche qui, il modello dovrebbe notificare i cambiamenti.
        }
    }

    public void redo() {
        if (canRedo()) {
            Command cmdToRedo = redoStack.pop();
            cmdToRedo.execute(); // Riesegue il comando
            undoStack.push(cmdToRedo);
            // Il modello notifica.
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
        // Potrebbe essere utile notificare che la history è stata resettata,
        // se la UI ha pulsanti undo/redo da disabilitare.
        // Questo di solito avviene quando si carica un nuovo disegno.
        if (drawingModel != null) {
            // Non c'è un evento specifico per "stacks cleared", ma un'azione come LOAD
            // potrebbe implicare questo e causare un refresh UI.
        }
    }
    
    public void setDrawingModel(Drawing model){
        this.drawingModel = model;
    }
}
