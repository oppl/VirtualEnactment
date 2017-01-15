package at.jku.ce.CoMPArE.visualize;

/**
 * Created by oppl on 24/11/2016.
 */
import at.jku.ce.CoMPArE.CoMPArEUI;
import at.jku.ce.CoMPArE.LogHelper;
import at.jku.ce.CoMPArE.process.*;
import at.jku.ce.CoMPArE.process.Process;

import com.vaadin.pontus.vizcomponent.VizComponent;
import com.vaadin.pontus.vizcomponent.VizComponent.EdgeClickEvent;
import com.vaadin.pontus.vizcomponent.VizComponent.NodeClickEvent;
import com.vaadin.pontus.vizcomponent.VizComponent.NodeClickListener;
import com.vaadin.pontus.vizcomponent.client.ZoomSettings;
import com.vaadin.pontus.vizcomponent.model.Graph;
import com.vaadin.ui.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class VizualizeModel extends VerticalLayout {

    String name;

    Panel panel;
    Graph graph;
    VizComponent component;
    private Graph.Node selectedNode;

    public VizualizeModel(String name, CoMPArEUI parent) {

        this.name = name;

        panel = new Panel("");
        panel.setWidth("800");
        panel.setHeight("450");

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
                parent.informAboutSelectedNode(name,selectedNode.getId());
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
            if (c != null) edge.setParam("label", "\""+c.toString()+"\"");
        }
        if (!loopFound)
            for (State nextState : state.getNextStates().keySet())
                if (nextState != null) addState(node, state, nextState,alreadyVisitedStates);
    }

    public void greyOutCompletedStates(LinkedList<State> history, State currentState) {
        for (State s : history) {
            Graph.Node node = graph.getNode(s.toString());
            if (node != null) {
                component.addCss(node, "stroke", "grey");
                component.addCss(node, "stroke-width", "3");
                component.addTextCss(node, "fill", "grey");
            }
        }
        Graph.Node node = null;
        if (currentState != null) graph.getNode(currentState.toString());
        if (node != null) {
            component.addCss(node, "stroke-width", "3");
            component.addTextCss(node, "font-weight", "bold");
        }

    }

    public void showSubjectInteraction(Process p) {
        graph = new Graph("", Graph.DIGRAPH);
        for (Message m: p.getMessages()) {
//            LogHelper.logInfo("showSubjectInteraction: "+m.toString()+" from "+p.getSenderOfMessage(m)+" to "+p.getRecipientOfMessage(m));
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


/*            Graph.Edge edge = graph.getEdge(sender,recipient);
            edge.setParam("label", "\""+m.toString().replace(" ","\\n")+"\"");
            edge.setParam("fontsize","10");
            edge.setParam("tooltip","\""+m.toString()+"\"");*/
//            LogHelper.logInfo("subjInteractionViz: Edge between "+sender+" and "+recipient+" with label "+edge.getParam("label")+" "+m.toString());

        }
        component.drawGraph(graph);
    }

    public String getSelectedNodeName() {
        if (selectedNode == null) return null;
        return selectedNode.getId();
    }

}

