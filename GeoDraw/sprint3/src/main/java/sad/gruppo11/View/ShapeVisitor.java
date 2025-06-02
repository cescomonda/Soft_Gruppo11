
package sad.gruppo11.View;

import sad.gruppo11.Model.EllipseShape;
import sad.gruppo11.Model.LineSegment;
import sad.gruppo11.Model.PolygonShape;
import sad.gruppo11.Model.RectangleShape;
import sad.gruppo11.Model.TextShape;
import sad.gruppo11.Model.GroupShape; // Aggiunto per Sprint 3

public interface ShapeVisitor {
    void visit(RectangleShape r);
    void visit(EllipseShape e);
    void visit(LineSegment l);
    void visit(PolygonShape p);
    void visit(TextShape t);
    void visit(GroupShape g); // Aggiunto per Sprint 3
}
