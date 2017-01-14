package at.jku.ce.CoMPArE.elaborate.changeCommands;

import at.jku.ce.CoMPArE.process.ActionState;
import at.jku.ce.CoMPArE.process.Condition;
import at.jku.ce.CoMPArE.process.State;
import at.jku.ce.CoMPArE.process.Subject;

import java.util.Map;
import java.util.Set;

/**
 * Created by oppl on 17/12/2016.
 */
public class AddConditionalStateCommand extends ProcessChangeCommand {

    private State state;
    private State newState;
    private Subject subject;
    private Map<State, Condition> originalConditions;
    private Map<State, Condition> newConditions;

    public AddConditionalStateCommand(Subject s, State target, State newState, Map<State, Condition> originalConditions, Map<State, Condition> newConditions) {
        super();
        this.state = target;
        this.newState = newState;
        this.subject = s;
        this.originalConditions = originalConditions;
        this.newConditions = newConditions;
    }

    @Override
    public boolean perform() {
        if (state == subject.getFirstState()) {
            State decisionState = new ActionState("Make decision", subject);
            subject.addState(decisionState);
            decisionState.addNextState(state, originalConditions.values().iterator().next());
            decisionState.addNextState(newState, newConditions.values().iterator().next());
            subject.setFirstState(decisionState);
        }

        Set<State> predecessorStates = subject.getPredecessorStates(state);

        if (!predecessorStates.isEmpty()) {
            for (State predecessorState : predecessorStates) {
                predecessorState.getNextStates().remove(state.getUUID());
                predecessorState.addNextState(state, originalConditions.get(predecessorState));
                predecessorState.addNextState(newState, newConditions.get(predecessorState));
            }
        }
        newActiveState = newState;
        return true;
    }

    @Override
    public boolean undo() {
        return false;
    }
}
