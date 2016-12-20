package at.jku.ce.CoMPArE.scaffolding.agents;

import at.jku.ce.CoMPArE.execute.Instance;
import at.jku.ce.CoMPArE.process.Process;
import at.jku.ce.CoMPArE.process.State;
import at.jku.ce.CoMPArE.scaffolding.ScaffoldingManager;
import com.vaadin.ui.Notification;

/**
 * Created by oppl on 20/12/2016.
 */
public class OnboardingAgent extends ScaffoldingAgent {

    public OnboardingAgent(Process p, ScaffoldingManager manager) {
        super(p, manager);
        this.freq = ScaffoldingAgent.FREQ_EACHSTEP;
    }

    @Override
    public void init() {
        super.init();

    }

    @Override
    public void updateScaffolds(Instance currentInstance, State finishedState) {
        super.updateScaffolds(currentInstance, finishedState);
    }
}
