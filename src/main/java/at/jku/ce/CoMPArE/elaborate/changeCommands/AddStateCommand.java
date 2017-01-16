package at.jku.ce.CoMPArE.elaborate.changeCommands;

import at.jku.ce.CoMPArE.LogHelper;
import at.jku.ce.CoMPArE.process.*;

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
        s.addState(newState);
        if (newState instanceof RecvState) {
            s.getParentProcess().addMessages(((RecvState) newState).getRecvdMessages());
        }
        if (newState instanceof SendState) {
            s.getParentProcess().addMessage(((SendState) newState).getSentMessage());
        }
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
        newActiveState = target;
        s.removeState(newState);
        if (newState instanceof RecvState) {
            for (Message m:((RecvState) newState).getRecvdMessages()) {
                s.getParentProcess().removeMessage(m);
            }
        }
        if (newState instanceof SendState) {
            s.getParentProcess().removeMessage(((SendState) newState).getSentMessage());
        }
        if (before) {
            if (newState == s.getFirstState()) {
                s.setFirstState(target);
                return true;
            }

            Set<State> predecessorStates = s.getPredecessorStates(newState);

            if (!predecessorStates.isEmpty()) {
                for (State predecessorState : predecessorStates) {
                    Condition c = predecessorState.getNextStates().get(newState);
                    predecessorState.getNextStates().remove(newState);
                    predecessorState.getNextStates().put(target, c);
                }
                return true;
            } else return false;
        } else {
            for (State nextState : newState.getNextStates().keySet()) {
                target.addNextState(nextState, newState.getNextStates().get(nextState));
            }
            return true;
        }
    }
}

