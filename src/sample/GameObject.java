package sample;

import javafx.scene.shape.Shape;
import javafx.scene.Group;

/**
 * Created by lirong on 10/1/14.
 */
public abstract class GameObject extends Group {
    protected Shape obj;
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

    public Shape getShapeObject() {
        return obj;
    }

    abstract void move();

    abstract void updateSpeed();

    // TODO: make it correct
    public double getPositionX() {
        return obj.getLayoutX() + obj.getTranslateX();
    }

    // TODO: make it correct
    public double getPositionY() {
        return obj.getLayoutY() + obj.getTranslateY();
    }
}
