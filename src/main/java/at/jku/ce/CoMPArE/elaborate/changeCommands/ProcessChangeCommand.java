package at.jku.ce.CoMPArE.elaborate.changeCommands;

/**
 * Created by oppl on 15/12/2016.
 */
public abstract class ProcessChangeCommand {

    private boolean changeStepCompleted;

    public ProcessChangeCommand() {
        changeStepCompleted = true;
    }

    public abstract boolean perform();

    public abstract boolean undo();

    public boolean isChangeStepCompleted() {
        return changeStepCompleted;
    }

    public void setChangeStepCompleted(boolean changeStepCompleted) {
        this.changeStepCompleted = changeStepCompleted;
    }
}
