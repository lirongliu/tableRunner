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

    //private float[][] leftStates = {{  0,   0, 0},
    //                                {-40,  80, -40},
      //                              {-80, 160, -80}};

    //private float[][] rightStates = {{  0,   0, 0},
        //                            {-40,  80, -40},
          //                          {-80, 160, -80}};


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
        //if (jumping) return;
        velocityX += aX;
        velocityY += aY;
        speedWhenLastAccelerate = velocityX;
        lastXVelocityDecrease = -aX;
    }

    public void jump(double power) {
        if (jumping) return;
        if (velocityY > 2) return;      //  Temporary solution TODO: Fix it
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

        //System.out.println(state);

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


        /*if((state == 9) || (state == 9) || (state == 10) || (state == 10)) {

            leftHip.getTransforms().add(new Rotate(120 * leftBend - 60));
            rightHip.getTransforms().add(new Rotate(120 * rightBend - 60));

            //rightLegLower.getTransforms().add(new Rotate(160 * (-1 * rightBend*(rightBend-2)), 3, 2.5));
            if((state == 2) || (state == 9)) {
                leftKnee.getTransforms().add(new Rotate(-160 * (Math.pow((leftBend - 1), 2) - 1)));
                rightKnee.getTransforms().add(new Rotate(160 * Math.pow(rightBend, 2)));
            } else {
                leftKnee.getTransforms().add(new Rotate(160 * Math.pow(leftBend, 2)));
                rightKnee.getTransforms().add(new Rotate(-160 * (Math.pow((rightBend - 1), 2) - 1)));
            }
        } else {
            leftHip.getTransforms().add(new Rotate(-80 * leftBend));
            rightHip.getTransforms().add(new Rotate(-80 * rightBend));
            leftKnee.getTransforms().add(new Rotate(160 * leftBend));
            rightKnee.getTransforms().add(new Rotate(160 * rightBend));
        }*/


        double vertOffset = body.getBoundsInLocal().getMaxY();

        body.setTranslateY(-vertOffset);


       // System.out.println("pos: " + getPositionX());
        /*if (getTranslateX() > 512 && velocityX > 0) {
            GameEngine.updateSceneSpeed(-velocityX + GameEngine.defaultSceneSpeed);
            //System.out.println("pass mid point");
        } else {
            this.setTranslateX(this.getTranslateX() + velocityX);
            GameEngine.updateSceneSpeed(GameEngine.defaultSceneSpeed);
        }*/
        this.setTranslateX(this.getTranslateX() + velocityX);
        this.setTranslateY(this.getTranslateY() + velocityY);

//        System.out.println(this.getTranslateY());
//        System.out.println(this.getBoundsInParent().getMaxY());

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

    public void setBend(double leftBend, double rightBend) {
        this.leftBend = leftBend;
        this.rightBend = rightBend;
        int action = -1;
        if (leftBend < NOT_BEND) {
            if (rightBend < NOT_BEND) {
                action = 0;    //  not considered right now
            } else if (rightBend > BEND) {
                action = 1;
            }
        } else if (leftBend > BEND) {
            if (rightBend < NOT_BEND) {
                action = 2;
            } else if (rightBend > BEND) {
                action = 3;
            }
        }
        //System.out.println("leftBend: " + leftBend + "\t" + "rightBend: " + rightBend + "\t" + "action: " + action);
        DFA(action);
    }

    public void walk() {
        accelerate(300 / Main.fps, 0);
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

    private void DFA(int action) {

        int prevState = state;

        if (action < 0) return;
        if (prevAction == action) return;
        if (action == 0) {
            readyToJumpTime = now();
        }

        switch (state) {
            case 0:
                // Initial state
                if (action == 0) {
                    state = 11;
                } else if (action == 1) {
                    state = 1;
                } else if (action == 2) {
                    state = 3;
                }
                break;
            case 1:
                // Lift up Leg A
                if (action == 0) {
                    state = 11;
                } else {
                    if (action == 2) {
                        state = 9;
                    } else if (action == 3) {
                        state = 12;
                    }
                    walk();
                }
                break;
            case 2:
                /*// Put down Leg A. Just finished one step.
                if (action == 0) {
                    state = 11;
                } else if (action == 1) {
                    state = 5;
                    kick();
                } else if (action == 2) {
                    state = 3;
                }
                break;*/
            case 3:
                // Lift up Leg B
                if (action == 0) {
                    state = 11;
                } else {
                    if (action == 1) {
                        state = 10;
                    } else if (action == 3) {
                        state = 12;
                    }
                    walk();
                }
                break;
            case 4:
                /*// Put down Leg B. Just finished one step.
                if (action == 0) {
                    state = 11;
                } else if (action == 1) {
                    state = 1;
                } else if (action == 2) {
                    state = 7;
                    kick();
                }
                break;*/
            case 5:
                // Lift up Leg A. Just finished kicking.
                if (action == 0) {
                    state = 11;
                } else if (action == 2) {
                    state = 9;
                    walk();
                } else if (action == 3) {
                    state = 6;
                }
                break;
            case 6:
                // Put down Leg A.
                if (action == 0) {
                    state = 11;
                } else if (action == 1) {
                    state = 5;
                    kick();
                } else if (action == 2) {
                    state = 3;
                }
                break;
            case 7:
                // Lift up Leg B. Just finished kicking.
                if (action == 0) {
                    state = 11;
                } else if (action == 1) {
                    state = 10;
                    walk();
                } else if (action == 3) {
                    state = 8;
                }
                break;
            case 8:
                // Put down Leg B.
                if (action == 0) {
                    state = 11;
                } else if (action == 1) {
                    state = 1;
                } else if (action == 2) {
                    state = 7;
                    kick();
                }
                break;
            case 9:
                // Put down Leg A while lifting Leg B. Just finished one step.
                if (action == 0) {
                    state = 11;
                } else {
                    if (action == 1) {
                        state = 10;

                        rightStates[1][0] = -50;
                        rightStates[1][1] = 100;
                        rightStates[1][2] = -50;

                        leftStates[1][0] = 0;
                        leftStates[1][1] = 0;
                        leftStates[1][2] = 0;
                    } else if (action == 3) {
                        state = 12;
                    }
                    walk();
                }
                break;
            case 10:
                // Put down Leg B while lifting Leg A. Just finished one step.
                if (action == 0) {
                    state = 11;
                } else {
                    if (action == 2) {
                        state = 9;

                        leftStates[1][0] = -50;
                        leftStates[1][1] = 100;
                        leftStates[1][2] = -50;

                        rightStates[1][0] = 0;
                        rightStates[1][1] = 0;
                        rightStates[1][2] = 0;
                    } else if (action == 3) {
                        state = 12;
                    }
                    walk();
                }
                break;
            case 11:
                // Ready to jump.
                if (action == 1) {
                    state = 1;
                } else if (action == 2) {
                    state = 3;
                } else if (action == 3) {
                    //jump(Math.min(GameEngine.maxJumpingPower, GameEngine.maxJumpingPower * 200 / (double)((now() - readyToJumpTime) / 1e6)));
                    state = 12;
                }
                break;
            case 12:
                // Jump
                if (action == 0) {
                    state = 11;
                    jump(Math.min(GameEngine.maxJumpingPower, GameEngine.maxJumpingPower * 200 / (double)((now() - readyToJumpTime) / 1e6)));
                } else if (action == 1) {
                    state = 1;
                } else if (action == 2) {
                    state = 3;
                }
                break;
        }


        /*switch (state) {
            case 0:
                // Initial state
                if (action == 0) {
                    state = 11;
                } else if (action == 1) {
                    state = 1;
                } else if (action == 2) {
                    state = 3;
                }
                break;
            case 1:
                // Lift up Leg A
                if (action == 0) {
                    state = 11;
                } else {
                    if (action == 2) {
                        state = 9;
                    } else if (action == 3) {
                        state = 2;
                    }
                    walk();
                }
                break;
            case 2:
                // Put down Leg A. Just finished one step.
                if (action == 0) {
                    state = 11;
                } else if (action == 1) {
                    state = 5;
                    kick();
                } else if (action == 2) {
                    state = 3;
                }
                break;
            case 3:
                // Lift up Leg B
                if (action == 0) {
                    state = 11;
                } else {
                    if (action == 1) {
                        state = 10;
                    } else if (action == 3) {
                        state = 4;
                    }
                    walk();
                }
                break;
            case 4:
                // Put down Leg B. Just finished one step.
                if (action == 0) {
                    state = 11;
                } else if (action == 1) {
                    state = 1;
                } else if (action == 2) {
                    state = 7;
                    kick();
                }
                break;
            case 5:
                // Lift up Leg A. Just finished kicking.
                if (action == 0) {
                    state = 11;
                } else if (action == 2) {
                    state = 9;
                    walk();
                } else if (action == 3) {
                    state = 6;
                }
                break;
            case 6:
                // Put down Leg A.
                if (action == 0) {
                    state = 11;
                } else if (action == 1) {
                    state = 5;
                    kick();
                } else if (action == 2) {
                    state = 3;
                }
                break;
            case 7:
                // Lift up Leg B. Just finished kicking.
                if (action == 0) {
                    state = 11;
                } else if (action == 1) {
                    state = 10;
                    walk();
                } else if (action == 3) {
                    state = 8;
                }
                break;
            case 8:
                // Put down Leg B.
                if (action == 0) {
                    state = 11;
                } else if (action == 1) {
                    state = 1;
                } else if (action == 2) {
                    state = 7;
                    kick();
                }
                break;
            case 9:
                // Put down Leg A while lifting Leg B. Just finished one step.
                if (action == 0) {
                    state = 11;
                } else {
                    if (action == 1) {
                        state = 10;
                    } else if (action == 3) {
                        state = 4;
                    }
                    walk();
                }
                break;
            case 10:
                // Put down Leg B while lifting Leg A. Just finished one step.
                if (action == 0) {
                    state = 11;
                } else {
                    if (action == 2) {
                        state = 9;
                    } else if (action == 3) {
                        state = 2;
                    }
                    walk();
                }
                break;
            case 11:
                // Ready to jump.
                if (action == 1) {
                    state = 1;
                } else if (action == 2) {
                    state = 3;
                } else if (action == 3) {
                    //jump(Math.min(GameEngine.maxJumpingPower, GameEngine.maxJumpingPower * 200 / (double)((now() - readyToJumpTime) / 1e6)));
                    state = 12;
                }
                break;
            case 12:
                // Jump
                if (action == 0) {
                    state = 11;
                    jump(Math.min(GameEngine.maxJumpingPower, GameEngine.maxJumpingPower * 200 / (double)((now() - readyToJumpTime) / 1e6)));
                } else if (action == 1) {
                    state = 1;
                } else if (action == 2) {
                    state = 3;
                }
                break;
        }*/
        prevAction = action;

        if(prevState != state) {
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