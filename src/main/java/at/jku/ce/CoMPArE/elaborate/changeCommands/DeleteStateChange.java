package at.jku.ce.CoMPArE.elaborate.changeCommands;

import at.jku.ce.CoMPArE.process.State;

/**
 * Created by oppl on 15/12/2016.
 */
public class DeleteStateChange extends ProcessChange {

    private State toBeRemovedState;


    public DeleteStateChange() {
        toBeRemovedState = null;
    }

    public DeleteStateChange(State toBeRemovedState) {
        this();
        this.toBeRemovedState = toBeRemovedState;
    }

    @Override
    public boolean perform() {
        return false;
    }

    @Override
    public boolean undo() {
        return false;
    }

}
