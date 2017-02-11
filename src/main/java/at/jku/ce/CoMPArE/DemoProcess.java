package at.jku.ce.CoMPArE;

import at.jku.ce.CoMPArE.process.*;
import at.jku.ce.CoMPArE.process.Process;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by oppl on 22/11/2016.
 */
public class DemoProcess {

    private static void addSubjectToProcess(Subject s, Process p) {
        p.addSubject(s);
    }

    private static void addMessageToProcess(Message m, Process p) {
        p.addMessage(m);
    }

    public static Process getDemoProcess() {
        Process p = new Process("Vacation Application (incomplete version)");

        Subject s1 = new Subject("Employee");
        Subject s2 = new Subject("Secretary");
        Subject s3 = new Subject("Boss");

        addSubjectToProcess(s1,p);
        addSubjectToProcess(s2,p);
        addSubjectToProcess(s3,p);

        Message applicationForm = new Message("Filled Application Form");
        Message checkedApplicationForm = new Message("Checked Application Form");
        Message confirmedApplication = new Message("Confirmed Application Form");
        Message confirmation = new Message("Confirmation");

        addMessageToProcess(applicationForm, p);
        addMessageToProcess(checkedApplicationForm, p);
        addMessageToProcess(confirmedApplication, p);
        addMessageToProcess(confirmation, p);

        State s = s1.setFirstState(new ActionState("Fill Application Form"));
        s = s.addNextState(new SendState("Send Application Form", applicationForm));
        s = s.addNextState(new RecvState("Wait for Confirmation", confirmation));
        s = s.addNextState(new ActionState("Book Holiday"));

        s = s2.setFirstState(new RecvState("Wait for Application Form", applicationForm));
        s = s.addNextState(new ActionState("Check for Conflicts"));
        s = s.addNextState(new SendState("Forward checked Application", checkedApplicationForm));
        s = s.addNextState(new RecvState("Wait for confirmed Application", confirmedApplication));
        s = s.addNextState(new ActionState("File confirmed Application"));
        s = s.addNextState(new SendState("Send Confirmation",confirmation));

        s = s3.setFirstState(new RecvState("Wait for checked Application From", checkedApplicationForm));
        s = s.addNextState(new ActionState("Confirm Application"));
        s = s.addNextState(new SendState("Return confirmed Application Form", confirmedApplication));

        return p;
    }

    public static Process getComplexDemoProcess() {
        Process p = new Process("Vacation Application");

        Subject s1 = new Subject("Employee");
        Subject s2 = new Subject("Secretary");
        Subject s3 = new Subject("Boss");

        addSubjectToProcess(s1,p);
        addSubjectToProcess(s2,p);
        addSubjectToProcess(s3,p);

        Message applicationForm = new Message("Filled Application Form");
        Message checkedApplicationForm = new Message("Checked Application Form");
        Message confirmedApplication = new Message("Confirmed Application Form");
        Message declinedApplication = new Message("Declined Application Form");
        Message confirmation = new Message("Confirmation");
        Message rejection = new Message("Rejection");

        addMessageToProcess(applicationForm, p);
        addMessageToProcess(checkedApplicationForm, p);
        addMessageToProcess(confirmedApplication, p);
        addMessageToProcess(declinedApplication, p);
        addMessageToProcess(confirmation, p);
        addMessageToProcess(rejection, p);

        State s = s1.setFirstState(new ActionState("Fill Application Form"));
        s = s.addNextState(new SendState("Send Application Form", applicationForm));
        s = s.addNextState(new RecvState("Wait for Decision", confirmation));
        ((RecvState) s).addRecvdMessage(rejection);
        s.addNextState(new ActionState("Book Holiday"), new MessageCondition(confirmation.getUUID()));
        s.addNextState(new ActionState("Be angry"), new MessageCondition(rejection.getUUID()));

        s = s2.setFirstState(new RecvState("Wait for Application Form", applicationForm));
        s = s.addNextState(new ActionState("Check for Conflicts"));
        State temp = s.addNextState(new SendState("Forward checked Application", checkedApplicationForm), new Condition("no Conflicts"));
        s.addNextState(new SendState("Decline Application because of Conflicts", rejection), new Condition("Conflicts Identified"));
        s = temp;
        s = s.addNextState(new RecvState("Wait for Decision on Application", confirmedApplication));
        ((RecvState) s).addRecvdMessage(declinedApplication);
        s = s.addNextState(new ActionState("File decided Application"));
        s.addNextState(new SendState("Send confirmed Application",confirmation),new MessageCondition(confirmedApplication.getUUID()));
        s.addNextState(new SendState("Send declined Application",rejection), new MessageCondition(declinedApplication.getUUID()));

        s = s3.setFirstState(new RecvState("Wait for checked Application From", checkedApplicationForm));
        s = s.addNextState(new ActionState("Decide on Application"));
        s.addNextState(new SendState("Return confirmed Application Form", confirmedApplication), new Condition("confirm"));
        s.addNextState(new SendState("Return rejected Application Form", declinedApplication), new Condition("reject"));

        return p;
    }

    public static Process getSplitJoinDemoProcess() {
        Process p = new Process("Split/Join Demo");

        Subject s1 = new Subject("Actor");
        p.addSubject(s1);

        State s = s1.setFirstState(new ActionState("Decide whether to go left or right"));
        State left = new ActionState("Going left");
        State right = new ActionState("Going right");
        s.addNextState(left, new Condition("left"));
        s.addNextState(right,new Condition("right"));
        State straightAgain = new ActionState("Go straight ahead again");
        left.addNextState(straightAgain);
        right.addNextState(straightAgain);

        return p;
    }

    public static Process getLoopDemoProcess() {

        Process p = new Process("Loop Demo");

        Subject s1 = new Subject("Actor");
        p.addSubject(s1);

        State s = s1.setFirstState(new ActionState("Prepare for Jumping"));
        State loopState = new ActionState("Jump");
        State finalState = new ActionState("Relax");
        State restState = new ActionState("Rest");
        s.addNextState(loopState);
        loopState.addNextState(finalState, new Condition("done jumping"));
        loopState.addNextState(restState,new Condition("continue jumping"));
        restState.addNextState(loopState);

        return p;
    }

    public static Set<Process> getDemoProcesses() {
        Set<Process> demoProcesses = new HashSet<>();
        demoProcesses.add(DemoProcess.getDemoProcess());
        demoProcesses.add(DemoProcess.getComplexDemoProcess());
        demoProcesses.add(DemoProcess.getSplitJoinDemoProcess());
        demoProcesses.add(DemoProcess.getLoopDemoProcess());

        return demoProcesses;
    }
}
