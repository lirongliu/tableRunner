package sample;

import javafx.animation.AnimationTimer;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;

import java.util.Iterator;
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
    private double minObstacleInterval = 300;  //  distance
    private double maxObstacleInterval = 1000;  //  distance
    private double acceleration = 250;

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

    private double minGroundGapLength = 50;
    private int groundGapLengthVariation = 200;
    private double groundGapLength = rand.nextInt(groundGapLengthVariation) + minGroundGapLength;

    private double obstacleGapLength = minObstacleInterval + rand.nextInt((int)(maxObstacleInterval - minObstacleInterval));


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
                    kick();
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
                    gameCharacter.accelerate(acceleration / fps, 0);
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
                updateCollision();
                updateObstaclesAndGround();
                updateGameCharacter();

                // Now update the scene position
                cloudGroup.setTranslateX((cumulativeSceneDistance % 256) * -1);
                sceneGroup.setTranslateX(-cumulativeSceneDistance);

                double characterOffset = gameCharacter.getTranslateX() + sceneGroup.getTranslateX();

                if(characterOffset > 512) {
                    cumulativeSceneDistance += characterOffset - 512;
                }

                cumulativeSceneDistance += Math.abs(sceneSpeed);


                sceneController.clearOutdatedObstacles();

                lastSceneUpdateTime = now;

                Ground ground = sceneController.getLastGroundObject();

                double groundGap = ground.getGap(sceneGroup.getTranslateX());
                if (groundGap > groundGapLength) {
                    sceneController.drawGround(Main.SCENE_WIDTH - sceneGroup.getTranslateX(), rand.nextInt(2000) + Ground.minGroundLength, Color.BLACK);
                    groundGapLength = rand.nextInt(groundGapLengthVariation) + minGroundGapLength;
                }

                if (cumulativeSceneDistance - lastObstacleGeneratingDistance > obstacleGapLength && groundGap < -100) {
                    sceneController.generateObstacle();
                    lastObstacleGeneratingDistance = cumulativeSceneDistance;
                    obstacleGapLength = minObstacleInterval + rand.nextInt((int)(maxObstacleInterval - minObstacleInterval));
                }
            }
        }.start();
    }

    void updateObstacleCollision(GameObject obj, Obstacle obstacle) {
        if (obj == obstacle) return;

        double objMinX = obj.getBoundsInParent().getMinX();
        double objMaxX = obj.getBoundsInParent().getMaxX();
        double objMinY = obj.getBoundsInParent().getMinY();
        double objMaxY = obj.getBoundsInParent().getMaxY();
        double obstacleMinX = obstacle.getBoundsInParent().getMinX();
        double obstacleMaxX = obstacle.getBoundsInParent().getMaxX();
        double obstacleMinY = obstacle.getBoundsInParent().getMinY();
        double obstacleMaxY = obstacle.getBoundsInParent().getMaxY();

        // p ----- q     r ----- s
        // if ((q - p) * (r - q) * (s - r) > 0) ==> not intersect
        // if ((q - p) * (r - q) * (s - r) <= 0) ==> intersect

        double px, qx, rx, sx;
        double py, qy, ry, sy;
        if (objMinX < obstacleMinX) {
            px = objMinX + obj.getVelocityX();
            qx = objMaxX + obj.getVelocityX();
            rx = obstacleMinX + obstacle.getVelocityX();
            sx = obstacleMaxX + obstacle.getVelocityX();
        } else {
            rx = objMinX + obj.getVelocityX();
            sx = objMaxX + obj.getVelocityX();
            px = obstacleMinX + obstacle.getVelocityX();
            qx = obstacleMaxX + obstacle.getVelocityX();
        }

        if (objMinY < obstacleMinY) {
            py = objMinY + obj.getVelocityY();
            qy = objMaxY + obj.getVelocityY();
            ry = obstacleMinY + obstacle.getVelocityY();
            sy = obstacleMaxY + obstacle.getVelocityY();
        } else {
            ry = objMinY + obj.getVelocityY();
            sy = objMaxY + obj.getVelocityY();
            py = obstacleMinY + obstacle.getVelocityY();
            qy = obstacleMaxY + obstacle.getVelocityY();
        }

//            System.out.println("(qx - px) * (rx - qx) * (sx - rx): " + (qx - px) * (rx - qx) * (sx - rx));
//            System.out.println("(qy - py) * (ry - qy) * (sy - ry): " + (qy - py) * (ry - qy) * (sy - ry));

        if ((qx - px) * (rx - qx) * (sx - rx) <= 0 && (qy - py) * (ry - qy) * (sy - ry) <= 0) {
            if (objMaxX <= obstacleMinX || objMinX >= obstacleMaxX) {
                obj.horizontalCollision(obstacle);
                obstacle.horizontalCollision(obj);
            } else if (objMaxY >= obstacleMinY) {
                obj.collisionDownward(obstacleMinY, obstacle);
            }

//            if ((obstacle instanceof Ground) || (obstacle instanceof RectangleObstacle)) {
//                if (objMaxX <= obstacleMinX || objMinX >= obstacleMaxX) {
//                    obj.horizontalCollision(false);
//                } else if (objMaxY >= obstacleMinY) {
//                    obj.collisionDownward(obstacleMinY, false);
//                }
//            } else if ((obj instanceof GameCharacter) && (obstacle instanceof CircleObstacle)) {
//                if (objMaxX <= obstacleMinX || objMinX >= obstacleMaxX) {
//                    obj.horizontalCollision(true);
//                } else if (objMaxY >= obstacleMinY) {
//                    obj.collisionDownward(obstacleMinY, false);
//                }
//            } else if ((obj instanceof GameCharacter) && (obstacle instanceof ThornObstacle)) {
//                obj.die();
//            }
        }
    }

    void updateCollision() {
        for (Obstacle obstacle : obstacleQueue) {
            updateObstacleCollision(gameCharacter, obstacle);
        }

        for (Obstacle ground : groundQueue) {
            updateObstacleCollision(gameCharacter, ground);
        }

        Iterator<Obstacle> iter = obstacleQueue.iterator();
        while (iter.hasNext()) {
            Obstacle obj = iter.next();
            if (obj instanceof CircleObstacle) {

                for (Obstacle obstacle : obstacleQueue) {
                    updateObstacleCollision(obj, obstacle);
                }

                for (Obstacle ground : groundQueue) {
                    updateObstacleCollision(obj, ground);
                }
            }
        }
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

    public double getCumulativeSceneDistance() {
        return cumulativeSceneDistance;
    }

    public void kick() {
        System.out.println("kick in GameEngine");
        double gcPosMinX = gameCharacter.getBoundsInParent().getMinX();
        double gcPosMaxX = gameCharacter.getBoundsInParent().getMaxX();
        double gcPosMinY = gameCharacter.getBoundsInParent().getMinY();
        double gcPosMaxY = gameCharacter.getBoundsInParent().getMaxY();
        Iterator<Obstacle> iter = obstacleQueue.iterator();
        Obstacle obstacleToRemove = null;
        while (iter.hasNext()) {
            Obstacle obstacle = iter.next();
            if (obstacle instanceof RectangleObstacle) {
                double obstacleMinX = obstacle.getBoundsInParent().getMinX();
                double obstacleMaxX = obstacle.getBoundsInParent().getMaxX();
                double obstacleMinY = obstacle.getBoundsInParent().getMinY();
                double obstacleMaxY = obstacle.getBoundsInParent().getMaxY();
                System.out.println("gcPosMaxX: " + gcPosMaxX);
                System.out.println("gcPosMaxY: " + gcPosMaxY);
                System.out.println("obstacleMinX: " + obstacleMinX);
                System.out.println("obstacleMaxY: " + obstacleMaxY);
                if (gcPosMaxX > obstacleMinX + 3) return;
                if (gcPosMaxY < -3) return;
                if (obstacleMaxY < -3) return;
                if (obstacleMinX - gcPosMaxX <= 10) {
                    obstacle.die();
                    iter.remove();
                    break;
                }
            }
        }
    }
}
