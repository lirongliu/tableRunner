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

    private double w, h;
    private int count;

    public ThornObstacle(double w, double h, int count) {
        super();
        this.w = w;
        this.h = h;
        this.count = count;
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

    @Override
    public ThornObstacle getDeepCopy() {
        System.out.println("thorn deep copy");
        return new ThornObstacle(w, h, count);
    }
}
