package at.jku.ce.CoMPArE.elaborate.changeCommands;

import at.jku.ce.CoMPArE.execute.Instance;
import at.jku.ce.CoMPArE.process.Process;
import at.jku.ce.CoMPArE.process.Subject;

/**
 * Created by oppl on 17/12/2016.
 */
public class AddSubjectCommand extends ProcessChangeCommand {

    Subject subject;
    Process process;
    Instance instance;

    public AddSubjectCommand(Process p, Subject s, Instance i) {
        super();
        subject = s;
        process = p;
        instance = i;
    }

    @Override
    public boolean perform() {
        for (Subject s : process.getSubjects()) {
            if (subject.toString().equals(Subject.ANONYMOUS) && s.toString().equals(Subject.ANONYMOUS)) {
                return true;
            }
            if (s.toString().equals(subject.toString())) {
                return false;
            }
        }
        process.addSubject(subject);
        instance.addInputBufferAndHistoryForSubject(subject);
        return true;
    }

    @Override
    public boolean undo() {
        return false;
    }
}
