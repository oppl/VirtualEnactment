package at.jku.ce.CoMPArE.elaborate;

import at.jku.ce.CoMPArE.elaborate.changeCommands.ProcessChangeCommand;
import at.jku.ce.CoMPArE.process.Process;

import java.util.Vector;

/**
 * Created by oppl on 15/12/2016.
 */
public class ProcessChangeHistory {

    private Vector<ProcessChangeCommand> changes;

    public ProcessChangeHistory() {
        changes = new Vector<>();
    }

    public void add(ProcessChangeCommand processChange) {
        changes.add(processChange);
    }

    public boolean undoLatestChangeSequence(Process p) {
        ProcessChangeCommand pc = changes.lastElement();
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
        if (changes.isEmpty()) return;
        changes.lastElement().setChangeStepCompleted(true);
    }

    public Vector<ProcessChangeCommand> getHistory() {
        return changes;
    }
}
