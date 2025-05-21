package  sad.gruppo11.Controller;

import java.util.Objects;

import  sad.gruppo11.Factory.ShapeFactory;
import  sad.gruppo11.Infrastructure.AddShapeCommand;
import  sad.gruppo11.Model.LineSegment;
import  sad.gruppo11.Model.geometry.Point2D;

// Classe che implementa lo stato dello strumento per disegnare linee
public class LineState implements ToolState {
    private Point2D startPoint; // Memorizza il punto di inizio della linea

    @Override
    public void onMousePressed(Point2D p, GeoEngine engine) {
        // Verifica che il punto e il motore non siano null
        Objects.requireNonNull(p, "Point cannot be null in onMousePressed");
        Objects.requireNonNull(engine, "GeoEngine cannot be null in onMousePressed");

        // Salva il punto di partenza come copia
        this.startPoint = new Point2D(p);

        // System.out.println(getToolName() + ": Mouse Pressed at " + p); // Debug
        // Possibile punto per iniziare a disegnare una linea temporanea (ghost line)
    }

    @Override
    public void onMouseDragged(Point2D p, GeoEngine engine) {
        // Verifica che il punto e il motore non siano null
        Objects.requireNonNull(p, "Point cannot be null in onMouseDragged");
        Objects.requireNonNull(engine, "GeoEngine cannot be null in onMouseDragged");

        if (this.startPoint != null) {
            // System.out.println(getToolName() + ": Mouse Dragged to " + p); // Debug
            // Qui si potrebbe aggiornare la visualizzazione della ghost line
            // In un'applicazione più avanzata si potrebbe ridisegnare solo la regione interessata
        }
    }

    @Override
    public void onMouseReleased(Point2D p, GeoEngine engine) {
        // Verifica che il punto e il motore non siano null
        Objects.requireNonNull(p, "Point cannot be null in onMouseReleased");
        Objects.requireNonNull(engine, "GeoEngine cannot be null in onMouseReleased");

        if (this.startPoint != null) {
            // Crea una copia del punto finale
            Point2D endPoint = new Point2D(p);

            // Controlla che la distanza tra i punti sia significativa (evita linee nulle o quasi nulle)
            if (startPoint.distance(endPoint) > 1e-3) {

                // Opzione 1: Creazione diretta della linea
                LineSegment newLine = new LineSegment(this.startPoint, endPoint);
                
                // TODO: Rimuovere i commenti e le righe di debug
                System.out.println("Pressed: " + startPoint);
                System.out.println("Released: " + endPoint);

                // Imposta il colore di bordo corrente
                newLine.setStrokeColor(engine.getCurrentStrokeColorForNewShapes());

                // Opzione 2 (non usata): Uso della ShapeFactory per creare la linea
                // Esempio: ShapeFactory.createLine(this.startPoint, endPoint)

                // Aggiungi la linea al disegno tramite comando
                AddShapeCommand addCmd = new AddShapeCommand(engine.getDrawing(), newLine);
                engine.getCommandManager().execute(addCmd);

                // System.out.println(getToolName() + ": Line created from " + this.startPoint + " to " + endPoint); // Debug
            } else {
                // System.out.println(getToolName() + ": Line too short, not created."); // Debug
            }

            // Resetta il punto di partenza per il prossimo utilizzo dello strumento
            this.startPoint = null;
        }
    }

    @Override
    public void onEnterState(GeoEngine engine) {
        // Resetta lo stato interno quando questo strumento diventa attivo.
        // È buona pratica assicurarsi che lo stato sia pulito.
        this.startPoint = null;
        // System.out.println(getToolName() + ": Entering state, startPoint reset."); // Per debug se vuoi
    }

    @Override
    public String getToolName() {
        // Restituisce il nome dello strumento
        return "Line Tool";
    }

    @Override
    public void onExitState(GeoEngine engine) {
        // Resetta lo stato quando si esce dallo strumento
        this.startPoint = null;

        // In un'app reale, qui si potrebbe anche rimuovere la ghost line se presente
        // System.out.println(getToolName() + ": Exiting state, startPoint reset.");
    }
}
