package sad.gruppo11.View;

import java.io.Serializable;

/**
 * Interfaccia del pattern Observer.
 * Consente agli oggetti osservatori di ricevere notifiche quando
 * un oggetto osservato cambia stato.
 */
public interface Observer extends Serializable {
    /**
     * Notificato da un oggetto osservabile quando il suo stato cambia.
     *
     * @param observableSubject L'oggetto che ha generato l'evento di aggiornamento.
     *                          Solitamente è l'oggetto osservato, ad esempio un {@link sad.gruppo11.Model.Drawing}.
     */
    void update(Object observableSubject);
}
