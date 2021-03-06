package projects.Sorting;

import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import org.jgrapht.alg.ConnectivityInspector;
import platypus3000.algorithms.distributedalgorithms.*;
import platypus3000.analyticstools.overlays.DiscreteStateColorOverlay;
import platypus3000.analyticstools.overlays.VectorOverlay;
import platypus3000.simulation.*;
import platypus3000.simulation.Robot;
import platypus3000.simulation.communication.Message;
import platypus3000.simulation.communication.MessagePayload;
import platypus3000.simulation.control.RobotController;
import platypus3000.simulation.control.RobotInterface;
import platypus3000.simulation.neighborhood.LocalNeighborhood;
import platypus3000.simulation.neighborhood.NeighborView;
import platypus3000.utils.AngleUtils;
import platypus3000.utils.NeighborState.PublicState;
import platypus3000.utils.NeighborState.StateManager;
import platypus3000.visualisation.Colors;
import platypus3000.visualisation.VisualisationWindow;

import java.awt.*;
import java.io.IOException;
import java.util.*;

/**
 * Created by doms on 10/2/14.
 */
public class SortingController extends RobotController {
    private static final float REPULSION_RANGE = 0.05f;
    private static final float ATTRACTION_RANGE = 0.5f;
    PublicSortingState publicSortingState;
    StateManager stateManager = new StateManager();
    Vec2 pathRightVis = new Vec2();
    private Vec2 pathLeftVis = new Vec2();
    Vec2 movevis = new Vec2();

    DiscreteStateColorOverlay stateOverlay;
    DiscreteStateColorOverlay pathStateOverlay;
    DiscreteStateColorOverlay VISUALISATION_LEADERPATHETC;
    VectorOverlay VISUALISATION_CONTRACTION;

    VectorOverlay straighteningOverlay;
    VectorOverlay integrationOverlay;

    int STATE = 0;


    private WaveSort waveSort;


    public SortingController(int number) {

    }

    public void init(RobotInterface r) {
        VISUALISATION_LEADERPATHETC = new DiscreteStateColorOverlay(this, "Leader/Path/etc.", new String[]{"None","Min","Local Min","Max","Local Max", "Interior","Close","Distanced"}, new int[]{Colors.WHITE, Colors.BLUE, Colors.BLACK, Colors.CYAN, Colors.BLACK,Colors.GREEN, Colors.DARK_GRAY, Colors.LIGHT_GRAY});
        VISUALISATION_CONTRACTION = new VectorOverlay(this,"Contraction", new Vec2(), Colors.BLUE);
        new VectorOverlay(this, "PathRight", pathRightVis);
        new VectorOverlay(this, "PathLeft", pathLeftVis);
        new VectorOverlay(this, "Move", movevis);
        integrationOverlay = new VectorOverlay(this, "Integration", null);
        straighteningOverlay = new VectorOverlay(this, "Straightening", new Vec2());
        stateOverlay = new DiscreteStateColorOverlay(this, "States", 8);
        pathStateOverlay = new DiscreteStateColorOverlay(this, "PathState",3);
        publicSortingState = new PublicSortingState(r.getID());
        stateManager.setLocalState(SortingController.class.getName(), publicSortingState);
        maxLeaderElection = new EchoWaveLeaderElection<Integer>(this, r.getID(), new Comparator<Integer>() {
            @Override
            public int compare(Integer integer, Integer integer2) {
                return integer2-integer;
            }
        }, "Max");
        minLeaderElection = new EchoWaveLeaderElection<Integer>(this,  r.getID(),new Comparator<Integer>() {
            @Override
            public int compare(Integer integer, Integer integer2) {
                return integer-integer2;
            }
        }, "Min");

    }

    EchoWaveLeaderElection<Integer> maxLeaderElection;
    EchoWaveLeaderElection<Integer> minLeaderElection;


    MinimalSpanningTree mqst;
    MinimalSpanningTree contractionTree;
    EchoWave initialPathIsDetermined = new EchoWave("InitialPathIsDetermined");
    boolean waitingForIntegrationAcc = false;

    Integer receivedPathMessageFrom = null;

