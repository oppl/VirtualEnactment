package at.jku.ce.CoMPArE.elaborate;

import at.jku.ce.CoMPArE.LogHelper;
import at.jku.ce.CoMPArE.execute.Instance;
import at.jku.ce.CoMPArE.process.*;
import at.jku.ce.CoMPArE.process.Process;
import at.jku.ce.CoMPArE.visualize.VisualizationUI;
import com.vaadin.ui.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by oppl on 23/11/2016.
 */
public class ElaborationUI extends Window {

    public static int ELABORATE = 1;
    public static int INITIALSTEP = 2;
    public static int ADDITIONALSTEP = 3;
    public static int INITALSUBJECT = 4;

    FormLayout fLayout = new FormLayout();

    public ElaborationUI() {
        super("Elaborate on this problem");
        this.setWidth("900px");
        this.setHeight("500px");
        this.center();
        fLayout.setMargin(true);
        fLayout.setSpacing(true);
        setContent(fLayout);
    }

    public void elaborate(Subject subject, Instance instance) {
        askForReason(subject, instance);
    }

    public void initialStep(Subject subject, Instance instance) {
        this.setCaption("Add an initial step");
        newInitialStep(subject,instance);
    }

    public void initialSubject(Instance instance) {
        this.setCaption("Add an intial actor");
        newInitialSubject(instance);
    }

    public void additionalStep(Subject subject, Instance instance) {
        this.setCaption("Add an additional step");
        newAdditionalStep(subject,instance);
    }

    private void askForReason(Subject subject, Instance instance) {
        State state = instance.getAvailableStateForSubject(subject);

        final Label questionPrompt = new Label("What is the problem with \""+state+"\"?");

        final OptionGroup answerOptions = new OptionGroup("Please select:");
        final String option1 = new String("It can't be done at the moment.");
        final String option2 = new String("I rather need to do something else instead.");
        final String option3 = new String("It's too vague to be performed.");
        final String option4 = new String("It's incorrect.");


        answerOptions.addItem(option1);
        answerOptions.addItem(option2);
        answerOptions.addItem(option3);
        answerOptions.addItem(option4);

        Button confirm = new Button("Next");
        confirm.addClickListener( e -> {
            String selection = (String) answerOptions.getValue();
            if (selection.equals(option1)) cantBeDone(subject,instance);
            if (selection.equals(option2)) somethingElseInstead(subject,instance);
            if (selection.equals(option3)) tooVague(subject,instance);
            if (selection.equals(option4)) removeIncorrectState(subject,instance);
        });

        fLayout.removeAllComponents();
        fLayout.addComponent(questionPrompt);
        fLayout.addComponent(answerOptions);
        fLayout.addComponent(confirm);
    }

    private void tooVague(Subject subject, Instance instance) {
        State state = instance.getAvailableStateForSubject(subject);

        final Label questionPrompt = new Label("\""+state+"\" is too vague.");
        final TextField inputField = new TextField("What would be the first activity you need to do when refining \""+ state + "\"?");
        final CheckBox newMessage = new CheckBox("This activity leads to results I can provide to others.");
        Button confirm = new Button("Done");

        newMessage.addValueChangeListener( e -> {
            Boolean value = (Boolean) e.getProperty().getValue();
            if (value == Boolean.TRUE) confirm.setCaption("Next");
            else confirm.setCaption("Done");
        });

        confirm.addClickListener( e -> {
            State newState = new ActionState(inputField.getValue());
            replaceState(state,newState,subject,instance);
            if (newMessage.getValue() == Boolean.FALSE) {
                this.close();
            }
            else {
                resultsProvidedToOthers(newState,subject,instance);
            }
        });

        fLayout.removeAllComponents();
        fLayout.addComponent(questionPrompt);
        fLayout.addComponent(inputField);
        fLayout.addComponent(newMessage);
        fLayout.addComponent(confirm);

    }

