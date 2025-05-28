// File: sad/gruppo11/Controller/LineState.java
package sad.gruppo11.Controller;

import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.LineSegment; // Per creare la ghost shape
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Factory.ShapeFactory; // Usato tramite GeoEngine
import sad.gruppo11.Infrastructure.AddShapeCommand; // Usato tramite GeoEngine
import sad.gruppo11.View.DrawingView;

import java.util.Objects;

public class LineState implements ToolState {
    private Point2D firstPoint;
    // Non serve un campo ghostLine qui, lo creiamo al volo per drawTemporaryGhostShape

    @Override
    public String getName() { return "LineTool"; }
    
    @Override
    public void activate(GeoEngine engine) {
        this.firstPoint = null;
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
        // Non disegna nulla finché non c'è un drag
    }

    @Override
    public void onMouseDragged(GeoEngine engine, Point2D p) {
        Objects.requireNonNull(engine); Objects.requireNonNull(p);
        DrawingView view = engine.getView();
        if (this.firstPoint != null && view != null) {
            // Crea una ghost line temporanea
            // Usiamo direttamente il costruttore di LineSegment per la ghost shape,
            // perché ShapeFactory potrebbe avere logica aggiuntiva (es. ID univoci) non necessaria per una ghost.
            LineSegment ghost = new LineSegment(firstPoint, p, engine.getCurrentStrokeColorForNewShapes());
            // Nota: La ghost shape qui avrà il colore di stroke corrente.
            // CanvasPanel.drawCurrentTemporaryVisuals si occuperà di stilizzarla (es. tratteggio)
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
            if (firstPoint.distance(secondPoint) < 1.0) { // Soglia minima
                this.firstPoint = null; 
                engine.notifyViewToRefresh(); // Assicura che la ghost line sia sparita
                return; 
            }
            
            ColorData strokeColor = engine.getCurrentStrokeColorForNewShapes();
            // Le linee non usano fillColor, la factory dovrebbe gestirlo o passiamo trasparente
            ColorData fillColor = ColorData.TRANSPARENT; 
            
            Shape newLine = engine.getShapeFactory().createShape(getName(), this.firstPoint, secondPoint, strokeColor, fillColor, null);
            
            if (newLine != null) {
                engine.getCommandManager().executeCommand(new AddShapeCommand(engine.getDrawing(), newLine));
            }
            this.firstPoint = null;
            if(view != null) view.showUserMessage("Line created.");
        }
    }
}