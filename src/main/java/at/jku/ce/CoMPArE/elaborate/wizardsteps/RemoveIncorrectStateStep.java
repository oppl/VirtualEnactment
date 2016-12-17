package at.jku.ce.CoMPArE.elaborate.wizardsteps;

import at.jku.ce.CoMPArE.elaborate.changeCommands.ProcessChangeCommand;
import at.jku.ce.CoMPArE.execute.Instance;
import at.jku.ce.CoMPArE.process.State;
import at.jku.ce.CoMPArE.process.Subject;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import org.vaadin.teemu.wizards.Wizard;

import java.util.List;

/**
 * Created by oppl on 17/12/2016.
 */
public class RemoveIncorrectStateStep extends ElaborationStep {

    final OptionGroup answerOptions;
    final String option1;
    final String option2;

    public RemoveIncorrectStateStep(Wizard owner, Subject s, Instance i) {
        super(owner, s, i);
        State state = instance.getAvailableStateForSubject(subject);
        caption = new String("\"" + state + "\" is incorrect.");

        final Label questionPrompt = new Label("\"" + state + "\" is incorrect.");

        answerOptions = new OptionGroup("How can this be corrected?");
        option1 = new String("Simply remove \"" + state + "\".");
        option2 = new String("Replace \"" + state + "\" with something else.");

        answerOptions.addItem(option1);
        answerOptions.addItem(option2);

        answerOptions.addValueChangeListener(e -> {
            setCanAdvance(true);
            String selection = (String) answerOptions.getValue();
            if (selection != null) {
                removeNextSteps();
                ElaborationStep step = null;
                if (selection.equals(option2)) step = new ReplaceIncorrectStateStep(owner, subject, instance);
                addNextStep(step);
            }

        });

        fLayout.addComponent(questionPrompt);
        fLayout.addComponent(answerOptions);

    }

    @Override
    public List<ProcessChangeCommand> getProcessChanges() {
        String selection = (String) answerOptions.getValue();
        if (selection.equals(option1)) {
            //TODO: add DeleteStateCommand
        }
        return processChanges;
    }
}
