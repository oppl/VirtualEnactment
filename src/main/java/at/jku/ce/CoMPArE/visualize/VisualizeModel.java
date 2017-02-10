package at.jku.ce.CoMPArE.visualize;

/**
 * Created by oppl on 24/11/2016.
 */
import at.jku.ce.CoMPArE.CoMPArEUI;
import at.jku.ce.CoMPArE.LogHelper;
import at.jku.ce.CoMPArE.execute.InstanceHistoryStep;
import at.jku.ce.CoMPArE.process.*;
import at.jku.ce.CoMPArE.process.Process;

import com.vaadin.pontus.vizcomponent.VizComponent;
import com.vaadin.pontus.vizcomponent.VizComponent.EdgeClickEvent;
import com.vaadin.pontus.vizcomponent.VizComponent.NodeClickEvent;
import com.vaadin.pontus.vizcomponent.VizComponent.NodeClickListener;
import com.vaadin.pontus.vizcomponent.client.ZoomSettings;
import com.vaadin.pontus.vizcomponent.model.Graph;
import com.vaadin.ui.*;
import sun.rmi.runtime.Log;

import java.util.*;

public class VisualizeModel extends VerticalLayout {

    String name;

    Panel panel;
    Graph graph;
    VizComponent component;
    private Graph.Node selectedNode;

    public VisualizeModel(String name, CoMPArEUI parent, int width, int height) {

        this.name = name;

        panel = new Panel("");
        panel.setWidth(""+width);
        panel.setHeight(""+height);

        component = new VizComponent();
        graph = new Graph(name, Graph.DIGRAPH);

        component.setCaption("");
        component.setWidth("100%");
        component.setHeight("100%");
        panel.setContent(component);
//        component.drawGraph(graph);

        selectedNode = null;

//        setSizeFull();
        addComponent(panel);
//        setComponentAlignment(component, Alignment.MIDDLE_CENTER);

        ZoomSettings zs = new ZoomSettings();
        zs.setPreventMouseEventsDefault(true);
        zs.setFit(true);
        zs.setMaxZoom(2.0f);
        zs.setMinZoom(0.5f);
        component.setPanZoomSettings(zs);

        component.addClickListener(new NodeClickListener() {

            @Override
            public void nodeClicked(NodeClickEvent e) {
                selectedNode = e.getNode();
                LogHelper.logInfo("VizUI: selected node "+selectedNode.getId());
                if (parent != null) parent.informAboutSelectedNode(name,selectedNode.getId());
            }

        });


    }

    public void showSubject(Subject subject) {
        graph = new Graph(name, Graph.DIGRAPH);
        addStates(subject.getStates(), new HashSet());
        addTransitions(subject.getTransitions(),subject, new HashSet());
        component.drawGraph(graph);
        component.fitGraph();
    }

    public void showSubject(Subject subject, Set toBeMarked) {
        graph = new Graph(name, Graph.DIGRAPH);
        addStates(subject.getStates(),toBeMarked);
        addTransitions(subject.getTransitions(),subject, toBeMarked);
        component.drawGraph(graph);
        component.fitGraph();
    }


    public void addStates(Collection<State> states, Set toBeMarked) {
        for (State s: states) {
            Graph.Node node = new Graph.Node(s.getUUID().toString());
            node.setParam("shape", "box");
            node.setParam("label", "\""+s.toString()+"\"");
            if (toBeMarked.contains(s)) {
                node.setParam("style", "filled");
                node.setParam("fillcolor", "lightgreen");
                node.setParam("color", "darkgreen");
                node.setParam("penwidth", "3.0");
            }
            graph.addNode(node);
        }
    }

    public void addTransitions(Set<Transition> transitions, Subject subject, Set toBeMarked) {
        for (Transition t: transitions) {
            graph.addEdge(graph.getNode(t.getSource().toString()), graph.getNode(t.getDest().toString()));
            Graph.Edge edge = graph.getEdge(graph.getNode(t.getSource().toString()), graph.getNode(t.getDest().toString()));
            Condition c = t.getCondition();
            if (toBeMarked.contains(t)) {
                edge.setParam("color","darkgreen");
                edge.setParam("penwidth","3.0");
                edge.setParam("fontcolor","darkgreen");
            }
            if (c != null) {
                if (c instanceof MessageCondition) edge.setParam("label","\""+subject.getParentProcess().getMessageByUUID(((MessageCondition) c).getMessage()).toString()+"\"");
                else edge.setParam("label", "\""+c.toString()+"\"");
            }

        }
    }

