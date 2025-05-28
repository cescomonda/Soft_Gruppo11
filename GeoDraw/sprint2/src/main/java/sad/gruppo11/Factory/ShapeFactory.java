package sad.gruppo11.Factory;

import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.LineSegment;
import sad.gruppo11.Model.RectangleShape;
import sad.gruppo11.Model.EllipseShape;
import sad.gruppo11.Model.PolygonShape;
import sad.gruppo11.Model.TextShape;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Rect;
import sad.gruppo11.Model.geometry.ColorData;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ShapeFactory {
    @SuppressWarnings("unchecked")
    public Shape createShape(String toolName, Point2D p1, Point2D p2, 
                             ColorData strokeColor, ColorData fillColor, 
                             Map<String, Object> optionalParams) {
        Objects.requireNonNull(toolName, "Tool name cannot be null for shape creation.");
        Objects.requireNonNull(strokeColor, "Stroke color cannot be null for shape creation.");
        Objects.requireNonNull(fillColor, "Fill color cannot be null for shape creation.");

        if (("LineTool".equalsIgnoreCase(toolName) || "LineSegment".equalsIgnoreCase(toolName)) && p1 != null && p2 != null) {
            if (p1.distance(p2) > 1e-2) return new LineSegment(p1, p2, strokeColor);
        } else if (("RectangleTool".equalsIgnoreCase(toolName) || "RectangleShape".equalsIgnoreCase(toolName)) && p1 != null && p2 != null) {
            double x = Math.min(p1.getX(), p2.getX());
            double y = Math.min(p1.getY(), p2.getY());
            double width = Math.abs(p1.getX() - p2.getX());
            double height = Math.abs(p1.getY() - p2.getY());
            if (width > 1e-2 && height > 1e-2) return new RectangleShape(new Rect(new Point2D(x, y), width, height), strokeColor, fillColor);
        } else if (("EllipseTool".equalsIgnoreCase(toolName) || "EllipseShape".equalsIgnoreCase(toolName)) && p1 != null && p2 != null) {
            double x = Math.min(p1.getX(), p2.getX());
            double y = Math.min(p1.getY(), p2.getY());
            double width = Math.abs(p1.getX() - p2.getX());
            double height = Math.abs(p1.getY() - p2.getY());
            if (width > 1e-2 && height > 1e-2) return new EllipseShape(new Rect(new Point2D(x, y), width, height), strokeColor, fillColor);
        } else if (("PolygonTool".equalsIgnoreCase(toolName) || "PolygonShape".equalsIgnoreCase(toolName)) && optionalParams != null) {
            if (optionalParams.containsKey("vertices")) {
                try {
                    List<Point2D> vertices = (List<Point2D>) optionalParams.get("vertices");
                    if (vertices != null && vertices.size() >= 3) return new PolygonShape(vertices, strokeColor, fillColor);
                } catch (ClassCastException e) { System.err.println("Error creating Polygon: 'vertices' param has wrong type."); }
            }
        } else if (("TextTool".equalsIgnoreCase(toolName) || "TextShape".equalsIgnoreCase(toolName)) && optionalParams != null) {
            Point2D textPosition = p1; 
            if (optionalParams.containsKey("position") && optionalParams.get("position") instanceof Point2D) {
                textPosition = (Point2D) optionalParams.get("position");
            }
            if (textPosition != null && optionalParams.containsKey("text") && optionalParams.containsKey("fontSize") && optionalParams.containsKey("fontName")) {
                try {
                    String text = (String) optionalParams.get("text");
                    double fontSize = ((Number) optionalParams.get("fontSize")).doubleValue();
                    String fontName = (String) optionalParams.get("fontName");
                    ColorData textColor = strokeColor; 
                    if (optionalParams.containsKey("textColor") && optionalParams.get("textColor") instanceof ColorData){
                        textColor = (ColorData) optionalParams.get("textColor");
                    }
                    if (text != null && !text.isEmpty() && fontSize > 0 && fontName != null && !fontName.isEmpty()) {
                        return new TextShape(text, textPosition, fontSize, fontName, textColor);
                    }
                } catch (ClassCastException | NullPointerException e) { System.err.println("Error creating Text: " + e.getMessage()); }
            }
        }
        System.err.println("ShapeFactory: Could not create shape for toolName '" + toolName + "' with given parameters.");
        return null;
    }
    
    public Shape createShape(String toolName, Point2D p1, Point2D p2, 
                             ColorData strokeColor, ColorData fillColor) {
        return createShape(toolName, p1, p2, strokeColor, fillColor, null);
    }
}