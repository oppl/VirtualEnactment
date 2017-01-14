package at.jku.ce.CoMPArE.elaborate.wizardsteps;

import at.jku.ce.CoMPArE.elaborate.changeCommands.*;
import at.jku.ce.CoMPArE.execute.Instance;
import at.jku.ce.CoMPArE.process.*;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import org.vaadin.teemu.wizards.Wizard;

import java.util.List;

/**
 * Created by oppl on 17/12/2016.
 */
public class AskForNewSendSubjectStep extends ElaborationStep {

    State state;

    final Label questionPrompt;
    final TextField inputField;
    final String messageName;

    public AskForNewSendSubjectStep(Wizard owner, String input, Subject s, Instance i) {
        super(owner, s, i);

        state = instance.getAvailableStateForSubject(subject);

        caption = new String("I can provide this input to somebody else.");
        questionPrompt = new Label("I can provide this input to somebody else.");
        inputField = new TextField("Whom can you provide this input with?");
        messageName = input;

        inputField.addValueChangeListener(e -> {
            if (inputField.getValue().equals("")) setCanAdvance(false);
            else setCanAdvance(true);
        });

        fLayout.addComponent(questionPrompt);
        fLayout.addComponent(inputField);
    }

    @Override
    public List<ProcessChangeCommand> getProcessChanges() {
        state = instance.getAvailableStateForSubject(subject);
        Subject newSubject = new Subject(inputField.getValue(),instance.getProcess());
        processChanges.add(new AddSubjectCommand(instance.getProcess(),newSubject,instance));
        SendState newState = new SendState("Send " + messageName,subject);
        Message newMessage = new Message(messageName);
        newState.setSentMessage(newMessage);
        processChanges.add(new AddStateCommand(subject,state,newState,false));
        processChanges.add(new AddProvidedMessageCommand(newSubject,newMessage));

        return super.getProcessChanges();
    }
}
