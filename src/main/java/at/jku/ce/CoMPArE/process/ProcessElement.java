package at.jku.ce.CoMPArE.process;

import java.util.UUID;

/**
 * Created by oppl on 14/01/2017.
 */
public class ProcessElement {

    private UUID uuid;

    public ProcessElement() {
        uuid = UUID.randomUUID();
    }

    public ProcessElement(ProcessElement p) {
        uuid = p.getUUID();
    }

    public UUID getUUID() {
        return uuid;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ProcessElement)) return false;
        if (((ProcessElement) obj).getUUID().equals(uuid)) return true;
        return false;
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}
