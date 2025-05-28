package sad.gruppo11.Controller;

import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.TextShape; // For specific text operations
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Vector2D;
import sad.gruppo11.Model.geometry.Rect;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Infrastructure.CommandManager;
import sad.gruppo11.Infrastructure.Clipboard;
import sad.gruppo11.Infrastructure.Command;
import sad.gruppo11.Infrastructure.AddShapeCommand;
import sad.gruppo11.Infrastructure.DeleteShapeCommand;
import sad.gruppo11.Infrastructure.MoveShapeCommand;
import sad.gruppo11.Infrastructure.ResizeShapeCommand;
import sad.gruppo11.Infrastructure.ChangeStrokeColorCommand;
import sad.gruppo11.Infrastructure.ChangeTextContentCommand;
import sad.gruppo11.Infrastructure.ChangeFillColorCommand;
import sad.gruppo11.Infrastructure.CutShapeCommand;
import sad.gruppo11.Infrastructure.CopyShapeCommand;
import sad.gruppo11.Infrastructure.PasteShapeCommand;
import sad.gruppo11.Infrastructure.BringToFrontCommand;
import sad.gruppo11.Infrastructure.SendToBackCommand;
import sad.gruppo11.Infrastructure.RotateShapeCommand;
import sad.gruppo11.Infrastructure.ChangeTextSizeCommand;
import sad.gruppo11.Persistence.PersistenceController;
import sad.gruppo11.Factory.ShapeFactory;
import sad.gruppo11.View.DrawingView;
import sad.gruppo11.Model.Observable; // For GeoEngine to be Observable
import sad.gruppo11.View.Observer;   // For GeoEngine to be Observable


import java.util.Map;
import java.util.HashMap;
import java.util.Objects;
import java.util.List; // For observer list
import java.util.ArrayList; // For observer list
import java.util.concurrent.CopyOnWriteArrayList; // For observer list


public class GeoEngine implements Observable { // GeoEngine itself can be Observable for UI properties
    private ToolState currentState;
    private Drawing drawing;
    private final CommandManager cmdMgr;
    private final PersistenceController persistenceController;
    private final Clipboard clipboard;
    private final ShapeFactory shapeFactory;
    private DrawingView view; // Main drawing view
    private Shape selectedShape;

    // Default properties for new shapes & UI state
    private ColorData currentStrokeColorForNewShapes = ColorData.BLACK;
    private ColorData currentFillColorForNewShapes = ColorData.TRANSPARENT;
    private String currentDefaultFontName = "Arial";
    private double currentDefaultFontSize = 12.0;

    // Zoom, Pan, Grid properties (source of truth)
    private double currentZoom = 1.0;
    private double scrollOffsetX = 0.0;
    private double scrollOffsetY = 0.0;
    private boolean gridEnabled = false;
    private double gridSize = 20.0;

    // Tool state instances
    private final Map<String, ToolState> toolStates = new HashMap<>();
    
    // Observers for GeoEngine properties (like zoom/grid changes)
    private transient List<Observer> geoEngineObservers = new CopyOnWriteArrayList<>();


    public GeoEngine(Drawing drawing, CommandManager cmdMgr, 
                     PersistenceController persistenceController, 
                     Clipboard clipboard, ShapeFactory factory) {
        Objects.requireNonNull(drawing); Objects.requireNonNull(cmdMgr); 
        Objects.requireNonNull(persistenceController); Objects.requireNonNull(clipboard); Objects.requireNonNull(factory);

        this.drawing = drawing;
        this.cmdMgr = cmdMgr;
        // Pass drawing model to command manager if it needs to notify it
        this.cmdMgr.setDrawingModel(this.drawing);

        this.persistenceController = persistenceController;
        this.clipboard = clipboard;
        this.shapeFactory = factory;
        
        // Initialize tool states
        toolStates.put("LineTool", new LineState());
        toolStates.put("RectangleTool", new RectangleState());
        toolStates.put("EllipseTool", new EllipseState());
        toolStates.put("SelectTool", new SelectState());
        toolStates.put("PolygonTool", new PolygonState());
        toolStates.put("TextTool", new TextState());

        setState("SelectTool"); // Default state
    }

    public void setView(DrawingView view) {
        Objects.requireNonNull(view, "DrawingView cannot be null.");
        this.view = view;
        // The view should register itself as an observer to the Drawing model.
        // GeoEngine makes the model available; view attachment is view's responsibility or MainApp.
        if (this.view != null && this.drawing != null) {
            // If view needs explicit model set, do it here or ensure constructor does it
            // this.view.setDrawingModel(this.drawing); // Or DrawingView gets it from controller.getDrawing()
        }
        // Initial notification for view to get current state of zoom/pan/grid
        notifyGeoEngineObservers(new Drawing.DrawingChangeEvent(Drawing.DrawingChangeEvent.ChangeType.TRANSFORM));

    }
    public DrawingView getView() { return this.view; }


