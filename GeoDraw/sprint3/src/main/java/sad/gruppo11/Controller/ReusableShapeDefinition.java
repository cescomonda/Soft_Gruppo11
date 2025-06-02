
package sad.gruppo11.Controller; // O sad.gruppo11.Infrastructure

import sad.gruppo11.Model.GroupShape;
import sad.gruppo11.Model.Shape;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Rappresenta la definizione di una forma riutilizzabile,
 * contenente un nome e un prototipo della forma.
 */
public class ReusableShapeDefinition implements Serializable {

    private final String name;
    private final Shape prototype;

    public ReusableShapeDefinition(String name, Shape prototypes) {
        Objects.requireNonNull(name, "Name for reusable shape definition cannot be null.");
        Objects.requireNonNull(prototypes, "Prototype shape for reusable definition cannot be null.");
        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name for reusable shape definition cannot be empty.");
        }
        this.name = name;
        // Il prototipo dovrebbe essere un clone della forma/gruppo selezionato
        // con i suoi ID originali, così come era al momento del "Salva come forma riutilizzabile".
        this.prototype = prototypes;
    }

    public String getName() {
        return name;
    }

    /**
     * Restituisce una copia del prototipo.
     * Per l'inserimento nel disegno, si dovrebbe chiamare cloneWithNewId() su questo prototipo.
     * @return Un clone del prototipo della forma.
     */
    public Shape getPrototype() {
        return prototype;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReusableShapeDefinition that = (ReusableShapeDefinition) o;
        return name.equals(that.name) && prototype == that.getPrototype(); // L'unicità è basata sul nome
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, prototype);
    }

    @Override
    public String toString() {
        return "ReusableShapeDefinition{name='" + name + "', prototypeId=" + prototype.getId() + "}";
    }
}
