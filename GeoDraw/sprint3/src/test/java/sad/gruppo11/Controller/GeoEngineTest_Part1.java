
package sad.gruppo11.Controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import static org.mockito.MockitoAnnotations.openMocks;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
import java.util.Collections;
import java.util.List;

class GeoEngineTest_Part1 {

    private GeoEngine geoEngine;

    @Mock private Drawing mockDrawing;
    @Mock private CommandManager mockCmdMgr;
    @Mock private PersistenceController mockPersistenceCtrl;
    @Mock private Clipboard mockClipboard;
    @Mock private ShapeFactory mockShapeFactory;
    @Mock private ReusableShapeLibrary mockReusableLib; // Mock for ReusableShapeLibrary
    @Mock private DrawingView mockView;
    @Mock private Observer mockObserver;

    // Mocks for specific ToolStates to be put into geoEngine's getToolStates() map
    @Mock private ToolState mockSelectState;
    @Mock private ToolState mockLineState;
    // ... other tool states can be mocked as needed

    private Shape mockShape1;
    private Shape mockShape2;

    @BeforeEach
    void setUp() {
        openMocks(this); // Initializes all @Mock annotated fields

        // Configure default names for mocked tool states
        when(mockSelectState.getName()).thenReturn("SelectTool");
        when(mockLineState.getName()).thenReturn("LineTool");

        // Create GeoEngine instance, but we need to inject mocked getToolStates() map
        // This is tricky with the current GeoEngine constructor which initializes getToolStates() internally.
        // Option 1: Modify GeoEngine to allow injecting getToolStates() (better for testability).
        // Option 2: Use spy on GeoEngine and stub getToolStateByName or similar if such method existed.
        // Option 3: For now, we'll test based on the default tools it creates, and if we need to
        //           verify interactions with specific states, we'll have to be creative or accept
        //           that we are testing the real state objects it creates.
        // For this part, we'll assume the internal creation of SelectState, LineState etc. is okay,
        // and we'll mostly verify activate/deactivate calls on the *current* state.

        // GeoEngine constructor initializes ReusableShapeLibrary itself.
        // To use a mock ReusableShapeLibrary, we'd need to modify GeoEngine or use a more complex setup.
        // For now, we'll test with the real ReusableShapeLibrary created by GeoEngine.
        // If we need to control ReusableShapeLibrary behavior, this will be a limitation.

        geoEngine = new GeoEngine(mockDrawing, mockCmdMgr, mockPersistenceCtrl, mockClipboard, mockShapeFactory);
        // The line above uses a real ReusableShapeLibrary.
        // To use mockReusableLib, the GeoEngine constructor would need to accept it.
        // For example:
        // geoEngine = new GeoEngine(mockDrawing, mockCmdMgr, mockPersistenceCtrl, mockClipboard, mockShapeFactory, mockReusableLib);

        geoEngine.attach(mockObserver);
        geoEngine.setView(mockView); // Set the view for notifications

        mockShape1 = new LineSegment(new Point2D(0,0), new Point2D(1,1), ColorData.BLACK);
        mockShape2 = new LineSegment(new Point2D(2,2), new Point2D(3,3), ColorData.RED);
    }

    @Test
    void constructor_shouldInitializeDependenciesAndDefaultState() {
        assertNotNull(geoEngine.getDrawing());
        assertNotNull(geoEngine.getCommandManager());
        assertNotNull(geoEngine.getPersistenceController());
        assertNotNull(geoEngine.getClipboard());
        assertNotNull(geoEngine.getShapeFactory());
        assertNotNull(geoEngine.getReusableShapeLibrary()); // Checks the internally created one

        assertEquals("SelectTool", geoEngine.getCurrentToolName(), "Default tool should be SelectTool.");
        assertTrue(geoEngine.getSelectedShapes().isEmpty(), "Selection should be empty initially.");

        verify(mockCmdMgr).setDrawingModel(mockDrawing);
        // Verify the default "SelectTool" (which is a real SelectState instance) had activate called.
        // This is hard without access to the actual state instances unless we spy on GeoEngine.
        // For now, we assume setState("SelectTool") in constructor works.
    }
    
