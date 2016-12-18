package at.jku.ce.CoMPArE.elaborate.wizardsteps;

import at.jku.ce.CoMPArE.LogHelper;
import at.jku.ce.CoMPArE.elaborate.changeCommands.AddStateCommand;
import at.jku.ce.CoMPArE.elaborate.changeCommands.ProcessChangeCommand;
import at.jku.ce.CoMPArE.elaborate.changeCommands.RemoveProvidedMessageCommand;
import at.jku.ce.CoMPArE.execute.Instance;
import at.jku.ce.CoMPArE.process.*;
import at.jku.ce.CoMPArE.visualize.VisualizationUI;
import com.vaadin.ui.*;
import org.vaadin.teemu.wizards.Wizard;

import java.util.List;

/**
 * Created by oppl on 17/12/2016.
 */
public class AddAdditionalStepStep extends ElaborationStep {

    final String optionNoInput;
    final OptionGroup availableProvidedMessages;
    final Label questionPrompt;
    final TextField inputField;
    final CheckBox newMessage;

    public AddAdditionalStepStep(Wizard owner, Subject s, Instance i) {
        super(owner, s, i);
        caption = new String("I want to set an additional step for  " + subject + ".");
        questionPrompt = new Label("I want to set an additional step for  " + subject + ".");
        inputField = new TextField("What do you want to do?");
        newMessage = new CheckBox("This activity leads to results I can provide to others.");

        inputField.addValueChangeListener(e -> {
            if (inputField.getValue().equals("")) setCanAdvance(false);
            else setCanAdvance(true);
        });

        final Button selectFromExisting = new Button("Let me choose from existing steps");
        selectFromExisting.addClickListener(e -> {
            VisualizationUI viz = new VisualizationUI(instance, "viz");
            owner.getUI().addWindow(viz);
            viz.showSubject(subject);
            viz.activateSelectionMode();
            viz.addCloseListener(e1 -> {
                LogHelper.logInfo("Elaboration: now getting selected state from behaviour vizualization ...");
                State selectedState = viz.getSelectedState();
                if (selectedState != null) {
                    LogHelper.logInfo("Elaboration: selected state found");
                    inputField.setValue(selectedState.toString());
                    newMessage.setVisible(false);
                    newMessage.setDescription("You cannot alter the selected existing step here.");
                    newMessage.setDescription("Existing steps can only be inserted as alternatives to the current step.");
                }
            });
        });

        inputField.addValueChangeListener(e -> {
            if (instance.getProcess().getStateWithName(inputField.getValue()) == null) {
                newMessage.setVisible(true);
                newMessage.setDescription("");
            }
        });

        newMessage.addValueChangeListener(e -> {
            Boolean value = (Boolean) e.getProperty().getValue();
            removeNextSteps();
            ElaborationStep step;
            if (value == Boolean.TRUE) step = new ResultsProvidedToOthersStep(owner, inputField.getValue(), subject, instance);
            else step = null;
            addNextStep(step);
        });

        availableProvidedMessages = new OptionGroup("There is input available, on which you might want to react:");
        for (Message m : subject.getProvidedMessages()) {
            availableProvidedMessages.addItem(m);
        }
        optionNoInput = new String("I don't want to react on any input.");
        availableProvidedMessages.addItem(optionNoInput);
        availableProvidedMessages.setValue(optionNoInput);


        fLayout.addComponent(questionPrompt);
        fLayout.addComponent(inputField);
        fLayout.addComponent(selectFromExisting);
        fLayout.addComponent(newMessage);
        if (!subject.getProvidedMessages().isEmpty()) fLayout.addComponent(availableProvidedMessages);

    }

    @Override
    public List<ProcessChangeCommand> getProcessChanges() {
        LogHelper.logInfo("Elaboration: inserting new additional step " + inputField.getValue() + " into " + subject);

        RecvState newRecvState = null;
        if (availableProvidedMessages.getValue() instanceof Message) {
            Message m = (Message) availableProvidedMessages.getValue();
            newRecvState = new RecvState("Wait for " + m);
            newRecvState.addRecvdMessage(m);
            processChanges.add(new AddStateCommand(subject,instance.getHistoryForSubject(subject).getFirst(),newRecvState,false));
            processChanges.add(new RemoveProvidedMessageCommand(subject, m));
        }
        State newActionState = new ActionState(inputField.getValue());
        if (newRecvState != null) processChanges.add(new AddStateCommand(subject, newRecvState, newActionState,false));
        else processChanges.add(new AddStateCommand(subject, instance.getHistoryForSubject(subject).getFirst(), newActionState,false));
        return processChanges;
    }
}
