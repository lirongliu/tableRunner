package sample;

import javafx.scene.Group;

/**
 * Created by lirong on 10/1/14.
 */
public abstract class GameObject extends Group {
    protected double velocityX, velocityY;

    public void setVelocity(double x, double y) {
        velocityX = x;
        velocityY = y;
    }

    public double getVelocityX() {
        return velocityX;
    }

    public double getVelocityY() {
        return velocityY;
    }

    abstract void move();

    abstract void updateSpeed();

    abstract void die();
}
