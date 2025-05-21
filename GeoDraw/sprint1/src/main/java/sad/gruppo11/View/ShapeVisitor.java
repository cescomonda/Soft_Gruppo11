package sad.gruppo11.View;

import sad.gruppo11.Model.EllipseShape;
import sad.gruppo11.Model.LineSegment;
import sad.gruppo11.Model.RectangleShape;

/**
 * Interfaccia del pattern Visitor per la gerarchia di {@code Shape}.
 * Consente di definire nuove operazioni sulle forme senza modificare le loro classi.
 */
public interface ShapeVisitor {

    /**
     * Visita un {@link RectangleShape}.
     *
     * @param r il rettangolo da elaborare
     */
    void visit(RectangleShape r);

    /**
     * Visita un {@link EllipseShape}.
     *
     * @param e l'ellisse da elaborare
     */
    void visit(EllipseShape e);

    /**
     * Visita un {@link LineSegment}.
     *
     * @param l il segmento di linea da elaborare
     */
    void visit(LineSegment l);

    // Estendere con nuovi metodi visit() per nuove forme, es.:
    // void visit(PolygonShape p);
}
