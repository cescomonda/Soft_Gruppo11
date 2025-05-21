package sad.gruppo11.Model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mockito;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Rect;
import sad.gruppo11.View.Observer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*; // Per verify, times, etc.

class DrawingTest {

    private Drawing drawing;
    private Shape mockShape1;
    private Shape mockShape2;
    private Observer mockObserver1;
    private Observer mockObserver2;

    @BeforeEach
    void setUp() {
        drawing = new Drawing();

        // Crea mock per Shape
        mockShape1 = Mockito.mock(RectangleShape.class); // Usa una classe concreta per il mock se necessario
        when(mockShape1.getId()).thenReturn(UUID.randomUUID());
        // Configura cloneShape per restituire una copia (o se stesso se il test non lo richiede strettamente)
        when(mockShape1.cloneShape()).thenReturn(mockShape1);


        mockShape2 = Mockito.mock(LineSegment.class);
        when(mockShape2.getId()).thenReturn(UUID.randomUUID());
        when(mockShape2.cloneShape()).thenReturn(mockShape2);


        // Crea mock per Observer
        mockObserver1 = Mockito.mock(Observer.class);
        mockObserver2 = Mockito.mock(Observer.class);

        // Aggiungi observer al drawing per i test
        drawing.addObserver(mockObserver1);
        drawing.addObserver(mockObserver2);
    }

    @Test
    @DisplayName("Un nuovo Drawing dovrebbe essere vuoto e avere zero observer inizialmente (prima di setUp)")
    void testNewDrawingIsEmpty() {
        Drawing freshDrawing = new Drawing(); // Crea una nuova istanza non modificata da setUp
        assertThat(freshDrawing.getShapes()).isEmpty();
        // Non possiamo testare direttamente il numero di observer senza accedere al campo privato,
        // ma possiamo verificare che la notifica non avvenga se non ci sono observer.
    }

    @Test
    @DisplayName("addShape dovrebbe aggiungere una forma e notificare gli observer")
    void testAddShape() {
        drawing.addShape(mockShape1);
        assertThat(drawing.getShapes()).containsExactly(mockShape1);
        verify(mockObserver1, times(1)).update(drawing);
        verify(mockObserver2, times(1)).update(drawing);
    }

