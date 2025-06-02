
package sad.gruppo11.Controller;

import sad.gruppo11.Model.geometry.Point2D;

public interface ToolState {
    void onMousePressed(GeoEngine engine, Point2D p);
    void onMouseDragged(GeoEngine engine, Point2D p);
    void onMouseReleased(GeoEngine engine, Point2D p);
    String getName();
    void activate(GeoEngine engine);
    void deactivate(GeoEngine engine);
}
