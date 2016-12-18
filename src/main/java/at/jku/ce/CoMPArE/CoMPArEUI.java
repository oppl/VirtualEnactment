package at.jku.ce.CoMPArE;

import javax.servlet.annotation.WebServlet;

import at.jku.ce.CoMPArE.elaborate.ElaborationUI;
import at.jku.ce.CoMPArE.elaborate.ProcessChangeHistory;
import at.jku.ce.CoMPArE.elaborate.StateClickListener;
import at.jku.ce.CoMPArE.execute.Instance;
import at.jku.ce.CoMPArE.process.*;
import at.jku.ce.CoMPArE.process.Process;
import at.jku.ce.CoMPArE.scaffolding.ScaffoldingManager;
import at.jku.ce.CoMPArE.simulate.Simulator;
import at.jku.ce.CoMPArE.storage.XMLStore;
import at.jku.ce.CoMPArE.visualize.VizualizeModel;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.util.filter.Not;
import com.vaadin.server.*;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import org.vaadin.googleanalytics.tracking.GoogleAnalyticsTracker;
import org.vaadin.sliderpanel.SliderPanel;
import org.vaadin.sliderpanel.SliderPanelBuilder;
import org.vaadin.sliderpanel.SliderPanelStyles;
import org.vaadin.sliderpanel.client.SliderMode;
import org.vaadin.sliderpanel.client.SliderPanelListener;
import org.vaadin.sliderpanel.client.SliderTabPosition;

import java.io.File;
import java.util.*;

/**
 * This UI is the application entry point. A UI may either represent a browser window 
 * (or tab) or some part of a html page where a Vaadin application is embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is intended to be 
 * overridden to add component to the user interface and initialize non-component functionality.
 */

@Theme("demo")
@Push
public class CoMPArEUI extends UI implements SliderPanelListener {

    private Map<Subject,Panel> subjectPanels;
    private Panel scaffoldingPanel;
    private HorizontalLayout mainLayoutFrame;
    private HorizontalLayout toolBar;
    private VerticalLayout mainInteractionArea;
    private GridLayout subjectLayout;
    private TabSheet visualizationTabs;
    private SliderPanel visualizationSlider;

    private Process currentProcess;
    private Instance currentInstance;


    private GoogleAnalyticsTracker tracker;
    private ScaffoldingManager scaffoldingManager;
    private Simulator simulator;
    private StateClickListener stateClickListener;
    private boolean initialStartup;
    private boolean selectionMode;

    private ProcessChangeHistory processChangeHistory;
    private Subject lastActiveSubject;


    private Button differentProcess;
    private Button simulate;
    private Button restart;

    private long id;

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        id = UUID.randomUUID().getLeastSignificantBits();
        initialStartup = true;
        selectionMode = false;
        lastActiveSubject = null;
        stateClickListener = null;
        currentProcess = DemoProcess.getComplexDemoProcess();
        processChangeHistory = new ProcessChangeHistory();
        tracker = new GoogleAnalyticsTracker("UA-37510687-4","auto");
        tracker.extend(this);
        currentInstance = new Instance(currentProcess);

        scaffoldingPanel = new Panel("What to consider:");
        scaffoldingPanel.setWidth("950px");
        scaffoldingPanel.setHeight("200px");
        scaffoldingPanel.setContent(new HorizontalLayout());

        scaffoldingManager = new ScaffoldingManager(currentProcess,scaffoldingPanel);

        differentProcess = new Button("Select different Process");
        differentProcess.addClickListener( e -> {
            selectDifferentProcess();
        });

        restart = new Button("Restart Process");

        createBasicLayout();
        simulator = new Simulator(currentInstance,subjectPanels,this);

        updateUI();

