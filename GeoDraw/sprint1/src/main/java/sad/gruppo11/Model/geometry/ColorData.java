package sad.gruppo11.Model.geometry;

import java.io.Serializable;
import java.util.Objects;

/*
 * ColorData rappresenta un colore con componenti RGBA.
 * È immutabile nel senso che non dovrebbe essere esteso (classe final).
 * Utilizzato per serializzazione e confronto di colori nelle forme.
 */
public final class ColorData implements Serializable {

    /* Componente rossa (0-255) */
    private int r;

    /* Componente verde (0-255) */
    private int g;

    /* Componente blu (0-255) */
    private int b;

    /* Componente alpha/opacità (0.0 - 1.0) */
    private double a;

    /* Colori comuni predefiniti */
    public static final ColorData BLACK = new ColorData(0, 0, 0, 1.0);
    public static final ColorData WHITE = new ColorData(255, 255, 255, 1.0);
    public static final ColorData RED = new ColorData(255, 0, 0, 1.0);
    public static final ColorData GREEN = new ColorData(0, 255, 0, 1.0);
    public static final ColorData BLUE = new ColorData(0, 0, 255, 1.0);
    public static final ColorData TRANSPARENT = new ColorData(0, 0, 0, 0.0);
    public static final ColorData YELLOW = new ColorData(255, 255, 0, 1.0);

    /*
     * Costruttore principale con componenti RGBA.
     *
     * @param r componente rossa
     * @param g componente verde
     * @param b componente blu
     * @param a opacità (alpha)
     */
    public ColorData(int r, int g, int b, double a) {
        this.r = clamp(r, 0, 255);
        this.g = clamp(g, 0, 255);
        this.b = clamp(b, 0, 255);
        this.a = clamp(a, 0.0, 1.0);
    }

    /*
     * Costruttore di copia.
     *
     * @param other Istanza da copiare
     */
    public ColorData(ColorData other) {
        this.r = other.r;
        this.g = other.g;
        this.b = other.b;
        this.a = other.a;
    }

    /*
     * Clamping per interi (RGB).
     */
    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    /*
     * Clamping per double (alpha).
     */
    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /* Getter e setter con clamping */

    public int getR() {
        return r;
    }

    public void setR(int r) {
        this.r = clamp(r, 0, 255);
    }

    public int getG() {
        return g;
    }

    public void setG(int g) {
        this.g = clamp(g, 0, 255);
    }

    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = clamp(b, 0, 255);
    }

    public double getA() {
        return a;
    }

    public void setA(double a) {
        this.a = clamp(a, 0.0, 1.0);
    }

    /*
     * Due colori sono uguali se tutti i componenti RGBA coincidono.
     */
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

    /*
     * Genera un hash coerente con equals.
     */
    @Override
    public int hashCode() {
        return Objects.hash(r, g, b, a);
    }

    /*
     * Rappresentazione testuale del colore.
     */
    @Override
    public String toString() {
        return "ColorData{" +
               "r=" + r +
               ", g=" + g +
               ", b=" + b +
               ", a=" + a +
               '}';
    }
}
