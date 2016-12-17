package at.jku.ce.CoMPArE.elaborate;

import at.jku.ce.CoMPArE.LogHelper;
import at.jku.ce.CoMPArE.elaborate.changeCommands.AddStateCommand;
import at.jku.ce.CoMPArE.elaborate.changeCommands.ProcessChangeCommand;
import at.jku.ce.CoMPArE.elaborate.changeCommands.RemoveExpectedMessageCommand;
import at.jku.ce.CoMPArE.elaborate.wizardsteps.*;
import at.jku.ce.CoMPArE.execute.Instance;
import at.jku.ce.CoMPArE.process.*;
import at.jku.ce.CoMPArE.process.Process;
import at.jku.ce.CoMPArE.visualize.VisualizationUI;
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

    private void deleteState(State state, Subject subject, Instance instance) {
        Set<State> predecessorStates = subject.getPredecessorStates(state);
        Map<State, Condition> nextStates = state.getNextStates();
        if (state instanceof SendState) subject.addExpectedMessage(((SendState) state).getSentMessage());
        if (state instanceof RecvState) {
            for (Message m : ((RecvState) state).getRecvdMessages())
                subject.addProvidedMessage(m);
        }
        if (predecessorStates.isEmpty()) {
            if (nextStates.size() == 1) {
                subject.setFirstState(nextStates.keySet().iterator().next());
                instance.getAvailableStates().replace(subject, subject.getFirstState());
            } else {
                if (nextStates.size() > 1)
                    state.setName("Make decision");
            }
        } else {

            for (State pre : predecessorStates) {
                for (State s : nextStates.keySet())
                    pre.addNextState(s, nextStates.get(s));
                pre.removeNextState(state);
            }
            if (nextStates.size() == 1)
                instance.getAvailableStates().replace(subject, nextStates.keySet().iterator().next());
            else instance.getAvailableStates().replace(subject, predecessorStates.iterator().next());
        }
    }

    private void replaceState(State oldState, State newState, Subject subject, Instance instance) {
        Set<State> predecessorStates = subject.getPredecessorStates(oldState);
        Map<State, Condition> nextStates = oldState.getNextStates();
        if (oldState instanceof SendState) subject.addExpectedMessage(((SendState) oldState).getSentMessage());
        if (oldState instanceof RecvState) {
            for (Message m : ((RecvState) oldState).getRecvdMessages())
                subject.addProvidedMessage(m);
        }
        for (State s : nextStates.keySet()) {
            newState.addNextState(s, nextStates.get(s));
        }

        if (predecessorStates.isEmpty()) {
            subject.setFirstState(newState);

        } else {
            for (State pre : predecessorStates) {
                pre.addNextState(newState, nextStates.get(pre.getNextStates().get(oldState)));
                pre.removeNextState(oldState);
            }
        }
        instance.getAvailableStates().replace(subject, newState);
    }

    private Subject addAnonymousSubject(Instance instance) {
        Subject anonymous = null;
        for (Subject s : instance.getProcess().getSubjects()) {
            if (s.toString().equals(Subject.ANONYMOUS)) {
                anonymous = s;
            }
        }
        if (anonymous == null) {
            anonymous = new Subject(Subject.ANONYMOUS);
            insertNewSubject(anonymous, instance);
        }
        return anonymous;
    }

    private void insertNewSubject(Subject newSubject, Instance instance) {
        Process p = instance.getProcess();
        p.addSubject(newSubject);
        instance.addInputBufferAndHistoryForSubject(newSubject);
    }

    private State insertNewActionState(String stateName, Subject s, Instance instance, boolean insertBefore) {
        State newState = new ActionState(stateName);
        insertNewState(newState, s, instance, insertBefore, true);
        return newState;
    }

    private State insertNewReceiveState(String messageName, Subject provider, Subject subject, Instance instance, boolean insertBefore) {
        RecvState newState = new RecvState("Wait for " + messageName);
        Message newMessage = new Message(messageName);
        newState.addRecvdMessage(newMessage);
        insertNewState(newState, subject, instance, insertBefore, false);
        provider.addExpectedMessage(newMessage); // TODO replace with command
        return newState;
    }

    private State insertNewReceiveState(Message message, Subject subject, Instance instance, boolean insertBefore) {
        RecvState newState = new RecvState("Wait for " + message);
        newState.addRecvdMessage(message);
        insertNewState(newState, subject, instance, insertBefore, true);
        return newState;
    }

    private State insertNewSendState(String messageName, Subject target, Subject subject, Instance instance, boolean insertBefore) {
        SendState newState = new SendState("Send " + messageName);
        Message newMessage = new Message(messageName);
        newState.setSentMessage(newMessage);
        insertNewState(newState, subject, instance, insertBefore, false);
        target.addProvidedMessage(newMessage); // TODO replace with command

        return newState;
    }

    private State insertNewSendStateAndRemoveExpectedMessage(Message message, Subject subject, Instance instance, boolean insertBefore) {
        SendState newState = new SendState("Send " + message);
        newState.setSentMessage(message);
        insertNewState(newState, subject, instance, insertBefore, false);
        ProcessChangeCommand c = new RemoveExpectedMessageCommand(subject,message);
        boolean successful = c.perform();
        if (successful) {
            c.setChangeStepCompleted(true);
            processChangeHistory.add(c);
        }
        subject.removeExpectedMessage(message);
        return newState;
    }


    private void insertNewState(State newState, Subject s, Instance instance, boolean insertBefore, boolean changesDone) {
        State currentState = instance.getAvailableStateForSubject(s);

        if (!insertBefore && currentState == null) {
            currentState = instance.getHistoryForSubject(s).getFirst();
        }

        ProcessChangeCommand c = new AddStateCommand(s, currentState, newState, insertBefore);
        boolean successful = c.perform();
        if (successful) {
            c.setChangeStepCompleted(changesDone);
            processChangeHistory.add(c);
            instance.getAvailableStates().replace(s, newState);
        }
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