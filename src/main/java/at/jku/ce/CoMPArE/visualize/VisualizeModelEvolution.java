package at.jku.ce.CoMPArE.visualize;

import at.jku.ce.CoMPArE.LogHelper;
import at.jku.ce.CoMPArE.diff.ModelDiff;
import at.jku.ce.CoMPArE.elaborate.ProcessChangeHistory;
import at.jku.ce.CoMPArE.elaborate.ProcessChangeTransaction;
import at.jku.ce.CoMPArE.process.Process;
import at.jku.ce.CoMPArE.process.ProcessElement;
import at.jku.ce.CoMPArE.process.Subject;
import com.vaadin.ui.*;
import org.apache.commons.logging.impl.Log4JLogger;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.Vector;

/**
 * Created by oppl on 09/02/2017.
 */
public class VisualizeModelEvolution extends VerticalLayout {

    Slider slider;
    TabSheet tabSheet;

    Vector<Process> history;

    public VisualizeModelEvolution(Process process, ProcessChangeHistory processChangeHistory) {
        LinkedList<Process> historyList = new LinkedList<>();
        LinkedList<ProcessChangeTransaction> transactions = processChangeHistory.getHistory();
        LinkedList<ProcessChangeTransaction> reverse = new LinkedList<>();
        historyList.addFirst(new Process(process));
        for (ProcessChangeTransaction transaction: transactions) {
            transaction.undo();
            historyList.addFirst(new Process(process));
            reverse.addFirst(transaction);
        }
        for (ProcessChangeTransaction transaction: reverse) {
            transaction.perform();
        }
        history = new Vector<>(historyList);
//        createLayout();
    }

    public VisualizeModelEvolution(Vector<Process> processes) {
        history = sortProcesses(processes);
        createLayout();
    }

    private Vector<Process> sortProcesses(Vector<Process> processes) {
        LinkedList<Process> sorted = new LinkedList<>();
        for (Process p:processes) {
            if (sorted.size()==0) sorted.addFirst(p);
            else {
                int index = 0;
                for (Process a:sorted) {
                    if (!p.getTimestamp().after(a.getTimestamp())) {
                        sorted.add(index, p);
                        break;
                    }
                    index++;
                }
                if (index == sorted.size()) sorted.addLast(p);
            }
        }
        return new Vector<>(sorted);
    }

    public void createLayout() {
        this.removeAllComponents();
        if (history == null) return;
        tabSheet = new TabSheet();
        int count = 1;
        Process previous = null;
        for (Process p: history) {
            Panel panel = new Panel(createTabForProcess(p,previous));
            previous = p;
            panel.setHeight((UI.getCurrent().getPage().getBrowserWindowHeight()-100)+"px");
            tabSheet.addTab(panel,""+count);
            count++;
        }
        this.addComponent(tabSheet);
        this.setWidth((UI.getCurrent().getPage().getBrowserWindowWidth()-200)+"px");
        this.setHeight((UI.getCurrent().getPage().getBrowserWindowHeight()-20)+"px");
        this.setMargin(true);
        this.setSpacing(true);

    }

    private GridLayout createTabForProcess(Process p, Process previous) {
        GridLayout gl = null;

        ModelDiff diff = null;
        if (previous != null) diff = new ModelDiff(previous,p);
        int availableWidth = UI.getCurrent().getPage().getBrowserWindowWidth()-200;
        int numberOfSubjects = p.getSubjects().size()+1;
        int widthOfColumns = (availableWidth-170) / 3;
        int numberOfRows = numberOfSubjects / 4;

        gl = new GridLayout(3,numberOfRows);

        Panel panel = new Panel("Interaction");
        panel.setWidth(widthOfColumns+"px");
        panel.setHeight("350px");
        VisualizeModel model = new VisualizeModel("Interaction",null, widthOfColumns,300);
        model.showSubjectInteraction(p);
        panel.setContent(model);
        gl.addComponent(panel);

        for (Subject s: p.getSubjects()) {
            panel = new Panel(s.toString());
            panel.setWidth(widthOfColumns+"px");
            panel.setHeight("350px");
            model = new VisualizeModel(s.toString(),null, widthOfColumns, 300);
            Set toBeMarked = new HashSet();
            if (diff != null) {
                toBeMarked.addAll(diff.getAddedStates());
                toBeMarked.addAll(diff.getAddedTransitions());
            }

            model.showSubject(s,toBeMarked);
/*            if (diff != null) {
                model.markProcessElements(diff.getAddedStates(),true);
                model.markProcessElements(diff.getAddedTransitions(),true);
            }
*/            panel.setContent(model);
            gl.addComponent(panel);
        }
        gl.setMargin(true);
        gl.setSpacing(true);
        return gl;
    }


}
