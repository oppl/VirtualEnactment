package at.jku.ce.CoMPArE.elaborate.wizardsteps;

import at.jku.ce.CoMPArE.elaborate.changeCommands.ProcessChangeCommand;
import at.jku.ce.CoMPArE.execute.Instance;
import at.jku.ce.CoMPArE.process.Message;
import at.jku.ce.CoMPArE.process.State;
import at.jku.ce.CoMPArE.process.Subject;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import org.vaadin.teemu.wizards.Wizard;

import java.util.List;

/**
 * Created by oppl on 17/12/2016.
 */
public class NeedMoreInputStep extends ElaborationStep {

    final Label questionPrompt;
    final OptionGroup infoSource;
    final TextField inputField;
    final OptionGroup availableProvidedMessages;
    final String optionSpecifyMyself;

    final String optionSomebodyElse;
    final String optionSystem;
    final String optionDontKnow;


    public NeedMoreInputStep(Wizard owner, Subject s, Instance i) {
        super(owner, s, i);
        State state = instance.getAvailableStateForSubject(subject);

        caption = new String("I need more input to do \"" + state + "\".");

        questionPrompt = new Label("I need more input to do \"" + state + "\".");
        infoSource = new OptionGroup("Where could you get it from?");

        inputField = new TextField("Which input would you need?");
        if (subject.getProvidedMessages().size() != 0) {
            inputField.setVisible(false);
            infoSource.setVisible(false);
        }

        inputField.addValueChangeListener(e -> {
            if (infoSource.getValue() != null) {
                if (inputField.getValue().equals("")) setCanAdvance(false);
                else setCanAdvance(true);
            }
        });

        availableProvidedMessages = new OptionGroup("There is some input available, which you currently do not use:");
        for (Message m : subject.getProvidedMessages()) {
            availableProvidedMessages.addItem(m);
        }
        optionSpecifyMyself = new String("I need different input.");
        availableProvidedMessages.addItem(optionSpecifyMyself);
        availableProvidedMessages.addValueChangeListener(e -> {
            Object selectedItem = e.getProperty().getValue();
            if (selectedItem == optionSpecifyMyself) {
                inputField.setVisible(true);
                infoSource.setVisible(true);
                if (inputField.getValue().equals("")) setCanAdvance(false);
            } else {
                inputField.setVisible(false);
                infoSource.setVisible(false);
                setCanAdvance(true);
            }
        });

        for (Subject sub : instance.getProcess().getSubjects())
            if (sub != subject) infoSource.addItem(s);
        optionSomebodyElse = new String("I can get this input from somebody else.");
        optionSystem = new String("I can retrieve this input from a system I have access to.");
        optionDontKnow = new String("I do not know, where I can get this input from");
        infoSource.addItem(optionSomebodyElse);
        infoSource.addItem(optionSystem);
        infoSource.addItem(optionDontKnow);

        infoSource.addValueChangeListener(e -> {
            if ((!subject.getProvidedMessages().isEmpty() && availableProvidedMessages.getValue() != optionSpecifyMyself) || !inputField.getValue().equals(""))
                setCanAdvance(true);
            Object selectedItem = e.getProperty().getValue();
            removeNextSteps();
            ElaborationStep step = null;
            if (selectedItem.equals(optionSystem)) step = new AskForSystemStep(owner, inputField.getValue(), subject, instance);
            if (selectedItem.equals(optionSomebodyElse)) step = new AskForNewRecvSubjectStep(owner, inputField.getValue(), subject, instance);
            addNextStep(step);
        });

        fLayout.addComponent(questionPrompt);
        if (subject.getProvidedMessages().size() != 0) fLayout.addComponent(availableProvidedMessages);
        fLayout.addComponent(inputField);
        fLayout.addComponent(infoSource);
        if (!inputField.isEnabled()) infoSource.setVisible(false);

    }

    @Override
    public List<ProcessChangeCommand> getProcessChanges() {
        if (inputField.isEnabled() && infoSource.getValue() != null) {
            String selection = infoSource.getValue().toString();
            if (selection.equals(optionDontKnow)) {
                //TODO replace with command: Subject source = addAnonymousSubject(instance);
                //TODO replace with command: insertNewReceiveState(inputField.getValue(), source, subject, instance, true);
            }
            if (infoSource.getValue() instanceof Subject) {
                //TODO replace with command: insertNewReceiveState(inputField.getValue(), (Subject) infoSource.getValue(), subject, instance, true);
            }
        } else {
            Message m = (Message) availableProvidedMessages.getValue();
            //TODO replace with command: insertNewReceiveState(m, subject, instance, true);
            //TODO replace with command: subject.removeProvidedMessage(m);
        }
        return processChanges;
    }
}
