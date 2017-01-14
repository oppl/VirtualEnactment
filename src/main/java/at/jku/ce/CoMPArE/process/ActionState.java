package at.jku.ce.CoMPArE.process;

/**
 * Created by oppl on 22/11/2016.
 */
public class ActionState extends State {

    private String description;

    public ActionState(String name, Subject container) {
        super(name, container);
    }

    public ActionState(String name, String description, Subject container) {
        super(name, container);
        this.description = description;
    }

    public ActionState(ActionState s, Subject container) {
        super(s, container);
        description = s.getDescription();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
