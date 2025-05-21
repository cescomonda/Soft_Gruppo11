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
import sad.gruppo11.Model.EllipseShape; // Corretto per Ellipse
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Rect;


import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class EllipseStateTest {

    private EllipseState ellipseState;
    private GeoEngine mockGeoEngine;
    private CommandManager mockCommandManager;
    private Drawing mockDrawing;

    private Point2D p1, p2_valid, p3_degenerateWidth, p4_degenerateHeight;
    private ColorData defaultStrokeColor;
    private ColorData defaultFillColor;

    @BeforeEach
    void setUp() {
        ellipseState = new EllipseState();

        mockGeoEngine = Mockito.mock(GeoEngine.class);
        mockCommandManager = Mockito.mock(CommandManager.class);
        mockDrawing = Mockito.mock(Drawing.class);

        when(mockGeoEngine.getCommandManager()).thenReturn(mockCommandManager);
        when(mockGeoEngine.getDrawing()).thenReturn(mockDrawing);

        defaultStrokeColor = new ColorData(ColorData.BLUE); // Colore diverso per differenziare
        defaultFillColor = new ColorData(255, 255, 0, 0.5);
        

        when(mockGeoEngine.getCurrentStrokeColorForNewShapes()).thenReturn(defaultStrokeColor);
        when(mockGeoEngine.getCurrentFillColorForNewShapes()).thenReturn(defaultFillColor);

        p1 = new Point2D(5, 15);
        p2_valid = new Point2D(55, 115); // Crea un'ellisse con bounds 50x100
        // Punti per creare ellissi degeneri (tolleranza 1e-3 = 0.001)
        p3_degenerateWidth = new Point2D(p1.getX() + 0.0005, p1.getY() + 50); // width < 0.001
        p4_degenerateHeight = new Point2D(p1.getX() + 50, p1.getY() + 0.0005); // height < 0.001
    }

    @Test
    @DisplayName("onMousePressed dovrebbe memorizzare il primo angolo")
    void testOnMousePressed() {
        assertDoesNotThrow(() -> ellipseState.onMousePressed(p1, mockGeoEngine));
    }

    @Test
    @DisplayName("onMousePressed con argomenti nulli dovrebbe lanciare NullPointerException")
    void testOnMousePressedNullArgs() {
        assertThatThrownBy(() -> ellipseState.onMousePressed(null, mockGeoEngine))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> ellipseState.onMousePressed(p1, null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("onMouseDragged non dovrebbe avere effetti significativi visibili esternamente")
    void testOnMouseDragged() {
        ellipseState.onMousePressed(p1, mockGeoEngine);
        assertDoesNotThrow(() -> ellipseState.onMouseDragged(p2_valid, mockGeoEngine));
        verifyNoInteractions(mockCommandManager, mockDrawing);
    }

    @Test
    @DisplayName("onMouseReleased dovrebbe creare e eseguire AddShapeCommand se l'ellisse è valida")
    void testOnMouseReleasedValidEllipse() {
        ellipseState.onMousePressed(p1, mockGeoEngine);
        ellipseState.onMouseReleased(p2_valid, mockGeoEngine);

        ArgumentCaptor<Command> commandCaptor = ArgumentCaptor.forClass(Command.class);
        verify(mockCommandManager, times(1)).execute(commandCaptor.capture());

        Command executedCommand = commandCaptor.getValue();
        assertThat(executedCommand).isInstanceOf(AddShapeCommand.class);
        
        // Verifichiamo che lo stato sia resettato
        ellipseState.onMouseReleased(new Point2D(200,200), mockGeoEngine); // Chiamata successiva
        verify(mockCommandManager, times(1)).execute(any(Command.class)); // Ancora 1 execute
    }
    
    @Test
    @DisplayName("L'EllipseShape creata dovrebbe avere i colori corretti da GeoEngine")
    void testEllipseShapeColorsFromGeoEngine() {
        ellipseState.onMousePressed(p1, mockGeoEngine);
        ellipseState.onMouseReleased(p2_valid, mockGeoEngine);

        verify(mockGeoEngine, atLeastOnce()).getCurrentStrokeColorForNewShapes();
        verify(mockGeoEngine, atLeastOnce()).getCurrentFillColorForNewShapes();
        // Come per gli altri stati, la verifica precisa che i colori *sulla shape* siano corretti
        // è più un test di integrazione o richiede un accesso che non abbiamo qui.
    }


    @Test
    @DisplayName("onMouseReleased NON dovrebbe creare comando se l'ellisse ha larghezza degenere")
    void testOnMouseReleasedDegenerateWidth() {
        ellipseState.onMousePressed(p1, mockGeoEngine);
        ellipseState.onMouseReleased(p3_degenerateWidth, mockGeoEngine);

        verify(mockCommandManager, never()).execute(any(Command.class));
    }

    @Test
    @DisplayName("onMouseReleased NON dovrebbe creare comando se l'ellisse ha altezza degenere")
    void testOnMouseReleasedDegenerateHeight() {
        ellipseState.onMousePressed(p1, mockGeoEngine);
        ellipseState.onMouseReleased(p4_degenerateHeight, mockGeoEngine);

        verify(mockCommandManager, never()).execute(any(Command.class));
    }

    @Test
    @DisplayName("onMouseReleased senza un precedente onMousePressed non dovrebbe fare nulla")
    void testOnMouseReleasedWithoutPress() {
        ellipseState.onMouseReleased(p2_valid, mockGeoEngine);
        verifyNoInteractions(mockCommandManager);
    }

    @Test
    @DisplayName("getToolName dovrebbe restituire 'Ellipse Tool'")
    void testGetToolName() {
        assertThat(ellipseState.getToolName()).isEqualTo("Ellipse Tool");
    }

    @Test
    @DisplayName("onEnterState dovrebbe resettare firstCorner")
    void testOnEnterStateResetsFirstCorner() {
        ellipseState.onMousePressed(p1, mockGeoEngine); 
        ellipseState.onEnterState(mockGeoEngine);      

        Mockito.reset(mockCommandManager); 
        ellipseState.onMouseReleased(p2_valid, mockGeoEngine);
        verify(mockCommandManager, never()).execute(any(Command.class));
    }

    @Test
    @DisplayName("onExitState dovrebbe resettare firstCorner")
    void testOnExitStateResetsFirstCorner() {
        ellipseState.onMousePressed(p1, mockGeoEngine); 
        ellipseState.onExitState(mockGeoEngine);       

        Mockito.reset(mockCommandManager); 
        ellipseState.onMouseReleased(p2_valid, mockGeoEngine);
        verify(mockCommandManager, never()).execute(any(Command.class));
    }
}