package at.jku.ce.CoMPArE.elaborate.changeCommands;

import at.jku.ce.CoMPArE.process.Message;
import at.jku.ce.CoMPArE.process.Subject;

/**
 * Created by oppl on 16/12/2016.
 */
public class RemoveExpectedMessageCommand extends ProcessChangeCommand {

    private Subject subject;
    private Message message;

    public RemoveExpectedMessageCommand(Subject s, Message m) {
        super();
        subject = s;
        message = m;
    }

    @Override
    public boolean perform() {
        if (!subject.getExpectedMessages().contains(message)) return false;
        subject.removeExpectedMessage(message);
        return true;
    }

    @Override
    public boolean undo() {
        if (subject == null || message == null) return false;
        subject.addExpectedMessage(message);
        return true;
    }
}
