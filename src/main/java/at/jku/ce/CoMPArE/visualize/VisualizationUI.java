package at.jku.ce.CoMPArE.visualize;

import at.jku.ce.CoMPArE.execute.Instance;
import at.jku.ce.CoMPArE.process.State;
import at.jku.ce.CoMPArE.process.Subject;
import com.vaadin.ui.*;

/**
 * Created by oppl on 24/11/2016.
 */
public class VisualizationUI extends Window {

    private VizualizeModel view;

    private Instance instance;

    public VisualizationUI(String name) {
        super("Visualization of Process Progress");
        this.setWidth("900px");
        this.setHeight("500px");
        this.center();
//        VerticalLayout content = new VerticalLayout();
        view = new VizualizeModel(this, name);
        setContent(view);
        instance = null;
    }

    public VisualizationUI(Instance instance, String name) {
        this(name);
        this.instance = instance;
 //       view.showSubject(instance.getProcess().getSubjects().iterator().next());
    }

    public void showSubject(Subject s) {
        view.showSubject(s);
    }

    public void showSubjectProgress(Subject subject) {
        view.showSubject(subject);
        view.greyOutCompletedStates(instance.getHistoryForSubject(subject),instance.getAvailableStateForSubject(subject));
    }

    public void showSubjectInteraction() {
        view.showSubjectInteraction(instance.getProcess());
    }

    public void activateSelectionMode() {
        view.setSelectionMode(true);
    }

    public State getSelectedState() {
        String selectedStateName = view.getSelectedNodeName();
        if (selectedStateName == null) return null;
        return instance.getProcess().getStateWithName(selectedStateName);
    }

    public Subject getSelectedSubject() {
        String selectedSubjectName = view.getSelectedNodeName();
        if (selectedSubjectName == null) return null;
        return instance.getProcess().getSubjectWithName(selectedSubjectName);
    }

}
