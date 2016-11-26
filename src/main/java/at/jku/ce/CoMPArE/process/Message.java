package at.jku.ce.CoMPArE.process;

/**
 * Created by oppl on 22/11/2016.
 */
public class Message {

    private String name;
    private Object content;

    public Message(String name) {
        this.name = name;
        this.content = null;
    }

    public Message(String name, Object content) {
        this.name = name;
        this.content = content;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return name;
    }
}
