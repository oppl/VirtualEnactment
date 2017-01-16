package at.jku.ce.CoMPArE.elaborate;

import at.jku.ce.CoMPArE.elaborate.changeCommands.ProcessChangeCommand;
import at.jku.ce.CoMPArE.process.Process;

import java.util.Vector;

/**
 * Created by oppl on 15/12/2016.
 */
public class ProcessChangeHistory {

    private Vector<ProcessChangeTransaction> changes;

    public ProcessChangeHistory() {
        changes = new Vector<>();
    }

    public void add(ProcessChangeTransaction processChange) {
        changes.add(processChange);
    }

    public boolean undoLatestChangeTransaction(Process p) {
        ProcessChangeTransaction pc = changes.lastElement();
        return pc.undo();
    }


    public Vector<ProcessChangeTransaction> getHistory() {
        return changes;
    }
}
