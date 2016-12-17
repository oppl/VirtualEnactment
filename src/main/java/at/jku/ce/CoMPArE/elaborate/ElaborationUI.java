package at.jku.ce.CoMPArE.elaborate;

import at.jku.ce.CoMPArE.LogHelper;
import at.jku.ce.CoMPArE.elaborate.changeCommands.AddStateCommand;
import at.jku.ce.CoMPArE.elaborate.changeCommands.ProcessChangeCommand;
import at.jku.ce.CoMPArE.elaborate.changeCommands.RemoveExpectedMessageCommand;
import at.jku.ce.CoMPArE.elaborate.wizardsteps.*;
import at.jku.ce.CoMPArE.execute.Instance;
import at.jku.ce.CoMPArE.process.*;
import at.jku.ce.CoMPArE.process.Process;
import com.vaadin.ui.*;
import org.vaadin.teemu.wizards.Wizard;
import org.vaadin.teemu.wizards.WizardStep;
import org.vaadin.teemu.wizards.event.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
    }

    public void elaborate(Subject subject, Instance instance) {
        wizard.addStep(new AskForReasonStep(wizard, subject, instance));
    }
    public void initialStep(Subject subject, Instance instance) {
        this.setCaption("Add an initial step");
        wizard.addStep(new AddInitialStepStep(wizard, subject, instance));
    }

    public void initialSubject(Instance instance) {
        this.setCaption("Add an intial actor");
        wizard.addStep(new AddInitialSubjectStep(wizard, instance));
    }

    public void additionalStep(Subject subject, Instance instance) {
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
        for (WizardStep ws: steps) {
            List<ProcessChangeCommand> changes = ((ElaborationStep) ws).getProcessChanges();
            if (!changes.isEmpty()) {
                for (ProcessChangeCommand pc: changes) {
                    pc.perform();
                    processChangeHistory.add(pc);
                }
            }
        }
        if (!steps.isEmpty()) processChangeHistory.setLatestStepAsLastInSequence();
        //TODO: switch instance to state where it should continue
        this.close();
    }

    @Override
    public void wizardCancelled(WizardCancelledEvent wizardCancelledEvent) {
        this.close();
    }

    private void insertNewStateAsideCurrentState(State newState, Map<State, Condition> newCs, Map<State, Condition> oldCs, Subject s, Instance instance) {
        State currentState = instance.getAvailableStateForSubject(s);
        if (currentState == s.getFirstState()) {
            State decisionState = new ActionState("Make decision");
            s.setFirstState(decisionState);
            decisionState.addNextState(currentState, oldCs.values().iterator().next());
            decisionState.addNextState(newState, newCs.values().iterator().next());
            instance.getAvailableStates().replace(s, newState);
            return;
        }

        Set<State> predecessorStates = s.getPredecessorStates(currentState);

        if (!predecessorStates.isEmpty()) {
            for (State predecessorState : predecessorStates) {
                predecessorState.getNextStates().remove(currentState);
                predecessorState.addNextState(currentState, oldCs.get(predecessorState));
                predecessorState.addNextState(newState, newCs.get(predecessorState));
                instance.getAvailableStates().replace(s, newState);
            }
        }

    }

}