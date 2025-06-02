
package sad.gruppo11.Controller;

import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.LineSegment;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Infrastructure.AddShapeCommand;
import sad.gruppo11.View.DrawingView;

import java.util.Objects;

public class LineState implements ToolState {
    private Point2D firstPoint;

    @Override
    public String getName() { return "LineTool"; }
    
    @Override
    public void activate(GeoEngine engine) {
        this.firstPoint = null;
        engine.clearSelection(); // Deseleziona tutto
        if(engine.getView() != null) engine.getView().showUserMessage("Line Tool: Click and drag to draw a line.");
    }

    @Override
    public void deactivate(GeoEngine engine) {
        this.firstPoint = null;
        DrawingView view = engine.getView();
        if (view != null) {
            view.clearTemporaryVisuals();
            view.clearUserMessage();
        }
    }

    @Override
    public void onMousePressed(GeoEngine engine, Point2D p) {
        Objects.requireNonNull(engine); Objects.requireNonNull(p);
        this.firstPoint = new Point2D(p);
    }

    @Override
    public void onMouseDragged(GeoEngine engine, Point2D p) {
        Objects.requireNonNull(engine); Objects.requireNonNull(p);
        DrawingView view = engine.getView();
        if (this.firstPoint != null && view != null) {
            LineSegment ghost = new LineSegment(firstPoint, p, engine.getCurrentStrokeColorForNewShapes());
            view.drawTemporaryGhostShape(ghost);
            engine.notifyViewToRefresh(); 
        }
    }

    @Override
    public void onMouseReleased(GeoEngine engine, Point2D p) {
        Objects.requireNonNull(engine); Objects.requireNonNull(p);
        DrawingView view = engine.getView();
        if (view != null) {
            view.clearTemporaryVisuals(); 
        }

        if (this.firstPoint != null) {
            Point2D secondPoint = new Point2D(p);
            // Verifica una distanza minima per creare la forma
            if (firstPoint.distance(secondPoint) < 1.0 / engine.getCurrentZoom()) { 
                this.firstPoint = null; 
                engine.notifyViewToRefresh();
                return; 
            }
            
            ColorData strokeColor = engine.getCurrentStrokeColorForNewShapes();
            ColorData fillColor = ColorData.TRANSPARENT; 
            
            Shape newLine = engine.getShapeFactory().createShape(getName(), this.firstPoint, secondPoint, strokeColor, fillColor, null);
            
            if (newLine != null) {
                engine.addShapeToDrawing(newLine); // addShapeToDrawing usa AddShapeCommand
            }
            this.firstPoint = null;
            if(view != null) view.showUserMessage("Line created.");
        }
    }
}
