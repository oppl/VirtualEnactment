package at.jku.ce.CoMPArE.elaborate.changeCommands;

import at.jku.ce.CoMPArE.process.State;

/**
 * Created by oppl on 15/12/2016.
 */
public class DeleteStateCommand extends ProcessChangeCommand {

    private State toBeRemovedState;


    public DeleteStateCommand() {
        toBeRemovedState = null;
    }

    public DeleteStateCommand(State toBeRemovedState) {
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
