package sad.gruppo11.Infrastructure;

/*
 * Interfaccia per il pattern Command.
 * Ogni comando rappresenta un'azione eseguibile e annullabile,
 * incapsulata in un oggetto.
 *
 * Questo approccio consente:
 * - Parametrizzazione dei client con azioni diverse
 * - Logging o accodamento delle azioni
 * - Implementazione di undo/redo
 */
public interface Command {

    /*
     * Esegue l'azione associata a questo comando.
     */
    void execute();

    /*
     * Annulla l'azione eseguita da questo comando,
     * riportando lo stato dell'applicazione a com'era prima di execute().
     */
    void undo();

    /*
     * Estensioni opzionali (non attivate nel progetto attuale):
     *
     * Redo esplicito (se diverso da execute()):
     * default void redo() { execute(); }
     *
     * Descrizione testuale per interfacce utente o log:
     * default String getDescription() { return this.getClass().getSimpleName(); }
     */
}
