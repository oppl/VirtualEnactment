package at.jku.ce.CoMPArE.process;

/**
 * Created by oppl on 23/11/2016.
 */
public class MessageCondition extends Condition {

    private Message receivedMessage;

    public MessageCondition(Message receivedMessage) {
        super(receivedMessage.toString());
        this.receivedMessage = receivedMessage;
    }

    public MessageCondition(MessageCondition messageCondition) {
        super(messageCondition);
        receivedMessage = new Message(messageCondition.getMessage());
    }

    public boolean checkCondition(Message messageToBeChecked) {
        if (messageToBeChecked == receivedMessage) return true;
        return false;
    }

    public Message getMessage() {
        return receivedMessage;
    }

}
