package at.jku.ce.CoMPArE.execute;

import at.jku.ce.CoMPArE.LogHelper;
import at.jku.ce.CoMPArE.process.*;
import at.jku.ce.CoMPArE.process.Process;

import java.util.*;

/**
 * Created by oppl on 22/11/2016.
 */
public class Instance {

    private Process p;
    private Map<Subject,State> availableStates;
    private Map<Subject, Set<Message>> inputBuffer;
    private Map<Subject, Set<Message>> receivedMessages;
    private Map<Subject, Message> latestProcessedMessages;
    private Map<Subject, LinkedList<State>> history;
    private boolean processHasBeenChanged;

    public Instance(Process p) {
        this.p = p;
        p.setTimestampToNow();
        LogHelper.logInfo("Constructing new instance of "+p+" "+p.getTimestamp());
        availableStates = new HashMap<>();
        inputBuffer = new HashMap<>();
        receivedMessages = new HashMap<>();
        latestProcessedMessages = new HashMap<>();
        processHasBeenChanged = false;
        history = new HashMap<>();
        for (Subject s: p.getSubjects()) {
            if (s.getFirstState() instanceof RecvState)
                availableStates.put(s,null);
            else {
                availableStates.put(s, s.getFirstState());
//                LogHelper.logInfo("instanceConstructor: "+s+" instantiated in "+p);
            }
            inputBuffer.put(s,new HashSet<Message>());
            receivedMessages.put(s,new HashSet<Message>());
            history.put(s,new LinkedList<>());
            latestProcessedMessages.put(s,null);
        }
    }

    public Map<Subject, State> getAvailableStates() {
        return availableStates;
    }

    public State getAvailableStateForSubject(Subject s) {
        return availableStates.get(s);
    }

    public Set<State> getNextStatesOfSubject(Subject s) {
        State currentState = availableStates.get(s);
        if (currentState == null) return null;
        return currentState.getNextStates().keySet();
    }

    public Condition getConditionForStateInSubject(Subject subject, State state) {
        State currentState = availableStates.get(subject);
        if (currentState == null) return null;
        return currentState.getNextStates().get(state);
    }

    public Set<Message> getAvailableMessagesForSubject(Subject subject) {
 //       LogHelper.logInfo("getAvailableMessagesForSubject "+subject+": "+inputBuffer.get(subject).size()+" messages available");
        return inputBuffer.get(subject);
    }

    public Message getLatestProcessedMessageForSubject(Subject subject) {
 //       LogHelper.logInfo("getLatestProcessedMessageForSubject "+subject+": "+latestProcessedMessages.get(subject));
        return latestProcessedMessages.get(subject);

    }

    public boolean subjectCanProgress(Subject subject) {
        State state = availableStates.get(subject);
        if (state == null) return false;
        if (state instanceof RecvState) {
            if (inputBufferContainsAcceptableMessage(subject) == null) return false;
        }

        return true;
    }

    public boolean subjectFinished(Subject s) {
        if (!history.get(s).isEmpty() && getAvailableStateForSubject(s) == null) return true;
        return false;
    }

    public boolean processFinished() {
        boolean finished = true;
        for (State s : getAvailableStates().values()) {
            if (s != null) finished = false;
        }
        return finished;
    }

    public boolean processIsBlocked() {
        boolean isBlocked = true;
        for (State s : getAvailableStates().values()) {
            if (s instanceof ActionState || s instanceof SendState) isBlocked = false;
        }
        return isBlocked;
    }

    public void addMessageToInputBuffer(Subject recipient, Message m) {
        inputBuffer.get(recipient).add(m);
    }

