package sample;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.PrintStream;
import java.util.Queue;
import java.util.Random;


// TODO: (FIX ME) When falling from an object, gc can still jump.
// TODO: Refactor kick()
// TODO: (Debug) kicking sometimes doesn't work
public class Main extends Application {
    private static PrintStream p = System.out;

    final public static int SCENE_WIDTH = 1024;
    final public static int SCENE_HEIGHT = 768;
    final public static double GROUND_HEIGHT = 100;
    final public static int fps = 60;   //  max fps

    private Group root;

    private GameCharacter gameCharacter;
    private SceneController sceneController;
    private GameEngine gameEngine;

    private SerialController controller;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        root = new Group();
        Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT, Color.WHITE);
        stage.setTitle("JavaFX Scene Graph Demo");

        //TODO: use full screen, and make sure that everything is the right scale for the given monitor
        //stage.setFullScreen(true);

        controller = new SerialController();
        controller.initialize();

        stage.setOnHiding(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent event) {
                controller.close();
            }
        });

        sceneController = new SceneController(root);
        gameCharacter = sceneController.generateCharacter();
        sceneController.generateInitialScene();
        gameEngine = new GameEngine(scene, sceneController, gameCharacter);

        controller.setCharacters(gameCharacter);

        gameEngine.setup();
        gameEngine.run();
        stage.setScene(scene);
        stage.show();
    }
}