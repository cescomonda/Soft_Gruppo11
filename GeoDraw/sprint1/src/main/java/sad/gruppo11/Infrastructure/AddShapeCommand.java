package sad.gruppo11.Infrastructure;

import java.util.Objects;

/*
 * Comando per aggiungere una forma al disegno.
 * Implementa l'interfaccia Command e supporta undo/redo.
 */
public class AddShapeCommand implements Command {

    /*
     * Il modello di disegno a cui viene aggiunta o da cui viene rimossa la forma.
     */
    private final sad.gruppo11.Model.Drawing drawing;

    /*
     * La forma specifica da aggiungere o rimuovere.
     */
    private final sad.gruppo11.Model.Shape shapeToAdd;

    /*
     * Costruttore del comando AddShapeCommand.
     *
     * @param drawing     Il disegno a cui la forma sarà aggiunta.
     * @param shapeToAdd  La forma da aggiungere.
     */
    public AddShapeCommand(sad.gruppo11.Model.Drawing drawing, sad.gruppo11.Model.Shape shapeToAdd) {
        Objects.requireNonNull(drawing, "Drawing cannot be null for AddShapeCommand.");
        Objects.requireNonNull(shapeToAdd, "Shape to add cannot be null for AddShapeCommand.");
        this.drawing = drawing;
        this.shapeToAdd = shapeToAdd;
    }

    /*
     * Esegue il comando: aggiunge la forma al disegno.
     */
    @Override
    public void execute() {
        drawing.addShape(shapeToAdd);
    }

    /*
     * Annulla il comando: rimuove la forma dal disegno.
     */
    @Override
    public void undo() {
        drawing.removeShape(shapeToAdd);
    }

    /*
     * Restituisce una descrizione testuale utile per debug o interfacce utente.
     */
    @Override
    public String toString() {
        return "AddShapeCommand{shapeId=" + (shapeToAdd != null ? shapeToAdd.getId() : "null") + "}";
    }
}
