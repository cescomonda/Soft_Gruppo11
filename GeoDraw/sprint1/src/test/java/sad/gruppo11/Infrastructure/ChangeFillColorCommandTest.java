package sad.gruppo11.Infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.RectangleShape; // Classe concreta per mock che supporta fill
import sad.gruppo11.Model.LineSegment;    // Classe concreta per mock che NON supporta fill
import sad.gruppo11.Model.geometry.ColorData;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChangeFillColorCommandTest {

    private Shape mockRectangleShape; // Una forma che supporta il fill
    private Shape mockLineSegment;  // Una forma che NON supporta il fill
    private ColorData initialFillColor;
    private ColorData newTestFillColor;

    @BeforeEach
    void setUp() {
        mockRectangleShape = Mockito.mock(RectangleShape.class);
        when(mockRectangleShape.getId()).thenReturn(UUID.randomUUID());

        mockLineSegment = Mockito.mock(LineSegment.class);
        when(mockLineSegment.getId()).thenReturn(UUID.randomUUID());
        // Le linee non hanno colore di riempimento, quindi getFillColor() dovrebbe restituire null
        when(mockLineSegment.getFillColor()).thenReturn(null);


        initialFillColor = new ColorData(ColorData.GREEN);
        newTestFillColor = new ColorData(ColorData.YELLOW);

        // Configura mockRectangleShape per restituire initialFillColor
        when(mockRectangleShape.getFillColor()).thenReturn(initialFillColor);
    }

    @Test
    @DisplayName("Costruttore con Shape e newColor valido dovrebbe inizializzare correttamente")
    void testConstructorValid() {
        ChangeFillColorCommand command = new ChangeFillColorCommand(mockRectangleShape, newTestFillColor);
        // Il test implicito è che non ci siano eccezioni e che newFillColor sia clonato (verificato in execute)
        assertThat(command).isNotNull();
    }

    @Test
    @DisplayName("Costruttore con newColor nullo dovrebbe impostare newFillColor a TRASPARENTE")
    void testConstructorNullNewColor() {
        ChangeFillColorCommand command = new ChangeFillColorCommand(mockRectangleShape, null);
        // Verifichiamo questo comportamento tramite execute
        command.execute();
        ArgumentCaptor<ColorData> colorCaptor = ArgumentCaptor.forClass(ColorData.class);
        verify(mockRectangleShape, atLeastOnce()).setFillColor(colorCaptor.capture());
        
        // Trova il colore passato a setFillColor in execute
        ColorData appliedColor = colorCaptor.getAllValues().stream()
            .filter(c -> c.equals(ColorData.TRANSPARENT)) // Cerca il colore trasparente
            .findFirst()
            .orElse(null);

        assertThat(appliedColor).isNotNull();
        assertThat(appliedColor).isEqualTo(ColorData.TRANSPARENT);
    }


    @Test
    @DisplayName("Il costruttore dovrebbe lanciare NullPointerException per targetShape nullo")
    void testConstructorNullShape() {
        assertThatThrownBy(() -> new ChangeFillColorCommand(null, newTestFillColor))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Target shape cannot be null");
    }

    @Test
    @DisplayName("captureOldColor dovrebbe memorizzare una copia del colore di riempimento attuale")
    void testCaptureOldColor() {
        // mockRectangleShape.getFillColor() restituisce initialFillColor (GREEN)
        ChangeFillColorCommand command = new ChangeFillColorCommand(mockRectangleShape, newTestFillColor);
        command.captureOldColor(); // Cattura esplicita

        command.execute(); // Applica YELLOW
        reset(mockRectangleShape);
        when(mockRectangleShape.getFillColor()).thenReturn(newTestFillColor); // Simula che la forma ora abbia YELLOW

        command.undo();

        ArgumentCaptor<ColorData> colorCaptor = ArgumentCaptor.forClass(ColorData.class);
        verify(mockRectangleShape).setFillColor(colorCaptor.capture());
        assertThat(colorCaptor.getValue()).isEqualTo(initialFillColor); // Undo dovrebbe usare GREEN
        assertThat(colorCaptor.getValue()).isNotSameAs(initialFillColor); // Copia
    }

    @Test
    @DisplayName("captureOldColor dovrebbe impostare oldFillColor a null se getFillColor restituisce null")
    void testCaptureOldColorWhenOriginalIsNull() {
        when(mockRectangleShape.getFillColor()).thenReturn(null); // Simula originale null
        ChangeFillColorCommand command = new ChangeFillColorCommand(mockRectangleShape, newTestFillColor);
        command.captureOldColor();

        command.execute(); // Applica YELLOW
        reset(mockRectangleShape);
        when(mockRectangleShape.getFillColor()).thenReturn(newTestFillColor);

        command.undo(); // Dovrebbe tentare di impostare a null

        ArgumentCaptor<ColorData> colorCaptor = ArgumentCaptor.forClass(ColorData.class);
        verify(mockRectangleShape).setFillColor(colorCaptor.capture());
        assertThat(colorCaptor.getValue()).isNull();
    }

    @Test
    @DisplayName("execute su RectangleShape dovrebbe chiamare setFillColor con newColor")
    void testExecuteOnRectangleShape() {
        ChangeFillColorCommand command = new ChangeFillColorCommand(mockRectangleShape, newTestFillColor);
        command.execute(); // oldColor (GREEN) catturato, setFillColor(newTestFillColor=YELLOW)

        ArgumentCaptor<ColorData> colorCaptor = ArgumentCaptor.forClass(ColorData.class);
        verify(mockRectangleShape).setFillColor(colorCaptor.capture());
        assertThat(colorCaptor.getValue()).isEqualTo(newTestFillColor); // YELLOW
    }

    @Test
    @DisplayName("execute su LineSegment NON dovrebbe chiamare setFillColor")
    void testExecuteOnLineSegment() {
        ChangeFillColorCommand command = new ChangeFillColorCommand(mockLineSegment, newTestFillColor);
        // LineSegment.getFillColor() mockato per restituire null
        
        command.execute(); // oldColor (null) catturato, ma setFillColor non dovrebbe essere chiamato

        verify(mockLineSegment, never()).setFillColor(any(ColorData.class));
    }


    @Test
    @DisplayName("undo su RectangleShape dovrebbe chiamare setFillColor con oldColor")
    void testUndoOnRectangleShape() {
        ChangeFillColorCommand command = new ChangeFillColorCommand(mockRectangleShape, newTestFillColor);
        command.execute(); // Cattura GREEN, applica YELLOW
        command.undo();    // Ripristina GREEN

        ArgumentCaptor<ColorData> colorCaptor = ArgumentCaptor.forClass(ColorData.class);
        verify(mockRectangleShape, times(2)).setFillColor(colorCaptor.capture());

        List<ColorData> capturedColors = colorCaptor.getAllValues();
        assertThat(capturedColors.get(0)).isEqualTo(newTestFillColor); // Da execute (YELLOW)
        assertThat(capturedColors.get(1)).isEqualTo(initialFillColor); // Da undo (GREEN)
    }

    @Test
    @DisplayName("undo su LineSegment NON dovrebbe chiamare setFillColor")
    void testUndoOnLineSegment() {
        ChangeFillColorCommand command = new ChangeFillColorCommand(mockLineSegment, newTestFillColor);
        command.execute(); // Non fa nulla per il fill
        command.undo();    // Non fa nulla per il fill

        verify(mockLineSegment, never()).setFillColor(any(ColorData.class));
    }
    
    @Test
    @DisplayName("toString dovrebbe generare una stringa rappresentativa")
    void testToStringOutput() {
        ChangeFillColorCommand command = new ChangeFillColorCommand(mockRectangleShape, newTestFillColor);
        String strBeforeExecute = command.toString();
        assertThat(strBeforeExecute).contains("shapeId=" + mockRectangleShape.getId());
        assertThat(strBeforeExecute).contains("newColor=" + newTestFillColor.toString());
        assertThat(strBeforeExecute).contains("oldColor=not captured");
        assertThat(strBeforeExecute).contains("isLine=false");

        command.execute();
        String strAfterExecute = command.toString();
        assertThat(strAfterExecute).contains("oldColor=" + initialFillColor.toString());

        // Test con LineSegment
        ChangeFillColorCommand lineCommand = new ChangeFillColorCommand(mockLineSegment, ColorData.RED);
        assertThat(lineCommand.toString()).contains("isLine=true");
        lineCommand.execute(); // oldFillColor (null) per linea viene catturato
        assertThat(lineCommand.toString()).contains("oldColor=null (captured)");

    }
}