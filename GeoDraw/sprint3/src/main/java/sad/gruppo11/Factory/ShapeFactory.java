
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
            // Aggiungi una soglia minima per la distanza, altrimenti non creare la forma
            if (p1.distance(p2) > 1e-2) { // 0.01, una soglia piccola
                return new LineSegment(p1, p2, strokeColor);
            }
        } else if (("RectangleTool".equalsIgnoreCase(toolName) || "RectangleShape".equalsIgnoreCase(toolName)) && p1 != null && p2 != null) {
            double x = Math.min(p1.getX(), p2.getX());
            double y = Math.min(p1.getY(), p2.getY());
            double width = Math.abs(p1.getX() - p2.getX());
            double height = Math.abs(p1.getY() - p2.getY());
            if (width > 1e-2 && height > 1e-2) { // Soglia minima per larghezza e altezza
                return new RectangleShape(new Rect(new Point2D(x, y), width, height), strokeColor, fillColor);
            }
        } else if (("EllipseTool".equalsIgnoreCase(toolName) || "EllipseShape".equalsIgnoreCase(toolName)) && p1 != null && p2 != null) {
            double x = Math.min(p1.getX(), p2.getX());
            double y = Math.min(p1.getY(), p2.getY());
            double width = Math.abs(p1.getX() - p2.getX());
            double height = Math.abs(p1.getY() - p2.getY());
            if (width > 1e-2 && height > 1e-2) {
                return new EllipseShape(new Rect(new Point2D(x, y), width, height), strokeColor, fillColor);
            }
        } else if (("PolygonTool".equalsIgnoreCase(toolName) || "PolygonShape".equalsIgnoreCase(toolName)) && optionalParams != null) {
            if (optionalParams.containsKey("vertices")) {
                try {
                    List<Point2D> vertices = (List<Point2D>) optionalParams.get("vertices");
                    // PolygonShape constructor già controlla vertices.size() >= 3
                    if (vertices != null) { // Non serve controllare size qui se il costruttore lo fa
                        return new PolygonShape(vertices, strokeColor, fillColor);
                    }
                } catch (ClassCastException e) { 
                    System.err.println("ShapeFactory: Error creating Polygon - 'vertices' parameter has wrong type. " + e.getMessage()); 
                } catch (IllegalArgumentException e) { // Cattura l'eccezione dal costruttore di PolygonShape
                     System.err.println("ShapeFactory: Error creating Polygon - " + e.getMessage());
                }
            }
        } else if (("TextTool".equalsIgnoreCase(toolName) || "TextShape".equalsIgnoreCase(toolName)) && optionalParams != null) {
            Point2D textPosition = p1; // Usa p1 come fallback se "position" non è nei parametri
            if (optionalParams.containsKey("position") && optionalParams.get("position") instanceof Point2D) {
                textPosition = (Point2D) optionalParams.get("position");
            }
            
            if (textPosition != null && 
                optionalParams.containsKey("text") && 
                optionalParams.containsKey("fontSize") && 
                optionalParams.containsKey("fontName")) {
                try {
                    String text = (String) optionalParams.get("text");
                    double fontSize = ((Number) optionalParams.get("fontSize")).doubleValue();
                    String fontName = (String) optionalParams.get("fontName");
                    
                    // Per TextShape, strokeColor è usato come colore del testo. FillColor è ignorato.
                    ColorData textColorToUse = strokeColor; 
                    // Potremmo anche avere un parametro opzionale "textColor" se vogliamo distinguerlo
                    if (optionalParams.containsKey("textColor") && optionalParams.get("textColor") instanceof ColorData){
                        textColorToUse = (ColorData) optionalParams.get("textColor");
                    }

                    if (text != null && !text.isEmpty() && fontSize > 0 && fontName != null && !fontName.isEmpty()) {
                        return new TextShape(text, textPosition, fontSize, fontName, textColorToUse);
                    }
                } catch (ClassCastException | NullPointerException | IllegalArgumentException e) { 
                    System.err.println("ShapeFactory: Error creating TextShape - " + e.getMessage()); 
                }
            }
        }
        // Se nessuna condizione è soddisfatta o si verificano errori nei parametri, non crea la forma.
        // System.err.println("ShapeFactory: Could not create shape for toolName '" + toolName + "' with given parameters or parameters were invalid.");
        return null; // Restituisce null se la forma non può essere creata
    }
    
    // Metodo sovraccaricato senza optionalParams per compatibilità o usi semplici
    public Shape createShape(String toolName, Point2D p1, Point2D p2, 
                             ColorData strokeColor, ColorData fillColor) {
        return createShape(toolName, p1, p2, strokeColor, fillColor, null);
    }
}
