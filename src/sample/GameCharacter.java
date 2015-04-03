package sample;

import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.util.Random;

/**
 * Created by lirong on 10/1/14.
 */
public class GameCharacter extends GameObject {
    private boolean jumping;
    private double speedWhenLastAccelerate;
    private long lastJumpStartTime;
    protected static double dampingRatio = 0.2 / Main.fps;  // per second
    public static double radius = 20;

    private double leftBend;
    private double rightBend;

    private boolean dead = false;
    private boolean reviving = false;

    private Group body;
    private ImageView head;
    private Group leftHip;
    private ImageView leftThigh;
    private Group leftKnee;
    private ImageView leftShin;
    private ImageView leftFoot;
    private Group rightHip;
    private ImageView rightThigh;
    private Group rightKnee;
    private ImageView rightShin;
    private ImageView rightFoot;

    private float[][] leftStates = {{-30, 10, 20}, {0,  0, 0}, { 30,  10, -40}};

    private float[][] rightStates = {{-30, 10, 20}, {0,  0, 0}, { 30,  10, -40}};
    
    private double lastXVelocityDecrease;
    final private double initialXVelocityDampingRatio = 0.002;
    final private double XVelocityDampingRatioMultiplier = 1.05;

    final private double BEND = 0.6;
    final private double NOT_BEND = 0.4;

    private long readyToJumpTime;

    private int prevAction;
    private int state;

    private boolean walking = false;

    public GameCharacter() {
        velocityX = 0.0;
        velocityY = 0.0;
        jumping = false;

        Image headImage = new Image("sample/images/Body.png");
        Image upperLegImage = new Image("sample/images/UpperLeg.png");
        Image lowerLegImage = new Image("sample/images/LowerLeg.png");
        Image footImage = new Image("sample/images/Foot.png");

        body = new Group();

        head = new ImageView(headImage);
        head.setTranslateY(-74);

        leftHip = new Group();
        leftThigh = new ImageView(upperLegImage);
        leftKnee = new Group();
        leftShin = new ImageView(lowerLegImage);
        leftFoot = new ImageView(footImage);
        leftHip.setTranslateX(9);
        leftHip.setTranslateY(-29);
        leftThigh.setTranslateX(-8);
        leftThigh.setTranslateY(-8);
        leftKnee.setTranslateX(0);
        leftKnee.setTranslateY(16);
        leftShin.setTranslateX(-4);
        leftShin.setTranslateY(-4);
        leftFoot.setTranslateX(-3);
        leftFoot.setTranslateY(9);
        leftHip.getChildren().add(leftThigh);
        leftHip.getChildren().add(leftKnee);
        leftKnee.getChildren().add(leftShin);
        leftKnee.getChildren().add(leftFoot);

        rightHip = new Group();
        rightThigh = new ImageView(upperLegImage);
        rightKnee = new Group();
        rightShin = new ImageView(lowerLegImage);
        rightFoot = new ImageView(footImage);
        rightHip.setTranslateX(9);
        rightHip.setTranslateY(-29);
        rightThigh.setTranslateX(-8);
        rightThigh.setTranslateY(-8);
        rightKnee.setTranslateX(0);
        rightKnee.setTranslateY(16);
        rightShin.setTranslateX(-4);
        rightShin.setTranslateY(-4);
        rightFoot.setTranslateX(-3);
        rightFoot.setTranslateY(9);
        rightHip.getChildren().add(rightThigh);
        rightHip.getChildren().add(rightKnee);
        rightKnee.getChildren().add(rightShin);
        rightKnee.getChildren().add(rightFoot);

        body.getChildren().add(leftHip);
        body.getChildren().add(head);
        body.getChildren().add(rightHip);

        this.getChildren().add(body);
    }

