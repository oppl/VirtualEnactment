package at.jku.ce.CoMPArE.elaborate.wizardsteps;

import at.jku.ce.CoMPArE.LogHelper;
import at.jku.ce.CoMPArE.elaborate.changeCommands.ProcessChange;
import at.jku.ce.CoMPArE.elaborate.changeCommands.ReplaceStateChange;
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
public class ReplaceIncorrectStateStep extends ElaborationStep {

    State state;
    final Label questionPrompt;
    final TextField inputField;
    final CheckBox newMessage;


    public ReplaceIncorrectStateStep(Wizard owner, Subject s, Instance i) {
        super(owner, s, i);
        state = instance.getAvailableStateForSubject(subject);
        caption = new String("I want to replace \"" + state + "\" with something else.");

        questionPrompt = new Label("I want to replace \"" + state + "\" with something else.");
        inputField = new TextField("What is the new activity?");
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
            ElaborationStep step = null;
            if (value == Boolean.TRUE) step = new AddNewResultsProvidedToOthersStep(owner, subject, instance);
            addNextStep(step);
        });

        fLayout.addComponent(questionPrompt);
        fLayout.addComponent(inputField);
        fLayout.addComponent(selectFromExisting);
        fLayout.addComponent(newMessage);

    }

    @Override
    public List<ProcessChange> getProcessChanges() {
        if (state != null) {
            State newState = new ActionState(inputField.getValue());
            processChanges.add(new ReplaceStateChange(subject, state, newState));
        }
        return processChanges;
    }
}
