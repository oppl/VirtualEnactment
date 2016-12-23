package at.jku.ce.CoMPArE.scaffolding;

import at.jku.ce.CoMPArE.LogHelper;
import at.jku.ce.CoMPArE.execute.Instance;
import at.jku.ce.CoMPArE.process.Process;
import at.jku.ce.CoMPArE.process.State;
import at.jku.ce.CoMPArE.scaffolding.agents.*;
import at.jku.ce.CoMPArE.scaffolding.scaffolds.Scaffold;
import com.vaadin.shared.ui.slider.SliderOrientation;
import com.vaadin.ui.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by oppl on 25/11/2016.
 */
public class ScaffoldingManager {

    public static int MODE_NONE = 0;
    public static int MODE_CONC = 1;
    public static int MODE_META = 2;
    public static int MODE_STRAT = 3;
    public static int MODE_PROC = 4;
//    public static int MODE_ALL = 5;

    private Process process;
    private Panel scaffoldingPanel;
    private Set<ScaffoldingAgent> scaffoldingAgents;
    private int globalScaffoldingMode;
    private Table table;
    private Slider slider;

    public ScaffoldingManager(Process p, Panel scaffoldingPanel) {
        HorizontalLayout hLayout = (HorizontalLayout) scaffoldingPanel.getContent();

        this.process = p;
        this.scaffoldingPanel = scaffoldingPanel;
        this.scaffoldingAgents = new HashSet<>();
        this.globalScaffoldingMode = ScaffoldingManager.MODE_STRAT;
        addScaffoldingAgent(new ElaborationProcessAgent(p, this));
        addScaffoldingAgent(new ExplorationAgent(p, this));
        addScaffoldingAgent(new UnhandledCommunicationAgent(p, this));
        addScaffoldingAgent(new OnboardingAgent(p, this, scaffoldingPanel));

        table = new Table("");
        table.addContainerProperty("Advice", String.class, null);
        table.addContainerProperty("Button",  Component.class, null);
        table.setWidth("100%");

        table.setColumnWidth("Button",150);
//        table.setColumnWidth("Advice",700);
        table.setColumnHeaderMode(Table.ColumnHeaderMode.HIDDEN);
        table.setColumnAlignment("Button", Table.Align.CENTER);

        slider = new Slider(0.0,4.0,0);
        slider.setOrientation(SliderOrientation.VERTICAL);
        slider.setWidth("50px");
        slider.setHeight("150px");
        slider.setValue(new Double(globalScaffoldingMode));
        slider.setImmediate(true);
        slider.setDescription("Adjust suggestions here - higher means more specific tips");

        slider.addValueChangeListener( e -> {
                LogHelper.logInfo("Slider value changed to "+slider.getValue());
                double value = (Double) slider.getValue();
                if (value == 0.0) {
                    globalScaffoldingMode = ScaffoldingManager.MODE_NONE;
                }
                if (value == 1.0) {
                    globalScaffoldingMode = ScaffoldingManager.MODE_CONC;
                }
                if (value == 2.0) {
                    globalScaffoldingMode = ScaffoldingManager.MODE_META;
                }
                if (value == 3.0) {
                    globalScaffoldingMode = ScaffoldingManager.MODE_STRAT;
                }
                if (value == 4.0) {
                    globalScaffoldingMode = ScaffoldingManager.MODE_PROC;
                }
                updateScaffoldingPanel();
        });

        hLayout.addComponent(slider);
        hLayout.addComponent(table);

        updateScaffolds(new Instance(p), new State(""));
    }

    public void addScaffoldingAgent(ScaffoldingAgent newAgent) {
        scaffoldingAgents.add(newAgent);
        newAgent.init();
    }

    public void removeScaffoldingAgent(ScaffoldingAgent agent) {
        scaffoldingAgents.remove(agent);
    }

    public void updateScaffolds(Instance currentInstance, State finishedState) {
//        LogHelper.logInfo("ScaffoldingManager: now checking for agents to be informed about finishing state "+finishedState);
        for (ScaffoldingAgent agent: scaffoldingAgents) {
            if (agent.getFreq() == ScaffoldingAgent.FREQ_EACHSTEP) {
                agent.updateScaffolds(currentInstance, finishedState);
//                LogHelper.logInfo("ScaffoldingManager: informing "+agent.getClass().getName());
            }
        }
        updateScaffoldingPanel();
    }

    public void updateScaffolds(Instance finishedInstance) {
        LogHelper.logInfo("ScaffoldingManager: now checking for agents to be informed about a finishing instance");
        for (ScaffoldingAgent agent: scaffoldingAgents) {
            if (agent.getFreq() == ScaffoldingAgent.FREQ_EACHINSTANCE)
                agent.updateScaffolds(finishedInstance);
        }
        updateScaffoldingPanel();
    }

    private void updateScaffoldingPanel() {
        table.removeAllItems();
        Set<Scaffold> scaffolds = new HashSet<>();
        for (ScaffoldingAgent agent: scaffoldingAgents) {
            LogHelper.logInfo("updScaf: Retrieving "+agent.getScaffolds(globalScaffoldingMode).size()+" scaffolds from "+agent.getClass().getName()+" on level "+globalScaffoldingMode);
            scaffolds.addAll(agent.getScaffolds(globalScaffoldingMode));
        }
        int itemID = 0;

        for (Scaffold scaffold: scaffolds) {
            LogHelper.logInfo("updScaf: Adding scaffold "+scaffold.getScaffoldingPrompt());
            table.addItem(new Object[]{scaffold.getScaffoldingPrompt(),scaffold.getInteractiveComponent()}, itemID);
            itemID++;
        }
        LogHelper.logInfo("updScaf: Now displaying table with "+table.size()+" scaffolds");
        table.setPageLength(table.size());
//        table.setSelectable(true);
        if (table.size() == 0) table.setVisible(false);
        else table.setVisible(true);
    }

    public void immediatelyUpdateScaffoldingPanel(ScaffoldingAgent trigger) {
        if (scaffoldingAgents.contains(trigger)) {
            updateScaffoldingPanel();
        }
    }

    public void openScaffoldingDetails(Window w) {
        scaffoldingPanel.getUI().addWindow(w);
    }

}
