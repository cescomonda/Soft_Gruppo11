
package sad.gruppo11.Infrastructure;

import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.Shape;
import java.util.Objects;

public abstract class AbstractShapeCommand implements Command {
    protected final Shape receiverShape;
    protected final Drawing drawing; // Aggiunto per il contesto del disegno
    // Per un undo più robusto, potremmo memorizzare uno snapshot dello stato PRECEDENTE della forma.
    // protected Shape shapeStateBeforeExecute; 

    public AbstractShapeCommand(Drawing drawing, Shape receiver) {
        Objects.requireNonNull(receiver, "Receiver Shape cannot be null for AbstractShapeCommand.");
        Objects.requireNonNull(drawing, "drawing cannot be null for ChangeFillColorCommand.");
        this.receiverShape = receiver;
        this.drawing = drawing; 
    }

    // Metodo helper che le sottoclassi potrebbero usare prima di execute()
    // protected void storeInitialState() {
    //     if (this.receiverShape != null) {
    //         this.shapeStateBeforeExecute = this.receiverShape.clone(); // Richiede che clone() sia profondo
    //     }
    // }
}
