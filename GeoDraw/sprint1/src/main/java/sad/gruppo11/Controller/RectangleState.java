package  sad.gruppo11.Controller;

import java.util.Objects;

import  sad.gruppo11.Infrastructure.AddShapeCommand;
import  sad.gruppo11.Model.RectangleShape;
import  sad.gruppo11.Model.geometry.Point2D;
import  sad.gruppo11.Model.geometry.Rect;

// Classe che rappresenta lo stato dello strumento rettangolo
public class RectangleState implements ToolState {
    private Point2D firstCorner; // Memorizza il primo angolo del rettangolo (punto iniziale del drag)

    @Override
    public void onMousePressed(Point2D p, GeoEngine engine) {
        // Verifica che i parametri non siano null
        Objects.requireNonNull(p, "Point cannot be null in onMousePressed");
        Objects.requireNonNull(engine, "GeoEngine cannot be null in onMousePressed");

        // Salva una copia del punto iniziale (primo angolo del rettangolo)
        this.firstCorner = new Point2D(p);

        // System.out.println(getToolName() + ": Mouse Pressed at " + p);
        // In questa fase si potrebbe iniziare a visualizzare un rettangolo temporaneo (ghost rectangle)
    }

    @Override
    public void onMouseDragged(Point2D p, GeoEngine engine) {
        // Verifica che i parametri non siano null
        Objects.requireNonNull(p, "Point cannot be null in onMouseDragged");
        Objects.requireNonNull(engine, "GeoEngine cannot be null in onMouseDragged");

        if (this.firstCorner != null) {
            // System.out.println(getToolName() + ": Mouse Dragged to " + p);
            // Durante il trascinamento si potrebbe aggiornare la visualizzazione del ghost rectangle
        }
    }

    @Override
    public void onMouseReleased(Point2D p, GeoEngine engine) {
        // Verifica che i parametri non siano null
        Objects.requireNonNull(p, "Point cannot be null in onMouseReleased");
        Objects.requireNonNull(engine, "GeoEngine cannot be null in onMouseReleased");

        if (this.firstCorner != null) {
            // Crea una copia del secondo punto (rilascio del mouse)
            Point2D secondCorner = new Point2D(p);
            
            // TODO: Rimuovere i commenti e le righe di debug
            System.out.println("Pressed: " + firstCorner);
            System.out.println("Released: " + secondCorner);

            // Calcola coordinate e dimensioni del bounding box del rettangolo
            double x = Math.min(firstCorner.getX(), secondCorner.getX());
            double y = Math.min(firstCorner.getY(), secondCorner.getY());
            double width = Math.abs(firstCorner.getX() - secondCorner.getX());
            double height = Math.abs(firstCorner.getY() - secondCorner.getY());

            // Verifica che il rettangolo abbia una dimensione significativa
            if (width > 1e-2 && height > 1e-2) {
                // Crea l’oggetto bounds rettangolare
                Rect bounds = new Rect(new Point2D(x,y), width, height);

                // Crea il rettangolo con i bounds calcolati
                RectangleShape newRectangle = new RectangleShape(bounds); // Il costruttore imposta già valori di default

                // Applica colori attualmente selezionati nel motore
                newRectangle.setStrokeColor(engine.getCurrentStrokeColorForNewShapes());
                newRectangle.setFillColor(engine.getCurrentFillColorForNewShapes());

                // Aggiunge la forma al modello tramite comando (per supportare undo/redo)
                AddShapeCommand addCmd = new AddShapeCommand(engine.getDrawing(), newRectangle);
                engine.getCommandManager().execute(addCmd);
            }

            // Resetta lo stato in vista di un nuovo disegno
            this.firstCorner = null;
        }
    }

    @Override
    public String getToolName() {
        // Restituisce il nome dello strumento attivo
        return "Rectangle Tool";
    }

    @Override
    public void onExitState(GeoEngine engine) {
        // Pulisce lo stato interno quando si esce dallo strumento
        this.firstCorner = null;
        // System.out.println(getToolName() + ": Exiting state, firstCorner reset.");
    }

    @Override
    public void onEnterState(GeoEngine engine) {
        // Reset the firstCorner so no rectangle can be created until a new press
        this.firstCorner = null;
    }
}