    public void greyOutCompletedStates(LinkedList<InstanceHistoryStep> history, State currentState) {
        Graph.Node currentNode = null;
        Graph.Node previousNode = null;
        LinkedList<InstanceHistoryStep> reverseHistory = new LinkedList<>(history);
        Collections.reverse(reverseHistory);
        for (InstanceHistoryStep s : reverseHistory) {
            currentNode = graph.getNode(s.getState().getUUID().toString());
            if (currentNode != null) {
                currentNode.setParam("style", "filled");
                currentNode.setParam("fillcolor", "lightgrey");
                currentNode.setParam("color", "darkgreen");
                currentNode.setParam("penwidth", "3.0");
                currentNode.setParam("fontcolor","darkgreen");

            }
            if (previousNode!=null && currentNode != null) {
                Graph.Edge edge = graph.getEdge(previousNode, currentNode);
                if (edge != null) {
                    edge.setParam("color", "darkgreen");
                    edge.setParam("fontcolor","darkgreen");

                }
            }
            previousNode = currentNode;
        }
        if (currentState != null) currentNode = graph.getNode(currentState.getUUID().toString());
        if (currentNode != null) {
            currentNode.setParam("style", "filled");
            currentNode.setParam("fillcolor", "lightgreen");

            component.addTextCss(currentNode, "font-weight", "bold");
            if (previousNode!=null) {
                Graph.Edge edge = graph.getEdge(previousNode, currentNode);
                if (edge != null) {
                    edge.setParam("color", "darkgreen");
                    edge.setParam("fontcolor","darkgreen");
                    component.addTextCss(edge, "fill", "darkgreen");
                }
            }
        }
        component.drawGraph(graph);
        component.fitGraph();
    }


    public void showSubjectInteraction(Process p) {
        showSubjectInteraction(p, new HashSet());
    }

    public void showSubjectInteraction(Process p, Set toBeMarked) {
//        LogHelper.logInfo("creating subject interaction");
        graph = new Graph("", Graph.DIGRAPH);
        for (Message m: p.getMessages()) {
//            LogHelper.logInfo("adding information for Message "+m.toString());
            Graph.Node sender = new Graph.Node(p.getSenderOfMessage(m).getUUID().toString());
            Graph.Node recipient = new Graph.Node(p.getRecipientOfMessage(m).getUUID().toString());
            sender.setParam("label", "\""+p.getSenderOfMessage(m).toString()+"\"");
            recipient.setParam("label", "\""+p.getRecipientOfMessage(m).toString()+"\"");

            if (toBeMarked.contains(p.getSenderOfMessage(m))) {
                sender.setParam("style", "filled");
                sender.setParam("fillcolor", "lightgreen");
                sender.setParam("color", "darkgreen");
                sender.setParam("penwidth", "3.0");
            }

            if (toBeMarked.contains(p.getRecipientOfMessage(m))) {
                recipient.setParam("style", "filled");
                recipient.setParam("fillcolor", "lightgreen");
                recipient.setParam("color", "darkgreen");
                recipient.setParam("penwidth", "3.0");
            }

            Graph.Node message = new Graph.Node((m.getUUID().toString()));
            message.setParam("label", "\""+m.toString().replace(" ","\\n")+"\"");
            message.setParam("fontsize","10");
            message.setParam("shape", "note");
            if (toBeMarked.contains(m)) {
                message.setParam("style", "filled");
                message.setParam("fillcolor", "lightgreen");
                message.setParam("color", "darkgreen");
                message.setParam("penwidth", "3.0");
            }


            graph.addEdge(sender, message);
            graph.addEdge(message,recipient);

        }
        component.drawGraph(graph);
    }

    public String getSelectedNodeName() {
        if (selectedNode == null) return null;
        return selectedNode.getId();
    }

}

