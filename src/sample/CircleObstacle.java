package sample;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;

/**
 * Created by lirong on 10/3/14.
 */
public class CircleObstacle extends Obstacle {
    private static double circleObstacleVelocityX = 10 * GameEngine.defaultSceneSpeed;
    private double radius = 30;

    public CircleObstacle() {
        super(circleObstacleVelocityX, 0);
        Circle circle = new Circle(radius, -radius, radius);
        circle.setFill(Color.BLACK);
        getChildren().add(circle);
    }

    @Override
    public void move() {
        super.move();
        rotate();
    }

    // TODO
    public void rotate() {

    }
}
