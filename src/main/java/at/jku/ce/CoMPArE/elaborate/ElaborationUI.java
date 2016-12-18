package at.jku.ce.CoMPArE.elaborate;

import at.jku.ce.CoMPArE.LogHelper;
import at.jku.ce.CoMPArE.elaborate.changeCommands.ProcessChangeCommand;
import at.jku.ce.CoMPArE.elaborate.wizardsteps.*;
import at.jku.ce.CoMPArE.execute.Instance;
import at.jku.ce.CoMPArE.process.*;
import com.vaadin.ui.*;
import org.vaadin.teemu.wizards.Wizard;
import org.vaadin.teemu.wizards.WizardStep;
import org.vaadin.teemu.wizards.event.*;
import sun.rmi.runtime.Log;

import java.util.List;

/**
 * Created by oppl on 23/11/2016.
 */
public class ElaborationUI extends Window implements WizardProgressListener {

    public static int ELABORATE = 1;
    public static int INITIALSTEP = 2;
    public static int ADDITIONALSTEP = 3;
    public static int INITALSUBJECT = 4;

    FormLayout fLayout = new FormLayout();
    Wizard wizard = new Wizard();
    ProcessChangeHistory processChangeHistory;

    private Subject subject;
    private Instance instance;

    public ElaborationUI(ProcessChangeHistory processChangeHistory) {
        super("Elaborate on this problem");
        this.setWidth("100%");
        this.setHeight("500px");
        this.center();
        this.processChangeHistory = processChangeHistory;
        if (this.processChangeHistory == null) this.processChangeHistory = new ProcessChangeHistory();
        fLayout.setMargin(true);
        fLayout.setSpacing(true);
        setContent(fLayout);
        fLayout.addComponent(wizard);
        wizard.addListener(this);
        subject = null;
        instance = null;
    }

    public void elaborate(Subject subject, Instance instance) {
        this.subject = subject;
        this.instance = instance;
        wizard.addStep(new AskForReasonStep(wizard, subject, instance));
    }
    public void initialStep(Subject subject, Instance instance) {
        this.subject = subject;
        this.instance = instance;
        this.setCaption("Add an initial step");
        wizard.addStep(new AddInitialStepStep(wizard, subject, instance));
    }

    public void initialSubject(Instance instance) {
        this.setCaption("Add an intial actor");
        this.instance = instance;
        wizard.addStep(new AddInitialSubjectStep(wizard, instance));
    }

    public void additionalStep(Subject subject, Instance instance) {
        this.subject = subject;
        this.instance = instance;
        this.setCaption("Add an additional step");
        wizard.addStep(new AddAdditionalStepStep(wizard, subject, instance));
    }

    @Override
    public void activeStepChanged(WizardStepActivationEvent wizardStepActivationEvent) {

    }

    @Override
    public void stepSetChanged(WizardStepSetChangedEvent wizardStepSetChangedEvent) {

    }

    @Override
    public void wizardCompleted(WizardCompletedEvent wizardCompletedEvent) {
        LogHelper.logInfo("Wizard completed, now performing changes");
        List<WizardStep> steps = wizard.getSteps();
        State finalActiveState = null;
        for (WizardStep ws: steps) {
            List<ProcessChangeCommand> changes = ((ElaborationStep) ws).getProcessChanges();
            if (!changes.isEmpty()) {
                for (ProcessChangeCommand pc: changes) {
                    LogHelper.logInfo("Wizard: Executing "+pc);
                    pc.perform();
                    processChangeHistory.add(pc);
                    State newActiveState = pc.getNewActiveState();
                    if (subject != null && newActiveState != null) {
                        instance.getAvailableStates().put(subject,newActiveState);
                        if (finalActiveState == null) finalActiveState = newActiveState;
                        LogHelper.logInfo("Wizard: Setting active state in "+subject+" to "+newActiveState);
                    }
                }
            }
        }
        if (!steps.isEmpty()) processChangeHistory.setLatestStepAsLastInSequence();
        if (finalActiveState != null) instance.getAvailableStates().put(subject,finalActiveState);
        this.close();
    }

    @Override
    public void wizardCancelled(WizardCancelledEvent wizardCancelledEvent) {
        this.close();
    }

}