    public void setState(String stateName) {
        ToolState newState = toolStates.get(stateName);
        Objects.requireNonNull(newState, "Unknown tool state name: " + stateName);

        if (this.currentState != newState) {
            if (this.currentState != null) {
                this.currentState.deactivate(this);
            }
            this.currentState = newState;
            this.currentState.activate(this);
            
            if (view != null) view.showUserMessage("Active Tool: " + getCurrentToolName());
            // Notify UI components (e.g. MainApp to update tool buttons)
            notifyGeoEngineObservers(this.currentState.getName()); // Pass tool name as arg
        }
    }

    public String getCurrentToolName() {
        return (this.currentState != null) ? this.currentState.getName() : "None";
    }

    public void onMousePressed(Point2D p) { if (currentState != null) currentState.onMousePressed(this, p); }
    public void onMouseDragged(Point2D p) { if (currentState != null) currentState.onMouseDragged(this, p); }
    public void onMouseReleased(Point2D p) { if (currentState != null) currentState.onMouseReleased(this, p); }
    
    // Specific actions for tools that require it (e.g. PolygonTool finishing on double-click/key)
    public void attemptToolFinishAction() {
        if (currentState instanceof PolygonState) {
            ((PolygonState)currentState).tryFinishPolygonOnAction(this);
        }
    }


    // --- Shape Manipulation Operations ---
    public void addShapeToDrawing(Shape shape) { // Typically called by ToolStates via factory & AddShapeCommand
        Objects.requireNonNull(shape, "Shape to add cannot be null.");
        cmdMgr.executeCommand(new AddShapeCommand(this.drawing, shape));
    }

    public void removeSelectedShapeFromDrawing() {
        if (this.selectedShape != null) {
            cmdMgr.executeCommand(new DeleteShapeCommand(this.drawing, this.selectedShape));
            setSelectedShape(null); // Deselect after removal
        }
    }

    public void moveSelectedShape(Vector2D delta) {
        if (this.selectedShape != null && delta != null) {
            cmdMgr.executeCommand(new MoveShapeCommand(this.selectedShape, delta));
        }
    }
    
    public void resizeSelectedShape(Rect newBounds) {
        if (this.selectedShape != null && newBounds != null) {
            cmdMgr.executeCommand(new ResizeShapeCommand(this.selectedShape, newBounds));
        }
    }

    public void changeSelectedShapeStrokeColor(ColorData color) {
        if (this.selectedShape != null && color != null) {
            cmdMgr.executeCommand(new ChangeStrokeColorCommand(this.selectedShape, color));
        }
    }

    public void changeSelectedShapeFillColor(ColorData color) {
        if (this.selectedShape != null && color != null) {
            cmdMgr.executeCommand(new ChangeFillColorCommand(this.selectedShape, color));
        }
    }
    
    public void cutSelectedShape() {
        if (this.selectedShape != null) {
            cmdMgr.executeCommand(new CutShapeCommand(this.selectedShape, this.drawing, this.clipboard));
            setSelectedShape(null);
        }
    }

    public void copySelectedShape() {
        if (this.selectedShape != null) {
            // Copy command doesn't modify model, so it could be executed directly or through cmdMgr
            // For consistency, using cmdMgr, though its undo might be no-op.
            cmdMgr.executeCommand(new CopyShapeCommand(this.selectedShape, this.clipboard));
        }
    }

    public void pasteShape() {
        Vector2D defaultOffset = new Vector2D(10, 10);
        if (!this.clipboard.isEmpty()) {
            cmdMgr.executeCommand(new PasteShapeCommand(this.drawing, this.clipboard, defaultOffset));
        }
    }
    public void pasteShape(Vector2D offset) { // Overload if specific offset is needed
         if (!this.clipboard.isEmpty() && offset != null) {
            cmdMgr.executeCommand(new PasteShapeCommand(this.drawing, this.clipboard, offset));
        }
    }


    // --- Z-Order Operations ---
    public void bringSelectedShapeToFront() {
        if (this.selectedShape != null) {
            cmdMgr.executeCommand(new BringToFrontCommand(this.drawing, this.selectedShape));
        }
    }
    public void sendSelectedShapeToBack() {
        if (this.selectedShape != null) {
            cmdMgr.executeCommand(new SendToBackCommand(this.drawing, this.selectedShape));
        }
    }

    // --- Rotation & Text Operations ---
    public void rotateSelectedShape(double angleDegrees) { // Absolute angle or delta? Diagram implies delta.
        if (this.selectedShape != null) {
            // Assuming angleDegrees is the *target* absolute angle for simplicity with UI.
            // If it's a delta: new RotateShapeCommand(this.selectedShape, angleDegrees)
            // If it's absolute:
            cmdMgr.executeCommand(new RotateShapeCommand(this.selectedShape, angleDegrees));
        }
    }
    public void changeSelectedTextSize(double newSize) {
        if (this.selectedShape instanceof TextShape && newSize > 0) {
            cmdMgr.executeCommand(new ChangeTextSizeCommand(this.selectedShape, newSize));
        }
    }

