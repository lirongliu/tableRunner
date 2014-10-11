package sample;

import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

/**
 * Created by lirong on 10/3/14.
 */
public class Ground extends Obstacle {
    public Ground(int width) {
        Line line = new Line(0, 0,
                width, 0);
        line.setFill(Color.BLACK);

        getChildren().add(line);
    }
}