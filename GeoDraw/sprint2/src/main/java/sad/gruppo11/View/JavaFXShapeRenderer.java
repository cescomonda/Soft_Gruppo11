package sad.gruppo11.View;

import sad.gruppo11.Model.EllipseShape;
import sad.gruppo11.Model.LineSegment;
import sad.gruppo11.Model.PolygonShape;
import sad.gruppo11.Model.RectangleShape;
import sad.gruppo11.Model.TextShape;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Rect;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;

import java.util.List;
import java.util.Objects;

public class JavaFXShapeRenderer implements ShapeVisitor {
    private GraphicsContext gc;
    private double defaultLineWidth = 1.5;
    private sad.gruppo11.Model.Shape currentlySelectedShapeForRendering;

    public JavaFXShapeRenderer(GraphicsContext gc) {
        Objects.requireNonNull(gc, "GraphicsContext cannot be null for JavaFXShapeRenderer.");
        this.gc = gc;
    }

    public void setDefaultLineWidth(double width) {
        if (width > 0) this.defaultLineWidth = width;
    }

    public void setSelectedShapeForRendering(sad.gruppo11.Model.Shape selectedShape) {
        this.currentlySelectedShapeForRendering = selectedShape;
    }

    public static Color convertModelToFxColor(ColorData colorData) {
        if (colorData == null) return Color.TRANSPARENT;
        return Color.rgb(colorData.getR(), colorData.getG(), colorData.getB(), colorData.getA());
    }

    private void drawSelectionIndicator(Rect bounds, double rotationAngle, Point2D rotationCenter) {
        if (bounds == null) return;
        gc.save();
        if (rotationAngle != 0 && rotationCenter != null) {
            Rotate r = new Rotate(rotationAngle, rotationCenter.getX(), rotationCenter.getY());
            gc.transform(r.getMxx(), r.getMyx(), r.getMxy(), r.getMyy(), r.getTx(), r.getTy());
        }
        gc.setStroke(Color.CORNFLOWERBLUE); 
        gc.setLineWidth(1.0);
        gc.setLineDashes(4, 4);
        gc.strokeRect(bounds.getX() - 2, bounds.getY() - 2, bounds.getWidth() + 4, bounds.getHeight() + 4);
        gc.restore(); 
    }
    
    private void drawSelectionIndicatorForLine(Point2D start, Point2D end, double rotationAngle, Point2D rotationCenter) {
        if (start == null || end == null) return;
        gc.save();
        if (rotationAngle != 0 && rotationCenter != null) {
            Rotate r = new Rotate(rotationAngle, rotationCenter.getX(), rotationCenter.getY());
            gc.transform(r.getMxx(), r.getMyx(), r.getMxy(), r.getMyy(), r.getTx(), r.getTy());
        }
        gc.setStroke(Color.CORNFLOWERBLUE);
        gc.setLineWidth(1.0);
        gc.setLineDashes(4, 4);
        double minX = Math.min(start.getX(), end.getX()) - 3;
        double minY = Math.min(start.getY(), end.getY()) - 3;
        double width = Math.abs(start.getX() - end.getX()) + 6;
        double height = Math.abs(start.getY() - end.getY()) + 6;
        if (width < 6) width = 6; 
        if (height < 6) height = 6; 
        gc.strokeRect(minX, minY, width, height);
        gc.restore();
    }

