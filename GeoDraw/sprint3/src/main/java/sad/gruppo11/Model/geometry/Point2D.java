
package sad.gruppo11.Model.geometry;

import java.io.Serializable;
import java.util.Objects;

public class Point2D implements Serializable {
    private double x;
    private double y;

    public Point2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Point2D(Point2D other) {
        Objects.requireNonNull(other, "Other Point2D cannot be null for copy constructor.");
        this.x = other.x;
        this.y = other.y;
    }

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

    public double distance(Point2D other) {
        Objects.requireNonNull(other, "Other Point2D cannot be null for distance calculation.");
        double dx_ = this.x - other.x;
        double dy_ = this.y - other.y;
        return Math.sqrt(dx_ * dx_ + dy_ * dy_);
    }

    public void translate(double dx_, double dy_) {
        this.x += dx_;
        this.y += dy_;
    }
    
    public void translate(Vector2D v) {
        Objects.requireNonNull(v, "Vector2D v cannot be null for translation.");
        this.x += v.getDx();
        this.y += v.getDy();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point2D point2D = (Point2D) o;
        return Double.compare(point2D.x, x) == 0 &&
               Double.compare(point2D.y, y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "Point2D{" +
               "x=" + x +
               ", y=" + y +
               '}';
    }
}