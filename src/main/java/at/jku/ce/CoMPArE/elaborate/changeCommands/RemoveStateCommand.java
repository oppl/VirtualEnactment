package at.jku.ce.CoMPArE.elaborate.changeCommands;

import at.jku.ce.CoMPArE.process.*;

import java.util.Map;
import java.util.Set;

/**
 * Created by oppl on 17/12/2016.
 */
public class RemoveStateCommand extends ProcessChangeCommand {

    State state;
    Subject subject;

    State replacementState;

    public RemoveStateCommand(Subject subject, State state) {
        super();
        this.state = state;
        this.subject = subject;
        replacementState = null;
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
        if (predecessorStates.isEmpty()) {
            if (nextStates.size() == 1) {
                subject.setFirstState(nextStates.keySet().iterator().next());
            } else {
                if (nextStates.size() > 1)
                    state.setName("Make decision");
            }
        } else {

            for (State pre : predecessorStates) {
                for (State s : nextStates.keySet())
                    pre.addNextState(s, nextStates.get(s));
                pre.removeNextState(state);
            }
        }

        if (nextStates.size()==1) newActiveState = nextStates.keySet().iterator().next();
        else newActiveState = predecessorStates.iterator().next();

        replacementState = newActiveState;

        return true;
    }

    @Override
    public boolean undo() {
        Set<State> predecessorStates = subject.getPredecessorStates(replacementState);
        if (state instanceof SendState) subject.removeExpectedMessage(((SendState) state).getSentMessage());
        if (state instanceof RecvState) {
            for (Message m : ((RecvState) state).getRecvdMessages())
                subject.removeProvidedMessage(m);
        }
        if (predecessorStates.isEmpty()) {
            subject.setFirstState(state);
        } else {

            for (State pre : predecessorStates) {
                Condition c = pre.getNextStates().get(replacementState);
                pre.removeNextState(replacementState);
                pre.addNextState(state,c);
            }
        }

        newActiveState = state;

        return true;
    }

}
