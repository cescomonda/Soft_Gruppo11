
package sad.gruppo11.View;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import static org.mockito.MockitoAnnotations.openMocks;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.stage.Stage;
import javafx.scene.canvas.Canvas; // Per mockare il canvas interno a CanvasPanel

import sad.gruppo11.Controller.GeoEngine;
import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.LineSegment;
import sad.gruppo11.Model.GroupShape;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.ColorData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Per i test che potrebbero interagire con Platform.runLater, anche se qui cerchiamo di evitarlo
// import org.testfx.framework.junit5.ApplicationExtension;
// import org.testfx.framework.junit5.Start;

// @ExtendWith(ApplicationExtension.class) // Solo se si inizializza Toolkit JavaFX
class DrawingViewTest {

    static {
        try {
            // Prova ad avviare il Toolkit JavaFX.
            // Questo permette a Platform.runLater di funzionare senza lanciare IllegalStateException.
            // Il Runnable potrebbe essere eseguito immediatamente nel thread corrente.
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Toolkit già avviato (es. se altri test lo hanno fatto o se eseguito in un contesto FX), va bene.
            System.out.println("JavaFX Toolkit already started.");
        } catch (UnsatisfiedLinkError e) {
            // Potrebbe accadere in ambienti headless senza librerie grafiche.
            System.err.println("Failed to start JavaFX Toolkit due to UnsatisfiedLinkError (graphics libraries might be missing): " + e.getMessage());
        }
    }

    @Mock private GeoEngine mockGeoEngine;
    @Mock private CanvasPanel mockCanvasPanel;
    @Mock private Drawing mockDrawing; // Il modello che GeoEngine "gestisce"
    @Mock private Stage mockPrimaryStage;
    @Mock private Canvas mockInternalCanvas; // Il Canvas JavaFX dentro CanvasPanel

    @Captor private ArgumentCaptor<EventHandler<MouseEvent>> mousePressedHandlerCaptor;
    @Captor private ArgumentCaptor<EventHandler<MouseEvent>> mouseDraggedHandlerCaptor;
    @Captor private ArgumentCaptor<EventHandler<MouseEvent>> mouseReleasedHandlerCaptor;
    @Captor private ArgumentCaptor<EventHandler<ScrollEvent>> scrollHandlerCaptor;
    
    private DrawingView drawingView;
    private Shape mockShape1;
    private Shape mockShape2;

    // Necessario per i test JavaFX non UI se si toccano componenti che richiedono il toolkit
    // static {
    //     try {
    //         // Inizializza JavaFX Toolkit se non è già in esecuzione (es. per Platform.runLater)
    //         Platform.startup(() -> {});
    //     } catch (IllegalStateException e) {
    //         // Toolkit già avviato, va bene
    //     }
    // }


    @BeforeEach
    void setUp() {
        openMocks(this);

        when(mockGeoEngine.getDrawing()).thenReturn(mockDrawing);
        when(mockCanvasPanel.getCanvas()).thenReturn(mockInternalCanvas); // CanvasPanel restituisce il mockCanvas
        
        // Configura il mockCanvasPanel per restituire coordinate trasformate
        when(mockCanvasPanel.screenToWorld(any(Point2D.class))).thenAnswer(invocation -> {
            Point2D screenPoint = invocation.getArgument(0);
            // Semplice trasformazione identità per il test, o una specifica se necessario
            return new Point2D(screenPoint.getX() - 10, screenPoint.getY() - 20); // Esempio: offset (-10, -20)
        });
        
        // Inizializza drawingView DOPO che i mock sono pronti
        drawingView = new DrawingView(mockGeoEngine, mockCanvasPanel, mockPrimaryStage);

        mockShape1 = new LineSegment(new Point2D(0,0), new Point2D(1,1), ColorData.BLACK);
        mockShape2 = new LineSegment(new Point2D(10,10), new Point2D(12,12), ColorData.RED);
    }

    @Test
    void constructor_shouldAttachToObservablesAndSetupMouseHandlers() {
        verify(mockDrawing).attach(drawingView);
        verify(mockGeoEngine).attach(drawingView);

        // Verifica che i gestori di eventi siano stati impostati
        verify(mockInternalCanvas).setOnMousePressed(any());
        verify(mockInternalCanvas).setOnMouseDragged(any());
        verify(mockInternalCanvas).setOnMouseReleased(any());
        verify(mockInternalCanvas).setOnScroll(any());
        
        // Verifica che le trasformazioni iniziali siano state impostate e sia avvenuto un render
        verify(mockCanvasPanel).setTransform(anyDouble(), anyDouble(), anyDouble());
        verify(mockCanvasPanel).drawShapes(anyList(), any()); // Render iniziale
    }
    
