package at.jku.ce.CoMPArE.elaborate.wizardsteps;

import at.jku.ce.CoMPArE.elaborate.changeCommands.ProcessChange;
import at.jku.ce.CoMPArE.elaborate.changeCommands.ReplaceStateChange;
import at.jku.ce.CoMPArE.execute.Instance;
import at.jku.ce.CoMPArE.process.ActionState;
import at.jku.ce.CoMPArE.process.State;
import at.jku.ce.CoMPArE.process.Subject;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import org.vaadin.teemu.wizards.Wizard;

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
            if (inputField.getValue().equals("")) canAdvance = false;
            else canAdvance = true;
        });

        newMessage.addValueChangeListener(e -> {
            Boolean value = (Boolean) e.getProperty().getValue();
            if (value == Boolean.TRUE) ; // addNewResultsProvidedToOthersStep
        });

        fLayout.removeAllComponents();
        fLayout.addComponent(questionPrompt);
        fLayout.addComponent(inputField);
        fLayout.addComponent(newMessage);

    }

    @Override
    public ProcessChange getProcessChange() {
        State newState = new ActionState(inputField.getValue());
        return new ReplaceStateChange(subject, instance.getAvailableStateForSubject(subject),newState);
    }
}
