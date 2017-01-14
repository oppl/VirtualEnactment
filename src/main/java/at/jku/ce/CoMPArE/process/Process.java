package at.jku.ce.CoMPArE.process;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by oppl on 22/11/2016.
 */
public class Process extends ProcessElement {

    private String name;
    private Date timestamp;

    private Set<Subject> subjects;

    public Process(String name) {
        super();
        this.name = name;
        subjects = new HashSet<Subject>();
        this.timestamp = new Date();
    }

    public Process(Process p) {
        super(p);
        name = p.toString();
        timestamp = p.getTimestamp();
        subjects = new HashSet<Subject>();

        for (Subject s: p.getSubjects()) {
            subjects.add(new Subject(s));
        }
    }

    public void addSubject(Subject s) {
        subjects.add(s);
    }

    public Set<Subject> getSubjects() {
        return subjects;
    }

    public Set<Message> getMessages() {
        HashSet<Message> messages = new HashSet<>();
        for (Subject s: subjects) {
            messages.addAll(s.getSentMessages());
            messages.addAll(s.getRecvdMessages());
//            messages.addAll(s.getExpectedMessages());
//            messages.addAll(s.getProvidedMessages());
        }
        return messages;
    }

    public Subject getSenderOfMessage(Message message) {
        for (Subject s: subjects) {
            if (s.getSentMessages().contains(message)) return s;
            if (s.getExpectedMessages().contains(message)) return s;
        }
        return null;
    }

    public Subject getRecipientOfMessage(Message message) {
        for (Subject s: subjects) {
            if (s.getRecvdMessages().contains(message)) return s;
            if (s.getProvidedMessages().contains(message)) return s;
        }
        return null;
    }

    public State getStateWithName(String name) {
        for (Subject s: subjects) {
            for (State state: s.getStates()) {
                if (state.toString().equals(name)) return state;
            }
        }
        return null;
    }

    public Subject getSubjectWithName(String name) {
        for (Subject s: subjects) {
            if (s.toString().equals(name)) return s;
        }
        return null;
    }

    public Subject getSubjectWithState(State state) {
        for (Subject s: subjects) {
            if (s.getStates().contains(state)) return s;
        }
        return null;
    }

    @Override
    public String toString() {
        return name;
    }

    public void setTimestampToNow() {
        timestamp = new Date();
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Date getTimestamp() {
        return timestamp;
    }

}
