package at.jku.ce.CoMPArE.simulate;

import at.jku.ce.CoMPArE.CoMPArEUI;
import at.jku.ce.CoMPArE.LogHelper;
import at.jku.ce.CoMPArE.execute.Instance;
import at.jku.ce.CoMPArE.process.*;
import com.vaadin.ui.*;
import sun.rmi.runtime.Log;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * Created by oppl on 29/11/2016.
 */
public class Simulator {

    private Instance instance;
    private Map<Subject,Panel> subjectPanels;
    private CoMPArEUI ui;

    public Simulator(Instance instance, Map<Subject,Panel> subjectPanels, CoMPArEUI ui) {
        this.instance = instance;
        this.subjectPanels = subjectPanels;
        this.ui = ui;
    }

    private LinkedList<SimulatorStep> findOverallPathToState(State targetState) {
        LinkedList<SimulatorStep> overallSteps = new LinkedList<>();
        findPathToState(targetState, overallSteps,0);
        StringBuffer path = new StringBuffer();
        for (SimulatorStep s : overallSteps) path.append(s.subject+":"+s.state+" ("+s.condition+") -> \n");
        LogHelper.logInfo("Simulator: found overall path: "+path.toString());
        return overallSteps;
    }

    public boolean simulatePathToState(State targetState) {
        if (findOverallPathToState(targetState).isEmpty()) return false;
        Thread thread = new Thread(){
            public void run() {
                LinkedList<SimulatorStep> overallSteps = findOverallPathToState(targetState);
                delayNextStep();
                for (SimulatorStep ss : overallSteps) {
                    LogHelper.logInfo("Simulator: executing simulator step: " + ss.subject + " " + ss.state + " " + ss.condition);
                    Panel subjectPanel = subjectPanels.get(ss.subject);
                    Label stateName = null;
                    Button performStep = null;
                    OptionGroup conditions = null;
                    VerticalLayout subjectLayout = ((VerticalLayout) subjectPanel.getContent());
                    for (int i = 0; i < subjectLayout.getComponentCount(); i++) {
                        Component c = subjectLayout.getComponent(i);
                        if (i == 0 && c instanceof Label) stateName = (Label) c;
                        if (c instanceof OptionGroup) conditions = (OptionGroup) c;
                        if (c instanceof Button && c.getCaption().equals("Perform step")) performStep = (Button) c;
                    }
                    if (!stateName.getValue().equals(ss.state.getName())) continue;
                    Condition toBeSet = null;
//                    LogHelper.logInfo("Simulator: currently at step index "+overallSteps.lastIndexOf(ss)+" of "+overallSteps.size()+" simulation steps");
                    if (overallSteps.lastIndexOf(ss) < overallSteps.size() - 1) {
                        SimulatorStep nextSS = overallSteps.get(overallSteps.lastIndexOf(ss) + 1);
//                        LogHelper.logInfo("Simulator: next simulator step: " + nextSS.subject + " " + nextSS.state + " " + nextSS.condition);
                        if (nextSS.condition != null && nextSS.condition.toString() != "") {
//                            LogHelper.logInfo("Simulator: selecting option "+nextSS.condition+" in "+conditions);
                            if (!(nextSS.condition instanceof MessageCondition) && conditions == null) return;
                            if (conditions != null) {
                                toBeSet = nextSS.condition;
                                conditions.setValue(nextSS.condition);
                            }
                        }
                    }
                    if (performStep == null) return;
                    final Condition optionGroupSelection = toBeSet;
                    final OptionGroup optionGroupToBeTargeted = conditions;
                    if (optionGroupToBeTargeted != null && optionGroupSelection != null) {
                        toggleLabel(stateName);
//                        delayNextStep();
                        ui.access(new Runnable() {
                            @Override
                            public void run() {
                                optionGroupToBeTargeted.setValue(optionGroupSelection);
                            }
                        });
                        //toggleLabel(stateName);
                    }
                    final Button clickButton = performStep;
                    if (optionGroupToBeTargeted == null) toggleLabel(stateName);
                    delayNextStep();
                    if (overallSteps.getLast() != ss) {
                        ui.access(new Runnable() {
                            @Override
                            public void run() {
                                clickButton.click();
                            }
                        });
                    }
                    //delayNextStep();
                    toggleLabel(stateName);
                }
            }
        };

        thread.start();
        return true;
    }

