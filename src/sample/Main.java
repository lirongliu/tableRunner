package sample;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.PrintStream;


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
    private Group menuRoot;

    private MenuScene menuScene;

    private Stage stage;

    private GameCharacter gameCharacter[];
    private SceneController sceneController;
    private GameEngine gameEngine;

    private SerialController serialController;

    private int numOfPlayers;

    private String gameState = "menu";

    public static void main(String[] args) {
        launch(args);
    }

    private boolean testWithKeyboard = true;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
//        stage.setTitle("Physical Computing Midterm");
        stage.setTitle("Glove Runner");

        serialController = new SerialController();
        serialController.initialize(this);

        stage.setOnHiding(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent event) {
                serialController.close();
            }
        });

        if (testWithKeyboard) {
            /* for testing with keyboard
             * player 1: Down and Right arrows are for acceleration; Space is for jumping; K is for kicking
             * player 2: S, D are for acceleration; Q is for jumping; A is for kicking
             */
            GameCharacter[] gameCharacters = new GameCharacter[2];
            gameCharacters[0] = new GameCharacter();
            gameCharacters[1] = new GameCharacter();
            gameStart(gameCharacters);
        } else {
            /* Play with glove */
            menuStart();
        }

        stage.show();
    }

    public void gameStart(GameCharacter[] characters) {
        root = new Group();
        Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT, Color.WHITE);

        this.numOfPlayers = 0;

        for(int i = 0; i < characters.length; ++i) {
            if(characters[i] != null) {
                numOfPlayers++;
            }
        }

        sceneController = new SceneController(root, numOfPlayers);
        gameCharacter = sceneController.generateCharacter();
        for (int i = 0;i < numOfPlayers;i++) {
            sceneController.generateInitialScene(i);
        }
        
        gameEngine = new GameEngine(scene, sceneController, gameCharacter, numOfPlayers, serialController, this, root);

        if(numOfPlayers == 1) {
            for(int i = 0; i < characters.length; ++i) {
                if(characters[i] != null) {
                    characters[i] = gameCharacter[0];
                }
            }

            serialController.setCharacters(characters);
        } else {
            serialController.setCharacters(gameCharacter);
        }

        gameEngine.setup();
        gameEngine.run();
        stage.setScene(scene);
    }

    public void menuStart() {
        if(menuRoot == null) {
            menuRoot = new Group();
            menuScene = new MenuScene(menuRoot, this, serialController);
        } else {
            menuScene.clean();
        }

        stage.setScene(menuScene);
    }
}