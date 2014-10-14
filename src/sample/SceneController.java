package sample;

import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

/**
 * Created by lirong on 10/3/14.
 */

// TODO: Add more static scene on the sky
// TODO: Add gap obstacles (could be tricky because there should be no obstacle between the gap)
public class SceneController {
    Group root;
    final private String[] obstacleTypes = {"Rectangle", "Thorn", "Circle"};
    private Queue<Obstacle> obstacleQueue;
    private Queue<Ground> groundQueue;
    private int numberOfObstacleTypes = obstacleTypes.length;
    private Random rand = new Random();

    private Ground lastGroundObject;

    private double clearThreshold = 100;    // distance. Used to clear the objects out of the scene

    final private static int newObstaclePositionOffsetToScene = 20;     //  distance to the right of the scene


    private Group cloudGroup;
    private Group sceneGroup;

    public SceneController(Group root) {
        this.root = root;
        obstacleQueue = new LinkedList<Obstacle>();
        groundQueue = new LinkedList<Ground>();


        cloudGroup = new Group();

        Image cloudImage = new Image("sample/images/CloudBG.png");
        cloudGroup.getChildren().add(new ImageView(cloudImage));

        root.getChildren().add(cloudGroup);
        cloudGroup.setTranslateY(Main.SCENE_HEIGHT - Main.GROUND_HEIGHT - 240);


        sceneGroup = new Group();
        root.getChildren().add(sceneGroup);
        sceneGroup.setTranslateY(Main.SCENE_HEIGHT - Main.GROUND_HEIGHT);
    }

    void generateObstacle() {
        //String obstacleType = obstacleTypes[rand.nextInt(numberOfObstacleTypes)];
        //if (obstacleType.equals("Rectangle")) {
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

            rec.setTranslateX(Math.abs(sceneGroup.getTranslateX()) + Main.SCENE_WIDTH + newObstaclePositionOffsetToScene);
            sceneGroup.getChildren().add(rec);
            obstacleQueue.add(rec);
        /*} else if (obstacleType.equals("Thorn")) {
            drawThornObstacle(Main.SCENE_WIDTH + newObstaclePositionOffsetToScene,
                    0,
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
                    rand.nextInt(ThornObstacle.maxThornCountInARow) + 1,
                    Color.BLACK
            );
        } else if (obstacleType.equals("Circle")) {
            drawCircleObstacle(Main.SCENE_WIDTH + newObstaclePositionOffsetToScene, 0, 20, Color.BLACK);
        }*/
    }

    GameCharacter generateCharacter() {
        int originalX = Main.SCENE_WIDTH / 3;

        GameCharacter gameCharacter = new GameCharacter();
        root.getChildren().add(gameCharacter);

        gameCharacter.setTranslateX(originalX);
        gameCharacter.setTranslateY(Main.SCENE_HEIGHT - Main.GROUND_HEIGHT - 20);

        return gameCharacter;
    }

    void generateInitialScene() {
        Random random = new Random();
        drawGround(0, 1500, Color.BLACK); //rand.nextInt(1000) + Ground.minGroundLength
    }

    Shape drawCircleObstacle(double x, double y, double r, Color color) {
        Circle circle = new Circle(x, Main.SCENE_HEIGHT - Main.GROUND_HEIGHT - y - r, r);
        circle.setFill(color);
        root.getChildren().add(circle);
        //obstacleQueue.add(new CircleObstacle(circle));
        return circle;
    }

    Ground drawGround(double posX, double length, Color color) {
        Ground newGround = new Ground(posX, length, sceneGroup.getTranslateX());
        sceneGroup.getChildren().add(newGround);
        groundQueue.add(newGround);
        lastGroundObject = newGround;
        return newGround;
    }

    /*Rectangle drawRectangleObstacle(double x, double y, double w, double h, Color color) {
        root.getChildren().add(rec);
        obstacleQueue.add(new RectangleObstacle(rec));
        return rec;
    }*/

    void drawThornObstacle(double x, double y, double w, double h, int count, Color color) {
        for (int i = 0;i < count;i++) {
            Polygon triangle = new Polygon();
            triangle.getPoints().addAll(new Double[]{
                            x + i * w / count, Main.SCENE_HEIGHT - Main.GROUND_HEIGHT - y,
                            x + (i + 1) * w / count, Main.SCENE_HEIGHT - Main.GROUND_HEIGHT - y,
                            x + (2 * i + 1) * w / count / 2, Main.SCENE_HEIGHT - Main.GROUND_HEIGHT - y - h }
            );
            triangle.setFill(color);
            root.getChildren().add(triangle);
            obstacleQueue.add(new ThornObstacle(triangle));
        }
    }


    void clearOutdatedObstacles() {
        Iterator<Obstacle> iter = obstacleQueue.iterator();
        while (iter.hasNext()) {
            Group obstacle = iter.next();
            Bounds bounds = obstacle.getBoundsInParent();
            double translateX = sceneGroup.getTranslateX();
            double translateY = sceneGroup.getTranslateY();

            if (bounds.getMaxX() + translateX < -clearThreshold) iter.remove();
            else if (bounds.getMaxY() + translateY < -clearThreshold) iter.remove();
            else if (bounds.getMinX() + translateX > Main.SCENE_WIDTH + clearThreshold) iter.remove();
            else if (bounds.getMinY() + translateY > Main.SCENE_HEIGHT + clearThreshold) iter.remove();
        }
    }

    public Queue<Obstacle> getObstacleQueue() {
        return this.obstacleQueue;
    }

    public Queue<Ground> getGroundQueue() {
        return this.groundQueue;
    }

    public Group getCloudGroup() {
        return this.cloudGroup;
    }

    public Group getSceneGroup() {
        return this.sceneGroup;
    }

    public Ground getLastGroundObject() { return lastGroundObject; }
}