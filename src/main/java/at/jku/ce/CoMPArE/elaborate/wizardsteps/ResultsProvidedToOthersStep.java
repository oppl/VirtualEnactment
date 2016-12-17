package at.jku.ce.CoMPArE.elaborate.wizardsteps;

import at.jku.ce.CoMPArE.elaborate.changeCommands.ProcessChangeCommand;
import at.jku.ce.CoMPArE.execute.Instance;
import at.jku.ce.CoMPArE.process.Message;
import at.jku.ce.CoMPArE.process.Subject;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import org.vaadin.teemu.wizards.Wizard;

import java.util.List;

/**
 * Created by oppl on 17/12/2016.
 */
public class ResultsProvidedToOthersStep extends ElaborationStep {

    String newState;

    Label questionPrompt;
    final TextField inputField;
    final OptionGroup infoTarget;
    final OptionGroup availableExpectedMessages;
    final String optionSpecifyMyself;
    final String optionSomebodyElse;
    final String optionDontKnow;


    public ResultsProvidedToOthersStep(Wizard owner, String newState, Subject s, Instance i) {
        super(owner, s, i);
        this.newState = newState;
        caption = new String("\"" + newState + "\" leads to results I can provide to others.");
        questionPrompt = new Label("\"" + newState + "\" leads to results I can provide to others.");
        inputField = new TextField("What can you provide to others?");
        infoTarget = new OptionGroup("Whom do you provide it to?");

        inputField.addValueChangeListener(e -> {
            if (infoTarget.getValue() != null) {
                if (inputField.getValue().equals("")) setCanAdvance(false);
                else setCanAdvance(true);
            }
        });

        availableExpectedMessages = new OptionGroup("There are some expected results, which you currently do not provide:");

        if (subject.getExpectedMessages().size() != 0) {
            inputField.setVisible(false);
            infoTarget.setVisible(false);
        }

        for (Message m : subject.getExpectedMessages()) {
            availableExpectedMessages.addItem(m);
        }
        optionSpecifyMyself = new String("I can provide other results.");
        availableExpectedMessages.addItem(optionSpecifyMyself);

        availableExpectedMessages.addValueChangeListener(e -> {
            Object selectedItem = e.getProperty().getValue();
            if (selectedItem == optionSpecifyMyself) {
                inputField.setVisible(true);
                infoTarget.setVisible(true);
                if (inputField.getValue().equals("")) setCanAdvance(false);
            } else {
                inputField.setVisible(false);
                infoTarget.setVisible(false);
                setCanAdvance(true);
            }
        });

        for (Subject sub : instance.getProcess().getSubjects())
            if (sub != subject) infoTarget.addItem(sub);
        optionSomebodyElse = new String("Somebody else.");
        optionDontKnow = new String("I do not know who could be interested");
        infoTarget.addItem(optionSomebodyElse);
        infoTarget.addItem(optionDontKnow);

        infoTarget.addValueChangeListener(e -> {
            if ((!subject.getExpectedMessages().isEmpty() && availableExpectedMessages.getValue() != optionSpecifyMyself) || !inputField.getValue().equals(""))
                setCanAdvance(true);
            Object selectedItem = e.getProperty().getValue();
            removeNextSteps();
            ElaborationStep step = null;
            if (selectedItem.equals(optionSomebodyElse)) step = new AskForNewSendSubjectStep(owner, inputField.getValue(), subject, instance);
            addNextStep(step);
        });

        fLayout.addComponent(questionPrompt);
        if (subject.getExpectedMessages().size() != 0) fLayout.addComponent(availableExpectedMessages);
        fLayout.addComponent(inputField);
        if (inputField.isEnabled()) fLayout.addComponent(infoTarget);

    }

    @Override
    public List<ProcessChangeCommand> getProcessChanges() {
        if (inputField.isEnabled() && infoTarget.getValue() != null) {
            String selection = infoTarget.getValue().toString();
            if (selection.equals(optionDontKnow)) {
                //TODO create according command: Subject target = addAnonymousSubject(instance);
                //TODO create according command: insertNewSendState(inputField.getValue(), target, subject, instance, false);
            }
            if (infoTarget.getValue() instanceof Subject) {
                //TODO create according command: insertNewSendState(inputField.getValue(), (Subject) infoTarget.getValue(), subject, instance, false);
            }
        } else {
            Message m = (Message) availableExpectedMessages.getValue();
            // TODO create according command: insertNewSendStateAndRemoveExpectedMessage(m, subject, instance, false);
        }
        return processChanges;
    }

    public void updateNameOfState(String newState) {
        this.newState = newState;
        caption = new String("\"" + newState + "\" leads to results I can provide to others.");
        questionPrompt = new Label("\"" + newState + "\" leads to results I can provide to others.");

    }
}
