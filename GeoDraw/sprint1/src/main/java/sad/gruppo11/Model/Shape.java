package sad.gruppo11.Model;

import java.io.Serializable;
import java.util.UUID;

import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Rect;
import sad.gruppo11.Model.geometry.Vector2D;
import sad.gruppo11.View.ShapeVisitor;

/**
 * Interfaccia generale per tutte le forme geometriche.
 * Definisce il comportamento comune tra forme come rettangoli, ellissi e segmenti di linea.
 * Estende {@link Serializable} per consentire la serializzazione.
 */
public interface Shape extends Serializable {

    /**
     * Restituisce l'identificatore univoco della forma.
     *
     * @return UUID univoco della forma.
     */
    UUID getId();

    /**
     * Sposta la forma in base al vettore specificato.
     *
     * @param v Il vettore di spostamento.
     */
    void move(Vector2D v);

    /**
     * Ridimensiona la forma per adattarla ai nuovi limiti rettangolari.
     * L'interpretazione del ridimensionamento dipende dal tipo concreto di forma.
     *
     * @param newBounds I nuovi limiti rettangolari.
     */
    void resize(Rect newBounds);

    /**
     * Imposta il colore del bordo della forma.
     *
     * @param c Il nuovo colore del tratto.
     */
    void setStrokeColor(ColorData c);

    /**
     * Restituisce il colore attuale del bordo della forma.
     *
     * @return Il colore del tratto.
     */
    ColorData getStrokeColor();

    /**
     * Imposta il colore di riempimento della forma.
     *
     * @param c Il nuovo colore di riempimento.
     */
    void setFillColor(ColorData c);

    /**
     * Restituisce il colore di riempimento della forma.
     *
     * @return Il colore di riempimento.
     */
    ColorData getFillColor();

    /**
     * Verifica se un punto specificato è contenuto all'interno della forma.
     *
     * @param p Il punto da verificare.
     * @return true se il punto è contenuto nella forma, false altrimenti.
     */
    boolean contains(Point2D p);

    /**
     * Accetta un visitatore che eseguirà operazioni su questa forma.
     * Parte del pattern Visitor.
     *
     * @param v Il visitatore.
     */
    void accept(ShapeVisitor v);

    /**
     * Crea una copia profonda (clone) della forma, mantenendo lo stesso UUID.
     * Parte del pattern Prototype.
     *
     * @return Una copia identica della forma.
     */
    Shape cloneShape();

    /**
     * Restituisce la bounding box rettangolare della forma.
     *
     * @return I limiti della forma.
     */
    Rect getBounds();

    /**
     * Crea una copia della forma con un nuovo UUID.
     * Utile per operazioni come copia/incolla.
     *
     * @return Una nuova istanza della forma con nuovo identificatore.
     */
    Shape cloneWithNewId();
}
