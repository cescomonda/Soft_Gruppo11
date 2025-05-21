package sad.gruppo11.Controller; // Assicurati che il package sia corretto

// Import delle classi utilizzate dal controller
import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Infrastructure.CommandManager;
import sad.gruppo11.Infrastructure.Clipboard;
import sad.gruppo11.Persistence.PersistenceController;
import sad.gruppo11.View.Observer; // Per notificare DrawingView per il render del canvas

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer; // Per i listener di cambio selezione

// Classe principale del controller: gestisce logica, eventi, stato corrente e interazione fra moduli
public class GeoEngine {
    private ToolState currentState; // Stato corrente dello strumento
    private sad.gruppo11.Model.Drawing drawing; // Modello dati principale (lista di forme)
    private final CommandManager cmdMgr; // Gestore dei comandi per undo/redo
    private final PersistenceController persistenceController; // Per salvataggio/caricamento disegni
    private final Clipboard clipboard; // Gestione della clipboard (copia/incolla)

    private ColorData currentStrokeColorForNewShapes; // Colore bordo per nuove forme
    private ColorData currentFillColorForNewShapes;   // Colore riempimento per nuove forme

    private final List<Observer> modelObservers; // Lista degli observer che devono essere notificati su cambiamenti del modello
    private final List<Consumer<Shape>> selectionChangeListeners; // Listener per cambiamenti nella selezione di una forma
    private final List<Consumer<ToolState>> toolStateChangeListeners; // Listener per cambiamenti dello strumento attivo

    // Riferimenti agli stati degli strumenti (linea, rettangolo, ellisse, selezione)
    private final ToolState lineState;
    private final ToolState rectangleState;
    private final ToolState ellipseState;
    private final ToolState selectState;

    private Shape currentlySelectedShapeByEngine; // Forma attualmente selezionata (se presente)

    // Costruttore
    public GeoEngine() {
        this.drawing = new sad.gruppo11.Model.Drawing();
        this.cmdMgr = new CommandManager(this); // Passa se stesso per callback
        this.persistenceController = new PersistenceController();
        this.clipboard = Clipboard.getInstance();
        this.modelObservers = new ArrayList<>();
        this.selectionChangeListeners = new ArrayList<>();
        this.toolStateChangeListeners = new ArrayList<>();

        // Inizializzazione stati
        this.lineState = new LineState();
        this.rectangleState = new RectangleState();
        this.ellipseState = new EllipseState();
        this.selectState = new SelectState();

        // Colori di default
        this.currentStrokeColorForNewShapes = new ColorData(ColorData.BLACK);
        this.currentFillColorForNewShapes = new ColorData(ColorData.TRANSPARENT);

        this.currentlySelectedShapeByEngine = null;

        setState(this.selectState); // Stato iniziale = strumento di selezione
        // System.out.println("GeoEngine: Initialized.");
    }

    // Observer pattern - registrazione observer del modello (es. view)
    public void addModelObserver(Observer observer) {
        Objects.requireNonNull(observer, "Model Observer cannot be null.");
        if (!this.modelObservers.contains(observer)) {
            this.modelObservers.add(observer);
        }
    }

    public void removeModelObserver(Observer observer) {
        this.modelObservers.remove(observer);
    }

    // Aggiunta listener per cambi di selezione (es. pannello proprietà)
    public void addSelectionChangeListener(Consumer<Shape> listener) {
        Objects.requireNonNull(listener, "SelectionChange Listener cannot be null.");
        if (!this.selectionChangeListeners.contains(listener)) {
            this.selectionChangeListeners.add(listener);
        }
    }

    // Aggiunta listener per cambi di tool
    public void addToolStateChangeListener(Consumer<ToolState> listener) {
        Objects.requireNonNull(listener, "ToolStateChange Listener cannot be null.");
        if (!this.toolStateChangeListeners.contains(listener)) {
            this.toolStateChangeListeners.add(listener);
        }
    }

    // Notifica a tutti i listener che lo strumento è cambiato
    private void notifyToolStateChanged(ToolState newState) {
        for (Consumer<ToolState> listener : toolStateChangeListeners) {
            listener.accept(newState);
        }
    }

    public void removeSelectionChangeListener(Consumer<Shape> listener) {
        this.selectionChangeListeners.remove(listener);
    }

    // Notifica a tutti gli observer che il modello è cambiato
    public void notifyModelChanged() {
        for (Observer observer : modelObservers) {
            observer.update(this.drawing);
        }

        // Verifica validità della selezione
        if (currentlySelectedShapeByEngine != null && (drawing.getShapes() == null || !drawing.getShapes().contains(currentlySelectedShapeByEngine))) {
            setCurrentlySelectedShape(null); // La forma selezionata non è più valida
        } else {
            notifySelectionChanged(currentlySelectedShapeByEngine); // Aggiorna proprietà
        }
    }