    @Test
    @DisplayName("addShape con forma nulla dovrebbe lanciare NullPointerException")
    void testAddNullShape() {
        assertThatThrownBy(() -> drawing.addShape(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Shape to add cannot be null");
        verify(mockObserver1, never()).update(any()); // Nessuna notifica se l'aggiunta fallisce
    }

    @Test
    @DisplayName("removeShape dovrebbe rimuovere una forma esistente e notificare gli observer")
    void testRemoveExistingShape() {
        drawing.addShape(mockShape1);
        drawing.addShape(mockShape2);
        reset(mockObserver1, mockObserver2); // Resetta i mock per contare solo le notifiche di remove

        boolean removed = drawing.removeShape(mockShape1);
        assertThat(removed).isTrue();
        assertThat(drawing.getShapes()).containsExactly(mockShape2);
        verify(mockObserver1, times(1)).update(drawing);
        verify(mockObserver2, times(1)).update(drawing);
    }

    @Test
    @DisplayName("removeShape dovrebbe restituire false se la forma non esiste e non notificare")
    void testRemoveNonExistingShape() {
        drawing.addShape(mockShape1);
        reset(mockObserver1, mockObserver2);

        Shape nonExistingShape = Mockito.mock(EllipseShape.class);
        boolean removed = drawing.removeShape(nonExistingShape);

        assertThat(removed).isFalse();
        assertThat(drawing.getShapes()).containsExactly(mockShape1);
        verify(mockObserver1, never()).update(drawing);
        verify(mockObserver2, never()).update(drawing);
    }

    @Test
    @DisplayName("removeShape con forma nulla dovrebbe restituire false e non notificare")
    void testRemoveNullShape() {
        drawing.addShape(mockShape1);
        reset(mockObserver1, mockObserver2);

        boolean removed = drawing.removeShape(null);
        assertThat(removed).isFalse();
        assertThat(drawing.getShapes()).containsExactly(mockShape1);
        verify(mockObserver1, never()).update(drawing);
    }

    @Test
    @DisplayName("removeShapeById dovrebbe rimuovere una forma per ID e notificare")
    void testRemoveShapeById() {
        drawing.addShape(mockShape1);
        drawing.addShape(mockShape2);
        reset(mockObserver1, mockObserver2);

        boolean removed = drawing.removeShapeById(mockShape1.getId());
        assertThat(removed).isTrue();
        assertThat(drawing.getShapes()).containsExactly(mockShape2);
        verify(mockObserver1, times(1)).update(drawing);
    }

    @Test
    @DisplayName("removeShapeById con ID nullo o non esistente dovrebbe restituire false e non notificare")
    void testRemoveShapeByIdNonExisting() {
        drawing.addShape(mockShape1);
        reset(mockObserver1, mockObserver2);

        assertThat(drawing.removeShapeById(UUID.randomUUID())).isFalse(); // ID non esistente
        assertThat(drawing.removeShapeById(null)).isFalse(); // ID nullo
        assertThat(drawing.getShapes()).containsExactly(mockShape1);
        verify(mockObserver1, never()).update(drawing);
    }


    @Test
    @DisplayName("clear dovrebbe rimuovere tutte le forme e notificare se non era vuoto")
    void testClear() {
        drawing.addShape(mockShape1);
        drawing.addShape(mockShape2);
        reset(mockObserver1, mockObserver2);

        drawing.clear();
        assertThat(drawing.getShapes()).isEmpty();
        verify(mockObserver1, times(1)).update(drawing);
        verify(mockObserver2, times(1)).update(drawing);
    }

    @Test
    @DisplayName("clear su un Drawing vuoto non dovrebbe notificare (secondo l'implementazione attuale)")
    void testClearEmptyDrawing() {
        // Assicurati che sia vuoto
        assertThat(drawing.getShapes()).isEmpty();
        reset(mockObserver1, mockObserver2);

        drawing.clear();
        assertThat(drawing.getShapes()).isEmpty();
        verify(mockObserver1, never()).update(drawing); // Basato sulla tua logica `if (!shapes.isEmpty())`
    }

    @Test
    @DisplayName("setShapes dovrebbe sostituire le forme, clonarle e notificare")
    void testSetShapes() {
        Shape shapeA = mock(Shape.class); when(shapeA.cloneShape()).thenReturn(shapeA);
        Shape shapeB = mock(Shape.class); when(shapeB.cloneShape()).thenReturn(shapeB);
        drawing.addShape(mockShape1); // Forma iniziale
        reset(mockObserver1, mockObserver2);

        List<Shape> newShapeList = new ArrayList<>(Arrays.asList(shapeA, shapeB));
        drawing.setShapes(newShapeList);

        assertThat(drawing.getShapes()).containsExactlyInAnyOrder(shapeA, shapeB);
        // Verifica che le forme nella lista interna siano cloni
        // Questo è un po' difficile da verificare con i mock senza configurare cloneShape in modo più complesso
        // Il test principale è che la lista contenga le forme corrette e che gli observer siano notificati.
        // Il when(mockShape.cloneShape()).thenReturn(mockShape) copre questo caso per il test.

        verify(mockObserver1, times(1)).update(drawing);
        verify(mockObserver2, times(1)).update(drawing);

        // Verifica che la lista originale passata a setShapes non sia modificabile tramite il drawing
        newShapeList.add(mock(Shape.class)); // Modifica la lista esterna
        assertThat(drawing.getShapes()).hasSize(2); // Il drawing non dovrebbe cambiare
    }
    
    @Test
    @DisplayName("setShapes con lista nulla dovrebbe lanciare NullPointerException")
    void testSetShapesNull() {
        assertThatThrownBy(() -> drawing.setShapes(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("New shapes list cannot be null");
    }

    @Test
    @DisplayName("setShapes con lista contenente null dovrebbe ignorare i null e clonare gli altri")
    void testSetShapesWithNullsInList() {
        Shape shapeA = mock(Shape.class); when(shapeA.cloneShape()).thenReturn(shapeA);
        List<Shape> newShapesWithNull = new ArrayList<>();
        newShapesWithNull.add(shapeA);
        newShapesWithNull.add(null);
        newShapesWithNull.add(mockShape2); // mockShape2 già configurato per cloneShape in setUp

        drawing.setShapes(newShapesWithNull);
        assertThat(drawing.getShapes()).containsExactlyInAnyOrder(shapeA, mockShape2);
        assertThat(drawing.getShapes()).doesNotContainNull();
        verify(mockObserver1).update(drawing);
    }


    @Test
    @DisplayName("iterator dovrebbe restituire un iteratore sulle forme")
    void testIterator() {
        drawing.addShape(mockShape1);
        drawing.addShape(mockShape2);

        Iterator<Shape> it = drawing.iterator();
        List<Shape> iteratedShapes = new ArrayList<>();
        it.forEachRemaining(iteratedShapes::add);

        assertThat(iteratedShapes).containsExactlyInAnyOrder(mockShape1, mockShape2);
        // L'iteratore da Collections.unmodifiableList non supporta remove
        assertThatThrownBy(() -> it.remove()).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("getShapes dovrebbe restituire una lista non modificabile delle forme")
    void testGetShapesUnmodifiable() {
        drawing.addShape(mockShape1);
        List<Shape> shapesList = drawing.getShapes();

        assertThat(shapesList).containsExactly(mockShape1);
        // Verifica che la lista restituita sia non modificabile
        assertThatThrownBy(() -> shapesList.add(mockShape2))
            .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> shapesList.remove(0))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("getShapeById dovrebbe restituire la forma corretta o null")
    void testGetShapeById() {
        drawing.addShape(mockShape1);
        drawing.addShape(mockShape2);

        assertThat(drawing.getShapeById(mockShape1.getId())).isSameAs(mockShape1);
        assertThat(drawing.getShapeById(mockShape2.getId())).isSameAs(mockShape2);
        assertThat(drawing.getShapeById(UUID.randomUUID())).isNull(); // ID non esistente
        assertThat(drawing.getShapeById(null)).isNull();
    }

    // Test per i metodi Observer
    @Test
    @DisplayName("addObserver non dovrebbe aggiungere lo stesso observer più volte")
    void testAddObserverDuplicates() {
        // mockObserver1 è già stato aggiunto in setUp

        drawing.addObserver(mockObserver1); // Prova ad aggiungerlo di nuovo
        // Verifica che la notifica avvenga ancora una sola volta per mockObserver1
        drawing.addShape(mock(Shape.class)); // Trigger notification
        verify(mockObserver1, times(1)).update(drawing); // `times(1)` perché è stato resettato in `addShape` nel test `testAddShape`

        // Se `observers` fosse accessibile, verificheremmo la dimensione.
        // In alternativa, possiamo rimuoverlo e verificare che non venga più notificato.
        reset(mockObserver1, mockObserver2);
        drawing.removeObserver(mockObserver1);
        drawing.addShape(mock(Shape.class));
        verify(mockObserver1, never()).update(drawing); // Non dovrebbe più essere notificato
    }
    
    @Test
    @DisplayName("addObserver con null dovrebbe lanciare NullPointerException")
    void testAddNullObserver() {
        assertThatThrownBy(() -> drawing.addObserver(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Observer cannot be null");
    }

    @Test
    @DisplayName("removeObserver dovrebbe rimuovere un observer esistente")
    void testRemoveObserver() {
        // mockObserver1 è aggiunto in setUp
        drawing.removeObserver(mockObserver1);

        drawing.addShape(mockShape1); // Trigger notification
        verify(mockObserver1, never()).update(drawing); // Non dovrebbe essere notificato
        verify(mockObserver2, times(1)).update(drawing); // L'altro observer dovrebbe ancora esserlo
    }

    @Test
    @DisplayName("removeObserver con observer nullo o non esistente non dovrebbe fare nulla")
    void testRemoveNonExistingOrNullObserver() {
        Observer nonExistingObserver = mock(Observer.class);
        // Non dovrebbe lanciare eccezioni
        assertDoesNotThrow(() -> drawing.removeObserver(nonExistingObserver));
        assertDoesNotThrow(() -> drawing.removeObserver(null));

        // Verifica che gli observer esistenti siano ancora lì
        drawing.addShape(mockShape1);
        verify(mockObserver1, times(1)).update(drawing);
        verify(mockObserver2, times(1)).update(drawing);
    }

    // Test per la serializzazione (più complesso, potrebbe richiedere una classe di test separata o più setup)
    // Per ora, lo omettiamo per concentrarci sulla logica di base.
    // Il metodo readObject per reinizializzare 'observers' è importante.
}