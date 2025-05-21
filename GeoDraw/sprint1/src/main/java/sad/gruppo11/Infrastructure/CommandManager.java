package sad.gruppo11.Infrastructure;

import java.util.Stack;
import java.util.Objects;
import sad.gruppo11.Controller.GeoEngine;

/*
 * CommandManager gestisce una cronologia di comandi eseguiti,
 * permettendo operazioni di undo e redo.
 *
 * Utilizza due stack:
 * - uno per i comandi annullabili (undoStack)
 * - uno per i comandi ripristinabili (redoStack)
 */
public class CommandManager {

    /* Stack dei comandi eseguiti, per supportare undo */
    private final Stack<Command> undoStack;

    /* Stack dei comandi annullati, per supportare redo */
    private final Stack<Command> redoStack;

    /* Riferimento al GeoEngine, usato per notificare cambiamenti */
    private final GeoEngine engine;

    /*
     * Costruttore.
     *
     * @param engine Il GeoEngine da notificare dopo ogni operazione. Può essere null.
     */
    public CommandManager(GeoEngine engine) {
        this.engine = engine;
        this.undoStack = new Stack<>();
        this.redoStack = new Stack<>();
    }

    /*
     * Esegue un comando, lo aggiunge allo stack di undo e svuota lo stack di redo.
     *
     * @param cmd Il comando da eseguire. Non può essere null.
     */
    public void execute(Command cmd) {
        Objects.requireNonNull(cmd, "Command to execute cannot be null.");

        cmd.execute();
        undoStack.push(cmd);
        redoStack.clear();

        notifyEngine();
    }

    /*
     * Annulla l'ultimo comando eseguito, se disponibile.
     * Il comando viene spostato nello stack di redo.
     */
    public void undo() {
        if (canUndo()) {
            Command cmdToUndo = undoStack.pop();
            cmdToUndo.undo();
            redoStack.push(cmdToUndo);

            notifyEngine();
        }
    }

    /*
     * Riesegue l'ultimo comando annullato, se disponibile.
     * Il comando viene spostato nuovamente nello stack di undo.
     */
    public void redo() {
        if (canRedo()) {
            Command cmdToRedo = redoStack.pop();
            cmdToRedo.execute();
            undoStack.push(cmdToRedo);

            notifyEngine();
        }
    }

    /*
     * Verifica se ci sono comandi da annullare.
     *
     * @return true se è possibile fare undo, false altrimenti.
     */
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    /*
     * Verifica se ci sono comandi da ripristinare.
     *
     * @return true se è possibile fare redo, false altrimenti.
     */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    /*
     * Pulisce entrambi gli stack.
     * Tipicamente chiamato quando si crea o carica un nuovo disegno.
     */
    public void clearStacks() {
        undoStack.clear();
        redoStack.clear();
        // notifyEngine(); // opzionale, attivare se serve aggiornare l’UI
    }

    /*
     * Notifica GeoEngine che lo stato del modello potrebbe essere cambiato.
     */
    private void notifyEngine() {
        if (this.engine != null) {
            this.engine.notifyModelChanged();
        }
    }
}
