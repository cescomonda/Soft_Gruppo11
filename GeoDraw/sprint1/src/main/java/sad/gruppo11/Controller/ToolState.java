package  sad.gruppo11.Controller;

import  sad.gruppo11.Model.geometry.Point2D;

/**
 * Interfaccia per il pattern State, rappresenta lo stato corrente dello strumento
 * di disegno nell'applicazione GeoEngine.
 * Ogni implementazione concreta definisce il comportamento specifico per uno strumento
 * in risposta agli eventi del mouse.
 */
public interface ToolState {
    /**
     * Chiamato quando il pulsante del mouse viene premuto mentre questo stato è attivo.
     * @param p La posizione del mouse nel sistema di coordinate del canvas.
     * @param engine L'istanza di GeoEngine, per permettere allo stato di interagire
     *               con il  sad.gruppo11.Modello, il command manager, o cambiare stato.
     */
    void onMousePressed(Point2D p, GeoEngine engine);

    /**
     * Chiamato quando il mouse viene trascinato (mosso con un pulsante premuto)
     * mentre questo stato è attivo.
     * @param p La posizione corrente del mouse.
     * @param engine L'istanza di GeoEngine.
     */
    void onMouseDragged(Point2D p, GeoEngine engine);

    /**
     * Chiamato quando il pulsante del mouse viene rilasciato mentre questo stato è attivo.
     * È tipicamente qui che una forma viene finalizzata e aggiunta al  sad.gruppo11.Modello
     * tramite un comando.
     * @param p La posizione del mouse al momento del rilascio.
     * @param engine L'istanza di GeoEngine.
     */
    void onMouseReleased(Point2D p, GeoEngine engine);

    /**
     * Restituisce un nome descrittivo per questo strumento/stato.
     * Utile per l'interfaccia utente.
     * @return Il nome dello strumento.
     */
    String getToolName();

    /**
     * Chiamato quando lo stato viene attivato (quando GeoEngine passa a questo stato).
     * Può essere usato per inizializzazioni specifiche dello stato.
     * @param engine L'istanza di GeoEngine.
     */
    default void onEnterState(GeoEngine engine) {
        // Implementazione di default non fa nulla
        // System.out.println("Entering state: " + getToolName());
    }

    /**
     * Chiamato quando lo stato viene disattivato (quando GeoEngine passa da questo stato a un altro).
     * Può essere usato per pulizie specifiche dello stato.
     * @param engine L'istanza di GeoEngine.
     */
    default void onExitState(GeoEngine engine) {
        // Implementazione di default non fa nulla
        // System.out.println("Exiting state: " + getToolName());
    }
}