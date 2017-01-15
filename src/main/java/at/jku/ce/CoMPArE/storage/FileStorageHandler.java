package at.jku.ce.CoMPArE.storage;

import at.jku.ce.CoMPArE.CoMPArEUI;
import at.jku.ce.CoMPArE.LogHelper;
import at.jku.ce.CoMPArE.process.Process;
import com.vaadin.server.VaadinService;
import sun.rmi.runtime.Log;

import javax.servlet.http.Cookie;
import java.io.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.Vector;

/**
 * Created by oppl on 14/01/2017.
 */
public class FileStorageHandler {

    File baseDirectory;
    Vector<Process> storageBuffer;
    String groupID;
    int counter;


    public FileStorageHandler() {
        baseDirectory = new File(CoMPArEUI.CoMPArEServlet.getResultFolderName());
        storageBuffer = new Vector<>();
        groupID = "anonymous-"+ UUID.randomUUID().toString().substring(0,7);
        isIDCookieAvailable();
        this.counter = getIntitalValueForCounter(groupID);
    }

    public void addProcessToStorageBuffer(Process process) {
        process.setTimestampToNow();
        storageBuffer.add(new Process(process)); // use deep copy constructor to avoid influences of future changes
    }

    public void setGroupID(String id) {
        groupID = id;
        this.counter = getIntitalValueForCounter(groupID);
        updateIDCookie();
    }

    public boolean isIDCookieAvailable() {
        Cookie[] cookies = VaadinService.getCurrentRequest().getCookies();

        // Iterate to find cookie by its name
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("groupid")) {
                groupID = cookie.getValue();
                return true;
            }
        }
        return false;
    }

    private void updateIDCookie() {
        Cookie groupCookie = new Cookie("groupid", groupID);
// Make cookie expire in 2 hours
        groupCookie.setMaxAge(7200);
// Set the cookie path.
        groupCookie.setPath(VaadinService.getCurrentRequest().getContextPath());
// Save cookie
        VaadinService.getCurrentResponse().addCookie(groupCookie);
    }

    public void saveToServer() {
        XMLStore xmlStore = new XMLStore();
        File groupBaseDir = null;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        groupBaseDir = new File(baseDirectory,groupID + "_" + dtf.format(LocalDateTime.now())+"_"+counter);
        if (!groupBaseDir.exists()) {
            groupBaseDir.mkdir();
        }

        for (Process process:storageBuffer) {
            String processName = process.toString();
            dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
            LocalDateTime timestamp = LocalDateTime.ofInstant(process.getTimestamp().toInstant(), ZoneId.of("GMT+1"));
            String fileName = new String(groupID + "_" + processName.replace(" ", "_") + "_" + dtf.format(timestamp) + ".xml");

            Writer writer = null;
            File f;
            try {
                LogHelper.logInfo("XMLStore: storing process " + processName + " to " + groupBaseDir.getName() + "/" + fileName);
                f = new File(groupBaseDir, fileName);
                if (!f.exists()) {
                    writer = new BufferedWriter(new FileWriter(f));
                    writer.write(xmlStore.convertToXML(process));
                }
            } catch (IOException e) {
                LogHelper.logError("XMLStore: storing failed");
            } finally {
                try {
                    if (writer != null)
                        writer.close();
                } catch (IOException e) {
                }
            }
        }
        storageBuffer.removeAllElements();
    }

    public void newProcessStarted() {
        counter++;
    }

    private int getIntitalValueForCounter(String groupID) {
        File groupBaseDir = null;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        int counter = 1;
        while (true) {
            groupBaseDir = new File(baseDirectory,groupID + "_" + dtf.format(LocalDateTime.now())+"_"+counter);
            if (!groupBaseDir.exists()) {
                return counter;
            }
            else counter++;
        }
    }
}
