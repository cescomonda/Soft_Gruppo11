package sad.gruppo11.Model.geometry;

import java.io.Serializable;
import java.util.Objects;

/*
 * Classe che rappresenta un rettangolo con angolo superiore sinistro,
 * larghezza e altezza. Fornisce metodi per manipolazione geometrica
 * e controllo di contenimento.
 */
public class Rect implements Serializable {

    /* Punto superiore sinistro del rettangolo */
    private Point2D topLeft;

    /* Larghezza del rettangolo */
    private double width;

    /* Altezza del rettangolo */
    private double height;

    /*
     * Costruttore principale.
     *
     * @param topLeft Punto superiore sinistro
     * @param width   Larghezza (valore negativo sarà resettato a 0)
     * @param height  Altezza (valore negativo sarà resettato a 0)
     */
    public Rect(Point2D topLeft, double width, double height) {
        if (topLeft == null) {
            throw new IllegalArgumentException("TopLeft point cannot be null.");
        }
        this.topLeft = new Point2D(topLeft); // Copia difensiva
        this.width = Math.max(0, width);
        this.height = Math.max(0, height);
    }

    /*
     * Costruttore alternativo con coordinate X,Y.
     */
    public Rect(double x, double y, double width, double height) {
        this(new Point2D(x, y), width, height);
    }

    /*
     * Costruttore di copia.
     */
    public Rect(Rect other) {
        this.topLeft = new Point2D(other.topLeft);
        this.width = other.width;
        this.height = other.height;
    }

    /* Getter e setter con protezione dell'incapsulamento */

    public Point2D getTopLeft() {
        return new Point2D(topLeft); // Copia difensiva
    }

    public void setTopLeft(Point2D topLeft) {
        if (topLeft == null) {
            throw new IllegalArgumentException("TopLeft point cannot be null.");
        }
        this.topLeft = new Point2D(topLeft);
    }

    public void setTopLeft(double x, double y) {
        this.topLeft.setX(x);
        this.topLeft.setY(y);
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = Math.max(0, width);
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = Math.max(0, height);
    }

    /* Coordinate derivate */

    public double getX() {
        return topLeft.getX();
    }

    public double getY() {
        return topLeft.getY();
    }

    public double getRight() {
        return topLeft.getX() + width;
    }

    public double getBottom() {
        return topLeft.getY() + height;
    }

    public Point2D getBottomRight() {
        return new Point2D(getRight(), getBottom());
    }

    public Point2D getCenter() {
        return new Point2D(getX() + width / 2.0, getY() + height / 2.0);
    }

    /*
     * Verifica se un punto è contenuto all'interno del rettangolo.
     */
    public boolean contains(Point2D p) {
        if (p == null) return false;
        return p.getX() >= getX() &&
               p.getX() <= getRight() &&
               p.getY() >= getY() &&
               p.getY() <= getBottom();
    }

    /*
     * Trasla il rettangolo modificando il punto topLeft.
     */
    public void translate(double dx, double dy) {
        this.topLeft.translate(dx, dy);
    }

    /*
     * Restituisce un nuovo rettangolo traslato.
     */
    public Rect translated(double dx, double dy) {
        return new Rect(new Point2D(topLeft.getX() + dx, topLeft.getY() + dy), width, height);
    }

    /*
     * Due rettangoli sono uguali se topLeft, larghezza e altezza sono uguali.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rect rect = (Rect) o;
        return Double.compare(rect.width, width) == 0 &&
               Double.compare(rect.height, height) == 0 &&
               Objects.equals(topLeft, rect.topLeft);
    }

    /*
     * Genera un hash coerente con equals.
     */
    @Override
    public int hashCode() {
        return Objects.hash(topLeft, width, height);
    }

    /*
     * Rappresentazione testuale del rettangolo.
     */
    @Override
    public String toString() {
        return "Rect{" +
               "topLeft=" + topLeft +
               ", width=" + width +
               ", height=" + height +
               '}';
    }
}
