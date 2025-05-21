package sad.gruppo11.Model.geometry;

import java.io.Serializable;
import java.util.Objects;

/*
 * Classe che rappresenta un vettore bidimensionale (dx, dy).
 * Include operazioni di base su vettori come normalizzazione e inversione.
 */
public class Vector2D implements Serializable {

    /* Componente X del vettore */
    private double dx;

    /* Componente Y del vettore */
    private double dy;

    /*
     * Costruttore principale.
     *
     * @param dx componente lungo l'asse X
     * @param dy componente lungo l'asse Y
     */
    public Vector2D(double dx, double dy) {
        this.dx = dx;
        this.dy = dy;
    }

    /*
     * Costruttore di copia.
     *
     * @param other Vettore da copiare
     */
    public Vector2D(Vector2D other) {
        this.dx = other.dx;
        this.dy = other.dy;
    }

    /* Getter e setter */

    public double getDx() {
        return dx;
    }

    public void setDx(double dx) {
        this.dx = dx;
    }

    public double getDy() {
        return dy;
    }

    public void setDy(double dy) {
        this.dy = dy;
    }

    /*
     * Calcola la lunghezza (magnitudine) del vettore.
     *
     * @return sqrt(dx^2 + dy^2)
     */
    public double length() {
        return Math.sqrt(dx * dx + dy * dy);
    }

    /*
     * Normalizza il vettore rendendolo unitario (lunghezza 1).
     * Modifica l'istanza corrente.
     */
    public void normalize() {
        double len = length();
        if (len != 0) {
            this.dx /= len;
            this.dy /= len;
        }
    }

    /*
     * Restituisce una nuova istanza normalizzata senza modificare l'originale.
     *
     * @return Vettore normalizzato oppure (0,0) se vettore nullo
     */
    public Vector2D normalized() {
        double len = length();
        if (len != 0) {
            return new Vector2D(this.dx / len, this.dy / len);
        }
        return new Vector2D(0, 0);
    }

    /*
     * Restituisce il vettore inverso (negazione di dx e dy).
     *
     * @return nuovo vettore invertito
     */
    public Vector2D inverse() {
        return new Vector2D(-this.dx, -this.dy);
    }

    /*
     * Confronto tra vettori: uguali se dx e dy coincidono.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vector2D vector2D = (Vector2D) o;
        return Double.compare(vector2D.dx, dx) == 0 &&
               Double.compare(vector2D.dy, dy) == 0;
    }

    /*
     * Hashcode coerente con equals.
     */
    @Override
    public int hashCode() {
        return Objects.hash(dx, dy);
    }

    /*
     * Rappresentazione testuale del vettore.
     */
    @Override
    public String toString() {
        return "Vector2D{" +
               "dx=" + dx +
               ", dy=" + dy +
               '}';
    }
}
