package sample;

import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

/**
 * Created by lirong on 10/3/14.
 */

public class SceneController {
    Group root;
    final private String[] obstacleTypes = {"Rectangle", "Thorn", "Circle"};
    private Queue<Obstacle> obstacleQueue[];
//    private Queue<Ground> groundQueue[];
    private int numberOfObstacleTypes = obstacleTypes.length;
    private Random rand = new Random();

    private Ground lastGroundObject[];

    private double clearThreshold = 100;    // distance. Used to clear the objects out of the scene

    final public static int newObstaclePositionOffsetToScene = 20;     //  distance to the right of the scene


    private Group[] cloudGroup;
    private Group[] sceneGroup;

    private Rectangle[] clippingRects;

    private Text[] distLabel;

    private int numOfPlayers;

    public SceneController(Group root, int numOfPlayers) {
        this.numOfPlayers = numOfPlayers;
        lastGroundObject = new Ground[2];
        this.root = root;
        obstacleQueue = new LinkedList[numOfPlayers];
//        groundQueue = new LinkedList[numOfPlayers];
        cloudGroup = new Group[numOfPlayers];
        sceneGroup = new Group[numOfPlayers];
        clippingRects = new Rectangle[numOfPlayers];
        distLabel = new Text[numOfPlayers];

        Image flagImage = new Image("sample/images/Flag.png");
        Image cloudImage = new Image("sample/images/CloudBG.png");

        for (int i = 0;i < numOfPlayers;i++) {
            obstacleQueue[i] = new LinkedList<Obstacle>();
//            groundQueue[i] = new LinkedList<Ground>();
            cloudGroup[i] = new Group();

            cloudGroup[i].getChildren().add(new ImageView(cloudImage));

            root.getChildren().add(cloudGroup[i]);
            cloudGroup[i].setTranslateY(Main.SCENE_HEIGHT - 384 * (i + 1));

            clippingRects[i] = new Rectangle(0, Main.GROUND_HEIGHT - 384, Main.SCENE_WIDTH, 384);

            sceneGroup[i] = new Group();
            sceneGroup[i].setClip(clippingRects[i]);
            root.getChildren().add(sceneGroup[i]);
            sceneGroup[i].setTranslateY(Main.SCENE_HEIGHT - Main.GROUND_HEIGHT - (i * 384));

            ImageView flag = new ImageView(flagImage);
            sceneGroup[i].getChildren().add(flag);
            flag.setTranslateX(GameEngine.endDistance + 512);
            flag.setTranslateY(Main.GROUND_HEIGHT - 200);

            distLabel[i] = new Text(20, Main.SCENE_HEIGHT - 340 - (i * 384), "Player " + (i + 1) + ": 0 / " + GameEngine.endDistance);
            distLabel[i].setFont(new Font(18));
            root.getChildren().add(distLabel[i]);
        }
    }

    public Obstacle generateObstacle(int i) {
        System.out.println("generating an obstacle");
        String obstacleType = obstacleTypes[rand.nextInt(numberOfObstacleTypes)];
        if (obstacleType.equals("Rectangle")) {
            System.out.println("Rectangle");
            RectangleObstacle rec = new RectangleObstacle(
                                            RectangleObstacle.minObstacleWidth +
                                                rand.nextInt(
                                                    RectangleObstacle.maxObstacleWidth -
                                                    RectangleObstacle.minObstacleWidth + 1
                                                ),
                                            RectangleObstacle.minObstacleHeight +
                                                rand.nextInt(
                                                    RectangleObstacle.maxObstacleHeight -
                                                    RectangleObstacle.minObstacleHeight + 1
                                            ));

            rec.setTranslateX(Math.abs(sceneGroup[i].getTranslateX()) + Main.SCENE_WIDTH + newObstaclePositionOffsetToScene);
            sceneGroup[i].getChildren().add(rec);
            obstacleQueue[i].add(rec);
            return rec;
        } else if (obstacleType.equals("Thorn")) {
            System.out.println("Thorn");
            ThornObstacle thornObstacle = new ThornObstacle(
                    ThornObstacle.minObstacleWidth +
                            rand.nextInt(
                                    ThornObstacle.maxObstacleWidth -
                                            ThornObstacle.minObstacleWidth + 1
                            ),
                    ThornObstacle.minObstacleHeight +
                            rand.nextInt(
                                    ThornObstacle.maxObstacleHeight -
                                            ThornObstacle.minObstacleHeight + 1
                            ),
                    rand.nextInt(ThornObstacle.maxThornCountInARow) + ThornObstacle.minThornCountInARow
            );
            thornObstacle.setTranslateX(Math.abs(sceneGroup[i].getTranslateX()) + Main.SCENE_WIDTH + newObstaclePositionOffsetToScene);
            sceneGroup[i].getChildren().add(thornObstacle);
            obstacleQueue[i].add(thornObstacle);
            return thornObstacle;
        } else if (obstacleType.equals("Circle")) {
            System.out.println("Circle");
            CircleObstacle circleObstacle = new CircleObstacle();
            circleObstacle.setTranslateX(Math.abs(sceneGroup[i].getTranslateX()) + Main.SCENE_WIDTH + newObstaclePositionOffsetToScene);
            sceneGroup[i].getChildren().add(circleObstacle);
            obstacleQueue[i].add(circleObstacle);
            return circleObstacle;
        } else {
            return null;
        }
    }

