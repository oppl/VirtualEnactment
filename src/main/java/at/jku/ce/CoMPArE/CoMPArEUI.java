package at.jku.ce.CoMPArE;

import javax.servlet.annotation.WebServlet;

import at.jku.ce.CoMPArE.elaborate.ElaborationUI;
import at.jku.ce.CoMPArE.execute.Instance;
import at.jku.ce.CoMPArE.process.*;
import at.jku.ce.CoMPArE.process.Process;
import at.jku.ce.CoMPArE.scaffolding.ScaffoldingManager;
import at.jku.ce.CoMPArE.visualize.VisualizationUI;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import org.vaadin.googleanalytics.tracking.GoogleAnalyticsTracker;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This UI is the application entry point. A UI may either represent a browser window 
 * (or tab) or some part of a html page where a Vaadin application is embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is intended to be 
 * overridden to add component to the user interface and initialize non-component functionality.
 */

@Theme("demo")
public class CoMPArEUI extends UI {

    private Map<Subject,Panel> subjectPanels;
    private Panel scaffoldingPanel;
    private VerticalLayout mainLayoutFrame;
    private GridLayout subjectLayout;
    private Process currentProcess;
    private GoogleAnalyticsTracker tracker;
    private ScaffoldingManager scaffoldingManager;
    private boolean initialStartup;

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        initialStartup = true;
        currentProcess = DemoProcess.getComplexDemoProcess();
        tracker = new GoogleAnalyticsTracker("UA-37510687-4","auto");
        tracker.extend(this);
        Instance instance = new Instance(currentProcess);

        scaffoldingPanel = new Panel("What to consider:");
        scaffoldingPanel.setWidth("950px");
        scaffoldingPanel.setHeight("200px");
        scaffoldingPanel.setContent(new HorizontalLayout());

        scaffoldingManager = new ScaffoldingManager(currentProcess,scaffoldingPanel);

