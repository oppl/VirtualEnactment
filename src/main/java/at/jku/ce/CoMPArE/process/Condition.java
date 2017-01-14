package at.jku.ce.CoMPArE.process;

/**
 * Created by oppl on 22/11/2016.
 */
public class Condition extends ProcessElement {

//    public static Condition noCondition = new Condition("nC");

    private String condition;

    public Condition(String condition) {
        super();
        this.condition = condition;
    }

    public Condition(Condition condition) {
        super(condition);
        this.condition = condition.getCondition();
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    @Override
    public String toString() {
        return condition;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return new Condition(condition);
    }
}
