package at.jku.ce.CoMPArE.process;

import at.jku.ce.CoMPArE.LogHelper;

import java.util.UUID;

/**
 * Created by oppl on 22/11/2016.
 */
public class SendState extends State {

    private UUID sentMessageID;

    public SendState(String name) {
        super(name);
        sentMessageID = null;
    }

    public SendState(String name, Message sentMessage) {
        super(name);
        this.sentMessageID = sentMessage.getUUID();
    }

    public SendState(SendState s, Subject container) {
        super(s,container);
        sentMessageID = container.getParentProcess().getMessageByUUID(s.sentMessageID).getUUID();
    }

    public Message getSentMessage() {
        if (sentMessageID == null) return null;
        return parentSubject.getParentProcess().getMessageByUUID(sentMessageID);
    }

    public void setSentMessage(Message sentMessage) {
        parentSubject.getParentProcess().addMessage(sentMessage);
        this.sentMessageID = sentMessage.getUUID();
    }
}