    @Override
    public void loop(RobotInterface robot) {
        robot.say(Integer.toString(robot.getID()));
        VISUALISATION_CONTRACTION.drawnVector = new Vec2();
        robot.setMovementAccuracy(0.3f);
        stateManager.loop(robot);
        robot.setMovement(new Vec2());
        //robot.say(publicSortingState.number + "");
        movevis.setZero();
        initialPathIsDetermined.loop(robot);

        if(mqst==null){
            HashMap<Integer, Float> hm = new HashMap<Integer, Float>();
            for(NeighborView n: robot.getNeighborhood()){
                hm.put(n.getID(), n.getDistance()*n.getDistance());
            }
            mqst = new MinimalSpanningTree(this, minLeaderElection.isLeader(), "Initial Path Tree",hm);
        }
        mqst.loop(robot);

        for(Message m: robot.incomingMessages()){
            if(m.msg instanceof AccMsg){
                if(((AccMsg) m.msg).left){
                    publicSortingState.left = m.sender;
                } else {
                    publicSortingState.right = m.sender;
                    waitingForIntegrationAcc= false;
                }
                m.delete();
            } else if (m.msg instanceof RejMessage){
                waitingForIntegrationAcc = false;
                m.delete();
            } else if (m.msg instanceof PathMessage){
                receivedPathMessageFrom = m.sender;
                m.delete();
            }
        }

        minLeaderElection.loop(robot);
        maxLeaderElection.loop(robot);
        if(publicSortingState.path!=null && publicSortingState.path) VISUALISATION_LEADERPATHETC.setState(5);
        if(minLeaderElection.isLeader()) VISUALISATION_LEADERPATHETC.setState(1);
        if(maxLeaderElection.isLeader()) VISUALISATION_LEADERPATHETC.setState(3);

        if(contractionTree!=null && !contractionTree.isTerminated()) contractionTree.loop(robot);

        switch (STATE) {
            case 0: //Min-Max-Determination incl. Tree

                if(minLeaderElection.isTerminated() && maxLeaderElection.isTerminated()){
                    STATE = 1;
                    if(minLeaderElection.isLeader()) mqst.init(robot);
                    if(minLeaderElection.getLeaderValue()!=0){
                        abort = true;
                    }
                }
                break;
            case 1:
                if(mqst.isTerminated()){
                    STATE = 2;
                }
                break;
            case 2: //Min-Max-Path and static contraction tree

                //Detect if the robot is on the path
                if(publicSortingState.path == null){
                    if(maxLeaderElection.isLeader()) {
                        publicSortingState.path = true;
                        publicSortingState.left = mqst.getPredecessor();
                        robot.send(new PathMessage(), mqst.getPredecessor());
                    }
                    if(receivedPathMessageFrom!=null){
                        if(minLeaderElection.isLeader()){
                            initialPathIsDetermined.init(robot);
                            publicSortingState.path = true;
                            publicSortingState.right = receivedPathMessageFrom;
                        } else {
                            robot.send(new PathMessage(), mqst.getPredecessor());
                            publicSortingState.path = true;
                            publicSortingState.right = receivedPathMessageFrom;
                            publicSortingState.left = mqst.getPredecessor();
                        }
                    }

                }

                //If the initial path is determined, build the contraction tree
                if(contractionTree==null && initialPathIsDetermined.isTerminated()){
                    if(publicSortingState.path == null) publicSortingState.path = false;
                    HashMap<Integer, Float> hm = new HashMap<Integer, Float>();
                    for(NeighborView n: robot.getNeighborhood()){
                        if(publicSortingState.right!=null && publicSortingState.right == n.getID()){
                            hm.put(n.getID(),0f);
                        } else if(publicSortingState.left!=null && publicSortingState.left == n.getID()){
                            hm.put(n.getID(),0f);
                        } else {
                            hm.put(n.getID(), n.getDistance());
                        }
                    }
                    contractionTree = new MinimalSpanningTree(this, minLeaderElection.isLeader(), "ContractionTree",hm);
                    if(minLeaderElection.isLeader()) contractionTree.init(robot);
                }

                //Move closer to predecessor if necessary
                if(contractionTree!=null && !contractionTree.isTerminated() && contractionTree.areChildrenTerminated()){
                    robot.setMovementAccuracy(0.3f);
                    boolean constr = robot.getNeighborhood().getById(contractionTree.getPredecessor()).getDistance()>0.8f*getConfiguration().getRobotCommunicationRange();
                    contractionTree.delay(constr);
                    if(constr){
                        robot.setMovement(robot.getNeighborhood().getById(contractionTree.getPredecessor()).getLocalPosition());
                    }
                }

                //If the contraction tree is built, transit to next phase
                if(contractionTree!=null && contractionTree.isTerminated()){
                    STATE = 3;
                    publicSortingState.path_hops = contractionTree.getValue();
                }
                break;
            case 3: //integrate other robots into path


                integrationOverlay.drawnVector = null;
                if (publicSortingState.path) {
                    for (Message m : robot.incomingMessages()) {
                        if (m.msg instanceof OfferMsg) {
                            robot.send(new RejMessage(), m.sender);
                            //assert false;
                            m.delete();
                        }
                    }
                    if(publicSortingState.left!=null){
                        assert robot.getNeighborhood().contains(publicSortingState.left);
                        publicSortingState.leftIntegrationPos = robot.getNeighborhood().getById(publicSortingState.left).getLocalPosition().mul(0.5f);
                    }
                    if(publicSortingState.right!=null){
                        assert robot.getNeighborhood().contains(publicSortingState.right);
                        publicSortingState.rightIntegrationPos = robot.getNeighborhood().getById(publicSortingState.right).getLocalPosition().mul(0.5f);
                    }
                    publicSortingState.path_hops = 0f;

                    if(!waitingForIntegrationAcc && publicSortingState.right!=null) {
                        for (PublicSortingState nbrState : stateManager.<PublicSortingState>getStates(SortingController.class.getName())) {
                            if(nbrState.path == null) continue; //Some synchronization issues....
                            if (!nbrState.path &&
                                    robot.getNeighborhood().contains(nbrState.getRobotID()) &&
                                    robot.getNeighborhood().getById(nbrState.getRobotID()).getLocalPosition().sub(publicSortingState.rightIntegrationPos).lengthSquared() < 0.1f*0.1f) {
                                robot.send(new OfferMsg(robot.getID(), publicSortingState.right), nbrState.getRobotID());
                                //publicSortingState.right = null;
                                waitingForIntegrationAcc = true;
                                break;
                            }
                        }
                    }


                    //Straightening
                    if (publicSortingState.left != null && publicSortingState.right != null) {
                        NeighborView left = robot.getNeighborhood().getById(publicSortingState.left);
                        NeighborView right = robot.getNeighborhood().getById(publicSortingState.right);
                        if (left != null && right != null) {
                            robot.setMovement(left.getLocalPosition().add(right.getLocalPosition()).mul(4));
                            if(robot.hasCollision() && MathUtils.randomFloat(0,1)<0.5f){
                                robot.setMovement(robot.getLocalPositionOfCollision().mul(-30));
                            }
                        }
                    }
                    //Check if done
                    boolean no_nbrs_in_range_not_in_path = true;
                    for(NeighborView n: robot.getNeighborhood()){
                        if(stateManager.getState(n.getID(), SortingController.class.getName())== null || stateManager.<PublicSortingState>getState(n.getID(), SortingController.class.getName()).path==null || !stateManager.<PublicSortingState>getState(n.getID(), SortingController.class.getName()).path){
                            no_nbrs_in_range_not_in_path = false;
                        }
                    }
                    if (no_nbrs_in_range_not_in_path) {
                        if (minLeaderElection.isLeader() || (stateManager.getState(publicSortingState.left, SortingController.class.getName()) != null && stateManager.<PublicSortingState>getState(publicSortingState.left, SortingController.class.getName()).left_done)) {
                            publicSortingState.left_done = true;
                        }
                        if (publicSortingState.left_done) {
                            //assert maxLeaderElection.isLeader() || publicSortingState.right !=null;
                            if (maxLeaderElection.isLeader() ||
                                    (publicSortingState.right != null && stateManager.getState(publicSortingState.right, SortingController.class.getName()) != null
                                            && stateManager.<PublicSortingState>getState(publicSortingState.right, SortingController.class.getName()).right_done)) {
                                publicSortingState.right_done = true;
                                //System.out.println("Done!");
                                if(maxLeaderElection.isLeader()){
                                    System.out.println("Integration: "+robot.getLocalTime());
                                    SortingController.addResultIntegration(robot.getLocalTime());
                                }
                                integrated++;
                                STATE = 5;
                            }
                        }
                    }

                } else {
                    VISUALISATION_LEADERPATHETC.setState(7);//Distanced, change if close
                    //Not in Path yet
                    boolean gotAcc = false;
                    for (Message m : robot.incomingMessages()) {
                        if(m.msg instanceof OfferMsg){
                            if(gotAcc){     //Reject all other Offer Messages
                                robot.send(new RejMessage(), m.sender);
                            } else {
                                publicSortingState.right = ((OfferMsg) m.msg).right;
                                publicSortingState.left = ((OfferMsg) m.msg).left;
                                publicSortingState.path = true;
                                publicSortingState.path_hops = 0f;
                                robot.send(new AccMsg(false), publicSortingState.left);
                                robot.send(new AccMsg(true), publicSortingState.right);
                                m.delete();
                                gotAcc = true;
                            }
                            m.delete();
                        }
                    }
                    if(publicSortingState.path) break;

                    //find best neighbor
                    Integer bstNbr = null;
                    Float bestNbr_hops = null;
                    Vec2 bestNbrPos = null;
                    for (PublicSortingState nbrState : stateManager.<PublicSortingState>getStates(SortingController.class.getName())) {
                        if(robot.getNeighborhood().contains(nbrState.getRobotID())){
                            NeighborView nbr = robot.getNeighborhood().getById(nbrState.getRobotID());

                            if(nbrState.path_hops == null) continue;
                            float nbrDistanceValue = nbrState.path_hops+nbr.getDistance();
                            if(bstNbr == null || bestNbr_hops>nbrDistanceValue){
                                bestNbr_hops = nbrDistanceValue;
                                bstNbr = nbrState.getRobotID();
                                if(nbrState.path && (nbrState.getRobotID() == contractionTree.getPredecessor() || robot.getNeighborhood().getById(nbrState.getRobotID()).getDistance()<0.8*getConfiguration().getRobotCommunicationRange())) {
                                    if (nbrState.rightIntegrationPos != null && (nbrState.leftIntegrationPos == null || (nbr.transformPointToObserversViewpoint(nbrState.rightIntegrationPos.mul(1/nbrState.rightIntegrationPos.length())).lengthSquared() < nbr.transformPointToObserversViewpoint(nbrState.leftIntegrationPos.mul(1/nbrState.leftIntegrationPos.length())).lengthSquared()))) {
                                        //if (nbrState.rightIntegrationPos != null && (nbrState.leftIntegrationPos == null || (nbr.transformPointToObserversViewpoint(nbrState.rightIntegrationPos).lengthSquared() < nbr.transformPointToObserversViewpoint(nbrState.leftIntegrationPos).lengthSquared()))) {
                                        bestNbrPos = nbr.transformPointToObserversViewpoint(nbrState.rightIntegrationPos);
                                    } else if (nbrState.leftIntegrationPos != null) {
                                        bestNbrPos = nbr.transformPointToObserversViewpoint(nbrState.leftIntegrationPos);

                                    }
                                    VISUALISATION_LEADERPATHETC.setState(6);//Close
                                } else {
                                    if(robot.getNeighborhood().contains(contractionTree.getPredecessor())){
                                        bestNbrPos = robot.getNeighborhood().getById(contractionTree.getPredecessor()).getLocalPosition();
                                    } else {
                                        bestNbrPos = robot.getNeighborhood().getById(bstNbr).getLocalPosition();
                                    }
                                    if(bestNbrPos.lengthSquared()<0.1f) bestNbrPos = new Vec2();
                                }
                            }
                        }
                    }
                    integrationOverlay.drawnVector = bestNbrPos;
                    if(bestNbrPos!=null){
                        VISUALISATION_CONTRACTION.drawnVector = bestNbrPos.clone();

                        publicSortingState.path_hops = bestNbr_hops;
                        Vec2 collisionAvoidance = new Vec2();
                        Float closestNbr = null;
                        for(NeighborView n: robot.getNeighborhood()){
                            if(n.getLocalPosition().lengthSquared() < (2*getConfiguration().getRobotRadius()+0.03f)*(2*getConfiguration().getRobotRadius()+0.03f) || retreating){
                                if(n.getLocalPosition().sub(bestNbrPos).lengthSquared()<bestNbrPos.lengthSquared() || (stateManager.contains(n.getID(), SortingController.class.getName()) && stateManager.<PublicSortingState>getState(n.getID(), SortingController.class.getName()).path)){
                                    float x = 1f/n.getLocalPosition().lengthSquared();
                                    collisionAvoidance.addLocal(n.getLocalPosition().mul(-x));
                                    if(n.getLocalPosition().lengthSquared() < (2*getConfiguration().getRobotRadius()+0.03f)*(2*getConfiguration().getRobotRadius()+0.03f)) retreating = true;
                                    if(closestNbr == null || n.getLocalPosition().lengthSquared()<closestNbr) closestNbr = n.getLocalPosition().lengthSquared();
                                }
                            }
                        }
                        if(closestNbr == null || closestNbr>0.2f*0.2f) retreating = false;

                        bestNbrPos.mulLocal(5);
                        //applyCollisionAvoidance(robot.getNeighborhood(), bestNbrPos);
                        robot.setMovement(bestNbrPos.add(collisionAvoidance));

                        if(publicSortingState.path == null && robot.hasCollision() && MathUtils.randomFloat(0,1)<0.5f){
                            //robot.setMovement(robot.getLocalPositionOfCollision().mul(-3));
                        }

                    }
                }
                break;
            case 5:
                integrationOverlay.drawnVector = null;
                //Straightening
                if (publicSortingState.left != null && publicSortingState.right != null) {
                    PublicSortingState leftNbrState = stateManager.<PublicSortingState>getState(publicSortingState.left, SortingController.class.getName());
                    PublicSortingState rightNbrState = stateManager.<PublicSortingState>getState(publicSortingState.right, SortingController.class.getName());

                    NeighborView left = robot.getNeighborhood().getById(publicSortingState.left);
                    NeighborView right = robot.getNeighborhood().getById(publicSortingState.right);

                    if( leftNbrState == null || rightNbrState == null || left == null || right == null ){
                        System.err.println("Error in Neighborhood"+leftNbrState+rightNbrState+left+right);
                        break;
                    }

                    float leftDist =  left.getLocalPosition().length();
                    float rightDist = right.getLocalPosition().length();

                    publicSortingState.avgDistLeft = leftNbrState.avgDistLeft+leftDist;
                    publicSortingState.avgDistRight = rightNbrState.avgDistRight+rightDist;
                    publicSortingState.avgDistLeft_i = leftNbrState.avgDistLeft_i+1;
                    publicSortingState.avgDistRight_i = rightNbrState.avgDistRight_i+1;

                    float avgDist = ((float)publicSortingState.avgDistLeft_i/(publicSortingState.avgDistLeft_i+publicSortingState.avgDistRight_i))*(publicSortingState.avgDistLeft/publicSortingState.avgDistLeft_i)+
                            ((float)publicSortingState.avgDistRight_i/(publicSortingState.avgDistLeft_i+publicSortingState.avgDistRight_i))*(publicSortingState.avgDistRight/publicSortingState.avgDistRight_i);

                    float attrRepLeft = 10;
                    float attrRepRight = 10;

                    straighteningOverlay.drawnVector.set(left.getLocalPosition().add(right.getLocalPosition()));

                    Vec2 v = new Vec2();
                    //v.addLocal(getCollisionAvoidanceVector(robot.getNeighborhood()));

                    if (left != null && right != null) {
                        robot.setMovement(v.add(left.getLocalPosition().mul(attrRepLeft).add(right.getLocalPosition().mul(attrRepRight))).mul(3));
                    }
                }
                if (publicSortingState.left == null) {
                    publicSortingState.left_straight = true;
                    publicSortingState.right_straight = stateManager.getState(publicSortingState.right, SortingController.class.getName()) != null && stateManager.<PublicSortingState>getState(publicSortingState.right, SortingController.class.getName()).right_straight;
                } else if (publicSortingState.right == null) {
                    publicSortingState.right_straight = stateManager.getState(publicSortingState.left, SortingController.class.getName()) != null && stateManager.<PublicSortingState>getState(publicSortingState.left, SortingController.class.getName()).left_straight;
                    publicSortingState.left_straight = publicSortingState.right_straight;
                } else {
                    if (robot.getNeighborhood().getById(publicSortingState.left).getLocalPosition().add(robot.getNeighborhood().getById(publicSortingState.right).getLocalPosition()).lengthSquared() < 0.000005f) {
                        if (stateManager.getState(publicSortingState.left, SortingController.class.getName()) != null && stateManager.<PublicSortingState>getState(publicSortingState.left, SortingController.class.getName()).left_straight) {
                            publicSortingState.left_straight = publicSortingState.left == null || publicSortingState.right == null || (robot.getNeighborhood().getById(publicSortingState.left).getDistance()>0.2f && robot.getNeighborhood().getById(publicSortingState.right).getDistance() > 0.2f);
                            if (publicSortingState.left_straight && stateManager.getState(publicSortingState.right, SortingController.class.getName()) != null && stateManager.<PublicSortingState>getState(publicSortingState.right, SortingController.class.getName()).right_straight) {
                                publicSortingState.right_straight = true;
                            }
                        }
                    }
                }
                if (publicSortingState.right_straight) {
                    assert publicSortingState.left == null || publicSortingState.right == null || (robot.getNeighborhood().getById(publicSortingState.left).getDistance()>0.2f && robot.getNeighborhood().getById(publicSortingState.right).getDistance() > 0.2f);
                    //System.out.println("Straight!");
                    if(maxLeaderElection.isLeader()){
                        System.out.println("Straight: "+ robot.getLocalTime());
                        SortingController.addResultStraightening(robot.getLocalTime());
                    }
                    STATE = 6;
                }
                break;
            case 6:
                if(waveSort==null) waveSort = new WaveSort(this, stateManager, publicSortingState.left, publicSortingState.right);
                if(waveSort.loop(robot)){
                    if(waveSort.isMin()) System.out.println("Sorted!");
                    STATE = 7;
                } else {
                    publicSortingState.left = waveSort.left;
                    publicSortingState.right = waveSort.right;
                }
                break;

        }


        stateOverlay.setState(STATE);
        pathStateOverlay.setState(publicSortingState.path==null?0:(publicSortingState.path?2:1));
        stateManager.broadcast(robot);
        if (publicSortingState.right != null) {
            NeighborView n = robot.getNeighborhood().getById(publicSortingState.right);
            if (n != null) pathRightVis.set(n.getLocalPosition());
        } else {
            pathRightVis.setZero();
        }
        if (publicSortingState.left != null) {
            NeighborView n = robot.getNeighborhood().getById(publicSortingState.left);
            if(n!=null) pathLeftVis.set(n.getLocalPosition());
        } else {
            pathLeftVis.setZero();
        }
    }
    int substate =0;
    boolean retreating = false;

