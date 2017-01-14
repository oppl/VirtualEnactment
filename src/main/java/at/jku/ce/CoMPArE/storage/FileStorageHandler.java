package at.jku.ce.CoMPArE.storage;

import at.jku.ce.CoMPArE.CoMPArEUI;
import at.jku.ce.CoMPArE.LogHelper;
import at.jku.ce.CoMPArE.process.Process;
import com.vaadin.server.VaadinService;

import javax.servlet.http.Cookie;
import java.io.*;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Vector;

/**
 * Created by oppl on 14/01/2017.
 */
public class FileStorageHandler {

    File baseDirectory;
    Vector<Process> storageBuffer;
    String groupID;


    public FileStorageHandler() {
        baseDirectory = new File(CoMPArEUI.CoMPArEServlet.getResultFolderName());
        storageBuffer = new Vector<>();
        groupID = "anonymous";
        checkForAvailableIDCookie();
    }

    public void addProcessToStorageBuffer(Process process) {
        process.setTimestampToNow();
        storageBuffer.add(process);
    }

    public void setGroupID(String id) {
        groupID = id;
        updateIDCookie();
    }

    public boolean checkForAvailableIDCookie() {
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
        int counter = 1;
        while (groupBaseDir == null) {
            File testBaseDir = new File(baseDirectory,groupID + "_" + dtf.format(LocalDateTime.now())+"_"+counter);
            if (!testBaseDir.exists()) {
                groupBaseDir = testBaseDir;
                groupBaseDir.mkdir();
            }
            else counter++;
        }

        for (Process process:storageBuffer) {
            String processName = process.toString();
            dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
            LocalDateTime timestamp = LocalDateTime.ofInstant(process.getTimestamp().toInstant(), ZoneId.of("GMT+1"));
            String fileName = new String(groupID + "_" + processName.replace(" ", "_") + "_" + dtf.format(timestamp) + ".xml");

            Writer writer = null;
            File f;
            try {
                LogHelper.logInfo("XMLStore: storing process " + processName + " to " + System.getProperty("catalina.base") + "/" + fileName);
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
    }
}
