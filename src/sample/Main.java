package sample;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.*;
import javafx.stage.Stage;

import java.io.PrintStream;
import java.util.Queue;
import java.util.Random;

public class Main extends Application {
    private static PrintStream p = System.out;

    final public static int SCENE_WIDTH = 1200;
    final public static int SCENE_HEIGHT = 800;
    final public static double GROUND_HEIGHT = 100;
    final public static int fps = 60;   //  max fps

    private Group root;

    private GameCharacter gameCharacter;
    private SceneController sceneController;
    private GameEngine gameEngine;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        root = new Group();
        Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT, Color.WHITE);
        stage.setTitle("JavaFX Scene Graph Demo");

        sceneController = new SceneController(root);
        gameCharacter = sceneController.generateCharacter();
        sceneController.generateInitialScene();
        gameEngine = new GameEngine(scene, sceneController, gameCharacter);

        gameEngine.setup();
        gameEngine.run();
        stage.setScene(scene);
        stage.show();

    }
}