    public static int integrated = 0;
    public static boolean abort =false;
    public static int SIZE = 20;
    public static int SIZE_MAX = 130;
    public static float HIGHT = 10;

    public static long[] times_integration = new long[SIZE_MAX];
    public static long[] times_straightening = new long[SIZE_MAX];
    public static long[] times_sorting = new long[SIZE_MAX];

    public static void main(String[] args) throws IOException{

         loop:
         while(SIZE<=SIZE_MAX) {
         integrated = 0;
         Simulator sim = new Simulator(new Configuration("/home/doms/Projects/SwarmRoboticResearch/platypus3000/src/main/java/projects/Sorting/simulation.properties"));
         sim.createRobot(0, 0,0,0).setController(new SortingController(0));


         System.out.println("EXPERIMENT WITH "+SIZE+" ROBOTS");
         abort = false;
         float l = SIZE*0.4f;// MathUtils.sqrt(SIZE*0.4f*SIZE*0.4f*0.5f);
         for (int i = 1; i < SIZE - 1; i++) {
             sim.createRobot(i, MathUtils.randomFloat(0, l), MathUtils.randomFloat(0, HIGHT), 0).setController(new SortingController(i));
         }
         sim.createRobot(SIZE-1, l, 0, 0).setController(new SortingController(SIZE-1));
         SimulationRunner runner = new SimulationRunner(sim);
         //runner.loop(10);
         for(Robot r: sim.getRobots()){
         if(r.hasCollision()){
         System.out.println("Colliding initial configuration, skip");
         abort = true;
         }
         }
             sim.step();
         if(!(new ConnectivityInspector(sim.getGlobalNeighborhood().getGraph())).isGraphConnected()){
             abort = true;
             //new VisualisationWindow(sim, new Dimension(1800,1000));
         System.out.println("Not connected");
         }
         boolean printed = false;
/**
         new VisualisationWindow(runner, new Dimension(1920,1100));
             runner.paused = true;
             return; }**/
         while (!abort) {
         if (SIZE != integrated && sim.getTime() > 9000) {
         System.out.println("Abort" + integrated);
         abort = true;
         try {
         System.err.println("Not terminated");
         new VisualisationWindow(sim, new Dimension(1800,1000));   return;
         //break loop;
         //SwarmVisualisation.drawSwarmToPDF("Screenshot-Abort" + integrated + "-" + MathUtils.round(MathUtils.randomFloat(0, 10000)) + ".pdf", sim);
         } catch (Throwable e){
         System.err.println("Couldn't draw pdf "+e);
         }
         //System.exit(1);
         }
         runner.loop(100);
         if(sim.getTime()>100000 && times_straightening[SIZE-1]==0 && !printed){
         try {
         System.err.println("Not terminated");
         //new VisualisationWindow(sim, new Dimension(1800,1000));
         //break loop;
        // SwarmVisualisation.drawSwarmToPDF("Screenshot-toolongstraightening" + MathUtils.round(MathUtils.randomFloat(0, 10000)) + ".pdf", sim);
         } catch(Throwable e){
         System.err.println("Couldn't draw pdf"+e);
         }
         printed = true;
         }
         }
         if(integrated==SIZE) SIZE+=40;
         }
         for(int i=19; i<SIZE_MAX; i+=40){
         System.out.println((i+1)+"\t"+times_integration[i]+"\t"+times_straightening[i]+"\t"+times_sorting[i]);
         }
         System.exit(0);
         /**

        integrated = 0;
        Simulator sim = new Simulator(new Configuration("/home/doms/Projects/SwarmRoboticResearch/platypus3000/src/main/java/RobotSorting/simulation.properties"));
        new Robot(Integer.toString(0), new SortingController(0), sim, 0, 0f, 0);

        System.out.println("EXPERIMENT WITH "+SIZE+" ROBOTS");
        abort = false;
        float l = MathUtils.sqrt(75*0.4f*75*0.4f*0.5f)*0.8f;
        for (int i = 1; i < SIZE - 1; i++) {
            new Robot(Integer.toString(i), new SortingController(i), sim, MathUtils.randomFloat(0, (1.6f)*l), MathUtils.randomFloat(0, (1/1.6f)*l), 0);
        }
        new Robot(Integer.toString(SIZE - 1), new SortingController(SIZE), sim, (1.6f)*l, 0, 0);
        VisualisationWindow w = new VisualisationWindow(sim, new Dimension(1800,1000));
        //w.visualisation.simulationSpeed=40;
         **/

    }