    // Forza il refresh della UI anche se il modello non è cambiato
    public void notifyViewToRefresh() {
        for (Observer observer : modelObservers) {
            observer.update(this.drawing);
        }
        notifySelectionChanged(this.currentlySelectedShapeByEngine);
    }

    // Notifica listener di cambio selezione
    private void notifySelectionChanged(Shape selectedShape) {
        for (Consumer<Shape> listener : selectionChangeListeners) {
            listener.accept(selectedShape);
        }
    }

    // Getter/setter dei colori per nuove forme
    public ColorData getCurrentStrokeColorForNewShapes() {
        return new ColorData(currentStrokeColorForNewShapes);
    }

    public void setCurrentStrokeColorForNewShapes(ColorData currentStrokeColorForNewShapes) {
        this.currentStrokeColorForNewShapes = new ColorData(currentStrokeColorForNewShapes);
    }

    public ColorData getCurrentFillColorForNewShapes() {
        return new ColorData(currentFillColorForNewShapes);
    }

    public void setCurrentFillColorForNewShapes(ColorData currentFillColorForNewShapes) {
        this.currentFillColorForNewShapes = new ColorData(currentFillColorForNewShapes);
    }

    // Cambio di stato (strumento attivo)
    public void setState(ToolState newState) {
        Objects.requireNonNull(newState, "New state cannot be null.");
        if (this.currentState == newState) return;

        ToolState oldState = this.currentState;
        if (oldState != null) {
            oldState.onExitState(this);
        }

        this.currentState = newState;
        
        if (!(newState instanceof SelectState)) {
            if (getSelectedShape() != null) {
                setCurrentlySelectedShape(null);
            }
        }
        
        newState.onEnterState(this);
        notifyToolStateChanged(newState);
        notifyViewToRefresh();
        System.out.println("GeoEngine: State changed to " + this.currentState.getToolName());
    }

    // Eventi mouse delegati allo stato corrente
    public void onMousePressed(Point2D p) {
        if (currentState != null) {
            currentState.onMousePressed(p, this);
            if (!(currentState instanceof SelectState)) {
                setCurrentlySelectedShape(null);
            }
        }
    }

    public void onMouseDragged(Point2D p) {
        if (currentState != null) {
            currentState.onMouseDragged(p, this);
        }
    }

    public void onMouseReleased(Point2D p) {
        if (currentState != null) {
            currentState.onMouseReleased(p, this);
        }
    }

    // Salvataggio disegno su file
    public void saveDrawing(String path) throws Exception {
        // System.out.println("GeoEngine: Save drawing requested to path: " + path);
        persistenceController.saveDrawing(this.drawing, path);
    }

    // Caricamento disegno da file
    public void loadDrawing(String path) {
        try {
            sad.gruppo11.Model.Drawing loadedDrawing = persistenceController.loadDrawing(path);
            if (loadedDrawing != null) {
                this.drawing = loadedDrawing;
                this.cmdMgr.clearStacks();
                setCurrentlySelectedShape(null);
                notifyModelChanged();
                System.out.println("GeoEngine: Drawing loaded successfully from " + path);
            } else {
                System.err.println("GeoEngine: Loaded drawing was null from path: " + path);
            }
        } catch (Exception e) {
            System.err.println("GeoEngine: Failed to load drawing from path: " + path + ". Error: " + e.getMessage());
            throw new RuntimeException("Failed to load drawing: " + e.getMessage(), e);
        }
    }

    // Crea un nuovo disegno
    public void createNewDrawing() {
        this.drawing = new sad.gruppo11.Model.Drawing();
        this.cmdMgr.clearStacks();
        // setCurrentlySelectedShape(null);
        this.currentlySelectedShapeByEngine = null;
        notifyModelChanged();
        System.out.println("GeoEngine: New drawing created. Command history cleared.");
    }

    // Getter per componenti interni
    public sad.gruppo11.Model.Drawing getDrawing() { return drawing; }
    public CommandManager getCommandManager() { return cmdMgr; }
    public Clipboard getClipboard() { return clipboard; }
    public ToolState getCurrentState() { return currentState; }
    public ToolState getLineState() { return lineState; }
    public ToolState getRectangleState() { return rectangleState; }
    public ToolState getEllipseState() { return ellipseState; }
    public ToolState getSelectState() { return selectState; }

    public Shape getSelectedShape() {
        return this.currentlySelectedShapeByEngine;
    }

    // Gestione selezione
    public void setCurrentlySelectedShape(Shape shape) {
        if (this.currentlySelectedShapeByEngine != shape) {
            this.currentlySelectedShapeByEngine = shape;
            // System.out.println("GeoEngine: Selection changed to: " + (shape != null ? shape.getId() : "null"));
            notifyViewToRefresh();
        }
    }
}