        createBasicLayout(currentProcess, instance);
        updateUI(instance);
    }

    private void updateUI(Instance instance) {
        Button differentProcess = new Button("Select different Process");
        differentProcess.addClickListener( e -> {
            selectDifferentProcess();
        });

        for (Subject s: instance.getProcess().getSubjects()) {
            fillSubjectPanel(s,instance);
        }
        if (instance.processFinished()) {
            LogHelper.logInfo("Process finished, offering to restart ...");
            mainLayoutFrame.removeComponent(scaffoldingPanel);
            Button restart = new Button("Restart Process");
            restart.addClickListener( e -> {
                mainLayoutFrame.removeAllComponents();
                createBasicLayout(currentProcess, instance);
                Instance newInstance = new Instance(currentProcess);
                scaffoldingManager.updateScaffolds(instance);
                updateUI(newInstance);
            });
            if (instance.getProcess().getSubjects().size() > 0) mainLayoutFrame.addComponent(restart);
            mainLayoutFrame.addComponent(differentProcess);
        }
        if (initialStartup) {
            mainLayoutFrame.addComponent(differentProcess);
            initialStartup = false;
        }
    }

    private void createBasicLayout(Process process, Instance instance) {
        mainLayoutFrame = new VerticalLayout();
        subjectLayout = new GridLayout(3,process.getSubjects().size()/3+1);

        subjectPanels = new HashMap<>();
        for (Subject s: process.getSubjects()) {
            Panel panel = new Panel(s.toString());
            panel.setWidth("300px");
            panel.setHeight("400px");
            VerticalLayout panelLayout = new VerticalLayout();
            panelLayout.setSpacing(true);
            panelLayout.setMargin(true);
            panel.setContent(panelLayout);
            subjectPanels.put(s,panel);
            subjectLayout.addComponent(panel);
        }
//        subjectLayout.setMargin(true);
        subjectLayout.setSpacing(true);

        Button visualize = new Button ("Show interaction");
        visualize.addClickListener( e -> {
            openVisualizationOverlay(instance);
        });

        Button addInitialSubject = new Button ("Add a first actor");
        addInitialSubject.addClickListener( e -> {
            openElaborationOverlay(null,instance,ElaborationUI.INITALSUBJECT);
        });
        if (process.getSubjects().isEmpty()) mainLayoutFrame.addComponent(addInitialSubject);
        else mainLayoutFrame.addComponent(subjectLayout);
        if (process.getSubjects().size() > 1) mainLayoutFrame.addComponent(visualize);
        mainLayoutFrame.addComponent(scaffoldingPanel);

        mainLayoutFrame.setMargin(true);
        mainLayoutFrame.setSpacing(true);

        setContent(mainLayoutFrame);

    }

    private void fillSubjectPanel(Subject s, Instance instance) {

        VerticalLayout panelContent = (VerticalLayout) subjectPanels.get(s).getContent();
        final OptionGroup nextStates = new OptionGroup("Select one of the following options:");
        panelContent.removeAllComponents();

        Label availableMessageList = new Label("");
        Label processMessageLabel = new Label("");
        Label expectedMessageLabel = new Label("<small>The following messages are expected from"+s+", but are not currently provided:</small>", ContentMode.HTML);
        final ComboBox expectedMessageSelector = new ComboBox("please select:");
        Button expectedMessageSend = new Button("Send");

        Set<Message> availableMessages = instance.getAvailableMessagesForSubject(s);
        if (availableMessages != null && availableMessages.size() > 0) {
            StringBuffer list = new StringBuffer("<small>The following messages are available:<ul>");
            for (Message m: availableMessages) {
                list.append("<li>"+m.toString()+"</li>");
            }
            list.append("</ul></small>");
            availableMessageList = new Label(list.toString(), ContentMode.HTML);
        }
        if (instance.getLatestProcessedMessageForSubject(s) != null) {
            processMessageLabel = new Label("<small>Recently received message:<ul><li>"+instance.getLatestProcessedMessageForSubject(s)+"</li></ul></small>", ContentMode.HTML);
        }

        if (s.getExpectedMessages().size()>0) {
            for (Message m: s.getExpectedMessages()) {
                expectedMessageSelector.addItem(m);
            }
            expectedMessageSelector.setValue(s.getExpectedMessages().iterator().next());
            expectedMessageSend.addClickListener( e -> {
                Message m = (Message) expectedMessageSelector.getValue();
                Subject recipient = instance.getProcess().getRecipientOfMessage(m);
                instance.putMessageInInputbuffer(recipient,m);
                updateUI(instance);
            });
        }

        StringBuffer providedMessages = new StringBuffer();
        if (s.getProvidedMessages().size()>0) {
            providedMessages.append("<small>The following messages are provided to "+s+" but are not currently used:<ul>");
            for (Message m: s.getProvidedMessages()) {
                providedMessages.append("<li>"+m+"</li>");
            }
            providedMessages.append("</ul></small>");
        }
        Label providedMessagesLabel = new Label(providedMessages.toString(),ContentMode.HTML);

        State currentState = instance.getAvailableStateForSubject(s);
        if (currentState != null) {
            Label label1 = new Label(currentState.toString());
            panelContent.addComponent(label1);

            Set<State> nextPossibleSteps = instance.getNextStatesOfSubject(s);
            if (nextPossibleSteps != null && nextPossibleSteps.size()>0) {
                if (nextPossibleSteps.size() == 1) {
                    State nextState = nextPossibleSteps.iterator().next();
                    if (instance.getConditionForStateInSubject(s, nextState) != Condition.noCondition) {
                        Label label2 = new Label("You can only progress under the following condition: <br>"+instance.getConditionForStateInSubject(s,nextState));
                        panelContent.addComponent(label2);
                    }
                }
                else {
                    if (instance.subjectCanProgress(s)) {
                        boolean toBeShown = false;
                        for (State nextState : nextPossibleSteps) {
                            Condition condition = instance.getConditionForStateInSubject(s, nextState);
                            nextStates.addItem(condition);
                            if (!(condition instanceof MessageCondition)) toBeShown = true;
                        }
                        nextStates.addValueChangeListener(event -> {
                            LogHelper.logInfo("UI: condition for subject " + s + " changed to " + event.getProperty().getValue());
                        });
                        if (toBeShown) panelContent.addComponent(nextStates);
                    }
                }
            }

        }
        else {
            Label label1 = new Label("nothing to do");
            if (!(s.toString().equals(Subject.ANONYMOUS))) panelContent.addComponent(label1);
        }

        Button perform = new Button("Perform step");
        perform.addClickListener( e -> {
            Condition c = null;
            if (nextStates.size() > 0) c = (Condition) nextStates.getValue();
            instance.advanceStateForSubject(s, c);
            scaffoldingManager.updateScaffolds(instance,currentState);
            updateUI(instance);
        });

        Button elaborate = new Button("I have a problem here");
        elaborate.addClickListener( e -> {
            perform.setEnabled(false);
            elaborate.setEnabled(false);
            openElaborationOverlay(s,instance,ElaborationUI.ELABORATE);
        });

        perform.setEnabled(instance.subjectCanProgress(s));

        Button visualize = new Button("Show behaviour");
        visualize.addClickListener( e -> {
            openVisualizationOverlay(s, instance);
        });

        Button addInitialStep = new Button("Add an initial step");
        addInitialStep.addClickListener( e -> {
            openElaborationOverlay(s, instance,ElaborationUI.INITIALSTEP);

        });

        Button addAdditionStep = new Button("Add an additional step");
        addAdditionStep.addClickListener( e -> {
            openElaborationOverlay(s, instance, ElaborationUI.ADDITIONALSTEP);
        });

        if (!(s.toString().equals(Subject.ANONYMOUS))) panelContent.addComponent(perform);
        if (instance.subjectCanProgress(s)) panelContent.addComponent(elaborate);
        if (!availableMessages.isEmpty()) panelContent.addComponent(availableMessageList);
        if (!processMessageLabel.getValue().equals("")) panelContent.addComponent(processMessageLabel);
        if (s.getExpectedMessages().size()>0) {
            panelContent.addComponents(expectedMessageLabel, expectedMessageSelector, expectedMessageSend);
        }
        if (s.getProvidedMessages().size()>0) {
            panelContent.addComponent(providedMessagesLabel);
        }
        if (s.getFirstState() == null && !s.toString().equals(Subject.ANONYMOUS)) panelContent.addComponent(addInitialStep);
        if (instance.subjectFinished(s) && s.getFirstState() != null) panelContent.addComponent(addAdditionStep);
        if (!s.toString().equals(Subject.ANONYMOUS)) panelContent.addComponent(visualize);
    }

    private void openElaborationOverlay(Subject s, Instance instance, int mode) {
        ElaborationUI elaborationUI = new ElaborationUI();
        getUI().addWindow(elaborationUI);

        if (mode == ElaborationUI.ELABORATE) elaborationUI.elaborate(s, instance);
        if (mode == ElaborationUI.INITIALSTEP) elaborationUI.initialStep(s, instance);
        if (mode == ElaborationUI.ADDITIONALSTEP) elaborationUI.additionalStep(s, instance);
        if (mode == ElaborationUI.INITALSUBJECT) elaborationUI.initialSubject(instance);

        elaborationUI.addCloseListener(new Window.CloseListener() {
            @Override
            public void windowClose(Window.CloseEvent e) {
                createBasicLayout(currentProcess, instance);
                scaffoldingManager.updateScaffolds(instance,instance.getAvailableStateForSubject(s));
                updateUI(instance);
            }
        });

    }

    private void openVisualizationOverlay(Instance instance) {
        VisualizationUI visualizationUI = new VisualizationUI(instance);
        getUI().addWindow(visualizationUI);
        visualizationUI.showSubjectInteraction();
        visualizationUI.addCloseListener(new Window.CloseListener() {
            @Override
            public void windowClose(Window.CloseEvent e) {
                createBasicLayout(currentProcess, instance);
                updateUI(instance);
            }
        });

    }

    private void openVisualizationOverlay(Subject s, Instance instance) {
        VisualizationUI visualizationUI = new VisualizationUI(instance);
        getUI().addWindow(visualizationUI);
        visualizationUI.showSubjectProgress(s);
        visualizationUI.addCloseListener(new Window.CloseListener() {
            @Override
            public void windowClose(Window.CloseEvent e) {
                createBasicLayout(currentProcess, instance);
                updateUI(instance);
            }
        });

    }

    private void selectDifferentProcess() {
        ProcessSelectorUI processSelectorUI = new ProcessSelectorUI();
        getUI().addWindow(processSelectorUI);
        processSelectorUI.showProcessSelector();
        processSelectorUI.addCloseListener(new Window.CloseListener() {
            @Override
            public void windowClose(Window.CloseEvent e) {
                Process newProcess = processSelectorUI.getSelectedProcess();
                if (newProcess != null) {
                    currentProcess = newProcess;
                    Instance instance = new Instance(currentProcess);
                    createBasicLayout(currentProcess, instance);
                    updateUI(instance);
                }
            }
        });

    }

    @WebServlet(urlPatterns = "/*", name = "CoMPArEServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = CoMPArEUI.class, productionMode = false)
    public static class CoMPArEServlet extends VaadinServlet {
    }
}