    public static void addResultIntegration(long time){
        times_integration[SIZE-1]=time;
    }
    public static void addResultStraightening(long time){
        times_straightening[SIZE-1]=time;
    }
    public static void addResultSorting(long time){
        times_sorting[SIZE-1]=time;
    }

    Vec2 getCollisionAvoidanceVector(RobotInterface r, LocalNeighborhood neighborhood){
        Vec2 v = new Vec2();
        for(NeighborView n: neighborhood){
            if(n.getLocalPosition().lengthSquared() < (2*getConfiguration().getRobotRadius()+0.01f)*(2*getConfiguration().getRobotRadius()+0.01f)){
                float x = 1f/n.getLocalPosition().lengthSquared();
                if(n.getID()< r.getID()) x*=5;
                v.addLocal(n.getLocalPosition().mul(-x));
            }
        }
        return v;
    }



    private class OfferMsg implements MessagePayload {
        int left;
        int right;

        public OfferMsg(int left, int right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public MessagePayload deepCopy() {
            OfferMsg cloned = new OfferMsg(left, right);
            return cloned;
        }
    }

    private class AccMsg implements MessagePayload {
        boolean left;
        public AccMsg(boolean left) {
            this.left = left;

        }

        @Override
        public MessagePayload deepCopy() {
            return new AccMsg(left);
        }
    }

