
package sad.gruppo11.Infrastructure;

import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.GroupShape;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UngroupShapeCommand extends AbstractDrawingCommand {
    private final GroupShape groupToUngroup; // Riferimento al gruppo originale nel Drawing
    private List<Shape> originalChildren;
    private int originalGroupIndex = -1;

    public UngroupShapeCommand(Drawing drawing, GroupShape groupToUngroup) {
        super(drawing);
        Objects.requireNonNull(groupToUngroup, "Group to ungroup cannot be null.");
        this.groupToUngroup = groupToUngroup;
    }

    @Override
    public void execute() {
        // Memorizza i figli e l'indice del gruppo prima di rimuoverlo
        this.originalChildren = new ArrayList<>(groupToUngroup.getChildren()); // Copia difensiva dei riferimenti ai figli
        this.originalGroupIndex = receiverDrawing.getShapeIndex(groupToUngroup);

        if (receiverDrawing.removeShape(groupToUngroup)) { // Notifica (REMOVE)
            // Aggiungi i figli individualmente al disegno
            // Potrebbero essere aggiunti nella posizione del gruppo o alla fine.
            // Per ora, li aggiungiamo alla fine.
            for (Shape child : this.originalChildren) {
                receiverDrawing.addShape(child); // Notifica (ADD)
            }
        }
    }

    @Override
    public void undo() {
        if (this.originalChildren != null && !this.originalChildren.isEmpty()) {
            // Rimuovi i figli individuali (che ora sono nel drawing)
            for (Shape child : this.originalChildren) {
                receiverDrawing.removeShape(child); // Notifica (REMOVE)
            }
            // Riaggiungi il gruppo originale. Se originalGroupIndex è valido, usalo.
            if (this.originalGroupIndex != -1 && this.originalGroupIndex <= receiverDrawing.getModifiableShapesList().size()) {
                 receiverDrawing.addShapeAtIndex(groupToUngroup, this.originalGroupIndex); // Notifica (ADD)
            } else {
                 receiverDrawing.addShape(groupToUngroup); // Notifica (ADD)
            }
        }
        this.originalChildren = null; // Resetta per una possibile riesecuzione
    }
    
    @Override
    public String toString() {
        return "UngroupShapeCommand{groupId=" + groupToUngroup.getId().toString() + "}";
    }
}
