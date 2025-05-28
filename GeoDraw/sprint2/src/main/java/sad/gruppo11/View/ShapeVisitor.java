package sad.gruppo11.View;

import sad.gruppo11.Model.EllipseShape;
import sad.gruppo11.Model.LineSegment;
import sad.gruppo11.Model.PolygonShape;
import sad.gruppo11.Model.RectangleShape;
import sad.gruppo11.Model.TextShape;

public interface ShapeVisitor {
    void visit(RectangleShape r);
    void visit(EllipseShape e);
    void visit(LineSegment l);
    void visit(PolygonShape p);
    void visit(TextShape t);
}