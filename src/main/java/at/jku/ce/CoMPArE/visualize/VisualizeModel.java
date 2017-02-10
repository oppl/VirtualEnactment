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
        if (subject.getFirstState() != null) addState(null,null,subject.getFirstState(), new HashSet<>());
        component.drawGraph(graph);
        component.fitGraph();
    }

    public void addState(Graph.Node parentNode, State parentState, State state, Collection<State> alreadyVisitedStates) {
        Boolean loopFound = alreadyVisitedStates.contains(state);
        Graph.Node node = new Graph.Node(state.getUUID().toString());
        if (!loopFound) {
//            LogHelper.logInfo("modelViz: adding node " + state);
//            if (state instanceof SendState) node.setParam("color", "red");
//            if (state instanceof RecvState) node.setParam("color", "green");
            node.setParam("shape", "box");
            node.setParam("label", "\""+state.toString()+"\"");
            graph.addNode(node);
            alreadyVisitedStates.add(state);
/*            if (state instanceof SendState) {
                Message m = ((SendState) state).getSentMessage();
                Graph.Node message = new Graph.Node(m.getUUID().toString());
                message.setParam("label", "\""+m.toString().replace(" ","\\n")+"\"");
                message.setParam("fontsize","10");
                message.setParam("shape","note");

                graph.addNode(message);
                graph.addEdge(node,message);
            }*/
        }
        if (parentNode != null) {
//            LogHelper.logInfo("modelViz: adding edge from " + parentNode.getId() + " to " + node.getId());
            graph.addEdge(parentNode, node);
            Graph.Edge edge = graph.getEdge(parentNode, node);
            Condition c = parentState.getNextStates().get(state);
            if (c != null) {
                if (c instanceof MessageCondition) edge.setParam("label","\""+parentState.getParentSubject().getParentProcess().getMessageByUUID(((MessageCondition) c).getMessage()).toString()+"\"");
                else edge.setParam("label", "\""+c.toString()+"\"");
            }

        }
        if (!loopFound)
            for (State nextState : state.getNextStates().keySet())
                if (nextState != null) addState(node, state, nextState,alreadyVisitedStates);
    }

    public void greyOutCompletedStates(LinkedList<InstanceHistoryStep> history, State currentState) {
        Graph.Node currentNode = null;
        Graph.Node previousNode = null;
        LinkedList<InstanceHistoryStep> reverseHistory = new LinkedList<>(history);
        Collections.reverse(reverseHistory);
        for (InstanceHistoryStep s : reverseHistory) {
            currentNode = graph.getNode(s.getState().getUUID().toString());
            if (currentNode != null) {
                component.addCss(currentNode, "stroke", "darkgreen");
                component.addCss(currentNode, "fill", "lightgrey");
                component.addCss(currentNode, "stroke-width", "3");
                component.addTextCss(currentNode, "fill", "darkgreen");
            }
            if (previousNode!=null && currentNode != null) {
                Graph.Edge edge = graph.getEdge(previousNode, currentNode);
                if (edge != null) {
                    component.addCss(edge,"stroke","darkgreen");
                    component.addCss(edge,"fill","darkgreen");
                    component.addTextCss(edge, "fill", "darkgreen");
                }
            }
            previousNode = currentNode;
        }
        if (currentState != null) currentNode = graph.getNode(currentState.getUUID().toString());
        if (currentNode != null) {
            component.addCss(currentNode, "stroke-width", "3");
            component.addCss(currentNode, "fill", "lightgreen");
            component.addTextCss(currentNode, "font-weight", "bold");
            if (previousNode!=null) {
                Graph.Edge edge = graph.getEdge(previousNode, currentNode);
                if (edge != null) {
                    component.addCss(edge, "stroke", "darkgreen");
                    component.addCss(edge,"fill","darkgreen");
                    component.addTextCss(edge, "fill", "darkgreen");
                }
            }
        }

    }

    public void showSubjectInteraction(Process p) {
//        LogHelper.logInfo("creating subject interaction");
        graph = new Graph("", Graph.DIGRAPH);
        for (Message m: p.getMessages()) {
//            LogHelper.logInfo("adding information for Message "+m.toString());
            Graph.Node sender = new Graph.Node(p.getSenderOfMessage(m).getUUID().toString());
            Graph.Node recipient = new Graph.Node(p.getRecipientOfMessage(m).getUUID().toString());
            sender.setParam("label", "\""+p.getSenderOfMessage(m).toString()+"\"");
            recipient.setParam("label", "\""+p.getRecipientOfMessage(m).toString()+"\"");

            Graph.Node message = new Graph.Node((m.getUUID().toString()));
            message.setParam("label", "\""+m.toString().replace(" ","\\n")+"\"");
            message.setParam("fontsize","10");
            message.setParam("shape", "note");

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

