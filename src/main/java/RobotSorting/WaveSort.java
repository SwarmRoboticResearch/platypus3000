package RobotSorting;

import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import platypus3000.analyticstools.overlays.DiscreteStateColorOverlay;
import platypus3000.simulation.Odometer;
import platypus3000.simulation.communication.Message;
import platypus3000.simulation.communication.MessagePayload;
import platypus3000.simulation.control.RobotController;
import platypus3000.simulation.control.RobotInterface;
import platypus3000.utils.NeighborState.PublicState;
import platypus3000.utils.NeighborState.StateManager;
import platypus3000.utils.VectorUtils;

/**
 * Sorts a straight line of robots.
 */
public class WaveSort {
    private static final float ACCURACY_FINAL_POS = 0.003f;
    float ACCURACY_INTERMEDIATE_POS = 0.01f;
    float SPEEDUP = 3;

    WaveSortState publicState = new WaveSortState();
    StateManager stateManager;
    int STATE = 0;
    int SUB_STATE = 0;
    Integer left;
    Integer right;

    boolean is_master;
    Integer newLeftNbr;
    Integer newRightNbr;

    Vec2 moveToPosition;
    Odometer odometer;

    DiscreteStateColorOverlay stateColorOverlay;
    private Vec2 intermediatePosition;


    public WaveSort(RobotController controller, StateManager stateManager, Integer left, Integer right){
        stateColorOverlay = new DiscreteStateColorOverlay(controller, "Sorting State", 5);
        this.left = left;
        this.right = right;
        this.stateManager = stateManager;
        stateManager.setLocalState(WaveSort.class.getName(), publicState);
    }

    boolean isMob(){
        return left != null && right != null;
    }

    boolean isMin(){
        return left == null;
    }

    boolean isMax(){
        return right == null;
    }

