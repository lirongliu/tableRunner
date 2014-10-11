package sample;

import javafx.animation.AnimationTimer;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.shape.Shape;

import java.util.Queue;
import java.util.Random;

/**
 * Created by lirong on 10/3/14.
 */
public class GameEngine {
    final public static int fps = 50;   //  max fps
    final public static double gravity = 80.0 / fps;
    final public static double defaultSceneSpeed = -20.0 / fps;      //  x coordinate

    private static double sceneSpeed;      //  x coordinate

    private GameCharacter gameCharacter;
    private SceneController sceneController;
    private Scene scene;

    private long lastSceneUpdateTime;
    private double lastObstacleGeneratingDistance;  //  absolute value
    private double cumulativeSceneDistance; //  absolute value
    private double minObstacleInterval;  //  distance
    private double maxObstacleInterval;  //  distance

    final public static double maxJumpingPower = 1500;     //  Increased Y velocity

    // Event handling variables
    private long lastJumpPressTime;     //  Last time the jump button was pressed
    private boolean jumpKeyRelease = true;

    private long maxJumpPressTime = (long)1e9 / 12;      //  If the press time exceeds this value, jump will be triggered automatically.

    private Random rand = new Random();

    private EventHandler<KeyEvent> pressHandler;
    private EventHandler<KeyEvent> releaseHandler;

    private Queue<Obstacle> obstacleQueue;
    private Queue<Ground> groundQueue;


    private Group cloudGroup;
    private Group sceneGroup;


    public GameEngine(Scene scene, SceneController sceneController, GameCharacter gameCharacter) {
        this.scene = scene;
        this.gameCharacter = gameCharacter;
        this.sceneController = sceneController;
        obstacleQueue = sceneController.getObstacleQueue();
        groundQueue = sceneController.getGroundQueue();

        cloudGroup = sceneController.getCloudGroup();
        sceneGroup = sceneController.getSceneGroup();
    }

    public void setup() {
        lastSceneUpdateTime = System.nanoTime();
        lastObstacleGeneratingDistance = 0;
        cumulativeSceneDistance = 0;
        minObstacleInterval = 300;
        maxObstacleInterval = 1000;
        sceneSpeed = defaultSceneSpeed;

        pressHandler = new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                keyEvent.consume();
                if (!jumpKeyRelease) return;
                if (keyEvent.getCode() == KeyCode.SPACE) {
                    lastJumpPressTime = timeNow();
                    jumpKeyRelease = false;
                } else if (keyEvent.getCode() == KeyCode.K) {
                    //  TODO: implement kicking
                }
            }
        };

        releaseHandler = new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                long now = timeNow();
                if (keyEvent.getCode() == KeyCode.SPACE) {     //  jump
                    double power;
                    long totalJumpPressTime = now - lastJumpPressTime;

                    // TODO: do it better
                    power = Math.min(maxJumpingPower, (double)totalJumpPressTime / maxJumpPressTime * maxJumpingPower);

                    gameCharacter.jump(power);
                    jumpKeyRelease = true;

                } else if (keyEvent.getCode() == KeyCode.RIGHT || keyEvent.getCode() == KeyCode.DOWN) {   //  accelerate
                    gameCharacter.accelerate(100 / fps, 0);
                } else if (keyEvent.getCode() == KeyCode.P) {

                    //System.out.println(gameCharacter.getShapeObject().localToParent(gameCharacter.getShapeObject().getLayoutBounds()).getMaxX());
                    //System.out.println(gameCharacter.getShapeObject().localToParent(gameCharacter.getShapeObject().getLayoutBounds()).getMaxY());
                    //System.out.println(gameCharacter.getPositionX() + " " + gameCharacter.getPositionY());
                }
                keyEvent.consume();
            }
        };

        scene.addEventHandler(KeyEvent.KEY_PRESSED, getPressHandler());
        scene.addEventHandler(KeyEvent.KEY_RELEASED, getReleaseHandler());
    }

    public void run() {
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (now - lastSceneUpdateTime < 1e9 / Main.fps) return;

                if (!jumpKeyRelease && System.nanoTime() - lastJumpPressTime > maxJumpPressTime) {
                    gameCharacter.jump(maxJumpingPower);
                    jumpKeyRelease = true;
                    lastJumpPressTime = now;
                }
                updateGroundCollision();
                //updateWithGroundCollision();
                updateObstaclesAndGround();
                updateGameCharacter();
                sceneController.clearOutdatedObstacles();

                lastSceneUpdateTime = now;

                if (cumulativeSceneDistance - lastObstacleGeneratingDistance >
                        minObstacleInterval + rand.nextInt((int)(maxObstacleInterval - minObstacleInterval))) {
                    sceneController.generateObstacle();
                    //System.out.println(obstacleQueue.size());
                    lastObstacleGeneratingDistance = cumulativeSceneDistance;
                }
            }
        }.start();
    }

    // TODO: make sure it's correct
    void updateGroundCollision() {

        if(gameCharacter.velocityY + gameCharacter.getTranslateY() > Main.SCENE_HEIGHT - Main.GROUND_HEIGHT) {
            gameCharacter.land();
        }

        /*//Shape gc = gameCharacter.getShapeObject();
        GameCharacter gc = gameCharacter;

        Bounds gcBounds = gc.localToParent(gc.getLayoutBounds());
        double maxY = gcBounds.getMaxY();
        double centerX = gcBounds.getMaxX() + GameCharacter.radius;
        for (Ground ground : groundQueue) {
            Bounds groundBounds = ground.getShapeObject().localToParent(ground.getShapeObject().getLayoutBounds());
            if (centerX > groundBounds.getMinX() &&
                centerX < groundBounds.getMaxX() &&
                    maxY + gameCharacter.velocityY > groundBounds.getMinY()) {
                gameCharacter.land();
            }
        }*/
    }

    void updateGameCharacter() {
        gameCharacter.move();
    }

    void updateObstaclesAndGround() {
        for (Obstacle obj : obstacleQueue) {
            obj.move();
        }
        for (Obstacle obj : groundQueue) {
            obj.move();
        }

        cloudGroup.setTranslateX((cumulativeSceneDistance % 256) * -1);
        sceneGroup.setTranslateX(-cumulativeSceneDistance);

        cumulativeSceneDistance += Math.abs(sceneSpeed);
    }

    public EventHandler<KeyEvent> getPressHandler() {
        return pressHandler;
    }

    public EventHandler<KeyEvent> getReleaseHandler() {
        return releaseHandler;
    };

    public static double getSceneSpeed() {
        return sceneSpeed;
    }

    public static void updateSceneSpeed(double s) {
        sceneSpeed = s;
    }

    public long timeNow() { return System.nanoTime(); }
}
