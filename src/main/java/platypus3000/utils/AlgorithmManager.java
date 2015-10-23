package platypus3000.utils;

import platypus3000.simulation.control.RobotInterface;

import java.util.HashSet;
import java.util.LinkedList;

/**
 * Created by doms on 7/16/14.
 */
public class AlgorithmManager {
    LinkedList<Loopable> executionOrder = new LinkedList<Loopable>();
    HashSet<Loopable> alreadyAdded = new HashSet<Loopable>();

    public void addLoopAlgorithms(Loopable... loopables) {
        for(Loopable l : loopables)
            addLoopAlgorithm(l);
    }

    public void addLoopAlgorithm(Loopable loopable){
         if(alreadyAdded.contains(loopable)) return;
        if(loopable.getDependencies() != null)
            for(Loopable dependency: loopable.getDependencies())
                addLoopAlgorithm(dependency);

        executionOrder.addLast(loopable);
        alreadyAdded.add(loopable);
    }

    public void loop(RobotInterface robot){
         for(Loopable algorithm: executionOrder){
             algorithm.loop(robot);
         }
    }
}