        Page.getCurrent().addBrowserWindowResizeListener(e -> {
            recalculateSubjectLayout(e.getWidth());
        });

    }


    private void createBasicLayout() {

        LogHelper.logInfo("Building basic layout");
        mainLayoutFrame = new HorizontalLayout();

//        SliderPanel scaffoldingSlider = createScaffoldingSlider(process, instance);
        visualizationSlider = createVisualizationSlider();

        mainInteractionArea = new VerticalLayout();

        HorizontalLayout toolBar = createToolbar();
        Component subjects = createSubjectLayout(Page.getCurrent().getBrowserWindowWidth());

        mainInteractionArea.addComponent(subjects);
        mainInteractionArea.addComponent(toolBar);
        mainInteractionArea.addComponent(scaffoldingPanel);
        mainInteractionArea.setMargin(true);
        mainInteractionArea.setSpacing(true);


        VerticalLayout padding = new VerticalLayout();
        padding.setWidth("50px");
        padding.setHeight("100%");


//        mainLayoutFrame.addComponent(scaffoldingSlider);
        mainLayoutFrame.addComponent(visualizationSlider);
        mainLayoutFrame.addComponent(padding);
        mainLayoutFrame.addComponent(mainInteractionArea);

        this.setContent(mainLayoutFrame);

    }

    private SliderPanel createScaffoldingSlider(Process process, Instance instance) {
        return new SliderPanelBuilder(scaffoldingPanel, "What to consider").mode(SliderMode.LEFT)
                        .tabPosition(SliderTabPosition.BEGINNING).style(SliderPanelStyles.COLOR_WHITE).flowInContent(true).animationDuration(500).build();

    }

    private SliderPanel createVisualizationSlider() {
        VerticalLayout visualizationSliderContent = new VerticalLayout();
        visualizationSliderContent.setWidth("950px");
        visualizationSliderContent.setHeight("600px");
        visualizationSliderContent.setMargin(true);
        visualizationSliderContent.setSpacing(true);

        visualizationTabs = new TabSheet();
        visualizationSliderContent.addComponent(visualizationTabs);
        for (Subject s: currentProcess.getSubjects()) {
            VerticalLayout subjectVizualization = new VerticalLayout();
            subjectVizualization.setCaption(s.toString());
            visualizationTabs.addTab(subjectVizualization,s.toString());
        }
        VerticalLayout interaction = new VerticalLayout();
        interaction.setCaption("Interaction");
        visualizationTabs.addTab(interaction, "Interaction");
        visualizationTabs.addSelectedTabChangeListener( e -> {
            String selected = e.getTabSheet().getSelectedTab().getCaption();
            LogHelper.logInfo("Now processing visualizationTab "+selected);
            if (selected != null) {
                VerticalLayout vl = (VerticalLayout) e.getTabSheet().getSelectedTab();
                vl.removeAllComponents();
//                vl.addComponent(new MoreComplexDemoView());

                VizualizeModel vizualizeModel = new VizualizeModel(selected, this);
                vizualizeModel.setCaption(selected);
                if (selected.equals("Interaction")) {
                    vizualizeModel.showSubjectInteraction(currentProcess);
                }
                else {
                    Subject s = currentProcess.getSubjectWithName(selected);
                    vizualizeModel.showSubject(s);
                    vizualizeModel.greyOutCompletedStates(currentInstance.getHistoryForSubject(s),currentInstance.getAvailableStateForSubject(s));
                }
                vl.addComponent(vizualizeModel);
            }
        });

        final SliderPanel visualizationSlider =
                new SliderPanelBuilder(visualizationSliderContent, "Show behaviour").mode(SliderMode.LEFT)
                        .tabPosition(SliderTabPosition.BEGINNING).style(SliderPanelStyles.COLOR_WHITE).flowInContent(true).animationDuration(500).build();

        visualizationSlider.addListener(this);
        return visualizationSlider;
    }

    @Override
    public void onToggle(boolean b) {
        if (b) {
            String toBeActivated = null;
            Set<Subject> candidates = new HashSet<>();
            for (Subject s: subjectPanels.keySet()) {
                if (currentInstance.subjectCanProgress(s)) candidates.add(s);
            }
            if (candidates.size() == 0) toBeActivated = "Interaction";
            else if (candidates.size() == 1) toBeActivated = candidates.iterator().next().toString();
            else if (candidates.contains(lastActiveSubject)) toBeActivated = lastActiveSubject.toString();
            else toBeActivated = candidates.iterator().next().toString();

            Iterator<Component> i = visualizationTabs.iterator();
            while (i.hasNext()) {
                Component tab = i.next();
                if (tab.getCaption().equals(toBeActivated)) {
                    if (visualizationTabs.getSelectedTab() == tab) visualizationTabs.setSelectedTab(visualizationTabs.getComponentCount()-1);
                    visualizationTabs.setSelectedTab(tab);
                    return;
                }
            }
        }
        if (!b && selectionMode) {
            selectionMode = false;
            if (stateClickListener != null) {
                stateClickListener.clickedState(null);
                stateClickListener = null;
            }
        }
    }

    private HorizontalLayout createToolbar() {
        toolBar = new HorizontalLayout();

        simulate = new Button("Auto-progress");
        simulate.addClickListener( e -> {
            selectionMode = true;
            Notification.show("Please select where to progress to.", Notification.Type.WARNING_MESSAGE);
            visualizationSlider.expand();
        });

        restart.addClickListener( e -> {
            mainLayoutFrame.removeAllComponents();
            createBasicLayout();
            currentInstance = new Instance(currentProcess);
            scaffoldingManager.updateScaffolds(currentInstance);
            simulator = new Simulator(currentInstance, subjectPanels, this);

            updateUI();
        });

        if (!currentProcess.getSubjects().isEmpty()) toolBar.addComponent(simulate);
        toolBar.addComponent(restart);
        toolBar.addComponent(differentProcess);
        toolBar.setSpacing(true);
        return toolBar;

    }

    public void notifyAboutClickedState(StateClickListener listener) {
        stateClickListener = listener;
    }

    public void informAboutSelectedNode(String vizName, String name) {
        if (!selectionMode) return;

        if (vizName.equals("Interaction")) {
            Iterator<Component> i = visualizationTabs.iterator();
            while (i.hasNext()) {
                Component tab = i.next();
                if (tab.getCaption().equals(name)) {
                    visualizationTabs.setSelectedTab(tab);
                    return;
                }
            }

        }

        State selectedState = currentProcess.getStateWithName(name);
        if (selectedState == null) return;

        selectionMode = false;
        visualizationSlider.collapse();

        if (stateClickListener == null) {
            simulate(selectedState);
        }
        else {
            stateClickListener.clickedState(selectedState);
            stateClickListener = null;
        }
    }

    public void expandVisualizationSlider() {
        selectionMode = true;
        Notification.show("Please select the existing step you want to use.", Notification.Type.WARNING_MESSAGE);
        visualizationSlider.expand();
    }

    private Component recalculateSubjectLayout(int availableWidth) {
        int numberOfSubjects = subjectPanels.keySet().size();
        int numberOfColumns = availableWidth / 350;
        int numberOfRows = numberOfSubjects / numberOfColumns + 1;

        GridLayout oldLayout = subjectLayout;

        subjectLayout = new GridLayout(numberOfColumns,numberOfRows);
        subjectLayout.setSpacing(true);
        for (Panel p: subjectPanels.values()) {
            subjectLayout.addComponent(p);
        }

        mainInteractionArea.replaceComponent(oldLayout,subjectLayout);

        return subjectLayout;
    }
    private Component createSubjectLayout(int availableWidth) {
        int numberOfSubjects = currentProcess.getSubjects().size();
        int numberOfColumns = availableWidth / 350;
        int numberOfRows = numberOfSubjects / numberOfColumns + 1;

        subjectLayout = new GridLayout(numberOfColumns,numberOfRows);

        subjectPanels = new HashMap<>();
        for (Subject s: currentProcess.getSubjects()) {
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

        Button addInitialSubject = new Button ("Add a first actor");
        addInitialSubject.addClickListener( e -> {
            openElaborationOverlay(null,ElaborationUI.INITALSUBJECT);
        });

        if (currentProcess.getSubjects().isEmpty()) return addInitialSubject;
        else return subjectLayout;

    }

    private void updateUI() {
        differentProcess.setVisible(false);
        simulate.setVisible(true);

        for (Subject s: currentInstance.getProcess().getSubjects()) {
            fillSubjectPanel(s);
        }
        if (initialStartup) {
            differentProcess.setVisible(true);
            initialStartup = false;
        }

        if (!currentInstance.processFinished() && !currentInstance.processIsBlocked()) {
            restart.setVisible(false);
            scaffoldingPanel.setVisible(true);
        }

        if (!currentInstance.processFinished() && currentInstance.processIsBlocked()) {
            LogHelper.logInfo("Process blocked, offering to restart ...");
//             scaffoldingPanel.setVisible(false);
            restart.setVisible(true);
        }

        if (currentInstance.processFinished()) {
            simulate.setVisible(false);
            LogHelper.logInfo("Process finished, offering to restart ...");
            mainLayoutFrame.removeComponent(scaffoldingPanel);
            scaffoldingPanel.setVisible(false);
            if (currentInstance.getProcess().getSubjects().size() > 0) {
                restart.setVisible(true);

                XMLStore xmlStore = new XMLStore(id);
                String xml = xmlStore.convertToXML(currentInstance.getProcess());
                String fileName = xmlStore.saveToServerFile(currentInstance.getProcess().toString(),xml);
                FileDownloader fd = new FileDownloader(new FileResource(new File(fileName)));
                Button save = new Button("Download Process");

                fd.extend(save);

                toolBar.addComponent(save);
            }
            differentProcess.setVisible(true);
        }
    }

    private void fillSubjectPanel(Subject s) {

        VerticalLayout panelContent = (VerticalLayout) subjectPanels.get(s).getContent();
        final OptionGroup nextStates = new OptionGroup("Select one of the following options:");
        panelContent.removeAllComponents();

        Label availableMessageList = new Label("");
        Label processMessageLabel = new Label("");
        Label expectedMessageLabel = new Label("<small>The following messages are expected from"+s+", but are not currently provided:</small>", ContentMode.HTML);
        final ComboBox expectedMessageSelector = new ComboBox("please select:");
        Button expectedMessageSend = new Button("Send");

        Set<Message> availableMessages = currentInstance.getAvailableMessagesForSubject(s);
        if (availableMessages != null && availableMessages.size() > 0) {
            StringBuffer list = new StringBuffer("<small>The following messages are available:<ul>");
            for (Message m: availableMessages) {
                list.append("<li>"+m.toString()+"</li>");
            }
            list.append("</ul></small>");
            availableMessageList = new Label(list.toString(), ContentMode.HTML);
        }
        if (currentInstance.getLatestProcessedMessageForSubject(s) != null) {
            processMessageLabel = new Label("<small>Recently received message:<ul><li>"+currentInstance.getLatestProcessedMessageForSubject(s)+"</li></ul></small>", ContentMode.HTML);
        }

        if (s.getExpectedMessages().size()>0) {
            for (Message m: s.getExpectedMessages()) {
                expectedMessageSelector.addItem(m);
            }
            expectedMessageSelector.setValue(s.getExpectedMessages().iterator().next());
            expectedMessageSend.addClickListener( e -> {
                Message m = (Message) expectedMessageSelector.getValue();
                Subject recipient = currentInstance.getProcess().getRecipientOfMessage(m);
                currentInstance.putMessageInInputbuffer(recipient,m);
                updateUI();
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

        State currentState = currentInstance.getAvailableStateForSubject(s);
        if (currentState != null) {
            Label label1 = new Label(currentState.toString(),ContentMode.HTML);
            panelContent.addComponent(label1);

            Set<State> nextPossibleSteps = currentInstance.getNextStatesOfSubject(s);
            if (nextPossibleSteps != null && nextPossibleSteps.size()>0) {
                if (nextPossibleSteps.size() == 1) {
                    State nextState = nextPossibleSteps.iterator().next();
                    if (!currentInstance.getConditionForStateInSubject(s, nextState).toString().equals("")) {
                        Label label2 = new Label("You can only progress under the following condition: <br>"+currentInstance.getConditionForStateInSubject(s,nextState),ContentMode.HTML);
                        panelContent.addComponent(label2);
                    }
                }
                else {
                    if (currentInstance.subjectCanProgress(s)) {
                        boolean toBeShown = false;
                        for (State nextState : nextPossibleSteps) {
                            Condition condition = currentInstance.getConditionForStateInSubject(s, nextState);
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
            LogHelper.logInfo("UI: clicking on perfom button for subject "+s);
            lastActiveSubject = s;
            Condition c = null;
            if (nextStates.size() > 0) c = (Condition) nextStates.getValue();
            currentInstance.advanceStateForSubject(s, c);
            scaffoldingManager.updateScaffolds(currentInstance,currentState);
            updateUI();
        });

        Button elaborate = new Button("I have a problem here");
        elaborate.addClickListener( e -> {
            perform.setEnabled(false);
            elaborate.setEnabled(false);
            openElaborationOverlay(s,ElaborationUI.ELABORATE);
        });

        perform.setEnabled(currentInstance.subjectCanProgress(s));

        Button addInitialStep = new Button("Add an initial step");
        addInitialStep.addClickListener( e -> {
            openElaborationOverlay(s,ElaborationUI.INITIALSTEP);

        });

        Button addAdditionStep = new Button("Add an additional step");
        addAdditionStep.addClickListener( e -> {
            openElaborationOverlay(s, ElaborationUI.ADDITIONALSTEP);
        });

        if (!(s.toString().equals(Subject.ANONYMOUS))) panelContent.addComponent(perform);
        if (currentInstance.subjectCanProgress(s)) panelContent.addComponent(elaborate);
        if (!availableMessages.isEmpty()) panelContent.addComponent(availableMessageList);
        if (!processMessageLabel.getValue().equals("")) panelContent.addComponent(processMessageLabel);
        if (s.getExpectedMessages().size()>0) {
            panelContent.addComponents(expectedMessageLabel, expectedMessageSelector, expectedMessageSend);
        }
        if (s.getProvidedMessages().size()>0) {
            panelContent.addComponent(providedMessagesLabel);
        }
        if (s.getFirstState() == null && !s.toString().equals(Subject.ANONYMOUS)) panelContent.addComponent(addInitialStep);
        if (currentInstance.subjectFinished(s) && s.getFirstState() != null) panelContent.addComponent(addAdditionStep);
    }

    private void openElaborationOverlay(Subject s, int mode) {
        ElaborationUI elaborationUI = new ElaborationUI(processChangeHistory);
        getUI().addWindow(elaborationUI);

        if (mode == ElaborationUI.ELABORATE) elaborationUI.elaborate(s, currentInstance);
        if (mode == ElaborationUI.INITIALSTEP) elaborationUI.initialStep(s, currentInstance);
        if (mode == ElaborationUI.ADDITIONALSTEP) elaborationUI.additionalStep(s, currentInstance);
        if (mode == ElaborationUI.INITALSUBJECT) elaborationUI.initialSubject(currentInstance);

        elaborationUI.addCloseListener(new Window.CloseListener() {
            @Override
            public void windowClose(Window.CloseEvent e) {
                createBasicLayout();
                scaffoldingManager.updateScaffolds(currentInstance,currentInstance.getAvailableStateForSubject(s));
                updateUI();
            }
        });

    }
/*
    private void openVisualizationOverlay() {
        VisualizationUI visualizationUI = new VisualizationUI(currentInstance,"Interaction");
        getUI().addWindow(visualizationUI);
        visualizationUI.showSubjectInteraction();
        visualizationUI.addCloseListener(new Window.CloseListener() {
            @Override
            public void windowClose(Window.CloseEvent e) {
                createBasicLayout();
                updateUI();
            }
        });

    }

    private void openVisualizationOverlay(Subject s) {
        VisualizationUI visualizationUI = new VisualizationUI(currentInstance,s.toString());
        getUI().addWindow(visualizationUI);
        visualizationUI.showSubjectProgress(s);
        visualizationUI.addCloseListener(new Window.CloseListener() {
            @Override
            public void windowClose(Window.CloseEvent e) {
                createBasicLayout();
                updateUI();
            }
        });

    }
*/
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
                    initialStartup = true;
                    processChangeHistory = new ProcessChangeHistory();
                    currentInstance = new Instance(currentProcess);
                    createBasicLayout();
                    updateUI();
                }
            }
        });

    }

    public boolean simulate(State toState) {
        boolean simSuccessful = simulator.simulatePathToState(toState);
        if (!simSuccessful) Notification.show("Could not go to "+toState,
                "The process has already been executed too far. Finish this round and try again after restarting.",
                Notification.Type.ASSISTIVE_NOTIFICATION);
        return simSuccessful;
    }

    @WebServlet(urlPatterns = "/*", name = "CoMPArEServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = CoMPArEUI.class, productionMode = false)
    public static class CoMPArEServlet extends VaadinServlet {
    }
}
