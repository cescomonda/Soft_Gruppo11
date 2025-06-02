
package sad.gruppo11.Infrastructure;

import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.Drawing; // Per notificare il modello del cambiamento

public class ReflectHorizontalCommand extends AbstractShapeCommand {
    private Drawing drawingModel; // Opzionale, per notificare la vista dopo l'esecuzione

    public ReflectHorizontalCommand(Drawing drawingModel, Shape shape) {
        super(drawingModel, shape);
        this.drawingModel = drawingModel; // Può essere null
    }

    @Override
    public void execute() {
        // Salvare lo stato per l'undo potrebbe essere complesso per la riflessione.
        // Se la riflessione è la sua stessa inversa (applicandola due volte si torna all'originale),
        // l'undo è semplice. Altrimenti, serve un clone dello stato precedente.
        // Per ora, assumiamo che reflectHorizontal sia la sua stessa inversa o che l'undo
        // la applichi di nuovo.
        this.drawingModel.reflectShapeHorizontal(receiverShape);
    }

    @Override
    public void undo() {
        // Se reflectHorizontal() è la sua stessa inversa:
        this.drawingModel.reflectShapeHorizontal(receiverShape);
        // Altrimenti, se avessimo salvato lo stato:
        // receiverShape.restoreState(savedState);
    }

    @Override
    public String toString() {
        return "ReflectHorizontalCommand{shapeId=" + receiverShape.getId().toString() + "}";
    }
}