    GameCharacter[] generateCharacter() {
        double originalX = Main.SCENE_WIDTH / 3;

        GameCharacter gameCharacter[] = new GameCharacter[numOfPlayers];
        for (int i = 0;i < numOfPlayers;i++) {
            gameCharacter[i] = new GameCharacter();
            sceneGroup[i].getChildren().add(gameCharacter[i]);

            gameCharacter[i].setTranslateX(originalX);
            //gameCharacter.setTranslateY(Main.SCENE_HEIGHT - Main.GROUND_HEIGHT - 20);
        }

        return gameCharacter;
    }

    void generateInitialScene(int i) {
        Random random = new Random();
        drawGround(i, 0, 1500); //rand.nextInt(1000) + Ground.minGroundLength
    }

//    Shape drawCircleObstacle(double x, double y, double r, Color color) {
//        Circle circle = new Circle(x, Main.SCENE_HEIGHT - Main.GROUND_HEIGHT - y - r, r);
//        circle.setFill(color);
//        root.getChildren().add(circle);
//        //obstacleQueue[0].add(new CircleObstacle(circle));
//        return circle;
//    }

    // posX: screen position
    Ground drawGround(int i, double posX, double length) {
        Ground newGround = new Ground(posX, length);
        sceneGroup[i].getChildren().add(newGround);
        obstacleQueue[i].add(newGround);
        lastGroundObject[i] = newGround;
        return newGround;
    }

//    Rectangle drawRectangleObstacle(double x, double y, double w, double h, Color color) {
//        root.getChildren().add(rec);
//        obstacleQueue[0].add(new RectangleObstacle(rec));
//        return rec;
//    }

//    void drawThornObstacle(double x, double y, double w, double h, int count, Color color) {
//        for (int i = 0;i < count;i++) {
//            Polygon triangle = new Polygon();
//            triangle.getPoints().addAll(new Double[]{
//                            x + i * w / count, Main.SCENE_HEIGHT - Main.GROUND_HEIGHT - y,
//                            x + (i + 1) * w / count, Main.SCENE_HEIGHT - Main.GROUND_HEIGHT - y,
//                            x + (2 * i + 1) * w / count / 2, Main.SCENE_HEIGHT - Main.GROUND_HEIGHT - y - h }
//            );
//            triangle.setFill(color);
//            root.getChildren().add(triangle);
//            obstacleQueue[0].add(new ThornObstacle(triangle));
//        }
//    }


    void clearOutdatedObstacles(int i) {
        Iterator<Obstacle> iter = obstacleQueue[i].iterator();
        while (iter.hasNext()) {
            Group obstacle = iter.next();
            Bounds bounds = obstacle.getBoundsInParent();
            double minX = bounds.getMinX() + sceneGroup[i].getTranslateX();
            double maxX = bounds.getMaxX() + sceneGroup[i].getTranslateX();
            double minY = bounds.getMinY(); // + sceneGroup[i].getTranslateY();
            double maxY = bounds.getMaxY(); // + sceneGroup[i].getTranslateY();
            //if (obstacle instanceof Ground) System.out.println("minY: " + minY + ", " + "maxY: " + maxY);
//            Bounds bounds = obstacle.getLayoutBounds();
//            double translateX = obstacle.getTranslateX();
//            double translateY = obstacle.getTranslateY();
            if (maxX < -clearThreshold) {
                sceneGroup[i].getChildren().remove(obstacle);
                iter.remove();
            } else if (maxY < -clearThreshold) {
                sceneGroup[i].getChildren().remove(obstacle);
                iter.remove();
            } else if (minX > Main.SCENE_WIDTH + clearThreshold) {
                sceneGroup[i].getChildren().remove(obstacle);
                iter.remove();
            }else if (minY > Main.GROUND_HEIGHT) {
                sceneGroup[i].getChildren().remove(obstacle);
                iter.remove();
            }
        }
    }

    public Queue<Obstacle> getObstacleQueue(int i) {
        return this.obstacleQueue[i];
    }

//    public Queue<Ground> getGroundQueue(int i) {
//        return this.groundQueue[i];
//    }

    public Group getCloudGroup(int i) {
        return this.cloudGroup[i];
    }

    public Group getSceneGroup(int i) {
        return this.sceneGroup[i];
    }

    public Rectangle getClippingRect(int i) {
        return this.clippingRects[i];
    }

    public Text getDistLabel(int i) {
        return this.distLabel[i];
    }

    public Ground getLastGroundObject(int i) { return lastGroundObject[i]; }

    public void setLastGroundObject(int i, Ground lastGroundObject) { this.lastGroundObject[i] = lastGroundObject; }
}