package at.jku.ce.CoMPArE.process;

import at.jku.ce.CoMPArE.process.Condition;
import at.jku.ce.CoMPArE.process.State;

import java.util.UUID;

/**
 * Created by oppl on 10/02/2017.
 */
public class Transition extends ProcessElement {
    private UUID source;
    private UUID dest;
    private Condition condition;

    public Transition(State source, State dest) {
        super();
        this.source = source.getUUID();
        this.dest = dest.getUUID();
        this.condition = null;
    }

    public Transition(State source, State dest, Condition condition) {
        super();
        this.source = source.getUUID();
        this.dest = dest.getUUID();
        this.condition = condition;
    }

    public Transition(Transition transition) {
        super(transition);
        this.source = transition.getSource();
        this.dest = transition.getDest();
        if (transition.getCondition() instanceof MessageCondition)
            this.condition = new MessageCondition(((MessageCondition) transition.getCondition()).getMessage());
        else if (transition.getCondition() == null) this.condition = null;
        else this.condition = new Condition(transition.getCondition());
    }

    public UUID getSource() {
        return source;
    }

    public UUID getDest() {
        return dest;
    }

    public Condition getCondition() {
        return condition;
    }
}
