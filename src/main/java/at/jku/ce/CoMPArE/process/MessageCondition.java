package at.jku.ce.CoMPArE.process;

/**
 * Created by oppl on 23/11/2016.
 */
public class MessageCondition extends Condition {

    private Message receivedMessage;

    public MessageCondition(String condition) {
        super(condition);
    }

    public MessageCondition(Message receivedMessage) {
        super(receivedMessage.toString());
        this.receivedMessage = receivedMessage;
    }

    public boolean checkCondition(Message messageToBeChecked) {
        if (messageToBeChecked == receivedMessage) return true;
        return false;
    }

    public Message getMessage() {
        return receivedMessage;
    }
}
