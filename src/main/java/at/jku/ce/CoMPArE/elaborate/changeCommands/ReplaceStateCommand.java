package at.jku.ce.CoMPArE.elaborate.changeCommands;

import at.jku.ce.CoMPArE.process.*;

import java.util.Map;
import java.util.Set;

/**
 * Created by oppl on 15/12/2016.
 */
public class ReplaceStateCommand extends ProcessChangeCommand {

    private State state;
    private State newState;
    private Subject subject;
    private boolean before;


    public ReplaceStateCommand(Subject s, State state, State newState) {
        super();
        this.subject = s;
        this.state = state;
        this.newState = newState;
    }

    @Override
    public boolean perform() {
        Set<State> predecessorStates = subject.getPredecessorStates(state);
        Map<State, Condition> nextStates = state.getNextStates();
        if (state instanceof SendState) subject.addExpectedMessage(((SendState) state).getSentMessage());
        if (state instanceof RecvState) {
            for (Message m : ((RecvState) state).getRecvdMessages())
                subject.addProvidedMessage(m);
        }
        for (State s : nextStates.keySet()) {
            newState.addNextState(s, nextStates.get(s));
        }

        if (predecessorStates.isEmpty()) {
            subject.setFirstState(newState);

        } else {
            for (State pre : predecessorStates) {
                pre.addNextState(newState, nextStates.get(pre.getNextStates().get(state)));
                pre.removeNextState(state);
            }
        }

        newActiveState = newState;

        return true;
    }

    @Override
    public boolean undo() {
        Set<State> predecessorStates = subject.getPredecessorStates(state);
        Map<State, Condition> nextStates = state.getNextStates();
        if (state instanceof SendState) subject.removeExpectedMessage(((SendState) state).getSentMessage());
        if (state instanceof RecvState) {
            for (Message m : ((RecvState) state).getRecvdMessages())
                subject.removeProvidedMessage(m);
        }

        if (predecessorStates.isEmpty()) {
            subject.setFirstState(state);

        } else {
            for (State pre : predecessorStates) {
                pre.addNextState(state, nextStates.get(pre.getNextStates().get(state)));
                pre.removeNextState(newState);
            }
        }

        newActiveState = state;

        return true;
    }

}
