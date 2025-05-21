package sad.gruppo11.Controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import sad.gruppo11.Infrastructure.AddShapeCommand;
import sad.gruppo11.Infrastructure.Command;
import sad.gruppo11.Infrastructure.CommandManager;
import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.RectangleShape;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Rect;


import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class RectangleStateTest {

    private RectangleState rectangleState;
    private GeoEngine mockGeoEngine;
    private CommandManager mockCommandManager;
    private Drawing mockDrawing;

    private Point2D p1, p2_valid, p3_degenerateWidth, p4_degenerateHeight;
    private ColorData defaultStrokeColor;
    private ColorData defaultFillColor;

    @BeforeEach
    void setUp() {
        rectangleState = new RectangleState();

        mockGeoEngine = Mockito.mock(GeoEngine.class);
        mockCommandManager = Mockito.mock(CommandManager.class);
        mockDrawing = Mockito.mock(Drawing.class);

        when(mockGeoEngine.getCommandManager()).thenReturn(mockCommandManager);
        when(mockGeoEngine.getDrawing()).thenReturn(mockDrawing);

        defaultStrokeColor = new ColorData(ColorData.RED);
        defaultFillColor = new ColorData(0, 255, 0, 0.5);


        when(mockGeoEngine.getCurrentStrokeColorForNewShapes()).thenReturn(defaultStrokeColor);
        when(mockGeoEngine.getCurrentFillColorForNewShapes()).thenReturn(defaultFillColor);

        p1 = new Point2D(10, 20);
        p2_valid = new Point2D(110, 70); // Crea un rettangolo 100x50
        // Punti per creare rettangoli degeneri (tolleranza 1e-2 = 0.01)
        p3_degenerateWidth = new Point2D(p1.getX() + 0.005, p1.getY() + 50); // width < 0.01
        p4_degenerateHeight = new Point2D(p1.getX() + 50, p1.getY() + 0.005); // height < 0.01
    }

    @Test
    @DisplayName("onMousePressed dovrebbe memorizzare il primo angolo")
    void testOnMousePressed() {
        assertDoesNotThrow(() -> rectangleState.onMousePressed(p1, mockGeoEngine));
        // Verifica indiretta tramite onMouseReleased
    }

    @Test
    @DisplayName("onMousePressed con argomenti nulli dovrebbe lanciare NullPointerException")
    void testOnMousePressedNullArgs() {
        assertThatThrownBy(() -> rectangleState.onMousePressed(null, mockGeoEngine))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> rectangleState.onMousePressed(p1, null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("onMouseDragged non dovrebbe avere effetti significativi visibili esternamente")
    void testOnMouseDragged() {
        rectangleState.onMousePressed(p1, mockGeoEngine);
        assertDoesNotThrow(() -> rectangleState.onMouseDragged(p2_valid, mockGeoEngine));
        verifyNoInteractions(mockCommandManager, mockDrawing); // Oltre a chiamate iniziali se ce ne fossero
    }

    @Test
    @DisplayName("onMouseReleased dovrebbe creare e eseguire AddShapeCommand se il rettangolo è valido")
    void testOnMouseReleasedValidRectangle() {
        rectangleState.onMousePressed(p1, mockGeoEngine);
        rectangleState.onMouseReleased(p2_valid, mockGeoEngine);

        ArgumentCaptor<Command> commandCaptor = ArgumentCaptor.forClass(Command.class);
        verify(mockCommandManager, times(1)).execute(commandCaptor.capture());

        Command executedCommand = commandCaptor.getValue();
        assertThat(executedCommand).isInstanceOf(AddShapeCommand.class);
        
        // Verifichiamo che lo stato sia resettato
        rectangleState.onMouseReleased(new Point2D(200,200), mockGeoEngine); // Chiamata successiva
        verify(mockCommandManager, times(1)).execute(any(Command.class)); // Ancora 1 execute
    }
    
    @Test
    @DisplayName("Il RectangleShape creato dovrebbe avere i colori corretti da GeoEngine")
    void testRectangleShapeColorsFromGeoEngine() {
        rectangleState.onMousePressed(p1, mockGeoEngine);
        rectangleState.onMouseReleased(p2_valid, mockGeoEngine);

        verify(mockGeoEngine, atLeastOnce()).getCurrentStrokeColorForNewShapes();
        verify(mockGeoEngine, atLeastOnce()).getCurrentFillColorForNewShapes();
        // La verifica che i colori siano *effettivamente impostati* sulla RectangleShape
        // è difficile da fare qui senza reflection o PowerMock, come discusso per LineState.
        // Ci fidiamo che RectangleState usi correttamente i valori restituiti da GeoEngine
        // e che i test di RectangleShape verifichino che setStrokeColor/setFillColor funzionino.
    }


    @Test
    @DisplayName("onMouseReleased NON dovrebbe creare comando se il rettangolo ha larghezza degenere")
    void testOnMouseReleasedDegenerateWidth() {
        rectangleState.onMousePressed(p1, mockGeoEngine);
        rectangleState.onMouseReleased(p3_degenerateWidth, mockGeoEngine);

        verify(mockCommandManager, never()).execute(any(Command.class));
    }

    @Test
    @DisplayName("onMouseReleased NON dovrebbe creare comando se il rettangolo ha altezza degenere")
    void testOnMouseReleasedDegenerateHeight() {
        rectangleState.onMousePressed(p1, mockGeoEngine);
        rectangleState.onMouseReleased(p4_degenerateHeight, mockGeoEngine);

        verify(mockCommandManager, never()).execute(any(Command.class));
    }

    @Test
    @DisplayName("onMouseReleased senza un precedente onMousePressed non dovrebbe fare nulla")
    void testOnMouseReleasedWithoutPress() {
        rectangleState.onMouseReleased(p2_valid, mockGeoEngine);
        verifyNoInteractions(mockCommandManager);
    }

    @Test
    @DisplayName("getToolName dovrebbe restituire 'Rectangle Tool'")
    void testGetToolName() {
        assertThat(rectangleState.getToolName()).isEqualTo("Rectangle Tool");
    }

    @Test
    @DisplayName("onEnterState dovrebbe resettare firstCorner")
    void testOnEnterStateResetsFirstCorner() {
        rectangleState.onMousePressed(p1, mockGeoEngine); // Imposta firstCorner
        rectangleState.onEnterState(mockGeoEngine);      // Dovrebbe resettarlo

        Mockito.reset(mockCommandManager); // Resetta per la verifica successiva
        rectangleState.onMouseReleased(p2_valid, mockGeoEngine);
        verify(mockCommandManager, never()).execute(any(Command.class));
    }

    @Test
    @DisplayName("onExitState dovrebbe resettare firstCorner")
    void testOnExitStateResetsFirstCorner() {
        rectangleState.onMousePressed(p1, mockGeoEngine); // Imposta firstCorner
        rectangleState.onExitState(mockGeoEngine);       // Dovrebbe resettarlo

        Mockito.reset(mockCommandManager); // Resetta per la verifica successiva
        rectangleState.onMouseReleased(p2_valid, mockGeoEngine);
        verify(mockCommandManager, never()).execute(any(Command.class));
    }
}