    public void changeSelectedTextContent(String newText) {
        if (this.selectedShape instanceof TextShape && newText != null && !newText.isEmpty()) {
            cmdMgr.executeCommand(new ChangeTextContentCommand(this.selectedShape, newText));
        }
    }

    // --- Undo/Redo ---
    public void undoLastCommand() { cmdMgr.undo(); this.setSelectedShape(null);}
    public void redoLastCommand() { cmdMgr.redo(); this.setSelectedShape(null); }
    public boolean canUndo() { return cmdMgr.canUndo(); }
    public boolean canRedo() { return cmdMgr.canRedo(); }

    // --- Persistence ---
    public void saveDrawing(String path) throws Exception {
        persistenceController.saveDrawing(this.drawing, path);
    }
    public void loadDrawing(String path) throws Exception {
        Drawing loadedDrawing = persistenceController.loadDrawing(path);
        if (loadedDrawing != null) {
            this.drawing.clear(); // Clear current shapes (notifies observers)
            // Add shapes from loaded drawing to current drawing instance
            // This ensures observers of the *current* drawing instance are updated.
            for(Shape s : loadedDrawing.getShapesInZOrder()){
                this.drawing.addShape(s.cloneWithNewId()); // Add clones with new IDs to avoid issues
            }
            // OR replace the drawing instance itself and re-notify view.
            // this.drawing = loadedDrawing; // Simpler, but view needs to re-attach.
            // if(view!=null) view.setDrawingModel(this.drawing);
            
            cmdMgr.clearStacks();
            setSelectedShape(null);
            this.drawing.notifyObservers(new Drawing.DrawingChangeEvent(Drawing.DrawingChangeEvent.ChangeType.LOAD));
        }
    }
    public void createNewDrawing() {
        this.drawing.clear(); // This will notify observers
        cmdMgr.clearStacks();
        setSelectedShape(this.selectedShape);
        // Reset zoom/pan/grid to defaults
        setZoomLevel(this.currentZoom);
        setScrollOffset(this.scrollOffsetX,this.scrollOffsetY); 
        setGridEnabled(this.gridEnabled);
        // Notify about transform change due to reset
        notifyGeoEngineObservers(new Drawing.DrawingChangeEvent(Drawing.DrawingChangeEvent.ChangeType.TRANSFORM));
    }


    // --- Zoom, Pan, Grid ---
    public final double MIN_ZOOM = 1.7; // Minimum zoom level
    public final double MAX_ZOOM = 10.0; // Maximum zoom level
    public void setZoomLevel(double level) {
        if (level < MIN_ZOOM)
            level = MIN_ZOOM; // Clamp to minimum zoom
        else if (level > MAX_ZOOM)
            level = MAX_ZOOM; // Clamp to maximum zoom

        this.currentZoom = level; // Clamp zoom
        notifyGeoEngineObservers(new Drawing.DrawingChangeEvent(Drawing.DrawingChangeEvent.ChangeType.TRANSFORM));
    }
    public void zoomIn(double centerX, double centerY) { // CenterX, CenterY are screen coordinates
        double oldZoom = this.currentZoom;
        double newZoom = Math.min(this.currentZoom * 1.2, MAX_ZOOM); // Zoom in by 20%, max 10x

        // Adjust scrollOffset to keep point (centerX, centerY) fixed on screen
        this.scrollOffsetX = centerX - (centerX - this.scrollOffsetX) * (newZoom / oldZoom);
        this.scrollOffsetY = centerY - (centerY - this.scrollOffsetY) * (newZoom / oldZoom);
        this.currentZoom = newZoom;
        notifyGeoEngineObservers(new Drawing.DrawingChangeEvent(Drawing.DrawingChangeEvent.ChangeType.TRANSFORM));
    }
    public void zoomOut(double centerX, double centerY) {
        double oldZoom = this.currentZoom;
        double newZoom = Math.max(this.currentZoom / 1.2, MIN_ZOOM); // Zoom out by 20%, min 0.1x
        this.scrollOffsetX = centerX - (centerX - this.scrollOffsetX) * (newZoom / oldZoom);
        this.scrollOffsetY = centerY - (centerY - this.scrollOffsetY) * (newZoom / oldZoom);
        this.currentZoom = newZoom;
        notifyGeoEngineObservers(new Drawing.DrawingChangeEvent(Drawing.DrawingChangeEvent.ChangeType.TRANSFORM));
    }
    public void setScrollOffset(double offsetX, double offsetY) {
        this.scrollOffsetX = offsetX; this.scrollOffsetY = offsetY;
        notifyGeoEngineObservers(new Drawing.DrawingChangeEvent(Drawing.DrawingChangeEvent.ChangeType.TRANSFORM));
    }
    public void scroll(double deltaX, double deltaY) { // Delta in screen coordinates
        this.scrollOffsetX += deltaX; this.scrollOffsetY += deltaY;
        notifyGeoEngineObservers(new Drawing.DrawingChangeEvent(Drawing.DrawingChangeEvent.ChangeType.TRANSFORM));
    }
    public void toggleGrid() {
        this.gridEnabled = !this.gridEnabled;
        notifyGeoEngineObservers(new Drawing.DrawingChangeEvent(Drawing.DrawingChangeEvent.ChangeType.GRID));
    }
    public void setGridSize(double size) {
        if (size <= 0) {
            this.gridSize = 20.0; // Reset to default if invalid size
            return;
        }
        if (size > 0) this.gridSize = size;
        if (size < 0.5) this.gridSize = 0.5; // Minimum grid size
        notifyGeoEngineObservers(new Drawing.DrawingChangeEvent(Drawing.DrawingChangeEvent.ChangeType.GRID));
    }
    public void setGridEnabled(boolean enabled) { // Added setter for completeness
        this.gridEnabled = enabled;
        notifyGeoEngineObservers(new Drawing.DrawingChangeEvent(Drawing.DrawingChangeEvent.ChangeType.GRID));
    }

