// File: sad/gruppo11/Controller/EllipseState.java
package sad.gruppo11.Controller;

import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.EllipseShape; // Per la ghost shape
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Rect;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Factory.ShapeFactory; // Usato tramite GeoEngine
import sad.gruppo11.Infrastructure.AddShapeCommand; // Usato tramite GeoEngine
import sad.gruppo11.View.DrawingView;

import java.util.Objects;

public class EllipseState implements ToolState {
    private Point2D firstCorner; 

    @Override
    public String getName() { return "EllipseTool"; }
    
    @Override
    public void activate(GeoEngine engine) { 
        this.firstCorner = null; 
        if(engine.getView() != null) engine.getView().showUserMessage("Ellipse Tool: Click and drag to draw an ellipse.");
    }

    @Override
    public void deactivate(GeoEngine engine) { 
        this.firstCorner = null; 
        DrawingView view = engine.getView();
        if (view != null) {
            view.clearTemporaryVisuals();
            view.clearUserMessage();
        }
    }

    @Override
    public void onMousePressed(GeoEngine engine, Point2D p) {
        Objects.requireNonNull(engine); Objects.requireNonNull(p);
        this.firstCorner = new Point2D(p);
    }

    @Override
    public void onMouseDragged(GeoEngine engine, Point2D p) {
        Objects.requireNonNull(engine); Objects.requireNonNull(p);
        DrawingView view = engine.getView();
        if (this.firstCorner != null && view != null) {
            double x = Math.min(firstCorner.getX(), p.getX());
            double y = Math.min(firstCorner.getY(), p.getY());
            double width = Math.abs(firstCorner.getX() - p.getX());
            double height = Math.abs(firstCorner.getY() - p.getY());

            if (width > 0 && height > 0) {
                Rect ghostBounds = new Rect(new Point2D(x,y), width, height);
                EllipseShape ghost = new EllipseShape(ghostBounds, 
                                                      engine.getCurrentStrokeColorForNewShapes(), 
                                                      engine.getCurrentFillColorForNewShapes());
                view.drawTemporaryGhostShape(ghost);
                engine.notifyViewToRefresh();
            } else {
                view.clearTemporaryVisuals();
                engine.notifyViewToRefresh();
            }
        }
    }

    @Override
    public void onMouseReleased(GeoEngine engine, Point2D p) {
        Objects.requireNonNull(engine); Objects.requireNonNull(p);
        DrawingView view = engine.getView();
        if (view != null) {
            view.clearTemporaryVisuals();
        }

        if (this.firstCorner != null) {
            Point2D secondCorner = new Point2D(p);
            if (Math.abs(firstCorner.getX() - secondCorner.getX()) < 1.0 || 
                Math.abs(firstCorner.getY() - secondCorner.getY()) < 1.0) {
                this.firstCorner = null;
                engine.notifyViewToRefresh();
                return;
            }

            ColorData strokeColor = engine.getCurrentStrokeColorForNewShapes();
            ColorData fillColor = engine.getCurrentFillColorForNewShapes();
            
            Shape newEllipse = engine.getShapeFactory().createShape(getName(), this.firstCorner, secondCorner, strokeColor, fillColor, null);
            
            if (newEllipse != null) {
                engine.getCommandManager().executeCommand(new AddShapeCommand(engine.getDrawing(), newEllipse));
            }
            this.firstCorner = null;
            if(view != null) view.showUserMessage("Ellipse created.");
        }
    }
}