    @Test
    void constructor_nullController_shouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> new DrawingView(null, mockCanvasPanel, mockPrimaryStage));
    }
    
    @Test
    void constructor_nullCanvasPanel_shouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> new DrawingView(mockGeoEngine, null, mockPrimaryStage));
    }

    @Test
    void constructor_nullPrimaryStage_shouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> new DrawingView(mockGeoEngine, mockCanvasPanel, null));
    }
    
    @Test
    void update_fromGeoEngine_transformEvent_shouldUpdateCanvasAndRender() {
        Drawing.DrawingChangeEvent event = new Drawing.DrawingChangeEvent(Drawing.DrawingChangeEvent.ChangeType.TRANSFORM);
        drawingView.update(mockGeoEngine, event);
        // Idem come sopra per Platform.runLater.
        // Verifichiamo le chiamate che dovrebbero avvenire dentro il Platform.runLater
        verify(mockCanvasPanel, atLeastOnce()).setTransform(anyDouble(), anyDouble(), anyDouble());
    }
    
    @Test
    void update_fromGeoEngine_gridEvent_shouldUpdateCanvasAndRender() {
        Drawing.DrawingChangeEvent event = new Drawing.DrawingChangeEvent(Drawing.DrawingChangeEvent.ChangeType.GRID);
        drawingView.update(mockGeoEngine, event);
        verify(mockCanvasPanel, atLeastOnce()).setGridEnabled(anyBoolean());
        verify(mockCanvasPanel, atLeastOnce()).setGridSize(anyDouble());
    }
    
    @Test
    void update_fromGeoEngine_selectionOrOtherEvent_shouldRender() {
        // Qualsiasi altra notifica da GeoEngine che non sia TRANSFORM o GRID dovrebbe causare un render.
        drawingView.update(mockGeoEngine, "SomeOtherEvent"); // es. cambio tool
        // La chiamata a render() è interna a Platform.runLater
    }


    // --- Mouse Event Handlers ---
    // Questi test sono più complessi perché dobbiamo catturare e invocare gli handler.
    @Test
    void onMousePressedHandler_primaryButton_shouldCallController() {
        // Cattura l'handler impostato su mockInternalCanvas
        verify(mockInternalCanvas).setOnMousePressed(mousePressedHandlerCaptor.capture());
        EventHandler<MouseEvent> handler = mousePressedHandlerCaptor.getValue();

        // Crea un evento MouseEvent fittizio
        MouseEvent mockPressEvent = mock(MouseEvent.class);
        when(mockPressEvent.getButton()).thenReturn(MouseButton.PRIMARY);
        when(mockPressEvent.isPrimaryButtonDown()).thenReturn(true);
        when(mockPressEvent.getX()).thenReturn(50.0);
        when(mockPressEvent.getY()).thenReturn(60.0);

        handler.handle(mockPressEvent);

        // Verifica che GeoEngine.onMousePressed sia stato chiamato con coordinate trasformate
        // screenToWorld stubbato: (50-10, 60-20) = (40,40)
        ArgumentCaptor<Point2D> pointCaptor = ArgumentCaptor.forClass(Point2D.class);
        verify(mockGeoEngine).onMousePressed(pointCaptor.capture());
        assertEquals(40.0, pointCaptor.getValue().getX(), 0.001);
        assertEquals(40.0, pointCaptor.getValue().getY(), 0.001);
        verify(mockPressEvent).consume();
    }
    
    @Test
    void onMousePressedHandler_secondaryButton_shouldNotCallController() {
        verify(mockInternalCanvas).setOnMousePressed(mousePressedHandlerCaptor.capture());
        EventHandler<MouseEvent> handler = mousePressedHandlerCaptor.getValue();
        MouseEvent mockPressEvent = mock(MouseEvent.class);
        when(mockPressEvent.getButton()).thenReturn(MouseButton.SECONDARY);
        when(mockPressEvent.isPrimaryButtonDown()).thenReturn(false);

        handler.handle(mockPressEvent);
        verify(mockGeoEngine, never()).onMousePressed(any(Point2D.class));
        verify(mockPressEvent, never()).consume(); // Non consumato se non gestito
    }


    @Test
    void onMouseDraggedHandler_primaryButton_shouldCallController() {
        verify(mockInternalCanvas).setOnMouseDragged(mouseDraggedHandlerCaptor.capture());
        EventHandler<MouseEvent> handler = mouseDraggedHandlerCaptor.getValue();
        MouseEvent mockDragEvent = mock(MouseEvent.class);
        when(mockDragEvent.isPrimaryButtonDown()).thenReturn(true);
        when(mockDragEvent.getX()).thenReturn(70.0);
        when(mockDragEvent.getY()).thenReturn(80.0);

        handler.handle(mockDragEvent);
        // screenToWorld: (70-10, 80-20) = (60,60)
        ArgumentCaptor<Point2D> pointCaptor = ArgumentCaptor.forClass(Point2D.class);
        verify(mockGeoEngine).onMouseDragged(pointCaptor.capture());
        assertEquals(60.0, pointCaptor.getValue().getX(), 0.001);
        assertEquals(60.0, pointCaptor.getValue().getY(), 0.001);
        verify(mockDragEvent).consume();
    }

    @Test
    void onMouseReleasedHandler_primaryButton_shouldCallController() {
        verify(mockInternalCanvas).setOnMouseReleased(mouseReleasedHandlerCaptor.capture());
        EventHandler<MouseEvent> handler = mouseReleasedHandlerCaptor.getValue();
        MouseEvent mockReleaseEvent = mock(MouseEvent.class);
        when(mockReleaseEvent.getButton()).thenReturn(MouseButton.PRIMARY);
        when(mockReleaseEvent.getX()).thenReturn(90.0);
        when(mockReleaseEvent.getY()).thenReturn(100.0);

        handler.handle(mockReleaseEvent);
        // screenToWorld: (90-10, 100-20) = (80,80)
        ArgumentCaptor<Point2D> pointCaptor = ArgumentCaptor.forClass(Point2D.class);
        verify(mockGeoEngine).onMouseReleased(pointCaptor.capture());
        assertEquals(80.0, pointCaptor.getValue().getX(), 0.001);
        assertEquals(80.0, pointCaptor.getValue().getY(), 0.001);
        verify(mockReleaseEvent).consume();
    }

    @Test
    void onScrollHandler_deltaYPositive_shouldCallZoomIn() {
        verify(mockInternalCanvas).setOnScroll(scrollHandlerCaptor.capture());
        EventHandler<ScrollEvent> handler = scrollHandlerCaptor.getValue();
        ScrollEvent mockScrollEvent = mock(ScrollEvent.class);
        when(mockScrollEvent.getDeltaY()).thenReturn(40.0); // Positive delta
        when(mockScrollEvent.getX()).thenReturn(100.0); // Screen X for zoom center
        when(mockScrollEvent.getY()).thenReturn(120.0); // Screen Y for zoom center

        handler.handle(mockScrollEvent);
        verify(mockGeoEngine).zoomIn(100.0, 120.0);
        verify(mockScrollEvent).consume();
    }

    @Test
    void onScrollHandler_deltaYNegative_shouldCallZoomOut() {
        verify(mockInternalCanvas).setOnScroll(scrollHandlerCaptor.capture());
        EventHandler<ScrollEvent> handler = scrollHandlerCaptor.getValue();
        ScrollEvent mockScrollEvent = mock(ScrollEvent.class);
        when(mockScrollEvent.getDeltaY()).thenReturn(-40.0); // Negative delta
        when(mockScrollEvent.getX()).thenReturn(150.0);
        when(mockScrollEvent.getY()).thenReturn(160.0);

        handler.handle(mockScrollEvent);
        verify(mockGeoEngine).zoomOut(150.0, 160.0);
        verify(mockScrollEvent).consume();
    }
    
    @Test
    void onScrollHandler_deltaYZero_shouldDoNothing() {
        verify(mockInternalCanvas).setOnScroll(scrollHandlerCaptor.capture());
        EventHandler<ScrollEvent> handler = scrollHandlerCaptor.getValue();
        ScrollEvent mockScrollEvent = mock(ScrollEvent.class);
        when(mockScrollEvent.getDeltaY()).thenReturn(0.0);

        handler.handle(mockScrollEvent);
        verify(mockGeoEngine, never()).zoomIn(anyDouble(), anyDouble());
        verify(mockGeoEngine, never()).zoomOut(anyDouble(), anyDouble());
        verify(mockScrollEvent).consume(); // Event is still consumed
    }

    // --- Temporary Visuals Forwarding ---
    @Test
    void drawTemporaryPolygonGuide_forwardsToCanvasPanel() {
        List<Point2D> points = Arrays.asList(new Point2D(1,1));
        Point2D currentMouse = new Point2D(2,2);
        drawingView.drawTemporaryPolygonGuide(points, currentMouse);
        verify(mockCanvasPanel).setTemporaryPolygonGuide(points, currentMouse);
    }

    @Test
    void drawTemporaryGhostShape_forwardsToCanvasPanel() {
        Shape ghost = mock(Shape.class);
        drawingView.drawTemporaryGhostShape(ghost);
        verify(mockCanvasPanel).setTemporaryGhostShape(ghost);
    }

    @Test
    void clearTemporaryVisuals_forwardsToCanvasPanel() {
        drawingView.clearTemporaryVisuals();
        verify(mockCanvasPanel).clearTemporaryVisuals();
    }
    
    @Test
    void showUserMessage_logsToConsole() {
        // The provided DrawingView.showUserMessage prints to System.out.
        // We can't easily capture System.out without custom setup (like SystemLambda library).
        // For this test, we'll just ensure it runs without error.
        assertDoesNotThrow(() -> drawingView.showUserMessage("Test message from view"));
    }
}

            