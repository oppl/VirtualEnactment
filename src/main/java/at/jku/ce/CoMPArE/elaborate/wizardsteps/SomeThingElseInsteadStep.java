package at.jku.ce.CoMPArE.elaborate.wizardsteps;

import at.jku.ce.CoMPArE.LogHelper;
import at.jku.ce.CoMPArE.elaborate.changeCommands.AddStateChange;
import at.jku.ce.CoMPArE.elaborate.changeCommands.ProcessChange;
import at.jku.ce.CoMPArE.execute.Instance;
import at.jku.ce.CoMPArE.process.ActionState;
import at.jku.ce.CoMPArE.process.State;
import at.jku.ce.CoMPArE.process.Subject;
import at.jku.ce.CoMPArE.visualize.VisualizationUI;
import com.vaadin.ui.*;
import org.vaadin.teemu.wizards.Wizard;

import java.util.List;

/**
 * Created by oppl on 17/12/2016.
 */
public class SomeThingElseInsteadStep extends ElaborationStep {

    State state;
    final Label questionPrompt;
    final TextField inputField;
    final CheckBox newMessage;
    final OptionGroup relationship;
    final String optionConditionalReplace;
    final String optionAdditionalActivity;

    ElaborationStep newMessageStep = null;
    ElaborationStep specifyConditionsStep = null;

    public SomeThingElseInsteadStep(Wizard owner, Subject s, Instance i) {
        super(owner, s, i);
        state = instance.getAvailableStateForSubject(subject);
        caption = new String("I need to do something else instead of \"" + state + "\".");

        questionPrompt = new Label("I need to do something else instead of \"" + state + "\".");
        inputField = new TextField("What do you need to do?");
        newMessage = new CheckBox("This step leads to results I can provide to others.");
        relationship = new OptionGroup("How does this relate to \"" + state + "\"?");
        optionConditionalReplace = new String("It replaces \"" + state + "\" under certain conditions.");
        optionAdditionalActivity = new String("It is complementary to \"" + state + "\", I still need to do \"" + state + "\", too.");

        inputField.addValueChangeListener(e -> {
            if (relationship.getValue() != null) {
                if (inputField.getValue().equals("")) setCanAdvance(false);
                else setCanAdvance(true);
            }
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
                    relationship.setValue(optionConditionalReplace);
                    relationship.setEnabled(false);
                    newMessage.setDescription("Existing steps can only be inserted as alternatives to the current step.");
                }
            });
        });

        inputField.addValueChangeListener(e -> {
            if (instance.getProcess().getStateWithName(inputField.getValue()) == null) {
                newMessage.setVisible(true);
                newMessage.setDescription("");
                relationship.setEnabled(true);
                relationship.setDescription("");
            }
        });

        relationship.addItem(optionConditionalReplace);
        relationship.addItem(optionAdditionalActivity);

        relationship.addValueChangeListener(e -> {
            if (!inputField.getValue().equals("")) setCanAdvance(true);
            Object selectedItem = e.getProperty().getValue();

            if (selectedItem == optionConditionalReplace) {
                specifyConditionsStep = new AskForConditionsStep(owner, inputField.getValue(), subject, instance);
                addNextStep(specifyConditionsStep);
            }
            else {
                removeParticularFollowingStep(specifyConditionsStep);
                specifyConditionsStep = null;
            }
        });

        newMessage.addValueChangeListener(e -> {
            Boolean value = (Boolean) e.getProperty().getValue();
            if (value == Boolean.TRUE) {
                newMessageStep = new AddNewResultsProvidedToOthersStep(owner, subject, instance);
                addNextStep(newMessageStep);
            }
            else {
                removeParticularFollowingStep(newMessageStep);
                newMessageStep = null;
            }

        });

        fLayout.addComponent(questionPrompt);
        fLayout.addComponent(inputField);
        fLayout.addComponent(selectFromExisting);
        fLayout.addComponent(newMessage);
        fLayout.addComponent(relationship);

    }

    @Override
    public List<ProcessChange> getProcessChanges() {
        String selection = relationship.getValue().toString();

        if (selection.equals(optionAdditionalActivity)) {
            processChanges.add(new AddStateChange(subject,state, new ActionState(inputField.getValue()),true));
        }

        return processChanges;
    }
}
