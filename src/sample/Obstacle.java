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

    public Obstacle(/*Shape obj*/) {
        /*this.obj = obj;*/
        //this.velocityX = GameEngine.getSceneSpeed();
        //this.velocityY = 0.0;
    }

    public Obstacle(/*Shape obj,*/ double vx, double vy) {
        //this.obj = obj;
        this.velocityX = vx;
        this.velocityY = vy;
        //System.out.println("Circle Speed: " + velocityX);
    }

    @Override
    public void updateSpeed() {
        //velocityX = GameEngine.getSceneSpeed();
    }

    @Override
    public void move() {
        setTranslateX(getTranslateX() + velocityX);
        setTranslateY(getTranslateY() + velocityY);
        updateSpeed();
    }

    public void die() {

    }
}
