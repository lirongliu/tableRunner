package sample;

import javafx.geometry.Bounds;
import javafx.scene.Group;
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

    private double clearThreshold = 100;    // distance. Used to clear the objects out of the scene

    final private static int newObstaclePositionOffsetToScene = 20;     //  distance to the right of the scene


    public SceneController(Group root) {
        this.root = root;
        obstacleQueue = new LinkedList<Obstacle>();
        groundQueue = new LinkedList<Ground>();
    }

    void generateObstacle() {
        String obstacleType = obstacleTypes[rand.nextInt(numberOfObstacleTypes)];
        if (obstacleType.equals("Rectangle")) {
            drawRectangleObstacle(Main.SCENE_WIDTH + newObstaclePositionOffsetToScene,
                    0,
                    RectangleObstacle.minObstacleWidth +
                            rand.nextInt(
                                    RectangleObstacle.maxObstacleWidth -
                                    RectangleObstacle.minObstacleWidth + 1
                            ),
                    RectangleObstacle.minObstacleHeight +
                            rand.nextInt(
                                    RectangleObstacle.maxObstacleHeight -
                                    RectangleObstacle.minObstacleHeight + 1
                            ),
                    Color.BLACK
            );
        } else if (obstacleType.equals("Thorn")) {
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
        }
    }

    GameCharacter generateCharacter() {
        int originalX = Main.SCENE_WIDTH / 3;

        //Circle character = drawGameCharacter(originalX, 0, 20, Color.GREEN);
        //Circle character = new Circle(0, 0, 20);
        //character.setFill(Color.GREEN);

        GameCharacter gameCharacter = new GameCharacter(null);
        root.getChildren().add(gameCharacter);

        gameCharacter.setTranslateX(originalX);
        gameCharacter.setTranslateY(Main.SCENE_HEIGHT - Main.GROUND_HEIGHT - 20);

        return gameCharacter;
    }

    void generateInitialScene() {
        Random random = new Random();
        drawGround(Color.BLACK);
    }

    /*Circle drawGameCharacter(double x, double y, double r, Color color) {
        Circle circle = new Circle(x, Main.SCENE_HEIGHT - Main.GROUND_HEIGHT - y - r, r);
        circle.setFill(color);
        return circle;
    }*/

    Shape drawCircleObstacle(double x, double y, double r, Color color) {
        Circle circle = new Circle(x, Main.SCENE_HEIGHT - Main.GROUND_HEIGHT - y - r, r);
        circle.setFill(color);
        root.getChildren().add(circle);
        obstacleQueue.add(new CircleObstacle(circle));
        return circle;
    }

    Line drawGround(Color color) {
        Line line = new Line(0, Main.SCENE_HEIGHT - Main.GROUND_HEIGHT,
                30000, Main.SCENE_HEIGHT - Main.GROUND_HEIGHT);
        line.setFill(color);
        root.getChildren().add(line);
        groundQueue.add(new Ground(line));
        return line;
    }

    Rectangle drawRectangleObstacle(double x, double y, double w, double h, Color color) {
        Rectangle rec = new Rectangle(x, Main.SCENE_HEIGHT - Main.GROUND_HEIGHT - h - y, w, h);
        rec.setFill(color);
        root.getChildren().add(rec);
        obstacleQueue.add(new RectangleObstacle(rec));
        return rec;
    }

    void drawThornObstacle(double x, double y, double w, double h, int count, Color color) {
        //System.out.println("count" + count);
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
            //System.out.println("obstacleQueue size" + obstacleQueue.size());
        }
    }


    void clearOutdatedObstacles() {
        Iterator<Obstacle> iter = obstacleQueue.iterator();
        while (iter.hasNext()) {
            Shape shape = iter.next().getShapeObject();
            Bounds bounds = shape.getLayoutBounds();
            double translateX = shape.getTranslateX();
            double translateY = shape.getTranslateY();
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

}
