package sample;

import javafx.scene.shape.Shape;

/**
 * Created by lirong on 10/1/14.
 */
public class Obstacle extends GameObject {
    public static int minObstacleWidth = 30;
    public static int maxObstacleWidth = 100;
    public static int minObstacleHeight = 20;
    public static int maxObstacleHeight = 100;

    public Obstacle() {
        this.velocityX = 0;
        this.velocityY = 0;
    }

    public Obstacle(double vx, double vy) {
        this.velocityX = vx;
        this.velocityY = vy;
    }

    @Override
    public void updateSpeed() {
    }

    @Override
    public void move() {
        setTranslateX(getTranslateX() + velocityX);
        setTranslateY(getTranslateY() + velocityY);
        updateSpeed();
    }

    public void die() {

    }

    @Override
    public Obstacle getDeepCopy() {
        return new Obstacle();
    }
}
