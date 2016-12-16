package at.jku.ce.CoMPArE.elaborate.wizardsteps;

import at.jku.ce.CoMPArE.execute.Instance;
import at.jku.ce.CoMPArE.process.Subject;
import com.vaadin.ui.Component;
import org.vaadin.teemu.wizards.Wizard;

/**
 * Created by oppl on 16/12/2016.
 */
public class CantBeDoneStep extends ElaborationStep {

    public CantBeDoneStep(Wizard owner, Subject s, Instance i) {
        super(owner, s, i);
    }
}
