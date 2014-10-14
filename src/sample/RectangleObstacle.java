package sample;

import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import javax.xml.crypto.dsig.keyinfo.KeyValue;

/**
 * Created by lirong on 10/3/14.
 */
public class RectangleObstacle extends Obstacle {
    public static int minObstacleWidth = 30;
    public static int maxObstacleWidth = 100;
    public static int minObstacleHeight = 20;
    public static int maxObstacleHeight = 250;
    private int w, h;
    private Rectangle rec;


    public RectangleObstacle(int width, int height) {
        super();
        Rectangle rec = new Rectangle(0, 0, width, height);
        w = width;
        h = height;
        rec.setFill(Color.BLACK);

        rec.setTranslateY(-height);
        getChildren().add(rec);
        this.rec = rec;
    }

    public void die() {
        System.out.println("Rec is broken");
        double minX = rec.getBoundsInParent().getMinX();
        double minY = rec.getBoundsInParent().getMinY();
        final Rectangle subRec[] = new Rectangle[4];
        subRec[0] = new Rectangle(minX, minY, w / 2, h / 2);                    //  top left
        subRec[1] = new Rectangle(minX + w / 2, minY, w / 2, h / 2);            //  top right
        subRec[2] = new Rectangle(minX, minY + h / 2, w / 2, h / 2);            //  bottom left
        subRec[3] = new Rectangle(minX + w / 2, minY + h / 2, w / 2, h / 2);    //  bottom right
        getChildren().addAll(subRec);
        getChildren().remove(rec);

        TranslateTransition translate[] = new TranslateTransition[4];
        EventHandler<ActionEvent> eventOnFinished  = new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                getChildren().removeAll(subRec);        //  NOTE: could be problematic
            }
        };

        for (int i = 0;i < 4;i++) {
            translate[i] = new TranslateTransition(Duration.millis(1500));
            translate[i].setToX(-2000 + (4000 * (i & 1)));
            translate[i].setToY(-2000 + (4000 * (i / 2)));
            translate[i].play();

            ParallelTransition transition = new ParallelTransition(subRec[i],
                    translate[i]);
            transition.play();

            transition.setOnFinished(eventOnFinished);
        }
    }

    @Override
    public RectangleObstacle getDeepCopy() {
        System.out.println("Rectangle deep copy");
        return new RectangleObstacle(w, h);
    }
}