    private String captionBuffer = null;

    private void toggleLabel(Label label) {
        ui.access(new Runnable() {
            @Override
            public void run() {
                if (captionBuffer != null) {
                    label.setValue(captionBuffer);
                    captionBuffer = null;
                }
                else {
                    String caption = label.getValue();
                    label.setValue("<b>" + caption + "</b>");
                    captionBuffer = caption;
                }
            }
        });
    }

    private void delayNextStep() {
        try {
            Thread.sleep(1000);
        }
        catch (InterruptedException e) {

        }
    }

    public boolean findPathToState(State targetState, LinkedList<SimulatorStep> overallList, int entryIndex) {
        Subject targetSubject = instance.getProcess().getSubjectWithState(targetState);
        if (instance.subjectFinished(targetSubject)) return false;
        boolean pathFound;
        LinkedList<SimulatorStep> steps = new LinkedList<>();
        State sourceState = instance.getAvailableStateForSubject(targetSubject);
        if (sourceState == null) sourceState = targetSubject.getFirstState();
        pathFound = findPath(targetSubject, sourceState,targetState,steps);
        if (!pathFound) return false;
        else {

            StringBuffer path = new StringBuffer();
            for (SimulatorStep s : steps) path.append(s.state+" -> ");
            LogHelper.logInfo("Simulator: found path in subject "+targetSubject+": "+path.toString());

            overallList.addAll(entryIndex,steps);

            for (int i = steps.size()-1; i >= 0; i--) {
                SimulatorStep s = steps.get(i);
                if (s.state instanceof RecvState) {
                    Set<Message> messages = ((RecvState) s.state).getRecvdMessages();
                    Set<Message> requiredMessages = new HashSet<>();
                    for (SimulatorStep ss: steps) {
                        if (ss.condition instanceof MessageCondition) requiredMessages.add(((MessageCondition) ss.condition).getMessage());
                    }
                    if (messages.isEmpty()) return false;
                    boolean requiredMessageIsContained = false;
                    for (Message m: messages) {
                        if (requiredMessages.contains(m)) {
                            requiredMessageIsContained = true;
//                            LogHelper.logInfo("Simulator: required Message "+m.toString()+" is contained");
                        }
                    }
                    for (Message m: messages) {
                        if (messages.size()>1 && requiredMessageIsContained && !requiredMessages.contains(m)) continue;
                        Subject sendingSubject = instance.getProcess().getSenderOfMessage(m);
                        State sendingState = sendingSubject.getSendState(m);
                        boolean stateAlreadyContained = false;
                        for (SimulatorStep ss : overallList)
                            if (ss.state == sendingState) stateAlreadyContained = true;
                        if (!stateAlreadyContained) {
                            boolean successFull = findPathToState(sendingState, overallList, overallList.lastIndexOf(s));
                            if (successFull) break;
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean findPath(Subject subject, State source, State target, LinkedList<SimulatorStep> steps) {
        Set<State> predecessors = subject.getPredecessorStates(target);
//        LogHelper.logInfo("Simulator: looking at "+target);
        if (source == target) {
            steps.addFirst(new SimulatorStep(subject,source,source.getNextStates().get(target)));
            return true;
        }
        if (predecessors.isEmpty()) return false;
        for (State predecessor: predecessors) {
            LinkedList<SimulatorStep> triedSteps = new LinkedList<>();
            triedSteps.addFirst(new SimulatorStep(subject,target,predecessor.getNextStates().get(target)));
            boolean success = findPath(subject, source, predecessor, triedSteps);
            if (success) {
                steps.addAll(0,triedSteps);
                return true;
            }

        }
        return false;
    }

    private class SimulatorStep {
        public Subject subject;
        public State state;
        public Condition condition;

        public SimulatorStep(Subject subject, State state, Condition condition) {
            this.subject = subject;
            this.state = state;
            this.condition = condition;
        }

    }

}
