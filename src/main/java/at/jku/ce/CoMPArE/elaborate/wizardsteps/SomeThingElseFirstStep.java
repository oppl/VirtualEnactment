package at.jku.ce.CoMPArE.elaborate.wizardsteps;

import at.jku.ce.CoMPArE.LogHelper;
import at.jku.ce.CoMPArE.elaborate.changeCommands.AddStateCommand;
import at.jku.ce.CoMPArE.elaborate.changeCommands.ProcessChangeCommand;
import at.jku.ce.CoMPArE.execute.Instance;
import at.jku.ce.CoMPArE.process.*;
import com.vaadin.ui.*;
import org.vaadin.teemu.wizards.Wizard;

import java.util.List;

/**
 * Created by oppl on 17/12/2016.
 */
public class SomeThingElseFirstStep extends ElaborationStep {

    State state;
    ResultsProvidedToOthersStep step;

    final Label questionPrompt;
    final TextField inputField;
    final CheckBox newMessage;
    final OptionGroup availableProvidedMessages;
    final String optionNo;

    public SomeThingElseFirstStep(Wizard owner, Subject s, Instance i) {
        super(owner, s, i);
        state = instance.getAvailableStateForSubject(subject);
        caption = new String("I need to do something else before I do \"" + state + "\".");

        questionPrompt = new Label("I need to do something else before I do \"" + state + "\".");
        inputField = new TextField("What do you need to do?");
        newMessage = new CheckBox("This activity leads to results I can provide to others.");

        inputField.addValueChangeListener(e -> {
            if (inputField.getValue().equals("")) setCanAdvance(false);
            else setCanAdvance(true);
            if (step != null) step.updateNameOfState(inputField.getValue());
        });

        newMessage.addValueChangeListener(e -> {
            Boolean value = (Boolean) e.getProperty().getValue();
            removeNextSteps();
            if (value == Boolean.TRUE) step = new ResultsProvidedToOthersStep(owner, inputField.getValue(), subject, instance);
            else step = null;
            addNextStep(step);
        });

        availableProvidedMessages = new OptionGroup("Do you want to react on any of the following available inputs in this step?");
        for (Message m : subject.getProvidedMessages()) {
            availableProvidedMessages.addItem(m);
        }
        optionNo = new String("No");
        availableProvidedMessages.addItem(optionNo);
        availableProvidedMessages.setValue(optionNo);

        fLayout.addComponent(questionPrompt);
        fLayout.addComponent(inputField);
        fLayout.addComponent(newMessage);
        if (!subject.getProvidedMessages().isEmpty()) fLayout.addComponent(availableProvidedMessages);
    }

    @Override
    public List<ProcessChangeCommand> getProcessChanges() {
        LogHelper.logInfo("Elaboration: inserting " + inputField.getValue() + " into " + subject);
        Object selectedItem = availableProvidedMessages.getValue();
        if (selectedItem != optionNo) {
            // TODO: add RecvState and remove Message from providedMessages
            /*State predecessor = null;
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
            subject.removeProvidedMessage((Message) selectedItem);*/

        }
        processChanges.add(new AddStateCommand(subject, state, new ActionState(inputField.getValue()),true));
        return processChanges;
    }
}
