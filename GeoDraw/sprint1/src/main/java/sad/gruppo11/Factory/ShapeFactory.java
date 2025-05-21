package sad.gruppo11.Factory;

import java.util.Objects;

import sad.gruppo11.Controller.EllipseState;
import sad.gruppo11.Controller.LineState;
import sad.gruppo11.Controller.RectangleState;
import sad.gruppo11.Controller.ToolState;
import sad.gruppo11.Model.EllipseShape;
import sad.gruppo11.Model.LineSegment;
import sad.gruppo11.Model.RectangleShape;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Rect;

/*
 * ShapeFactory è responsabile della creazione di istanze concrete di Shape.
 * Utilizza il pattern Factory Method per disaccoppiare la logica di creazione delle forme
 * dal client che le richiede (es. gli stati degli strumenti come LineState, RectangleState, ecc.).
 */
public class ShapeFactory {

    /*
     * Crea una nuova istanza di Shape basata sul tipo di ToolState fornito e sui due punti.
     *
     * @param tool Lo stato dello strumento attivo (es. LineState, RectangleState, ecc.)
     * @param p1   Il primo punto di definizione della forma (es. inizio linea, primo angolo rettangolo)
     * @param p2   Il secondo punto di definizione (es. fine linea, angolo opposto)
     * @return     Una nuova Shape se i dati sono validi, null altrimenti
     */
    public Shape createShape(ToolState tool, Point2D p1, Point2D p2) {
        Objects.requireNonNull(tool, "ToolState cannot be null for shape creation.");
        Objects.requireNonNull(p1, "Point p1 cannot be null for shape creation.");
        Objects.requireNonNull(p2, "Point p2 cannot be null for shape creation.");

        /*
         * Creazione di una LineSegment, se la distanza tra i punti è sufficiente
         */
        if (tool instanceof LineState) {
            if (p1.distance(p2) > 1e-3) {
                return new LineSegment(new Point2D(p1), new Point2D(p2));
            } else {
                return null;
            }
        }

        /*
         * Creazione di un RectangleShape, se larghezza e altezza sono valide
         */
        if (tool instanceof RectangleState) {
            double x = Math.min(p1.getX(), p2.getX());
            double y = Math.min(p1.getY(), p2.getY());
            double width = Math.abs(p1.getX() - p2.getX());
            double height = Math.abs(p1.getY() - p2.getY());

            if (width > 1e-3 && height > 1e-3) {
                Rect bounds = new Rect(new Point2D(x, y), width, height);
                return new RectangleShape(bounds);
            } else {
                return null;
            }
        }

        /*
         * Creazione di un EllipseShape, se larghezza e altezza sono valide
         */
        if (tool instanceof EllipseState) {
            double x = Math.min(p1.getX(), p2.getX());
            double y = Math.min(p1.getY(), p2.getY());
            double width = Math.abs(p1.getX() - p2.getX());
            double height = Math.abs(p1.getY() - p2.getY());

            if (width > 1e-3 && height > 1e-3) {
                Rect bounds = new Rect(new Point2D(x, y), width, height);
                return new EllipseShape(bounds);
            } else {
                return null;
            }
        }

        /*
         * Strumento non riconosciuto o non supportato
         */
        return null;
    }

    /*
     * In futuro si potrebbero aggiungere metodi dedicati per ogni tipo di forma,
     * evitando di passare ToolState (es. createLine, createRectangle, ecc.).
     */
}
