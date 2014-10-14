package sample;

import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.transform.Rotate;

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

    private Group body;
    private ImageView head;
    private Group leftLeg;
    private ImageView leftLegUpper;
    private ImageView leftLegLower;
    private ImageView leftFoot;
    private Group rightLeg;
    private ImageView rightLegUpper;
    private ImageView rightLegLower;
    private ImageView rightFoot;
    
    private double lastXVelocityDecrease;
    final private double initialXVelocityDampingRatio = 0.002;
    final private double XVelocityDampingRatioMultiplier = 1.05;

    final private double BEND = 0.55;
    final private double NOT_BEND = 0.4;

    private long readyToJumpTime;

    private int prevAction;
    private int state;

    public GameCharacter() {
        velocityX = GameEngine.getSceneSpeed();
        velocityY = 0.0;
        jumping = false;

        Image headImage = new Image("sample/images/Body.png");
        Image upperLegImage = new Image("sample/images/UpperLeg.png");
        Image lowerLegImage = new Image("sample/images/LowerLeg.png");
        Image footImage = new Image("sample/images/Foot.png");

        body = new Group();

        head = new ImageView(headImage);
        head.setTranslateY(-50);

        leftLeg = new Group();
        leftLegUpper = new ImageView(upperLegImage);
        leftLegLower = new ImageView(lowerLegImage);
        leftFoot = new ImageView(footImage);
        leftLegUpper.setTranslateY(-4);
        leftLegLower.setTranslateX(2);
        leftLegLower.setTranslateY(8);
        leftFoot.setTranslateX(2);
        leftFoot.setTranslateY(8);
        leftLeg.setTranslateX(4);
        leftLeg.setTranslateY(-20);
        leftLeg.getChildren().add(leftLegUpper);
        leftLeg.getChildren().add(leftLegLower);
        //leftLeg.getChildren().add(leftFoot);

        rightLeg = new Group();
        rightLegUpper = new ImageView(upperLegImage);
        rightLegLower = new ImageView(lowerLegImage);
        rightFoot = new ImageView(footImage);
        rightLegUpper.setTranslateY(-4);
        rightLegLower.setTranslateX(2);
        rightLegLower.setTranslateY(8);
        rightFoot.setTranslateX(2);
        rightFoot.setTranslateY(8);
        rightLeg.setTranslateX(4);
        rightLeg.setTranslateY(-20);
        rightLeg.getChildren().add(rightLegUpper);
        rightLeg.getChildren().add(rightLegLower);
        //rightLeg.getChildren().add(rightFoot);

        body.getChildren().add(leftLeg);
        body.getChildren().add(head);
        body.getChildren().add(rightLeg);

        this.getChildren().add(body);
    }

    public void accelerate(double aX, double aY) {
        if (jumping) return;
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
        velocityX -= dampingRatio * (velocityX - GameEngine.defaultSceneSpeed);
        velocityX = (velocityX - GameEngine.defaultSceneSpeed < 0.01) ? GameEngine.defaultSceneSpeed : velocityX;
        velocityY = velocityY + GameEngine.gravity;
    }

    @Override
    public void move() {

        //System.out.println(state);

        leftLeg.getTransforms().clear();
        leftLegLower.getTransforms().clear();
        rightLeg.getTransforms().clear();
        rightLegLower.getTransforms().clear();

        if((state == 2) || (state == 9) || (state == 4) || (state == 10)) {

            leftLeg.getTransforms().add(new Rotate(120 * leftBend - 60, 4, 0));
            rightLeg.getTransforms().add(new Rotate(120 * rightBend - 60, 4, 0));

            //rightLegLower.getTransforms().add(new Rotate(160 * (-1 * rightBend*(rightBend-2)), 3, 2.5));
            if((state == 2) || (state == 9)) {
                leftLegLower.getTransforms().add(new Rotate(-160 * (Math.pow((leftBend - 1), 2) - 1), 3, 2.5));
                rightLegLower.getTransforms().add(new Rotate(160 * Math.pow(rightBend, 2), 3, 2.5));
            } else {
                leftLegLower.getTransforms().add(new Rotate(160 * Math.pow(leftBend, 2), 3, 2.5));
                rightLegLower.getTransforms().add(new Rotate(-160 * (Math.pow((rightBend - 1), 2) - 1), 3, 2.5));
            }
        } else {
            leftLeg.getTransforms().add(new Rotate(-80 * leftBend, 4, 0));
            rightLeg.getTransforms().add(new Rotate(-80 * rightBend, 4, 0));
            leftLegLower.getTransforms().add(new Rotate(160 * leftBend, 3, 2.5));
            rightLegLower.getTransforms().add(new Rotate(160 * rightBend, 3, 2.5));
        }


        double vertOffset = body.getBoundsInLocal().getMaxY();
        //System.out.println("vert " + vertOffset);
        body.setTranslateY(-vertOffset);


       // System.out.println("pos: " + getPositionX());
        if (getTranslateX() > 512 && velocityX > 0) {
            GameEngine.updateSceneSpeed(-velocityX + GameEngine.defaultSceneSpeed);
            //System.out.println("pass mid point");
        } else {
            this.setTranslateX(this.getTranslateX() + velocityX);
            GameEngine.updateSceneSpeed(GameEngine.defaultSceneSpeed);
        }
        this.setTranslateY(this.getTranslateY() + velocityY);
        updateSpeed();
    }

    public void updateSpeed() {
        damping();
    }

    public boolean onTheGround() {
        return !jumping;
    }

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
        System.out.println("Walk: " + state);
        accelerate(100 / Main.fps, 0);
    }

    public void kick() {
        System.out.println("kick");
    }

    public void collideForward() {
        velocityX = GameEngine.getSceneSpeed();
    }

    public void collideDownward(double y) {
        land(y);
    }

    private void DFA(int action) {
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
        }
        prevAction = action;
    }
}