    boolean sorted = false;
    public boolean loop(RobotInterface robot) {
        if(isMob()) {
            assert left != null && right !=null;

            switch (STATE) {
                case 0: //Sleep
                    for (Message m : robot.incomingMessages()) {
                        if(m.msg instanceof InitMsg){
                            is_master = ((InitMsg) m.msg).type_is_master;
                            newLeftNbr = ((InitMsg) m.msg).newLeft;
                            sorted = ((InitMsg) m.msg).sorted;
                            STATE = 1;
                            m.delete();
                            break;
                        }
                    }
                    if(left!=null && right != null && robot.getNeighborhood().contains(left) && robot.getNeighborhood().contains(right)) robot.setMovement(robot.getNeighborhood().getById(left).getLocalPosition().add(robot.getNeighborhood().getById(right).getLocalPosition()));
                    break;
                case 1: //Wait
                    if(isNeighborAvailable(robot, right) && stateManager.<WaveSortState>getState(right, WaveSort.class.getName()).iteration==publicState.iteration){
                        STATE = 2;
                    }
                    break;
                case 2: //Synchronize
                    if(is_master){
                        if(right<robot.getID()){ //Swap

                            switch (SUB_STATE) {
                                case 0: //Send Init and Ack
                                    robot.send(new InitMsg(false, newLeftNbr, sorted&&robot.getID()>left&&robot.getID()<right), right);
                                    robot.send(new AckMsg(right), left);
                                    SUB_STATE = 1;
                                    break;
                                case 1: //Wait for ACK
                                    for(Message m: robot.incomingMessages()){
                                        if(m.msg instanceof AckMsg){
                                            newRightNbr = ((AckMsg) m.msg).newRight;
                                            SUB_STATE = 0;
                                            STATE = 3; //->Swap
                                            m.delete();
                                            break;
                                        }
                                    }
                                    break;
                            }
                        } else {  //No swap
                            switch (SUB_STATE){
                                case 0:
                                    robot.send(new InitMsg(false, robot.getID(),sorted&&robot.getID()>left&&robot.getID()<right), right);
                                    robot.send(new AckMsg(robot.getID()), left);
                                    SUB_STATE = 1;
                                    break;
                                case 1:
                                    for(Message m: robot.incomingMessages()) {
                                        if (m.msg instanceof AckMsg) {
                                            left = newLeftNbr;
                                            STATE = 4; //->Iterate
                                            SUB_STATE = 0;
                                            m.delete();
                                            break;
                                        }
                                    }
                                    break;
                            }
                        }
                    } else {
                        if(left>robot.getID()){ //Swap

                            switch (SUB_STATE){
                                case 0:
                                    robot.send(new InitMsg(true, left,sorted&&robot.getID()>left&&robot.getID()<right), right);
                                    SUB_STATE = 1;
                                    break;
                                case 1://Wait for ACK
                                    for(Message m: robot.incomingMessages()){
                                        if(m.msg instanceof AckMsg){
                                            newRightNbr = ((AckMsg) m.msg).newRight;
                                            SUB_STATE = 2;
                                            m.delete();
                                            break;
                                        }
                                    }
                                    break;
                                case 2:
                                    robot.send(new AckMsg(newRightNbr), left);
                                    SUB_STATE = 0;
                                    STATE = 3; //->Swap
                                    break;
                            }
                        } else {  //No swap
                            switch (SUB_STATE){
                                case 0:
                                    robot.send(new InitMsg(true, robot.getID(),sorted&&robot.getID()>left&&robot.getID()<right), right);
                                    robot.send(new AckMsg(robot.getID()), left);
                                    SUB_STATE = 1;
                                    break;
                                case 1://Wait for ACK
                                    for(Message m: robot.incomingMessages()){
                                        if(m.msg instanceof AckMsg){
                                            newRightNbr = ((AckMsg) m.msg).newRight;
                                            right = newRightNbr;
                                            SUB_STATE = 0;
                                            STATE = 4;
                                            m.delete();
                                            break;
                                        }
                                    }
                                    break;
                            }
                        }

                    }
                    break;
                case 3: //Swap
                    robot.setMovementAccuracy(0.3f);
                    if(is_master){ //Swap with right neighbor
                        if(moveToPosition == null){
                            moveToPosition = robot.getNeighborhood().getById(right).getLocalPosition();
                            odometer = robot.getOdometryVector();
                            intermediatePosition = VectorUtils.rotate(moveToPosition, 0.5f*MathUtils.PI);
                            intermediatePosition.normalize();
                            intermediatePosition.mulLocal(0.15f).addLocal(moveToPosition.mul(0.5f));

                        }
                        if(intermediatePosition==null) {
                        Vec2 v = odometer.transformOldPosition(moveToPosition);

                        if(robot.hasCollision()){
                            //v = robot.getLocalPositionOfCollision().mul(-100);
                        }
                        robot.setMovement(v.mul(SPEEDUP));
                        if(v.lengthSquared()<ACCURACY_FINAL_POS) {
                            left = right;
                            right = newRightNbr;
                            STATE = 4;
                        }
                        } else {
                            Vec2 v = odometer.transformOldPosition(intermediatePosition);
                            //v.normalize();
                            robot.setMovement(v.mul(SPEEDUP));
                            if(v.lengthSquared()<ACCURACY_INTERMEDIATE_POS){
                                intermediatePosition = null;
                            }
                        }
                    } else { //Swap with left neighbor
                        if(moveToPosition == null){
                            moveToPosition = robot.getNeighborhood().getById(left).getLocalPosition();
                            odometer = robot.getOdometryVector();
                            intermediatePosition = VectorUtils.rotate(moveToPosition, 0.5f*MathUtils.PI);
                            intermediatePosition.normalize();
                            intermediatePosition.mulLocal(0.15f).addLocal(moveToPosition.mul(0.5f));

                        }
                        if(intermediatePosition==null) {
                            Vec2 v = odometer.transformOldPosition(moveToPosition);
                            if (robot.hasCollision()) {
                                v = robot.getLocalPositionOfCollision().mul(-100);
                            }
                            robot.setMovement(v.mul(SPEEDUP));
                            if (v.lengthSquared() < ACCURACY_FINAL_POS) {
                                right = left;
                                left = newLeftNbr;
                                STATE = 4;
                            }
                        } else {
                            Vec2 v = odometer.transformOldPosition(intermediatePosition);
                            //v.normalize();
                            robot.setMovement(v.mul(SPEEDUP));
                            if(v.lengthSquared()<ACCURACY_INTERMEDIATE_POS){
                                intermediatePosition = null;
                            }
                        }
                    }
                    break;
                case 4: //Iterate
                    moveToPosition = null;
                    assert SUB_STATE == 0;
                    publicState.iteration++;
                    STATE = 0;
                    break;
            }
        } else if(isMin()){

            switch (STATE) {
                case 0:
                    if (isNeighborAvailable(robot, right)) {
                        robot.send(new InitMsg(true, robot.getID(),true), right);
                        STATE = 1;
                    }
                    break;
                case 1://Wait for ACK
                    for (Message m : robot.incomingMessages()) {
                        if (m.msg instanceof AckMsg) {
                            right = ((AckMsg) m.msg).newRight;
                            STATE = 2;
                            publicState.iteration++;
                            m.delete();
                            break;
                        }
                    }
                    break;
                case 2:
                    if (isNeighborAvailable(robot, right)) {
                        robot.send(new InitMsg(false, robot.getID(),true), right);
                        STATE = 3;
                    }
                    break;
                case 3://Wait for ACK
                    for (Message m : robot.incomingMessages()) {
                        if (m.msg instanceof AckMsg) {
                            right = ((AckMsg) m.msg).newRight;
                            STATE = 0;
                            publicState.iteration++;
                            m.delete();
                            break;
                        }
                    }
                    break;
            }
        } else if(isMax()){

            SortingController.abort = true;//TODO

            if(isNeighborAvailable(robot, left)){

            }
            for(Message m: robot.incomingMessages()){
                if(m.msg instanceof InitMsg){
                    robot.send(new AckMsg(robot.getID()), left);
                    left = ((InitMsg) m.msg).newLeft;
                    publicState.iteration++;
                    if(((InitMsg) m.msg).sorted){
                        if(!said){
                            System.out.println("Sorted: "+robot.getLocalTime());
                            SortingController.addResultSorting(robot.getLocalTime());
                            said = true;
                        }
                        SortingController.abort = true;

                    }
                    m.delete();
                }
            }
        }
        stateColorOverlay.setState(STATE);
        //robot.say(Integer.toString(publicState.iteration));
        return false;
    }

    boolean said = false;

    boolean isNeighborAvailable(RobotInterface r, int id){
        return r.getNeighborhood().contains(id) && stateManager.getState(id, WaveSort.class.getName())!=null && stateManager.<WaveSortState>getState(id, WaveSort.class.getName()).iteration == publicState.iteration;
    }



}
class InitMsg implements MessagePayload{
    boolean type_is_master;
    int newLeft;
    boolean sorted;
    InitMsg(boolean type_is_master, int newLeft, boolean sorted){
        this.type_is_master = type_is_master;
        this.newLeft = newLeft;
        this.sorted = sorted;
    }

    @Override
    public MessagePayload deepCopy() {
        return new InitMsg(type_is_master, newLeft, sorted);
    }
}

class AckMsg implements MessagePayload{
    int newRight;
    AckMsg(int newRight){
        this.newRight = newRight;
    }

    @Override
    public MessagePayload deepCopy() {
        return new AckMsg(newRight);
    }
}

class WaveSortState extends PublicState {
    int iteration;


    @Override
    public PublicState clone() throws CloneNotSupportedException {
        WaveSortState cloned = new WaveSortState();
        cloned.iteration = iteration;

        return cloned;
    }
}

