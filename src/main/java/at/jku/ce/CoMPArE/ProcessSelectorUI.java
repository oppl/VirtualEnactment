package at.jku.ce.CoMPArE;

import at.jku.ce.CoMPArE.process.Message;
import at.jku.ce.CoMPArE.process.Process;
import at.jku.ce.CoMPArE.process.State;
import at.jku.ce.CoMPArE.process.Subject;
import com.vaadin.ui.*;

/**
 * Created by oppl on 26/11/2016.
 */
public class ProcessSelectorUI extends Window {
    FormLayout fLayout = new FormLayout();

    private Process selectedProcess;

    public ProcessSelectorUI() {
        super("Select a new Process");
        this.setWidth("900px");
        this.setHeight("500px");
        this.center();
        fLayout.setMargin(true);
        fLayout.setSpacing(true);
        setContent(fLayout);
        selectedProcess = null;
    }

    public void showProcessSelector() {

        final Button confirm = new Button("Done");
        final Label questionPrompt = new Label("Select a new process to be explored:");

        final TextField inputField = new TextField("How should it be named?");

        final OptionGroup availableDemoProcesses = new OptionGroup("Please select:");
        for (Process p : DemoProcess.getDemoProcesses()) {
            availableDemoProcesses.addItem(p);
        }
        final String optionSpecifyMyself = new String("I want to start a new process from scratch.");
        availableDemoProcesses.addItem(optionSpecifyMyself);
        availableDemoProcesses.addValueChangeListener( e -> {
            Object selectedItem = e.getProperty().getValue();
            if (selectedItem == optionSpecifyMyself) {
                inputField.setEnabled(true);
            }
            else {
                inputField.setEnabled(false);
            }
        });

        confirm.addClickListener( e -> {
            if (inputField.isEnabled()) {
                selectedProcess = new Process(inputField.getValue());
                this.close();
            }
            else {
                selectedProcess = (Process) availableDemoProcesses.getValue();
                this.close();
            }
        });

        fLayout.removeAllComponents();
        fLayout.addComponent(questionPrompt);
        fLayout.addComponent(availableDemoProcesses);
        fLayout.addComponent(inputField);
        inputField.setEnabled(false);
        fLayout.addComponent(confirm);
    }

    public Process getSelectedProcess() {
        return selectedProcess;
    }
}
