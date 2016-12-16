package at.jku.ce.CoMPArE.elaborate.wizardsteps;

import at.jku.ce.CoMPArE.elaborate.changeCommands.ProcessChange;
import at.jku.ce.CoMPArE.execute.Instance;
import at.jku.ce.CoMPArE.process.Subject;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import org.vaadin.teemu.wizards.Wizard;
import org.vaadin.teemu.wizards.WizardStep;

/**
 * Created by oppl on 16/12/2016.
 */
public class ElaborationStep implements WizardStep {

    protected Wizard owner;

    protected Instance instance;
    protected Subject subject;
    protected String caption;

    protected boolean canAdvance;
    protected boolean canGoBack;

    protected ElaborationStep nextStep;

    protected ProcessChange processChange;

    protected VerticalLayout fLayout;

    public ElaborationStep(Wizard owner, Subject s, Instance i) {
        subject = s;
        instance = i;
        this.owner = owner;
        canAdvance = false;
        canGoBack = true;
        nextStep = null;
        processChange = null;
        fLayout = new VerticalLayout();
        fLayout.setMargin(true);
        fLayout.setSpacing(true);
        caption = new String("");
    }


    @Override
    public final String getCaption() {
        return caption;
    }

    @Override
    public final Component getContent() {
        return fLayout;
    }

    @Override
    public final boolean onAdvance() {
        return canAdvance;
    }

    @Override
    public final boolean onBack() {
        return canGoBack;
    }

    public ProcessChange getProcessChange() {
        return processChange;
    }
}
