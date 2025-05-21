package  sad.gruppo11.Controller;

import java.util.Objects;

import  sad.gruppo11.Infrastructure.AddShapeCommand;
import  sad.gruppo11.Model.EllipseShape;
import  sad.gruppo11.Model.geometry.Point2D;
import  sad.gruppo11.Model.geometry.Rect;

// Classe che implementa lo stato dello strumento per disegnare ellissi
public class EllipseState implements ToolState {
    private Point2D firstCorner; // Memorizza il primo angolo del bounding box dell'ellisse

    @Override
    public void onMousePressed(Point2D p, GeoEngine engine) {
        // Verifica che i parametri non siano nulli
        Objects.requireNonNull(p, "Point cannot be null in onMousePressed");
        Objects.requireNonNull(engine, "GeoEngine cannot be null in onMousePressed");

        // Salva la posizione del primo click come primo angolo dell’ellisse
        this.firstCorner = new Point2D(p);
        // System.out.println(getToolName() + ": Mouse Pressed at " + p);
    }

    @Override
    public void onMouseDragged(Point2D p, GeoEngine engine) {
        // Verifica che i parametri non siano nulli
        Objects.requireNonNull(p, "Point cannot be null in onMouseDragged");
        Objects.requireNonNull(engine, "GeoEngine cannot be null in onMouseDragged");

        if (this.firstCorner != null) {
            // Se l'utente ha già cliccato, potrebbe essere utile aggiornare la visualizzazione
            // dell'ellisse in tempo reale (ghost ellipse), ma il codice è commentato
            // System.out.println(getToolName() + ": Mouse Dragged to " + p);
            // Aggiorna visualizzazione ghost ellipse
        }
    }

    @Override
    public void onMouseReleased(Point2D p, GeoEngine engine) {
        // Verifica che i parametri non siano nulli
        Objects.requireNonNull(p, "Point cannot be null in onMouseReleased");
        Objects.requireNonNull(engine, "GeoEngine cannot be null in onMouseReleased");

        if (this.firstCorner != null) {
            // Crea un secondo punto (rilascio del mouse)
            Point2D secondCorner = new Point2D(p);

            // Calcola il rettangolo che racchiude l’ellisse
            double x = Math.min(firstCorner.getX(), secondCorner.getX());
            double y = Math.min(firstCorner.getY(), secondCorner.getY());
            double width = Math.abs(firstCorner.getX() - secondCorner.getX());
            double height = Math.abs(firstCorner.getY() - secondCorner.getY());

            // Verifica che l’ellisse non sia troppo piccola (degenere)
            if (width > 1e-3 && height > 1e-3) {
                // Crea il bounding box
                Rect bounds = new Rect(new Point2D(x,y), width, height);
                // Crea una nuova ellisse con i bounds calcolati
                EllipseShape newEllipse = new EllipseShape(bounds);

                // Imposta i colori (tratto e riempimento) correnti dal motore
                newEllipse.setStrokeColor(engine.getCurrentStrokeColorForNewShapes());
                newEllipse.setFillColor(engine.getCurrentFillColorForNewShapes());

                // Crea e esegue il comando per aggiungere la nuova forma al disegno
                AddShapeCommand addCmd = new AddShapeCommand(engine.getDrawing(), newEllipse);
                engine.getCommandManager().execute(addCmd);

                // System.out.println(getToolName() + ": Ellipse created with bounds: " + bounds);
            } else {
                // Se troppo piccola, non crea la figura
                // System.out.println(getToolName() + ": Ellipse too small, not created.");
            }

            // Resetta il primo punto per disegnare una nuova figura in futuro
            this.firstCorner = null;
        }
    }

    @Override
    public String getToolName() {
        // Restituisce il nome dello strumento corrente
        return "Ellipse Tool";
    }

    @Override
    public void onEnterState(GeoEngine engine) {
        // Inizializza lo stato interno quando si entra nello strumento
        this.firstCorner = null;
        // System.out.println(getToolName() + ": Entering state, firstCorner initialized.");
    }

    @Override
    public void onExitState(GeoEngine engine) {
        // Resetta lo stato interno quando si esce dallo strumento
        this.firstCorner = null;
        // System.out.println(getToolName() + ": Exiting state, firstCorner reset.");
    }
}
