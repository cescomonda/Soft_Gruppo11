
package sad.gruppo11.Controller;

import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.GroupShape; // Necessario per instanceof e ungroup
import sad.gruppo11.Model.TextShape; 
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
// Nuovi comandi per Sprint 3
import sad.gruppo11.Infrastructure.ReflectHorizontalCommand;
import sad.gruppo11.Infrastructure.ReflectVerticalCommand;
import sad.gruppo11.Infrastructure.GroupShapesCommand;
import sad.gruppo11.Infrastructure.UngroupShapeCommand;

import sad.gruppo11.Persistence.PersistenceController;
// Rimosso IReusableShapeLibrarySerializer e ReusableShapeLibrarySerializer da qui,
// verranno gestiti da PersistenceController
import sad.gruppo11.Factory.ShapeFactory;
import sad.gruppo11.View.DrawingView;
import sad.gruppo11.Model.Observable;
import sad.gruppo11.View.Observer;


import java.util.Map;
import java.util.HashMap;
import java.util.Objects;
import java.util.List;
import java.io.IOException;
import java.util.ArrayList; 
import java.util.Collection; // Per getReusableShapeDefinitions
import java.util.Collections; // Per Collections.emptyList()
import java.util.concurrent.CopyOnWriteArrayList;

import javafx.scene.Group; 
import java.util.stream.Collectors; 


public class GeoEngine implements Observable {
    private ToolState currentState;
    private Drawing drawing;
    private final CommandManager cmdMgr;
    private final PersistenceController persistenceController;
    private final Clipboard clipboard;
    private final ShapeFactory shapeFactory;
    private DrawingView view; 
    
    // Modificato per selezione multipla
    private List<Shape> selectedShapes = new ArrayList<>();

    private ColorData currentStrokeColorForNewShapes = ColorData.BLACK;
    private ColorData currentFillColorForNewShapes = ColorData.TRANSPARENT;
    private String currentDefaultFontName = "Arial";
    private double currentDefaultFontSize = 12.0;

    private double currentZoom = 1.0;
    private double scrollOffsetX = 0.0;
    private double scrollOffsetY = 0.0;
    private boolean gridEnabled = false;
    private double gridSize = 20.0;

    private boolean isShiftKeyPressed = false;

    private final Map<String, ToolState> toolStates = new HashMap<>();
    private transient List<Observer> geoEngineObservers = new CopyOnWriteArrayList<>();

    // Nuovi campi per Sprint 3 (Forme Riutilizzabili)
    private final ReusableShapeLibrary reusableShapeLibrary;


    public GeoEngine(Drawing drawing, CommandManager cmdMgr, 
                     PersistenceController persistenceController, 
                     Clipboard clipboard, ShapeFactory factory) {
        Objects.requireNonNull(drawing); Objects.requireNonNull(cmdMgr); 
        Objects.requireNonNull(persistenceController); Objects.requireNonNull(clipboard); Objects.requireNonNull(factory);

        this.drawing = drawing;
        this.cmdMgr = cmdMgr;
        this.cmdMgr.setDrawingModel(this.drawing);

        this.persistenceController = persistenceController;
        this.clipboard = clipboard;
        this.shapeFactory = factory;
        
        this.reusableShapeLibrary = new ReusableShapeLibrary(); // Inizializza la libreria
        
        toolStates.put("LineTool", new LineState());
        toolStates.put("RectangleTool", new RectangleState());
        toolStates.put("EllipseTool", new EllipseState());
        toolStates.put("SelectTool", new SelectState(this.drawing));
        toolStates.put("PolygonTool", new PolygonState());
        toolStates.put("TextTool", new TextState());
        // Potrebbe esserci un "PlaceReusableShapeState" se l'inserimento è un tool a parte

        setState("SelectTool"); 
    }

