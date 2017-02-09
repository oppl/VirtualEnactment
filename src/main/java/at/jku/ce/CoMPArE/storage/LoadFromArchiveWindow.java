package at.jku.ce.CoMPArE.storage;

import at.jku.ce.CoMPArE.CoMPArEUI;
import at.jku.ce.CoMPArE.ProcessSelectorUI;
import at.jku.ce.CoMPArE.process.Process;
import at.jku.ce.CoMPArE.visualize.VisualizeModelEvolution;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.*;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by oppl on 15/01/2017.
 */
public class LoadFromArchiveWindow extends Window {

    ProcessSelectorUI manager;
    Table table;
    VerticalLayout layout;
    FileStorageHandler fileStorageHandler;

    public LoadFromArchiveWindow(ProcessSelectorUI manager) {
        super("Load process files from archive");
        this.center();
        this.setWidth("90%");
        fileStorageHandler = new FileStorageHandler();
        layout = new VerticalLayout();
        layout.setWidth("100%");
        layout.setMargin(true);
        layout.setSpacing(true);
        this.manager = manager;
        buildTable();
        setContent(layout);
    }

    private void buildTable() {
        if (!fileStorageHandler.isIDCookieAvailable()) {
            Window w = new GroupIDEntryWindow(fileStorageHandler);
            w.addCloseListener(e-> {
                buildTable();
            });
            manager.getUI().addWindow(w);
        }
        else {
            List<File> resultFiles = loadResults(new File(CoMPArEUI.CoMPArEServlet.getResultFolderName()), fileStorageHandler.getGroupID());
            if (table != null) layout.removeComponent(table);

            table = new Table("Available Results");
            table.addContainerProperty("ResultName", String.class, null);
            table.addContainerProperty("ResultDate", String.class, null);
            table.addContainerProperty("ButtonShow", Button.class, null);
            table.addContainerProperty("ButtonDownload", Button.class, null);
            table.addContainerProperty("SortDate", Long.class, null);
            table.setSortContainerPropertyId("SortDate");
            table.setSortAscending(false);
            table.setWidth("90%");

            table.setColumnWidth("ResultDate", 200);
            table.setColumnWidth("ButtonShow", 150);
            table.setColumnWidth("ButtonDownload", 150);
            table.setColumnHeaderMode(Table.ColumnHeaderMode.HIDDEN);
            table.setColumnAlignment("ButtonShow", Table.Align.CENTER);
            table.setColumnAlignment("ButtonDownload", Table.Align.CENTER);

            int itemID = 0;
            //          LogHelper.logInfo("ResultView: "+resultFiles.size()+" results available.");

            for (File result : resultFiles) {

                Button showButton = new Button("show content");
                showButton.addClickListener(e -> {
                    this.getUI().addWindow(new ShowFolderContentWindow(result));
                });


                Button loadButton = new Button("load");
                loadButton.addClickListener(e -> {
                    this.close();
                    Vector<Process> processes = loadProcessesFromFolder(result);
                    manager.setSelectedProcess(getLatestProcess(processes));
                });


                BasicFileAttributes attr = null;
                try {
                    attr = Files.readAttributes(result.toPath(), BasicFileAttributes.class);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (attr == null) continue;
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
                LocalDateTime creationTime = LocalDateTime.ofInstant(attr.lastModifiedTime().toInstant(), ZoneId.of("GMT+1"));
                table.addItem(new Object[]{
                        result.getName(),
                        dtf.format(creationTime),
                        showButton,
                        loadButton,
                        new Long(attr.lastModifiedTime().toMillis())
                }, itemID);
//                LogHelper.logInfo("ResultView: added "+result.getName());
                itemID++;
            }
            table.sort();
            table.setVisibleColumns(new String[]{"ResultName", "ResultDate", "ButtonShow", "ButtonDownload"});
            table.setPageLength(table.size());
            layout.addComponent(table);
            Button close = new Button("Close");
            close.addClickListener( e-> {
                this.close();
            });
            layout.addComponent(close);

        }
    }

    private List<File> loadResults(File containingFolder, String groupID) {

        assert containingFolder.exists() && containingFolder.isDirectory();

        File[] files = containingFolder.listFiles(f -> f.isDirectory()
                && f.getName().startsWith(groupID));

//            LogHelper.logInfo("Found "+files.length+" results");
        return Arrays.asList(files);
    }

    private Vector<Process> loadProcessesFromFolder(File folder) {
        assert folder.exists() && folder.isDirectory();

        File[] files = folder.listFiles(f -> f.isFile()
                && f.getName().endsWith(".xml"));

//                LogHelper.logInfo("Found "+files.length+" results");
        List<File> processFiles = Arrays.asList(files);

        XMLStore xmlStore = new XMLStore();
        Vector<Process> processes = new Vector<>();
        for (File processFile:processFiles) {
            Process p = xmlStore.readXML(processFile);
            if (p != null) processes.add(p);
        }
        return processes;
    }

    private Process getLatestProcess(Vector<Process> processes) {
        Process latest = null;
        for (Process p:processes) {
            if (latest == null) latest = p;
            if (!latest.getTimestamp().after(p.getTimestamp())) latest = p;
        }
        return latest;
    }

    private class ShowFolderContentWindow extends Window {

        public ShowFolderContentWindow(File folder) {
            super("Contained Process Files");
            VerticalLayout vLayout = new VerticalLayout();
            this.center();
            this.setWidth("500px");
            vLayout = new VerticalLayout();
            vLayout.setWidth("100%");
            vLayout.setMargin(true);
            vLayout.setSpacing(true);
            vLayout.addComponent(createTable(folder));
            HorizontalLayout hLayout = new HorizontalLayout();
            hLayout.setSpacing(true);
            vLayout.addComponent(hLayout);
            Button visualize = new Button("Show Details");
            visualize.addClickListener( e -> {
                Vector<Process> processes = loadProcessesFromFolder(folder);
                Window w = new Window();
                w.setContent(new VisualizeModelEvolution(processes));
                w.center();
                this.getUI().addWindow(w);
            });
            Button close = new Button("Close");
            close.addClickListener( e-> {
                this.close();
            });
            hLayout.addComponent(visualize);
            hLayout.addComponent(close);
            setContent(vLayout);

        }

        private Table createTable(File folder) {
            Table table = new Table("Contained Results");
            table.addContainerProperty("ResultName", String.class, null);
            table.addContainerProperty("SortDate",Long.class,null);
            table.setSortContainerPropertyId("SortDate");
            table.setSortAscending(false);
            table.setWidth("90%");

            table.setColumnHeaderMode(Table.ColumnHeaderMode.HIDDEN);

            assert folder.exists() && folder.isDirectory();

            File[] files = folder.listFiles(f -> f.isFile()
                    && f.getName().endsWith(".xml"));

//                LogHelper.logInfo("Found "+files.length+" results");
            List<File> resultFiles = Arrays.asList(files);

            int itemID = 0;
//                LogHelper.logInfo("ResultView: "+resultFiles.size()+" results available.");

            for (File result: resultFiles) {

                BasicFileAttributes attr = null;
                try {
                    attr = Files.readAttributes(result.toPath(), BasicFileAttributes.class);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (attr == null) continue;
                table.addItem(new Object[]{
                        result.getName(),
                        new Long(attr.lastModifiedTime().toMillis())
                }, itemID);
//                    LogHelper.logInfo("ResultView: added "+result.getName());
                itemID++;
            }
            table.sort();
            table.setVisibleColumns(new String[] { "ResultName"});
            table.setPageLength(Math.min(table.size(),5));

            return table;
        }

    }

}
