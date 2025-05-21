package sad.gruppo11.Model.geometry;

import java.io.Serializable;
import java.util.Objects;

/*
 * Point2D rappresenta un punto nel piano con coordinate double (x, y).
 * Fornisce metodi per copia, distanza, traslazione e confronto.
 */
public class Point2D implements Serializable {

    /* Coordinata X */
    private double x;

    /* Coordinata Y */
    private double y;

    /*
     * Costruttore principale.
     *
     * @param x Coordinata X
     * @param y Coordinata Y
     */
    public Point2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /*
     * Costruttore di copia.
     *
     * @param other Punto da copiare
     */
    public Point2D(Point2D other) {
        this.x = other.x;
        this.y = other.y;
    }

    /* Getter e setter */

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    /*
     * Calcola la distanza euclidea da un altro punto.
     *
     * @param other Il punto di destinazione
     * @return La distanza tra i due punti
     */
    public double distance(Point2D other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /*
     * Trasla il punto di un vettore (dx, dy).
     *
     * @param dx Spostamento lungo X
     * @param dy Spostamento lungo Y
     */
    public void translate(double dx, double dy) {
        this.x += dx;
        this.y += dy;
    }

    /*
     * Due punti sono uguali se hanno stesse coordinate.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point2D point2D = (Point2D) o;
        return Double.compare(point2D.x, x) == 0 &&
               Double.compare(point2D.y, y) == 0;
    }

    /*
     * Genera hash coerente con equals.
     */
    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    /*
     * Rappresentazione testuale del punto.
     */
    @Override
    public String toString() {
        return "Point2D{" +
               "x=" + x +
               ", y=" + y +
               '}';
    }
}
