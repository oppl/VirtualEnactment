package at.jku.ce.CoMPArE.elaborate;

import at.jku.ce.CoMPArE.elaborate.changeCommands.ProcessChange;
import at.jku.ce.CoMPArE.process.Process;

import java.util.Vector;

/**
 * Created by oppl on 15/12/2016.
 */
public class ProcessChangeHistory {

    private Vector<ProcessChange> changes;

    public ProcessChangeHistory() {
        changes = new Vector<>();
    }

    public void add(ProcessChange processChange) {
        changes.add(processChange);
    }

    public boolean undoLatestChangeSequence(Process p) {
        ProcessChange pc = changes.lastElement();
        do {
            boolean success = pc.undo();
            if (!success) return false;
            changes.remove(pc);
            pc = changes.lastElement();
        }
        while (!pc.isChangeStepCompleted());

        return true;
    }

    public void setLatestStepAsLastInSequence() {
        changes.lastElement().setChangeStepCompleted(true);
    }

    public Vector<ProcessChange> getHistory() {
        return changes;
    }
}