    public void setView(DrawingView view) {
        Objects.requireNonNull(view, "DrawingView cannot be null.");
        this.view = view;
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
            clearSelection(); // Deseleziona tutto quando si cambia tool (tranne forse da Select a Select)
            this.currentState = newState;
            this.currentState.activate(this);
            notifyGeoEngineObservers(new Drawing.DrawingChangeEvent(Drawing.DrawingChangeEvent.ChangeType.SELECTION)); // Notifica cambio selezione (a vuoto)
            notifyGeoEngineObservers("ToolChanged:" + this.currentState.getName());
        }
    }

    public String getCurrentToolName() {
        return (this.currentState != null) ? this.currentState.getName() : "None";
    }

    public void onMousePressed(Point2D p) { if (currentState != null) currentState.onMousePressed(this, p); }
    public void onMouseDragged(Point2D p) { if (currentState != null) currentState.onMouseDragged(this, p); }
    public void onMouseReleased(Point2D p) { if (currentState != null) currentState.onMouseReleased(this, p); }
    
    public void attemptToolFinishAction() {
        if (currentState instanceof PolygonState) {
            ((PolygonState)currentState).tryFinishPolygonOnAction(this);
        }
    }

    // --- Gestione Selezione (Modificata per Lista) ---
    public List<Shape> getSelectedShapes() {
        return Collections.unmodifiableList(new ArrayList<>(selectedShapes));
    }

    public Shape getSelectedShape() { // Restituisce la prima forma selezionata, o null
        return selectedShapes.isEmpty() ? null : selectedShapes.get(0);
    }
    
    public void setSelectedShapes(List<Shape> shapes) {
        this.selectedShapes.clear();
        if (shapes != null) {
            this.selectedShapes.addAll(shapes);
        }
        notifyGeoEngineObservers(new Drawing.DrawingChangeEvent(Drawing.DrawingChangeEvent.ChangeType.SELECTION));
        if (view != null) view.render();
    }

    public void clearSelection() {
        if (!selectedShapes.isEmpty()) {
            selectedShapes.clear();
            notifyGeoEngineObservers(new Drawing.DrawingChangeEvent(Drawing.DrawingChangeEvent.ChangeType.SELECTION));
            if (view != null) view.render();
        }
    }

    public void addShapeToSelection(Shape shape) {
        Objects.requireNonNull(shape);
        if (!selectedShapes.contains(shape)) {
            selectedShapes.add(shape);
            notifyGeoEngineObservers(new Drawing.DrawingChangeEvent(Drawing.DrawingChangeEvent.ChangeType.SELECTION));
            if (view != null) view.render();
        }
    }
     public void setSingleSelectedShape(Shape shape) { // Helper per quando si vuole selezionare una singola forma
        this.selectedShapes.clear();
        if (shape != null) {
            this.selectedShapes.add(shape);
        }
        notifyGeoEngineObservers(new Drawing.DrawingChangeEvent(Drawing.DrawingChangeEvent.ChangeType.SELECTION));
        if (view != null) view.render();
    }


    // --- Operazioni sulle Forme (adattate per selezione multipla o singola dove appropriato) ---
    public void addShapeToDrawing(Shape shape) { 
        Objects.requireNonNull(shape, "Shape to add cannot be null.");
        cmdMgr.executeCommand(new AddShapeCommand(this.getDrawing(), shape));
        // Opzionale: selezionare la forma appena aggiunta
        // setSingleSelectedShape(shape); 
    }

    public void removeSelectedShapesFromDrawing() {
        if (!this.selectedShapes.isEmpty()) {
            // Per l'undo/redo, è meglio un comando per ogni forma o un comando composito?
            // Per ora, un comando per forma se sono molte, o un comando batch se fattibile.
            // Semplificazione: creiamo un comando di cancellazione per ogni forma selezionata.
            // Bisogna fare attenzione se un comando di cancellazione cambia gli indici degli altri.
            // Soluzione migliore: un comando che prende una lista di forme da cancellare.
            // Per ora, lo facciamo iterando (potrebbe essere migliorato con un BatchDeleteCommand).
            List<Shape> shapesToRemove = new ArrayList<>(this.selectedShapes); // Copia per evitare ConcurrentModificationException
            clearSelection(); // Deseleziona prima di rimuovere
            for(Shape shape : shapesToRemove) {
                cmdMgr.executeCommand(new DeleteShapeCommand(this.getDrawing(), shape));
            }
        }
    }
    
    public void removeSelectedShapeFromDrawing() { // Mantenuto per compatibilità se si vuole cancellare solo il "primario"
        if (getSelectedShape() != null) {
            Shape shapeToRemove = getSelectedShape();
            clearSelection(); // O rimuovi solo questo dalla selezione
            cmdMgr.executeCommand(new DeleteShapeCommand(this.getDrawing(), shapeToRemove));
        }
    }


    public void moveSelectedShapes(Vector2D delta) {
        if (!this.selectedShapes.isEmpty() && delta != null) {
            // Applica un MoveShapeCommand a ogni forma selezionata.
            // Se sono raggruppate logicamente in un GroupShape e GroupShape è selezionato,
            // il MoveShapeCommand su GroupShape sposterà tutti i suoi figli.
            for (Shape shape : this.selectedShapes) {
                cmdMgr.executeCommand(new MoveShapeCommand(this.getDrawing(), shape, delta));
            }
        }
    }

    public void setIsShiftKeyPressed(boolean newState)
    {
        isShiftKeyPressed = newState;
    }
    
    public boolean isShiftKeyPressed()
    {
        return isShiftKeyPressed;
    }
    
    // Resize, Rotate, ColorChange di solito si applicano a una singola forma selezionata (la "primaria")
    // o richiedono UI più complessa per selezioni multiple.
    // Per ora, operano sulla prima forma selezionata.
    public void resizeSelectedShape(Rect newBounds) {
        if (getSelectedShape() != null && newBounds != null) {
            cmdMgr.executeCommand(new ResizeShapeCommand(this.getDrawing(), getSelectedShape(), newBounds));
        }
    }

    public void changeSelectedShapeStrokeColor(ColorData color) {
        if (getSelectedShape() != null && color != null) {
            cmdMgr.executeCommand(new ChangeStrokeColorCommand(this.getDrawing(), getSelectedShape(), color));
        }
    }

    public void changeSelectedShapeFillColor(ColorData color) {
        if (getSelectedShape() != null && color != null) {
            cmdMgr.executeCommand(new ChangeFillColorCommand(this.getDrawing(), getSelectedShape(), color));
        }
    }
    
    public void cutSelectedShape() { // Opera sulla selezione primaria
        if (getSelectedShape() != null) {
            Shape shapeToCut = getSelectedShape();
            cmdMgr.executeCommand(new CutShapeCommand(this.getDrawing(), shapeToCut, this.clipboard));
            // Rimuovi la forma tagliata dalla selezione
            selectedShapes.remove(shapeToCut); 
            notifyGeoEngineObservers(new Drawing.DrawingChangeEvent(Drawing.DrawingChangeEvent.ChangeType.SELECTION));
        }
    }

    public void copySelectedShape() { // Opera sulla selezione primaria
        if (getSelectedShape() != null) {
            cmdMgr.executeCommand(new CopyShapeCommand(this.getDrawing(), getSelectedShape(), this.clipboard));
             notifyGeoEngineObservers("ClipboardUpdated"); // Per abilitare/disabilitare il pulsante paste
        }
    }
    
    
    public void pasteShape() {
        Vector2D defaultOffset = new Vector2D(10, 10); // Offset di default per il paste
        if (!this.clipboard.isEmpty()) {
            cmdMgr.executeCommand(new PasteShapeCommand(this.getDrawing(), this.clipboard, defaultOffset));
            // Opzionale: selezionare la forma incollata (PasteShapeCommand potrebbe restituirla o l'ID)
        }
    }
    public void pasteShape(Vector2D offset) { 
        if (!this.clipboard.isEmpty() && offset != null) {
            cmdMgr.executeCommand(new PasteShapeCommand(this.getDrawing(), this.clipboard, offset));
        }
    }
    
    public void bringSelectedShapeToFront() { // Opera sulla selezione primaria
        if (getSelectedShape() != null) {
            cmdMgr.executeCommand(new BringToFrontCommand(this.getDrawing(), getSelectedShape()));
        }
    }
    public void sendSelectedShapeToBack() { // Opera sulla selezione primaria
        if (getSelectedShape() != null) {
            cmdMgr.executeCommand(new SendToBackCommand(this.getDrawing(), getSelectedShape()));
        }
    }

    
    public void rotateSelectedShape(double angleDegrees) { 
        if (getSelectedShape() != null) {
            cmdMgr.executeCommand(new RotateShapeCommand(this.getDrawing(), getSelectedShape(), angleDegrees));
        }
    }
    public void changeSelectedTextSize(double newSize) {
        Shape selected = getSelectedShape();
        if (selected instanceof TextShape && newSize > 0) {
            cmdMgr.executeCommand(new ChangeTextSizeCommand(this.getDrawing(), selected, newSize));
        }
    }
    
    public void changeSelectedTextContent(String newText) {
        Shape selected = getSelectedShape();
        if (selected instanceof TextShape && newText != null) { // Permetti stringa vuota
            cmdMgr.executeCommand(new ChangeTextContentCommand(this.getDrawing(), selected, newText));
        }
    }
    
    // --- Nuovi metodi per Sprint 3 ---
    public void reflectSelectedShapesHorizontal() {
        if (!selectedShapes.isEmpty()) {
            for (Shape shape : selectedShapes) {
                // Passa drawing per permettere al comando di notificare il modello
                cmdMgr.executeCommand(new ReflectHorizontalCommand(this.getDrawing(), shape));
            }
        }
    }
    
    public void reflectSelectedShapesVertical() {
        if (!selectedShapes.isEmpty()) {
            for (Shape shape : selectedShapes) {
                cmdMgr.executeCommand(new ReflectVerticalCommand(this.getDrawing(), shape));
            }
        }
    }
    
    public void groupSelectedShapes() {
        if (selectedShapes.size() >= 2) {
            // Il comando si aspetta i riferimenti alle forme nel drawing
            List<Shape> shapesToActuallyGroup = new ArrayList<>();
            for(Shape s : selectedShapes) {
                // Assicurati che la forma sia ancora nel disegno
                if (drawing.getShapeIndex(s) != -1) {
                    shapesToActuallyGroup.add(s);
                }
            }
            if (shapesToActuallyGroup.size() < 2) return;
            
            GroupShapesCommand groupCmd = new GroupShapesCommand(this.getDrawing(), shapesToActuallyGroup);
            cmdMgr.executeCommand(groupCmd);
            // Dopo il raggruppamento, seleziona il nuovo gruppo
            // GroupShapesCommand dovrebbe esporre il gruppo creato, o GeoEngine lo cerca.
            // Assumendo che GroupShapeCommand non esponga direttamente il gruppo creato:
            // Questa parte è un po' euristica, GroupCommand dovrebbe rendere disponibile il gruppo creato.
            // Per ora, deselezioniamo. Una soluzione migliore è che il comando restituisca il gruppo.
            clearSelection(); 
            // Se il comando GroupShapesCommand avesse un metodo getCreatedGroup():
            // setSingleSelectedShape(groupCmd.getCreatedGroup());
        }
    }
    
    public void ungroupSelectedShape() {
        Shape selected = getSelectedShape(); // Prende la prima (e si spera unica) forma selezionata
        if (selected instanceof GroupShape) {
            UngroupShapeCommand ungroupCmd = new UngroupShapeCommand(this.getDrawing(), (GroupShape) selected);
            cmdMgr.executeCommand(ungroupCmd);
            clearSelection(); // Deseleziona dopo la separazione
        }
    }
    
    // Metodo per l'esportazione della libreria corrente
    public void exportReusableLibrary(String path) throws IOException {
        if (this.reusableShapeLibrary != null && this.persistenceController != null) {
            this.persistenceController.exportReusableLibrary(this.reusableShapeLibrary, path);
            if (view != null) view.showUserMessage("Reusable shape library exported successfully to: " + path);
        } else {
            if (view != null) view.showError("Cannot export library: library or persistence service not available.");
            throw new IOException("Reusable library or persistence service not available for export.");
        }
    }

    // Metodo per l'importazione di una libreria, che si fonde con quella corrente
    public void importReusableLibrary(String path) throws IOException, ClassNotFoundException {
        if (this.persistenceController != null) {
            ReusableShapeLibrary importedLibrary = this.persistenceController.importReusableLibrary(path);
            if (importedLibrary != null) {
                int count = this.reusableShapeLibrary.importDefinitions(importedLibrary); // importDefinitions gestisce i conflitti di nome
                notifyGeoEngineObservers("ReusableLibraryChanged"); // Notifica la UI per aggiornare la lista
                if (view != null) view.showUserMessage(count + " reusable shape(s) imported successfully from: " + path);
            }
        } else {
            if (view != null) view.showError("Cannot import library: persistence service not available.");
            throw new IOException("Persistence service not available for import.");
        }
    }

    public void saveSelectedAsReusableShape(String name) {
        List<Shape> selectedShapes = getSelectedShapes(); // Prende la selezione primaria
        ReusableShapeDefinition def;
        if (selectedShapes != null && name != null && !name.trim().isEmpty()) {
            if (reusableShapeLibrary.containsDefinition(name)) {
                // Gestisci conflitto nome (es. chiedi all'utente o sovrascrivi)
                if (view != null) view.showError("A reusable shape with this name already exists.");
                return;
            }
            if (selectedShapes.size() > 1){
                // Il prototipo è un clone della forma selezionata.
                List<Shape> shapesToGroup = selectedShapes.stream()
                    .map(Shape::cloneWithNewId)
                    .toList(); // Crea una lista di cloni
                GroupShape groupShape = new GroupShape(shapesToGroup);
                def = new ReusableShapeDefinition(name, groupShape);
            }
            else{
                def = new ReusableShapeDefinition(name, getSelectedShape());
            }

            reusableShapeLibrary.addDefinition(def);
            notifyGeoEngineObservers("ReusableLibraryChanged"); // Notifica la UI (es. MainApp)
            if (view != null) view.showUserMessage("Shape '" + name + "' saved as reusable.");
        }
    }

    public void deleteReusableShapeDefinition(String name) {
        if (name != null && this.reusableShapeLibrary != null) {
            if (this.reusableShapeLibrary.removeDefinition(name)) {
                notifyGeoEngineObservers("ReusableLibraryChanged");
                if (view != null) view.showUserMessage("Reusable shape '" + name + "' deleted from library.");
            } else {
                if (view != null) view.showUserMessage("Reusable shape '" + name + "' not found in library.");
            }
        }
    }

    public void placeReusableShape(String name, Point2D position) {
        ReusableShapeDefinition def = reusableShapeLibrary.getDefinition(name);
        if (def != null && position != null) {
            Shape prototype = def.getPrototype(); // Ottiene un clone del prototipo
            Shape newInstance = prototype.cloneWithNewId(); // Crea un'istanza con un nuovo ID

            // Posiziona la nuova istanza. Il prototipo è già posizionato.
            // Dobbiamo spostare la newInstance in modo che il suo centro (o angolo topLeft)
            // sia a 'position'.
            Rect bounds = newInstance.getBounds();
            Vector2D moveVec = new Vector2D(
                position.getX() - bounds.getCenter().getX(), // O position.getX() - bounds.getX() se si posiziona da topLeft
                position.getY() - bounds.getCenter().getY()  // O position.getY() - bounds.getY()
            );
            newInstance.move(moveVec);
            
            addShapeToDrawing(newInstance);
            // setSingleSelectedShape(newInstance); // Opzionale: seleziona la forma appena piazzata
        }
    }

    public Collection<ReusableShapeDefinition> getReusableShapeDefinitions() {
        return reusableShapeLibrary.getAllDefinitions();
    }

    public void checkIfAnySelectedShapeWasDeleted()
    {
        List<Shape> newSelectedShapes = new ArrayList<>();
        for(Shape selectedShape : selectedShapes)
        {
            if(drawing.getModifiableShapesList().contains(selectedShape))
            {
                newSelectedShapes.add(selectedShape);
            }
        }
        selectedShapes = newSelectedShapes;
    }
    
    // --- Undo/Redo ---
    public void undoLastCommand() { 
        cmdMgr.undo(); 
        // L'undo potrebbe cambiare la selezione (es. se una forma viene ri-aggiunta)
        // Una logica più fine potrebbe essere necessaria qui per ripristinare la selezione
        // allo stato pre-comando, ma è complesso. Per ora, deselezioniamo o lasciamo.
        checkIfAnySelectedShapeWasDeleted();
        notifyGeoEngineObservers(new Drawing.DrawingChangeEvent(Drawing.DrawingChangeEvent.ChangeType.SELECTION));
    }
    public void redoLastCommand() { 
        cmdMgr.redo(); 
        checkIfAnySelectedShapeWasDeleted();
        notifyGeoEngineObservers(new Drawing.DrawingChangeEvent(Drawing.DrawingChangeEvent.ChangeType.SELECTION));
    }
    public boolean canUndo() { return cmdMgr.canUndo(); }
    public boolean canRedo() { return cmdMgr.canRedo(); }

    // --- Persistence ---
    public void saveDrawing(String path) throws Exception { /* ... come prima ... */ }
    public void loadDrawing(String path) throws Exception {
        Drawing loadedDrawing = persistenceController.loadDrawing(path);
        if (loadedDrawing != null) {
            this.getDrawing().clear(); 
            for(Shape s : loadedDrawing.getShapesInZOrder()){
                this.getDrawing().addShape(s.cloneWithNewId()); 
            }
            cmdMgr.clearStacks();
            clearSelection(); // Deseleziona tutto dopo il caricamento
            this.getDrawing().notifyObservers(new Drawing.DrawingChangeEvent(Drawing.DrawingChangeEvent.ChangeType.LOAD));
             // Notifica anche GeoEngine observers per resettare UI (es. stato tool)
            notifyGeoEngineObservers(new Drawing.DrawingChangeEvent(Drawing.DrawingChangeEvent.ChangeType.LOAD));
        }
    }
    public void createNewDrawing() { /* ... come prima, assicurati che clearSelection() sia chiamato ... */ 
        this.getDrawing().clear(); 
        cmdMgr.clearStacks();
        clearSelection();
        setZoomLevel(1.0); // Resetta zoom
        setScrollOffset(0,0); // Resetta pan
        setGridEnabled(false); // Resetta griglia
        notifyGeoEngineObservers(new Drawing.DrawingChangeEvent(Drawing.DrawingChangeEvent.ChangeType.TRANSFORM));
        notifyGeoEngineObservers(new Drawing.DrawingChangeEvent(Drawing.DrawingChangeEvent.ChangeType.GRID));

    }


    // --- Zoom, Pan, Grid ---
    // ... metodi esistenti per zoom, pan, grid rimangono simili ...
    // Assicurati che notifichino con Drawing.DrawingChangeEvent.ChangeType.TRANSFORM o .GRID

    public final double MIN_ZOOM = 1.7; 
    public final double MAX_ZOOM = 10.0; 
    public void setZoomLevel(double level) {
        double clampedLevel = Math.max(MIN_ZOOM, Math.min(level, MAX_ZOOM));
        if (this.currentZoom != clampedLevel) {
            this.currentZoom = clampedLevel;
            notifyGeoEngineObservers(new Drawing.DrawingChangeEvent(Drawing.DrawingChangeEvent.ChangeType.TRANSFORM));
        }
    }
    public void zoomIn(double centerX, double centerY) { 
        double oldZoom = this.currentZoom;
        double newZoom = Math.min(this.currentZoom * 1.2, MAX_ZOOM);
        if (oldZoom == newZoom) return;

        this.scrollOffsetX = centerX - (centerX - this.scrollOffsetX) * (newZoom / oldZoom);
        this.scrollOffsetY = centerY - (centerY - this.scrollOffsetY) * (newZoom / oldZoom);
        this.currentZoom = newZoom;
        notifyGeoEngineObservers(new Drawing.DrawingChangeEvent(Drawing.DrawingChangeEvent.ChangeType.TRANSFORM));
    }
    public void zoomOut(double centerX, double centerY) {
        double oldZoom = this.currentZoom;
        double newZoom = Math.max(this.currentZoom / 1.2, MIN_ZOOM);
        if (oldZoom == newZoom) return;
        this.scrollOffsetX = centerX - (centerX - this.scrollOffsetX) * (newZoom / oldZoom);
        this.scrollOffsetY = centerY - (centerY - this.scrollOffsetY) * (newZoom / oldZoom);
        this.currentZoom = newZoom;
        notifyGeoEngineObservers(new Drawing.DrawingChangeEvent(Drawing.DrawingChangeEvent.ChangeType.TRANSFORM));
    }
    public void setScrollOffset(double offsetX, double offsetY) {
        if (this.scrollOffsetX != offsetX || this.scrollOffsetY != offsetY) {
            this.scrollOffsetX = offsetX; this.scrollOffsetY = offsetY;
            notifyGeoEngineObservers(new Drawing.DrawingChangeEvent(Drawing.DrawingChangeEvent.ChangeType.TRANSFORM));
        }
    }
    public void scroll(double deltaX, double deltaY) { 
        this.scrollOffsetX += deltaX; this.scrollOffsetY += deltaY;
        notifyGeoEngineObservers(new Drawing.DrawingChangeEvent(Drawing.DrawingChangeEvent.ChangeType.TRANSFORM));
    }
    public void setGridEnabled(boolean enabled) { 
        if (this.gridEnabled != enabled) {
            this.gridEnabled = enabled;
            notifyGeoEngineObservers(new Drawing.DrawingChangeEvent(Drawing.DrawingChangeEvent.ChangeType.GRID));
        }
    }
    public void setGridSize(double size) {
        double newGridSize = Math.max(0.5, size); // Min gridSize
        if (this.gridSize != newGridSize) {
            this.gridSize = newGridSize;
            if (this.gridEnabled) { // Notifica solo se la griglia è abilitata e la dimensione cambia
                notifyGeoEngineObservers(new Drawing.DrawingChangeEvent(Drawing.DrawingChangeEvent.ChangeType.GRID));
            }
        }
    }

    // --- Getters per UI state ---
    public double getCurrentZoom() { return this.currentZoom; }
    public double getScrollOffsetX() { return this.scrollOffsetX; }
    public double getScrollOffsetY() { return this.scrollOffsetY; }
    public boolean isGridEnabled() { return this.gridEnabled; }
    public double getGridSize() { return this.gridSize; }
    
    public Drawing getDrawing() { return this.drawing; }
    public Clipboard getClipboard() { return this.clipboard; }
    public ShapeFactory getShapeFactory() { return this.shapeFactory; }
    public CommandManager getCommandManager() { return this.cmdMgr; }
    public ReusableShapeLibrary getReusableShapeLibrary() { return this.reusableShapeLibrary; }
    
    public ColorData getCurrentStrokeColorForNewShapes() { return new ColorData(currentStrokeColorForNewShapes); }
    public void setCurrentStrokeColorForNewShapes(ColorData color) { this.currentStrokeColorForNewShapes = new ColorData(Objects.requireNonNull(color)); }
    public ColorData getCurrentFillColorForNewShapes() { return new ColorData(currentFillColorForNewShapes); }
    public void setCurrentFillColorForNewShapes(ColorData color) { this.currentFillColorForNewShapes = new ColorData(Objects.requireNonNull(color)); }
    public String getCurrentDefaultFontName() { return currentDefaultFontName; }
    public void setCurrentDefaultFontName(String fontName) { this.currentDefaultFontName = Objects.requireNonNull(fontName); }
    public double getCurrentDefaultFontSize() { return currentDefaultFontSize; }
    public void setCurrentDefaultFontSize(double fontSize) { if(fontSize > 0) this.currentDefaultFontSize = fontSize; }

    public void notifyViewToRefresh(){ 
        if(view != null) {
            notifyGeoEngineObservers(new Drawing.DrawingChangeEvent(Drawing.DrawingChangeEvent.ChangeType.TRANSFORM)); // Per aggiornare zoom/pan
            view.render();
        }
    }
    
    // --- Observable implementation for GeoEngine ---
    @Override
    public void attach(Observer o) {
        if (o != null && !geoEngineObservers.contains(o)) geoEngineObservers.add(o);
    }
    @Override
    public void detach(Observer o) {
        if (o != null) geoEngineObservers.remove(o);
    }
    @Override
    public void notifyObservers(Object arg) { 
        notifyGeoEngineObservers(arg);
    }
    // Metodo helper per chiarezza se GeoEngine è anche un observer di qualcos'altro
    private void notifyGeoEngineObservers(Object arg) {
        // Crea una copia per iterare se gli observer possono modificarsi durante la notifica
        List<Observer> observersCopy = new ArrayList<>(geoEngineObservers);
        for (Observer obs : observersCopy) {
            obs.update(this, arg);
        }
    }

    public PersistenceController getPersistenceController() {
        return persistenceController;
    }

    public Map<String,ToolState> getToolStates() {
        return toolStates;
    }

    
}
