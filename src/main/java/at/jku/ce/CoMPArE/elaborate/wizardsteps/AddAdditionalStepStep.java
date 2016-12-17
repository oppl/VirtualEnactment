package at.jku.ce.CoMPArE.elaborate.wizardsteps;

import at.jku.ce.CoMPArE.LogHelper;
import at.jku.ce.CoMPArE.elaborate.changeCommands.AddStateCommand;
import at.jku.ce.CoMPArE.elaborate.changeCommands.ProcessChangeCommand;
import at.jku.ce.CoMPArE.execute.Instance;
import at.jku.ce.CoMPArE.process.ActionState;
import at.jku.ce.CoMPArE.process.State;
import at.jku.ce.CoMPArE.process.Subject;
import at.jku.ce.CoMPArE.visualize.VisualizationUI;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import org.vaadin.teemu.wizards.Wizard;

import java.util.List;

/**
 * Created by oppl on 17/12/2016.
 */
public class AddAdditionalStepStep extends ElaborationStep {

    State state;
    final Label questionPrompt;
    final TextField inputField;
    final CheckBox newMessage;

    public AddAdditionalStepStep(Wizard owner, Subject s, Instance i) {
        super(owner, s, i);
        state = instance.getAvailableStateForSubject(s);
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


        fLayout.addComponent(questionPrompt);
        fLayout.addComponent(inputField);
        fLayout.addComponent(selectFromExisting);
        fLayout.addComponent(newMessage);
    }

    @Override
    public List<ProcessChangeCommand> getProcessChanges() {
        LogHelper.logInfo("Elaboration: inserting new additional step " + inputField.getValue() + " into " + subject);

        //TODO: insert correct command: State newState = insertNewActionState(inputField.getValue(), subject, instance, false);
        return processChanges;
    }
}
