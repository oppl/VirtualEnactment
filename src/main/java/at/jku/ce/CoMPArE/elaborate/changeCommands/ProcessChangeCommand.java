package at.jku.ce.CoMPArE.elaborate.changeCommands;

import at.jku.ce.CoMPArE.process.State;

/**
 * Created by oppl on 15/12/2016.
 */
public abstract class ProcessChangeCommand {

    private boolean changeStepCompleted;
    protected State newActiveState;

    public ProcessChangeCommand() {
        changeStepCompleted = true;
        newActiveState = null;
    }

    public abstract boolean perform();

    public abstract boolean undo();

    public State getNewActiveState() { return newActiveState; }

    public boolean isChangeStepCompleted() {
        return changeStepCompleted;
    }

    public void setChangeStepCompleted(boolean changeStepCompleted) {
        this.changeStepCompleted = changeStepCompleted;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
