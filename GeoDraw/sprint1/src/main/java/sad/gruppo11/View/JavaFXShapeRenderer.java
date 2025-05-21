package sad.gruppo11.View;

import sad.gruppo11.Model.EllipseShape;
import sad.gruppo11.Model.LineSegment;
import sad.gruppo11.Model.RectangleShape;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Rect;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

import java.util.Objects;

/**
 * {@code JavaFXShapeRenderer} implementa {@link ShapeVisitor} per disegnare le forme
 * su un {@link javafx.scene.canvas.Canvas} utilizzando JavaFX.
 */
public class JavaFXShapeRenderer implements ShapeVisitor {
    private final GraphicsContext gc;
    private double defaultLineWidth = 1.5;
    private Shape currentlySelectedShapeForRendering;

    public JavaFXShapeRenderer(GraphicsContext gc) {
        this.gc = Objects.requireNonNull(gc, "GraphicsContext cannot be null.");
    }

    public void setDefaultLineWidth(double width) {
        if (width > 0) {
            this.defaultLineWidth = width;
        }
    }

    public void setSelectedShapeForRendering(Shape selectedShape) {
        this.currentlySelectedShapeForRendering = selectedShape;
    }

    private Color convertColor(ColorData colorData) {
        if (colorData == null) return Color.TRANSPARENT;
        return Color.rgb(colorData.getR(), colorData.getG(), colorData.getB(), colorData.getA());
    }

    private void drawSelectionIndicator(Rect bounds) {
        gc.setStroke(Color.CORNFLOWERBLUE);
        gc.setLineWidth(defaultLineWidth + 2.0);
        gc.setLineDashes(5, 5);
        gc.strokeRect(bounds.getX() - 2, bounds.getY() - 2, bounds.getWidth() + 4, bounds.getHeight() + 4);
        gc.setLineDashes(0);
    }

    private void drawSelectionIndicatorForLine(Point2D start, Point2D end) {
        gc.setStroke(Color.CORNFLOWERBLUE);
        gc.setLineWidth(defaultLineWidth + 2.0);
        gc.setLineDashes(5, 5);
        double minX = Math.min(start.getX(), end.getX()) - 2;
        double minY = Math.min(start.getY(), end.getY()) - 2;
        double width = Math.abs(start.getX() - end.getX()) + 4;
        double height = Math.abs(start.getY() - end.getY()) + 4;
        gc.strokeRect(minX, minY, width, height);
        gc.setLineDashes(0);
    }

    @Override
    public void visit(RectangleShape r) {
        if (r == null) return;
        Rect bounds = r.getBounds();
        Color stroke = convertColor(r.getStrokeColor());
        Color fill = convertColor(r.getFillColor());

        if (fill.getOpacity() > 0) {
            gc.setFill(fill);
            gc.fillRect(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
        }

        gc.setStroke(stroke);
        gc.setLineWidth(defaultLineWidth);
        gc.setLineCap(StrokeLineCap.SQUARE);
        gc.setLineJoin(StrokeLineJoin.MITER);
        gc.strokeRect(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());

        if (r.equals(currentlySelectedShapeForRendering)) {
            drawSelectionIndicator(bounds);
        }
    }

    @Override
    public void visit(EllipseShape e) {
        if (e == null) return;
        Rect bounds = e.getBounds();
        Color stroke = convertColor(e.getStrokeColor());
        Color fill = convertColor(e.getFillColor());

        if (fill.getOpacity() > 0) {
            gc.setFill(fill);
            gc.fillOval(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
        }

        gc.setStroke(stroke);
        gc.setLineWidth(defaultLineWidth);
        gc.strokeOval(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());

        if (e.equals(currentlySelectedShapeForRendering)) {
            drawSelectionIndicator(bounds);
        }
    }

    @Override
    public void visit(LineSegment l) {
        if (l == null) return;
        Point2D start = l.getStartPoint();
        Point2D end = l.getEndPoint();
        Color stroke = convertColor(l.getStrokeColor());

        gc.setStroke(stroke);
        gc.setLineWidth(defaultLineWidth);
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.strokeLine(start.getX(), start.getY(), end.getX(), end.getY());

        if (l.equals(currentlySelectedShapeForRendering)) {
            drawSelectionIndicatorForLine(start, end);
        }
    }
}
