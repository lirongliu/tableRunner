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
    final public static double GROUND_HEIGHT = 70;
    final public static int fps = 60;   //  max fps

    private Group root;

    private GameCharacter gameCharacter[];
    private SceneController sceneController;
    private GameEngine gameEngine;

    private SerialController controller;

    private int numOfPlayers;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        root = new Group();
        Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT, Color.WHITE);
        stage.setTitle("JavaFX Scene Graph Demo");

        //stage.setFullScreen(true);

        controller = new SerialController();
        controller.initialize();

        numOfPlayers = 1;

        stage.setOnHiding(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent event) {
                controller.close();
            }
        });

        sceneController = new SceneController(root, numOfPlayers);
        gameCharacter = sceneController.generateCharacter();
        for (int i = 0;i < numOfPlayers;i++) {
            sceneController.generateInitialScene(i);
        }
        gameEngine = new GameEngine(scene, sceneController, gameCharacter, numOfPlayers);

        controller.setCharacters(gameCharacter);

        gameEngine.setup();
        gameEngine.run();
        stage.setScene(scene);
        stage.show();
    }
}