    @Override
    public void visit(RectangleShape rShape) {
        Objects.requireNonNull(rShape, "RectangleShape cannot be null for visit.");
        Rect bounds = rShape.getBounds();
        Color strokeFx = convertModelToFxColor(rShape.getStrokeColor());
        Color fillFx = convertModelToFxColor(rShape.getFillColor());
        double rotation = rShape.getRotation();
        Point2D center = bounds.getCenter();
        gc.save();
        if (rotation != 0) {
            Rotate rot = new Rotate(rotation, center.getX(), center.getY());
            gc.transform(rot.getMxx(), rot.getMyx(), rot.getMxy(), rot.getMyy(), rot.getTx(), rot.getTy());
        }
        if (fillFx.getOpacity() > 0.0) { 
            gc.setFill(fillFx);
            gc.fillRect(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
        }
        gc.setStroke(strokeFx);
        gc.setLineWidth(defaultLineWidth);
        gc.setLineCap(StrokeLineCap.SQUARE);
        gc.setLineJoin(StrokeLineJoin.MITER);
        gc.strokeRect(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
        gc.restore();
        if (rShape.equals(currentlySelectedShapeForRendering)) {
            drawSelectionIndicator(bounds, rotation, center);
        }
    }

    @Override
    public void visit(EllipseShape eShape) {
        Objects.requireNonNull(eShape, "EllipseShape cannot be null for visit.");
        Rect bounds = eShape.getBounds();
        Color strokeFx = convertModelToFxColor(eShape.getStrokeColor());
        Color fillFx = convertModelToFxColor(eShape.getFillColor());
        double rotation = eShape.getRotation();
        Point2D center = bounds.getCenter();
        gc.save();
        if (rotation != 0) {
            Rotate rot = new Rotate(rotation, center.getX(), center.getY());
            gc.transform(rot.getMxx(), rot.getMyx(), rot.getMxy(), rot.getMyy(), rot.getTx(), rot.getTy());
        }
        if (fillFx.getOpacity() > 0.0) {
            gc.setFill(fillFx);
            gc.fillOval(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
        }
        gc.setStroke(strokeFx);
        gc.setLineWidth(defaultLineWidth);
        gc.strokeOval(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
        gc.restore();
        if (eShape.equals(currentlySelectedShapeForRendering)) {
            drawSelectionIndicator(bounds, rotation, center);
        }
    }

    @Override
    public void visit(LineSegment lShape) {
        Objects.requireNonNull(lShape, "LineSegment cannot be null for visit.");
        Point2D start = lShape.getStartPoint();
        Point2D end = lShape.getEndPoint();
        Color strokeFx = convertModelToFxColor(lShape.getStrokeColor());
        double rotation = lShape.getRotation();
        Point2D center = lShape.getBounds().getCenter();
        gc.save();
         if (rotation != 0) {
            Rotate rot = new Rotate(rotation, center.getX(), center.getY());
            gc.transform(rot.getMxx(), rot.getMyx(), rot.getMxy(), rot.getMyy(), rot.getTx(), rot.getTy());
        }
        gc.setStroke(strokeFx);
        gc.setLineWidth(defaultLineWidth);
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.strokeLine(start.getX(), start.getY(), end.getX(), end.getY());
        gc.restore();
        if (lShape.equals(currentlySelectedShapeForRendering)) {
            drawSelectionIndicatorForLine(start, end, rotation, center);
        }
    }

    @Override
    public void visit(PolygonShape pShape) {
        Objects.requireNonNull(pShape, "PolygonShape cannot be null for visit.");
        List<Point2D> modelVertices = pShape.getVertices();
        if (modelVertices.size() < 2) return;
        double[] xPoints = modelVertices.stream().mapToDouble(Point2D::getX).toArray();
        double[] yPoints = modelVertices.stream().mapToDouble(Point2D::getY).toArray();
        int nPoints = modelVertices.size();
        Color strokeFx = convertModelToFxColor(pShape.getStrokeColor());
        Color fillFx = convertModelToFxColor(pShape.getFillColor());
        double rotation = pShape.getRotation();
        Point2D center = pShape.getBounds().getCenter();
        gc.save();
        if (rotation != 0) {
            Rotate rot = new Rotate(rotation, center.getX(), center.getY());
            gc.transform(rot.getMxx(), rot.getMyx(), rot.getMxy(), rot.getMyy(), rot.getTx(), rot.getTy());
        }
        if (fillFx.getOpacity() > 0.0) {
            gc.setFill(fillFx);
            gc.fillPolygon(xPoints, yPoints, nPoints);
        }
        gc.setStroke(strokeFx);
        gc.setLineWidth(defaultLineWidth);
        gc.strokePolygon(xPoints, yPoints, nPoints);
        gc.restore();
        if (pShape.equals(currentlySelectedShapeForRendering)) {
            drawSelectionIndicator(pShape.getBounds(), rotation, center);
        }
    }

    @Override
    public void visit(TextShape tShape) {
        Objects.requireNonNull(tShape, "TextShape cannot be null for visit.");
        String textContent = tShape.getText();
        if (textContent == null || textContent.isEmpty()) {
            return; // Non c'è nulla da disegnare
        }

        Rect targetBounds = tShape.getDrawingBounds(); // I bounds (non ruotati) a cui il testo deve adattarsi
        double baseFontSize = tShape.getBaseFontSize();
        String fontName = tShape.getFontName();
        Color textColorFx = convertModelToFxColor(tShape.getStrokeColor());
        double rotation = tShape.getRotation();
        
        // Se i bounds di destinazione o il font size non sono validi, non possiamo procedere correttamente.
        // Potremmo disegnare il testo non scalato come fallback o semplicemente non disegnare nulla.
        if (targetBounds.getWidth() <= 0 || targetBounds.getHeight() <= 0 || baseFontSize <= 0) {
            System.err.println("TextShape visit: targetBounds o baseFontSize non validi. TargetBounds: " + targetBounds + ", FontSize: " + baseFontSize);
            // Fallback: Disegna testo non scalato alla posizione del topLeft dei targetBounds, se possibile.
            gc.save();
            gc.setFont(Font.font(fontName, baseFontSize > 0 ? baseFontSize : 10)); // Usa un font size di fallback se baseFontSize non è valido
            gc.setFill(textColorFx);
            gc.setTextAlign(TextAlignment.LEFT);
            gc.setTextBaseline(VPos.TOP); // Allinea il top del testo all'Y specificato

            Point2D fallbackPosition = targetBounds.getTopLeft();
            if (rotation != 0) {
                gc.translate(fallbackPosition.getX(), fallbackPosition.getY());
                gc.rotate(rotation);
                gc.fillText(textContent, 0, 0); // Disegna a (0,0) del sistema ruotato e traslato
            } else {
                gc.fillText(textContent, fallbackPosition.getX(), fallbackPosition.getY());
            }
            gc.restore();

            if (tShape.equals(currentlySelectedShapeForRendering)) {
                // L'indicatore di selezione usa targetBounds (che potrebbero essere degeneri) e ruota attorno al loro centro.
                drawSelectionIndicator(targetBounds, rotation, targetBounds.getCenter());
            }
            return;
        }

        gc.save();

        // 1. Calcola le dimensioni naturali del testo con baseFontSize per determinare la scala.
        Text textNodeForMeasurement = new Text(textContent);
        textNodeForMeasurement.setFont(Font.font(fontName, baseFontSize));
        javafx.geometry.Bounds naturalLayoutBounds = textNodeForMeasurement.getLayoutBounds();
        double naturalWidth = naturalLayoutBounds.getWidth();
        double naturalHeight = naturalLayoutBounds.getHeight();

        // Se le dimensioni naturali sono 0 (es. font non trovato, testo con solo spazi non renderizzabili),
        // non possiamo scalare. Il blocco precedente dovrebbe aver gestito stringhe vuote.
        if (naturalWidth <= 0 || naturalHeight <= 0) {
            System.err.println("TextShape visit: Dimensioni naturali del testo sono zero o negative. Width: " + naturalWidth + " Height: " + naturalHeight);
            gc.restore(); // Ripristina lo stato salvato
            // Potresti voler disegnare testo non scalato come nel blocco di fallback sopra.
            // Per ora, usciamo per evitare divisioni per zero.
            if (tShape.equals(currentlySelectedShapeForRendering)) { // Disegna comunque l'indicatore se selezionato
                drawSelectionIndicator(targetBounds, rotation, targetBounds.getCenter());
            }
            return;
        }

        // 2. Calcola i fattori di scala per adattare il testo naturale ai targetBounds.
        double scaleX = targetBounds.getWidth() / naturalWidth;
        double scaleY = targetBounds.getHeight() / naturalHeight;

        // 3. Definisci il centro dei targetBounds, che sarà il nostro pivot per le trasformazioni.
        Point2D centerOfTargetBounds = targetBounds.getCenter();

        // 4. Applica le trasformazioni al GraphicsContext:
        //    a. Trasla l'origine del GC al centro dei targetBounds.
        gc.translate(centerOfTargetBounds.getX(), centerOfTargetBounds.getY());
        
        //    b. Ruota attorno a questo nuovo origine (0,0 del GC), che è il centro dei targetBounds.
        if (rotation != 0) {
            gc.rotate(rotation);
        }

        //    c. Applica la scala. Anche la scala avverrà rispetto all'origine corrente (0,0) del GC.
        gc.scale(scaleX, scaleY);

        // 5. Prepara e disegna il testo.
        gc.setFont(Font.font(fontName, baseFontSize)); // Usa il baseFontSize; la scala applicata al GC farà il resto.
        gc.setFill(textColorFx);
        gc.setTextAlign(TextAlignment.LEFT); // L'allineamento è relativo al punto x,y di fillText.
        gc.setTextBaseline(VPos.TOP);    // Cruciale: Y in fillText si riferisce al top della cella del font.

        // Calcola le coordinate di disegno (drawX, drawY) nel sistema di coordinate
        // che è già stato traslato al centro, ruotato e scalato.
        // Vogliamo che il testo (con le sue dimensioni *naturali*) sia centrato
        // attorno all'origine (0,0) di questo sistema di coordinate trasformato.

        // drawX: per centrare orizzontalmente il testo naturale, il suo punto di ancoraggio X
        //        (che è il suo minX del layout) deve essere a -naturalWidth / 2.
        double drawX = -naturalWidth / 2.0 - naturalLayoutBounds.getMinX();

        // drawY: per centrare verticalmente il testo naturale, il suo punto di ancoraggio Y
        //        (che con VPos.TOP è il suo minY del layout) deve essere a -naturalHeight / 2.
        double drawY_forCentering = -naturalHeight / 2.0 - naturalLayoutBounds.getMinY();
        
        // Applica l'offset verticale personalizzato (ex "imbroglio")
        // Questo offset è definito in termini di baseFontSize, quindi è in coordinate "pre-scala".
        // Sposta il testo verso l'alto (valore negativo) o verso il basso (valore positivo)
        // rispetto alla posizione centrata verticalmente.
        // Il tuo offset originale era -(baseFontSize - baseFontSize * 0.16) = -baseFontSize * 0.84
        // Questo sposta il testo verso l'alto.
        double verticalTextOffset = -(baseFontSize - baseFontSize * 0.16); // Il tuo offset personalizzato

        double finalDrawY = drawY_forCentering + verticalTextOffset;

        gc.fillText(textContent, drawX, finalDrawY);
        
        gc.restore(); // Ripristina lo stato del GC (rimuove traslazione, rotazione, scala)

        // L'indicatore di selezione usa targetBounds (non ruotati) e la rotazione
        // viene applicata attorno al centro di targetBounds.
        // Questo dovrebbe allinearsi bene ora che il testo è anche trasformato attorno a quel centro.
        if (tShape.equals(currentlySelectedShapeForRendering)) {
            drawSelectionIndicator(targetBounds, rotation, centerOfTargetBounds);
        }
    }

    

}