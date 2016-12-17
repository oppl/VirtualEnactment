package at.jku.ce.CoMPArE.elaborate.wizardsteps;

import at.jku.ce.CoMPArE.elaborate.changeCommands.ProcessChange;
import at.jku.ce.CoMPArE.elaborate.changeCommands.ReplaceStateChange;
import at.jku.ce.CoMPArE.execute.Instance;
import at.jku.ce.CoMPArE.process.ActionState;
import at.jku.ce.CoMPArE.process.State;
import at.jku.ce.CoMPArE.process.Subject;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import org.vaadin.teemu.wizards.Wizard;

import java.util.List;

/**
 * Created by oppl on 16/12/2016.
 */
public class TooVagueStep extends ElaborationStep {

    Label questionPrompt;
    TextField inputField;
    CheckBox newMessage;

    public TooVagueStep(Wizard owner, Subject s, Instance i) {
        super(owner, s, i);
        State state = instance.getAvailableStateForSubject(subject);
        caption = new String("\"" + state + "\" is too vague.");

        questionPrompt = new Label("\"" + state + "\" is too vague.");
        inputField = new TextField("What would be the first activity you need to do when refining \"" + state + "\"?");
        newMessage = new CheckBox("This activity leads to results I can provide to others.");

        inputField.addValueChangeListener(e -> {
            if (inputField.getValue().equals("")) setCanAdvance(false);
            else setCanAdvance(true);
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
        fLayout.addComponent(newMessage);

    }

    @Override
    public List<ProcessChange> getProcessChanges() {
        State newState = new ActionState(inputField.getValue());
        processChanges.add(new ReplaceStateChange(subject, instance.getAvailableStateForSubject(subject),newState));
        return processChanges;
    }
}
