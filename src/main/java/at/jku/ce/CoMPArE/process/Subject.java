package at.jku.ce.CoMPArE.process;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by oppl on 22/11/2016.
 */
public class Subject {

    private String name;

    private State firstState;

    private Set<Message> expectedMessages;
    private Set<Message> providedMessages;

    public Subject(String name) {
        this.name = name;
        this.firstState = null;
        this.expectedMessages = new HashSet<Message>();
        this.providedMessages = new HashSet<Message>();    }

    public State setFirstState(State firstState) {
        return this.firstState = firstState;
    }

    public State getFirstState() {
        return this.firstState;
    }

    @Override
    public String toString() {
        return name;
    }

    public Set<State> getStates() { return this.getStates(firstState, new HashSet<State>());}

    public Set<State> getStates(State state, Set<State> states) {
        if (states.contains(state)) return null;
        states.add(state);
        for (State next: state.getNextStates().keySet()) {
            Set<State> nextStates = this.getStates(next, states);
            if (nextStates!=null) states.addAll(nextStates);
        }
        return states;
    }

    public Set<Message> getSentMessages() {
        if (firstState == null) return new HashSet<>();
        return this.getSentMessages(firstState, new HashSet<Message>());
    }

    private Set<Message> getSentMessages(State state, Set<Message> messages) {
        if (state instanceof SendState) {
            if (messages.contains(((SendState) state).getSentMessage())) return null;
            messages.add(((SendState) state).getSentMessage());
        }
        for (State next: state.getNextStates().keySet()) {
            Set<Message> nextMessages = this.getSentMessages(next, messages);
            if (nextMessages!=null) messages.addAll(nextMessages);
        }
        return messages;
    }

    public Set<Message> getRecvdMessages() {
        return this.getRecvdMessages(firstState, new HashSet<Message>());
    }

    private Set<Message> getRecvdMessages(State state, Set<Message> messages) {
        if (state == null) return new HashSet<Message>();
        if (state instanceof RecvState) {
            if (messages.contains(((RecvState) state).getRecvdMessages().iterator().next())) return null;
            messages.addAll(((RecvState) state).getRecvdMessages());
        }
        for (State next: state.getNextStates().keySet()) {
            Set<Message> nextMessages = this.getRecvdMessages(next, messages);
            if (nextMessages!=null) messages.addAll(nextMessages);
        }
        return messages;
    }

    public Set<Message> getExpectedMessages() {
        return expectedMessages;
    }

    public void addExpectedMessage(Message expectedMessage) {
        this.expectedMessages.add(expectedMessage);
    }

    public void removeExpectedMessage(Message expectedMessage) {
        this.expectedMessages.remove(expectedMessage);
    }

    public Set<Message> getProvidedMessages() {
        return providedMessages;
    }

    public void addProvidedMessage(Message providedMessage) {
        this.providedMessages.add(providedMessage);
    }

    public void removeProvidedMessage(Message providedMessage) {
        this.providedMessages.remove(providedMessage);
    }

    public Set<State> getPredecessorStates(State target) {
        Set<State> predecessorStates = getPredecessorStates(this.firstState, target, new HashSet<>());
        if (predecessorStates == null) return new HashSet<>();
        return predecessorStates;
    }

    private Set<State> getPredecessorStates(State state, State target, Set<State> predecessorStates) {
        if (state.getNextStates().keySet().contains(target)) predecessorStates.add(state);
        else {
            for (State s: state.getNextStates().keySet()) {
                Set<State> collectedStates = getPredecessorStates(s, target, predecessorStates);
                if (collectedStates != null) predecessorStates.addAll(collectedStates);
            }
        }
        return predecessorStates;
    }

}
