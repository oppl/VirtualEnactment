package at.jku.ce.CoMPArE.process;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Created by oppl on 22/11/2016.
 */
public class RecvState extends State {

    private Set<UUID> recvdMessageIDs;

    public RecvState(String name, Subject container) {
        super(name, container);
        recvdMessageIDs = new HashSet<>();
    }

    public RecvState(String name, Message recvdMessage, Subject container) {
        this(name, container);
        container.getParentProcess().addMessage(recvdMessage);
        recvdMessageIDs.add(recvdMessage.getUUID());
    }

    public RecvState(String name, Set<Message> recvdMessages, Subject container) {
        this(name, container);
        for (Message m: recvdMessages) {
            this.recvdMessageIDs.add(m.getUUID());
            container.getParentProcess().addMessage(m);
        }
    }

    public RecvState(RecvState s, Subject container) {
        super(s,container);
        recvdMessageIDs = new HashSet<>();
        for (UUID messageID:s.recvdMessageIDs) {
            recvdMessageIDs.add(messageID);
        }
    }

    public Set<Message> getRecvdMessages() {
        Set<Message> recvdMessages = new HashSet<>();
        for (UUID recvdMessageID: recvdMessageIDs) {
            recvdMessages.add(parentSubject.getParentProcess().getMessageByUUID(recvdMessageID));
        }
        return recvdMessages;
    }

    public void addRecvdMessage(Message recvdMessage) {
        parentSubject.getParentProcess().addMessage(recvdMessage);
        this.recvdMessageIDs.add(recvdMessage.getUUID());
    }
}
