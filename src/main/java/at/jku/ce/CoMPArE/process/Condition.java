package at.jku.ce.CoMPArE.process;

/**
 * Created by oppl on 22/11/2016.
 */
public class Condition {

//    public static Condition noCondition = new Condition("nC");

    private String condition;

    public Condition(String condition) {
        this.condition = condition;
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
}
