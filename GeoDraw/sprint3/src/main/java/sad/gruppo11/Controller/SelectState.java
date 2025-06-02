
package sad.gruppo11.Controller;

import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.GroupShape;
import sad.gruppo11.Model.RectangleShape;
import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.TextShape;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Vector2D;
import sad.gruppo11.Model.geometry.Rect;
import sad.gruppo11.Infrastructure.MoveShapeCommand;
import sad.gruppo11.View.DrawingView;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent; // Non usato direttamente qui, ma GeoEngine gestisce i tasti

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class SelectState implements ToolState {

    private enum Mode { NONE, DRAG_SELECTION, SELECT_AREA, PAN_VIEW }
    private Mode currentMode = Mode.NONE;

    private Point2D pressPosWorld;      // Posizione del mouse (coordinate mondo) al momento del press
    private Point2D lastDragPosWorld;   // Ultima posizione del mouse durante un drag

    
    // Per il drag di forme: memorizza gli offset iniziali dei centri delle forme rispetto a pressPosWorld
    // così il movimento relativo è corretto.
    private List<Shape> shapesBeingDragged;
    private List<Vector2D> dragOffsets; 

    // Per la selezione ad area
    private Rect selectionAreaRect; // In coordinate mondo

    private long lastClickTime = 0;
    private Point2D lastClickPosWorld = null;
    private static final long DOUBLE_CLICK_THRESHOLD_MS = 300; // Millisecondi per doppio click

    private final Drawing drawing; // Il disegno corrente

    SelectState(Drawing drawing) {
        Objects.requireNonNull(drawing, "Drawing cannot be null for SelectState.");
        // Inizializza lo stato con il disegno corrente
        // Non è necessario fare nulla qui, il disegno viene gestito da GeoEngine
        this.drawing = drawing;

    }

    @Override
    public String getName() { return "SelectTool"; }

    @Override
    public void activate(GeoEngine engine) {
        resetState();
        // Non deselezionare qui, l'utente potrebbe voler passare a SelectTool mantenendo la selezione
        if (engine.getView() != null) engine.getView().showUserMessage("Select Tool: Click to select, Shift+Click to multi-select, Drag to move or pan.");
    }

    @Override
    public void deactivate(GeoEngine engine) {
        resetState();
        DrawingView view = engine.getView();
        if (view != null) {
            view.clearTemporaryVisuals(); // Rimuovi rettangolo di selezione area
            view.clearUserMessage();
        }
        // Non deselezionare necessariamente quando si disattiva SelectTool,
        // altre operazioni potrebbero voler agire sulla selezione corrente.
        // GeoEngine.setState() si occuperà di clearSelection() se necessario per il nuovo tool.
    }

    private void resetState() {
        currentMode = Mode.NONE;
        pressPosWorld = null;
        lastDragPosWorld = null;
        shapesBeingDragged = null;
        dragOffsets = null;
        selectionAreaRect = null;
    }

    @Override
    public void onMousePressed(GeoEngine engine, Point2D worldPoint) {
        Objects.requireNonNull(engine); Objects.requireNonNull(worldPoint);
        pressPosWorld = new Point2D(worldPoint);
        lastDragPosWorld = new Point2D(worldPoint);

        Shape hitShape = pickTopMostShapeOrGroup(engine.getDrawing(), worldPoint);
        boolean isShiftDown = engine.isShiftKeyPressed();

        if (hitShape != null) {
            currentMode = Mode.DRAG_SELECTION;
            shapesBeingDragged = new ArrayList<>();
            dragOffsets = new ArrayList<>();

            if (isShiftDown) { // Multi-selezione con Shift
                if (engine.getSelectedShapes().contains(hitShape)) {
                    // Deseleziona se già selezionato e Shift è premuto
                    List<Shape> newSelection = new ArrayList<>(engine.getSelectedShapes());
                    newSelection.remove(hitShape);
                    engine.setSelectedShapes(newSelection);
                } else {
                    // Aggiungi alla selezione
                    engine.addShapeToSelection(hitShape);
                }
            } else { // Selezione singola (o inizio drag di una selezione esistente)
                if (!engine.getSelectedShapes().contains(hitShape)) {
                    engine.setSingleSelectedShape(hitShape);
                }
                // Se hitShape fa parte della selezione corrente, tutte le forme selezionate verranno trascinate.
            }

            // Prepara per il drag di tutte le forme attualmente selezionate
            for (Shape selected : engine.getSelectedShapes()) {
                shapesBeingDragged.add(selected);
                // Calcola l'offset dal punto di clic al centro della forma (o al suo topLeft)
                // Per semplicità, usiamo l'offset rispetto al punto di clic sui bounds della forma.
                // Se si clicca su una forma, l'offset è dal punto di clic sulla forma al suo "handle" di drag.
                // Per ora, offset rispetto al centro della forma:
                Point2D shapeCenter = selected.getBounds().getCenter();
                dragOffsets.add(new Vector2D(
                    shapeCenter.getX() - pressPosWorld.getX(),
                    shapeCenter.getY() - pressPosWorld.getY()
                ));
            }
        } else { // Click su area vuota
            if (!isShiftDown) { // Se Shift non è premuto, deseleziona tutto e inizia selezione area o pan
                currentMode = Mode.SELECT_AREA;
                selectionAreaRect = new Rect(pressPosWorld, 0, 0);
                if (engine.getView() != null) engine.getView().drawTemporaryGhostShape(null); // Pulisci altre ghost
            }
            else{
                engine.clearSelection();
                currentMode = Mode.PAN_VIEW;
            }
        }
        engine.notifyViewToRefresh(); // Aggiorna la vista per mostrare la selezione
    }


    @Override
    public void onMouseDragged(GeoEngine engine, Point2D worldPoint) {
        Objects.requireNonNull(engine); Objects.requireNonNull(worldPoint);
        if (currentMode == Mode.NONE) return;

        Vector2D dragDelta = new Vector2D(
            worldPoint.getX() - lastDragPosWorld.getX(),
            worldPoint.getY() - lastDragPosWorld.getY()
        );

        switch (currentMode) {
            case DRAG_SELECTION:
                if (shapesBeingDragged != null && !shapesBeingDragged.isEmpty()) {
                    for (Shape shape : shapesBeingDragged) {
                        // Applica il delta di drag per il feedback visivo immediato
                        drawing.moveShape(shape, dragDelta); 
                    }
                    // engine.notifyViewToRefresh(); // Aggiorna la vista durante il drag
                }
                break;
            case SELECT_AREA:
                selectionAreaRect = new Rect(
                    Math.min(pressPosWorld.getX(), worldPoint.getX()),
                    Math.min(pressPosWorld.getY(), worldPoint.getY()),
                    Math.abs(pressPosWorld.getX() - worldPoint.getX()),
                    Math.abs(pressPosWorld.getY() - worldPoint.getY())
                );
                if (engine.getView() != null) {
                    // Crea una RectangleShape temporanea per visualizzare l'area di selezione
                    RectangleShape ghostRect = new RectangleShape(selectionAreaRect, 
                                                                new ColorData(0,0,255,0.5), // Blu semi-trasparente per stroke
                                                                new ColorData(0,0,255,0.1)  // Blu molto trasparente per fill
                                                                );
                    engine.getView().drawTemporaryGhostShape(ghostRect);
                    engine.notifyViewToRefresh();
                }
                break;
            case PAN_VIEW: // Se si implementasse il pan con il select tool attivo (es. con tasto spazio)
                engine.scroll(dragDelta.getDx(), dragDelta.getDy());
                break;
            default: {}
        }
        lastDragPosWorld = new Point2D(worldPoint);
    }

    @Override
    public void onMouseReleased(GeoEngine engine, Point2D worldPoint) {
        Objects.requireNonNull(engine); Objects.requireNonNull(worldPoint);

        switch (currentMode) {
            case DRAG_SELECTION:
                if (shapesBeingDragged != null && !shapesBeingDragged.isEmpty()) {
                    Vector2D totalDragVector = new Vector2D(
                        worldPoint.getX() - pressPosWorld.getX(),
                        worldPoint.getY() - pressPosWorld.getY()
                    );

                    // Se c'è stato un movimento significativo, crea i comandi
                    if (totalDragVector.length() > 1e-3) { 
                        
                        if (shapesBeingDragged != null && !shapesBeingDragged.isEmpty()) {
                            for (Shape shape : shapesBeingDragged) {
                                // Applica il delta di drag per il feedback visivo immediato
                                drawing.moveShape(shape, totalDragVector.inverse()); 
                            }
                            // engine.notifyViewToRefresh(); // Aggiorna la vista durante il drag
                        }

                        engine.moveSelectedShapes(totalDragVector);
                    } else { // Solo un click senza drag significativo, la selezione è già stata gestita in onPressed
                        engine.notifyViewToRefresh(); // Assicura che la vista sia aggiornata
                    }
                }
                break;
            case SELECT_AREA:
                if (engine.getView() != null) engine.getView().clearTemporaryVisuals();
                if (selectionAreaRect != null && (selectionAreaRect.getWidth() > 1 || selectionAreaRect.getHeight() > 1)) {
                    List<Shape> shapesInArea = findShapesInRect(engine.getDrawing(), selectionAreaRect);
                    boolean isShiftDown = false; // engine.isShiftDown(); // TODO: Get shift state
                    
                    if (!isShiftDown) engine.clearSelection(); // Se non è shift, nuova selezione
                    
                    for(Shape s : shapesInArea) {
                        // Se la forma è figlia di un gruppo, seleziona il gruppo padre
                        Shape topLevelShape = getTopLevelParentGroup(engine.getDrawing(), s);
                        engine.addShapeToSelection(topLevelShape);
                    }
                }
                else
                {
                    engine.clearSelection();
                }
                engine.notifyViewToRefresh();
                break;
            default: {}
        }
        resetState(); // Resetta lo stato del tool
    }

    private Shape pickTopMostShapeOrGroup(Drawing drawing, Point2D p) {
        List<Shape> shapes = drawing.getShapesInZOrder(); // Dal basso verso l'alto
        Shape picked = null;
        for (int i = shapes.size() - 1; i >= 0; i--) { // Itera dall'alto verso il basso
            Shape currentShape = shapes.get(i);
            if (currentShape.contains(p)) {
                picked = currentShape;
                break; 
            }
        }
        if (picked != null) {
            return getTopLevelParentGroup(drawing, picked);
        }
        return null;
    }
    
    /**
     * Se la shape data è figlia di un gruppo nel drawing, restituisce il gruppo padre di primo livello.
     * Altrimenti, restituisce la shape stessa.
     */
    private Shape getTopLevelParentGroup(Drawing drawing, Shape shape) {
        for (Shape topLevelShape : drawing.getShapesInZOrder()) {
            if (topLevelShape == shape) return shape; // È una forma di primo livello
            if (topLevelShape.isComposite()) {
                if (isDescendant(topLevelShape, shape)) {
                    return topLevelShape; // Restituisce il gruppo di primo livello che contiene la forma
                }
            }
        }
        return shape; // Non trovata come figlia, restituisci la forma originale
    }

    private boolean isDescendant(Shape ancestor, Shape potentialDescendant) {
        if (!ancestor.isComposite()) return false;
        for (Shape child : ancestor.getChildren()) {
            if (child == potentialDescendant) return true;
            if (isDescendant(child, potentialDescendant)) return true;
        }
        return false;
    }

    private List<Shape> findShapesInRect(Drawing drawing, Rect area) {
        List<Shape> found = new ArrayList<>();
        for (Shape shape : drawing.getShapesInZOrder()) {
            // Controlla se il bounding box della forma (ruotato) interseca l'area di selezione.
            // Questo è un controllo approssimativo, per precisione si dovrebbe controllare ogni punto/segmento.
            // Per ora, usiamo l'intersezione dei bounding box non ruotati come approssimazione.
            if (rectsIntersect(shape.getBounds(), area)) { // TODO: Intersezione di AABB di forma ruotata
                found.add(shape);
            }
        }
        return found;
    }
    
    private boolean rectsIntersect(Rect r1, Rect r2) {
        return r1.getX() < r2.getX() + r2.getWidth() &&
               r1.getX() + r1.getWidth() > r2.getX() &&
               r1.getY() < r2.getY() + r2.getHeight() &&
               r1.getY() + r1.getHeight() > r2.getY();
    }
}
