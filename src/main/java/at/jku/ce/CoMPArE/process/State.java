package at.jku.ce.CoMPArE.process;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by oppl on 22/11/2016.
 */
public class State {

    private String name;
    private Map<State, Condition> nextStates;

    public State(String name) {
        this.name = name;
        this.nextStates = new HashMap<State, Condition>();
    }

    public Map<State,Condition> getNextStates() {
        return nextStates;
    }

    public State addNextState(State nextState) {
        this.nextStates.put(nextState, Condition.noCondition);
        return nextState;
    }

    public State addNextState(State nextState, Condition condition) {
        this.nextStates.put(nextState, condition);
        return nextState;
    }

    public void alterConditionForNextState(State nextState, Condition condition) {
        nextStates.replace(nextState, condition);
    }

    public void removeNextState(State stateToBeRemoved) {
        nextStates.remove(stateToBeRemoved);
    }
    public void removeAllNextStates() {nextStates.clear();}

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
