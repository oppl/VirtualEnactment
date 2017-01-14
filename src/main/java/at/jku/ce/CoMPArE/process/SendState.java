package at.jku.ce.CoMPArE.process;

/**
 * Created by oppl on 22/11/2016.
 */
public class SendState extends State {

    private Message sentMessage;

    public SendState(String name) {
        super(name);
        sentMessage = null;
    }

    public SendState(String name, Message sentMessage) {
        super(name);
        this.sentMessage = sentMessage;
    }

    public SendState(SendState s, Subject container) {
        super(s,container);
        sentMessage = new Message(s.getSentMessage());
    }

    public Message getSentMessage() {
        return sentMessage;
    }

    public void setSentMessage(Message sentMessage) {
        this.sentMessage = sentMessage;
    }
}
