package at.jku.ce.CoMPArE.diff;

import at.jku.ce.CoMPArE.LogHelper;
import at.jku.ce.CoMPArE.elaborate.ProcessChangeTransaction;
import at.jku.ce.CoMPArE.process.*;
import at.jku.ce.CoMPArE.process.Process;
import sun.rmi.runtime.Log;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by oppl on 10/02/2017.
 */
public class ModelDiff {

    Set<State> addedStates;
    Set<State> removedStates;

    Set<Message> addedMessages;
    Set<Message> removedMessages;

    Set<Subject> addedSubjects;
    Set<Subject> removedSubjects;

    Set<Transition> addedTransitions;
    Set<Transition> removedTransitions;

    Set<MessageBuffer> addedExpectedMessages;
    Set<MessageBuffer> removedExpectedMessages;

    Set<MessageBuffer> addedProvidedMessages;
    Set<MessageBuffer> removedProvidedMessages;


    public ModelDiff(Process source, Process dest) {
        LogHelper.logInfo("Diff process started");

        addedStates = new HashSet<>();
        removedStates = new HashSet<>();

        addedMessages = new HashSet<>();
        removedMessages = new HashSet<>();

        addedSubjects = new HashSet<>();
        removedSubjects = new HashSet<>();

        addedTransitions = new HashSet<>();
        removedTransitions = new HashSet<>();

        addedExpectedMessages = new HashSet<>();
        removedExpectedMessages = new HashSet<>();

        addedProvidedMessages = new HashSet<>();
        removedProvidedMessages = new HashSet<>();

        for (Subject s: source.getSubjects()) {
            LogHelper.logInfo("now examining subject "+s);
            if (!dest.getSubjects().contains(s)) {
                removedSubjects.add(s); // subject has been removed
                removedStates.addAll(s.getStates());
                removedTransitions.addAll(s.getTransitions());
            }
            else { // subject has remained in process
//                LogHelper.logInfo("subject has remained in process");
                Subject destSubject = dest.getSubjectByUUID(s.getUUID());
                for (Message m: s.getExpectedMessages()) {
                    if (!destSubject.getExpectedMessages().contains(m)) removedExpectedMessages.add(new MessageBuffer(s,m));
                }
                for (Message m: destSubject.getExpectedMessages()) {
                    if (!s.getExpectedMessages().contains(m)) addedExpectedMessages.add(new MessageBuffer(s,m));
                }
                for (Message m: s.getProvidedMessages()) {
                    if (!destSubject.getProvidedMessages().contains(m)) removedProvidedMessages.add(new MessageBuffer(s,m));
                }
                for (Message m: destSubject.getProvidedMessages()) {
                    if (!s.getProvidedMessages().contains(m)) addedProvidedMessages.add(new MessageBuffer(s,m));
                }
                for (State state: s.getStates()) {
                    if (!destSubject.getStates().contains(state)) removedStates.add(state);
                }
                for (State state: destSubject.getStates()) {
//                    LogHelper.logInfo("checking if "+state+" has already been in process");
                    if (!s.getStates().contains(state)) {
                        addedStates.add(state);
                        LogHelper.logInfo("found added state "+state);
                    }
//                    else LogHelper.logInfo(state+" already contained (subject contains "+s.getStates().size()+" states)");
                }
                for (Transition transition: s.getTransitions()) {
                    if (!destSubject.getTransitions().contains(transition)) removedTransitions.add(transition);
                }
                for (Transition transition: destSubject.getTransitions()) {
                    if (!s.getTransitions().contains(transition)) {
                        LogHelper.logInfo("found added transition "+transition);
                        addedTransitions.add(transition);
                    }
                }
            }
        }

        for (Subject s: dest.getSubjects()) {
            if (!source.getSubjects().contains(s)) { // subject has been added
                addedSubjects.add(s);
                addedStates.addAll(s.getStates());
                addedTransitions.addAll(s.getTransitions());
                for (Message m: s.getExpectedMessages()) {
                    addedExpectedMessages.add(new MessageBuffer(s,m));
                }
                for (Message m: s.getProvidedMessages()) {
                    addedProvidedMessages.add(new MessageBuffer(s,m));
                }
            }
        }

        for (Message m: source.getMessages()) {
            if (!dest.getMessages().contains(m)) removedMessages.add(m); // messages have been removed
        }

        for (Message m: dest.getMessages()) {
            if (!source.getMessages().contains(m)) addedMessages.add(m); // messages have been added
        }

    }

    public ProcessChangeTransaction getProcessChangeTransaction() {
        ProcessChangeTransaction transaction = new ProcessChangeTransaction();
        //todo: create transactions from change lists
        return transaction;
    }

    public Set<State> getAddedStates() {
        return addedStates;
    }

    public Set<State> getRemovedStates() {
        return removedStates;
    }

    public Set<Message> getAddedMessages() {
        return addedMessages;
    }

    public Set<Message> getRemovedMessages() {
        return removedMessages;
    }

    public Set<Subject> getAddedSubjects() {
        return addedSubjects;
    }

    public Set<Subject> getRemovedSubjects() {
        return removedSubjects;
    }

    public Set<Transition> getAddedTransitions() {
        return addedTransitions;
    }

    public Set<Transition> getRemovedTransitions() {
        return removedTransitions;
    }

    private  class MessageBuffer {
        private Subject subject;
        private Message message;

        public MessageBuffer(Subject subject, Message message) {
            this.subject = subject;
            this.message = message;
        }


        public Subject getSubject() {
            return subject;
        }

        public Message getMessage() {
            return message;
        }
    }

}