    @Test
    void constructor_nullDependencies_shouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> new GeoEngine(null, mockCmdMgr, mockPersistenceCtrl, mockClipboard, mockShapeFactory));
        assertThrows(NullPointerException.class, () -> new GeoEngine(mockDrawing, null, mockPersistenceCtrl, mockClipboard, mockShapeFactory));
        assertThrows(NullPointerException.class, () -> new GeoEngine(mockDrawing, mockCmdMgr, null, mockClipboard, mockShapeFactory));
        assertThrows(NullPointerException.class, () -> new GeoEngine(mockDrawing, mockCmdMgr, mockPersistenceCtrl, null, mockShapeFactory));
        assertThrows(NullPointerException.class, () -> new GeoEngine(mockDrawing, mockCmdMgr, mockPersistenceCtrl, mockClipboard, null));
    }

    @Test
    void setView_shouldStoreViewAndNotify() {
        DrawingView newMockView = mock(DrawingView.class);
        geoEngine.setView(newMockView); // Call the method to test
        
        assertSame(newMockView, geoEngine.getView());
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(mockObserver, times(2)).update(eq(geoEngine), captor.capture()); // Once from setup, once from setView
        
        Object lastArg = captor.getValue();
        assertTrue(lastArg instanceof Drawing.DrawingChangeEvent);
        assertEquals(Drawing.DrawingChangeEvent.ChangeType.TRANSFORM, ((Drawing.DrawingChangeEvent)lastArg).type);
    }
    
    @Test
    void setView_nullView_shouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> geoEngine.setView(null));
    }

    @Test
    void setState_validToolName_shouldChangeCurrentStateAndNotify() {
        // Ignora notifiche di setup
        clearInvocations(mockObserver);

        geoEngine.setState("LineTool");

        assertEquals("LineTool", geoEngine.getCurrentToolName());
        assertTrue(geoEngine.getSelectedShapes().isEmpty(), "Changing tool should clear selection.");

        // Cattura le due notifiche
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(mockObserver, times(2)).update(eq(geoEngine), eventCaptor.capture());

        List<Object> allValues = eventCaptor.getAllValues();
        boolean selectionClearedNotified = false;
        boolean toolChangedNotified = false;

        for (Object arg : allValues) {
            if (arg instanceof Drawing.DrawingChangeEvent) {
                if (((Drawing.DrawingChangeEvent) arg).type == Drawing.DrawingChangeEvent.ChangeType.SELECTION) {
                    selectionClearedNotified = true;
                }
            } else if (arg instanceof String && "ToolChanged:LineTool".equals(arg)) {
                toolChangedNotified = true;
            }
        }
        assertTrue(selectionClearedNotified, "Observer should be notified of selection change (clear).");
        assertTrue(toolChangedNotified, "Observer should be notified of tool change.");
    }

    @Test
    void setState_unknownToolName_shouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> geoEngine.setState("UnknownTool"));
    }
    
    @Test
    void setState_toSameTool_shouldDoNothing() {
        String currentTool = geoEngine.getCurrentToolName(); // Should be "SelectTool"
        clearInvocations(mockObserver); // Clear previous notifications

        geoEngine.setState(currentTool); // Set to the same tool

        assertEquals(currentTool, geoEngine.getCurrentToolName());
        verify(mockObserver, never()).update(any(), any()); // No notifications if state doesn't change
        // Also, activate/deactivate on the state should not be called again. Harder to verify without spies.
    }

    // --- Selection Management Tests ---
    @Test
    void getSelectedShapes_initiallyEmpty() {
        assertTrue(geoEngine.getSelectedShapes().isEmpty());
    }

    @Test
    void getSelectedShape_singlePrimary_initiallyNull() {
        assertNull(geoEngine.getSelectedShape());
    }

    @Test
    void setSelectedShapes_shouldUpdateSelectionAndNotify() {
        List<Shape> newSelection = Arrays.asList(mockShape1, mockShape2);
        geoEngine.setSelectedShapes(newSelection);

        assertEquals(2, geoEngine.getSelectedShapes().size());
        assertTrue(geoEngine.getSelectedShapes().contains(mockShape1));
        assertTrue(geoEngine.getSelectedShapes().contains(mockShape2));
        assertEquals(mockShape1, geoEngine.getSelectedShape()); // Primary is the first

        ArgumentCaptor<Drawing.DrawingChangeEvent> eventCaptor = ArgumentCaptor.forClass(Drawing.DrawingChangeEvent.class);
        verify(mockObserver, atLeastOnce()).update(eq(geoEngine), eventCaptor.capture());
        
        boolean selectionEventFound = eventCaptor.getAllValues().stream()
            .filter(e -> e instanceof Drawing.DrawingChangeEvent)
            .map(e -> (Drawing.DrawingChangeEvent)e)
            .anyMatch(e -> e.type == Drawing.DrawingChangeEvent.ChangeType.SELECTION);
        assertTrue(selectionEventFound, "Selection change event should be notified.");
        
        verify(mockView, atLeastOnce()).render(); // Should trigger a render
    }
    
    @Test
    void setSelectedShapes_withNull_shouldClearSelectionAndNotify() {
        geoEngine.setSelectedShapes(Arrays.asList(mockShape1)); // Pre-select something
        clearInvocations(mockObserver, mockView);

        geoEngine.setSelectedShapes(null);

        assertTrue(geoEngine.getSelectedShapes().isEmpty());
        ArgumentCaptor<Drawing.DrawingChangeEvent> eventCaptor = ArgumentCaptor.forClass(Drawing.DrawingChangeEvent.class);
        verify(mockObserver, atLeastOnce()).update(eq(geoEngine), eventCaptor.capture());
         boolean selectionEventFound = eventCaptor.getAllValues().stream()
            .filter(e -> e instanceof Drawing.DrawingChangeEvent)
            .map(e -> (Drawing.DrawingChangeEvent)e)
            .anyMatch(e -> e.type == Drawing.DrawingChangeEvent.ChangeType.SELECTION);
        assertTrue(selectionEventFound);
        verify(mockView, atLeastOnce()).render();
    }

    @Test
    void clearSelection_whenNotEmpty_shouldClearAndNotify() {
        geoEngine.setSelectedShapes(Arrays.asList(mockShape1));
        assertFalse(geoEngine.getSelectedShapes().isEmpty());
        clearInvocations(mockObserver, mockView);

        geoEngine.clearSelection();

        assertTrue(geoEngine.getSelectedShapes().isEmpty());
        ArgumentCaptor<Drawing.DrawingChangeEvent> eventCaptor = ArgumentCaptor.forClass(Drawing.DrawingChangeEvent.class);
        verify(mockObserver, atLeastOnce()).update(eq(geoEngine), eventCaptor.capture());
        boolean selectionEventFound = eventCaptor.getAllValues().stream()
            .filter(e -> e instanceof Drawing.DrawingChangeEvent)
            .map(e -> (Drawing.DrawingChangeEvent)e)
            .anyMatch(e -> e.type == Drawing.DrawingChangeEvent.ChangeType.SELECTION);
        assertTrue(selectionEventFound);
        verify(mockView, atLeastOnce()).render();
    }

    @Test
    void clearSelection_whenAlreadyEmpty_shouldDoNothingAndNotNotify() {
        assertTrue(geoEngine.getSelectedShapes().isEmpty());
        clearInvocations(mockObserver, mockView);

        geoEngine.clearSelection();

        assertTrue(geoEngine.getSelectedShapes().isEmpty());
        verify(mockObserver, never()).update(eq(geoEngine), any(Drawing.DrawingChangeEvent.class));
        verify(mockView, never()).render();
    }

    @Test
    void addShapeToSelection_newShape_shouldAddAndNotify() {
        geoEngine.setSelectedShapes(new ArrayList<>(Arrays.asList(mockShape1))); // Initial selection
        clearInvocations(mockObserver, mockView);

        geoEngine.addShapeToSelection(mockShape2);

        assertEquals(2, geoEngine.getSelectedShapes().size());
        assertTrue(geoEngine.getSelectedShapes().contains(mockShape2));
        ArgumentCaptor<Drawing.DrawingChangeEvent> eventCaptor = ArgumentCaptor.forClass(Drawing.DrawingChangeEvent.class);
        verify(mockObserver, atLeastOnce()).update(eq(geoEngine), eventCaptor.capture());
        boolean selectionEventFound = eventCaptor.getAllValues().stream()
            .filter(e -> e instanceof Drawing.DrawingChangeEvent)
            .map(e -> (Drawing.DrawingChangeEvent)e)
            .anyMatch(e -> e.type == Drawing.DrawingChangeEvent.ChangeType.SELECTION);
        assertTrue(selectionEventFound);
        verify(mockView, atLeastOnce()).render();
    }

    @Test
    void addShapeToSelection_existingShape_shouldDoNothingAndNotNotify() {
        geoEngine.setSelectedShapes(Arrays.asList(mockShape1));
        clearInvocations(mockObserver, mockView);

        geoEngine.addShapeToSelection(mockShape1); // Try to add already selected shape

        assertEquals(1, geoEngine.getSelectedShapes().size());
        verify(mockObserver, never()).update(eq(geoEngine), any(Drawing.DrawingChangeEvent.class));
        verify(mockView, never()).render();
    }
    
    @Test
    void addShapeToSelection_nullShape_shouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> geoEngine.addShapeToSelection(null));
    }

    @Test
    void setSingleSelectedShape_shouldClearPreviousAndSelectNewAndNotify() {
        geoEngine.setSelectedShapes(Arrays.asList(mockShape1, mockShape2)); // Start with multi-selection
        clearInvocations(mockObserver, mockView);
        
        Shape mockShape3 = mock(Shape.class);
        geoEngine.setSingleSelectedShape(mockShape3);
        
        assertEquals(1, geoEngine.getSelectedShapes().size());
        assertSame(mockShape3, geoEngine.getSelectedShape());
        ArgumentCaptor<Drawing.DrawingChangeEvent> eventCaptor = ArgumentCaptor.forClass(Drawing.DrawingChangeEvent.class);
        verify(mockObserver, atLeastOnce()).update(eq(geoEngine), eventCaptor.capture());
        boolean selectionEventFound = eventCaptor.getAllValues().stream()
            .filter(e -> e instanceof Drawing.DrawingChangeEvent)
            .map(e -> (Drawing.DrawingChangeEvent)e)
            .anyMatch(e -> e.type == Drawing.DrawingChangeEvent.ChangeType.SELECTION);
        assertTrue(selectionEventFound);
        verify(mockView, atLeastOnce()).render();
    }

    @Test
    void setSingleSelectedShape_withNull_shouldClearSelectionAndNotify() {
        geoEngine.setSelectedShapes(Arrays.asList(mockShape1));
        clearInvocations(mockObserver, mockView);

        geoEngine.setSingleSelectedShape(null);

        assertTrue(geoEngine.getSelectedShapes().isEmpty());
        ArgumentCaptor<Drawing.DrawingChangeEvent> eventCaptor = ArgumentCaptor.forClass(Drawing.DrawingChangeEvent.class);
        verify(mockObserver, atLeastOnce()).update(eq(geoEngine), eventCaptor.capture());
         boolean selectionEventFound = eventCaptor.getAllValues().stream()
            .filter(e -> e instanceof Drawing.DrawingChangeEvent)
            .map(e -> (Drawing.DrawingChangeEvent)e)
            .anyMatch(e -> e.type == Drawing.DrawingChangeEvent.ChangeType.SELECTION);
        assertTrue(selectionEventFound);
        verify(mockView, atLeastOnce()).render();
    }

    // Test mouse event delegation to current tool state
    @Test
    void onMousePressed_shouldDelegateToCurrentToolState() {
        Point2D testPoint = new Point2D(1,1);
        // Get the actual SelectState instance created by GeoEngine
        ToolState currentSelectState = geoEngine.getToolStates().get("SelectTool");
        ToolState spiedSelectState = spy(currentSelectState);
        geoEngine.getToolStates().put("SelectTool", spiedSelectState); // Replace with spy
        geoEngine.setState("SelectTool"); // Ensure it's active

        geoEngine.onMousePressed(testPoint);
        verify(spiedSelectState, times(1)).onMousePressed(geoEngine, testPoint);
    }

    @Test
    void onMouseDragged_shouldDelegateToCurrentToolState() {
        Point2D testPoint = new Point2D(1,1);
        ToolState currentSelectState = geoEngine.getToolStates().get("SelectTool");
        ToolState spiedSelectState = spy(currentSelectState);
        geoEngine.getToolStates().put("SelectTool", spiedSelectState);
        geoEngine.setState("SelectTool");

        geoEngine.onMouseDragged(testPoint);
        verify(spiedSelectState, times(1)).onMouseDragged(geoEngine, testPoint);
    }

    @Test
    void onMouseReleased_shouldDelegateToCurrentToolState() {
        Point2D testPoint = new Point2D(1,1);
        ToolState currentSelectState = geoEngine.getToolStates().get("SelectTool");
        ToolState spiedSelectState = spy(currentSelectState);
        geoEngine.getToolStates().put("SelectTool", spiedSelectState);
        geoEngine.setState("SelectTool");
        
        geoEngine.onMouseReleased(testPoint);
        verify(spiedSelectState, times(1)).onMouseReleased(geoEngine, testPoint);
    }
    
    @Test
    void attemptToolFinishAction_delegatesToPolygonState() {
        // This test is more specific. We need to ensure current state is PolygonState
        // and then check if its tryFinishPolygonOnAction is called.
        // This requires either setting the internal map of states with a mock PolygonState
        // or spying on the real PolygonState instance if we can access it.

        // Get the actual PolygonState instance
        ToolState realPolygonState = geoEngine.getToolStates().get("PolygonTool");
        assertNotNull(realPolygonState, "PolygonTool state should exist in GeoEngine's map");
        assertTrue(realPolygonState instanceof PolygonState, "Fetched tool should be PolygonState");

        PolygonState spiedPolygonState = spy((PolygonState)realPolygonState);
        geoEngine.getToolStates().put("PolygonTool", spiedPolygonState); // Replace with spy
        geoEngine.setState("PolygonTool"); // Activate it

        geoEngine.attemptToolFinishAction();
        verify(spiedPolygonState, times(1)).tryFinishPolygonOnAction(geoEngine);
    }

    @Test
    void attemptToolFinishAction_whenNotPolygonState_doesNothing() {
        geoEngine.setState("SelectTool"); // Any state other than PolygonState
        ToolState currentSelectState = geoEngine.getToolStates().get("SelectTool");
        ToolState spiedSelectState = spy(currentSelectState);
        geoEngine.getToolStates().put("SelectTool", spiedSelectState);
        geoEngine.setState("SelectTool");


        assertDoesNotThrow(() -> geoEngine.attemptToolFinishAction());
        // Verify that no method like tryFinishPolygonOnAction was called on the SelectState (if it existed)
        // Or more simply, verify no exceptions and PolygonState's method wasn't called.
        // This is implicitly tested by the above specific test for PolygonState.
    }


    @Test
    void setIsShiftKeyPressed_and_isShiftKeyPressed_workCorrectly() {
        assertFalse(geoEngine.isShiftKeyPressed(), "Shift should be false initially.");
        geoEngine.setIsShiftKeyPressed(true);
        assertTrue(geoEngine.isShiftKeyPressed(), "Shift should be true after setting.");
        geoEngine.setIsShiftKeyPressed(false);
        assertFalse(geoEngine.isShiftKeyPressed(), "Shift should be false after resetting.");
    }
}

            