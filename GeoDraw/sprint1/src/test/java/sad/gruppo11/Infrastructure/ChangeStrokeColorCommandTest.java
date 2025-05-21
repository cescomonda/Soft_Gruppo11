package sad.gruppo11.Infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.EllipseShape; // Classe concreta per mock
import sad.gruppo11.Model.geometry.ColorData;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChangeStrokeColorCommandTest {

    private Shape mockTargetShape;
    private ColorData initialStrokeColor;
    private ColorData newTestStrokeColor;
    private ChangeStrokeColorCommand command;

    @BeforeEach
    void setUp() {
        mockTargetShape = Mockito.mock(EllipseShape.class);
        when(mockTargetShape.getId()).thenReturn(UUID.randomUUID()); // Per toString

        initialStrokeColor = ColorData.RED; // Colore iniziale fittizio
        newTestStrokeColor = ColorData.BLUE;   // Nuovo colore fittizio

        // Configura il mockTargetShape per restituire initialStrokeColor quando getStrokeColor() è chiamato
        when(mockTargetShape.getStrokeColor()).thenReturn(initialStrokeColor);

        command = new ChangeStrokeColorCommand(mockTargetShape, newTestStrokeColor);
    }

    @Test
    @DisplayName("Il costruttore dovrebbe lanciare NullPointerException per argomenti nulli")
    void testConstructorNullArguments() {
        assertThatThrownBy(() -> new ChangeStrokeColorCommand(null, newTestStrokeColor))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Target shape cannot be null");

        assertThatThrownBy(() -> new ChangeStrokeColorCommand(mockTargetShape, null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("New stroke color cannot be null");
    }

    @Test
    @DisplayName("Il costruttore dovrebbe creare una copia difensiva di newStrokeColor")
    void testConstructorDefensiveCopyOfNewColor() {
        ColorData originalNewColor = new ColorData(ColorData.GREEN);
        ChangeStrokeColorCommand cmd = new ChangeStrokeColorCommand(mockTargetShape, originalNewColor);

        originalNewColor.setR(0); // Modifica il colore originale

        cmd.execute(); // Questo chiamerà setStrokeColor con la copia interna

        ArgumentCaptor<ColorData> colorCaptor = ArgumentCaptor.forClass(ColorData.class);
        verify(mockTargetShape, atLeastOnce()).setStrokeColor(colorCaptor.capture());

        // Cerca la chiamata che usa il colore non modificato
        ColorData usedNewColor = colorCaptor.getAllValues().stream()
            .filter(c -> c.getR() == ColorData.GREEN.getR()) // Cerca il verde originale
            .findFirst()
            .orElse(null);
        
        assertThat(usedNewColor).isNotNull();
        assertThat(usedNewColor.getR()).isEqualTo(ColorData.GREEN.getR());
    }

    @Test
    @DisplayName("captureOldColor dovrebbe memorizzare una copia del colore di tratto attuale della shape")
    void testCaptureOldColor() {
        // initialStrokeColor (RED) è già configurato per mockTargetShape.getStrokeColor()
        command.captureOldColor(); // Chiamata esplicita

        // Per verificare, eseguiamo e poi annulliamo
        command.execute(); // Applica BLUE
        reset(mockTargetShape); // Resetta interazioni per la verifica di undo
        // Configura getStrokeColor per restituire BLUE dopo execute, se necessario per altri test,
        // ma per questo test di undo, oldStrokeColor è già catturato.
        when(mockTargetShape.getStrokeColor()).thenReturn(newTestStrokeColor);

        command.undo();

        ArgumentCaptor<ColorData> colorCaptor = ArgumentCaptor.forClass(ColorData.class);
        verify(mockTargetShape).setStrokeColor(colorCaptor.capture());
        assertThat(colorCaptor.getValue()).isEqualTo(initialStrokeColor); // Undo dovrebbe usare RED
        assertThat(colorCaptor.getValue()).isNotSameAs(initialStrokeColor); // Deve essere una copia
    }
    
    @Test
    @DisplayName("captureOldColor dovrebbe impostare oldStrokeColor a null se getStrokeColor restituisce null")
    void testCaptureOldColorWhenOriginalIsNull() {
        when(mockTargetShape.getStrokeColor()).thenReturn(null); // Simula che il colore originale sia null
        command.captureOldColor();

        command.execute(); // Applica newTestStrokeColor (BLUE)
        reset(mockTargetShape);
        when(mockTargetShape.getStrokeColor()).thenReturn(newTestStrokeColor); // La forma ora ha il nuovo colore

        command.undo(); // Dovrebbe tentare di impostare il colore a null

        ArgumentCaptor<ColorData> colorCaptor = ArgumentCaptor.forClass(ColorData.class);
        verify(mockTargetShape).setStrokeColor(colorCaptor.capture());
        assertThat(colorCaptor.getValue()).isNull(); // Verifica che l'undo imposti a null
    }


    @Test
    @DisplayName("execute dovrebbe catturare oldColor (se non già fatto) e chiamare setStrokeColor sulla Shape con newColor")
    void testExecute() {
        // mockTargetShape.getStrokeColor() restituisce initialStrokeColor (RED)
        command.execute(); // oldColor diventa copia di RED, shape.setStrokeColor(BLUE)

        ArgumentCaptor<ColorData> colorCaptor = ArgumentCaptor.forClass(ColorData.class);
        verify(mockTargetShape).setStrokeColor(colorCaptor.capture());

        assertThat(colorCaptor.getValue()).isEqualTo(newTestStrokeColor); // Deve essere BLUE
        assertThat(colorCaptor.getValue()).isNotSameAs(newTestStrokeColor); // Copia difensiva di newColor nel comando
    }

    @Test
    @DisplayName("undo dovrebbe chiamare setStrokeColor sulla Shape con oldColor dopo execute")
    void testUndoAfterExecute() {
        // 1. Stato iniziale: mockTargetShape.getStrokeColor() restituisce initialStrokeColor (RED)
        command.execute(); // oldColor (RED) catturato, setStrokeColor(newTestStrokeColor=BLUE) chiamato

        // 2. Ora la forma "ha" BLUE. L'undo dovrebbe ripristinare RED.
        command.undo();

        ArgumentCaptor<ColorData> colorCaptor = ArgumentCaptor.forClass(ColorData.class);
        verify(mockTargetShape, times(2)).setStrokeColor(colorCaptor.capture());

        List<ColorData> capturedColors = colorCaptor.getAllValues();
        assertThat(capturedColors.get(0)).isEqualTo(newTestStrokeColor); // Da execute (BLUE)
        assertThat(capturedColors.get(1)).isEqualTo(initialStrokeColor); // Da undo (RED)
        assertThat(capturedColors.get(1)).isNotSameAs(initialStrokeColor); // Copia difensiva di oldColor
    }

    @Test
    @DisplayName("Se captureOldColor è chiamato esplicitamente, execute non dovrebbe ricatturarlo con un valore diverso")
    void testExecuteAfterExplicitCaptureOldColor() {
        // mockTargetShape.getStrokeColor() restituisce initialStrokeColor (RED)
        command.captureOldColor(); // Cattura esplicita di RED

        // Simula che il colore della forma sia cambiato *dopo* la cattura esplicita ma *prima* di execute.
        ColorData intermediateColor = ColorData.GREEN;
        when(mockTargetShape.getStrokeColor()).thenReturn(intermediateColor);

        command.execute(); // Dovrebbe usare oldColor già catturato (RED) e applicare newTestStrokeColor (BLUE)

        command.undo(); // Dovrebbe ripristinare oldColor originale (RED)

        ArgumentCaptor<ColorData> colorCaptor = ArgumentCaptor.forClass(ColorData.class);
        verify(mockTargetShape, times(2)).setStrokeColor(colorCaptor.capture());
        List<ColorData> allValues = colorCaptor.getAllValues();

        assertThat(allValues.get(0)).isEqualTo(newTestStrokeColor); // Da execute (BLUE)
        assertThat(allValues.get(1)).isEqualTo(initialStrokeColor); // Da undo (RED originale)
    }

    @Test
    @DisplayName("toString dovrebbe includere il nuovo e il vecchio colore (se catturato)")
    void testToString() {
        // Caso 1: oldColor non ancora catturato (solo dopo execute o captureOldColor esplicita)
        String initialToString = command.toString();
        assertThat(initialToString).contains("newColor=" + newTestStrokeColor.toString());
        assertThat(initialToString).contains("oldColor=not captured");

        // Caso 2: oldColor catturato
        command.execute(); // Questo catturerà oldColor
        String afterExecuteToString = command.toString();
        assertThat(afterExecuteToString).contains("newColor=" + newTestStrokeColor.toString());
        assertThat(afterExecuteToString).contains("oldColor=" + initialStrokeColor.toString());
        
        // Caso 3: oldColor catturato come null
        reset(mockTargetShape); // Resetta mock
        when(mockTargetShape.getId()).thenReturn(UUID.randomUUID());
        when(mockTargetShape.getStrokeColor()).thenReturn(null); // Colore originale è null
        ChangeStrokeColorCommand cmdWithNullOld = new ChangeStrokeColorCommand(mockTargetShape, newTestStrokeColor);
        cmdWithNullOld.execute();
        assertThat(cmdWithNullOld.toString()).contains("oldColor=null (captured)");
    }
}