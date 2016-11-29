package at.jku.ce.CoMPArE.storage;

import at.jku.ce.CoMPArE.LogHelper;
import at.jku.ce.CoMPArE.process.*;
import at.jku.ce.CoMPArE.process.Process;
import com.thoughtworks.xstream.XStream;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Button;
import sun.rmi.runtime.Log;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by oppl on 29/11/2016.
 */
public class XMLStore {

    XStream xStream;
    FileDownloader fileDownloader;

    public XMLStore() {
        xStream = new XStream();
        xStream.alias("process", Process.class);
        xStream.alias("subject", Subject.class);

        xStream.alias("state", State.class);
        xStream.alias("actionstate", ActionState.class);
        xStream.alias("recvstate", RecvState.class);
        xStream.alias("sendstate", SendState.class);

        xStream.alias("message", Message.class);

        xStream.alias("condition", Condition.class);
        xStream.alias("messagecondition", MessageCondition.class);
    }


    public String convertToXML(Process p) {
        String xml = xStream.toXML(p);
        return xml;
    }

    public String saveToServerFile(String processName, String xml) {
        LogHelper.logInfo("XMLStore: created XML: " + xml);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
        LocalDateTime now = LocalDateTime.now();
        String fileName = new String(processName.replace(" ", "_") + "_" + dtf.format(now) + ".xml");

        Writer writer = null;
        File f = null;
        try {
            LogHelper.logInfo("XMLStore: storing process " + processName + " to " + System.getProperty("catalina.base") + "/" + fileName);
            f = new File(System.getProperty("catalina.base") + "/" + fileName);
            writer = new BufferedWriter(new FileWriter(f));
            writer.write(xml);
        } catch (IOException e) {
            LogHelper.logError("XMLStore: storing failed");
        } finally {
            try {
                if (writer != null)
                    writer.close();
            } catch (IOException e) {
            }
        }
        return System.getProperty("catalina.base") + "/" + fileName;

    }

    public Process readXML(File f) {
        Process p = null;
        String xml = null;

        try {
            byte[] encoded = Files.readAllBytes(Paths.get(f.getAbsolutePath()));
            xml = new String(encoded, Charset.defaultCharset());
        }
        catch (IOException e) {
            LogHelper.logError("XMLStore: reading failed");
        }
        if (xml != null) {
            p = (Process) xStream.fromXML(xml);
        }
        if (p!=null) LogHelper.logInfo("XMLStore: process read successfully");
        return p;
    }
}
