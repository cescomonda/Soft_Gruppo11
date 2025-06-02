
package sad.gruppo11.Model;

import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Rect;
import sad.gruppo11.Model.geometry.Vector2D;
import sad.gruppo11.View.ShapeVisitor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class GroupShape implements Shape, Serializable {
    private final UUID id;
    private List<Shape> children;
    private double rotationAngle; // Rotazione del gruppo stesso, applicata ai figli
    // Nota: i bounds di GroupShape sono calcolati dinamicamente dai figli

    public GroupShape(List<Shape> initialChildren) {
        this.id = UUID.randomUUID();
        Objects.requireNonNull(initialChildren, "Initial children list cannot be null for GroupShape.");
        // Clona i figli per evitare modifiche esterne alla lista interna e per assicurarsi
        // che il gruppo possieda le sue istanze (o copie con nuovi ID se necessario per la logica dell'app)
        this.children = new ArrayList<>();
        for (Shape child : initialChildren) {
            if (child != null) {
                // Quando si crea un gruppo, i figli mantengono i loro ID originali
                // e le loro proprietà. Il gruppo li "contiene".
                // Se si vuole che i figli siano copie "fresche", usare child.cloneWithNewId()
                // ma questo di solito avviene al paste, non al group.
                this.children.add(child); // Aggiunge il riferimento diretto, o child.clone() se si preferisce
            }
        }
        this.rotationAngle = 0.0;
    }
    
    // Costruttore privato per la clonazione
    private GroupShape(UUID id, List<Shape> clonedChildren, double rotationAngle) {
        this.id = id;
        this.children = clonedChildren; // Assume che clonedChildren siano già cloni appropriati
        this.rotationAngle = rotationAngle;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void move(Vector2D v) {
        Objects.requireNonNull(v, "Movement vector cannot be null.");
        for (Shape child : children) {
            child.move(v);
        }
        // La rotazione del gruppo è relativa al suo (nuovo) centro,
        // quindi muovere i figli muove implicitamente il pivot di rotazione del gruppo.
    }

    @Override
    public void resize(Rect newGroupBounds) {
        Objects.requireNonNull(newGroupBounds, "New group bounds cannot be null for resize.");
        Rect currentGroupBounds = getBounds(); // Bounds attuali del gruppo (non ruotati)

        if (currentGroupBounds.getWidth() == 0 || currentGroupBounds.getHeight() == 0) {
            System.err.println("GroupShape: Cannot resize a group with zero width or height bounds.");
            return;
        }
        if (newGroupBounds.getWidth() < 0 || newGroupBounds.getHeight() < 0) {
            System.err.println("GroupShape: New bounds have non-positive width or height.");
            return;
        }

        double scaleX = (currentGroupBounds.getWidth() != 0) ? newGroupBounds.getWidth() / currentGroupBounds.getWidth() : 1.0;
        double scaleY = (currentGroupBounds.getHeight() != 0) ? newGroupBounds.getHeight() / currentGroupBounds.getHeight() : 1.0;

        Point2D oldCenter = currentGroupBounds.getCenter();
        Point2D newCenter = newGroupBounds.getCenter(); // Il nuovo centro del gruppo

        for (Shape child : children) {
            Rect childOldBounds = child.getBounds(); // Bounds non ruotati del figlio
            Point2D childOldCenter = childOldBounds.getCenter();

            // 1. Calcola la posizione del centro del figlio relativa al vecchio centro del gruppo
            Vector2D relativePosToOldGroupCenter = new Vector2D(
                childOldCenter.getX() - oldCenter.getX(),
                childOldCenter.getY() - oldCenter.getY()
            );

            // 2. Scala questa posizione relativa
            Vector2D scaledRelativePos = new Vector2D(
                relativePosToOldGroupCenter.getDx() * scaleX,
                relativePosToOldGroupCenter.getDy() * scaleY
            );

            // 3. Calcola il nuovo centro del figlio basato sul nuovo centro del gruppo e la posizione relativa scalata
            Point2D childNewCenter = new Point2D(
                newCenter.getX() + scaledRelativePos.getDx(),
                newCenter.getY() + scaledRelativePos.getDy()
            );

            // 4. Calcola i nuovi bounds del figlio
            double childNewWidth = childOldBounds.getWidth() * scaleX;
            double childNewHeight = childOldBounds.getHeight() * scaleY;
            Rect childNewBounds = new Rect(
                childNewCenter.getX() - childNewWidth / 2.0,
                childNewCenter.getY() - childNewHeight / 2.0,
                childNewWidth,
                childNewHeight
            );
            
            // Applica il resize al figlio
            child.resize(childNewBounds);

            // Sposta il figlio in modo che il suo centro sia childNewCenter
            // (resize potrebbe non centrare automaticamente)
            Vector2D correctionMove = new Vector2D(
                childNewCenter.getX() - child.getBounds().getCenter().getX(),
                childNewCenter.getY() - child.getBounds().getCenter().getY()
            );
            if (correctionMove.length() > 1e-6) {
                 child.move(correctionMove);
            }
        }
    }


    @Override
    public void setStrokeColor(ColorData c) {
        Objects.requireNonNull(c, "Stroke color cannot be null.");
        for (Shape child : children) {
            child.setStrokeColor(c);
        }
    }

    @Override
    public ColorData getStrokeColor() {
        // Potrebbe restituire il colore del primo figlio o null/un colore speciale se diversi.
        if (!children.isEmpty()) {
            return children.get(0).getStrokeColor(); // Semplificazione
        }
        return ColorData.TRANSPARENT; // O un default appropriato
    }

    @Override
    public void setFillColor(ColorData c) {
        Objects.requireNonNull(c, "Fill color cannot be null.");
        for (Shape child : children) {
            child.setFillColor(c);
        }
    }

    @Override
    public ColorData getFillColor() {
        if (!children.isEmpty()) {
            return children.get(0).getFillColor(); // Semplificazione
        }
        return ColorData.TRANSPARENT;
    }

    @Override
    public boolean contains(Point2D p) {
        Objects.requireNonNull(p, "Point p cannot be null for contains check.");
        // Il gruppo contiene p se uno qualsiasi dei suoi figli contiene p.
        // Bisogna considerare la rotazione del gruppo.
        // Trasforma p nello spazio non ruotato del gruppo.
        Point2D groupCenter = getBounds().getCenter(); 
        double angleRadInverse = Math.toRadians(-this.rotationAngle);
        double cosA = Math.cos(angleRadInverse);
        double sinA = Math.sin(angleRadInverse);

        double translatedPx = p.getX() - groupCenter.getX();
        double translatedPy = p.getY() - groupCenter.getY();
        
        double localPx = translatedPx * cosA - translatedPy * sinA + groupCenter.getX();
        double localPy = translatedPx * sinA + translatedPy * cosA + groupCenter.getY();
        Point2D pointInGroupLocalSpace = new Point2D(localPx, localPy);

        for (Shape child : children) {
            // Il metodo contains del figlio opera già con la sua rotazione individuale
            // e i suoi bounds/vertici non ruotati.
            // Il punto passato al figlio deve essere nello stesso sistema di coordinate
            // dei dati del figlio (cioè, mondo, non ruotato dal gruppo).
            // Quindi, passiamo pointInGroupLocalSpace.
            if (child.contains(pointInGroupLocalSpace)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void accept(ShapeVisitor v) {
        Objects.requireNonNull(v, "ShapeVisitor cannot be null.");
        v.visit(this); // Il visitor deciderà come renderizzare il gruppo (es. iterare sui figli)
    }

    @Override
    public Shape clone() { // Per Clipboard (stesso ID per gruppo e figli)
        List<Shape> clonedChildren = new ArrayList<>();
        for (Shape child : this.children) {
            clonedChildren.add(child.clone()); // Clona ogni figlio (mantiene ID figlio)
        }
        return new GroupShape(this.id, clonedChildren, this.rotationAngle);
    }

    @Override
    public Shape cloneWithNewId() { // Per Paste (nuovo ID per gruppo e per ogni figlio)
        List<Shape> clonedChildrenWithNewIds = new ArrayList<>();
        for (Shape child : this.children) {
            clonedChildrenWithNewIds.add(child.cloneWithNewId()); // Clona con nuovo ID
        }
        // Crea il nuovo gruppo con un nuovo ID e i figli clonati (che hanno già nuovi ID)
        return new GroupShape(UUID.randomUUID(), clonedChildrenWithNewIds, this.rotationAngle);
    }

    @Override
    public Rect getBounds() {
        if (children.isEmpty()) {
            return new Rect(0, 0, 0, 0); // Gruppo vuoto
        }

        // Calcola il bounding box che racchiude tutti i bounding box dei figli.
        // Questi sono i bounds NON ruotati del gruppo.
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        for (Shape child : children) {
            Rect childBounds = child.getRotatedBounds(); // Bounds ruotati del figlio
            
            Point2D childTopLeft = childBounds.getTopLeft();
            Point2D childBottomRight = childBounds.getBottomRight();

            minX = Math.min(minX, childTopLeft.getX());
            minY = Math.min(minY, childTopLeft.getY());
            maxX = Math.max(maxX, childBottomRight.getX());
            maxY = Math.max(maxY, childBottomRight.getY());
        }
        return new Rect(new Point2D(minX, minY), maxX - minX, maxY - minY);
    }

    @Override
    public void setRotation(double angle) {
        // Ruotare un gruppo significa cambiare l'angolo di rotazione del gruppo stesso.
        // I figli mantengono la loro rotazione relativa al gruppo.
        // Il rendering applicherà prima la trasformazione del gruppo (inclusa questa rotazione)
        // e poi la trasformazione individuale di ogni figlio (inclusa la sua rotazione).
        this.rotationAngle = angle % 360.0;
        if (this.rotationAngle < 0) this.rotationAngle += 360.0;
        this.rotationAngle = this.rotationAngle == -0.0 ? 0.0 : this.rotationAngle;
    }

    @Override
    public double getRotation() {
        return this.rotationAngle;
    }

    // Metodi non applicabili direttamente a GroupShape (testo)
    @Override
    public void setText(String text) {}
    @Override
    public String getText() { return null; }
    @Override
    public void setFontSize(double size) {}
    @Override
    public double getFontSize() { return 0; }

    // --- Metodi per Riflessione (US 27) ---
    @Override
    public void reflectHorizontal() {
        Point2D groupCenter = getRotatedBounds().getCenter(); // Centro del AABB non ruotato del gruppo
        // Rifletti ogni figlio rispetto all'asse verticale passante per groupCenter.getX()
        // Questo significa che la posizione relativa di ogni figlio rispetto al centro del gruppo viene riflessa.
        for (Shape child : children) {
            // 1. Trova il centro del figlio
            Point2D childCenter = child.getBounds().getCenter();
            // 2. Calcola la nuova coordinata X del centro del figlio
            double newChildCenterX = 2 * groupCenter.getX() - childCenter.getX();
            // 3. Calcola il vettore di spostamento per il figlio
            Vector2D moveVector = new Vector2D(newChildCenterX - childCenter.getX(), 0);
            child.move(moveVector);
            // 4. Rifletti il figlio stesso intrinsecamente
            child.reflectHorizontal();
        }
        // Inverti la rotazione del gruppo.
        setRotation(-getRotation()); 
    }

    @Override
    public void reflectVertical() {
        Point2D groupCenter = getBounds().getCenter(); // Centro del AABB non ruotato del gruppo
        for (Shape child : children) {
            Point2D childCenter = child.getBounds().getCenter();
            double newChildCenterY = 2 * groupCenter.getY() - childCenter.getY();
            Vector2D moveVector = new Vector2D(0, newChildCenterY - childCenter.getY());
            child.move(moveVector);
            child.reflectVertical();
        }
        // Inverti la rotazione del gruppo.
        setRotation(-getRotation());
    }

    // --- Metodi del Pattern Composite ---
    @Override
    public void add(Shape s) {
        Objects.requireNonNull(s, "Cannot add a null shape to the group.");
        if (!this.children.contains(s)) {
            this.children.add(s);
        }
    }

    @Override
    public void remove(Shape s) {
        Objects.requireNonNull(s, "Cannot remove a null shape from the group.");
        this.children.remove(s);
    }

    @Override
    public Shape getChild(int i) {
        if (i < 0 || i >= children.size()) {
            throw new IndexOutOfBoundsException("Child index out of bounds: " + i);
        }
        return children.get(i);
    }

    @Override
    public List<Shape> getChildren() {
        // Restituisce una copia non modificabile per proteggere l'incapsulamento
        return Collections.unmodifiableList(new ArrayList<>(children));
    }
    
    public List<Shape> getModifiableChildren() {
        // Usato internamente o da comandi che sanno cosa stanno facendo
        return this.children;
    }

    @Override
    public boolean isComposite() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupShape that = (GroupShape) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("GroupShape{id=%s, childrenCount=%d, rotation=%.1f}",
                id, children.size(), rotationAngle);
    }

    private Point2D rotatePoint(Point2D point, Point2D pivot, double angleDegrees) { // Copia helper
        double angleRad = Math.toRadians(angleDegrees);
        double cosA = Math.cos(angleRad);
        double sinA = Math.sin(angleRad);
        double dx = point.getX() - pivot.getX();
        double dy = point.getY() - pivot.getY();
        double newX = pivot.getX() + (dx * cosA - dy * sinA);
        double newY = pivot.getY() + (dx * sinA + dy * cosA);
        return new Point2D(newX, newY);
    }

    @Override
    public Rect getRotatedBounds() {
        if (children.isEmpty()) {
            return new Rect(0, 0, 0, 0);
        }
        
        double groupAngle = getRotation();
        // Il pivot per la rotazione del gruppo è il centro del suo AABB *non* ruotato
        Point2D groupPivot = getBounds().getCenter(); 

        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        for (Shape child : children) {
            // Ottieni l'AABB del figlio, già considerando la rotazione *propria* del figlio.
            Rect childRotatedAABB = child.getRotatedBounds();

            // Ora, considera i 4 vertici di questo AABB del figlio.
            // Questi vertici sono nello spazio del mondo, ma devono essere
            // ulteriormente ruotati dalla rotazione del gruppo.
            Point2D c_topLeft = childRotatedAABB.getTopLeft();
            Point2D c_topRight = new Point2D(childRotatedAABB.getRight(), childRotatedAABB.getY());
            Point2D c_bottomLeft = new Point2D(childRotatedAABB.getX(), childRotatedAABB.getBottom());
            Point2D c_bottomRight = childRotatedAABB.getBottomRight();

            Point2D[] childVertices = {c_topLeft, c_topRight, c_bottomRight, c_bottomLeft};

            for (Point2D vertex : childVertices) {
                Point2D transformedVertex = vertex;
                // Applica la rotazione del gruppo a ciascun vertice dell'AABB ruotato del figlio
                if (groupAngle != 0.0) {
                    transformedVertex = rotatePoint(vertex, groupPivot, groupAngle);
                }
                minX = Math.min(minX, transformedVertex.getX());
                minY = Math.min(minY, transformedVertex.getY());
                maxX = Math.max(maxX, transformedVertex.getX());
                maxY = Math.max(maxY, transformedVertex.getY());
            }
        }
        
        if (minX > maxX) { // Se non ci sono figli validi o tutti degeneri
             return new Rect(groupPivot,0,0); // O un altro default sensato
        }

        return new Rect(new Point2D(minX, minY), maxX - minX, maxY - minY);
    }
}
