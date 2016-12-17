package at.jku.ce.CoMPArE.elaborate.wizardsteps;

import at.jku.ce.CoMPArE.execute.Instance;
import at.jku.ce.CoMPArE.process.Subject;
import org.vaadin.teemu.wizards.Wizard;

/**
 * Created by oppl on 17/12/2016.
 */
public class AskForNewRecvSubjectStep extends ElaborationStep {
    public AskForNewRecvSubjectStep(Wizard owner, String input, Subject s, Instance i) {
        super(owner, s, i);
    }
}
