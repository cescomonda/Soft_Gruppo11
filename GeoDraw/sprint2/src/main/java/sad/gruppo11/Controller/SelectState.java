// File: sad/gruppo11/Controller/SelectState.java
package sad.gruppo11.Controller;

import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.TextShape;
import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Vector2D;
import sad.gruppo11.Infrastructure.MoveShapeCommand;
import sad.gruppo11.View.DrawingView;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

/**
 * ToolState di selezione, trascinamento forme e pan (scroll) del canvas.
 * Gestisce anche il doppio click sui TextShape per la modifica del testo.
 */
public class SelectState implements ToolState {

    /**
     * Rappresenta la modalità di interazione corrente.
     */
    private enum Mode { NONE, DRAG_SHAPE, PAN_VIEW }

    private Mode mode = Mode.NONE;

    private Point2D pressPos;      // Punto di pressione del mouse
    private Point2D lastPos;       // Ultima posizione nota del mouse (per il delta incrementale)
    private Shape   shapeAtPress;  // Forma premuta al momento del mouse press

    @Override
    public String getName() { return "SelectTool"; }

    // -------------------------------------------------- Ciclo di vita --------------------------------------------------

    @Override
    public void activate(GeoEngine engine) {
        reset();
    }

    @Override
    public void deactivate(GeoEngine engine) {
        engine.setSelectedShape(null);
        reset();
    }

    private void reset() {
        mode = Mode.NONE;
        pressPos = null;
        lastPos  = null;
        shapeAtPress = null;
    }

    // -------------------------------------------------- Gestione Input --------------------------------------------------

    @Override
    public void onMousePressed(GeoEngine engine, Point2D worldPoint) {
        Objects.requireNonNull(engine); Objects.requireNonNull(worldPoint);

        Shape hitShape = pickShape(engine.getDrawing(), worldPoint);

        // ---------------------- Singolo click ----------------------
        pressPos = new Point2D(worldPoint);
        lastPos  = new Point2D(worldPoint);

        if (hitShape != null) {
            engine.setSelectedShape(hitShape);
            shapeAtPress = hitShape;
            mode = Mode.DRAG_SHAPE;
        } else {
            engine.setSelectedShape(null);
            mode = Mode.PAN_VIEW;
        }
        
        engine.notifyViewToRefresh();
    }

    @Override
    public void onMouseDragged(GeoEngine engine, Point2D worldPoint) {
        Objects.requireNonNull(engine); Objects.requireNonNull(worldPoint);
        if (mode == Mode.NONE) return;

        Vector2D delta = new Vector2D(worldPoint.getX() - lastPos.getX(),
                                      worldPoint.getY() - lastPos.getY());

        switch (mode) {
            case DRAG_SHAPE: {
                Shape selected = engine.getSelectedShape();
                if (selected != null) {
                    selected.move(delta);           // Feedback immediato
                    engine.notifyViewToRefresh();
                }
                break;
            }
            case PAN_VIEW: {
                if (Math.abs(delta.getDx()) > 0.1 || Math.abs(delta.getDy()) > 0.1) {
                    engine.scroll(delta.getDx(), delta.getDy());
                }
                break;
            }
            default: {}
        }

        lastPos = new Point2D(worldPoint);
    }

    @Override
    public void onMouseReleased(GeoEngine engine, Point2D worldPoint) {
        Objects.requireNonNull(engine); Objects.requireNonNull(worldPoint);

        if (mode == Mode.DRAG_SHAPE && shapeAtPress != null && pressPos != null) {
            Vector2D total = new Vector2D(worldPoint.getX() - pressPos.getX(),
                                          worldPoint.getY() - pressPos.getY());

            // Revert del feedback e comando undo/redo
            if (total.length() > 1e-3) {
                shapeAtPress.move(total.inverse());
                engine.getCommandManager().executeCommand(new MoveShapeCommand(shapeAtPress, total));
            } else {
                engine.notifyViewToRefresh();
            }
        }

        // Conclude l'interazione
        mode = Mode.NONE;
        pressPos = lastPos = null;
        shapeAtPress = null;
    }

    // -------------------------------------------------- Helper privati --------------------------------------------------

    /**
     * Restituisce la forma più in alto che contiene il punto, o {@code null}.
     */
    private Shape pickShape(Drawing drawing, Point2D p) {
        List<Shape> list = new ArrayList<>();
        drawing.getShapesInZOrder().forEach(list::add);
        Collections.reverse(list); // cima -> fondo
        for (Shape s : list) {
            if (s.contains(p)) return s;
        }
        return null;
    }
}
