package at.jku.ce.CoMPArE.elaborate.wizardsteps;

import at.jku.ce.CoMPArE.elaborate.changeCommands.ProcessChange;
import at.jku.ce.CoMPArE.execute.Instance;
import at.jku.ce.CoMPArE.process.Subject;
import org.vaadin.teemu.wizards.Wizard;

import java.util.List;

/**
 * Created by oppl on 17/12/2016.
 */
public class AskForConditionsStep extends ElaborationStep {

    public AskForConditionsStep(Wizard owner, String newState, Subject s, Instance i) {
        super(owner, s, i);
    }

    @Override
    public List<ProcessChange> getProcessChanges() {
        return super.getProcessChanges();
    }
}
