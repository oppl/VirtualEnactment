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

        Subject s1 = new Subject("Employee",p);
        Subject s2 = new Subject("Secretary", p);
        Subject s3 = new Subject("Boss", p);

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

        State s = s1.setFirstState(new ActionState("Fill Application Form",s1));
        s = s.addNextState(new SendState("Send Application Form", applicationForm, s1));
        s = s.addNextState(new RecvState("Wait for Confirmation", confirmation, s1));
        s = s.addNextState(new ActionState("Book Holiday",s1));

        s = s2.setFirstState(new RecvState("Wait for Application Form", applicationForm, s2));
        s = s.addNextState(new ActionState("Check for Conflicts", s2));
        s = s.addNextState(new SendState("Forward checked Application", checkedApplicationForm, s2));
        s = s.addNextState(new RecvState("Wait for confirmed Application", confirmedApplication, s2));
        s = s.addNextState(new ActionState("File confirmed Application", s2));
        s = s.addNextState(new SendState("Send Confirmation",confirmation, s2));

        s = s3.setFirstState(new RecvState("Wait for checked Application From", checkedApplicationForm, s3));
        s = s.addNextState(new ActionState("Confirm Application", s3));
        s = s.addNextState(new SendState("Return confirmed Application Form", confirmedApplication, s3));

        return p;
    }

    public static Process getComplexDemoProcess() {
        Process p = new Process("Vacation Application");

        Subject s1 = new Subject("Employee", p);
        Subject s2 = new Subject("Secretary", p);
        Subject s3 = new Subject("Boss", p);

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

        State s = s1.setFirstState(new ActionState("Fill Application Form", s1));
        s = s.addNextState(new SendState("Send Application Form", applicationForm, s1));
        s = s.addNextState(new RecvState("Wait for Decision", confirmation, s1));
        ((RecvState) s).addRecvdMessage(rejection);
        s.addNextState(new ActionState("Book Holiday", s1), new MessageCondition(confirmation,s));
        s.addNextState(new ActionState("Be angry", s1), new MessageCondition(rejection,s));

        s = s2.setFirstState(new RecvState("Wait for Application Form", applicationForm, s2));
        s = s.addNextState(new ActionState("Check for Conflicts", s2));
        State temp = s.addNextState(new SendState("Forward checked Application", checkedApplicationForm, s2), new Condition("no Conflicts", s));
        s.addNextState(new SendState("Decline Application because of Conflicts", rejection, s2), new Condition("Conflicts Identified", s));
        s = temp;
        s = s.addNextState(new RecvState("Wait for Decision on Application", confirmedApplication, s2));
        ((RecvState) s).addRecvdMessage(declinedApplication);
        s = s.addNextState(new ActionState("File decided Application", s2));
        s.addNextState(new SendState("Send confirmed Application",confirmation, s2),new MessageCondition(confirmedApplication, s));
        s.addNextState(new SendState("Send declined Application",rejection, s2), new MessageCondition(declinedApplication, s));

        s = s3.setFirstState(new RecvState("Wait for checked Application From", checkedApplicationForm, s3));
        s = s.addNextState(new ActionState("Decide on Application", s3));
        s.addNextState(new SendState("Return confirmed Application Form", confirmedApplication, s3), new Condition("confirm", s));
        s.addNextState(new SendState("Return rejected Application Form", declinedApplication, s3), new Condition("reject", s));

        return p;
    }

    public static Process getSplitJoinDemoProcess() {
        Process p = new Process("Split/Join Demo");

        Subject s1 = new Subject("Actor",p);
        p.addSubject(s1);

        State s = s1.setFirstState(new ActionState("Decide whether to go left or right",s1));
        State left = new ActionState("Going left",s1);
        State right = new ActionState("Going right",s1);
        s.addNextState(left, new Condition("left",s));
        s.addNextState(right,new Condition("right",s));
        State straightAgain = new ActionState("Go straight ahead again",s1);
        left.addNextState(straightAgain);
        right.addNextState(straightAgain);

        return p;
    }

    public static Process getLoopDemoProcess() {

        Process p = new Process("Loop Demo");

        Subject s1 = new Subject("Actor",p);
        p.addSubject(s1);

        State s = s1.setFirstState(new ActionState("Prepare for Jumping",s1));
        State loopState = new ActionState("Jump",s1);
        State finalState = new ActionState("Relax",s1);
        State restState = new ActionState("Rest",s1);
        s.addNextState(loopState);
        loopState.addNextState(finalState, new Condition("done jumping",loopState));
        loopState.addNextState(restState,new Condition("continue jumping",loopState));
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
