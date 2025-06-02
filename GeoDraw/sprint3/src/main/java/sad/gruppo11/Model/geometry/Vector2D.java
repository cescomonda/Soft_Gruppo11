
package sad.gruppo11.Model.geometry;

import java.io.Serializable;
import java.util.Objects;

public class Vector2D implements Serializable {
    private double dx;
    private double dy;

    public Vector2D(double dx, double dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public Vector2D(Vector2D other) {
        Objects.requireNonNull(other, "Other Vector2D cannot be null for copy constructor.");
        this.dx = other.dx;
        this.dy = other.dy;
    }

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

    public double length() {
        return Math.sqrt(dx * dx + dy * dy);
    }

    public void normalize() {
        double len = length();
        if (len != 0 && len != 1.0) {
            this.dx /= len;
            this.dy /= len;
        }
    }

    public Vector2D normalized() {
        double len = length();
        if (len != 0) {
            return new Vector2D(this.dx / len, this.dy / len);
        }
        return new Vector2D(0, 0);
    }
    
    public Vector2D inverse() {
        return new Vector2D(-this.dx, -this.dy);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vector2D vector2D = (Vector2D) o;
        return Double.compare(vector2D.dx, dx) == 0 &&
               Double.compare(vector2D.dy, dy) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dx, dy);
    }

    @Override
    public String toString() {
        return "Vector2D{" +
               "dx=" + dx +
               ", dy=" + dy +
               '}';
    }
}