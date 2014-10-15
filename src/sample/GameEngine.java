package sample;

import javafx.animation.AnimationTimer;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Pair;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

/**
 * Created by lirong on 10/3/14.
 */
public class GameEngine {
    final public static int fps = 50;   //  max fps
    final public static double gravity = 80.0 / fps;
    final public static double defaultSceneSpeed = -20.0 / fps;      //  x coordinate

    private static double sceneSpeed[];      //  x coordinate

    private GameCharacter gameCharacter[];
    private SceneController sceneController;
    private Rectangle[] clippingRects;
    private Scene scene;

    private long lastSceneUpdateTime;
    private double lastObstacleGeneratingDistance[];  //  absolute value
    private double cumulativeSceneDistance[]; //  absolute value
    private double minObstacleInterval = 300;  //  distance
    private double maxObstacleInterval = 1000;  //  distance
    private double acceleration = 250;

    final public static double maxJumpingPower = 1500;     //  Increased Y velocity

    // Event handling variables
    private long lastJumpPressTime[];     //  Last time the jump button was pressed
    private boolean jumpKeyRelease[];

    private long maxJumpPressTime = (long)1e9 / 12;      //  If the press time exceeds this value, jump will be triggered automatically.

    private Random rand = new Random();

    private EventHandler<KeyEvent> pressHandler;
    private EventHandler<KeyEvent> releaseHandler;

    private Queue<Obstacle> obstacleQueue[];
    //private Queue<Ground> groundQueue[];
    private Queue<Pair<Double, Obstacle>> bufferedObstacles;


    private Group cloudGroup[];
    private Group sceneGroup[];

    private double minGroundGapLength = 50;
    private int groundGapLengthVariation = 200;
    private double groundGapLength = rand.nextInt(groundGapLengthVariation) + minGroundGapLength;

    private double obstacleGapLength = minObstacleInterval + rand.nextInt((int)(maxObstacleInterval - minObstacleInterval));

    private int numOfPlayers;




    public GameEngine(Scene scene, SceneController sceneController, GameCharacter[] gameCharacter, int numOfPlayers) {
        lastJumpPressTime = new long[numOfPlayers];
        jumpKeyRelease = new boolean[numOfPlayers];
        for (int i = 0;i < numOfPlayers;i++) {
            jumpKeyRelease[i] = true;
        }
        lastObstacleGeneratingDistance = new double[numOfPlayers];
        cumulativeSceneDistance = new double[numOfPlayers];
        sceneSpeed = new double[numOfPlayers];

        bufferedObstacles = new LinkedList<Pair<Double, Obstacle>>();
        obstacleQueue = new LinkedList[numOfPlayers];
        //groundQueue = new LinkedList[numOfPlayers];
        this.gameCharacter = new GameCharacter[numOfPlayers];
        this.cloudGroup = new Group[numOfPlayers];
        this.sceneGroup = new Group[numOfPlayers];
        this.clippingRects = new Rectangle[numOfPlayers];
        this.numOfPlayers = numOfPlayers;
        this.scene = scene;
        this.sceneController = sceneController;

        for (int i = 0;i < numOfPlayers;i++) {
            this.gameCharacter[i] = gameCharacter[i];
            obstacleQueue[i] = sceneController.getObstacleQueue(i);
            //groundQueue[i] = sceneController.getGroundQueue(i);
            cloudGroup[i] = sceneController.getCloudGroup(i);
            sceneGroup[i] = sceneController.getSceneGroup(i);
            clippingRects[i] = sceneController.getClippingRect(i);
        }
    }

