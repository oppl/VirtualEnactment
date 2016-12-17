package at.jku.ce.CoMPArE.elaborate.changeCommands;

import at.jku.ce.CoMPArE.process.*;
import at.jku.ce.CoMPArE.scaffolding.scaffolds.Scaffold;

import java.util.Map;
import java.util.Set;

/**
 * Created by oppl on 17/12/2016.
 */
public class RemoveStateCommand extends ProcessChangeCommand {

    State state;
    Subject subject;

    public RemoveStateCommand(Subject subject, State state) {
        super();
        this.state = state;
        this.subject = subject;
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
        return true;
    }

    @Override
    public boolean undo() {
        return false;
    }

}
