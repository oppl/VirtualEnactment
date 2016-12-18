package at.jku.ce.CoMPArE.elaborate.changeCommands;

import at.jku.ce.CoMPArE.LogHelper;
import at.jku.ce.CoMPArE.process.Condition;
import at.jku.ce.CoMPArE.process.State;
import at.jku.ce.CoMPArE.process.Subject;

import java.util.Set;

/**
 * Created by oppl on 15/12/2016.
 */
public class AddStateCommand extends ProcessChangeCommand {

    private State target;
    private State newState;
    private Subject s;
    private boolean before;

    public AddStateCommand(Subject s, State target, State newState, boolean before) {
        super();
        this.target = target;
        this.newState = newState;
        this.before = before;
        this.s = s;
    }

    @Override
    public boolean perform() {
        newActiveState = newState;
        if (before) {
            if (target == s.getFirstState() || s.getFirstState() == null) {
                LogHelper.logInfo("Elaboration: inserting " + newState + " as new first state in subject " + s);
                s.setFirstState(newState);
                if (target != null) newState.addNextState(target);
                return true;
            }

            Set<State> predecessorStates = s.getPredecessorStates(target);
            LogHelper.logInfo("Elaboration: found " + predecessorStates.size() + " predecessors for inserting " + newState);

            if (!predecessorStates.isEmpty()) {
                for (State predecessorState : predecessorStates) {
                    LogHelper.logInfo("Elaboration: inserting " + newState + " after " + predecessorState);
                    Condition c = predecessorState.getNextStates().get(target);
                    predecessorState.getNextStates().remove(target);
                    predecessorState.getNextStates().put(newState, c);
                }
                newState.addNextState(target);
                return true;
            } else return false;
        } else {
            for (State nextState : target.getNextStates().keySet()) {
                newState.addNextState(nextState, target.getNextStates().get(nextState));
            }
            target.removeAllNextStates();
            target.addNextState(newState);
            return true;
        }
    }

    @Override
    public boolean undo() {
        return false;
    }
}

