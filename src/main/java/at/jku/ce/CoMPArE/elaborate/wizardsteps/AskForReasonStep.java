package at.jku.ce.CoMPArE.elaborate.wizardsteps;

import at.jku.ce.CoMPArE.execute.Instance;
import at.jku.ce.CoMPArE.process.State;
import at.jku.ce.CoMPArE.process.Subject;
import com.vaadin.ui.*;
import org.vaadin.teemu.wizards.Wizard;

/**
 * Created by oppl on 16/12/2016.
 */
public class AskForReasonStep extends ElaborationStep {

    public AskForReasonStep(Wizard owner, Subject s, Instance i) {
        super(owner, s, i);
        State state = instance.getAvailableStateForSubject(subject);
        caption = new String("What is the problem with \"" + state + "\"?");

        final Label questionPrompt = new Label("What is the problem with \"" + state + "\"?");

        final OptionGroup answerOptions = new OptionGroup("Please select:");
        final String option1 = new String("It can't be done at the moment.");
        final String option2 = new String("I rather need to do something else instead.");
        final String option3 = new String("It's too vague to be performed.");
        final String option4 = new String("It's incorrect.");

        answerOptions.addItem(option1);
        answerOptions.addItem(option2);
        answerOptions.addItem(option3);
        answerOptions.addItem(option4);
        answerOptions.addValueChangeListener(e -> {
            canAdvance = true;
            String selection = (String) answerOptions.getValue();
            if (selection != null) {
                if (nextStep != null) owner.removeStep(nextStep);
                if (selection.equals(option1)) nextStep = new CantBeDoneStep(owner, subject, instance);
                if (selection.equals(option2)) nextStep = null;//somethingElseInstead(subject, instance);
                if (selection.equals(option3)) nextStep = new TooVagueStep(owner, subject, instance);
                if (selection.equals(option4)) nextStep = null;//removeIncorrectState(subject, instance);
                if (nextStep != null) owner.addStep(nextStep);
            }
        });

        fLayout.addComponent(questionPrompt);
        fLayout.addComponent(answerOptions);
    }

}
