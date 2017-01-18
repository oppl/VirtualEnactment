package at.jku.ce.CoMPArE.elaborate.wizardsteps;

import at.jku.ce.CoMPArE.LogHelper;
import at.jku.ce.CoMPArE.elaborate.changeCommands.AddConditionalStateCommand;
import at.jku.ce.CoMPArE.elaborate.changeCommands.ProcessChangeCommand;
import at.jku.ce.CoMPArE.execute.Instance;
import at.jku.ce.CoMPArE.process.*;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import org.vaadin.teemu.wizards.Wizard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by oppl on 17/12/2016.
 */
public class AskForConditionsStep extends ElaborationStep {

    State state;

    State alreadyExistingState;

    final Label questionPrompt;
    TextField inputFieldNew;
    TextField inputFieldOld;

    final Set<State> predecessorStates;

    final Map<State, TextField> originalConditionTextFields;
    final Map<State, TextField> newConditionTextFields;

    final Map<State, Condition> originalConditions;
    final Map<State, Condition> newConditions;

    String newState;

    public AskForConditionsStep(Wizard owner, State newState, Subject s, Instance i) {
        this(owner,newState.getName(),s,i);
        LogHelper.logInfo("Instantiate newMessageStep with already known state");
        alreadyExistingState = newState;
    }

    public AskForConditionsStep(Wizard owner, String newState, Subject s, Instance i) {
        super(owner, s, i);
        alreadyExistingState = null;
        state = instance.getAvailableStateForSubject(subject);
        this.newState = newState;

        caption = new String("\"" + newState + "\" replaces \"" + state + "\" under certain conditions.");
        questionPrompt = new Label("\"" + newState + "\" replaces \"" + state + "\" under certain conditions.");
        inputFieldNew = new TextField("What is the condition for \"" + newState + "\"?");
        inputFieldOld = new TextField("What is the condition for \"" + state + "\"?");

        predecessorStates = subject.getPredecessorStates(state);
        LogHelper.logInfo("Elaboration: found " + predecessorStates.size() + " predecessors for inserting " + newState);

        originalConditionTextFields = new HashMap<>();
        newConditionTextFields = new HashMap<>();

        originalConditions = new HashMap<>();
        newConditions = new HashMap<>();

        if (predecessorStates.isEmpty()) {
            State dummyState = new ActionState("Make Decision");
            dummyState.addNextState(state);
            predecessorStates.add(dummyState);
        }
        for (State predecessor : predecessorStates) {
            inputFieldNew = new TextField("What is the condition for \"" + newState + "\" when coming from \"" + predecessor + "\"?");
            inputFieldOld = new TextField("What is the condition for \"" + state + "\" when coming from \"" + predecessor + "\"?");

            inputFieldNew.addValueChangeListener( e -> {
                if (inputFieldNew.getValue().equals("") || inputFieldOld.getValue().equals("")) setCanAdvance(false);
                else setCanAdvance(true);
                if (inputFieldNew.getValue().equals(inputFieldOld.getValue())) {
                    Notification.show("Conditions must not be identical!",Notification.Type.WARNING_MESSAGE);
                    setCanAdvance(false);
                }
            });

            inputFieldOld.addValueChangeListener( e -> {
                if (inputFieldNew.getValue().equals("") || inputFieldOld.getValue().equals("")) setCanAdvance(false);
                else setCanAdvance(true);
                if (inputFieldNew.getValue().equals(inputFieldOld.getValue())) {
                    Notification.show("Conditions must not be identical!",Notification.Type.WARNING_MESSAGE);
                    setCanAdvance(false);
                }
            });

            Condition originalCondition = predecessor.getNextStates().get(state);
            if (originalCondition != null && !originalCondition.getCondition().equals(""))
                originalConditions.put(predecessor, originalCondition);
            else originalConditions.put(predecessor, null);
            if (originalConditions.get(predecessor) != null) inputFieldOld.setValue(originalConditions.get(predecessor).getCondition());
            if (originalConditions.get(predecessor) instanceof MessageCondition) {
                inputFieldOld.setEnabled(false);
                inputFieldOld.setDescription("This condition is bound to incoming input and cannot be changed here");
            }
            originalConditionTextFields.put(predecessor, inputFieldOld);
            newConditionTextFields.put(predecessor, inputFieldNew);
        }

        fLayout.addComponent(questionPrompt);
        for (State predecessor: originalConditionTextFields.keySet()) {
            fLayout.addComponent(originalConditionTextFields.get(predecessor));
            fLayout.addComponent(newConditionTextFields.get(predecessor));
        }
    }

    @Override
    public List<ProcessChangeCommand> getProcessChangeList() {
        State newInsertedState = new ActionState(newState);
        if (alreadyExistingState != null) newInsertedState = alreadyExistingState;
        state = instance.getAvailableStateForSubject(subject);

        for (State predecessor : predecessorStates) {
            LogHelper.logInfo(originalConditionTextFields.get(predecessor).getValue()+" "+newConditionTextFields.get(predecessor).getValue());
            if (!(originalConditions.get(predecessor) instanceof MessageCondition)) // TODO: check whether this really works (entry might be missing when executing the command
                originalConditions.put(predecessor, new Condition(originalConditionTextFields.get(predecessor).getValue()));
            newConditions.put(predecessor, new Condition(newConditionTextFields.get(predecessor).getValue()));
        }

        processChanges.add(new AddConditionalStateCommand(subject,state, newInsertedState, originalConditions, newConditions));

        return processChanges;
    }
}