    // --- Getters for UI state ---
    public double getCurrentZoom() { return this.currentZoom; }
    public double getScrollOffsetX() { return this.scrollOffsetX; }
    public double getScrollOffsetY() { return this.scrollOffsetY; }
    public boolean isGridEnabled() { return this.gridEnabled; }
    public double getGridSize() { return this.gridSize; }
    
    public Drawing getDrawing() { return this.drawing; }
    public Clipboard getClipboard() { return this.clipboard; }
    public ShapeFactory getShapeFactory() { return this.shapeFactory; }
    public CommandManager getCommandManager() { return this.cmdMgr; }
    
    public Shape getSelectedShape() { return this.selectedShape; }
    public void setSelectedShape(Shape shape) { // Parameter can be null for deselection
        if (this.selectedShape != shape) {
            this.selectedShape = shape;
            // Notify UI elements interested in selection change (e.g. property panel)
            // This can be done via GeoEngine being Observable or specific listeners.
            notifyGeoEngineObservers(shape); // Pass selected shape or null
            if (view != null) view.render(); // Ask view to re-render to show selection change
        }
    }

    // Default colors/fonts for new shapes
    public ColorData getCurrentStrokeColorForNewShapes() { return new ColorData(currentStrokeColorForNewShapes); }
    public void setCurrentStrokeColorForNewShapes(ColorData color) { this.currentStrokeColorForNewShapes = new ColorData(Objects.requireNonNull(color)); }
    public ColorData getCurrentFillColorForNewShapes() { return new ColorData(currentFillColorForNewShapes); }
    public void setCurrentFillColorForNewShapes(ColorData color) { this.currentFillColorForNewShapes = new ColorData(Objects.requireNonNull(color)); }
    public String getCurrentDefaultFontName() { return currentDefaultFontName; }
    public void setCurrentDefaultFontName(String fontName) { this.currentDefaultFontName = Objects.requireNonNull(fontName); }
    public double getCurrentDefaultFontSize() { return currentDefaultFontSize; }
    public void setCurrentDefaultFontSize(double fontSize) { if(fontSize > 0) this.currentDefaultFontSize = fontSize; }

    public void notifyViewToRefresh(){ // General purpose refresh
        if(view != null) {
            // Re-send transform info, then render
            notifyGeoEngineObservers(new Drawing.DrawingChangeEvent(Drawing.DrawingChangeEvent.ChangeType.TRANSFORM));
            view.render();
        }
    }
    
    // --- Observable implementation for GeoEngine ---
    @Override
    public void attach(Observer o) {
        if (!geoEngineObservers.contains(o)) geoEngineObservers.add(o);
    }
    @Override
    public void detach(Observer o) {
        geoEngineObservers.remove(o);
    }
    @Override
    public void notifyObservers(Object arg) { // Renamed to notifyGeoEngineObservers to avoid conflict
        notifyGeoEngineObservers(arg);
    }
    public void notifyGeoEngineObservers(Object arg) {
        for (Observer obs : geoEngineObservers) {
            obs.update(this, arg);
        }
        // Also, if the view is not a direct GeoEngine observer but needs update:
        if (view != null && !geoEngineObservers.contains(view)) { // Avoid double update if view is direct observer
            view.update(this, arg); // The view will decide if this arg is relevant
        }
    }
}