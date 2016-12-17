package at.jku.ce.CoMPArE.elaborate.changeCommands;

import at.jku.ce.CoMPArE.process.*;

import java.util.Map;
import java.util.Set;

/**
 * Created by oppl on 15/12/2016.
 */
public class ReplaceStateCommand extends ProcessChangeCommand {

    private State toBeReplacedState;
    private State newState;
    private Subject subject;
    private boolean before;

    public ReplaceStateCommand() {
        toBeReplacedState = null;
        newState = null;
    }

    public ReplaceStateCommand(Subject s, State toBeReplacedState, State newState) {
        this();
        this.subject = s;
        this.toBeReplacedState = toBeReplacedState;
        this.newState = newState;
    }

    @Override
    public boolean perform() {
        Set<State> predecessorStates = subject.getPredecessorStates(toBeReplacedState);
        Map<State, Condition> nextStates = toBeReplacedState.getNextStates();
        if (toBeReplacedState instanceof SendState) subject.addExpectedMessage(((SendState) toBeReplacedState).getSentMessage());
        if (toBeReplacedState instanceof RecvState) {
            for (Message m : ((RecvState) toBeReplacedState).getRecvdMessages())
                subject.addProvidedMessage(m);
        }
        for (State s : nextStates.keySet()) {
            newState.addNextState(s, nextStates.get(s));
        }

        if (predecessorStates.isEmpty()) {
            subject.setFirstState(newState);

        } else {
            for (State pre : predecessorStates) {
                pre.addNextState(newState, nextStates.get(pre.getNextStates().get(toBeReplacedState)));
                pre.removeNextState(toBeReplacedState);
            }
        }

        return true;
    }

    @Override
    public boolean undo() {
        return false;
    }

}
