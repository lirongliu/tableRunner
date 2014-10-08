package sample;

import javafx.scene.shape.Shape;

/**
 * Created by lirong on 10/3/14.
 */
public class Ground extends Obstacle {
    public Ground(Shape obj) {
        super(obj, GameEngine.getSceneSpeed(), 0);
    }
}
