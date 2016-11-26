package at.jku.ce.CoMPArE.process;

/**
 * Created by oppl on 22/11/2016.
 */
public class ActionState extends State {

    private String description;

    public ActionState(String name) {
        super(name);
    }

    public ActionState(String name, String description) {
        super(name);
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
