
package sad.gruppo11.Controller;

import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.ColorData;
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
        engine.clearSelection();
        DrawingView view = engine.getView();
        if (view != null) {
            view.showUserMessage("Text Tool: Click on canvas to place text.");
        }
    }

    @Override
    public void deactivate(GeoEngine engine) { 
        Objects.requireNonNull(engine);
        DrawingView view = engine.getView();
        if (view != null) {
             view.clearUserMessage();
        }
    }

    @Override
    public void onMousePressed(GeoEngine engine, Point2D p) {
        Objects.requireNonNull(engine); Objects.requireNonNull(p);
        DrawingView view = engine.getView();
        if (view == null) { System.err.println("TextState: DrawingView not available."); return; }
        
        String inputText = view.promptForText("Enter text:", ""); // Chiede il testo all'utente
        
        if (inputText != null && !inputText.isEmpty()) { // Solo se l'utente inserisce del testo
            ColorData textColor = engine.getCurrentStrokeColorForNewShapes(); // Il testo usa lo stroke color
            Map<String, Object> params = new HashMap<>();
            params.put("text", inputText);
            params.put("fontSize", engine.getCurrentDefaultFontSize());
            params.put("fontName", engine.getCurrentDefaultFontName());
            // 'p' è la posizione del click, che TextShape userà come topLeft dei suoi bounds iniziali stimati
            params.put("position", p); 
            // Non servono p1 e p2 per createShape di TextTool se passiamo la posizione nei params
            Shape newTextShape = engine.getShapeFactory().createShape(getName(), null, null, textColor, ColorData.TRANSPARENT, params);
            
            if (newTextShape != null) {
                engine.addShapeToDrawing(newTextShape); // Usa AddShapeCommand
                if (view != null) view.showUserMessage("Text added.");
            }
        } else {
            if (view != null) view.showUserMessage("Text input cancelled or empty.");
        }
    }
    
    @Override
    public void onMouseDragged(GeoEngine engine, Point2D p) { /* No-op per TextState */ }

    @Override
    public void onMouseReleased(GeoEngine engine, Point2D p) { /* No-op per TextState */ }
}