    public void die() {

        EventHandler<ActionEvent> eventOnFinished  = new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                dead = false;
                reviving = true;
            }
        };

        TranslateTransition translate = new TranslateTransition(Duration.millis(3000));
        RotateTransition rotate = new RotateTransition(Duration.millis(500));
        translate.setToY(-500);
        rotate.setByAngle(-90);
        translate.setAutoReverse(false);
        rotate.setAutoReverse(false);

        ParallelTransition transition = new ParallelTransition(this,
                translate, rotate);
        transition.play();

        transition.setOnFinished(eventOnFinished);
        dead = true;
    }

    public void accelerate(double aX, double aY) {
        velocityX += aX;
        velocityY += aY;
        speedWhenLastAccelerate = velocityX;
        lastXVelocityDecrease = -aX;
    }

    public void jump(double power) {
        if (jumping) return;
        velocityY -= power / Main.fps;
        jumping = true;
        System.out.println("jump");
    }

    public void damping() {
        if (!jumping) {
            velocityX -= getSpeedDecreaseRatio() * speedWhenLastAccelerate;
        }
        velocityX -= dampingRatio * velocityX;
        velocityX = (velocityX < 0.01) ? 0.0 : velocityX;
        velocityY = velocityY + GameEngine.gravity;
    }

    @Override
    public void move() {
        if (dead) return;

        if(walking) {
            accelerate(300 / Main.fps, 0);
            walking = false;
        }

        // Graphics detail is only for glove mode
        leftHip.getTransforms().clear();
        leftKnee.getTransforms().clear();
        leftFoot.getTransforms().clear();
        rightHip.getTransforms().clear();
        rightKnee.getTransforms().clear();
        rightFoot.getTransforms().clear();

        if(leftBend <= 0.5) {
            double newLeftBend = leftBend * 2;
            leftHip.getTransforms().add(new Rotate(newLeftBend * (leftStates[1][0] - leftStates[0][0]) + leftStates[0][0]));
            leftKnee.getTransforms().add(new Rotate(newLeftBend * (leftStates[1][1] - leftStates[0][1]) + leftStates[0][1]));
            leftFoot.getTransforms().add(new Rotate(newLeftBend * (leftStates[1][2] - leftStates[0][2]) + leftStates[0][2], 5, 3));
        } else {
            double newLeftBend = leftBend * 2 - 1;
            leftHip.getTransforms().add(new Rotate(newLeftBend * (leftStates[2][0] - leftStates[1][0]) + leftStates[1][0]));
            leftKnee.getTransforms().add(new Rotate(newLeftBend * (leftStates[2][1] - leftStates[1][1]) + leftStates[1][1]));
            leftFoot.getTransforms().add(new Rotate(newLeftBend * (leftStates[2][2] - leftStates[1][2]) + leftStates[1][2], 5, 3));
        }

        if(rightBend <= 0.5) {
            double newRightBend = rightBend * 2;
            rightHip.getTransforms().add(new Rotate(newRightBend * (rightStates[1][0] - rightStates[0][0]) + rightStates[0][0]));
            rightKnee.getTransforms().add(new Rotate(newRightBend * (rightStates[1][1] - rightStates[0][1]) + rightStates[0][1]));
            rightFoot.getTransforms().add(new Rotate(newRightBend * (rightStates[1][2] - rightStates[0][2]) + rightStates[0][2], 5, 3));
        } else {
            double newRightBend = rightBend * 2 - 1;
            rightHip.getTransforms().add(new Rotate(newRightBend * (rightStates[2][0] - rightStates[1][0]) + rightStates[1][0]));
            rightKnee.getTransforms().add(new Rotate(newRightBend * (rightStates[2][1] - rightStates[1][1]) + rightStates[1][1]));
            rightFoot.getTransforms().add(new Rotate(newRightBend * (rightStates[2][2] - rightStates[1][2]) + rightStates[1][2], 5, 3));
        }


        double vertOffset = body.getBoundsInLocal().getMaxY();

        body.setTranslateY(-vertOffset);

        this.setTranslateX(this.getTranslateX() + velocityX);
        this.setTranslateY(this.getTranslateY() + velocityY);
        updateSpeed();
    }

    public void updateSpeed() {
        damping();
    }

    public boolean onTheGround() {
        return !jumping;
    }

    @Override
    public void land(double y) {
        jumping = false;
        velocityY = 0;
        this.setTranslateY(y);
    }

    public double getSpeedDecreaseRatio() {
        if (lastXVelocityDecrease < 0) {
            lastXVelocityDecrease = speedWhenLastAccelerate * initialXVelocityDampingRatio;
        } else {
            lastXVelocityDecrease *= XVelocityDampingRatioMultiplier;
        }
        return lastXVelocityDecrease;
    }

    private long now() {
        return System.nanoTime();
    }

    /* Only for glove mode.
     * gesture:
     *      0: Both the left leg and right leg are straight
     *      1: Left leg is straight while right leg is bent
     *      2: Right leg is straight while left leg is bent
     *      3: Both the left leg and right leg are bent
     */
    public void setBend(double leftBend, double rightBend) {
        this.leftBend = leftBend;
        this.rightBend = rightBend;
        int gesture = -1;
        if (leftBend < NOT_BEND) {
            if (rightBend < NOT_BEND) {
                gesture = 0;    //  not considered right now
            } else if (rightBend > BEND) {
                gesture = 1;
            }
        } else if (leftBend > BEND) {
            if (rightBend < NOT_BEND) {
                gesture = 2;
            } else if (rightBend > BEND) {
                gesture = 3;
            }
        }
        //System.out.println("leftBend: " + leftBend + "\t" + "rightBend: " + rightBend + "\t" + "gesture: " + gesture);
        DFA(gesture);
    }

    public void walk() {
        walking = true;
    }

    public void kick() {
        if (dead) return;
        System.out.println("kick in GameCharacter.java");
    }

    @Override
    public void horizontalCollision(GameObject collidingObj) {
        if ((collidingObj instanceof CircleObstacle) || (collidingObj instanceof ThornObstacle)) {
            die();
            return;
        }
        velocityX = 0; //GameEngine.getSceneSpeed();
    }

    public void collisionDownward(double y, GameObject collidingObj) {
        if (collidingObj instanceof ThornObstacle) {
            die();
            return;
        }
        land(y);
    }

    /* Only for glove mode.
     * It is a Deterministic Finite Automata model used to interpret the game character's action
     */
    private void DFA(int gesture) {

        int prevGesture = state;

        if (gesture < 0) return;
        if (prevGesture == gesture) return;
        if (gesture == 0) {
            readyToJumpTime = now();
        }

        switch (state) {
            case 0:
                // Initial state
                if (gesture == 0) {
                    state = 11;
                } else if (gesture == 1) {
                    state = 1;
                } else if (gesture == 2) {
                    state = 3;
                }
                break;
            case 1:
                // Lift up Leg A
                if (gesture == 0) {
                    state = 11;
                } else {
                    if (gesture == 2) {
                        state = 9;
                    } else if (gesture == 3) {
                        state = 12;
                    }
                    walk();
                }
                break;
            case 2:
                /*// Put down Leg A. Just finished one step.
                if (gesture == 0) {
                    state = 11;
                } else if (gesture == 1) {
                    state = 5;
                    kick();
                } else if (gesture == 2) {
                    state = 3;
                }
                break;*/
            case 3:
                // Lift up Leg B
                if (gesture == 0) {
                    state = 11;
                } else {
                    if (gesture == 1) {
                        state = 10;
                    } else if (gesture == 3) {
                        state = 12;
                    }
                    walk();
                }
                break;
            case 4:
                /*// Put down Leg B. Just finished one step.
                if (gesture == 0) {
                    state = 11;
                } else if (gesture == 1) {
                    state = 1;
                } else if (gesture == 2) {
                    state = 7;
                    kick();
                }
                break;*/
            case 5:
                // Lift up Leg A. Just finished kicking.
                if (gesture == 0) {
                    state = 11;
                } else if (gesture == 2) {
                    state = 9;
                    walk();
                } else if (gesture == 3) {
                    state = 6;
                }
                break;
            case 6:
                // Put down Leg A.
                if (gesture == 0) {
                    state = 11;
                } else if (gesture == 1) {
                    state = 5;
                    kick();
                } else if (gesture == 2) {
                    state = 3;
                }
                break;
            case 7:
                // Lift up Leg B. Just finished kicking.
                if (gesture == 0) {
                    state = 11;
                } else if (gesture == 1) {
                    state = 10;
                    walk();
                } else if (gesture == 3) {
                    state = 8;
                }
                break;
            case 8:
                // Put down Leg B.
                if (gesture == 0) {
                    state = 11;
                } else if (gesture == 1) {
                    state = 1;
                } else if (gesture == 2) {
                    state = 7;
                    kick();
                }
                break;
            case 9:
                // Put down Leg A while lifting Leg B. Just finished one step.
                if (gesture == 0) {
                    state = 11;
                } else {
                    if (gesture == 1) {
                        state = 10;

                        rightStates[1][0] = -50;
                        rightStates[1][1] = 100;
                        rightStates[1][2] = -50;

                        leftStates[1][0] = 0;
                        leftStates[1][1] = 0;
                        leftStates[1][2] = 0;
                    } else if (gesture == 3) {
                        state = 12;
                    }
                    walk();
                }
                break;
            case 10:
                // Put down Leg B while lifting Leg A. Just finished one step.
                if (gesture == 0) {
                    state = 11;
                } else {
                    if (gesture == 2) {
                        state = 9;

                        leftStates[1][0] = -50;
                        leftStates[1][1] = 100;
                        leftStates[1][2] = -50;

                        rightStates[1][0] = 0;
                        rightStates[1][1] = 0;
                        rightStates[1][2] = 0;
                    } else if (gesture == 3) {
                        state = 12;
                    }
                    walk();
                }
                break;
            case 11:
                // Ready to jump.
                if (gesture == 1) {
                    state = 1;
                } else if (gesture == 2) {
                    state = 3;
                } else if (gesture == 3) {
                    //jump(Math.min(GameEngine.maxJumpingPower, GameEngine.maxJumpingPower * 200 / (double)((now() - readyToJumpTime) / 1e6)));
                    state = 12;
                }
                break;
            case 12:
                // Jump
                if (gesture == 0) {
                    state = 11;
                    jump(Math.min(GameEngine.maxJumpingPower, GameEngine.maxJumpingPower * 200 / (double)((now() - readyToJumpTime) / 1e6)));
                } else if (gesture == 1) {
                    state = 1;
                } else if (gesture == 2) {
                    state = 3;
                }
                break;
        }
        prevGesture = gesture;

        if(prevGesture != state) {
            //System.out.println("State: " + state);
        }
    }

    public boolean isDead() {
        return dead;
    }

    public boolean isReviving() {
        return reviving;
    }

    public void setAlive() {
        dead = false;
        reviving = false;
    }

    @Override
    public GameCharacter getDeepCopy() {
        return new GameCharacter();
    }
}