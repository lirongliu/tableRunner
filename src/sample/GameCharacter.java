package sample;

import javafx.scene.shape.Shape;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Circle;
import javafx.scene.Group;

/**
 * Created by lirong on 10/1/14.
 */
public class GameCharacter extends GameObject {
    private boolean jumping;
    private double speedWhenLastAccelerate;
    private long lastJumpStartTime;
    protected static double dampingRatio = 0.2 / Main.fps;  // per second
    public static double radius = 20;

    private Circle head;
    private Group leftLeg;
    private Rectangle leftLegUpper;
    private Rectangle leftLegLower;
    private Group rightLeg;
    private Rectangle rightLegUpper;
    private Rectangle rightLegLower;
    
    private double lastXVelocityDecrease;
    final private double initialXVelocityDampingRatio = 0.002;
    final private double XVelocityDampingRatioMultiplier = 1.05;

    public GameCharacter(Shape obj) {
        velocityX = GameEngine.getSceneSpeed();
        velocityY = 0.0;
        jumping = false;

        head = new Circle(0, -35, 4);

        leftLeg = new Group();
        leftLegUpper = new Rectangle(5, 10);
        leftLegLower = new Rectangle(5, 10);
        leftLegLower.setTranslateY(10);
        leftLeg.setTranslateX(-2);
        leftLeg.setTranslateY(-20);
        leftLeg.getChildren().add(leftLegUpper);
        leftLeg.getChildren().add(leftLegLower);

        rightLeg = new Group();
        rightLegUpper = new Rectangle(5, 10);
        rightLegLower = new Rectangle(5, 10);
        rightLegLower.setTranslateY(10);
        rightLeg.setTranslateX(-2);
        rightLeg.setTranslateY(-20);
        rightLeg.getChildren().add(rightLegUpper);
        rightLeg.getChildren().add(rightLegLower);

        this.getChildren().add(head);
        this.getChildren().add(leftLeg);
        this.getChildren().add(rightLeg);
    }

    public void accelerate(double aX, double aY) {
        if (jumping) return;
        velocityX += aX;
        velocityY += aY;
        speedWhenLastAccelerate = velocityX;
        lastXVelocityDecrease = -aX;
    }

    public void jump(int power) {
        if (jumping) return;
        velocityY -= power / Main.fps;
        jumping = true;
    }

    public void damping() {
        if (!jumping) {
            velocityX -= getSpeedDecreaseRatio() * speedWhenLastAccelerate;
        }
        velocityX -= dampingRatio * (velocityX - GameEngine.defaultSceneSpeed);
        velocityX = (velocityX - GameEngine.defaultSceneSpeed < 0.01) ? GameEngine.defaultSceneSpeed : velocityX;
        velocityY = velocityY + GameEngine.gravity;
    }

    @Override
    public void move() {
       // System.out.println("pos: " + getPositionX());
        if (getTranslateX() > 0 && velocityX > 0) {
            GameEngine.updateSceneSpeed(Math.min(GameEngine.defaultSceneSpeed, -velocityX + GameEngine.defaultSceneSpeed));
            //System.out.println("pass mid point");
        } else {
            this.setTranslateX(this.getTranslateX() + velocityX);
            GameEngine.updateSceneSpeed(GameEngine.defaultSceneSpeed);
        }
        this.setTranslateY(this.getTranslateY() + velocityY);
        updateSpeed();
    }

    public void updateSpeed() {
        damping();
    }

    public boolean onTheGround() {
        return !jumping;
    }

    public void land() {
        jumping = false;
        velocityY = 0;
        this.setTranslateY(Main.SCENE_HEIGHT - Main.GROUND_HEIGHT);
    }

    public double getSpeedDecreaseRatio() {
        if (lastXVelocityDecrease < 0) {
            lastXVelocityDecrease = speedWhenLastAccelerate * initialXVelocityDampingRatio;
        } else {
            lastXVelocityDecrease *= XVelocityDampingRatioMultiplier;
        }
        return lastXVelocityDecrease;
    }

    private long now() {
        return System.nanoTime();
    }
}