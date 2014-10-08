package sample;

import javafx.scene.shape.Shape;

/**
 * Created by lirong on 10/3/14.
 */
public class ThornObstacle extends Obstacle {
    public static int minObstacleWidth = 30;
    public static int maxObstacleWidth = 100;
    public static int minObstacleHeight = 20;
    public static int maxObstacleHeight = 100;
    public static int maxThornCountInARow = 6;

    public ThornObstacle(Shape obj) {
        super(obj, GameEngine.getSceneSpeed(), 0);
    }
}
