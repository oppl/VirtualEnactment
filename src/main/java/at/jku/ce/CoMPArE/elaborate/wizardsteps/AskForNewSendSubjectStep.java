package at.jku.ce.CoMPArE.elaborate.wizardsteps;

import at.jku.ce.CoMPArE.elaborate.changeCommands.ProcessChangeCommand;
import at.jku.ce.CoMPArE.execute.Instance;
import at.jku.ce.CoMPArE.process.Subject;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import org.vaadin.teemu.wizards.Wizard;

import java.util.List;

/**
 * Created by oppl on 17/12/2016.
 */
public class AskForNewSendSubjectStep extends ElaborationStep {

    final Label questionPrompt;
    final TextField inputField;
    final String newMessage;

    public AskForNewSendSubjectStep(Wizard owner, String input, Subject s, Instance i) {
        super(owner, s, i);

        caption = new String("I can provide this input to somebody else.");
        questionPrompt = new Label("I can provide this input to somebody else.");
        inputField = new TextField("Whom can you provide this input with?");
        newMessage = input;

        inputField.addValueChangeListener(e -> {
            if (inputField.getValue().equals("")) setCanAdvance(false);
            else setCanAdvance(true);
        });

        fLayout.addComponent(questionPrompt);
        fLayout.addComponent(inputField);
    }

    @Override
    public List<ProcessChangeCommand> getProcessChanges() {
        Subject newSubject = new Subject(inputField.getValue());
        //TODO: create according Command: insertNewSubject(newSubject, instance);
        //TODO: create according Command: insertNewSendState(newMessage, newSubject, subject, instance, false);

        return super.getProcessChanges();
    }
}
