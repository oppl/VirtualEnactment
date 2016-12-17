package at.jku.ce.CoMPArE.elaborate;

import at.jku.ce.CoMPArE.LogHelper;
import at.jku.ce.CoMPArE.elaborate.changeCommands.AddStateChange;
import at.jku.ce.CoMPArE.elaborate.changeCommands.ProcessChange;
import at.jku.ce.CoMPArE.elaborate.changeCommands.RemoveExpectedMessageChange;
import at.jku.ce.CoMPArE.elaborate.wizardsteps.AskForReasonStep;
import at.jku.ce.CoMPArE.elaborate.wizardsteps.ElaborationStep;
import at.jku.ce.CoMPArE.execute.Instance;
import at.jku.ce.CoMPArE.process.*;
import at.jku.ce.CoMPArE.process.Process;
import at.jku.ce.CoMPArE.visualize.VisualizationUI;
import com.vaadin.ui.*;
import org.vaadin.teemu.wizards.Wizard;
import org.vaadin.teemu.wizards.WizardStep;
import org.vaadin.teemu.wizards.event.*;

import java.util.HashMap;
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
            List<ProcessChange> changes = ((ElaborationStep) ws).getProcessChanges();
            if (!changes.isEmpty()) {
                for (ProcessChange pc: changes) {
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

    public void initialStep(Subject subject, Instance instance) {
        this.setCaption("Add an initial step");
        newInitialStep(subject, instance);
    }

    public void initialSubject(Instance instance) {
        this.setCaption("Add an intial actor");
        newInitialSubject(instance);
    }

    public void additionalStep(Subject subject, Instance instance) {
        this.setCaption("Add an additional step");
        newAdditionalStep(subject, instance);
    }


    private void replaceIncorrectState(Subject subject, Instance instance) {
        State state = instance.getAvailableStateForSubject(subject);

        final Label questionPrompt = new Label("I want to replace \"" + state + "\" with something else.");
        final TextField inputField = new TextField("What is the new activity?");
        final CheckBox newMessage = new CheckBox("This activity leads to results I can provide to others.");
        Button confirm = new Button("Done");
        confirm.setEnabled(false);

        inputField.addValueChangeListener(e -> {
            if (inputField.getValue().equals("")) confirm.setEnabled(false);
            else confirm.setEnabled(true);
        });

        final Button selectFromExisting = new Button("Let me choose from existing steps");
        selectFromExisting.addClickListener(e -> {
            VisualizationUI viz = new VisualizationUI(instance, "viz");
            getUI().addWindow(viz);
            viz.showSubject(subject);
            viz.activateSelectionMode();
            viz.addCloseListener(e1 -> {
                LogHelper.logInfo("Elaboration: now getting selected state from behaviour vizualization ...");
                State selectedState = viz.getSelectedState();
                if (selectedState != null) {
                    LogHelper.logInfo("Elaboration: selected state found");
                    inputField.setValue(selectedState.toString());
                    newMessage.setEnabled(false);
                    newMessage.setDescription("You cannot alter the selected existing step here.");
                    newMessage.setDescription("Existing steps can only be inserted as alternatives to the current step.");
                }
            });
        });

        inputField.addValueChangeListener(e -> {
            if (instance.getProcess().getStateWithName(inputField.getValue()) == null) {
                newMessage.setEnabled(true);
                newMessage.setDescription("");
            }
        });

        newMessage.addValueChangeListener(e -> {
            Boolean value = (Boolean) e.getProperty().getValue();
            if (value == Boolean.TRUE) confirm.setCaption("Next");
            else confirm.setCaption("Done");
        });

        confirm.addClickListener(e -> {
            State newState = new ActionState(inputField.getValue());
            replaceState(state, newState, subject, instance);
            if (newMessage.getValue() == Boolean.FALSE) {
                this.close();
            } else {
                resultsProvidedToOthers(newState, subject, instance);
            }
        });

        fLayout.removeAllComponents();
        fLayout.addComponent(questionPrompt);
        fLayout.addComponent(inputField);
        fLayout.addComponent(selectFromExisting);
        fLayout.addComponent(newMessage);
        fLayout.addComponent(confirm);

    }

    private void newInitialStep(Subject subject, Instance instance) {

        final Label questionPrompt = new Label("I want to set an initial step for  " + subject + ".");
        final TextField inputField = new TextField("What do you want to do?");
        final CheckBox newMessage = new CheckBox("This activity leads to results I can provide to others.");
        Button confirm = new Button("Done");
        confirm.setEnabled(false);

        inputField.addValueChangeListener(e -> {
            if (inputField.getValue().equals("")) confirm.setEnabled(false);
            else confirm.setEnabled(true);
        });

        newMessage.addValueChangeListener(e -> {
            Boolean value = (Boolean) e.getProperty().getValue();
            if (value == Boolean.TRUE) confirm.setCaption("Next");
            else confirm.setCaption("Done");
        });

        final OptionGroup availableProvidedMessages = new OptionGroup("There is input available, on which you might want to react:");
        for (Message m : subject.getProvidedMessages()) {
            availableProvidedMessages.addItem(m);
        }
        final String optionNoInput = new String("I don't want to react on any input.");
        availableProvidedMessages.addItem(optionNoInput);
        availableProvidedMessages.setValue(optionNoInput);

        confirm.addClickListener(e -> {
            LogHelper.logInfo("Elaboration: inserting new initial step " + inputField.getValue() + " into " + subject);
            State newState = insertNewActionState(inputField.getValue(), subject, instance, true);
            if (availableProvidedMessages.getValue() instanceof Message) {
                Message m = (Message) availableProvidedMessages.getValue();
                insertNewReceiveState(m, subject, instance, true);
                subject.removeProvidedMessage(m);
            }
            if (newMessage.getValue() == Boolean.FALSE) {
                this.close();
            } else {
                instance.getAvailableStates().replace(subject, newState);
                resultsProvidedToOthers(newState, subject, instance);
            }
        });

        fLayout.removeAllComponents();
        fLayout.addComponent(questionPrompt);
        fLayout.addComponent(inputField);
        fLayout.addComponent(newMessage);
        if (!subject.getProvidedMessages().isEmpty()) fLayout.addComponent(availableProvidedMessages);
        fLayout.addComponent(confirm);

    }

    private void newAdditionalStep(Subject subject, Instance instance) {

        final Label questionPrompt = new Label("I want to set an additional step for  " + subject + ".");
        final TextField inputField = new TextField("What do you want to do?");
        final CheckBox newMessage = new CheckBox("This activity leads to results I can provide to others.");
        Button confirm = new Button("Done");
        confirm.setEnabled(false);

        inputField.addValueChangeListener(e -> {
            if (inputField.getValue().equals("")) confirm.setEnabled(false);
            else confirm.setEnabled(true);
        });

        final Button selectFromExisting = new Button("Let me choose from existing steps");
        selectFromExisting.addClickListener(e -> {
            VisualizationUI viz = new VisualizationUI(instance, "viz");
            getUI().addWindow(viz);
            viz.showSubject(subject);
            viz.activateSelectionMode();
            viz.addCloseListener(e1 -> {
                LogHelper.logInfo("Elaboration: now getting selected state from behaviour vizualization ...");
                State selectedState = viz.getSelectedState();
                if (selectedState != null) {
                    LogHelper.logInfo("Elaboration: selected state found");
                    inputField.setValue(selectedState.toString());
                    newMessage.setEnabled(false);
                    newMessage.setDescription("You cannot alter the selected existing step here.");
                    newMessage.setDescription("Existing steps can only be inserted as alternatives to the current step.");
                }
            });
        });

        inputField.addValueChangeListener(e -> {
            if (instance.getProcess().getStateWithName(inputField.getValue()) == null) {
                newMessage.setEnabled(true);
                newMessage.setDescription("");
            }
        });

        newMessage.addValueChangeListener(e -> {
            Boolean value = (Boolean) e.getProperty().getValue();
            if (value == Boolean.TRUE) confirm.setCaption("Next");
            else confirm.setCaption("Done");
        });

        confirm.addClickListener(e -> {
            LogHelper.logInfo("Elaboration: inserting new additional step " + inputField.getValue() + " into " + subject);
            State newState = insertNewActionState(inputField.getValue(), subject, instance, false);
            if (newMessage.getValue() == Boolean.FALSE) {
                this.close();
            } else {
                resultsProvidedToOthers(newState, subject, instance);
            }
        });

        fLayout.removeAllComponents();
        fLayout.addComponent(questionPrompt);
        fLayout.addComponent(inputField);
        fLayout.addComponent(selectFromExisting);
        fLayout.addComponent(newMessage);
        fLayout.addComponent(confirm);

    }

    private void somethingElseFirst(Subject subject, Instance instance) {
        State state = instance.getAvailableStateForSubject(subject);

        final Label questionPrompt = new Label("I need to do something else before I do \"" + state + "\".");
        final TextField inputField = new TextField("What do you need to do?");
        final CheckBox newMessage = new CheckBox("This activity leads to results I can provide to others.");
        Button confirm = new Button("Done");
        confirm.setEnabled(false);

        inputField.addValueChangeListener(e -> {
            if (inputField.getValue().equals("")) confirm.setEnabled(false);
            else confirm.setEnabled(true);
        });

        newMessage.addValueChangeListener(e -> {
            Boolean value = (Boolean) e.getProperty().getValue();
            if (value == Boolean.TRUE) confirm.setCaption("Next");
            else confirm.setCaption("Done");
        });

        OptionGroup availableProvidedMessages = new OptionGroup("Do you want to react on any of the following available input in this step?");
        for (Message m : subject.getProvidedMessages()) {
            availableProvidedMessages.addItem(m);
        }
        final String optionNo = new String("No");
        availableProvidedMessages.addItem(optionNo);
        availableProvidedMessages.setValue(optionNo);

        confirm.addClickListener(e -> {
            LogHelper.logInfo("Elaboration: inserting " + inputField.getValue() + " into " + subject);
            State newState = insertNewActionState(inputField.getValue(), subject, instance, true);
            Object selectedItem = availableProvidedMessages.getValue();
            if (selectedItem != optionNo) {
                State predecessor = null;
                if (subject.getPredecessorStates(newState).size() == 1) {
                    predecessor = subject.getPredecessorStates(newState).iterator().next();
                }
                if (predecessor != null && predecessor instanceof RecvState) {
                    deleteState(newState, subject, instance);
                    instance.getAvailableStates().replace(subject, predecessor);
                    instance.addMessageToInputBuffer(subject, ((RecvState) predecessor).getRecvdMessages().iterator().next());
                    newState = insertNewActionState(inputField.getValue(), subject, instance, true);
                }
                insertNewReceiveState((Message) selectedItem, subject, instance, true);
                subject.removeProvidedMessage((Message) selectedItem);
                instance.getAvailableStates().replace(subject, newState);
            }
            if (newMessage.getValue() == Boolean.FALSE) {
                this.close();
            } else {
                resultsProvidedToOthers(newState, subject, instance);
            }
        });

        fLayout.removeAllComponents();
        fLayout.addComponent(questionPrompt);
        fLayout.addComponent(inputField);
        fLayout.addComponent(newMessage);
        if (!subject.getProvidedMessages().isEmpty()) fLayout.addComponent(availableProvidedMessages);
        fLayout.addComponent(confirm);
    }

    private void resultsProvidedToOthers(State newState, Subject subject, Instance instance) {
        State state = instance.getAvailableStateForSubject(subject);
        final Label questionPrompt = new Label("\"" + newState + "\" leads to results I can provide to others.");
        final TextField inputField = new TextField("What can you provide to others?");
        final OptionGroup infoTarget = new OptionGroup("Whom do you provide it to?");
        Button confirm = new Button("Done");

        confirm.setEnabled(false);

        inputField.addValueChangeListener(e -> {
            if (infoTarget.getValue() != null) {
                if (inputField.getValue().equals("")) confirm.setEnabled(false);
                else confirm.setEnabled(true);
            }
        });

        final OptionGroup availableExpectedMessages = new OptionGroup("There are some expected results, which you currently do not provide:");

        if (subject.getExpectedMessages().size() != 0) {
            inputField.setVisible(false);
            infoTarget.setVisible(false);
        }

        for (Message m : subject.getExpectedMessages()) {
            availableExpectedMessages.addItem(m);
        }
        final String optionSpecifyMyself = new String("I can provide other results.");
        availableExpectedMessages.addItem(optionSpecifyMyself);

        availableExpectedMessages.addValueChangeListener(e -> {
            Object selectedItem = e.getProperty().getValue();
            if (selectedItem == optionSpecifyMyself) {
                inputField.setVisible(true);
                infoTarget.setVisible(true);
                if (inputField.getValue().equals("")) confirm.setEnabled(false);
            } else {
                inputField.setVisible(false);
                infoTarget.setVisible(false);
                confirm.setEnabled(true);
            }
        });

        for (Subject s : instance.getProcess().getSubjects())
            if (s != subject) infoTarget.addItem(s);
        final String optionSomebodyElse = new String("Somebody else.");
        final String optionDontKnow = new String("I do not know who could be interested");
        infoTarget.addItem(optionSomebodyElse);
        infoTarget.addItem(optionDontKnow);

        infoTarget.addValueChangeListener(e -> {
            if ((!subject.getExpectedMessages().isEmpty() && availableExpectedMessages.getValue() != optionSpecifyMyself) || !inputField.getValue().equals(""))
                confirm.setEnabled(true);
            Object selectedItem = e.getProperty().getValue();
            if (selectedItem == optionSomebodyElse) confirm.setCaption("Next");
            else confirm.setCaption("Done");
        });

        confirm.addClickListener(e -> {
            if (inputField.isEnabled() && infoTarget.getValue() != null) {
                String selection = infoTarget.getValue().toString();
                if (selection.equals(optionSomebodyElse))
                    askForNewSendSubject(inputField.getValue(), subject, instance);
                if (selection.equals(optionDontKnow)) {
                    Subject target = addAnonymousSubject(instance);
                    insertNewSendState(inputField.getValue(), target, subject, instance, false);
                    this.close();
                }
                if (infoTarget.getValue() instanceof Subject) {
                    insertNewSendState(inputField.getValue(), (Subject) infoTarget.getValue(), subject, instance, false);
                    this.close();
                }
            } else {
                Message m = (Message) availableExpectedMessages.getValue();
                insertNewSendStateAndRemoveExpectedMessage(m, subject, instance, false);
                this.close();
            }
        });

        fLayout.removeAllComponents();
        fLayout.addComponent(questionPrompt);
        if (subject.getExpectedMessages().size() != 0) fLayout.addComponent(availableExpectedMessages);
        fLayout.addComponent(inputField);
        if (inputField.isEnabled()) fLayout.addComponent(infoTarget);
        fLayout.addComponent(confirm);
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

    private void askForNewRecvSubject(String newMessage, Subject subject, Instance instance) {
        State state = instance.getAvailableStateForSubject(subject);
        final Label questionPrompt = new Label("I can get this input from somebody else.");
        final TextField inputField = new TextField("Whom do you get this input from?");

        Button confirm = new Button("Done");
        confirm.setEnabled(false);

        inputField.addValueChangeListener(e -> {
            if (inputField.getValue().equals("")) confirm.setEnabled(false);
            else confirm.setEnabled(true);
        });

        confirm.addClickListener(e -> {
            Subject newSubject = new Subject(inputField.getValue());
            insertNewSubject(newSubject, instance);
            insertNewReceiveState(newMessage, newSubject, subject, instance, true);
            this.close();
        });

        fLayout.removeAllComponents();
        fLayout.addComponent(questionPrompt);
        fLayout.addComponent(inputField);
        fLayout.addComponent(confirm);
    }

    private void askForNewSendSubject(String newMessage, Subject subject, Instance instance) {
        State state = instance.getAvailableStateForSubject(subject);
        final Label questionPrompt = new Label("I can provide this input to somebody else.");
        final TextField inputField = new TextField("Whom can you provide this input with?");

        Button confirm = new Button("Done");
        confirm.setEnabled(false);

        inputField.addValueChangeListener(e -> {
            if (inputField.getValue().equals("")) confirm.setEnabled(false);
            else confirm.setEnabled(true);
        });

        confirm.addClickListener(e -> {
            Subject newSubject = new Subject(inputField.getValue());
            insertNewSubject(newSubject, instance);
            insertNewSendState(newMessage, newSubject, subject, instance, false);
            this.close();
        });

        fLayout.removeAllComponents();
        fLayout.addComponent(questionPrompt);
        fLayout.addComponent(inputField);
        fLayout.addComponent(confirm);
    }

    private void newInitialSubject(Instance instance) {
        final Label questionPrompt = new Label("You want to add your first actor.");
        final TextField inputField = new TextField("What's its name?");

        Button confirm = new Button("Done");
        confirm.setEnabled(false);

        inputField.addValueChangeListener(e -> {
            if (inputField.getValue().equals("")) confirm.setEnabled(false);
            else confirm.setEnabled(true);
        });

        confirm.addClickListener(e -> {
            Subject newSubject = new Subject(inputField.getValue());
            insertNewSubject(newSubject, instance);
            this.close();
        });

        fLayout.removeAllComponents();
        fLayout.addComponent(questionPrompt);
        fLayout.addComponent(inputField);
        fLayout.addComponent(confirm);
    }

    private void askForConditions(String newState, Subject subject, Instance instance, Boolean addSendState) {
        State currentState = instance.getAvailableStateForSubject(subject);
        Set<State> predecessorStates = subject.getPredecessorStates(currentState);
        LogHelper.logInfo("Elaboration: found " + predecessorStates.size() + " predecessors for inserting " + newState);

        Map<State, TextField> originalConditionTextFields = new HashMap<>();
        Map<State, TextField> newConditionTextFields = new HashMap<>();

        Map<State, Condition> originalConditions = new HashMap<>();
        Map<State, Condition> newConditions = new HashMap<>();

        final Label questionPrompt = new Label(newState + "\" replaces \"" + currentState + "\" under certain conditions.");
        fLayout.removeAllComponents();
        fLayout.addComponent(questionPrompt);
        if (predecessorStates.isEmpty()) {
            State dummyState = new ActionState("Make decision");
            dummyState.addNextState(currentState);
            predecessorStates.add(dummyState);
        }
        for (State predecessor : predecessorStates) {
            Condition originalCondition = predecessor.getNextStates().get(currentState);
            if (originalCondition != null && !originalCondition.getCondition().equals(""))
                originalConditions.put(predecessor, originalCondition);
            else originalConditions.put(predecessor, new Condition(""));
            TextField inputFieldNew = new TextField("What is the condition for \"" + newState + "\" when coming from \"" + predecessor + "\"?");
            TextField inputFieldOld = new TextField("What is the condition for \"" + currentState + "\" when coming from \"" + predecessor + "\"?");
            inputFieldOld.setValue(originalConditions.get(predecessor).getCondition());
            if (originalConditions.get(predecessor) instanceof MessageCondition) {
                inputFieldOld.setEnabled(false);
                inputFieldOld.setDescription("This condition is bound to incoming input and cannot be changed here");
            }
            originalConditionTextFields.put(predecessor, inputFieldOld);
            newConditionTextFields.put(predecessor, inputFieldNew);
            fLayout.addComponent(inputFieldOld);
            fLayout.addComponent(inputFieldNew);
        }

        Button confirm = new Button("Done");
        if (addSendState == Boolean.TRUE) confirm.setCaption("Next");

        confirm.addClickListener(e -> {
            State newInsertedState = new ActionState(newState);
            for (State predecessor : predecessorStates) {
                if (!(originalConditions.get(predecessor) instanceof MessageCondition))
                    originalConditions.put(predecessor, new Condition(originalConditionTextFields.get(predecessor).getValue()));
                newConditions.put(predecessor, new Condition(newConditionTextFields.get(predecessor).getValue()));
            }
            insertNewStateAsideCurrentState(newInsertedState, newConditions, originalConditions, subject, instance);
            if (addSendState == Boolean.FALSE) {
                this.close();
            } else {
                resultsProvidedToOthers(newInsertedState, subject, instance);
            }
        });

        fLayout.addComponent(confirm);
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
        ProcessChange c = new RemoveExpectedMessageChange(subject,message);
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

        ProcessChange c = new AddStateChange(s, currentState, newState, insertBefore);
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