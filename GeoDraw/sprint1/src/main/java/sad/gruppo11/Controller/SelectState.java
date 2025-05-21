package sad.gruppo11.Controller; // Assicurati che il package sia corretto

import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Vector2D;
import sad.gruppo11.Infrastructure.MoveShapeCommand;

import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Objects;

// Classe che implementa lo stato dello strumento di selezione
public class SelectState implements ToolState {
    // Coordinate per gestire il dragging
    private Point2D lastMousePosition;
    private Point2D pressMousePosition;
    private boolean isDragging;

    @Override
    public void onMousePressed(Point2D p, GeoEngine engine) {
        // Verifica che i parametri non siano null
        Objects.requireNonNull(p, "Point cannot be null in onMousePressed");
        Objects.requireNonNull(engine, "GeoEngine cannot be null in onMousePressed");

        // Salva la posizione in cui è stato premuto il mouse
        this.pressMousePosition = new Point2D(p);
        this.lastMousePosition = new Point2D(p);
        this.isDragging = false;

        // Ottiene tutte le forme dal disegno
        sad.gruppo11.Model.Drawing drawing = engine.getDrawing();

        if (drawing == null) {
            // Se non c'è un disegno, non c'è nulla da selezionare.
            // Notifica GeoEngine che la selezione è null.
            engine.setCurrentlySelectedShape(null);
            return; // Esci dal metodo presto
        }

        List<Shape> shapesToSearch = new ArrayList<>(drawing.getShapes());

        // Le forme più recenti vengono cercate per prime (top-down)
        Collections.reverse(shapesToSearch);

        Shape newlySelectedShape = null;

        // Cerca la prima forma che contiene il punto cliccato
        for (Shape shape : shapesToSearch) {
            if (shape.contains(p)) {
                newlySelectedShape = shape;
                break;
            }
        }

        // Imposta la forma selezionata tramite GeoEngine
        engine.setCurrentlySelectedShape(newlySelectedShape);
        // La view viene notificata automaticamente dal GeoEngine
    }

    @Override
    public void onMouseDragged(Point2D p, GeoEngine engine) {
        // Ottiene la forma attualmente selezionata
        Shape currentSelection = engine.getSelectedShape();

        if (currentSelection != null && this.pressMousePosition != null) {
            // Abilita il flag di trascinamento
            this.isDragging = true;

            // La logica della ghost shape è stata rimossa per la Sprint 1.
            // Si potrebbe implementare nuovamente in futuro per dare feedback visivo durante il drag.

            // Aggiorna la posizione corrente del mouse
            this.lastMousePosition = new Point2D(p);
        }
    }

    @Override
    public void onMouseReleased(Point2D p, GeoEngine engine) {
        // Rimuovere eventuale ghost shape (commentato perché non implementato)
        // if (engine.getGhostShapeHandler() != null) engine.getGhostShapeHandler().accept(null);

        Shape currentSelection = engine.getSelectedShape();

        if (currentSelection != null && this.isDragging && this.pressMousePosition != null) {
            // Calcola lo spostamento totale tra punto iniziale e rilascio del mouse
            double totalDx = p.getX() - pressMousePosition.getX();
            double totalDy = p.getY() - pressMousePosition.getY();

            // Applica lo spostamento solo se significativo (evita micro-movimenti inutili)
            if (Math.abs(totalDx) > 1e-3 || Math.abs(totalDy) > 1e-3) {
                // Crea e esegue il comando di spostamento
                MoveShapeCommand moveCmd = new MoveShapeCommand(currentSelection, new Vector2D(totalDx, totalDy));
                engine.getCommandManager().execute(moveCmd); // La view verrà aggiornata automaticamente
            }
        }

        // Reset degli stati locali
        this.isDragging = false;
        this.pressMousePosition = null;
        this.lastMousePosition = null;
    }

    @Override
    public String getToolName() {
        // Restituisce il nome dello strumento
        return "Select Tool";
    }

    @Override
    public void onEnterState(GeoEngine engine) {
        // Eventuale logica all'entrata nello stato di selezione
        // La selezione viene gestita da GeoEngine, quindi qui non è necessario fare nulla
        this.isDragging = false;
        this.pressMousePosition = null;
        this.lastMousePosition = null;
    }

    @Override
    public void onExitState(GeoEngine engine) {
        // Pulizia quando si abbandona lo stato Select
        this.isDragging = false;
        this.pressMousePosition = null;
        this.lastMousePosition = null;
    }

}
