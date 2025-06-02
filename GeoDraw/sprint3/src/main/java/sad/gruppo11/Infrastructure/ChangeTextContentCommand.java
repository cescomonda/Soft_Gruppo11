package sad.gruppo11.Infrastructure;

import java.util.Objects;

import sad.gruppo11.Model.Drawing;
import sad.gruppo11.Model.Shape;
import sad.gruppo11.Model.TextShape;

public class ChangeTextContentCommand extends AbstractShapeCommand {
    private final String newContent;
    private String oldContent;

    public ChangeTextContentCommand(Drawing drawing, Shape textShape, String newContent) {
        super(drawing, textShape);
        if (!(textShape instanceof TextShape)) {
            throw new IllegalArgumentException("ChangeTextContentCommand requires a TextShape.");
        }
        Objects.requireNonNull(newContent, "New content cannot be null.");
        this.newContent = newContent;
    }

    @Override
    public void execute() {
        TextShape ts = (TextShape) receiverShape;
        if(this.oldContent == null)
            this.oldContent = ts.getText();
        ts.setText(newContent);
    }

    @Override
    public void undo() {
        TextShape ts = (TextShape) receiverShape;
        ts.setText(oldContent);
    }

    @Override
    public String toString() {
        return "ChangeTextContentCommand{shapeId=" + receiverShape.getId().toString() + ", newContent='" + newContent + "'}";
    }
    
}
