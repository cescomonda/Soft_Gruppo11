
package sad.gruppo11.Controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import static org.mockito.MockitoAnnotations.openMocks;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import sad.gruppo11.Factory.ShapeFactory;
import sad.gruppo11.Infrastructure.Clipboard;
import sad.gruppo11.Infrastructure.CommandManager;
import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.LineSegment;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Persistence.PersistenceController;
import sad.gruppo11.View.DrawingView;
import sad.gruppo11.View.Observer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Test per Persistenza Disegno, Impostazioni Default, Zoom/Pan/Griglia, Notifiche
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GeoEngineTest_Part5 {

    private GeoEngine geoEngine;

    @Mock private Drawing mockDrawing; // GeoEngine usa questo mock
    @Mock private CommandManager mockCmdMgr;
    @Mock private PersistenceController mockPersistenceCtrl;
    @Mock private Clipboard mockClipboard;
    @Mock private ShapeFactory mockShapeFactory;
    @Mock private DrawingView mockView;
    @Mock private Observer mockObserver;

    private Shape mockShape1; // Per testare il caricamento

    @BeforeEach
    void setUp() {
        openMocks(this);
        // Importante: mockDrawing è passato a GeoEngine. Le operazioni di clear/addShape
        // su mockDrawing saranno verificate.
        geoEngine = new GeoEngine(mockDrawing, mockCmdMgr, mockPersistenceCtrl, mockClipboard, mockShapeFactory);
        geoEngine.attach(mockObserver);
        geoEngine.setView(mockView);

        mockShape1 = new LineSegment(new Point2D(0,0), new Point2D(1,1), ColorData.BLACK);
    }

    // --- Persistence Tests ---

    @Test
    void loadDrawing_validPath_shouldUpdateDrawingAndResetStates() throws Exception {
        String path = "test_load.ser";
        Drawing loadedDrawing = new Drawing(); // Un nuovo Drawing che verrà "caricato"
        Shape shapeInLoadedDrawing = new LineSegment(new Point2D(5,5), new Point2D(6,6), ColorData.BLUE);
        loadedDrawing.addShape(shapeInLoadedDrawing); // Aggiungi una forma al disegno caricato
        
        // Configura mockDrawing (quello dentro GeoEngine) per spiare le chiamate
        // In realtà, GeoEngine ottiene le forme da loadedDrawing e le aggiunge al suo mockDrawing.
        when(mockPersistenceCtrl.loadDrawing(path)).thenReturn(loadedDrawing);
        // Per cloneWithNewId
        Shape clonedShape = new LineSegment(new Point2D(5,5), new Point2D(6,6), ColorData.BLUE);
        // when(shapeInLoadedDrawing.cloneWithNewId()).thenReturn(clonedShape);


        geoEngine.loadDrawing(path);

        verify(mockPersistenceCtrl, times(1)).loadDrawing(path);
        // Verifica che il disegno interno di GeoEngine sia stato pulito e le nuove forme aggiunte
        verify(mockDrawing, times(1)).clear();
        ArgumentCaptor<Shape> shapeCaptor = ArgumentCaptor.forClass(Shape.class);
        verify(mockDrawing, times(1)).addShape(shapeCaptor.capture());
        Shape addedShape = shapeCaptor.getValue();
        // Verifica che le proprietà siano uguali tranne l'id
        assertTrue(addedShape instanceof LineSegment);
        LineSegment addedLine = (LineSegment) addedShape;
        assertEquals(new Point2D(5,5), addedLine.getStartPoint());
        assertEquals(new Point2D(6,6), addedLine.getEndPoint());
        assertEquals(ColorData.BLUE, addedLine.getStrokeColor());
        // L'id deve essere diverso (cloneWithNewId)
        assertNotEquals(shapeInLoadedDrawing.getId(), addedLine.getId());

        verify(mockCmdMgr, times(1)).clearStacks();
        assertTrue(geoEngine.getSelectedShapes().isEmpty(), "Selection should be cleared after load.");
        
        // Verifica notifiche di LOAD
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(mockObserver, atLeastOnce()).update(eq(geoEngine), eventCaptor.capture());
        
        boolean loadEventFoundForGeoEngine = eventCaptor.getAllValues().stream()
            .anyMatch(e -> e instanceof Drawing.DrawingChangeEvent && ((Drawing.DrawingChangeEvent)e).type == Drawing.DrawingChangeEvent.ChangeType.LOAD);
        assertTrue(loadEventFoundForGeoEngine, "GeoEngine observers should be notified of LOAD event.");

        // Il Drawing originale (mockDrawing) dovrebbe anche notificare i suoi observer (se GeoEngine lo fa propagare)
        // La notifica Drawing.notifyObservers è chiamata da Drawing.addShape/clear, quindi dovrebbe avvenire.
        // GeoEngine.loadDrawing chiama this.getDrawing().notifyObservers(...)
        verify(mockDrawing, times(1)).notifyObservers(argThat(arg -> 
            arg instanceof Drawing.DrawingChangeEvent && 
            ((Drawing.DrawingChangeEvent)arg).type == Drawing.DrawingChangeEvent.ChangeType.LOAD
        ));
    }
    
    @Test
    void loadDrawing_persistenceReturnsNull_shouldNotChangeDrawing() throws Exception {
        String path = "test_load_null.ser";
        when(mockPersistenceCtrl.loadDrawing(path)).thenReturn(null);

        geoEngine.loadDrawing(path);
        
        verify(mockDrawing, never()).clear();
        verify(mockDrawing, never()).addShape(any(Shape.class));
        verify(mockCmdMgr, never()).clearStacks();
    }

    @Test
    void createNewDrawing_shouldResetStatesAndNotify() {
        // Setup some state to be reset
        geoEngine.setZoomLevel(1.7);
        geoEngine.setScrollOffset(10, 10);
        geoEngine.setGridEnabled(true);
        geoEngine.setSelectedShapes(Arrays.asList(mockShape1));
        clearInvocations(mockDrawing, mockCmdMgr, mockObserver);

        geoEngine.createNewDrawing();

        verify(mockDrawing, times(1)).clear();
        verify(mockCmdMgr, times(1)).clearStacks();
        assertTrue(geoEngine.getSelectedShapes().isEmpty());
        assertEquals(1.7, geoEngine.getCurrentZoom());
        assertEquals(0.0, geoEngine.getScrollOffsetX());
        assertEquals(0.0, geoEngine.getScrollOffsetY());
        assertFalse(geoEngine.isGridEnabled());

        // Verify notifications (TRANSFORM for zoom/pan, GRID for grid)
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(mockObserver, atLeast(2)).update(eq(geoEngine), eventCaptor.capture()); // atLeast 2 for TRANSFORM and GRID

        boolean transformNotified = eventCaptor.getAllValues().stream().anyMatch(e -> 
            e instanceof Drawing.DrawingChangeEvent && ((Drawing.DrawingChangeEvent)e).type == Drawing.DrawingChangeEvent.ChangeType.TRANSFORM);
        boolean gridNotified = eventCaptor.getAllValues().stream().anyMatch(e -> 
            e instanceof Drawing.DrawingChangeEvent && ((Drawing.DrawingChangeEvent)e).type == Drawing.DrawingChangeEvent.ChangeType.GRID);
        
        assertTrue(transformNotified, "TRANSFORM event should be notified for zoom/pan reset.");
        assertTrue(gridNotified, "GRID event should be notified for grid reset.");
    }

    // --- Default Shape Property Tests ---
    @Test
    void setCurrentStrokeColorForNewShapes_shouldUpdateAndCopyColor() {
        ColorData newColor = ColorData.GREEN;
        geoEngine.setCurrentStrokeColorForNewShapes(newColor);
        assertNotSame(newColor, geoEngine.getCurrentStrokeColorForNewShapes(), "Should store a copy.");
        assertEquals(newColor, geoEngine.getCurrentStrokeColorForNewShapes());
    }
    
    @Test
    void setCurrentStrokeColorForNewShapes_nullColor_shouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> geoEngine.setCurrentStrokeColorForNewShapes(null));
    }

    @Test
    void setCurrentFillColorForNewShapes_shouldUpdateAndCopyColor() {
        ColorData newColor = ColorData.BLUE;
        geoEngine.setCurrentFillColorForNewShapes(newColor);
        assertNotSame(newColor, geoEngine.getCurrentFillColorForNewShapes());
        assertEquals(newColor, geoEngine.getCurrentFillColorForNewShapes());
    }

    @Test
    void setCurrentDefaultFontName_shouldUpdate() {
        String newFont = "Comic Sans MS";
        geoEngine.setCurrentDefaultFontName(newFont);
        assertEquals(newFont, geoEngine.getCurrentDefaultFontName());
    }
    
    @Test
    void setCurrentDefaultFontName_nullName_shouldThrowNullPointerException() {
         assertThrows(NullPointerException.class, () -> geoEngine.setCurrentDefaultFontName(null));
    }


    @Test
    void setCurrentDefaultFontSize_shouldUpdateIfPositive() {
        double newSize = 20.0;
        geoEngine.setCurrentDefaultFontSize(newSize);
        assertEquals(newSize, geoEngine.getCurrentDefaultFontSize());

        double originalSize = geoEngine.getCurrentDefaultFontSize();
        geoEngine.setCurrentDefaultFontSize(0.0); // Invalid
        assertEquals(originalSize, geoEngine.getCurrentDefaultFontSize(), "Font size should not change for invalid input.");
        geoEngine.setCurrentDefaultFontSize(-5.0); // Invalid
        assertEquals(originalSize, geoEngine.getCurrentDefaultFontSize());
    }

    // --- Zoom, Pan, Grid Tests ---
    @Test
    void setZoomLevel_shouldUpdateZoomAndNotifyTransform() {
        clearInvocations(mockObserver);
        geoEngine.setZoomLevel(2.5); // Min is 1.7
        assertEquals(2.5, geoEngine.getCurrentZoom());
        
        ArgumentCaptor<Drawing.DrawingChangeEvent> eventCaptor = ArgumentCaptor.forClass(Drawing.DrawingChangeEvent.class);
        verify(mockObserver, times(1)).update(eq(geoEngine), eventCaptor.capture());
        assertEquals(Drawing.DrawingChangeEvent.ChangeType.TRANSFORM, eventCaptor.getValue().type);
    }
    
    @Test
    void setZoomLevel_clampsToMinMax() {
        geoEngine.setZoomLevel(0.1); // Below min
        assertEquals(geoEngine.MIN_ZOOM, geoEngine.getCurrentZoom());
        
        geoEngine.setZoomLevel(100.0); // Above max
        assertEquals(geoEngine.MAX_ZOOM, geoEngine.getCurrentZoom());
    }

    @Test
    void zoomIn_shouldIncreaseZoomAdjustOffsetAndNotifyTransform() {
        double initialZoom = geoEngine.getCurrentZoom(); // default is 1.0, but MIN_ZOOM is 1.7
        geoEngine.setZoomLevel(2.0); // Start from a known zoom
        initialZoom = 2.0;
        double initialOffsetX = geoEngine.getScrollOffsetX();
        double initialOffsetY = geoEngine.getScrollOffsetY();
        clearInvocations(mockObserver);

        geoEngine.zoomIn(100, 100); // Center X, Center Y for zoom point

        assertTrue(geoEngine.getCurrentZoom() > initialZoom);
        // Offset calculation is complex, verify it changed and a notification occurred
        assertNotEquals(initialOffsetX, geoEngine.getScrollOffsetX());
        assertNotEquals(initialOffsetY, geoEngine.getScrollOffsetY());
        verify(mockObserver, times(1)).update(eq(geoEngine), any(Drawing.DrawingChangeEvent.class));
    }
    
    @Test
    void zoomOut_shouldDecreaseZoomAdjustOffsetAndNotifyTransform() {
        geoEngine.setZoomLevel(5.0); // Start from a higher zoom
        double initialZoom = 5.0;
        double initialOffsetX = geoEngine.getScrollOffsetX();
        double initialOffsetY = geoEngine.getScrollOffsetY();
        clearInvocations(mockObserver);

        geoEngine.zoomOut(100, 100);

        assertTrue(geoEngine.getCurrentZoom() < initialZoom);
        assertNotEquals(initialOffsetX, geoEngine.getScrollOffsetX());
        assertNotEquals(initialOffsetY, geoEngine.getScrollOffsetY());
        verify(mockObserver, times(1)).update(eq(geoEngine), any(Drawing.DrawingChangeEvent.class));
    }

    @Test
    void setScrollOffset_shouldUpdateOffsetAndNotifyTransform() {
        clearInvocations(mockObserver);
        geoEngine.setScrollOffset(50, -50);
        assertEquals(50, geoEngine.getScrollOffsetX());
        assertEquals(-50, geoEngine.getScrollOffsetY());
        verify(mockObserver, times(1)).update(eq(geoEngine), any(Drawing.DrawingChangeEvent.class));
    }
    
    @Test
    void setScrollOffset_sameOffset_shouldNotNotify() {
        double currentX = geoEngine.getScrollOffsetX();
        double currentY = geoEngine.getScrollOffsetY();
        clearInvocations(mockObserver);
        geoEngine.setScrollOffset(currentX, currentY);
        verify(mockObserver, never()).update(any(), any());
    }

    @Test
    void scroll_shouldUpdateOffsetAndNotifyTransform() {
        double initialX = geoEngine.getScrollOffsetX();
        double initialY = geoEngine.getScrollOffsetY();
        clearInvocations(mockObserver);

        geoEngine.scroll(10, 20);
        assertEquals(initialX + 10, geoEngine.getScrollOffsetX());
        assertEquals(initialY + 20, geoEngine.getScrollOffsetY());
        verify(mockObserver, times(1)).update(eq(geoEngine), any(Drawing.DrawingChangeEvent.class));
    }

    @Test
    void setGridEnabled_changeState_shouldUpdateAndNotifyGrid() {
        boolean initialState = geoEngine.isGridEnabled();
        clearInvocations(mockObserver);

        geoEngine.setGridEnabled(!initialState);
        assertEquals(!initialState, geoEngine.isGridEnabled());
        ArgumentCaptor<Drawing.DrawingChangeEvent> eventCaptor = ArgumentCaptor.forClass(Drawing.DrawingChangeEvent.class);
        verify(mockObserver, times(1)).update(eq(geoEngine), eventCaptor.capture());
        assertEquals(Drawing.DrawingChangeEvent.ChangeType.GRID, eventCaptor.getValue().type);
    }
    
    @Test
    void setGridEnabled_sameState_shouldNotNotify() {
        boolean initialState = geoEngine.isGridEnabled();
        clearInvocations(mockObserver);
        geoEngine.setGridEnabled(initialState);
        verify(mockObserver, never()).update(any(),any());
    }

    @Test
    void setGridSize_changeSize_shouldUpdateAndNotifyGridIfEnabled() {
        geoEngine.setGridEnabled(true); // Grid must be enabled for notification on size change
        double initialSize = geoEngine.getGridSize();
        clearInvocations(mockObserver);

        geoEngine.setGridSize(initialSize + 10.0);
        assertEquals(initialSize + 10.0, geoEngine.getGridSize());
        verify(mockObserver, times(1)).update(eq(geoEngine), any(Drawing.DrawingChangeEvent.class));
    }
    
    @Test
    void setGridSize_changeSize_gridDisabled_shouldUpdateButNotNotify() {
        geoEngine.setGridEnabled(false);
        double initialSize = geoEngine.getGridSize();
        clearInvocations(mockObserver);

        geoEngine.setGridSize(initialSize + 10.0);
        assertEquals(initialSize + 10.0, geoEngine.getGridSize());
        verify(mockObserver, never()).update(eq(geoEngine), any(Drawing.DrawingChangeEvent.class));
    }
    
    @Test
    void setGridSize_sameSize_shouldNotNotify() {
        double currentSize = geoEngine.getGridSize();
        geoEngine.setGridEnabled(true);
        clearInvocations(mockObserver);
        geoEngine.setGridSize(currentSize);
        verify(mockObserver, never()).update(any(), any());
    }
    
    @Test
    void setGridSize_invalidSize_shouldClampToMin() {
        geoEngine.setGridSize(0.1); // Below min 0.5
        assertEquals(0.5, geoEngine.getGridSize());
    }

    // --- Final Notification Test ---
    @Test
    void notifyViewToRefresh_shouldNotifyTransformAndViewRender() {
        clearInvocations(mockObserver, mockView);
        geoEngine.notifyViewToRefresh();

        ArgumentCaptor<Drawing.DrawingChangeEvent> eventCaptor = ArgumentCaptor.forClass(Drawing.DrawingChangeEvent.class);
        verify(mockObserver, times(1)).update(eq(geoEngine), eventCaptor.capture());
        assertEquals(Drawing.DrawingChangeEvent.ChangeType.TRANSFORM, eventCaptor.getValue().type);
        verify(mockView, times(1)).render();
    }
    
    @Test
    void notifyViewToRefresh_viewIsNull_shouldNotThrowException() {
        GeoEngine engineNoView = new GeoEngine(mockDrawing, mockCmdMgr, mockPersistenceCtrl, mockClipboard, mockShapeFactory);
        // engineNoView.setView(null) is disallowed by setView check.
        // But if view was set and then became null internally (not possible with current code).
        // The check 'if(view != null)' in notifyViewToRefresh handles this.
        assertDoesNotThrow(() -> engineNoView.notifyViewToRefresh());
    }
}

            