    public State advanceStateForSubject(Subject s, Condition c) {
        State currentState = availableStates.get(s);
        latestProcessedMessages.replace(s,null);
        LogHelper.logInfo("advanceStateForSubject "+s+": now checking advancement options ...");
        if (currentState == null) {
            LogHelper.logInfo("advanceStateForSubject "+s+": instance not yet started");
            if (s.getFirstState() instanceof RecvState) {
                Set<Message> availableMessages = inputBuffer.get(s);
                Set<Message> acceptableMessages = ((RecvState) s.getFirstState()).getRecvdMessages();
                for (Message m : availableMessages) {
                    if (acceptableMessages.contains(m)) {
                        availableStates.replace(s,s.getFirstState());
                        currentState = s.getFirstState();
                        LogHelper.logInfo("advanceStateForSubject "+s+": instantiated");
                    }
                }
                if (currentState == null) return currentState;
            }
            else
                return currentState;
        }
        if (currentState instanceof RecvState) {
            Message m = inputBufferContainsAcceptableMessage(s);
            if (m == null) {
                LogHelper.logInfo("advanceStateForSubject "+s+": necessary message not yet received, still waiting");
                return currentState;
            }
            else {
                LogHelper.logInfo("advanceStateForSubject "+s+": necessary message received, state is now actionable");
                inputBuffer.get(s).remove(m);
                receivedMessages.get(s).add(m);
                latestProcessedMessages.replace(s,m);
            }
        }
        if (currentState instanceof SendState) {
            Subject recipient = p.getRecipientOfMessage(((SendState) currentState).getSentMessage());
            LogHelper.logInfo("advanceStateForSubject "+s+": sending message to "+recipient);
            inputBuffer.get(recipient).add(((SendState) currentState).getSentMessage());
            advanceStateForSubject(recipient, null);
        }
        Map<State,Condition> nextStates = currentState.getNextStates();
        if (nextStates.size() == 1 && nextStates.values().iterator().next() == null) {
            availableStates.replace(s,nextStates.keySet().iterator().next());
            LogHelper.logInfo("advanceStateForSubject "+s+": progressing to next state "+availableStates.get(s));
        }
        else {
            for (State nextState: nextStates.keySet()) {
                Condition conditionToBeChecked = nextStates.get(nextState);
                LogHelper.logInfo("advanceStateForSubject "+s+": checking condition "+ conditionToBeChecked + ", which is a "+conditionToBeChecked.getClass());
                if (conditionToBeChecked.equals(c)) {
                    availableStates.replace(s, nextState);
                    LogHelper.logInfo("advanceStateForSubject "+s+": progressing to next state under condition "+ conditionToBeChecked);
                    break;
                }
                if (conditionToBeChecked instanceof MessageCondition) {
                    for (Message m: receivedMessages.get(s)) {
                        LogHelper.logInfo("advanceStateForSubject "+s+": checking message "+m);
                        if (((MessageCondition) conditionToBeChecked).checkCondition(m)) {
                            availableStates.replace(s, nextState);
                            LogHelper.logInfo("advanceStateForSubject "+s+": progressing to next state because of message condition "+ conditionToBeChecked);
                            break;
                        }
                    }
                }
            }
        }
        if (nextStates.size() == 0) {
            LogHelper.logInfo("advanceStateForSubject "+s+": instance finished");
            availableStates.replace(s, null);
        }
        history.get(s).addFirst(currentState);
        return availableStates.get(s);
    }

    private Message inputBufferContainsAcceptableMessage(Subject s) {
        State currentState = availableStates.get(s);
        if (currentState instanceof RecvState) {
            Set<Message> availableMessages = inputBuffer.get(s);
            Set<Message> acceptableMessages = ((RecvState) currentState).getRecvdMessages();
            for (Message m : availableMessages) {
                if (acceptableMessages.contains(m)) return m;
            }
        }
        return null;
    }

    public void putMessageInInputbuffer(Subject s, Message m) {
        inputBuffer.get(s).add(m);
        advanceStateForSubject(s, null);
    }

    public Process getProcess() {
        return p;
    }

    public LinkedList<State> getHistoryForSubject(Subject s) {
        return history.get(s);
    }

    public void addInputBufferAndHistoryForSubject(Subject s) {
        inputBuffer.put(s, new HashSet<Message>());
        history.put(s,new LinkedList<State>());
    }

    public boolean isProcessHasBeenChanged() {
        return processHasBeenChanged;
    }

    public void setProcessHasBeenChanged(boolean processHasBeenChanged) {
        this.processHasBeenChanged = processHasBeenChanged;
    }
}
