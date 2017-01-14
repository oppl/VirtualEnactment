package at.jku.ce.CoMPArE.process;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by oppl on 22/11/2016.
 */
public class State extends ProcessElement {

    private String name;
    private Map<State, Condition> nextStates;

    public State(String name) {
        super();
        this.name = name;
        this.nextStates = new HashMap<State, Condition>();
    }

    public State(State s, Subject container) {
        super(s);
        this.name = name;
        this.nextStates = new HashMap<State, Condition>();
        for (State next: s.getNextStates().keySet()) {
            State clonedNextState = null;
            for (State clonedState: container.getStates()) {
                if (clonedState.equals(next)) {
                    clonedNextState = clonedState;
                    break;
                }
            }
            if (clonedNextState == null) {
                if (s instanceof ActionState) clonedNextState = new ActionState((ActionState) s, container);
                if (s instanceof SendState) clonedNextState = new SendState((SendState) s, container);
                if (s instanceof RecvState) clonedNextState = new RecvState((RecvState) s, container);
            }
            Condition clonedCondition = null;
            Condition originalCondition = s.getNextStates().get(next);
            if (originalCondition instanceof MessageCondition) {
                clonedCondition = new MessageCondition((MessageCondition) originalCondition);
            }
            else {
                clonedCondition = new Condition(originalCondition);
            }
            nextStates.put(clonedNextState, clonedCondition);
        }
    }

    public Map<State,Condition> getNextStates() {
        return nextStates;
    }

    public State addNextState(State nextState) {
        this.nextStates.put(nextState, new Condition(""));
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
