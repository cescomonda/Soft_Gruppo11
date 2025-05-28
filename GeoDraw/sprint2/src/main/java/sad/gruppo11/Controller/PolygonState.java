// File: sad/gruppo11/Controller/PolygonState.java
package sad.gruppo11.Controller;

import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Infrastructure.AddShapeCommand; // Usato tramite GeoEngine
import sad.gruppo11.View.DrawingView; // Per i metodi temporanei

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;

public class PolygonState implements ToolState {
    private List<Point2D> currentPoints;
    private boolean isDrawing;
    private static final double CLOSE_POLYGON_THRESHOLD = 10.0; // In coordinate mondo

    @Override
    public String getName() { return "PolygonTool"; }

    @Override
    public void activate(GeoEngine engine) {
        currentPoints = new ArrayList<>(); 
        isDrawing = false;
        if(engine.getView() != null) engine.getView().showUserMessage("Polygon Tool: Click to add points. Click near start or double-click/Enter to finish.");
        else engine.notifyGeoEngineObservers("PolygonToolActivated: Click to add points. Click near start or double-click/Enter to finish.");
    }

    @Override
    public void deactivate(GeoEngine engine) {
        DrawingView view = engine.getView();
        if (view != null) {
            view.clearTemporaryVisuals(); 
            view.clearUserMessage();
        }
        currentPoints.clear(); 
        isDrawing = false;
        // Non è necessario notificare "PolygonToolDeactivated" se clearUserMessage gestisce già lo stato della UI
    }

    @Override
    public void onMousePressed(GeoEngine engine, Point2D p) {
        Objects.requireNonNull(engine); Objects.requireNonNull(p);
        DrawingView view = engine.getView();

        if (!isDrawing) { 
            currentPoints.add(new Point2D(p));
            isDrawing = true;
            if (view != null) {
                // Per il primo punto, rubberBandEnd è p stesso, non ci sono linee guida ancora.
                // Ma vogliamo che CanvasPanel sappia che stiamo disegnando un poligono.
                view.drawTemporaryPolygonGuide(currentPoints, p); 
            }
        } else {
            // Se il click è vicino al primo punto (e ci sono almeno 2 punti già piazzati, quindi size()>=2 prima di aggiungere p)
            if (currentPoints.size() >= 2 && p.distance(currentPoints.get(0)) < CLOSE_POLYGON_THRESHOLD / engine.getCurrentZoom()) {
                if (currentPoints.size() >= 3) { // Verifichiamo di avere almeno 3 punti prima di chiudere
                    finishPolygon(engine);
                } else {
                    if(view != null) view.showUserMessage("A polygon needs at least 3 points to close. Current: " + currentPoints.size());
                    else engine.notifyGeoEngineObservers("PolygonFeedback: A polygon needs at least 3 points to close.");
                    // Non aggiungiamo 'p' se l'intento era chiudere ma non ci sono abbastanza punti.
                    // L'utente deve aggiungere un altro punto o resettare.
                }
            } else { // Aggiunge un nuovo punto
                currentPoints.add(new Point2D(p));
                if (view != null) {
                    view.drawTemporaryPolygonGuide(currentPoints, p); // Aggiorna con il nuovo punto, p è anche rubberBandEnd per il drag successivo
                }
            }
        }
        engine.notifyViewToRefresh(); 
    }

    @Override
    public void onMouseDragged(GeoEngine engine, Point2D p) {
        DrawingView view = engine.getView();
        if (isDrawing && !currentPoints.isEmpty() && view != null) {
            view.drawTemporaryPolygonGuide(currentPoints, p); // 'p' è il rubberBandEnd
            engine.notifyViewToRefresh();
        }
    }

    @Override
    public void onMouseReleased(GeoEngine engine, Point2D p) { /* No action specifica per il rilascio in questo tool */ }
    
    // Chiamato da GeoEngine su Enter o double-click
    public void tryFinishPolygonOnAction(GeoEngine engine) { 
        if (isDrawing && currentPoints.size() >= 3) {
            finishPolygon(engine);
        } else if (isDrawing) {
            DrawingView view = engine.getView();
            if(view != null) view.showUserMessage("Not enough points for a polygon ("+ currentPoints.size() + "/3 min). Add more points or reset tool.");
            else engine.notifyGeoEngineObservers("PolygonFeedback: Not enough points. Add more or reset.");
        }
    }

    private void finishPolygon(GeoEngine engine) {
        DrawingView view = engine.getView();
        if (view != null) {
            view.clearTemporaryVisuals(); 
            view.showUserMessage("Polygon created!"); // Messaggio di successo
        }
        
        ColorData strokeColor = engine.getCurrentStrokeColorForNewShapes();
        ColorData fillColor = engine.getCurrentFillColorForNewShapes();
        Map<String, Object> params = new HashMap<>();
        // Passa una copia difensiva dei vertici
        params.put("vertices", new ArrayList<>(currentPoints)); 
        
        Shape newPolygon = engine.getShapeFactory().createShape(getName(), null, null, strokeColor, fillColor, params);
        
        if (newPolygon != null) {
            engine.getCommandManager().executeCommand(new AddShapeCommand(engine.getDrawing(), newPolygon));
        }
        
        currentPoints.clear(); 
        isDrawing = false;
        // Non è necessario un engine.notifyViewToRefresh() qui se AddShapeCommand lo causa tramite il modello Drawing.
    }
}