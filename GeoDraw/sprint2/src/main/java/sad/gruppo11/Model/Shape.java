package sad.gruppo11.Model;

import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Rect;
import sad.gruppo11.Model.geometry.Vector2D;
import sad.gruppo11.View.ShapeVisitor;

import java.io.Serializable;
import java.util.UUID;

public interface Shape extends Serializable {
    UUID getId();
    void move(Vector2D v);
    void resize(Rect bounds);
    
    void setStrokeColor(ColorData c);
    ColorData getStrokeColor();
    
    void setFillColor(ColorData c);
    ColorData getFillColor();
    
    boolean contains(Point2D p);
    void accept(ShapeVisitor v);
    Shape clone();
    Rect getBounds();

    void setRotation(double angle);
    double getRotation();

    void setText(String text);
    String getText();

    void setFontSize(double size);
    double getFontSize();

    Shape cloneWithNewId();
}