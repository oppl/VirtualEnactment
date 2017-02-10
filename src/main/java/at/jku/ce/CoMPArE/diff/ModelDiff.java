package at.jku.ce.CoMPArE.diff;

import at.jku.ce.CoMPArE.elaborate.ProcessChangeTransaction;
import at.jku.ce.CoMPArE.process.*;
import at.jku.ce.CoMPArE.process.Process;

import java.util.Vector;

/**
 * Created by oppl on 10/02/2017.
 */
public class ModelDiff {

    Vector<InsertState> addedStates;
    Vector<State> removedStates;

    Vector<Message> addedMessages;
    Vector<Message> removedMessages;

    Vector<Subject> addedSubjects;
    Vector<Subject> removedSubjects;

    Vector<Transition> addedTransitions;
    Vector<Transition> removedTransitions;

    Vector<MessageBuffer> addedExpectedMessages;
    Vector<MessageBuffer> removedExpectedMessages;

    Vector<MessageBuffer> addedProvidedMessages;
    Vector<MessageBuffer> removedProvidedMessages;


    public ModelDiff(Process source, Process dest) {

        addedStates = new Vector<>();
        removedStates = new Vector<>();

        addedMessages = new Vector<>();
        removedMessages = new Vector<>();

        addedSubjects = new Vector<>();
        removedSubjects = new Vector<>();

        addedTransitions = new Vector<>();
        removedTransitions = new Vector<>();

        addedExpectedMessages = new Vector<>();
        removedExpectedMessages = new Vector<>();

        addedProvidedMessages = new Vector<>();
        removedProvidedMessages = new Vector<>();

        for (Subject s: source.getSubjects()) {
            if (!dest.getSubjects().contains(s)) {
                removedSubjects.add(s); // subject has been removed
                removedStates.addAll(s.getStates());

            }
            else { // subject has remained in process
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
            }
        }

        for (Subject s: dest.getSubjects()) {
            if (!source.getSubjects().contains(s)) { // subject has been added
                addedSubjects.add(s);
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

    public Vector<InsertState> getAddedStates() {
        return addedStates;
    }

    public Vector<State> getRemovedStates() {
        return removedStates;
    }

    public Vector<Message> getAddedMessages() {
        return addedMessages;
    }

    public Vector<Message> getRemovedMessages() {
        return removedMessages;
    }

    public Vector<Subject> getAddedSubjects() {
        return addedSubjects;
    }

    public Vector<Subject> getRemovedSubjects() {
        return removedSubjects;
    }

    public Vector<Transition> getAddedTransitions() {
        return addedTransitions;
    }

    public Vector<Transition> getRemovedTransitions() {
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

    private  class InsertState {
        private State newState;
        private State beforeTarget;

        public InsertState(State newState, State beforeTarget) {
            this.newState = newState;
            this.beforeTarget = beforeTarget;
        }

        public State getNewState() {
            return newState;
        }

        public State getBeforeTarget() {
            return beforeTarget;
        }
    }
}
