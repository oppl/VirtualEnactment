package at.jku.ce.CoMPArE.storage;

import at.jku.ce.CoMPArE.scaffolding.scaffolds.ExpandingScaffold;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;

/**
 * Created by oppl on 15/01/2017.
 */
public class GroupIDEntryWindow extends Window {
    GridLayout gLayout = new GridLayout(2,4);

    private FileStorageHandler fileStorageHandler;

    public GroupIDEntryWindow(FileStorageHandler fileStorageHandler) {
        super("Enter ID");
        this.fileStorageHandler = fileStorageHandler;
        this.center();
        gLayout.setWidth("100%");
        this.setWidth("400px");
        Label titleLabel = new Label("<b>Please enter a unique name for this session:</b>", ContentMode.HTML);
        Label descrLabel = new Label("This name will be used to identify you results on the server and when downloading them.", ContentMode.HTML);
        TextField name = new TextField("Name:");
        Button close = new Button("Close");
        Button enter = new Button("OK");

        close.addClickListener( e -> {
            this.close();
        });

        enter.addClickListener( e -> {
            fileStorageHandler.setGroupID(name.getValue());
            this.close();
        });

        gLayout.addComponent(titleLabel,0,0,1,0);
        gLayout.addComponent(descrLabel,0,1,1,1);
        gLayout.addComponent(name,0,2,1,2);
        gLayout.addComponent(close,0,3);
        gLayout.addComponent(enter,1,3);

        gLayout.setRowExpandRatio(1,1);

        gLayout.setMargin(true);
        gLayout.setSpacing(true);
        setContent(gLayout);
    }

}