    private void removeIncorrectState(Subject subject, Instance instance) {
        State state = instance.getAvailableStateForSubject(subject);

        final Label questionPrompt = new Label("\""+state+"\" is incorrect.");

        final OptionGroup answerOptions = new OptionGroup("How can this be corrected?");
        final String option1 = new String("Simply remove \""+state+"\".");
        final String option2 = new String("Replace \""+state+"\" with something else.");

        answerOptions.addItem(option1);
        answerOptions.addItem(option2);

        Button confirm = new Button("Done");

        answerOptions.addValueChangeListener( e -> {
            Object selectedItem = e.getProperty().getValue();
            if (selectedItem == option2) confirm.setCaption("Next");
            else confirm.setCaption("Done");
        });

        confirm.addClickListener( e -> {
            String selection = (String) answerOptions.getValue();
            if (selection.equals(option1)) {
                deleteState(state,subject,instance);
                this.close();
            }
            if (selection.equals(option2)) replaceIncorrectState(subject, instance);
        });

        fLayout.removeAllComponents();
        fLayout.addComponent(questionPrompt);
        fLayout.addComponent(answerOptions);
        fLayout.addComponent(confirm);

    }

    private void replaceIncorrectState(Subject subject, Instance instance) {
        State state = instance.getAvailableStateForSubject(subject);

        final Label questionPrompt = new Label("I want to replace \""+state+"\" with something else.");
        final TextField inputField = new TextField("What is the new activity?");
        final CheckBox newMessage = new CheckBox("This activity leads to results I can provide to others.");
        Button confirm = new Button("Done");

        final Button selectFromExisting = new Button ("Let me choose from existing steps");
        selectFromExisting.addClickListener( e -> {
            VisualizationUI viz= new VisualizationUI(instance);
            getUI().addWindow(viz);
            viz.showSubject(subject);
            viz.activateSelectionMode();
            viz.addCloseListener( e1 -> {
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

        inputField.addValueChangeListener( e -> {
            if (instance.getProcess().getStateWithName(inputField.getValue()) == null) {
                newMessage.setEnabled(true);
                newMessage.setDescription("");
            }
        });

        newMessage.addValueChangeListener( e -> {
            Boolean value = (Boolean) e.getProperty().getValue();
            if (value == Boolean.TRUE) confirm.setCaption("Next");
            else confirm.setCaption("Done");
        });

        confirm.addClickListener( e -> {
            State newState = new ActionState(inputField.getValue());
            replaceState(state,newState,subject,instance);
            if (newMessage.getValue() == Boolean.FALSE) {
                this.close();
            }
            else {
                resultsProvidedToOthers(newState,subject,instance);
            }
        });

        fLayout.removeAllComponents();
        fLayout.addComponent(questionPrompt);
        fLayout.addComponent(inputField);
        fLayout.addComponent(selectFromExisting);
        fLayout.addComponent(newMessage);
        fLayout.addComponent(confirm);

    }

    private void cantBeDone(Subject subject, Instance instance) {
        State state = instance.getAvailableStateForSubject(subject);

        final Label questionPrompt = new Label("\""+state+"\" can't be done at the moment.");
        Button confirm = new Button("Next");

        final OptionGroup answerOptions = new OptionGroup("Why?");
        final String option1 = new String("I need to do something else first.");
        final String option2 = new String("I need more input to be able to do this activity.");

        answerOptions.addItem(option1);
        answerOptions.addItem(option2);

        confirm.addClickListener( e -> {
            String selection = (String) answerOptions.getValue();
            if (selection.equals(option1)) somethingElseFirst(subject,instance);
            if (selection.equals(option2)) needMoreInput(subject,instance);
        });

        fLayout.removeAllComponents();
        fLayout.addComponent(questionPrompt);
        fLayout.addComponent(answerOptions);
        fLayout.addComponent(confirm);
    }

    private void newInitialStep(Subject subject, Instance instance) {

        final Label questionPrompt = new Label("I want to set an initial step for  " + subject + ".");
        final TextField inputField = new TextField("What do you want to do?");
        final CheckBox newMessage = new CheckBox("This activity leads to results I can provide to others.");
        Button confirm = new Button("Done");

        newMessage.addValueChangeListener( e -> {
            Boolean value = (Boolean) e.getProperty().getValue();
            if (value == Boolean.TRUE) confirm.setCaption("Next");
            else confirm.setCaption("Done");
        });

        confirm.addClickListener( e -> {
            LogHelper.logInfo("Elaboration: inserting new initial step "+inputField.getValue()+" into "+subject);
            State newState = insertNewActionState(inputField.getValue(),subject,instance,true);
            if (newMessage.getValue() == Boolean.FALSE) {
                this.close();
            }
            else {
                resultsProvidedToOthers(newState,subject,instance);
            }
        });

        fLayout.removeAllComponents();
        fLayout.addComponent(questionPrompt);
        fLayout.addComponent(inputField);
        fLayout.addComponent(newMessage);
        fLayout.addComponent(confirm);

    }

    private void newAdditionalStep(Subject subject, Instance instance) {

        final Label questionPrompt = new Label("I want to set an additional step for  " + subject + ".");
        final TextField inputField = new TextField("What do you want to do?");
        final CheckBox newMessage = new CheckBox("This activity leads to results I can provide to others.");
        Button confirm = new Button("Done");

        final Button selectFromExisting = new Button ("Let me choose from existing steps");
        selectFromExisting.addClickListener( e -> {
            VisualizationUI viz= new VisualizationUI(instance);
            getUI().addWindow(viz);
            viz.showSubject(subject);
            viz.activateSelectionMode();
            viz.addCloseListener( e1 -> {
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

        inputField.addValueChangeListener( e -> {
            if (instance.getProcess().getStateWithName(inputField.getValue()) == null) {
                newMessage.setEnabled(true);
                newMessage.setDescription("");
            }
        });

        newMessage.addValueChangeListener( e -> {
            Boolean value = (Boolean) e.getProperty().getValue();
            if (value == Boolean.TRUE) confirm.setCaption("Next");
            else confirm.setCaption("Done");
        });

        confirm.addClickListener( e -> {
            LogHelper.logInfo("Elaboration: inserting new additional step "+inputField.getValue()+" into "+subject);
            State newState = insertNewActionState(inputField.getValue(),subject,instance,false);
            if (newMessage.getValue() == Boolean.FALSE) {
                this.close();
            }
            else {
                resultsProvidedToOthers(newState,subject,instance);
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

        newMessage.addValueChangeListener( e -> {
            Boolean value = (Boolean) e.getProperty().getValue();
            if (value == Boolean.TRUE) confirm.setCaption("Next");
            else confirm.setCaption("Done");
        });

        confirm.addClickListener( e -> {
            LogHelper.logInfo("Elaboration: inserting "+inputField.getValue()+" into "+subject);
            State newState = insertNewActionState(inputField.getValue(),subject,instance,true);
            if (newMessage.getValue() == Boolean.FALSE) {
                this.close();
            }
            else {
                resultsProvidedToOthers(newState,subject,instance);
            }
        });

        fLayout.removeAllComponents();
        fLayout.addComponent(questionPrompt);
        fLayout.addComponent(inputField);
        fLayout.addComponent(newMessage);
        fLayout.addComponent(confirm);
    }

    private void resultsProvidedToOthers(State newState, Subject subject, Instance instance) {
        State state = instance.getAvailableStateForSubject(subject);
        final Label questionPrompt = new Label("\"" + newState + "\" leads to results I can provide to others.");
        final TextField inputField = new TextField("What can you provide to others?");
        final OptionGroup infoTarget = new OptionGroup("Whom do you provide it to?");

        final OptionGroup availableExpectedMessages = new OptionGroup("There are some expected results, which you currently do not provide:");
        for (Message m : subject.getExpectedMessages()) {
            availableExpectedMessages.addItem(m);
        }
        final String optionSpecifyMyself = new String("I can provide other results.");
        availableExpectedMessages.addItem(optionSpecifyMyself);

        availableExpectedMessages.addValueChangeListener( e -> {
            Object selectedItem = e.getProperty().getValue();
            if (selectedItem == optionSpecifyMyself) {
                inputField.setEnabled(true);
                infoTarget.setVisible(true);
            }
            else {
                inputField.setEnabled(false);
                infoTarget.setVisible(false);

            }
        });

        for (Subject s: instance.getProcess().getSubjects())
            if (s != subject) infoTarget.addItem(s);
        final String optionSomebodyElse = new String("Somebody else.");
        final String optionDontKnow = new String("I do not know who could be interested");
        infoTarget.addItem(optionSomebodyElse);
        infoTarget.addItem(optionDontKnow);

        Button confirm = new Button("Done");

        infoTarget.addValueChangeListener( e -> {
            Object selectedItem = e.getProperty().getValue();
            if (selectedItem == optionSomebodyElse) confirm.setCaption("Next");
            else confirm.setCaption("Done");
        });

        confirm.addClickListener( e -> {
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
            }
            else {
                Message m = (Message) availableExpectedMessages.getValue();
                insertNewSendState(m,subject,instance,false);
                subject.removeExpectedMessage(m);
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
            for (Message m: ((RecvState) state).getRecvdMessages())
                subject.addProvidedMessage(m);
        }
        if (predecessorStates.isEmpty()) {
            if (nextStates.size() == 1) {
                subject.setFirstState(nextStates.keySet().iterator().next());
                instance.getAvailableStates().replace(subject,subject.getFirstState());
            }
            else {
                if (nextStates.size() > 1)
                    state.setName("Make decision");
            }
        }
        else {

            for (State pre : predecessorStates) {
                for (State s : nextStates.keySet())
                    pre.addNextState(s, nextStates.get(s));
                pre.removeNextState(state);
            }
            if (nextStates.size()==1) instance.getAvailableStates().replace(subject,nextStates.keySet().iterator().next());
            else instance.getAvailableStates().replace(subject,predecessorStates.iterator().next());
        }
    }

    private void replaceState(State oldState, State newState, Subject subject, Instance instance) {
        Set<State> predecessorStates = subject.getPredecessorStates(oldState);
        Map<State, Condition> nextStates = oldState.getNextStates();
        if (oldState instanceof SendState) subject.addExpectedMessage(((SendState) oldState).getSentMessage());
        if (oldState instanceof RecvState) {
            for (Message m: ((RecvState) oldState).getRecvdMessages())
                subject.addProvidedMessage(m);
        }
        for (State s : nextStates.keySet()) {
            newState.addNextState(s,nextStates.get(s));
        }

        if (predecessorStates.isEmpty()) {
            subject.setFirstState(newState);

        }
        else {
            for (State pre : predecessorStates) {
                pre.addNextState(newState, nextStates.get(pre.getNextStates().get(oldState)));
                pre.removeNextState(oldState);
            }
        }
        instance.getAvailableStates().replace(subject, newState);
    }

    private void needMoreInput(Subject subject, Instance instance) {
        State state = instance.getAvailableStateForSubject(subject);

        final Button confirm = new Button("Done");
        final Label questionPrompt = new Label("I need more input to do \"" + state + "\".");
        final OptionGroup infoSource = new OptionGroup("Where could you get it from?");

        final TextField inputField = new TextField("Which input would you need?");
        if (subject.getProvidedMessages().size() != 0) inputField.setEnabled(false);

        final OptionGroup availableProvidedMessages = new OptionGroup("There is some input available, which you currently do not use:");
        for (Message m : subject.getProvidedMessages()) {
            availableProvidedMessages.addItem(m);
        }
        final String optionSpecifyMyself = new String("I need different input.");
        availableProvidedMessages.addItem(optionSpecifyMyself);
        availableProvidedMessages.addValueChangeListener( e -> {
            Object selectedItem = e.getProperty().getValue();
            if (selectedItem == optionSpecifyMyself) {
                inputField.setEnabled(true);
                infoSource.setVisible(true);
            }
            else {
                inputField.setEnabled(false);
                infoSource.setVisible(false);

            }
        });

        for (Subject s: instance.getProcess().getSubjects())
            if (s != subject) infoSource.addItem(s);
        final String optionSomebodyElse = new String("I can get this input from somebody else.");
        final String optionSystem = new String("I can retrieve this input from a system I have access to.");
        final String optionDontKnow = new String("I do not know, where I can get this input from");
        infoSource.addItem(optionSomebodyElse);
        infoSource.addItem(optionSystem);
        infoSource.addItem(optionDontKnow);

        infoSource.addValueChangeListener( e -> {
            Object selectedItem = e.getProperty().getValue();
            if (selectedItem == optionSomebodyElse || selectedItem == optionSystem) confirm.setCaption("Next");
            else confirm.setCaption("Done");
        });

        confirm.addClickListener( e -> {
            if (inputField.isEnabled() && infoSource.getValue() != null) {
                String selection = infoSource.getValue().toString();
                if (selection.equals(optionSystem)) askForSystem(inputField.getValue(), subject, instance);
                if (selection.equals(optionSomebodyElse))
                    askForNewRecvSubject(inputField.getValue(), subject, instance);
                if (selection.equals(optionDontKnow)) {
                    Subject source = addAnonymousSubject(instance);
                    insertNewReceiveState(inputField.getValue(), source, subject, instance, true);
                    this.close();
                }
                if (infoSource.getValue() instanceof Subject) {
                    insertNewReceiveState(inputField.getValue(), (Subject) infoSource.getValue(), subject, instance, true);
                    this.close();
                }
            }
            else {
                Message m = (Message) availableProvidedMessages.getValue();
                insertNewReceiveState(m,subject,instance,true);
                subject.removeProvidedMessage(m);
                this.close();
            }
        });

        fLayout.removeAllComponents();
        fLayout.addComponent(questionPrompt);
        if (subject.getProvidedMessages().size() != 0) fLayout.addComponent(availableProvidedMessages);
        fLayout.addComponent(inputField);
        fLayout.addComponent(infoSource);
        if (!inputField.isEnabled()) infoSource.setVisible(false);
        fLayout.addComponent(confirm);

    }

    private void askForNewRecvSubject(String newMessage, Subject subject, Instance instance) {
        State state = instance.getAvailableStateForSubject(subject);
        final Label questionPrompt = new Label("I can get this input from somebody else.");
        final TextField inputField = new TextField("Whom do you get this input from?");

        Button confirm = new Button("Done");

        confirm.addClickListener( e -> {
            Subject newSubject = new Subject(inputField.getValue());
            insertNewSubject(newSubject,instance);
            insertNewReceiveState(newMessage,newSubject,subject,instance, true);
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
        confirm.addClickListener( e -> {
            Subject newSubject = new Subject(inputField.getValue());
            insertNewSubject(newSubject,instance);
            insertNewSendState(newMessage,newSubject,subject,instance,false);
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
        confirm.addClickListener( e -> {
            Subject newSubject = new Subject(inputField.getValue());
            insertNewSubject(newSubject,instance);
            this.close();
        });

        fLayout.removeAllComponents();
        fLayout.addComponent(questionPrompt);
        fLayout.addComponent(inputField);
        fLayout.addComponent(confirm);
    }


    private void askForSystem(String newMessage, Subject subject, Instance instance) {
        State state = instance.getAvailableStateForSubject(subject);
        final Label questionPrompt = new Label("I can retrieve this input from a system I have access to.");
        final TextField inputField = new TextField("Which system is this?");

        Button confirm = new Button("Done");
        confirm.addClickListener( e -> {
            insertNewActionState("Retrieve "+newMessage+" from "+inputField.getValue(),subject,instance, true);
            this.close();
        });

        fLayout.removeAllComponents();
        fLayout.addComponent(questionPrompt);
        fLayout.addComponent(inputField);
        fLayout.addComponent(confirm);
    }

    private void somethingElseInstead(Subject subject, Instance instance) {
        State state = instance.getAvailableStateForSubject(subject);

        final Label questionPrompt = new Label("I need to do something else instead of \"" + state + "\".");
        final TextField inputField = new TextField("What do you need to do?");
        final CheckBox newMessage = new CheckBox("This step leads to results I can provide to others.");
        final OptionGroup relationship = new OptionGroup("How does this relate to \"" + state + "\"?");
        final String optionConditionalReplace = new String("It replaces \""+state+"\" under certain conditions.");
        final String optionAdditionalActivity = new String("It is complementary to \"" + state + "\", I still need to do \"" + state + "\", too.");


        final Button selectFromExisting = new Button ("Let me choose from existing steps");
        selectFromExisting.addClickListener( e -> {
            VisualizationUI viz= new VisualizationUI(instance);
            getUI().addWindow(viz);
            viz.showSubject(subject);
            viz.activateSelectionMode();
            viz.addCloseListener( e1 -> {
                 LogHelper.logInfo("Elaboration: now getting selected state from behaviour vizualization ...");
                 State selectedState = viz.getSelectedState();
                 if (selectedState != null) {
                     LogHelper.logInfo("Elaboration: selected state found");
                     inputField.setValue(selectedState.toString());
                     newMessage.setEnabled(false);
                     newMessage.setDescription("You cannot alter the selected existing step here.");
                     relationship.setValue(optionConditionalReplace);
                     relationship.setEnabled(false);
                     newMessage.setDescription("Existing steps can only be inserted as alternatives to the current step.");
                 }
            });
        });

        inputField.addValueChangeListener( e -> {
            if (instance.getProcess().getStateWithName(inputField.getValue()) == null) {
                newMessage.setEnabled(true);
                newMessage.setDescription("");
                relationship.setEnabled(true);
                relationship.setDescription("");
            }
        });

        Button confirm = new Button("Done");

        relationship.addItem(optionConditionalReplace);
        relationship.addItem(optionAdditionalActivity);

        relationship.addValueChangeListener( e -> {
            Object selectedItem = e.getProperty().getValue();
            Boolean value = (Boolean) newMessage.getValue();
            if (selectedItem == optionConditionalReplace || value == Boolean.TRUE) confirm.setCaption("Next");
            else confirm.setCaption("Done");
        });

        newMessage.addValueChangeListener( e -> {
            Boolean value = (Boolean) e.getProperty().getValue();
            Object selectedItem = relationship.getValue();
            if (value == Boolean.TRUE || selectedItem == optionConditionalReplace) confirm.setCaption("Next");
            else confirm.setCaption("Done");
        });

        confirm.addClickListener( e -> {
            String selection = relationship.getValue().toString();
            if (selection.equals(optionConditionalReplace)) askForConditions(inputField.getValue(),subject,instance,newMessage.getValue());
            if (selection.equals(optionAdditionalActivity)) {
                State newState = insertNewActionState(inputField.getValue(),subject,instance, true);
                if (newMessage.getValue() == Boolean.FALSE) {
                    this.close();
                }
                else {
                    resultsProvidedToOthers(newState,subject,instance);
                }
            }
        });

        fLayout.removeAllComponents();
        fLayout.addComponent(questionPrompt);
        fLayout.addComponent(inputField);
        fLayout.addComponent(selectFromExisting);
        fLayout.addComponent(newMessage);
        fLayout.addComponent(relationship);
        fLayout.addComponent(confirm);

    }

    private void askForConditions(String newState, Subject subject, Instance instance, Boolean addSendState) {
        State currentState = instance.getAvailableStateForSubject(subject);
        Set<State> predecessorStates = subject.getPredecessorStates(currentState);
        LogHelper.logInfo("Elaboration: found "+predecessorStates.size()+" predecessors for inserting " + newState);

        Map<State,TextField> originalConditionTextFields = new HashMap<>();
        Map<State,TextField> newConditionTextFields = new HashMap<>();

        Map<State, Condition> originalConditions = new HashMap<>();
        Map<State,Condition> newConditions = new HashMap<>();

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
            if (originalCondition != null && !originalCondition.getCondition().equals("nC")) originalConditions.put(predecessor,originalCondition);
            else originalConditions.put(predecessor,new Condition(""));
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

        confirm.addClickListener( e -> {
            State newInsertedState = new ActionState(newState);
            for (State predecessor : predecessorStates) {
                if (!(originalConditions.get(predecessor) instanceof MessageCondition)) originalConditions.put(predecessor, new Condition(originalConditionTextFields.get(predecessor).getValue()));
                newConditions.put(predecessor, new Condition(newConditionTextFields.get(predecessor).getValue()));
            }
            insertNewStateAsideCurrentState(newInsertedState, newConditions, originalConditions,subject,instance);
            if (addSendState == Boolean.FALSE) {
                this.close();
            }
            else {
                resultsProvidedToOthers(newInsertedState,subject,instance);
            }
        });

        fLayout.addComponent(confirm);
    }

    private Subject addAnonymousSubject(Instance instance) {
        Subject anonymous = null;
        for (Subject s: instance.getProcess().getSubjects()) {
            if (s.toString().equals("Anonymous")) {
                anonymous = s;
            }
        }
        if (anonymous == null) {
            anonymous = new Subject("Anonymous");
            insertNewSubject(anonymous,instance);
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
        insertNewState(newState,s,instance, insertBefore);
        return newState;
    }

    private State insertNewReceiveState(String messageName, Subject provider, Subject subject, Instance instance, boolean insertBefore) {
        RecvState newState = new RecvState("Wait for "+messageName);
        Message newMessage = new Message(messageName);
        newState.addRecvdMessage(newMessage);
        insertNewState(newState, subject, instance, insertBefore);
        provider.addExpectedMessage(newMessage);
        return newState;
    }

    private State insertNewReceiveState(Message message, Subject subject, Instance instance, boolean insertBefore) {
        RecvState newState = new RecvState("Wait for " + message);
        newState.addRecvdMessage(message);
        insertNewState(newState, subject, instance, insertBefore);
        return newState;
    }

    private State insertNewSendState(String messageName, Subject target, Subject subject, Instance instance, boolean insertBefore) {
        SendState newState = new SendState("Send "+messageName);
        Message newMessage = new Message(messageName);
        newState.setSentMessage(newMessage);
        insertNewState(newState, subject, instance, insertBefore);
        target.addProvidedMessage(newMessage);
        return newState;
    }

    private State insertNewSendState(Message message, Subject subject, Instance instance, boolean insertBefore) {
        SendState newState = new SendState("Send "+message);
        newState.setSentMessage(message);
        insertNewState(newState, subject, instance, insertBefore);
        return newState;
    }


    private void insertNewState(State newState, Subject s, Instance instance, boolean insertBefore) {
        State currentState = instance.getAvailableStateForSubject(s);

        if (insertBefore) {
            if (currentState == s.getFirstState() || s.getFirstState() == null) {
                LogHelper.logInfo("Elaboration: inserting " + newState + " as new first state in subject " + s);
                s.setFirstState(newState);
                if (currentState != null) newState.addNextState(currentState);
                instance.getAvailableStates().put(s, newState);
                return;
            }

            Set<State> predecessorStates = s.getPredecessorStates(currentState);
            LogHelper.logInfo("Elaboration: found "+predecessorStates.size()+" predecessors for inserting " + newState);

            if (!predecessorStates.isEmpty()) {
                for (State predecessorState : predecessorStates) {
                    LogHelper.logInfo("Elaboration: inserting " + newState + " after " + predecessorState);
                    Condition c = predecessorState.getNextStates().get(currentState);
                    predecessorState.getNextStates().remove(currentState);
                    predecessorState.getNextStates().put(newState, c);
                }
                newState.addNextState(currentState);
                instance.getAvailableStates().replace(s, newState);
            }
        }
        else {
            if (currentState == null) {
                currentState = instance.getHistoryForSubject(s).getFirst();
                instance.getAvailableStates().replace(s,newState);
            }
            for (State nextState: currentState.getNextStates().keySet()) {
                newState.addNextState(nextState,currentState.getNextStates().get(nextState));
            }
            currentState.removeAllNextStates();
            currentState.addNextState(newState);
        }
    }

    private void insertNewStateAsideCurrentState(State newState, Map<State, Condition> newCs, Map<State, Condition> oldCs, Subject s, Instance instance) {
        State currentState = instance.getAvailableStateForSubject(s);
        if (currentState == s.getFirstState()) {
            State decisionState = new ActionState("Make decision");
            s.setFirstState(decisionState);
            decisionState.addNextState(currentState, oldCs.values().iterator().next());
            decisionState.addNextState(newState, newCs.values().iterator().next());
            instance.getAvailableStates().replace(s,newState);
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