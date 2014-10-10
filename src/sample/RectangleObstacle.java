package sample;

import javafx.scene.shape.Shape;

/**
 * Created by lirong on 10/3/14.
 */
public class RectangleObstacle extends Obstacle {
    public static int minObstacleWidth = 30;
    public static int maxObstacleWidth = 100;
    public static int minObstacleHeight = 20;
    public static int maxObstacleHeight = 250;

    public RectangleObstacle(Shape obj) {
        super(GameEngine.getSceneSpeed(), 0);
    }
}
