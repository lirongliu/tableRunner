package sample;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Created by lirong on 10/3/14.
 */
public class RectangleObstacle extends Obstacle {
    public static int minObstacleWidth = 30;
    public static int maxObstacleWidth = 100;
    public static int minObstacleHeight = 20;
    public static int maxObstacleHeight = 250;

    public RectangleObstacle(int width, int height) {
        Rectangle rec = new Rectangle(0, 0, width, height);
        rec.setFill(Color.BLACK);

        rec.setTranslateY(-height);
        getChildren().add(rec);
    }
}
