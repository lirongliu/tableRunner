package sample;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
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
    public static int minThornCountInARow = 2;

    public ThornObstacle(double w, double h, int count) {
        for (int i = 0;i < count;i++) {
            Polygon triangle = new Polygon();
            triangle.getPoints().addAll(new Double[]{
                            i * w / count, 0.0,
                            (i + 1) * w / count, 0.0,
                            (2 * i + 1) * w / count / 2, -h }
            );
            triangle.setFill(Color.BLACK);
            getChildren().add(triangle);
        }
    }
}
