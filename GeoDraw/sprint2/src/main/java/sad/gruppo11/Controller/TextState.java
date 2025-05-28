package sad.gruppo11.Controller;

import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Factory.ShapeFactory;
import sad.gruppo11.Infrastructure.CommandManager;
import sad.gruppo11.Infrastructure.AddShapeCommand;
import sad.gruppo11.View.DrawingView;

import java.util.Map;
import java.util.HashMap;
import java.util.Objects;

public class TextState implements ToolState {
    @Override
    public String getName() { return "TextTool"; }

    @Override
    public void activate(GeoEngine engine) { 
        Objects.requireNonNull(engine);
        DrawingView view = engine.getView();
        if (view == null) {
            System.err.println("TextState: DrawingView not available.");
            return;
        }
        
    }

    @Override
    public void deactivate(GeoEngine engine) { 
        Objects.requireNonNull(engine);
        DrawingView view = engine.getView();
        if (view == null) {
            System.err.println("TextState: DrawingView not available.");
            return;
        }
        // Optionally reset any state or UI elements related to text input
    }

    @Override
    public void onMousePressed(GeoEngine engine, Point2D p) {
        Objects.requireNonNull(engine); Objects.requireNonNull(p);
        DrawingView view = engine.getView();
        if (view == null) { System.err.println("TextState: DrawingView not available."); return; }
        String inputText = view.promptForText("Enter text:");
        if (inputText != null && !inputText.trim().isEmpty()) {
            ColorData textColor = engine.getCurrentStrokeColorForNewShapes();
            Map<String, Object> params = new HashMap<>();
            params.put("text", inputText);
            params.put("fontSize", engine.getCurrentDefaultFontSize());
            params.put("fontName", engine.getCurrentDefaultFontName());
            params.put("position", p);
            Shape newTextShape = engine.getShapeFactory().createShape(getName(), null, null, textColor, ColorData.TRANSPARENT, params);
            if (newTextShape != null) {
                engine.getCommandManager().executeCommand(new AddShapeCommand(engine.getDrawing(), newTextShape));
            }
        }
    }


    
    @Override
    public void onMouseDragged(GeoEngine engine, Point2D p) { /* No-op */ }

    @Override
    public void onMouseReleased(GeoEngine engine, Point2D p) { /* No-op */ }
}