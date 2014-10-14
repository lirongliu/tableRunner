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

    public void updateSpeed() {
        velocityY = velocityY + GameEngine.gravity;
    }

    @Override
    public void horizontalCollision(GameObject collidingObj) {
        if (collidingObj instanceof GameCharacter) return;
        if (collidingObj instanceof ThornObstacle) return;
        velocityX = -velocityX; //GameEngine.getSceneSpeed();
    }

    @Override
    public void collisionDownward(double y, GameObject collidingObj) {
        if (collidingObj instanceof GameCharacter) return;
        if (collidingObj instanceof ThornObstacle) return;
        land(y);
    }

    @Override
    public void land(double y) {
        velocityY = 0;
        this.setTranslateY(y);
    }

    // TODO
    public void rotate() {

    }
}
