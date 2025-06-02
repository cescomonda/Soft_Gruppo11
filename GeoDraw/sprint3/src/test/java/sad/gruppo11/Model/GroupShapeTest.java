package sad.gruppo11.Model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sad.gruppo11.Model.geometry.ColorData;
import sad.gruppo11.Model.geometry.Point2D;
import sad.gruppo11.Model.geometry.Rect;
import sad.gruppo11.Model.geometry.Vector2D;
import sad.gruppo11.View.ShapeVisitor;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GroupShapeTest {
    private RectangleShape child1;
    private EllipseShape child2;
    private GroupShape group;

    @BeforeEach
    void setUp() {
        child1 = new RectangleShape(new Rect(0, 0, 10, 10), ColorData.RED, ColorData.RED);
        child2 = new EllipseShape(new Rect(20, 0, 10, 10), ColorData.BLUE, ColorData.BLUE);
        List<Shape> children = new ArrayList<>(Arrays.asList(child1, child2));
        group = new GroupShape(children);
    }

    @Test
    void constructorShouldSetChildrenAndDefaultRotation() {
        assertThat(group.getChildren()).containsExactlyInAnyOrder(child1, child2);
        assertThat(group.getRotation()).isEqualTo(0.0);
        assertThat(group.getId()).isNotNull();
    }
    
    @Test
    void constructorShouldThrowForNullChildrenList() {
        assertThatNullPointerException().isThrownBy(() -> new GroupShape(null));
    }

    @Test
    void moveShouldMoveAllChildren() {
        Vector2D v = new Vector2D(5, 5);
        Rect child1OldBounds = child1.getBounds();
        Rect child2OldBounds = child2.getBounds();

        group.move(v);

        assertThat(child1.getBounds().getTopLeft()).isEqualTo(child1OldBounds.translated(v.getDx(), v.getDy()).getTopLeft());
        assertThat(child2.getBounds().getTopLeft()).isEqualTo(child2OldBounds.translated(v.getDx(), v.getDy()).getTopLeft());
    }

    @Test
    void resizeShouldResizeAndRepositionChildren() {
        // Initial group bounds (approx): (0,0) to (30,10) -> w=30, h=10, center (15,5)
        // child1 (0,0,10,10), center (5,5)
        // child2 (20,0,10,10), center (25,5)
        Rect initialGroupBounds = group.getBounds();
        assertThat(initialGroupBounds).isEqualTo(new Rect(0,0,30,10));


        Rect newGroupBounds = new Rect(0, 0, 60, 20); // Double size
        group.resize(newGroupBounds);

        // Expected scaleX = 60/30 = 2, scaleY = 20/10 = 2
        // Old group center (15,5). New group center (30,10)

        // Child1: old center (5,5). Rel to old group center (-10,0). Scaled rel (-20,0).
        // New child1 center: (30-20, 10+0) = (10,10).
        // New child1 width/height: 10*2=20, 10*2=20.
        // New child1 bounds: (10-10, 10-10, 20, 20) = (0,0,20,20)
        assertThat(child1.getBounds()).isEqualTo(new Rect(0, 0, 20, 20));

        // Child2: old center (25,5). Rel to old group center (10,0). Scaled rel (20,0).
        // New child2 center: (30+20, 10+0) = (50,10).
        // New child2 width/height: 10*2=20, 10*2=20.
        // New child2 bounds: (50-10, 10-10, 20, 20) = (40,0,20,20)
        assertThat(child2.getBounds()).isEqualTo(new Rect(40, 0, 20, 20));
        
        // Check final group bounds
        assertThat(group.getBounds()).isEqualTo(new Rect(0,0,60,20));
    }
    
    @Test
    void resizeWithZeroDimensionGroupBoundsShouldNotThrow() {
        child1.resize(new Rect(0,0,0,0)); // Make child1 degenerate
        child2.resize(new Rect(0,0,0,0)); // Make child2 degenerate
        group = new GroupShape(Arrays.asList(child1, child2)); // group bounds now 0,0,0,0
        
        assertThat(group.getBounds().getWidth()).isEqualTo(0);
        assertThat(group.getBounds().getHeight()).isEqualTo(0);
        
        Rect newGroupBounds = new Rect(0,0,10,10);
        // Should not throw DivisionByZero or other errors, as per implementation check
        assertThatCode(() -> group.resize(newGroupBounds)).doesNotThrowAnyException();
    }


    @Test
    void setColorShouldApplyToAllChildren() {
        ColorData newStroke = ColorData.BLUE;
        ColorData newFill = ColorData.RED;
        group.setStrokeColor(newStroke);
        group.setFillColor(newFill);
        assertThat(child1.getStrokeColor()).isEqualTo(newStroke);
        assertThat(child1.getFillColor()).isEqualTo(newFill);
        assertThat(child2.getStrokeColor()).isEqualTo(newStroke);
        assertThat(child2.getFillColor()).isEqualTo(newFill);
    }

    @Test
    void getColorShouldReturnFirstChildsColorOrDefault() {
        assertThat(group.getStrokeColor()).isEqualTo(child1.getStrokeColor());
        assertThat(group.getFillColor()).isEqualTo(child1.getFillColor());

        GroupShape emptyGroup = new GroupShape(new ArrayList<>());
        assertThat(emptyGroup.getStrokeColor()).isEqualTo(ColorData.TRANSPARENT);
        assertThat(emptyGroup.getFillColor()).isEqualTo(ColorData.TRANSPARENT);
    }

    @Test
    void containsShouldWorkForUnrotatedGroup() {
        assertThat(group.contains(new Point2D(5, 5))).isTrue(); // Inside child1
        assertThat(group.contains(new Point2D(25, 5))).isTrue(); // Inside child2
        assertThat(group.contains(new Point2D(15, 5))).isFalse(); // Between children
    }

    @Test
    void containsShouldWorkForRotatedGroup() {
        group.setRotation(90); // Rotates around group center (15,5)
        // Point (5,5) is in child1, center of child1. Rel to group center (-10,0)
        // Rotated by -90deg: (0,-10). Absolute: (15+0, 5-10) = (15, -5)
        // So, if we test (15,-5), it should be true.
        assertThat(group.contains(new Point2D(15, -5))).isTrue();
        assertThat(group.contains(new Point2D(0,0))).isFalse();
    }

    @Test
    void getBoundsShouldEncompassChildrenRotatedBounds() {
        // child1 (0,0,10,10), child2 (20,0,10,10)
        Rect bounds = group.getBounds();
        assertThat(bounds.getX()).isEqualTo(0);
        assertThat(bounds.getY()).isEqualTo(0);
        assertThat(bounds.getWidth()).isEqualTo(30); // (20+10) - 0
        assertThat(bounds.getHeight()).isEqualTo(10);
        
        child1.setRotation(90); // bounds of child1 (0,0,10,10) rotated is still (0,0,10,10) because it's a square centered at (5,5)
                               // AABB of rotated child1 is still (0,0,10,10)
        // bounds of child2 (20,0,10,10)
        // AABB of group should be (0,0,30,10)
        group = new GroupShape(Arrays.asList(child1, child2)); // re-create to re-evaluate children
        bounds = group.getBounds();
        assertThat(bounds).isEqualTo(new Rect(0,0,30,10));
    }
    
    @Test
    void getRotatedBoundsShouldEncompassRotatedChildrenConsideringGroupRotation() {
        // Group initial AABB: (0,0) to (30,10), center (15,5)
        // Child1: (0,0,10,10), center (5,5)
        // Child2: (20,0,10,10), center (25,5)
        group.setRotation(90); // Rotate group around (15,5)
        
        Rect rotatedGroupAABB = group.getRotatedBounds();
        // Expected AABB: group width 10, height 30. Centered at (15,5).
        // TopLeft: (15-10/2, 5-30/2) = (10, -10)
        assertThat(rotatedGroupAABB.getX()).isEqualTo(10, within(1e-6));
        assertThat(rotatedGroupAABB.getY()).isEqualTo(-10, within(1e-6));
        assertThat(rotatedGroupAABB.getWidth()).isEqualTo(10, within(1e-6));
        assertThat(rotatedGroupAABB.getHeight()).isEqualTo(30, within(1e-6));
    }

    @Test
    void cloneAndCloneWithNewId() {
        GroupShape groupClone = (GroupShape) group.clone();
        assertThat(groupClone.getId()).isEqualTo(group.getId());
        assertThat(groupClone.getChildren().size()).isEqualTo(2);
        assertThat(groupClone.getChildren().get(0).getId()).isEqualTo(child1.getId()); // Children cloned with same ID

        GroupShape groupCloneNewId = (GroupShape) group.cloneWithNewId();
        assertThat(groupCloneNewId.getId()).isNotEqualTo(group.getId());
        assertThat(groupCloneNewId.getChildren().size()).isEqualTo(2);
        assertThat(groupCloneNewId.getChildren().get(0).getId()).isNotEqualTo(child1.getId()); // Children cloned with new IDs
    }
    
    @Test
    void compositeMethodsShouldWork() {
        LineSegment child3 = new LineSegment(new Point2D(0,0), new Point2D(1,1), ColorData.BLACK);
        group.add(child3);
        assertThat(group.getChildren()).hasSize(3).contains(child3);
        assertThat(group.getChild(2)).isEqualTo(child3);
        
        group.remove(child1);
        assertThat(group.getChildren()).hasSize(2).doesNotContain(child1);
        
        assertThat(group.isComposite()).isTrue();
        
        assertThatThrownBy(() -> group.getChild(5)).isInstanceOf(IndexOutOfBoundsException.class);
    }
}