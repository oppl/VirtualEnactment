package at.jku.ce.CoMPArE.process;

import java.util.UUID;

/**
 * Created by oppl on 23/11/2016.
 */
public class MessageCondition extends Condition {

    private UUID receivedMessageID;

    public MessageCondition(Message receivedMessage, State container) {
        super(receivedMessage.toString(), container);
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
        return parentState.getParentSubject().getParentProcess().getMessageByUUID(receivedMessageID);
    }

}
