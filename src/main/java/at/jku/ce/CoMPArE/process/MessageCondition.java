package at.jku.ce.CoMPArE.process;

import java.util.UUID;

/**
 * Created by oppl on 23/11/2016.
 */
public class MessageCondition extends Condition {

    private UUID receivedMessageID;

    public MessageCondition(Message receivedMessage) {
        super(receivedMessage.toString());
        this.receivedMessageID = receivedMessage.getUUID();
    }

    public MessageCondition(MessageCondition messageCondition, State container) {
        super(messageCondition, container);
        receivedMessageID = messageCondition.getUUID();
    }

    public boolean checkCondition(Message messageToBeChecked) {
        if (messageToBeChecked.getUUID().equals(receivedMessageID)) return true;
        return false;
    }

    public Message getMessage() {
        if (parentState == null) return null;
        if (parentState.getParentSubject() == null) return null;
        if (parentState.getParentSubject().getParentProcess() == null) return null;
        return parentState.getParentSubject().getParentProcess().getMessageByUUID(receivedMessageID);
    }

}
