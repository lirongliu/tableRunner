package sample;

import javafx.animation.AnimationTimer;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Pair;

/**
 * Created by matthew
 */
public class MenuScene extends Scene {

    private long lastSceneUpdateTime;

    private Group root;
    private Main parent;

    private Text player1Label;
    private Text player2Label;
    private Text countdown;

    private GameCharacter[] characters = {null, null};

    private long countdownStart;

    private AnimationTimer timer;

    private SerialController controller;

    public MenuScene(Group root, Main parent, SerialController controller) {
        super(root, Main.SCENE_WIDTH, Main.SCENE_HEIGHT, Color.WHITE);

        this.root = root;
        this.parent = parent;
        this.controller = controller;

        Text text = new Text(240, 200, "Put on glove and then press button to begin");
        text.setFont(new Font(25));
        //text.setTextAlignment(TextAlignment.CENTER);
        root.getChildren().add(text);

        player1Label = new Text(180, 600, "Waiting for Player 1");
        player1Label.setFont(new Font(18));
        player1Label.setWrappingWidth(200);
        root.getChildren().add(player1Label);

        player2Label = new Text(640, 600, "Waiting for Player 2");
        player2Label.setFont(new Font(18));
        player2Label.setWrappingWidth(200);
        root.getChildren().add(player2Label);

        run();
    }

    public void run() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (now - lastSceneUpdateTime < 1e9 / Main.fps) return;

                if((controller.instruction > 0) && (controller.instruction != 10)) {
                    if(characters[1] == null && controller.instruction >= 2) {
                        characters[1] = new GameCharacter();
                        root.getChildren().add(characters[1]);
                        characters[1].setTranslateX(200);
                        characters[1].setTranslateY(500);
                        player1Label.setText("Player 1 Found");

                        controller.setCharacters(characters);
                    }

                    if(characters[0] == null && controller.instruction % 2 == 1) {
                        characters[0] = new GameCharacter();
                        root.getChildren().add(characters[0]);
                        characters[0].setTranslateX(400);
                        characters[0].setTranslateY(500);
                        player2Label.setText("Player 2 Found");

                        controller.setCharacters(characters);
                    }

                    if(countdown == null) {
                        countdown = new Text(300, 700, "Next game starts in 30 seconds...");
                        countdown.setFont(new Font(25));
                        root.getChildren().add(countdown);

                        countdownStart = now;

                        System.out.println("test");
                    }
                }

                if(countdown != null) {
                    long secondsRemain = 10 - (now - countdownStart) / 1000000000;
                    countdown.setText("Next game starts in " + secondsRemain + " " + (secondsRemain == 1 ? "second" : "seconds") + "...");

                    if(secondsRemain <= 0) {
                        timer.stop();
                        parent.gameStart(2);
                    }
                }

                lastSceneUpdateTime = now;
            }
        };

        timer.start();
    }

    public void clean() {
        for(int i = 0; i < characters.length; ++i) {
            if(characters[i] != null) {
                root.getChildren().remove(characters[i]);
                characters[i] = null;
            }
        }

        root.getChildren().remove(countdown);
        countdown = null;

        timer.start();
    }
}