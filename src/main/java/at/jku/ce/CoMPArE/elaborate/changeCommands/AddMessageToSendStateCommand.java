package at.jku.ce.CoMPArE.elaborate.changeCommands;

import at.jku.ce.CoMPArE.process.Message;
import at.jku.ce.CoMPArE.process.SendState;

/**
 * Created by oppl on 16/01/2017.
 */
public class AddMessageToSendStateCommand extends ProcessChangeCommand {

    SendState state;
    Message message;

    Message oldMessage;

    public AddMessageToSendStateCommand(SendState state, Message message) {
        this.state = state;
        this.message = message;
    }

    @Override
    public boolean perform() {
        oldMessage = state.getSentMessage();
        state.setSentMessage(message);
        return true;
    }

    @Override
    public boolean undo() {
        state.setSentMessage(oldMessage);
        return true;
    }

    @Override
    public String toString() {
        return "Added input \""+message+"\" to "+state+"\"";
    }
}
