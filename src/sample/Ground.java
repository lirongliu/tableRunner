package sample;

import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

/**
 * Created by lirong on 10/3/14.
 */
public class Ground extends Obstacle {
    public static int minGroundLength = 500;
    private double originalSceneGroupTranslate;
    private double originalMaxX;
    private double length;
    public Ground(double posX, double length, double originalSceneGroupTranslate) {
        Line line = new Line(posX, 0,
                posX + length, 0);
        this.length = length;
        originalMaxX = posX + length;
        this.originalSceneGroupTranslate = originalSceneGroupTranslate;
        line.setFill(Color.BLACK);

        getChildren().add(line);
    }

    public double getLength() {
        return length;
    }

    public double getGap(double newSceneGroupTranslate) {
        return Main.SCENE_WIDTH - originalMaxX + (-newSceneGroupTranslate) - (-originalSceneGroupTranslate);    //  Cound be negative
    }
}