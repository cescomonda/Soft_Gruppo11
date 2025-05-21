package sad.gruppo11.Infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mockito;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.RectangleShape; // Usiamo una classe concreta per mockare Shape

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClipboardTest {

    private Clipboard clipboard1;
    private Clipboard clipboard2;
    private Shape mockShape;
    private Shape clonedMockShapeInstance1;
    private Shape clonedMockShapeInstance2;

    @BeforeEach
    void setUp() {
        // Ottieni istanze del singleton. Dovrebbero essere la stessa.
        clipboard1 = Clipboard.getInstance();
        clipboard2 = Clipboard.getInstance();

        // Pulisci il clipboard prima di ogni test per isolarli
        clipboard1.clear();

        // Crea un mock di Shape e configura il suo comportamento di clonazione
        mockShape = Mockito.mock(RectangleShape.class); // Usa una classe concreta

        // Quando mockShape.cloneShape() è chiamato, vogliamo che restituisca istanze diverse
        // per simulare correttamente la clonazione.
        clonedMockShapeInstance1 = Mockito.mock(RectangleShape.class);
        clonedMockShapeInstance2 = Mockito.mock(RectangleShape.class);

        // Configura mockShape.cloneShape() per restituire cloni diversi in sequenza
        // o la stessa istanza "clonata" se i test non dipendono da istanze diverse dal clone.
        // Per i test di get(), è importante che restituisca una "nuova" copia.
        when(mockShape.cloneShape()).thenReturn(clonedMockShapeInstance1, clonedMockShapeInstance2);

        // Se vuoi che i cloni abbiano le stesse "proprietà" del mockShape per altri test (es. equals)
        // when(clonedMockShapeInstance1.getId()).thenReturn(mockShape.getId()); // Esempio
        // when(clonedMockShapeInstance2.getId()).thenReturn(mockShape.getId()); // Esempio
    }

    @Test
    @DisplayName("getInstance dovrebbe restituire sempre la stessa istanza (Singleton)")
    void testGetInstanceIsSingleton() {
        assertThat(clipboard1).isSameAs(clipboard2);
    }

    @Test
    @DisplayName("Il clipboard dovrebbe essere vuoto inizialmente (dopo clear in setUp)")
    void testInitialClipboardIsEmpty() {
        assertThat(clipboard1.isEmpty()).isTrue();
        assertThat(clipboard1.get()).isNull();
    }

    @Test
    @DisplayName("set dovrebbe memorizzare un clone della forma")
    void testSetStoresClone() {
        clipboard1.set(mockShape);

        // Verifica che cloneShape() sia stato chiamato su mockShape quando set viene invocato
        verify(mockShape, times(1)).cloneShape();
        assertThat(clipboard1.isEmpty()).isFalse();
    }

    @Test
    @DisplayName("set con forma nulla dovrebbe svuotare il clipboard")
    void testSetNullShape() {
        clipboard1.set(mockShape); // Metti qualcosa dentro prima
        assertThat(clipboard1.isEmpty()).isFalse();

        clipboard1.set(null);
        assertThat(clipboard1.isEmpty()).isTrue();
        assertThat(clipboard1.get()).isNull();
    }

    @Test
    @DisplayName("get dovrebbe restituire un clone della forma memorizzata")
    void testGetReturnsClone() {
        // mockShape.cloneShape() è configurato per restituire clonedMockShapeInstance1 la prima volta
        clipboard1.set(mockShape); // Questo chiama mockShape.cloneShape() una volta (risultato: clonedMockShapeInstance1 memorizzato)

        // Ora, clipboard.get() dovrebbe chiamare cloneShape() sull'istanza *memorizzata* (clonedMockShapeInstance1).
        // Dobbiamo configurare clonedMockShapeInstance1 per comportarsi come un clone.
        Shape internalClone = clonedMockShapeInstance1; // Questo è ciò che è dentro il clipboard
        Shape freshlyClonedForGet = Mockito.mock(RectangleShape.class); // Questo sarà restituito da get()
        when(internalClone.cloneShape()).thenReturn(freshlyClonedForGet);


        Shape retrievedShape1 = clipboard1.get();

        // Verifica che il metodo get chiami cloneShape sull'oggetto INTERNO al clipboard.
        verify(internalClone, times(1)).cloneShape();

        assertThat(retrievedShape1).isNotNull();
        assertThat(retrievedShape1).isSameAs(freshlyClonedForGet); // Deve essere l'istanza che abbiamo detto a internalClone.cloneShape() di restituire
        assertThat(retrievedShape1).isNotSameAs(mockShape); // Non l'originale
        assertThat(retrievedShape1).isNotSameAs(internalClone); // Non l'istanza interna, ma un suo clone

        // Chiamata successiva a get()
        Shape evenFresherCloneForGet = Mockito.mock(RectangleShape.class);
        when(internalClone.cloneShape()).thenReturn(evenFresherCloneForGet); // Riconfigura per la seconda chiamata a get

        Shape retrievedShape2 = clipboard1.get();
        verify(internalClone, times(2)).cloneShape(); // Ora chiamato due volte su internalClone
        assertThat(retrievedShape2).isNotNull();
        assertThat(retrievedShape2).isSameAs(evenFresherCloneForGet);
        assertThat(retrievedShape2).isNotSameAs(retrievedShape1); // Deve essere una nuova istanza
    }


    @Test
    @DisplayName("get su clipboard vuoto dovrebbe restituire null")
    void testGetOnEmptyClipboard() {
        assertThat(clipboard1.isEmpty()).isTrue();
        assertThat(clipboard1.get()).isNull();
    }

    @Test
    @DisplayName("isEmpty dovrebbe restituire true se vuoto, false altrimenti")
    void testIsEmpty() {
        assertThat(clipboard1.isEmpty()).isTrue();
        clipboard1.set(mockShape);
        assertThat(clipboard1.isEmpty()).isFalse();
        clipboard1.set(null);
        assertThat(clipboard1.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("clear dovrebbe svuotare il clipboard")
    void testClear() {
        clipboard1.set(mockShape);
        assertThat(clipboard1.isEmpty()).isFalse();

        clipboard1.clear();
        assertThat(clipboard1.isEmpty()).isTrue();
        assertThat(clipboard1.get()).isNull();
    }
}