    private class RejMessage implements MessagePayload {

        @Override
        public MessagePayload deepCopy() {
            return new RejMessage();
        }
    }

    private class PathMessage implements MessagePayload {

        @Override
        public MessagePayload deepCopy() {
            return new PathMessage();
        }
    }
    public void applyCollisionAvoidance(LocalNeighborhood ln, Vec2 dir) {
        ArrayList<NeighborView> possibleCollisions = new ArrayList<NeighborView>();
        for(NeighborView n : ln) {
            if(Math.abs(AngleUtils.normalizeToMinusPi_Pi(AngleUtils.getClockwiseRadian(dir, n.getLocalPosition()))) < Math.PI/2 &&
                    getPointLineVector(n, dir).length() < getConfiguration().getRobotRadius()*2 &&
                    n.getLocalPosition().lengthSquared() <= dir.lengthSquared())
                possibleCollisions.add(n);
        }

        if(possibleCollisions.size() > 0) {
            NeighborView closestCollision = Collections.min(possibleCollisions, NeighborView.distanceComparator);
            Vec2 avoidVector = getPointLineVector(closestCollision, dir);
            avoidVector.normalize();
            avoidVector.mulLocal(0.3f);
//            avoidVector.negateLocal();
            //d.drawArrow(new Vec2TOPVector(closestCollision.getLocalPosition()), new Vec2TOPVector(closestCollision.getLocalPosition().add(avoidVector)));
            dir.set(closestCollision.getLocalPosition().add(avoidVector));
        }
    }

