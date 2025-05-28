// File: sad/gruppo11/Controller/RectangleState.java
package sad.gruppo11.Controller;

import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.RectangleShape; // Per la ghost shape
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Rect;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Factory.ShapeFactory; // Usato tramite GeoEngine
import sad.gruppo11.Infrastructure.AddShapeCommand; // Usato tramite GeoEngine
import sad.gruppo11.View.DrawingView;

import java.util.Objects;

public class RectangleState implements ToolState {
    private Point2D firstCorner;

    @Override
    public String getName() { return "RectangleTool"; }

    @Override
    public void activate(GeoEngine engine) { 
        this.firstCorner = null; 
        if(engine.getView() != null) engine.getView().showUserMessage("Rectangle Tool: Click and drag to draw a rectangle.");
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

            if (width > 0 && height > 0) { // Solo se ha dimensioni valide
                Rect ghostBounds = new Rect(new Point2D(x,y), width, height);
                // Creiamo una ghost shape con i colori correnti, CanvasPanel la stilizzerà
                RectangleShape ghost = new RectangleShape(ghostBounds, 
                                                          engine.getCurrentStrokeColorForNewShapes(), 
                                                          engine.getCurrentFillColorForNewShapes());
                view.drawTemporaryGhostShape(ghost);
                engine.notifyViewToRefresh();
            } else { // Se width o height sono 0, pulisci la ghost precedente
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
            
            Shape newRectangle = engine.getShapeFactory().createShape(getName(), this.firstCorner, secondCorner, strokeColor, fillColor, null);

            if (newRectangle != null) {
                engine.getCommandManager().executeCommand(new AddShapeCommand(engine.getDrawing(), newRectangle));
            }
            this.firstCorner = null;
            if(view != null) view.showUserMessage("Rectangle created.");
        }
    }
}