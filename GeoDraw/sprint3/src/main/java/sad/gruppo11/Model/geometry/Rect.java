
package sad.gruppo11.Model.geometry;

import java.io.Serializable;
import java.util.Objects;

public class Rect implements Serializable {
    private Point2D topLeft;
    private double width;
    private double height;

    public Rect(Point2D topLeft, double width, double height) {
        Objects.requireNonNull(topLeft, "TopLeft point cannot be null for Rect.");
        this.topLeft = new Point2D(topLeft);
        this.width = Math.max(0, width);
        this.height = Math.max(0, height);
    }

    public Rect(double x, double y, double width, double height) {
       this(new Point2D(x,y), width, height);
    }

    public Rect(Rect other) {
        Objects.requireNonNull(other, "Other Rect cannot be null for copy constructor.");
        this.topLeft = new Point2D(other.topLeft);
        this.width = other.width;
        this.height = other.height;
    }

    public Point2D getTopLeft() {
        return new Point2D(topLeft);
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }
    
    public void setTopLeft(Point2D topLeft) {
        Objects.requireNonNull(topLeft, "TopLeft point cannot be null for setTopLeft.");
        this.topLeft = new Point2D(topLeft);
    }
    
    public void setTopLeft(double x, double y) {
        this.topLeft.setX(x);
        this.topLeft.setY(y);
    }

    public void setWidth(double width) {
        this.width = Math.max(0, width);
    }

    public void setHeight(double height) {
        this.height = Math.max(0, height);
    }
    
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

    public boolean contains(Point2D p) {
        Objects.requireNonNull(p, "Point p cannot be null for Rect.contains.");
        double px = p.getX();
        double py = p.getY();
        return px >= getX() && px <= getRight() &&
               py >= getY() && py <= getBottom();
    }
    
    public void translate(double dx, double dy) {
        this.topLeft.translate(dx, dy);
    }
    
    public void translate(Vector2D v) {
        Objects.requireNonNull(v, "Vector v cannot be null for Rect.translate");
        this.topLeft.translate(v);
    }

    public Rect translated(double dx, double dy) {
        Point2D newTopLeft = new Point2D(this.topLeft.getX() + dx, this.topLeft.getY() + dy);
        return new Rect(newTopLeft, this.width, this.height);
    }

    public Rect translated(Vector2D v) {
        Objects.requireNonNull(v, "Vector v cannot be null for Rect.translated");
        return translated(v.getDx(), v.getDy());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rect rect = (Rect) o;
        return Double.compare(rect.width, width) == 0 &&
               Double.compare(rect.height, height) == 0 &&
               Objects.equals(topLeft, rect.topLeft);
    }

    @Override
    public int hashCode() {
        return Objects.hash(topLeft, width, height);
    }

    @Override
    public String toString() {
        return String.format("Rect{topLeft=%s, width=%.2f, height=%.2f}", topLeft, width, height);
    }
}