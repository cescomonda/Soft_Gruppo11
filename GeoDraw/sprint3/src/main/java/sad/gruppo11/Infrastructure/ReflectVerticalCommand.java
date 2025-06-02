
package sad.gruppo11.Infrastructure;

import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.Drawing; // Per notificare il modello del cambiamento

public class ReflectVerticalCommand extends AbstractShapeCommand {
    private Drawing drawingModel; // Opzionale, per notificare la vista dopo l'esecuzione

    public ReflectVerticalCommand(Drawing drawingModel, Shape shape) {
        super(drawingModel, shape);
        this.drawingModel = drawingModel;
    }

    @Override
    public void execute() {
        drawingModel.reflectShapeVertical(receiverShape); // Associa il modello di disegno se non è già associato
    }

    @Override
    public void undo() {
        drawingModel.reflectShapeVertical(receiverShape); // La riflessione verticale è la sua stessa inversa
    }

    @Override
    public String toString() {
        return "ReflectVerticalCommand{shapeId=" + receiverShape.getId().toString() + "}";
    }
}
