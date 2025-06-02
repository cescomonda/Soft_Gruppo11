
package sad.gruppo11.Model.geometry;

import java.io.Serializable;
import java.util.Objects;

public final class ColorData implements Serializable {
    private int r;
    private int g;
    private int b;
    private double a; 

    public static final ColorData BLACK = new ColorData(0, 0, 0, 1.0);
    public static final ColorData WHITE = new ColorData(255, 255, 255, 1.0);
    public static final ColorData RED = new ColorData(255, 0, 0, 1.0);
    public static final ColorData GREEN = new ColorData(0, 255, 0, 1.0);
    public static final ColorData BLUE = new ColorData(0, 0, 255, 1.0);
    public static final ColorData YELLOW = new ColorData(255, 255, 0, 1.0);
    public static final ColorData TRANSPARENT = new ColorData(0, 0, 0, 0.0);

    public ColorData(int r, int g, int b, double a) {
        this.r = clamp(r, 0, 255);
        this.g = clamp(g, 0, 255);
        this.b = clamp(b, 0, 255);
        this.a = clamp(a, 0.0, 1.0);
    }

    public ColorData(ColorData other) {
        Objects.requireNonNull(other, "Other ColorData cannot be null for copy constructor.");
        this.r = other.r;
        this.g = other.g;
        this.b = other.b;
        this.a = other.a;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public int getR() {
        return r;
    }

    public int getG() {
        return g;
    }

    public int getB() {
        return b;
    }

    public double getA() {
        return a;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColorData colorData = (ColorData) o;
        return r == colorData.r &&
               g == colorData.g &&
               b == colorData.b &&
               Double.compare(colorData.a, a) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(r, g, b, a);
    }

    @Override
    public String toString() {
        return String.format("ColorData{r=%d, g=%d, b=%d, a=%.2f}", r, g, b, a);
    }
}