    public static Vec2 getPointLineVector(NeighborView n, Vec2 dir) {
        Vec2 v = new Vec2(dir.y, -dir.x); //perpendicular to dir
        Vec2 r = n.getLocalPosition().negate(); //vector from n to origin

        //we project r onto v
        v.normalize();
        float dLenght = Vec2.dot(r, v);
        Vec2 d = v.mul(dLenght);
        return d;
    }

}







class PublicSortingState extends PublicState {
    int number;


    //State 1
    public Boolean path = null;
    Integer left = null;
    Integer right = null;

    //State 2
    boolean left_done = false;
    boolean right_done = false;

    Vec2 rightIntegrationPos;
    Vec2 leftIntegrationPos;
    Float path_hops;

    //State 4
    boolean left_straight = false;
    boolean right_straight = false;

    float avgDistLeft;
    int avgDistLeft_i = 0;
    float avgDistRight;
    int avgDistRight_i = 0;


    PublicSortingState(int number) {
        this.number = number;
    }

    @Override
    public PublicState clone() throws CloneNotSupportedException {
        PublicSortingState cloned = new PublicSortingState(number);
        //State 1
        cloned.path = path;
        cloned.left = left;
        cloned.right = right;
        //State 2
        cloned.left_done = left_done;
        cloned.right_done = right_done;
        //State 4
        cloned.left_straight = left_straight;
        cloned.right_straight = right_straight;

        cloned.avgDistLeft = avgDistLeft;
        cloned.avgDistLeft_i = avgDistLeft_i;
        cloned.avgDistRight = avgDistRight;
        cloned.avgDistRight_i = avgDistRight_i;

        cloned.rightIntegrationPos = rightIntegrationPos;
        cloned.leftIntegrationPos = leftIntegrationPos;
        cloned.path_hops = path_hops;

        return cloned;
    }


}
