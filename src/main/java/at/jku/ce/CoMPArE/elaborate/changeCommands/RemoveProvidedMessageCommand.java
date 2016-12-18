package at.jku.ce.CoMPArE.elaborate.changeCommands;

import at.jku.ce.CoMPArE.process.Message;
import at.jku.ce.CoMPArE.process.Subject;

/**
 * Created by oppl on 17/12/2016.
 */
public class RemoveProvidedMessageCommand extends ProcessChangeCommand {

    private Subject subject;
    private Message message;

    public RemoveProvidedMessageCommand(Subject s, Message m) {
        super();
        subject = s;
        message = m;
    }

    @Override
    public boolean perform() {
        if (!subject.getProvidedMessages().contains(message)) return false;
        subject.removeProvidedMessage(message);
        return true;
    }

    @Override
    public boolean undo() {
        if (subject == null || message == null) return false;
        subject.addProvidedMessage(message);
        return true;
    }
}
