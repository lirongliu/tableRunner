package sample;

import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

/**
 * Created by lirong on 10/3/14.
 */
public class Ground extends Obstacle {
    public static int minGroundLength = 500;
    private double originalMaxX;
    private double length;
    private double posX;
    public Ground(double posX, double length) {
        super();
        Rectangle rec = new Rectangle(posX, 0, length, Main.GROUND_HEIGHT);
        rec.setStroke(Color.BLACK);
        rec.setFill(Color.WHITE);
        originalMaxX = posX + length;
        this.length = length;
        this.posX = posX;

        getChildren().add(rec);
    }

    public double getLength() {
        return length;
    }

    public double getGap(double newSceneGroupTranslate) {
        return Main.SCENE_WIDTH - newSceneGroupTranslate - originalMaxX;    //  Could be negative
    }

    @Override
    public Ground getDeepCopy() {
        return new Ground(this.posX, this.length);
    }
}