    public void setup() {
        for (int i = 0;i < numOfPlayers;i++) {
            lastSceneUpdateTime = System.nanoTime();
            lastObstacleGeneratingDistance[i] = 0;
            cumulativeSceneDistance[i] = 0;
            sceneSpeed[i] = defaultSceneSpeed;
        }

        pressHandler = new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                keyEvent.consume();
                if (keyEvent.getCode() == KeyCode.SPACE) {  //  player 1 jumpKeyRelease
                    if (!jumpKeyRelease[0]) return;
                    lastJumpPressTime[0] = timeNow();
                    jumpKeyRelease[0] = false;
                } else if (keyEvent.getCode() == KeyCode.K) {   // player 1 kicks
                    if (!jumpKeyRelease[0]) return;
                    kick(0);
                } else if (keyEvent.getCode() == KeyCode.Q) {   //  player 2 jumpKeyRelease
                    if (numOfPlayers < 2) return;
                    if (!jumpKeyRelease[1]) return;
                    lastJumpPressTime[1] = timeNow();
                    jumpKeyRelease[1] = false;
                } else if (keyEvent.getCode() == KeyCode.A) {   //  player 2 kicks
                    if (numOfPlayers < 2) return;
                    if (!jumpKeyRelease[1]) return;
                    kick(1);
                }
            }
        };

        releaseHandler = new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                long now = timeNow();
                if (keyEvent.getCode() == KeyCode.SPACE) {     //  Player 1 jumps
                    double power;
                    long totalJumpPressTime = now - lastJumpPressTime[0];

                    // TODO: do it better
                    power = Math.min(maxJumpingPower, (double)totalJumpPressTime / maxJumpPressTime * maxJumpingPower);

                    gameCharacter[0].jump(power);
                    jumpKeyRelease[0] = true;


                } else if (keyEvent.getCode() == KeyCode.RIGHT || keyEvent.getCode() == KeyCode.DOWN) {   // Player 1 accelerates
                    gameCharacter[0].accelerate(acceleration / fps, 0);
                } else if (keyEvent.getCode() == KeyCode.Q) {   //Player 2 jumps//  Player 1 jumps
                    double power;
                    long totalJumpPressTime = now - lastJumpPressTime[1];

                    // TODO: do it better
                    power = Math.min(maxJumpingPower, (double)totalJumpPressTime / maxJumpPressTime * maxJumpingPower);

                    gameCharacter[1].jump(power);
                    jumpKeyRelease[1] = true;

                } else if (keyEvent.getCode() == KeyCode.S || keyEvent.getCode() == KeyCode.D) {    // Player 2 accelerates
                    gameCharacter[1].accelerate(100 / fps, 0);
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

                for (int i = 0;i < numOfPlayers;i++) {
                    if (!jumpKeyRelease[i] && System.nanoTime() - lastJumpPressTime[i] > maxJumpPressTime) {
                        gameCharacter[i].jump(maxJumpingPower);
                        jumpKeyRelease[i] = true;
                        lastJumpPressTime[i] = now;
                    }
                    updateCollision(i);
                    updateObstaclesAndGround(i);
                    updateGameCharacter(i);

                    // Now update the scene position
                    cloudGroup[i].setTranslateX((cumulativeSceneDistance[i] % 256) * -1);
                    sceneGroup[i].setTranslateX(-cumulativeSceneDistance[i]);

                    clippingRects[i].setTranslateX(cumulativeSceneDistance[i]);

                    double characterOffset = gameCharacter[i].getTranslateX() + sceneGroup[i].getTranslateX();

                    if (characterOffset > 512) {
                        cumulativeSceneDistance[i] += characterOffset - 512;
                    }

                    cumulativeSceneDistance[i] += Math.abs(sceneSpeed[i]);


                    sceneController.clearOutdatedObstacles(i);


//                    Ground ground = sceneController.getLastGroundObject(i);
//
//                    double groundGap = ground.getGap(sceneGroup[i].getTranslateX());
//                    if (groundGap > groundGapLength) {
//                        sceneController.drawGround(i, Main.SCENE_WIDTH - sceneGroup[i].getTranslateX(), rand.nextInt(2000) + Ground.minGroundLength, Color.BLACK);
//                        groundGapLength = rand.nextInt(groundGapLengthVariation) + minGroundGapLength;
//                    }
//
//                    if (cumulativeSceneDistance[i] - lastObstacleGeneratingDistance[i] > obstacleGapLength && groundGap < -100) {
//                        sceneController.generateObstacle(i);
//                        lastObstacleGeneratingDistance[i] = cumulativeSceneDistance[i];
//                        obstacleGapLength = minObstacleInterval + rand.nextInt((int) (maxObstacleInterval - minObstacleInterval));
//                    }
                }
                if (numOfPlayers == 2) {
                    int fasterPlayerIndex = cumulativeSceneDistance[1] - cumulativeSceneDistance[0] > 0 ? 1 : 0;
                    int slowerPlayerIndex = 1 - fasterPlayerIndex;
                    Ground ground = sceneController.getLastGroundObject(fasterPlayerIndex);

                    double groundGap = ground.getGap(sceneGroup[fasterPlayerIndex].getTranslateX());
                    if (groundGap > groundGapLength) {
                        Ground newGround = sceneController.drawGround(
                                fasterPlayerIndex,
                                Main.SCENE_WIDTH - sceneGroup[fasterPlayerIndex].getTranslateX(),
                                rand.nextInt(2000) + Ground.minGroundLength);
                        groundGapLength = rand.nextInt(groundGapLengthVariation) + minGroundGapLength;

                        bufferedObstacles.add(new Pair<Double, Obstacle>(cumulativeSceneDistance[fasterPlayerIndex], newGround.getDeepCopy()));
                    }

                    if (cumulativeSceneDistance[fasterPlayerIndex] - lastObstacleGeneratingDistance[fasterPlayerIndex] > obstacleGapLength && groundGap < -100) {
                        Obstacle newObstacle = sceneController.generateObstacle(fasterPlayerIndex);
                        lastObstacleGeneratingDistance[fasterPlayerIndex] = cumulativeSceneDistance[fasterPlayerIndex];
                        obstacleGapLength = minObstacleInterval + rand.nextInt((int) (maxObstacleInterval - minObstacleInterval));
                        bufferedObstacles.add(new Pair<Double, Obstacle>(cumulativeSceneDistance[fasterPlayerIndex], newObstacle.getDeepCopy()));
                    }

                    if (bufferedObstacles.isEmpty() == false) {
                        if (cumulativeSceneDistance[slowerPlayerIndex] >= bufferedObstacles.peek().getKey()) {

                            Obstacle firstObstacle = bufferedObstacles.peek().getValue();
                            obstacleQueue[slowerPlayerIndex].add(firstObstacle);
                            if (firstObstacle instanceof Ground) {
                                sceneController.setLastGroundObject(slowerPlayerIndex, (Ground) firstObstacle);
                            } else {
                                firstObstacle.setTranslateX(Math.abs(sceneGroup[slowerPlayerIndex].getTranslateX()) + Main.SCENE_WIDTH + SceneController.newObstaclePositionOffsetToScene);
                            }
                            sceneGroup[slowerPlayerIndex].getChildren().add(firstObstacle);
                            bufferedObstacles.remove();     //  remove the head of bufferedObstacles
//                            System.out.println("post bufferedObstacles size: " + bufferedObstacles.size());
                            System.out.println("bufferedObstacles size: " + bufferedObstacles.size());
                            System.out.println("obstacleQueue[0] size: " + obstacleQueue[0].size());
                            System.out.println("obstacleQueue[1] size: " + obstacleQueue[1].size());
                        }
                    }
                }
                lastSceneUpdateTime = now;
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
        }
    }

    void updateCollision(int i) {
        for (Obstacle obstacle : obstacleQueue[i]) {
            updateObstacleCollision(gameCharacter[i], obstacle);
        }

//        for (Obstacle ground : groundQueue[i]) {
//            updateObstacleCollision(gameCharacter[i], ground);
//        }

        Iterator<Obstacle> iter = obstacleQueue[i].iterator();
        while (iter.hasNext()) {
            Obstacle obj = iter.next();
            if (obj instanceof CircleObstacle) {

                for (Obstacle obstacle : obstacleQueue[i]) {
                    updateObstacleCollision(obj, obstacle);
                }

//                for (Obstacle ground : groundQueue[i]) {
//                    updateObstacleCollision(obj, ground);
//                }
            }
        }
    }

    void updateGameCharacter(int i) {
        gameCharacter[i].move();
    }

    void updateObstaclesAndGround(int i) {
        for (Obstacle obj : obstacleQueue[i]) {
            obj.move();
        }
    }

    public EventHandler<KeyEvent> getPressHandler() {
        return pressHandler;
    }

    public EventHandler<KeyEvent> getReleaseHandler() {
        return releaseHandler;
    };

    public static double getSceneSpeed(int i) {
        return sceneSpeed[i];
    }

    public static void updateSceneSpeed(int i, double s) {
        sceneSpeed[i] = s;
    }

    public long timeNow() { return System.nanoTime(); }

    public double getCumulativeSceneDistance(int i) {
        return cumulativeSceneDistance[i];
    }

    public void kick(int i) {
        System.out.println("kick in GameEngine");
        double gcMaxX = gameCharacter[i].getBoundsInParent().getMaxX();
        double gcMaxY = gameCharacter[i].getBoundsInParent().getMaxY();
        double gcMinX = gameCharacter[i].getBoundsInParent().getMinX();
        double gcMinY = gameCharacter[i].getBoundsInParent().getMinY();
        Iterator<Obstacle> iter = obstacleQueue[i].iterator();
        while (iter.hasNext()) {
            Obstacle obstacle = iter.next();
            if (obstacle instanceof RectangleObstacle) {
                double obstacleMinX = obstacle.getBoundsInParent().getMinX();
                double obstacleMaxY = obstacle.getBoundsInParent().getMaxY();
                double obstacleMaxX = obstacle.getBoundsInParent().getMaxX();
                double obstacleMinY = obstacle.getBoundsInParent().getMinY();
                System.out.println("gcPosMaxX: " + gcMaxX);
                System.out.println("gcPosMaxY: " + gcMaxY);
                System.out.println("gcPosMinX: " + gcMinX);
                System.out.println("gcPosMinY: " + gcMinY);
                System.out.println("obstacleMinX: " + obstacleMinX);
                System.out.println("obstacleMaxY: " + obstacleMaxY);
                System.out.println("obstacleMaxX: " + obstacleMaxX);
                System.out.println("obstacleMinY: " + obstacleMinY);
                if (gcMaxX > obstacleMinX + 3) return;
                if (gcMaxY < -3) return;
                if (obstacleMaxY < -3) return;
                if (obstacleMinX - gcMaxX <= 10) {
                    obstacle.die();
                    iter.remove();
                    break;
                }
            }
        }
    }
}
