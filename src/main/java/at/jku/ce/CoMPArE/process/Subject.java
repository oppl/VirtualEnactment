package at.jku.ce.CoMPArE.process;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

import java.util.*;

/**
 * Created by oppl on 22/11/2016.
 */
public class Subject extends ProcessElement {

    public static String ANONYMOUS = "Anonymous";

    private String name;

    private UUID firstState;

    @XStreamOmitField
    private Process parentProcess;

    private Set<State> states;
    private Set<Message> expectedMessages;
    private Set<Message> providedMessages;

    public Subject(String name, Process parentProcess) {
        super();
        this.name = name;
        this.parentProcess = parentProcess;
        this.firstState = null;
        this.states = new HashSet<>();
        this.expectedMessages = new HashSet<>();
        this.providedMessages = new HashSet<>();
    }

    public Subject(Subject s, Process newProcess) {
        super(s);
        this.name = s.toString();
        this.parentProcess = newProcess;
        this.states = new HashSet<>();

        for (State state: s.getStates()) {
            State newState = null;
            if (state instanceof ActionState) newState = new ActionState((ActionState) state, this);
            if (state instanceof SendState) newState = new SendState((SendState) state, this);
            if (state instanceof RecvState) newState = new RecvState((RecvState) state, this);
            this.addState(newState);
            for (State nextState: state.getNextStates().keySet()) {
                Condition clonedCondition = null;
                Condition originalCondition = state.getNextStates().get(nextState);
                if (originalCondition instanceof MessageCondition) {
                    clonedCondition = new MessageCondition((MessageCondition) originalCondition, state);
                }
                else {
                    if (originalCondition != null) clonedCondition = new Condition(originalCondition, state);
                }
                newState.addNextState(nextState,clonedCondition);
            }
        }

        this.firstState = s.firstState;
        this.expectedMessages = new HashSet<>();
        this.providedMessages = new HashSet<>();
        for (Message m:s.getExpectedMessages()) {
            expectedMessages.add(new Message(m));
        }
        for (Message m:s.getProvidedMessages()) {
            providedMessages.add(new Message(m));
        }

    }

    public Process getParentProcess() {
        return parentProcess;
    }

    public void addState(State state) {
        states.add(state);
    }

    public void removeState(State state) {
        states.remove(state.getUUID());
    }

    public State setFirstState(State firstState) {
        if (!states.contains(firstState)) {
            states.add(firstState);
        }
        this.firstState = firstState.getUUID();
        return firstState;
    }

    public State getFirstState() {
        return getStateByUUID(firstState);
    }

    @Override
    public String toString() {
        return name;
    }

    public Collection<State> getStates() { return this.states;}

    public State getStateByUUID(UUID stateID) {
        for (State state: states)
            if (state.getUUID().equals(stateID)) return state;
        return null;
    }

    public State getSendState(Message m) {
        for (State s: getStates()) {
            if (s instanceof SendState && ((SendState) s).getSentMessage().equals(m)) return s;
        }
        return null;
    }

    public Set<Message> getSentMessages() {
        HashSet<Message> sentMessages = new HashSet<>();
        for (State s: states) {
            if (s instanceof SendState) {
                Message sentMessage = ((SendState) s).getSentMessage();
                sentMessages.add(sentMessage);
            }
        }
        return sentMessages;
    }

    public Set<Message> getRecvdMessages() {
        HashSet<Message> recvdMessages = new HashSet<>();
        for (State s: states) {
            if (s instanceof RecvState) {
                Set<Message> recvdMessagesFromState = ((RecvState) s).getRecvdMessages();
                recvdMessages.addAll(recvdMessagesFromState);
            }
        }
        return recvdMessages;
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
        Set<State> predecessorStates = new HashSet<>();
        for (State s: states) {
            for (State nextState: s.getNextStates().keySet()) {
                if (nextState.equals(target)) predecessorStates.add(s);
            }
        }
        return predecessorStates;
    }

    public void reconstructParentRelations(Process p) {
        this.parentProcess = p;
        for (State s: states) {
            s.reconstructParentRelations(this);
        }
    }

}
