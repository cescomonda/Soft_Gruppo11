
package sad.gruppo11.Controller; // O sad.gruppo11.Infrastructure

import sad.gruppo11.Model.Shape;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap; // Mantiene l'ordine di inserimento
import java.util.Map;
import java.util.Objects;

/**
 * Gestisce una collezione di definizioni di forme riutilizzabili.
 */
public class ReusableShapeLibrary implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<String, ReusableShapeDefinition> definitions;

    public ReusableShapeLibrary() {
        this.definitions = new LinkedHashMap<>(); // Usa LinkedHashMap per mantenere l'ordine
    }

    /**
     * Aggiunge una nuova definizione di forma riutilizzabile.
     * Se una definizione con lo stesso nome esiste già, viene sovrascritta.
     * @param definition La definizione da aggiungere.
     */
    public void addDefinition(ReusableShapeDefinition definition) {
        Objects.requireNonNull(definition, "ReusableShapeDefinition cannot be null.");
        definitions.put(definition.getName(), definition);
    }

    /**
     * Ottiene una definizione di forma riutilizzabile dal suo nome.
     * @param name Il nome della definizione.
     * @return La ReusableShapeDefinition, o null se non trovata.
     */
    public ReusableShapeDefinition getDefinition(String name) {
        return definitions.get(name);
    }

    /**
     * Rimuove una definizione di forma riutilizzabile.
     * @param name Il nome della definizione da rimuovere.
     * @return true se la definizione è stata rimossa, false altrimenti.
     */
    public boolean removeDefinition(String name) {
        return definitions.remove(name) != null;
    }

    /**
     * Restituisce una vista non modificabile di tutte le definizioni.
     * @return Una collezione di tutte le ReusableShapeDefinition.
     */
    public Collection<ReusableShapeDefinition> getAllDefinitions() {
        return Collections.unmodifiableCollection(definitions.values());
    }

    /**
     * Controlla se esiste una definizione con il nome specificato.
     * @param name Il nome da controllare.
     * @return true se esiste, false altrimenti.
     */
    public boolean containsDefinition(String name) {
        return definitions.containsKey(name);
    }

    /**
     * Pulisce tutte le definizioni dalla libreria.
     */
    public void clear() {
        definitions.clear();
    }
    
    /**
     * Importa le definizioni da un'altra libreria, gestendo i conflitti di nome.
     * Se una definizione importata ha un nome che già esiste, le viene aggiunto un suffisso.
     * @param otherLibrary La libreria da cui importare.
     * @return Il numero di definizioni importate con successo.
     */
    public int importDefinitions(ReusableShapeLibrary otherLibrary) {
        Objects.requireNonNull(otherLibrary, "Other library cannot be null for import.");
        int importedCount = 0;
        for (ReusableShapeDefinition defToImport : otherLibrary.getAllDefinitions()) {
            String originalName = defToImport.getName();
            String currentName = originalName;
            int suffix = 1;
            while (this.containsDefinition(currentName)) {
                currentName = originalName + "_" + suffix++;
            }
            // Crea una nuova definizione con il nome (potenzialmente) modificato e il prototipo originale
            addDefinition(new ReusableShapeDefinition(currentName, defToImport.getPrototype()));
            importedCount++;
        }
        return importedCount;
    }
}
