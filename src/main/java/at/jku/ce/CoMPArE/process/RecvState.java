package at.jku.ce.CoMPArE.process;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by oppl on 22/11/2016.
 */
public class RecvState extends State {

    private Set<Message> recvdMessages;

    public RecvState(String name) {
        super(name);
        recvdMessages = new HashSet<>();
    }

    public RecvState(String name, Message recvdMessage) {
        this(name);
        recvdMessages.add(recvdMessage);
    }

    public RecvState(String name, Set<Message> recvdMessages) {
        this(name);
        this.recvdMessages.addAll(recvdMessages);

    }

    public RecvState(RecvState s, Subject container) {
        super(s,container);
        recvdMessages = new HashSet<>();
        for (Message m:s.getRecvdMessages()) {
            recvdMessages.add(new Message(m));
        }
    }

    public Set<Message> getRecvdMessages() {
        return recvdMessages;
    }

    public void addRecvdMessage(Message recvdMessage) {
        this.recvdMessages.add(recvdMessage);
    }
}
