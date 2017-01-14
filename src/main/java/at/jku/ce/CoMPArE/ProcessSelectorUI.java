package at.jku.ce.CoMPArE;

import at.jku.ce.CoMPArE.process.Message;
import at.jku.ce.CoMPArE.process.Process;
import at.jku.ce.CoMPArE.process.State;
import at.jku.ce.CoMPArE.process.Subject;
import at.jku.ce.CoMPArE.storage.XMLStore;
import com.vaadin.server.Page;
import com.vaadin.ui.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Created by oppl on 26/11/2016.
 */
public class ProcessSelectorUI extends Window implements Upload.Receiver, Upload.SucceededListener {
    FormLayout fLayout = new FormLayout();
    File file;

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
        final Upload upload = new Upload("Select file",this);
        upload.addSucceededListener(this);
        final TextField inputField = new TextField("How should it be named?");

        final OptionGroup availableDemoProcesses = new OptionGroup("Please select:");
        for (Process p : DemoProcess.getDemoProcesses()) {
            availableDemoProcesses.addItem(p);
        }
        final String optionSpecifyMyself = new String("I want to start a new process from scratch.");
        final String optionUpload = new String("I want to upload a process file.");
        availableDemoProcesses.addItem(optionSpecifyMyself);
        availableDemoProcesses.addItem(optionUpload);
        availableDemoProcesses.addValueChangeListener( e -> {
            Object selectedItem = e.getProperty().getValue();
            if (selectedItem == optionSpecifyMyself) {
                inputField.setEnabled(true);
            }
            else {
                inputField.setEnabled(false);
            }
        });

        availableDemoProcesses.addValueChangeListener( e -> {
            Object selectedItem = e.getProperty().getValue();
            if (selectedItem == optionUpload) {
                confirm.setCaption("Upload");
                upload.setVisible(true);
                inputField.setVisible(false);
            }
            else {
                confirm.setCaption("Done");
                upload.setVisible(false);
                inputField.setVisible(true);
            }
        });

        confirm.addClickListener( e -> {
            if (inputField.isEnabled()) {
                selectedProcess = new Process(inputField.getValue());
                this.close();
            }
            else {
                if (availableDemoProcesses.getValue() != optionUpload) {
                    selectedProcess = (Process) availableDemoProcesses.getValue();
                    this.close();
                }
            }
        });

        fLayout.removeAllComponents();
        fLayout.addComponent(questionPrompt);
        fLayout.addComponent(availableDemoProcesses);
        fLayout.addComponent(inputField);
        fLayout.addComponent(upload);
        inputField.setEnabled(false);
        upload.setVisible(false);
        fLayout.addComponent(confirm);
    }

    public Process getSelectedProcess() {
        return selectedProcess;
    }

    @Override
    public OutputStream receiveUpload(String filename, String mimeType) {
        FileOutputStream fos = null; // Stream to write to
        try {
            // Open the file for writing.
            file = new File(System.getProperty("catalina.base") + "/" + filename);
            fos = new FileOutputStream(file);

        } catch (final java.io.FileNotFoundException e) {
            new Notification("Could not open file<br/>",
                    e.getMessage(),
                    Notification.Type.ERROR_MESSAGE)
                    .show(Page.getCurrent());
            return null;
        }
        return fos; // Return the output stream to write to
    }

    public void uploadSucceeded(Upload.SucceededEvent event) {
        XMLStore xmlStore = new XMLStore();
        Process p = xmlStore.readXML(file);
        p.reconstructParentRelations();
        if (p!=null) selectedProcess = p;
        this.close();
    }

}
