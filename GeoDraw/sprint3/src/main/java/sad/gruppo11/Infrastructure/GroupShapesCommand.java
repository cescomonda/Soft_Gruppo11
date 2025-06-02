
package sad.gruppo11.Infrastructure;

import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.GroupShape;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class GroupShapesCommand extends AbstractDrawingCommand {
    private final List<Shape> shapesToGroup; // Riferimenti alle forme originali nel Drawing
    private GroupShape createdGroup;
    // Per l'undo, potremmo aver bisogno di memorizzare gli indici originali per ripristinare lo Z-order,
    // ma la gestione dello Z-order per gruppi può diventare complessa.
    // Per ora, quando si separano, vengono aggiunti alla fine.
    private List<Integer> originalIndices;


    public GroupShapesCommand(Drawing drawing, List<Shape> shapesToGroup) {
        super(drawing);
        Objects.requireNonNull(shapesToGroup, "List of shapes to group cannot be null.");
        if (shapesToGroup.size() < 2) {
            throw new IllegalArgumentException("Cannot group less than two shapes.");
        }
        // Crea copie difensive della lista per evitare modifiche esterne
        this.shapesToGroup = new ArrayList<>(shapesToGroup);
        this.originalIndices = new ArrayList<>();
    }

    @Override
    public void execute() {
        // Rimuovi le forme singole dal disegno e memorizza i loro indici
        // Ordina shapesToGroup per indice decrescente per rimuovere correttamente
        // senza che gli indici cambino durante la rimozione.
        List<Shape> sortedShapesToGroup = shapesToGroup.stream()
            .filter(s -> receiverDrawing.getShapeIndex(s) != -1) // Assicurati che la forma sia ancora nel disegno
            .sorted((s1, s2) -> Integer.compare(receiverDrawing.getShapeIndex(s2), receiverDrawing.getShapeIndex(s1)))
            .collect(Collectors.toList());
        
        originalIndices.clear();
        for(Shape shape : sortedShapesToGroup) {
            int index = receiverDrawing.getShapeIndex(shape);
            if (index != -1) { // Assicurati che la forma esista ancora nel disegno
                originalIndices.add(0, index); // Aggiungi in testa per mantenere l'ordine originale dopo il reverse
                receiverDrawing.removeShape(shape); // Questo notificherà già (REMOVE)
            }
        }
        
        // Crea il nuovo gruppo con le forme (o cloni delle forme, a seconda della logica desiderata)
        // Se shapesToGroup contiene i riferimenti originali, GroupShape li userà.
        this.createdGroup = new GroupShape(new ArrayList<>(shapesToGroup)); // Passa una copia della lista di forme
        receiverDrawing.addShape(this.createdGroup); // Questo notificherà (ADD)
    }

    @Override
    public void undo() {
        if (this.createdGroup != null) {
            receiverDrawing.removeShape(this.createdGroup); // Notifica (REMOVE)
            // Riaggiungi le forme originali. È importante ripristinare lo Z-order se possibile.
            // Per semplicità, le aggiungiamo semplicemente.
            // Per ripristinare lo Z-order, dovremmo riaggiungerle agli originalIndices.
            
            // Assicurati che shapesToGroup e originalIndices abbiano la stessa dimensione e ordine.
            // Se sortedShapesToGroup è stato usato per la rimozione, usiamo shapesToGroup
            // e originalIndices che dovrebbero corrispondere nell'ordine originale di shapesToGroup.
            for (int i = 0; i < shapesToGroup.size(); i++) {
                Shape shape = shapesToGroup.get(i);
                // Se abbiamo memorizzato gli indici e vogliamo ripristinare l'ordine:
                // receiverDrawing.addShapeAtIndex(shape, originalIndices.get(i));
                // Per ora, aggiungiamo semplicemente:
                receiverDrawing.addShape(shape); // Notifica (ADD)
            }
        }
        this.createdGroup = null; // Resetta per una possibile riesecuzione
    }

    // FOR TEST ONLY
    public GroupShape getCreatedGroup()
    {
        return this.createdGroup;
    }

    @Override
    public String toString() {
        return "GroupShapesCommand{groupedShapeCount=" + shapesToGroup.size() + 
               (createdGroup != null ? ", groupId=" + createdGroup.getId().toString() : "") + "}";
    }
}
