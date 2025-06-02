
package sad.gruppo11.Controller;

import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Infrastructure.AddShapeCommand;
import sad.gruppo11.View.DrawingView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;

public class PolygonState implements ToolState {
    private List<Point2D> currentPoints;
    private boolean isDrawing;
    // Soglia di distanza per chiudere il poligono cliccando vicino al punto iniziale (in coordinate mondo)
    // PUBLIC FOR TESTS ONLY
    public static final double CLOSE_POLYGON_THRESHOLD_WORLD = 10.0; 

    public boolean isDrawing()
    {
        return isDrawing;
    }

    @Override
    public String getName() { return "PolygonTool"; }

    @Override
    public void activate(GeoEngine engine) {
        currentPoints = new ArrayList<>(); 
        isDrawing = false;
        engine.clearSelection();
        if(engine.getView() != null) engine.getView().showUserMessage("Polygon Tool: Click to add points. Click near start or Enter to finish.");
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
    }

    @Override
    public void onMousePressed(GeoEngine engine, Point2D p) {
        Objects.requireNonNull(engine); Objects.requireNonNull(p);
        DrawingView view = engine.getView();

        if (!isDrawing) { 
            currentPoints.add(new Point2D(p));
            isDrawing = true;
            if (view != null) {
                view.drawTemporaryPolygonGuide(currentPoints, p); 
            }
        } else {
            // La soglia di chiusura deve tenere conto dello zoom corrente
            double closeThresholdScreenScaled = CLOSE_POLYGON_THRESHOLD_WORLD / engine.getCurrentZoom();
            if (currentPoints.size() >= 2 && p.distance(currentPoints.get(0)) < closeThresholdScreenScaled) {
                if (currentPoints.size() >= 3) { 
                    finishPolygon(engine);
                } else {
                    if(view != null) view.showUserMessage("A polygon needs at least 3 points. Current: " + currentPoints.size());
                }
            } else { 
                currentPoints.add(new Point2D(p));
                if (view != null) {
                    view.drawTemporaryPolygonGuide(currentPoints, p); 
                }
            }
        }
        engine.notifyViewToRefresh(); 
    }

    @Override
    public void onMouseDragged(GeoEngine engine, Point2D p) {
        DrawingView view = engine.getView();
        if (isDrawing && !currentPoints.isEmpty() && view != null) {
            view.drawTemporaryPolygonGuide(currentPoints, p); 
            engine.notifyViewToRefresh();
        }
    }

    @Override
    public void onMouseReleased(GeoEngine engine, Point2D p) { /* No action */ }
    
    public void tryFinishPolygonOnAction(GeoEngine engine) { 
        if (isDrawing && currentPoints.size() >= 3) {
            finishPolygon(engine);
        } else if (isDrawing) {
            DrawingView view = engine.getView();
            if(view != null) view.showUserMessage("Not enough points for a polygon ("+ currentPoints.size() + "/3 min).");
        }
    }

    private void finishPolygon(GeoEngine engine) {
        DrawingView view = engine.getView();
        if (view != null) {
            view.clearTemporaryVisuals(); 
            view.showUserMessage("Polygon created!");
        }
        
        ColorData strokeColor = engine.getCurrentStrokeColorForNewShapes();
        ColorData fillColor = engine.getCurrentFillColorForNewShapes();
        Map<String, Object> params = new HashMap<>();
        params.put("vertices", new ArrayList<>(currentPoints)); 
        
        Shape newPolygon = engine.getShapeFactory().createShape(getName(), null, null, strokeColor, fillColor, params);
        
        if (newPolygon != null) {
            engine.addShapeToDrawing(newPolygon);
        }
        
        currentPoints.clear(); 
        isDrawing = false;
    }

    public List<Point2D> getCurrentPoints() {
        return currentPoints;
    }
}
